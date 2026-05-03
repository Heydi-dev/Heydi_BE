# 📓 [Heydi](https://heydi-liart.vercel.app/)
 **대화로 정리하는 나만의 일기, 헤이디**

Heydi와 함께 번거로운 작성 없이, AI와의 대화로 일기를 완성하세요!

---

## ✨ 프로젝트 소개

일기를 쓰는 일은 나를 돌아보는 가장 좋은 방법이지만, 매일 글로 정리하는 일은 쉽지 않습니다.

**Heydi**는 사용자가 하루를 **말로 이야기하면**,
AI가 실시간 음성 대화를 통해 공감하고 질문하며  
그날의 감정과 이야기를 **자연스러운 일기**로 정리해주는 **AI 음성 대화 기반 일기 서비스**로,

- 키보드 없이 **AI와 음성으로 대화하며 하루를 기록**,
- 작성된 일기를 통해 나의 **하루를 객관적으로 이해**,
- 일기를 **커뮤니티에 공유**하며 다른 사람들의 하루에 공감,
- 쌓인 기록을 바탕으로 **월간 리포트**를 통해 나의 변화를 확인할 수 있습니다. 

---

## 🚀 주요 기능

- 🎙️ **AI 음성 대화 기반 일기 작성**
  Gemini Live API를 활용해 AI와 음성으로 대화하며 하루를 기록하고 정리

- 📝 **자동 일기 생성**
  대화 내용을 바탕으로 감정과 주제를 분석해 AI 일기 자동 작성

- 📅 **일기 기록 관리**
  날짜별 일기 조회, 사진 추가 및 일기 수정·삭제 관리

- 📄 **일기 내보내기**
  작성한 일기를 PDF로 저장하고 보관

- 📊 **월간 리포트 생성**
  감정 변화 / 자주 등장한 주제 / 자주 한 활동 / 인사이트 확인

- 🌱 **커뮤니티**
  선택한 일기를 공유하고 좋아요와 댓글로 공감

- 🔔 **FCM 알림**
  일기 작성을 잊지 않도록 리마인더 알림 제공

- 👤 **마이페이지**
  나의 기록 히스토리 및 개인 설정 관리

---

## 📁 폴더 구조

