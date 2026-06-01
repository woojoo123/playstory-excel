# Playstory Excel

10만 건의 주문 데이터를 엑셀 파일로 생성하는 작업을 비동기로 처리하는
Spring Boot 기반 과제 프로젝트입니다.

사용자가 엑셀 생성을 요청하면 HTTP 요청 안에서 파일 생성을 끝내지 않습니다.
서버는 먼저 작업을 DB에 `PENDING` 상태로 저장하고 `202 Accepted` 응답을
반환합니다. 백그라운드 worker는 대기 중인 작업을 하나씩 처리하고 상태와
생성된 파일 경로를 DB에 기록합니다.

## 실행 방법

### 사전 조건

- Docker 및 Docker Compose를 실행할 수 있는 환경이 필요합니다.
- 호스트의 `8080`, `3307` 포트를 다른 프로그램이 사용하고 있지 않아야 합니다.

### 실행

```bash
docker compose up --build
```

브라우저에서 아래 주소를 엽니다.

```text
http://localhost:8080
```

화면의 `엑셀 생성 요청` 버튼을 누르면 작업이 등록됩니다. 화면은 3초마다
작업 목록을 다시 조회하고, 작업이 완료되면 다운로드 링크를 표시합니다.

### 종료

```bash
docker compose down
```

MySQL 데이터와 생성된 엑셀 파일은 Docker volume에 남습니다.
초기 상태부터 다시 확인하려면 아래 명령을 사용합니다.

```bash
docker compose down -v
docker compose up --build
```

`docker compose down -v`는 기존 DB 데이터와 Docker volume 안의 엑셀 파일을
삭제합니다.

## 주요 기능

- 엑셀 생성 작업 등록
- 작업 목록 및 단건 조회
- `PENDING`, `PROCESSING`, `DONE`, `FAILED` 상태 관리
- 완료된 엑셀 파일 다운로드
- MySQL 최초 실행 시 주문 데이터 10만 건 자동 생성
- 제한된 메모리를 고려한 엑셀 스트리밍 생성
- 단일 worker를 통한 순차 처리

## API

| Method | URL | 설명 |
| --- | --- | --- |
| `POST` | `/api/excel-jobs` | 엑셀 생성 작업을 등록하고 즉시 `202 Accepted` 반환 |
| `GET` | `/api/excel-jobs` | 엑셀 생성 작업 목록 조회 |
| `GET` | `/api/excel-jobs/{jobId}` | 특정 작업 조회 |
| `GET` | `/api/excel-jobs/{jobId}/download` | 완료된 엑셀 파일 다운로드 |

다운로드 API는 작업이 아직 완료되지 않았다면 `409 Conflict`, 작업이나
파일이 존재하지 않는다면 `404 Not Found`를 반환합니다.

## 처리 흐름

```text
사용자 요청
  -> excel_jobs 테이블에 PENDING 작업 저장
  -> 202 Accepted 응답 반환
  -> worker가 가장 오래 대기한 PENDING 작업 조회
  -> PROCESSING 상태로 변경
  -> orders 테이블을 나누어 조회하며 엑셀 생성
  -> 성공 시 DONE과 파일 경로 기록
  -> 실패 시 FAILED와 오류 메시지 기록
```

## 기술 선택 이유

### Spring Boot와 Spring Data JPA

HTTP API, 트랜잭션, DB 접근을 구현하기 위해 사용했습니다. JPA entity는 Java
객체와 DB 테이블을 연결합니다. 테이블 생성 자체는 SQL 초기화 스크립트가
담당하고, 애플리케이션 실행 시 Hibernate의 `ddl-auto: validate` 설정으로
entity와 테이블이 맞는지 검증합니다.

### MySQL 초기화 스크립트

MySQL 공식 이미지가 기본 제공하는 `/docker-entrypoint-initdb.d` 기능을
사용했습니다. Docker volume이 처음 생성될 때
`docker/mysql/init/01-schema.sql`, `02-seed.sql`이 순서대로 실행됩니다.

### Apache POI `SXSSFWorkbook`

Java 표준 라이브러리만으로는 `.xlsx` 파일을 생성할 수 없으므로 Apache POI를
사용했습니다. 일반 `XSSFWorkbook`은 작성한 행을 메모리에 계속 유지합니다.
이 프로젝트는 메모리 제한 안에서 10만 건을 처리해야 하므로 최근 100개 행만
메모리에 유지하고 나머지를 임시 파일로 내보내는 `SXSSFWorkbook`을
사용했습니다.

