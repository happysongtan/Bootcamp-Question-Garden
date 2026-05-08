const classNames = ["21반", "22반", "23반", "24반", "25반", "26반", "27반", "28반", "29반"];

const flowerOptions = [
    { type: "sunflower",      name: "해바라기" },
    { type: "tulip",          name: "튤립" },
    { type: "cherry-blossom", name: "벚꽃" },
    { type: "dandelion",      name: "민들레" },
    { type: "lavender",       name: "라벤더" },
    { type: "cosmos",         name: "코스모스" },
    { type: "rose",           name: "장미" },
    { type: "hydrangea",      name: "수국" },
    { type: "daisy",          name: "데이지" },
    { type: "lily",           name: "백합" },
    { type: "peony",          name: "작약" },
    { type: "camellia",       name: "동백꽃" },
];

const statusLabels = {
    WAITING:  { text: "질문 있어요", className: "waiting" },
    ANSWERED: { text: "어려워요",   className: "answered" },
    SOLVED:   { text: "해결됐어요", className: "solved" },
};

const growthMessages = {
    SEED:       "아직 첫 질문을 기다리고 있어요.",
    SPROUT:     "우리 반 질문이 싹트기 시작했어요.",
    STEM:       "질문과 답변이 꾸준히 쌓이고 있어요.",
    BUD:        "곧 꽃이 필 만큼 활발해졌어요.",
    BLOOMING:   "우리 반 질문 문화가 피어났어요.",
    FULL_BLOOM: "질문과 답변이 풍성하게 모였어요.",
};

const stageAssetNames = {
    SEED: "seed", SPROUT: "sprout", STEM: "stem",
    BUD: "bud", BLOOMING: "blooming", FULL_BLOOM: "full_bloom",
};

const stageThresholds = {
    SEED: 10, SPROUT: 25, STEM: 50, BUD: 80, BLOOMING: 120, FULL_BLOOM: null,
};

const state = { questions: [], activeQuestionId: null };
let classRankings = {};

/* ── DOM refs ── */
const classSelect       = document.querySelector("#classSelect");
const questionList      = document.querySelector("#questionList");
const popularList       = document.querySelector("#popularList");
const questionCount     = document.querySelector("#questionCount");
const keywordInput      = document.querySelector("#keywordInput");
const categorySelect    = document.querySelector("#categorySelect");
const sortSelect        = document.querySelector("#sortSelect");
const questionDialog    = document.querySelector("#questionDialog");
const questionForm      = document.querySelector("#questionForm");
const detailDialog      = document.querySelector("#detailDialog");
const questionDetail    = document.querySelector("#questionDetail");
const toast             = document.querySelector("#toast");
const gardenTitle       = document.querySelector("#gardenTitle");
const growthMessage     = document.querySelector("#growthMessage");
const growthBar         = document.querySelector("#growthBar");
const growthScoreLabel  = document.querySelector("#growthScoreLabel");
const flowerVisual      = document.querySelector("#flowerVisual");
const flowerImage       = document.querySelector("#flowerImage");
const classRankListEl       = document.querySelector("#classRankList");
const popularClassLabel     = document.querySelector("#popularClassLabel");
const reportPasswordDialog  = document.querySelector("#reportPasswordDialog");
const reportPasswordForm    = document.querySelector("#reportPasswordForm");
const reportBoxDialog       = document.querySelector("#reportBoxDialog");
const reportBoxList         = document.querySelector("#reportBoxList");
const flowerSettingsDialog  = document.querySelector("#flowerSettingsDialog");
const flowerSettingsList    = document.querySelector("#flowerSettingsList");
const actQuestions      = document.querySelector("#actQuestions");
const actAnswers        = document.querySelector("#actAnswers");
const actLikes          = document.querySelector("#actLikes");
const actSolved         = document.querySelector("#actSolved");