```bash
src/main/java/com/example/heydibe/
├── ai/                    # AI 서버 연동 및 AI 응답 처리
│   ├── client/            # 외부 AI API 호출 클라이언트
│   ├── dto/               # AI 요청/응답 데이터 객체
│   └── service/           # AI 관련 비즈니스 로직
│
├── auth/                  # 회원가입, 로그인, 로그아웃 등 인증 처리
│   ├── controller/        # 인증 관련 API 요청 처리
│   ├── dto/               # 로그인/회원가입 요청 및 응답 객체
│   └── service/           # 인증 비즈니스 로직
│
├── common/                # 프로젝트 전역 공통 기능
│   ├── auth/              # 로그인 사용자 추출 등 인증 공통 처리
│   ├── error/             # 에러 코드 정의
│   ├── exception/         # 전역 예외 처리 및 커스텀 예외
│   └── response/          # 공통 API 응답 형식
│
├── community/             # 커뮤니티 게시글, 댓글, 좋아요 기능
│   ├── comment/           # 게시글 댓글 기능
│   │   ├── controller/    # 댓글 API 요청 처리
│   │   ├── dto/           # 댓글 요청/응답 데이터 객체
│   │   ├── entity/        # 댓글 엔티티
│   │   ├── repository/    # 댓글 DB 접근
│   │   └── service/       # 댓글 비즈니스 로직
│   │
│   └── post/              # 커뮤니티 게시글 및 좋아요 기능
│       ├── controller/    # 게시글 API 요청 처리
│       ├── dto/           # 게시글 요청/응답 데이터 객체
│       ├── entity/        # 게시글, 좋아요, 첨부파일 엔티티
│       ├── repository/    # 게시글 관련 DB 접근
│       └── service/       # 게시글 작성, 조회, 좋아요 로직
│
├── config/                # 프로젝트 전역 설정 클래스
│
├── diary/                 # 일기 작성, 조회, 대화 기록 관리
│   ├── controller/        # 일기 관련 API 요청 처리
│   ├── dto/               # 일기 요청/응답 데이터 객체
│   ├── entity/            # 일기, 대화, 첨부파일 엔티티
│   ├── repository/        # 일기 관련 DB 접근
│   ├── service/           # 일기 비즈니스 로직
│   └── websocket/         # 실시간 대화 및 WebSocket 처리
│
├── infrastructure/        # 외부 인프라 및 서비스 연동
│   └── s3/                # AWS S3 파일 업로드/삭제 처리
│
├── mypage/                # 마이페이지 조회 및 사용자 활동 내역
│   ├── controller/        # 마이페이지 API 요청 처리
│   ├── dto/               # 마이페이지 응답 데이터 객체
│   └── service/           # 마이페이지 조회 비즈니스 로직
│
├── notification/          # FCM 알림 및 토큰 관리
│   ├── controller/        # 알림 관련 API 요청 처리
│   ├── dto/               # FCM 요청 데이터 객체
│   └── service/           # FCM 토큰 저장 및 알림 전송 로직
│
├── profile/               # 사용자 프로필 관련 기능
│   ├── entity/            # 프로필 엔티티
│   └── repository/        # 프로필 DB 접근
│
├── report/                # 월간 리포트 조회 및 분석 결과 제공
│   ├── controller/        # 리포트 API 요청 처리
│   ├── dto/               # 리포트 응답 데이터 객체
│   ├── repository/        # 리포트 DB 접근
│   └── service/           # 리포트 조회 및 데이터 가공 로직
│
├── security/              # Spring Security 보안 설정
│   └── config/            # 보안 필터 및 접근 권한 설정
│
├── settings/              # 사용자 설정 및 알림 설정 관리
│   ├── controller/        # 설정 관련 API 요청 처리
│   ├── dto/               # 설정 요청/응답 데이터 객체
│   ├── entity/            # 알림 설정 엔티티
│   ├── repository/        # 설정 DB 접근
│   └── service/           # 설정 변경 및 조회 로직
│
├── user/                  # 사용자 계정 정보 관리
│   ├── entity/            # 사용자 엔티티
│   └── repository/        # 사용자 DB 접근
│
└── HeydiBeApplication.java # Spring Boot 애플리케이션 실행 진입점

```
---

## 🛠️ 기술 스택 & 사용 라이브러리
![JAVA](https://img.shields.io/badge/JAVA-17-007396?style=flat-square&logo=java&logoColor=white)
![SPRING_BOOT](https://img.shields.io/badge/SPRING_BOOT-3.x-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![SPRING_DATA_JPA](https://img.shields.io/badge/SPRING_DATA_JPA-6DB33F?style=flat-square)
![POSTGRESQL](https://img.shields.io/badge/POSTGRESQL-4169E1?style=flat-square&logo=postgresql&logoColor=white)
![FIREBASE](https://img.shields.io/badge/FIREBASE_FCM-FFCA28?style=flat-square&logo=firebase&logoColor=black)

### Backend
- Java: 17
- Spring Boot: 3.x
- Spring Data JPA (Hibernate)

### Database
- PostgreSQL
- pgAdmin

### 인증 & 보안
- JWT 기반 인증
- Spring Security

### 외부 서비스
- Firebase Cloud Messaging (FCM)
- AI 서버 연동 (Gemini API)

### 빌드 & 개발 도구
- Gradle
- IntelliJ IDEA

---

## 💡 Commit Convention
```bash
- :sparkles:[feat]: 새로운 기능 추가
- :recycle:[refactor]: 코드 리팩토링
- :art:[style]: 스타일 수정, 코드 의미에 영향을 주지 않는 변경사항
- :bug:[fix]: 버그 수정
- :memo:[docs]: 문서 작성 및 수정
- :white_check_mark:[test]: 테스트 코드 추가
- :wrench:[chore]: 빌드 및 설정 변경
- :hammer:[rename]: 파일, 폴더 삭제 및 이름 수정
- :bulb:[comment]: 주석 추가 및 변경
```
## 💡 Branch Convention
```bash
{type}/#{issuenumber}/{task}
- feat: 기능 개발 Branch
- Refactor: 기존 기능 수정 Branch
- develop: 배포 전 통합 Branch
- main: 운영 배포 Branch

- ex) feat/#1/초기세팅
```
