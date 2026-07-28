# Agent.md — PrajapatiSamaj Matrimonial Platform

> Maintained by Claude. Update after every session.
> Last updated: 2026-07-04

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
| 32 | Resend OTP Feature | ✅ Done | Cooldown timer + backend endpoint |
| 33 | Family Details & Profile Description | ✅ Done | Added father/mother name (mandatory) & occupations (optional), description, and mobileNo display in profile section |
| 34 | Photo Grid Wrap & Instagram Profile Header | ✅ Done | Wrapped photo thumbnails in grid (no scrollbar) & added DP avatar, user email, Edit Profile / Edit Expectation tabs |
| 35 | Discover Page Filters | ✅ Done | Age range, Marital status, Height range, Diet filters with live updates, clear buttons & popup modal |
| 36 | Remove Name & Surname from Register | ✅ Done | Ask only during profile setup, remove validation and fields from register frontend & backend, sync to User table on profile save |
| 37 | New Registration Fields, Today Stats & Legal Pages | ✅ Done | Added DOB, Birth time, Weight (int kg), Blood group, Birthplace, Gujarati Mangal/Sani toggles, Alternate number, mandatory Education/Profession, Today's profile registration count page with Refresh button, & 5 Legal Policy pages copied from Razorpay site |
| 38 | Fix StatsController ApiResponse signature mismatch | ✅ Done | StatsController now returns ResponseEntity<StatsResponse> directly |
| 39 | Total User Stats, Profile Accordions, Expectations Expansion & Email Fix | ✅ Done | Added total user count, collapsible profile page accordions, expanded expectation fields (State, Mangal/Shani, Weight), reordered About Developer page, & fixed Gmail SMTP SSL port 465 timeout |
| 40 | Fix Delete Account FK Constraint Error | ✅ Done | Deletes payments associated with user prior to user entity deletion |
| 41 | Email Unsubscribe Preferences, Profile Action UI States & Premium About Button | ✅ Done | Added notification settings (like, interest, accept interest email toggles), persistent Liked / Interest Sent / Accepted UI buttons on profile details, and subtle About & Stats button in Premium page |
| 42 | Instagram-Style Photo Preview, Aspect Ratio & Crop Adjustment Modal | ✅ Done | Added multi-photo file manager selection, interactive HTML5 canvas cropping, 4:5/1:1/16:9/Original aspect ratios, zoom slider, 90° rotation, drag-to-pan repositioning, and multi-photo adjustment queue |

---

## 📋 REST API Endpoints

### Auth & Public
| Method | URL | Status |
|---|---|---|
| POST | /api/auth/register | ✅ Sends OTP |
| POST | /api/auth/register/resend-otp | ✅ NEW |
| POST | /api/auth/register/verify-otp | ✅ Activates & returns JWT |
| POST | /api/auth/login | ✅ |
| POST | /api/auth/forgot-password | ✅ |
| POST | /api/auth/verify-otp | ✅ |
| POST | /api/auth/reset-password | ✅ |
| GET | /api/stats/today-registrations | ✅ Public endpoint returning today's profile creation count |
| POST | /api/auth/register/resend-otp | ✅ NEW |
| POST | /api/auth/register/verify-otp | ✅ Activates & returns JWT |
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
| POST | /api/account/delete-otp | ✅ NEW |
| DELETE | /api/account?otpCode= | ✅ OTP Protected |

### Discovery (JWT Required)
| Method | URL | Status |
|---|---|---|
| GET | /api/discover?page=0&size=10&minAge=&maxAge=&maritalStatus=&minHeight=&maxHeight=&diet= | ✅ Filtered |
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

-- NEW: Separate Name & Surname
ALTER TABLE users
  ADD COLUMN name          VARCHAR(50) NULL,
  ADD COLUMN surname       VARCHAR(50) NULL;

ALTER TABLE profiles
  ADD COLUMN name          VARCHAR(50) NULL,
  ADD COLUMN surname       VARCHAR(50) NULL;

-- Populate name & surname from existing full_name
UPDATE profiles SET name = SUBSTRING_INDEX(full_name, ' ', 1), surname = SUBSTRING_INDEX(full_name, ' ', -1) WHERE name IS NULL OR name = '';
UPDATE users u JOIN profiles p ON u.id = p.user_id SET u.name = p.name, u.surname = p.surname WHERE u.name IS NULL OR u.name = '';
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

### 2026-07-01 — Resend OTP Implementation & SMTP App Password Update