## 설계 시 고민한 점

### 제한된 리소스에서 작업을 어떻게 처리할 것인가

단일 백엔드 컨테이너와 제한된 리소스를 전제로 합니다. 별도 Redis,
메시지 브로커, worker 컨테이너를 추가하지 않고 Spring이 기본 제공하는
Scheduler로 1초마다 가장 오래 대기한 작업 하나를 확인합니다.

worker는 작업을 한 번에 하나씩 처리합니다. 여러 엑셀 생성 작업이 동시에
메모리와 CPU를 경쟁하는 상황을 막기 위한 선택입니다. DB의 `excel_jobs`
테이블이 작업 상태와 대기열 역할을 함께 담당합니다.

### 10만 건 데이터를 어떻게 메모리 효율적으로 처리할 것인가

`orders` 전체를 한 번에 `List`로 가져오지 않습니다. 마지막으로 처리한
`id`보다 큰 데이터를 1,000건씩 조회하고 엑셀 파일에 순서대로 기록합니다.

`id`를 기준으로 다음 데이터를 조회하는 keyset pagination 방식을 사용했습니다.
offset 방식은 뒤쪽 데이터를 조회할수록 앞의 데이터를 건너뛰는 비용이
증가할 수 있기 때문입니다.

또한 Apache POI의 `SXSSFWorkbook`을 사용하여 최근 100개 행만 메모리에
유지하고, 나머지는 임시 파일로 내보냅니다.

완성되지 않은 파일이 다운로드되는 것을 막기 위해 먼저 `.xlsx.tmp` 파일에
작성합니다. 생성이 끝난 후 최종 `.xlsx` 경로로 이동하고 작업 상태를
`DONE`으로 변경합니다.

## Docker 구성

| 서비스 | 역할 | 호스트 포트 |
| --- | --- | --- |
| `backend` | Spring Boot API, worker, 정적 화면 | `8080` |
| `mysql` | 주문 데이터와 작업 상태 저장 | `3307` |

로컬 컴퓨터에서 MySQL에 직접 접근할 때는 `localhost:3307`을 사용합니다.
Docker 내부에서 백엔드가 MySQL에 접근할 때는 서비스 이름을 사용하여
`mysql:3306`으로 연결합니다.

백엔드 컨테이너에는 과제 조건에 따라 아래 리소스 제한을 적용했습니다.

| 항목 | 제한 | 예약 |
| --- | --- | --- |
| CPU | `0.5` | `0.25` |
| Memory | `1GB` | `512MB` |

## 프로젝트 구조

```text
src/main/java/com/playstory/excel
├── controller  HTTP 요청과 응답 처리
├── dto         API 응답 형태
├── entity      DB 테이블과 연결되는 Java 객체
├── repository  DB 조회와 저장
├── service     작업 상태 변경과 엑셀 생성
└── worker      대기 작업을 순차적으로 처리

docker/mysql/init
├── 01-schema.sql  테이블 생성
└── 02-seed.sql    주문 데이터 10만 건 생성
```

## 확인 결과

Docker Compose 환경에서 아래 내용을 직접 확인했습니다.

- `docker compose up --build`로 MySQL과 백엔드 실행
- MySQL 주문 데이터 `100,000`건 확인
- 엑셀 작업 요청이 `202 Accepted`로 즉시 반환되는 것 확인
- 백그라운드 처리 후 상태가 `DONE`으로 변경되는 것 확인
- 생성된 엑셀 파일이 헤더 포함 `100,001`행인 것 확인
- 생성된 파일 다운로드와 `.xlsx` 압축 구조 정상 여부 확인

## 한계와 개선 방향

현재 구조는 과제 조건인 단일 백엔드 컨테이너와 단일 worker에 맞춰
설계했습니다.

- 애플리케이션이 작업 처리 중 종료되면 해당 작업이 `PROCESSING`에 남을 수
  있습니다. 실제 운영 환경에서는 애플리케이션 시작 시 오래된
  `PROCESSING` 작업을 다시 `PENDING`으로 돌리거나 실패 처리하는 복구 정책이
  필요합니다.
- 백엔드 인스턴스가 여러 개라면 같은 작업을 동시에 가져가지 않도록 DB lock
  또는 메시지 큐가 필요합니다. 현재 과제에서는 백엔드 컨테이너가 하나이므로
  추가하지 않았습니다.
- 화면 상태 갱신은 단순한 3초 polling 방식입니다. 실시간성이 더 중요하다면
  SSE 같은 방식을 검토할 수 있습니다.
