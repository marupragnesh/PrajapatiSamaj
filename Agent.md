# Agent.md — PrajapatiSamaj Matrimonial Platform

> Maintained by Claude. Update after every session.
> Last updated: 2026-06-28

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
│                    PartnerPreferenceRepository
├── entity/          User, Profile, ProfilePhoto, Like, InterestRequest
│                    OtpToken, PartnerPreference
├── dto/
│   ├── request/     RegisterRequest, LoginRequest, ForgotPasswordRequest
│   │                VerifyOtpRequest, ResetPasswordRequest
│   │                ProfileRequest, PreferenceRequest
│   └── response/    ApiResponse, AuthResponse, ProfileResponse, PhotoDto
│                    LikerSafeView, MatchResponse, InterestResponse
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
| 4 | Profile Create / Update | ✅ Done | |
| 5 | Photo Upload (max 5) | ✅ Done | |
| 6 | Photo Delete | ✅ Done | |
| 7 | Partner Preference | ✅ Done | |
| 8 | Browse / Discover Profiles | ✅ Done | |
| 9 | View Another User's Profile | ✅ Done | |
| 10 | Like a Profile (3/day free) | ✅ Done | |
| 11 | View Who Liked Me | ✅ Done | |
| 12 | Send Interest Request | ✅ Done | |
| 13 | Accept / Decline Interest | ✅ Done | |
| 14 | View Received Interests | ✅ Done | |
| 15 | View Mutual Matches | ✅ Done | |
| 16 | Email Notifications (all) | ✅ Done | |
| 17 | JWT Auth Filter | ✅ Done | |
| 18 | Global Exception Handler | ✅ Done | |
| 19 | LikeController | ✅ Done | |
| 20 | InterestController | ✅ Done | |
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

## 🐛 Bugs Fixed

| Date | Bug | Fix |
|---|---|---|
| 2026-06-27 | Circular dependency JwtAuthFilter ↔ SecurityConfig | Extracted CustomUserDetailsService |
| 2026-06-28 | /api/interests/received returned broken JSON | Created InterestResponse DTO, fixed lazy loading |
| 2026-06-28 | CORS blocking browser requests from Vite | Added CorsConfigurationSource bean in SecurityConfig |
| 2026-06-28 | Photo not visible during upload / no instant preview | Added local object URL preview in PhotoUpload.jsx; fixed compressed Blob filename |

---

## 🚨 Known Issues / Deferred

| # | Issue | Priority |
|---|---|---|
| 1 | Matches bug — always picks receiver as matched person | 🟠 Fix before frontend uses /matches |
| 2 | Duplicate buildProfileResponse() in ProfileService & DiscoverService | 🟡 Code quality — before Phase 2 |
| 3 | deletePhoto throws IOException unnecessarily | 🟡 Minor cleanup |
| 4 | Photo MIME type spoofable | 🟡 Low risk Phase 1 |
| 5 | Goodbye email on account delete | 🔵 Phase 2 |

---

## 🔒 Phase 2 — Locked

| Feature | Notes |
|---|---|
| Premium Filter Pack | Age, height, city, religion, education, profession |
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
| 2026-06-28 | Fixed photo instant preview on upload — PhotoUpload.jsx (frontend only, no backend change) |

---

## ▶️ Next Session — Resume Here

**Priority order:**
1. Fix Matches bug (receiver vs sender logic) — needed before frontend uses /matches
2. Admin Panel (list users, deactivate accounts)

After Admin Panel → Phase 1 complete → Phase 2 planning.