- Updated `spring.mail.password` in `application.properties` to the new App Password `zsuqgqijbjoncvos`.
- Added `POST /api/auth/resend-otp` endpoint to `AuthController.java`.
- Added `resendOtp` to `authApi.js` in frontend.
- Added "Resend OTP" button with a 30-second cooldown timer to `VerifyOtpPage.jsx`.
- Fixed bug where frontend was sending `{ otp }` instead of `{ otpCode }` to the backend.
- Fixed backend bug where OTP was verified twice (once during verify-otp and again during reset-password), resulting in an "Invalid OTP" error on password reset since it was marked used in the first call.

### 2026-07-05 — Discover Page Filters Implementation

**Backend changes:**
- `dto/request/DiscoverFilterRequest.java` — added optional DTO (`minAge`, `maxAge`, `maritalStatus`, `minHeight`, `maxHeight`, `diet`)
- `repository/specification/ProfileSpecification.java` — dynamic JPA Specification for age range, marital status, diet, and multi-format height range parsing (`4.8`, `4'8`, `4'8"`, `5.8`, `6.2`)
- `repository/ProfileRepository.java` — extended `JpaSpecificationExecutor<Profile>`
- `service/DiscoverService.java` — updated `discoverProfiles` to use `ProfileSpecification` and log filter execution details
- `controller/DiscoverController.java` — updated `GET /api/discover` endpoint to bind `@ModelAttribute DiscoverFilterRequest filter`

**Frontend changes:**
- `api/discoverApi.js` — updated `discoverProfiles` to pass non-empty filter parameters
- `components/discover/FilterPopup.jsx` — created popup modal component with 4 filter fields, staging state with "Apply Filters" button, individual X clear buttons, and Clear All
- `pages/DiscoverPage.jsx` — integrated Filter button with active filter counter badge, active filter chips bar, and FilterPopup modal

### 2026-07-05 — OTP-Verified Account Deletion Implementation

**Backend changes:**
- `service/ProfileService.java` — added `sendDeleteAccountOtp(email)` and updated `deleteAccount(email, otpCode)` to verify OTP before purging profile data
- `controller/ProfileController.java` — added `POST /api/account/delete-otp` and updated `DELETE /api/account` to accept `@RequestParam String otpCode`

**Frontend changes:**
- `api/accountApi.js` — added `requestDeleteAccountOtp()` and updated `deleteAccount(otpCode)`
- `context/AuthContext.jsx` — updated `deleteAccount(otpCode)` to pass OTP code
- `pages/EditProfilePage.jsx` — built OTP verification modal with resend countdown timer for account deletion

### 2026-07-05 — Registration Email OTP Verification Implementation

**Backend changes:**
- `application.properties` — updated `otp.expiry.minutes=5` (5-minute OTP validity)
- `service/AuthService.java` — updated `register` to create inactive user & email 5-minute OTP, added `resendRegistrationOtp`, and `verifyRegistrationOtp` to activate account & issue JWT
- `controller/AuthController.java` — updated `POST /api/auth/register` and added `POST /api/auth/register/resend-otp` and `POST /api/auth/register/verify-otp`

**Frontend changes:**
- `api/authApi.js` — added `verifyRegistrationOtp` and `resendRegistrationOtp`
- `pages/RegisterPage.jsx` — implemented 2-step registration flow (Form → OTP Verification UI with 120-second cooldown timer)

### 2026-07-05 — Profile Hobbies Fix, Independent Edit Profile Sections & Are-You-Sure Delete Confirmation

**Frontend changes:**
- `components/profile/ProfileForm.jsx` — added missing `hobbies: initialData.hobbies || ''` in form state initialization so hobbies are retained and saved properly
- `pages/EditProfilePage.jsx` — relocated Profile Photos, Partner Gender Preference, and Danger Zone sections outside tab conditional rendering so scrolling down shows them on BOTH Edit Profile and Edit Expectation tabs; added "Are you sure?" confirmation popup modal before sending account deletion email OTP

### 2026-07-07 — Separate Name & Surname and Surname Filter

