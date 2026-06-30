# Agent.md — PrajapatiSamaj Matrimonial Platform

> Maintained by Claude. Update after every session.
> Last updated: 2026-06-30

---

## 📁 Project Overview

| Field | Details |
|---|---|
| Project Name | PrajapatiSamaj Matrimonial Platform |
| Location | D:\Projects\PrajapatiSamaj |
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Database | MySQL (`matrimonial_db`) |
| Architecture | Layered: Controller → Service → Repository |
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
│                    ExpectationResponse
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
| 4 | Profile Create / Update | ✅ Done | Includes new fields: maritalStatus, height, income, gotra, diet |
| 5 | Photo Upload (max 5) | ✅ Done | |
| 6 | Photo Delete | ✅ Done | |
| 7 | Set Primary Photo | ✅ Done | PUT /api/profile/photos/{id}/primary |
| 8 | Partner Preference | ✅ Done | |
| 9 | Partner Expectations | ✅ Done | Full feature — backend + frontend — see Session 2026-06-30 |
| 10 | Browse / Discover Profiles | ✅ Done | |
| 11 | View Another User's Profile | ✅ Done | |
| 12 | Like a Profile (3/day free) | ✅ Done | |
| 13 | View Who Liked Me | ✅ Done | |
| 14 | Send Interest Request | ✅ Done | |
| 15 | Accept / Decline Interest | ✅ Done | |
| 16 | View Received Interests | ✅ Done | |
| 17 | View Mutual Matches | ✅ Done | |
| 18 | Email Notifications (all) | ✅ Done | |
| 19 | JWT Auth Filter | ✅ Done | |
| 20 | Global Exception Handler | ✅ Done | |
| 21 | @EnableAsync | ✅ Done | |
| 22 | Circular dependency fix | ✅ Done | CustomUserDetailsService |
| 23 | CORS config | ✅ Done | Allows http://localhost:5173 |
| 24 | Delete Account (full) | ✅ Done | DELETE /api/account |
| 25 | Photo instant preview on upload | ✅ Done | Frontend only fix |
| 26 | Admin Panel | ❌ Pending | |

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
| GET | /api/profiles/{id} | ✅ |
| DELETE | /api/account | ✅ |

### Discovery (JWT Required)
| Method | URL | Status |
|---|---|---|
| GET | /api/discover | ✅ |

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

## 🐛 Bugs Fixed

| Date | Bug | Fix |
|---|---|---|
| 2026-06-27 | Circular dependency JwtAuthFilter ↔ SecurityConfig | Extracted CustomUserDetailsService |
| 2026-06-28 | /api/interests/received returned broken JSON | Created InterestResponse DTO, fixed lazy loading |
| 2026-06-28 | CORS blocking browser requests from Vite | Added CorsConfigurationSource bean in SecurityConfig |
| 2026-06-28 | Photo not visible during upload / no instant preview | Added local object URL preview in PhotoUpload.jsx |

---

## 🚨 Known Issues / Deferred

| # | Issue | Priority |
|---|---|---|
| 1 | Matches bug — always picks receiver as matched person | 🟠 Fix before frontend uses /matches |
| 2 | Duplicate buildProfileResponse() in ProfileService & DiscoverService | 🟡 Code quality — before Phase 2 |
| 3 | deletePhoto throws IOException unnecessarily | 🟡 Minor cleanup |
| 4 | Photo MIME type spoofable | 🟡 Low risk Phase 1 |
| 5 | Goodbye email on account delete | 🔵 Phase 2 |
| 6 | DB migration needed: add new columns to profiles + expectations tables | 🟠 Must run ALTER before testing |

---

## 🔒 Phase 2 — Locked

| Feature | Notes |
|---|---|
| Premium Filter Pack | Age, height, city, religion, education, profession, diet, gotra |
| View Contact Number | Reveal phone after match (paid) |
| Unlimited Likes | Free = 3/day |
| Payment Gateway | Razorpay or Stripe |
| Goodbye email on account delete | Send email before deleting account |
| Token blacklist / logout | Server-side logout |
| AWS S3 photo storage | Replace local disk |