/* ── Init ── */
initializeClassOptions();
closeOnBackdrop(questionDialog);
closeOnBackdrop(detailDialog);
closeOnBackdrop(reportPasswordDialog);
closeOnBackdrop(reportBoxDialog);
if (flowerSettingsDialog) closeOnBackdrop(flowerSettingsDialog);

document.querySelector("[data-open-report-box]").addEventListener("click", () => {
    reportPasswordForm.reset();
    reportPasswordDialog.showModal();
});
document.querySelector("[data-close-report-pw]").addEventListener("click", () => reportPasswordDialog.close());
document.querySelector("[data-close-report-box]").addEventListener("click", () => reportBoxDialog.close());

reportPasswordForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const password = reportPasswordForm.elements.password.value;
    try {
        const questions = await request(`/api/reports?password=${encodeURIComponent(password)}`);
        reportPasswordDialog.close();
        renderReportBox(questions, password);
        reportBoxDialog.showModal();
    } catch {
        /* 오류 토스트는 request()가 이미 표시 */
    }
});

document.querySelector("[data-open-flower-settings]").addEventListener("click", async () => {
    try {
        await openFlowerSettings();
    } catch {
        /* request()가 이미 토스트를 표시 */
    }
});
document.querySelector("[data-close-flower-settings]").addEventListener("click", () => flowerSettingsDialog.close());

document.querySelector("[data-open-create]").addEventListener("click", () => {
    questionForm.elements.className.value = classSelect.value;
    questionDialog.showModal();
});
document.querySelector("[data-close-create]").addEventListener("click", () => questionDialog.close());
classSelect.addEventListener("change", refresh);
keywordInput.addEventListener("input", debounce(loadQuestions, 250));
categorySelect.addEventListener("change", loadQuestions);
sortSelect.addEventListener("change", loadQuestions);

questionForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const fd = new FormData(questionForm);
    const selected = fd.get("className");
    await request("/api/questions", {
        method: "POST",
        body: JSON.stringify({
            className: selected,
            title:     fd.get("title").trim(),
            content:   fd.get("content").trim(),
            category:  fd.get("category"),
            nickname:  fd.get("nickname").trim(),
            password:  fd.get("password"),
            tags:      [],
        }),
    });
    questionForm.reset();
    classSelect.value = selected;
    questionForm.elements.className.value = selected;
    questionDialog.close();
    showToast("질문이 등록되었습니다.");
    await refresh();
});

/* ── Data loading ── */
async function refresh() {
    await Promise.all([loadQuestions(), loadPopular(), loadFlowerGrowth()]);
}

async function loadQuestions() {
    const params = new URLSearchParams();
    params.set("className", classSelect.value);
    if (keywordInput.value.trim()) params.set("keyword", keywordInput.value.trim());
    if (categorySelect.value) params.set("category", categorySelect.value);
    params.set("sort", sortSelect.value);

    state.questions = await request(`/api/questions?${params.toString()}`);
    questionCount.textContent = state.questions.length;
    renderQuestions();
}

async function loadPopular() {
    const questions = await request(
        `/api/questions/popular?${new URLSearchParams({ className: classSelect.value })}`
    );
    popularClassLabel.textContent = `${classSelect.value} · 이번 주 기준`;
    if (questions.length === 0) {
        popularList.innerHTML = "<div class='empty compact'>아직 인기 질문이 없습니다.</div>";
        return;
    }
    popularList.innerHTML = questions.map((q, i) => {
        const rankClass = i === 0 ? "" : i === 1 ? "rank-2" : "rank-3";
        return `
            <article class="popular-card" data-detail-id="${q.id}" tabindex="0" role="button">
                <div class="pop-card-top">
                    <div class="pop-rank-circle ${rankClass}">${i + 1}</div>
                    <h3>${escapeHtml(q.title)}</h3>
                </div>
                <div class="pop-stats">
                    <span>♡ ${q.likeCount}</span>
                    <span>💬 ${q.answerCount}</span>
                </div>
            </article>
        `;
    }).join("");
    popularList.querySelectorAll("[data-detail-id]").forEach(bindOpenDetailInteraction);
}

