# 뉴스 데이터 수집 서비스 (Spring Boot + Vue)

키워드를 기준으로 **뉴스를 자동 수집**하고, 웹에서 **조회/관리**할 수 있는 서비스입니다.

**Google OAuth2 로그인 → 대시보드 → 설정(키워드/소스)** 순으로 사용할 수 있고,

**새 키워드 최초 등록 시 1회 즉시 수집** 후, **매시 정각**에 정기 수집합니다.

---

# 핵심 기능

- Google OAuth2 로그인 → JWT 발급 → 프론트에서 보관
- 대시보드에서 수집된 기사 목록 조회(검색/필터/정렬/더보기)
- 설정
    - 키워드 CRUD (최초 등록 시 1회 즉시 수집)
    - 소스 CRUD 
- 크롤링
    - Google News RSS + `site:도메인` 필터 방식
    - 매시 정각 자동 실행 + 수동 트리거 API
- 다중 사용자 분리
    - 각 사용자의 키워드·소스가 분리 저장
    - `keywords`는 `(user_id, word)` **복합 UNIQUE** (사용자마다 같은 단어 허용)

---

# 아키텍처 개요

```
[Vue 3 + Vite]  -->  [Spring Boot 3 + MyBatis]  -->  [MySQL 8]
     JWT 보관           OAuth2 로그인/JWT 발급           스키마/인덱스
   /, /settings 등             /api/* REST API

```

- 인증: Google OAuth2 → 성공 시 백엔드가 JWT 발급 → 프론트로 `token` 전달
- 권한: `/api/articles` 공개, 그 외 `/api/**`는 JWT 필요
- 크롤링: `@Scheduled` 매시 정각 + `/api/crawl/trigger` 수동 트리거

---

# 주요 화면

- **대시보드** (`/`): 기사 목록, 검색/필터, 더보기 페이지네이션
- **설정** (`/settings`): 키워드/소스 관리(로그인 필요)
    - 키워드: 추가/ON·OFF/삭제
    - 소스: 추가/수정/ON·OFF/삭제 

---

# DB 세팅
### 1) MySQL 컨테이너 띄우기

```
docker run -d --name mysql8-news \
  -e MYSQL_ROOT_PASSWORD=rootpw \
  -e MYSQL_DATABASE=news \
  -e MYSQL_USER=news \
  -e MYSQL_PASSWORD=news \
  -p 3306:3306 \
  mysql:8

```


---

### 2) 컨테이너 안에서 mysql 접속

```
# 애플리케이션 계정으로 바로 DB 접속
docker exec -it mysql8-news mysql -unews -pnews news

# 루트 접속
# docker exec -it mysql8-news mysql -uroot -prootpw

```

---

### 3) 스키마/테이블 쿼리 붙여넣기

```
-- 1) users
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 2) keywords
CREATE TABLE `keywords` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `word` varchar(100) NOT NULL,
  `enabled` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_keywords_user_word` (`user_id`,`word`),
  KEY `idx_keywords_user` (`user_id`),
  CONSTRAINT `fk_keywords_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3) sources
CREATE TABLE `sources` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL,
  `name` varchar(100) NOT NULL,
  `base_url` varchar(300) NOT NULL,
  `enabled` tinyint(1) DEFAULT '1',
  `collector` varchar(50) NOT NULL DEFAULT 'AGGREGATOR_RSS',
  `params` json DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_sources_user` (`user_id`),
  CONSTRAINT `fk_sources_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 4) articles
CREATE TABLE `articles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `source_id` bigint NOT NULL,
  `keyword_id` bigint DEFAULT NULL,
  `url` varchar(1000) NOT NULL,
  `url_hash` char(64) NOT NULL,
  `title` varchar(1000) NOT NULL,
  `summary` text,
  `image_url` varchar(1000) DEFAULT NULL,
  `published_at` datetime DEFAULT NULL,
  `fetched_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lang` varchar(10) DEFAULT 'ko',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_article_url_hash` (`url_hash`),
  KEY `idx_articles_published` (`published_at`),
  KEY `idx_articles_source` (`source_id`),
  KEY `idx_articles_keyword` (`keyword_id`),
  CONSTRAINT `fk_articles_keyword` FOREIGN KEY (`keyword_id`) REFERENCES `keywords` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_articles_source` FOREIGN KEY (`source_id`) REFERENCES `sources` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8497 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

```

---

### 기초 데이터

```
-- 1. 사용자 생성
INSERT INTO users (email, name)
VALUES ('admin@example.com', '관리자');

-- 2. 뉴스 소스 생성 (user_id=1)
INSERT INTO sources (code, name, base_url, enabled, collector, params, user_id)
VALUES ('HK', '한국경제', 'https://www.hankyung.com/', 1, 'AGGREGATOR_RSS_SITE', JSON_OBJECT('site', 'www.hankyung.com'), 1);

-- 2. 키워드 생성 (user_id=1)
INSERT INTO keywords (word, enabled, user_id)
VALUES ('부동산', 1, 1);

-- 3. 기사 데이터 생성 (source_id=1, keyword_id=1)
INSERT INTO articles (source_id, keyword_id, url, url_hash, title, fetched_at)
VALUES (1, 1, 'https://www.hankyung.com/example1', 'hash_example_1', '부동산 시장 분석', NOW());

```

