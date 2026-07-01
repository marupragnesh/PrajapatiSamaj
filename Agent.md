# Agent.md — PrajapatiSamaj Matrimonial Platform

> Maintained by Claude. Update after every session.
> Last updated: 2026-07-01

---

## 📁 Project Overview

| Field | Details |
|---|---|
| Project Name | PrajapatiSamaj Matrimonial Platform |
| Location | D:\Projects\PrajapatiSamaj |
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Database | MySQL (`matrimonial_db`) |
| Architecture | Layered: Controller → Service → Repository + Mapper |
| Auth | JWT (Bearer token, 24hr expiry) |
| Password | BCrypt (strength 12) |
| Email | JavaMailSender (SMTP / Gmail) |
| File Storage | Local disk (Phase 2: AWS S3) |
| Frontend | React + Vite (port 5173) — D:\Projects\PrajapatiSamajFrontEnd |
| Phase 1 | Core platform — NO payment |
| Phase 2 | Premium plans, filters, contact reveal, payment |

---

## 📦 Package Structure

```
com.matrimonial
├── config/          JwtAuthFilter, SecurityConfig, CustomUserDetailsService
├── controller/      AuthController, ProfileController, DiscoverController
│                    LikeController, InterestController
├── service/         AuthService, ProfileService, DiscoverService
│                    LikeService, InterestService, EmailService, OtpService
├── mapper/          ProfileMapper  ← NEW: single source of truth for entity→DTO
├── repository/      UserRepository, ProfileRepository, PhotoRepository
│                    LikeRepository, InterestRepository, OtpRepository
│                    PartnerPreferenceRepository, ExpectationRepository
├── entity/          User, Profile, ProfilePhoto, Like, InterestRequest
│                    OtpToken, PartnerPreference, Expectation
│   └── enums/       MaritalStatus (SINGLE, DIVORCED, WIDOWED)
│                    Diet (VEG, NON_VEG, VEGAN)
├── dto/
│   ├── request/     RegisterRequest, LoginRequest, ForgotPasswordRequest
│   │                VerifyOtpRequest, ResetPasswordRequest
│   │                ProfileRequest, PreferenceRequest, ExpectationRequest
│   └── response/    ApiResponse, AuthResponse, ProfileResponse, PhotoDto
│                    LikerSafeView, MatchResponse, InterestResponse
│                    ExpectationResponse, ProfileSearchResultDto ← NEW
├── exception/       GlobalExceptionHandler, BadRequestException
│                    ResourceNotFoundException, UnauthorizedException
└── util/            JwtUtil, OtpUtil, EmailTemplateUtil
```

---

## ✅ Phase 1 — Feature Progress

| # | Feature | Status | Notes |
|---|---|---|---|
| 1 | User Registration | ✅ Done | |
| 2 | Login (JWT) | ✅ Done | |
| 3 | Forgot Password — OTP flow | ✅ Done | |
| 4 | Profile Create / Update | ✅ Done | Includes maritalStatus, height, income, gotra, diet, mobileNo, addressLine, state, pincode |
| 5 | Photo Upload (max 10) | ✅ Done | Raised from 5 → 10 |
| 6 | Photo Delete | ✅ Done | |
| 7 | Set Primary Photo | ✅ Done | |
| 8 | Partner Preference | ✅ Done | |
| 9 | Partner Expectations | ✅ Done | Full feature — backend + frontend |
| 10 | Browse / Discover Profiles | ✅ Done | |
| 11 | Search by Name | ✅ Done | GET /api/discover/search?keyword= with DP in results |
| 12 | View Another User's Profile | ✅ Done | Mobile masked (e.g. 98********) for non-owners |
| 13 | Like a Profile (3/day free) | ✅ Done | |
| 14 | View Who Liked Me | ✅ Done | |
| 15 | Send Interest Request | ✅ Done | |
| 16 | Accept / Decline Interest | ✅ Done | |
| 17 | View Received Interests | ✅ Done | |
| 18 | View Mutual Matches | ✅ Done | |
| 19 | Email Notifications (all) | ✅ Done | |
| 20 | JWT Auth Filter | ✅ Done | |
| 21 | Global Exception Handler | ✅ Done | |
| 22 | @EnableAsync | ✅ Done | |
| 23 | Circular dependency fix | ✅ Done | |
| 24 | CORS config | ✅ Done | |
| 25 | Delete Account (full) | ✅ Done | |
| 26 | Navbar DP avatar + ❗ badge | ✅ Done | ❗ when expectations empty, DP when photo uploaded |
| 27 | "Developed by Pragnesh Maru ❤️" | ✅ Done | Login + Register pages footer |
| 28 | ProfileMapper (dedup fix) | ✅ Done | DiscoverService was missing new fields — fixed via shared mapper |
| 29 | Backend Logging | ✅ Done | logback-spring.xml + @Slf4j + RequestLoggingFilter |
| 30 | Suggestion/Bug Report Page | ❌ Deferred | Will implement later |
| 31 | Admin Panel | ❌ Pending | Next major feature |

---

## 📋 REST API Endpoints

