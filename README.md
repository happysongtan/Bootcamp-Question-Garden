# 꽃피는 질문

반 친구들이 익명 질문, 공감, 답변을 모아 우리 반의 꽃을 함께 키우는 질문 공유 서비스입니다.

## 구현된 기능

- 질문 등록, 목록 조회, 상세 조회
- 21반부터 29반까지 고정 반 선택
- 반별 질문 필터, 인기 질문 조회, 성장 정보 조회
- 질문 공감 추가 및 공감 취소
- localStorage 기반 MVP 수준의 공감 상태 저장
- 질문 카드 클릭 및 키보드 Enter/Space로 상세 열기
- 답변 등록
- 질문 비밀번호 기반 해결 완료 표시
- 카테고리 필터, 키워드 검색, 정렬

## 성장 점수

- 질문 등록: 3점
- 공감 추가: 1점
- 답변 등록: 4점
- 해결 완료: 5점

## 실행 방법

Maven이 PATH에 잡혀 있으면:

```bash
mvn spring-boot:run
```

현재 PC의 IntelliJ 내장 Maven을 사용할 경우:

```powershell
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd' "-Dmaven.repo.local=C:\questionboard\.m2\repository" spring-boot:run
```

접속 주소:

```text
http://localhost:8080
```

H2 콘솔:

```text
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:question_board
User Name: sa
Password:
```

## 주요 API

| 기능 | Method | URL |
| --- | --- | --- |
| 질문 등록 | POST | /api/questions |
| 질문 목록 | GET | /api/questions |
| 인기 질문 | GET | /api/questions/popular |
| 꽃 성장 정보 | GET | /api/classes/{className}/flower |
| 질문 상세 | GET | /api/questions/{questionId} |
| 질문 수정 | PUT | /api/questions/{questionId} |
| 질문 삭제 | DELETE | /api/questions/{questionId} |
| 공감 추가 | POST | /api/questions/{questionId}/like |
| 공감 취소 | DELETE | /api/questions/{questionId}/like |
| 해결 완료 표시 | PATCH | /api/questions/{questionId}/solve |
| 답변 등록 | POST | /api/questions/{questionId}/answers |