async function loadFlowerGrowth() {
    const g = await request(`/api/classes/${encodeURIComponent(classSelect.value)}/flower`);
    gardenTitle.textContent = `${g.className}의 ${g.flowerName}`;
    growthMessage.textContent = growthMessages[g.growthStage] || "꽃을 키우는 중입니다.";
    growthBar.style.width = `${g.growthPercent}%`;

    const nextThreshold = stageThresholds[g.growthStage];
    growthScoreLabel.textContent = nextThreshold
        ? `${g.growthScore}점 · 다음 단계까지 ${nextThreshold - g.growthScore}점`
        : `${g.growthScore}점 · 최고 단계 달성! 🎉`;

    const stageAsset = stageAssetNames[g.growthStage] || "seed";
    flowerVisual.className = `flower-visual ${g.flowerType} stage-${stageAsset}`;
    flowerImage.src = `/images/flowers/${g.flowerType}-${stageAsset}.png`;
    flowerImage.alt = `${g.className} ${g.flowerName} ${g.growthStageName} 단계`;

    if (actQuestions) actQuestions.textContent = g.questionCount;
    if (actAnswers)   actAnswers.textContent   = g.answerCount;
    if (actLikes)     actLikes.textContent     = g.likeCount;
    if (actSolved)    actSolved.textContent    = g.solvedCount;

    renderClassRankList();
}

/* ── Class ranking ── */
async function computeAndCacheRankings() {
    const results = await Promise.allSettled(
        classNames.map((cn) =>
            fetch(`/api/classes/${encodeURIComponent(cn)}/flower`)
                .then((r) => (r.ok ? r.json() : null))
                .catch(() => null)
        )
    );
    const valid = results
        .filter((r) => r.status === "fulfilled" && r.value)
        .map((r) => r.value);
    valid.sort((a, b) => b.growthScore - a.growthScore);
    classRankings = {};
    valid.forEach((r, i) => { classRankings[r.className] = i + 1; });
    renderClassRankList();
}

function renderClassRankList() {
    if (!classRankListEl || Object.keys(classRankings).length === 0) return;
    const sorted = Object.entries(classRankings)
        .sort((a, b) => a[1] - b[1])
        .slice(0, 4);

    classRankListEl.innerHTML = sorted.map(([cn, rank]) => {
        const trophy = rank === 1 ? "🥇" : rank === 2 ? "🥈" : rank === 3 ? "🥉" : "  ";
        const isActive = cn === classSelect.value;
        return `
            <div class="rank-item ${isActive ? "active" : ""}">
                <span class="rank-class">${cn}</span>
                <span class="rank-badge-sm">
                    <span class="trophy">${trophy}</span>${rank}위
                </span>
            </div>
        `;
    }).join("");
}

/* ── Render questions ── */
function renderQuestions() {
    if (state.questions.length === 0) {
        questionList.innerHTML = `
            <div class="empty">검색 결과가 없습니다.<br>이 반의 첫 질문을 등록해 보세요.</div>
        `;
        return;
    }
    questionList.innerHTML = state.questions.map((q) => {
        const status = statusLabels[q.status] || statusLabels.WAITING;
        const isSolved = q.status === "SOLVED";
        return `
            <article class="question-row" data-detail-id="${q.id}" tabindex="0" role="button">
                <span class="badge ${status.className}">${status.text}</span>
                <h3 class="row-title">${escapeHtml(q.title)}</h3>
                <div class="row-meta">
                    <span class="user-icon">👤</span>
                    <span>${escapeHtml(q.nickname)}</span>
                    <span>·</span>
                    <span>${formatRelativeDate(q.createdAt)}</span>
                </div>
                <div class="row-stats">
                    <span>♡ ${q.likeCount}</span>
                    <span>💬 ${q.answerCount}</span>
                </div>
                <span class="row-solved-icon ${isSolved ? "solved" : ""}" title="${isSolved ? "해결됨" : "미해결"}">✓</span>
            </article>
        `;
    }).join("");
    questionList.querySelectorAll(".question-row[data-detail-id]").forEach(bindOpenDetailInteraction);
}