### Auth (Public)
| Method | URL | Status |
|---|---|---|
| POST | /api/auth/register | ✅ |
| POST | /api/auth/login | ✅ |
| POST | /api/auth/forgot-password | ✅ |
| POST | /api/auth/verify-otp | ✅ |
| POST | /api/auth/reset-password | ✅ |

### Profile (JWT Required)
| Method | URL | Status |
|---|---|---|
| GET | /api/profile/me | ✅ |
| POST | /api/profile | ✅ |
| PUT | /api/profile | ✅ |
| POST | /api/profile/photos | ✅ |
| DELETE | /api/profile/photos/{id} | ✅ |
| PUT | /api/profile/photos/{id}/primary | ✅ |
| GET | /api/profile/expectations | ✅ |
| PUT | /api/profile/expectations | ✅ |
| GET | /api/preferences | ✅ |
| PUT | /api/preferences | ✅ |
| GET | /api/profiles/{id} | ✅ Mobile masked |
| DELETE | /api/account | ✅ |

### Discovery (JWT Required)
| Method | URL | Status |
|---|---|---|
| GET | /api/discover?page=0&size=10 | ✅ |
| GET | /api/discover/search?keyword= | ✅ NEW |

### Likes (JWT Required)
| Method | URL | Status |
|---|---|---|
| POST | /api/likes/{profileId} | ✅ |
| GET | /api/likes/received | ✅ |

### Interests (JWT Required)
| Method | URL | Status |
|---|---|---|
| POST | /api/interests/{profileId} | ✅ |
| GET | /api/interests/received | ✅ |
| PUT | /api/interests/{id}/accept | ✅ |
| PUT | /api/interests/{id}/decline | ✅ |
| GET | /api/interests/matches | ✅ |

---

## 🗄️ Entity Fields Reference

### Profile (profiles table)
| Field | Type | Required | Notes |
|---|---|---|---|
| fullName | String | ✅ | max 100 chars |
| age | Integer | ✅ | 18–80 |
| gender | Enum | ✅ | MALE, FEMALE, PREFER_NOT_TO_SAY |
| maritalStatus | Enum | ✅ (DTO) | SINGLE, DIVORCED, WIDOWED — nullable in DB for old rows |
| city | String | ✅ | max 100 chars |
| mobileNo | String | ✅ (DTO) | 10-digit Indian mobile, masked for non-owners |
| addressLine | String | ✅ (DTO) | max 255 chars — nullable in DB for old rows |
| state | String | ✅ (DTO) | max 100 chars — nullable in DB for old rows |
| pincode | String | ✅ (DTO) | 6-digit — nullable in DB for old rows |
| education | String | ✅ | max 150 chars |
| profession | String | ✅ | max 150 chars |
| height | String | 🔵 Optional | stored as "5'8\"" |
| income | String | 🔵 Optional | e.g. "50,000/month" |
| gotra | String | 🔵 Optional | max 100 chars |
| diet | Enum | 🔵 Optional | VEG, NON_VEG, VEGAN |
| religion | String | 🔵 Optional | max 100 chars |
| hobbies | String | 🔵 Optional | TEXT |

### Expectation (expectations table)
| Field | Type | Required | Notes |
|---|---|---|---|
| minAge | Integer | 🔵 Optional | 18–80 |
| maxAge | Integer | 🔵 Optional | 18–80, ≥ minAge |
| preferredMaritalStatus | Enum | 🔵 Optional | SINGLE, DIVORCED, WIDOWED |
| preferredMinHeight | String | 🔵 Optional | e.g. "5'4\"" |
| preferredMaxHeight | String | 🔵 Optional | e.g. "6'0\"" |
| preferredCity | String | 🔵 Optional | max 100 chars |
| preferredEducation | String | 🔵 Optional | max 150 chars |
| preferredProfession | String | 🔵 Optional | max 150 chars |
| preferredIncome | String | 🔵 Optional | e.g. "40,000 - 80,000/month" |
| preferredGotra | String | 🔵 Optional | max 100 chars |
| preferredDiet | Enum | 🔵 Optional | VEG, NON_VEG, VEGAN |
| preferredReligion | String | 🔵 Optional | max 100 chars |
| aboutExpectations | String | 🔵 Optional | TEXT |

---

## 🗺️ Frontend Routes

| Path | Component | Auth |
|---|---|---|
| / | RootRedirect | — |
| /register | RegisterPage | Public |
| /login | LoginPage | Public |
| /forgot-password | ForgotPasswordPage | Public |
| /verify-otp | VerifyOtpPage | Public |
| /reset-password | ResetPasswordPage | Public |
| /account-deleted | AccountDeletedPage | Public |
| /profile/setup | ProfileSetupPage | ✅ Protected |
| /profile/edit | EditProfilePage | ✅ Protected |
| /profile/expectations | ExpectationsPage | ✅ Protected |
| /discover | DiscoverPage | ✅ Protected |
| /profiles/:profileId | ProfileDetailPage | ✅ Protected |
| /likes | LikesReceivedPage | ✅ Protected |
| /interests | InterestsReceivedPage | ✅ Protected |
| /matches | MatchesPage | ✅ Protected |

