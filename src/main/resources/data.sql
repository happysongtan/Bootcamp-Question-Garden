INSERT INTO class_flowers (class_name, flower_type) VALUES
('21반', 'sunflower'),
('22반', 'tulip'),
('23반', 'cherry-blossom'),
('24반', 'dandelion'),
('25반', 'lavender'),
('26반', 'cosmos'),
('27반', 'rose'),
('28반', 'hydrangea'),
('29반', 'daisy');

INSERT INTO questions
(class_name, title, content, category, nickname, password, status, like_count, view_count, tags, created_at, updated_at)
VALUES
('21반',
 'JdbcTemplate이 정확히 뭔지 모르겠습니다.',
 'Repository에서 JdbcTemplate을 사용해서 DB 데이터를 가져오는 걸 배웠는데, 기존 JDBC 방식과 정확히 어떤 차이가 있는지 궁금합니다.',
 'Spring', '익명 감자', '1234', 'ANSWERED', 15, 3, 'Spring,JDBC,Repository', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('21반',
 'NumberFormatException: null은 왜 발생하나요?',
 'BufferedReader로 입력을 받는 중 Integer.parseInt(br.readLine())에서 NumberFormatException: null이 발생했습니다. 입력 개수와 반복 조건 중 어디를 먼저 확인해야 할까요?',
 'Java', '익명 새싹', '1234', 'SOLVED', 9, 5, 'Java,BufferedReader,NumberFormatException', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('25반',
 'fetch 요청 후 화면을 다시 그리는 기준이 궁금합니다.',
 '질문 등록 후 목록을 다시 불러오는 방식과 현재 배열에 직접 추가하는 방식 중 어떤 접근이 더 안전한지 알고 싶습니다.',
 'JavaScript', '익명 잎사귀', '1234', 'WAITING', 7, 2, 'fetch,state,render', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('29반',
 '누적합에서 같은 나머지를 조합하는 이유가 이해되지 않습니다.',
 'S[j] % C == S[i] % C 이면 S[j] - S[i]가 C로 나누어떨어진다는 설명이 아직 헷갈립니다.',
 'Algorithm', '익명 개발자', '1234', 'WAITING', 13, 2, 'Algorithm,PrefixSum,Remainder', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answers
(question_id, content, nickname, password, created_at, updated_at)
VALUES
(1, 'JdbcTemplate은 Connection, PreparedStatement, ResultSet close 같은 반복 작업을 대신 처리해 줍니다. 개발자는 SQL과 결과 매핑에 더 집중할 수 있습니다.', '익명 멘토', '1234', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'readLine()이 입력 끝에서 null을 반환했을 가능성이 큽니다. 반복 횟수와 실제 입력 줄 수가 맞는지 먼저 확인해 보세요.', '익명 답변자', '1234', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