/* ── Detail view ── */
async function openDetail(questionId) {
    state.activeQuestionId = Number(questionId);
    const q = await request(`/api/questions/${questionId}`);
    const status = statusLabels[q.status] || statusLabels.WAITING;

    questionDetail.innerHTML = `
        <div class="detail-content">
            <div class="dialog-header">
                <h2>${escapeHtml(q.title)}</h2>
                <button class="ghost-button" type="button" data-close-detail>닫기</button>
            </div>
            <section class="question-body">
                <div class="badge-row">
                    <span class="badge class-badge">${escapeHtml(q.className)}</span>
                    <span class="badge category-badge">${escapeHtml(q.category)}</span>
                    <span class="badge ${status.className}">${status.text}</span>
                </div>
                <div class="meta-row">
                    ${escapeHtml(q.nickname)} · ${formatDate(q.createdAt)} · 공감 ${q.likeCount} · 조회 ${q.viewCount}
                </div>
                <p>${escapeHtml(q.content)}</p>
                <div class="actions">
                    <button class="secondary-button" type="button" data-detail-like="${q.id}">
                        ${hasLiked(q.id) ? "♡ 공감중" : "♡ 나도 궁금해요"}
                    </button>
                </div>
            </section>

            <section class="answer-section">
                <div class="section-heading">
                    <h3>답변 ${q.answers.length}개</h3>
                </div>
                <div class="answer-list">
                    ${q.answers.length
                        ? [...q.answers]
                            .sort((a, b) => (b.adopted ? 1 : 0) - (a.adopted ? 1 : 0))
                            .map((a) => renderAnswer(a, q.status, q.id))
                            .join("")
                        : "<div class='empty compact'>아직 답변이 없습니다.</div>"}
                </div>
            </section>

            <form class="answer-form" data-answer-form>
                <h3>답변 작성</h3>
                <label>답변 내용<textarea name="content" required minlength="5"></textarea></label>
                <div class="form-grid">
                    <label>닉네임<input name="nickname" placeholder="익명 답변자"></label>
                    <label>비밀번호<input name="password" type="password" required minlength="4"></label>
                </div>
                <button class="primary-button" type="submit">답변 등록</button>
            </form>

            <form class="password-form" data-solve-form>
                <label>
                    질문 비밀번호
                    <input name="password" type="password" required minlength="4" placeholder="해결 완료로 바꾸기">
                </label>
                <button class="ghost-button" type="submit">해결 완료 표시</button>
            </form>

            <form class="password-form" data-delete-form>
                <label>
                    질문 비밀번호
                    <input name="password" type="password" required minlength="4" placeholder="삭제하려면 비밀번호 입력">
                </label>
                <button class="danger-button" type="submit">질문 삭제</button>
            </form>

            <div class="report-action">
                <button class="report-btn" type="button" data-report-btn>🚨 이 글 신고하기</button>
            </div>
        </div>
    `;

    questionDetail.querySelector("[data-close-detail]").addEventListener("click", () => detailDialog.close());
    questionDetail.querySelector("[data-detail-like]").addEventListener("click", () => toggleLike(q.id, true));
    questionDetail.querySelector("[data-answer-form]").addEventListener("submit", submitAnswer);
    questionDetail.querySelector("[data-solve-form]").addEventListener("submit", solveQuestion);
    questionDetail.querySelector("[data-delete-form]").addEventListener("submit", deleteQuestion);
    questionDetail.querySelector("[data-report-btn]").addEventListener("click", () => {
        if (confirm("이 글을 신고하시겠습니까? 신고된 글은 관리자 검토 전까지 숨겨집니다.")) {
            reportQuestion(q.id);
        }
    });

    const bindToggle = (toggleSel, formSel, cancelSel) => {
        questionDetail.querySelectorAll(toggleSel).forEach((btn) => {
            const form = questionDetail.querySelector(`[${formSel}="${btn.dataset[Object.keys(btn.dataset)[0]]}"]`);
            btn.addEventListener("click", () => {
                form.style.display = form.style.display === "none" ? "flex" : "none";
            });
        });
        questionDetail.querySelectorAll(cancelSel).forEach((btn) => {
            const key = Object.keys(btn.dataset)[0];
            const form = questionDetail.querySelector(`[${formSel}="${btn.dataset[key]}"]`);
            btn.addEventListener("click", () => { form.style.display = "none"; });
        });
    };

    bindToggle("[data-adopt-toggle]",   "data-adopt-form",   "[data-adopt-cancel]");
    bindToggle("[data-unadopt-toggle]", "data-unadopt-form", "[data-unadopt-cancel]");

    questionDetail.querySelectorAll("[data-adopt-form]").forEach((form) => {
        form.addEventListener("submit", (e) => {
            e.preventDefault();
            adoptAnswer(q.id, form.dataset.adoptForm, form.querySelector("input[name='password']").value);
        });
    });
    questionDetail.querySelectorAll("[data-unadopt-form]").forEach((form) => {
        form.addEventListener("submit", (e) => {
            e.preventDefault();
            unadoptAnswer(q.id, form.dataset.unadoptForm, form.querySelector("input[name='password']").value);
        });
    });
    if (!detailDialog.open) detailDialog.showModal();
}