---

## ⚠️ DB Migration Required

Run these SQL statements on `matrimonial_db` before starting the backend:

```sql
-- From previous session (if not already run)
ALTER TABLE profiles
  ADD COLUMN marital_status VARCHAR(20) NULL,
  ADD COLUMN height         VARCHAR(20) NULL,
  ADD COLUMN income         VARCHAR(100) NULL,
  ADD COLUMN gotra          VARCHAR(100) NULL,
  ADD COLUMN diet           VARCHAR(20) NULL;

ALTER TABLE expectations
  ADD COLUMN preferred_marital_status VARCHAR(20) NULL,
  ADD COLUMN preferred_min_height     VARCHAR(20) NULL,
  ADD COLUMN preferred_max_height     VARCHAR(20) NULL,
  ADD COLUMN preferred_income         VARCHAR(100) NULL,
  ADD COLUMN preferred_gotra          VARCHAR(100) NULL,
  ADD COLUMN preferred_diet           VARCHAR(20) NULL;

-- NEW this session
ALTER TABLE profiles
  ADD COLUMN mobile_no     VARCHAR(10) NULL,
  ADD COLUMN address_line  VARCHAR(255) NULL,
  ADD COLUMN state         VARCHAR(100) NULL,
  ADD COLUMN pincode       VARCHAR(6) NULL;
```

---

## 🔄 Session History

| Date | What was done |
|---|---|
| 2026-06-27 | Full code review, LikeController, InterestController, @EnableAsync, Agent.md |
| 2026-06-27 | Fixed circular dependency |
| 2026-06-28 | Fixed /interests/received, CORS config, account deletion, photo preview fix |
| 2026-06-29 | Expectations feature (backend + frontend), Set Primary Photo |
| 2026-06-30 | Full Expectations + Profile expansion (maritalStatus, height, income, gotra, diet) |
| 2026-07-01 | See details below |

### 2026-07-01 — Address/Mobile, Search, Photo limit, Navbar DP, Footer

**Backend new files:**
- `mapper/ProfileMapper.java` — shared entity→DTO mapper (fixes DiscoverService drift bug)
- `dto/response/ProfileSearchResultDto.java` — lightweight search result (profileId + name + DP)

**Backend modified files:**
- `entity/Profile.java` — added mobileNo, addressLine, state, pincode
- `dto/request/ProfileRequest.java` — added required mobileNo, addressLine, state, pincode with validation
- `dto/response/ProfileResponse.java` — added mobileNo (masked), addressLine, state, pincode
- `repository/ProfileRepository.java` — added searchByFullNameContainingIgnoreCase() JPQL query
- `service/ProfileService.java` — MAX_PHOTOS 5→10, uses ProfileMapper, maps new address/mobile fields
- `service/DiscoverService.java` — uses ProfileMapper, added searchByName() method
- `controller/DiscoverController.java` — added GET /api/discover/search?keyword= endpoint

**Mobile masking logic:**
- Owner (getMyProfile) → full number shown
- Other users (getProfileById, discover) → first 2 digits visible, rest `*` (e.g. `98********`)
- Masking done in ProfileMapper.maskMobileIfNeeded()

**Frontend modified files:**
- `api/discoverApi.js` — added searchProfiles(keyword)
- `pages/DiscoverPage.jsx` — search bar with 400ms debounce + dropdown with DP + name
- `components/profile/ProfileForm.jsx` — added mobileNo, addressLine, state, pincode fields with validation
- `components/profile/PhotoUpload.jsx` — MAX_PHOTOS constant 5→10
- `components/common/Navbar.jsx` — profile button replaced with circular DP avatar + red ❗ badge when expectations empty
- `pages/LoginPage.jsx` — "Developed by Pragnesh Maru ❤️" footer
- `pages/RegisterPage.jsx` — "Developed by Pragnesh Maru ❤️" footer

### 2026-07-01 — Backend Logging Implementation (Antigravity session)

**Backend new files:**
- `src/main/resources/logback-spring.xml` — standard Logback configuration (daily rollover, max 10MB split, console/file)
- `config/RequestLoggingFilter.java` — logs incoming HTTP requests (method, URI, status, duration) in custom format

**Backend modified files:**
- `exception/GlobalExceptionHandler.java` — added `@Slf4j`, logs unhandled exceptions with full stack trace
- `util/JwtUtil.java` — added `@Slf4j`, logs expired/malformed/invalid JWT tokens as warnings
- `service/AuthService.java` — logs registration success, login success, and login failure reasons
- `service/OtpService.java` — logs OTP generation and verification without printing sensitive codes
- `service/ProfileService.java` — logs profile create, update, photo upload/delete, expectations save, and account deletion
- `service/DiscoverService.java` — logs searches performed (keyword, resultsCount, userId)

---

## ▶️ Next Session — Resume Here

**Priority order:**
1. ⚠️ Run the DB migration SQL above before starting the backend
2. Fix Matches bug (receiver vs sender logic in /api/interests/matches)
3. Admin Panel (list users, deactivate accounts)
4. Suggestion / Bug Report Page (deferred)