---

## 🔄 Session History

| Date | What was done |
|---|---|
| 2026-06-27 | Full code review, created LikeController, InterestController, @EnableAsync, Agent.md |
| 2026-06-27 | Fixed circular dependency — CustomUserDetailsService |
| 2026-06-28 | Fixed /interests/received lazy loading bug — InterestResponse DTO |
| 2026-06-28 | Added CORS config for Vite (localhost:5173) |
| 2026-06-28 | Implemented full account deletion — DELETE /api/account |
| 2026-06-28 | Fixed photo instant preview on upload — PhotoUpload.jsx (frontend only) |
| 2026-06-29 | Implemented Expectations feature — backend + frontend (Step 2) |
| 2026-06-29 | Implemented Set Primary Photo — backend endpoint + frontend ⭐ button (Step 3) |
| 2026-06-30 | Full Expectations + Profile expansion — see details below |

### 2026-06-30 — Expectations & Profile Expansion (Full)

**New shared enums:**
- `com.matrimonial.entity.enums.MaritalStatus` — SINGLE, DIVORCED, WIDOWED
- `com.matrimonial.entity.enums.Diet` — VEG, NON_VEG, VEGAN

**Profile entity — new fields added:**
- `maritalStatus` (MaritalStatus enum, required in DTO, nullable in DB for old rows)
- `height` (String, optional, e.g. "5'8\"")
- `income` (String, optional, e.g. "50,000/month")
- `gotra` (String, optional)
- `diet` (Diet enum, optional)

**Expectation entity — new fields added:**
- `preferredMaritalStatus`, `preferredMinHeight`, `preferredMaxHeight`
- `preferredIncome`, `preferredGotra`, `preferredDiet`

**Backend files updated:**
- `Profile.java` — new fields
- `Expectation.java` — new fields
- `ProfileRequest.java` — new fields + validation
- `ExpectationRequest.java` — new fields
- `ProfileResponse.java` — new fields + expectations embedded
- `ExpectationResponse.java` — new fields
- `ProfileService.java` — full rewrite with new fields in create/update/expectations
- `ProfileController.java` — added GET + PUT /api/profile/expectations

**Frontend files updated/created:**
- `profileApi.js` — added getMyExpectations(), saveExpectations()
- `ProfileForm.jsx` — added maritalStatus, height, income, gotra, diet fields
- `EditProfilePage.jsx` — added Section 2: Partner Expectations button → /profile/expectations
- `ExpectationsPage.jsx` — NEW page at /profile/expectations (full form, all fields)
- `App.jsx` — added /profile/expectations route (lazy loaded, protected)

---

## ⚠️ DB Migration Required

Before testing, run these SQL statements on `matrimonial_db`:

```sql
-- Profile table: add new columns
ALTER TABLE profiles
  ADD COLUMN marital_status VARCHAR(20) NULL,
  ADD COLUMN height         VARCHAR(20) NULL,
  ADD COLUMN income         VARCHAR(100) NULL,
  ADD COLUMN gotra          VARCHAR(100) NULL,
  ADD COLUMN diet           VARCHAR(20) NULL;

-- Expectations table: add new columns
ALTER TABLE expectations
  ADD COLUMN preferred_marital_status VARCHAR(20) NULL,
  ADD COLUMN preferred_min_height     VARCHAR(20) NULL,
  ADD COLUMN preferred_max_height     VARCHAR(20) NULL,
  ADD COLUMN preferred_income         VARCHAR(100) NULL,
  ADD COLUMN preferred_gotra          VARCHAR(100) NULL,
  ADD COLUMN preferred_diet           VARCHAR(20) NULL;
```

---

## ▶️ Next Session — Resume Here

**Priority order:**
1. ⚠️ Run the DB migration SQL above before starting the backend
2. Fix Matches bug (receiver vs sender logic) — needed before frontend uses /matches
3. Admin Panel (list users, deactivate accounts)

After Admin Panel → Phase 1 complete → Phase 2 planning.