function renderAnswer(answer, questionStatus, questionId) {
    const isAdopted  = answer.adopted;
    const canAdopt   = !isAdopted && questionStatus !== "SOLVED";
    const canUnadopt = isAdopted;
    return `
        <article class="answer-item ${isAdopted ? "adopted" : ""}">
            ${isAdopted ? `<div class="adopted-badge">✅ 채택된 답변</div>` : ""}
            <p>${escapeHtml(answer.content)}</p>
            <div class="answer-meta-row">
                <span class="meta-row">${escapeHtml(answer.nickname)} · ${formatDate(answer.createdAt)}</span>
                <span>
                    ${canAdopt   ? `<button class="adopt-toggle-btn" type="button" data-adopt-toggle="${answer.id}">채택하기</button>` : ""}
                    ${canUnadopt ? `<button class="adopt-toggle-btn unadopt" type="button" data-unadopt-toggle="${answer.id}">채택 취소</button>` : ""}
                </span>
            </div>
            ${canAdopt ? `
                <form class="adopt-form" data-adopt-form="${answer.id}" style="display:none;">
                    <input name="password" type="password" placeholder="질문 작성자 비밀번호" required minlength="4">
                    <button class="primary-button" type="submit">채택 확인</button>
                    <button class="ghost-button" type="button" data-adopt-cancel="${answer.id}">취소</button>
                </form>
            ` : ""}
            ${canUnadopt ? `
                <form class="adopt-form" data-unadopt-form="${answer.id}" style="display:none;">
                    <input name="password" type="password" placeholder="질문 작성자 비밀번호" required minlength="4">
                    <button class="danger-button" type="submit">채택 취소 확인</button>
                    <button class="ghost-button" type="button" data-unadopt-cancel="${answer.id}">취소</button>
                </form>
            ` : ""}
        </article>
    `;
}

async function deleteQuestion(event) {
    event.preventDefault();
    const fd = new FormData(event.currentTarget);
    await request(`/api/questions/${state.activeQuestionId}`, {
        method: "DELETE",
        body: JSON.stringify({ password: fd.get("password") }),
    });
    showToast("질문이 삭제되었습니다.");
    detailDialog.close();
    await refresh();
}

async function adoptAnswer(questionId, answerId, password) {
    await request(`/api/questions/${questionId}/answers/${answerId}/adopt`, {
        method: "PATCH",
        body: JSON.stringify({ password }),
    });
    showToast("답변이 채택되었습니다. 🎉");
    await refresh();
    await openDetail(questionId);
}