**Backend changes:**
- `dto/request/RegisterRequest.java` — added `name` and `surname` fields
- `entity/User.java` — added `name` and `surname` fields
- `dto/response/AuthResponse.java` — added `name` and `surname` fields
- `service/AuthService.java` — save name/surname to User entity on register and map to AuthResponse on login/verification
- `entity/Profile.java` — added `name` and `surname` fields
- `dto/request/ProfileRequest.java` — added `name` and `surname` fields (made `fullName` optional)
- `dto/response/ProfileResponse.java` — added `name` and `surname` fields
- `mapper/ProfileMapper.java` — map `name` and `surname` from Profile entity to response DTO
- `service/ProfileService.java` — populate name/surname and construct `fullName` automatically from name and surname
- `dto/request/DiscoverFilterRequest.java` — added optional `surname` parameter
- `repository/specification/ProfileSpecification.java` — add dynamic case-insensitive exact matching trimmed surname filter

**Frontend changes:**
- `api/authApi.js` — update `registerUser` payload
- `components/auth/RegisterForm.jsx` — added Name and Surname fields with validations
- `pages/RegisterPage.jsx` — pass name and surname inputs on register submit
- `components/profile/ProfileForm.jsx` — replaced Full Name input with First Name and Surname inputs with validation and backward compatibility split fallback logic
- `pages/ProfileSetupPage.jsx` — retrieve and pre-populate user name and surname from auth context
- `components/discover/FilterPopup.jsx` — added Surname textbox filter
- `pages/DiscoverPage.jsx` — sync surname filter state, chips, and clear action

### 2026-07-09 — Remove Name & Surname from Register Page

**Backend changes:**
- `dto/request/RegisterRequest.java` — removed validation annotations and fields for name/surname
- `service/AuthService.java` — stopped setting name/surname during registration
- `service/ProfileService.java` — synchronized name/surname changes from Profile creation and update to the associated User entity (users table)

**Frontend changes:**
- `api/authApi.js` — updated `registerUser` payload to exclude name and surname
- `components/auth/RegisterForm.jsx` — removed Name and Surname input fields, states, and validations
- `pages/RegisterPage.jsx` — updated submit handler to exclude name and surname

### 2026-07-21 — Configurable RazorpayProperties Integration

**Backend changes:**
- `config/RazorpayProperties.java` — created strongly-typed `@ConfigurationProperties(prefix = "razorpay.key")` component with `@Value` fallbacks for `razorpay.key.id` and `razorpay.key.secret`
- `config/RazorpayConfig.java` — updated to inject `RazorpayProperties` bean for creating `RazorpayClient`
- `service/PaymentService.java` — updated to inject `RazorpayProperties` bean instead of individual `@Value` fields

### 2026-07-23 — Email Templates, Clickable Brand Logo, Compass Icon & Masked Contact Info Button

**Backend changes:**
- `util/EmailTemplateUtil.java` — added dedicated HTML templates: `buildRegistrationEmail` (Registration OTP) & `buildAccountDeletionEmail` (Account Deletion OTP)
- `service/EmailService.java` — added `sendRegistrationOtpEmail`, `sendForgotPasswordOtpEmail`, and `sendAccountDeletionOtpEmail`
- `service/OtpService.java` — added `OtpPurpose` enum (`REGISTRATION`, `FORGOT_PASSWORD`, `ACCOUNT_DELETION`) to send purpose-appropriate emails
- `service/AuthService.java` & `service/ProfileService.java` — updated OTP send calls to specify purpose (`REGISTRATION`, `FORGOT_PASSWORD`, `ACCOUNT_DELETION`)

**Frontend changes:**
- `components/common/Navbar.jsx` — made `PrajapatiSamaj` brand title clickable leading to `/discover`; replaced broken lotus emoji with universal `🌸` emblem
- `components/common/Navbar.jsx` & `pages/DiscoverPage.jsx` — updated Discover nav icon to `🧭 Discover` (Compass)
- `pages/ProfileDetailPage.jsx` — restored masked mobile number (`99********`) with a small `ⓘ` info button opening a Premium Info Modal with upgrade link

### 2026-07-26 — Generic ApiResponse<T> DTO Implementation

**Backend changes:**
- `dto/response/ApiResponse.java` — converted `ApiResponse` to generic class `ApiResponse<T>`, added `private T data;` field with `@JsonInclude(JsonInclude.Include.NON_NULL)` and `ApiResponse.success(String message, T data)` static factory method overload. Resolves compilation error in `StatsController.java` when parameterizing `ApiResponse<StatsResponse>`.

---

## ▶️ Next Session — Resume Here

**Priority order:**
1. Fix Matches bug (receiver vs sender logic in /api/interests/matches)
2. Admin Panel (list users, deactivate accounts)
3. Suggestion / Bug Report Page (deferred)