async function unadoptAnswer(questionId, answerId, password) {
    await request(`/api/questions/${questionId}/answers/${answerId}/unadopt`, {
        method: "PATCH",
        body: JSON.stringify({ password }),
    });
    showToast("채택이 취소되었습니다.");
    await refresh();
    await openDetail(questionId);
}

async function reportQuestion(questionId) {
    await request(`/api/questions/${questionId}/report`, { method: "POST" });
    showToast("신고되었습니다. 검토 후 처리됩니다.");
    detailDialog.close();
    await refresh();
}

function renderReportBox(questions, adminPassword) {
    if (questions.length === 0) {
        reportBoxList.innerHTML = "<div class='empty'>신고된 글이 없습니다.</div>";
        return;
    }
    reportBoxList.innerHTML = questions.map((q) => `
        <div class="report-item">
            <div class="report-item-info">
                <span class="badge class-badge">${escapeHtml(q.className)}</span>
                <span class="row-title" style="flex:1;">${escapeHtml(q.title)}</span>
                <span class="meta-row">${escapeHtml(q.nickname)} · ${formatRelativeDate(q.createdAt)}</span>
            </div>
            <button class="ghost-button restore-btn" type="button" data-restore-id="${q.id}" data-admin-pw="${escapeHtml(adminPassword)}">복구</button>
        </div>
    `).join("");

    reportBoxList.querySelectorAll(".restore-btn").forEach((btn) => {
        btn.addEventListener("click", async () => {
            await request(`/api/reports/${btn.dataset.restoreId}/restore`, {
                method: "PATCH",
                body: JSON.stringify({ password: btn.dataset.adminPw }),
            });
            showToast("글이 복구되었습니다.");
            const updated = await request(`/api/reports?password=${encodeURIComponent(btn.dataset.adminPw)}`);
            renderReportBox(updated, btn.dataset.adminPw);
            await refresh();
        });
    });
}

/* ── Flower settings ── */
async function openFlowerSettings() {
    const cn = classSelect.value;
    const flowers = await request("/api/class-flowers");
    const flowerMap = flowers
        ? Object.fromEntries(flowers.map((f) => [f.className, f.flowerType]))
        : {};
    const currentType = flowerMap[cn] || "sunflower";
    flowerSettingsDialog.querySelector("h2").textContent = `🌸 ${cn} 꽃 변경`;
    renderFlowerSettings(cn, currentType);
    if (!flowerSettingsDialog.open) flowerSettingsDialog.showModal();
}

function renderFlowerSettings(cn, currentType) {
    const selectHtml = flowerOptions
        .map((f) => `<option value="${f.type}"${f.type === currentType ? " selected" : ""}>${f.name}</option>`)
        .join("");

    flowerSettingsList.innerHTML = `
        <div class="flower-single-body">
            <p class="flower-single-desc">${escapeHtml(cn)}의 꽃을 선택하세요.</p>
            <div class="flower-single-actions">
                <select class="sidebar-select" id="flowerTypeSelect">${selectHtml}</select>
                <button class="primary-button flower-confirm-btn" type="button">변경하기</button>
            </div>
        </div>
    `;

    flowerSettingsList.querySelector(".flower-confirm-btn").addEventListener("click", async () => {
        const flowerType = flowerSettingsList.querySelector("#flowerTypeSelect").value;
        try {
            await request(`/api/class-flowers/${encodeURIComponent(cn)}`, {
                method: "PATCH",
                body: JSON.stringify({ flowerType }),
            });
            showToast(`${cn} 꽃이 변경되었습니다.`);
            flowerSettingsDialog.close();
            await loadFlowerGrowth();
        } catch {
            /* request()가 이미 토스트를 표시 */
        }
    });
}

/* ── Actions ── */
async function toggleLike(questionId, reopenDetail = false) {
    if (hasLiked(questionId)) {
        await request(`/api/questions/${questionId}/like`, { method: "DELETE" });
        removeLiked(questionId);
        showToast("공감을 해제했습니다.");
    } else {
        await request(`/api/questions/${questionId}/like`, { method: "POST" });
        saveLiked(questionId);
        showToast("공감이 반영되었습니다.");
    }
    await refresh();
    if (reopenDetail) await openDetail(questionId);
}

async function submitAnswer(event) {
    event.preventDefault();
    const fd = new FormData(event.currentTarget);
    await request(`/api/questions/${state.activeQuestionId}/answers`, {
        method: "POST",
        body: JSON.stringify({
            content:  fd.get("content").trim(),
            nickname: fd.get("nickname").trim(),
            password: fd.get("password"),
        }),
    });
    showToast("답변이 등록되었습니다.");
    await refresh();
    await openDetail(state.activeQuestionId);
}

async function solveQuestion(event) {
    event.preventDefault();
    const fd = new FormData(event.currentTarget);
    await request(`/api/questions/${state.activeQuestionId}/solve`, {
        method: "PATCH",
        body: JSON.stringify({ password: fd.get("password") }),
    });
    showToast("해결 완료로 변경되었습니다.");
    await refresh();
    await openDetail(state.activeQuestionId);
}

/* ── HTTP ── */
async function request(url, options = {}) {
    const response = await fetch(url, {
        headers: { "Content-Type": "application/json" },
        ...options,
    });
    if (!response.ok) {
        const err = await response.json().catch(() => ({ message: "요청 처리에 실패했습니다." }));
        showToast(err.message || "요청 처리에 실패했습니다.");
        throw new Error(err.message);
    }
    if (response.status === 204) return null;
    return response.json();
}

/* ── Helpers ── */
function bindOpenDetailInteraction(el) {
    el.addEventListener("click", () => openDetail(el.dataset.detailId));
    el.addEventListener("keydown", (e) => {
        if (e.key === "Enter" || e.key === " ") {
            e.preventDefault();
            openDetail(el.dataset.detailId);
        }
    });
}

function closeOnBackdrop(dialog) {
    dialog.addEventListener("click", (e) => { if (e.target === dialog) dialog.close(); });
}

function initializeClassOptions() {
    const opts = classNames.map((cn) => `<option>${cn}</option>`).join("");
    classSelect.innerHTML = opts;
    questionForm.elements.className.innerHTML = opts;
    classSelect.value = "21반";
    questionForm.elements.className.value = "21반";
}


function hasLiked(id)  { return getLikedIds().includes(Number(id)); }
function saveLiked(id) {
    const ids = new Set(getLikedIds());
    ids.add(Number(id));
    localStorage.setItem("likedQuestionIds", JSON.stringify([...ids]));
}
function removeLiked(id) {
    const ids = getLikedIds().filter((x) => x !== Number(id));
    localStorage.setItem("likedQuestionIds", JSON.stringify(ids));
}
function getLikedIds() {
    return JSON.parse(localStorage.getItem("likedQuestionIds") || "[]");
}

function showToast(msg) {
    toast.textContent = msg;
    toast.classList.add("show");
    setTimeout(() => toast.classList.remove("show"), 2200);
}

function formatDate(value) {
    if (!value) return "";
    return new Intl.DateTimeFormat("ko-KR", { dateStyle: "short", timeStyle: "short" })
        .format(new Date(value));
}

function formatRelativeDate(value) {
    if (!value) return "";
    const diff = Date.now() - new Date(value).getTime();
    const mins = Math.floor(diff / 60000);
    if (mins < 1)   return "방금 전";
    if (mins < 60)  return `${mins}분 전`;
    const hours = Math.floor(mins / 60);
    if (hours < 24) return `${hours}시간 전`;
    const days = Math.floor(hours / 24);
    if (days < 7)   return `${days}일 전`;
    return formatDate(value);
}

function debounce(fn, delay) {
    let timer;
    return (...args) => { clearTimeout(timer); timer = setTimeout(() => fn(...args), delay); };
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;").replaceAll("'", "&#039;");
}

refresh().then(() => computeAndCacheRankings());
