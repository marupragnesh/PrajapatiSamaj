# Agent.md — PrajapatiSamaj Matrimonial Platform

> Maintained by Claude. Update after every session.
> Last updated: 2026-06-27

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
│   └── response/    ApiResponse, AuthResponse, ProfileResponse
│                    LikerSafeView, MatchResponse
├── exception/       GlobalExceptionHandler, BadRequestException
│                    ResourceNotFoundException, UnauthorizedException
└── util/            JwtUtil, OtpUtil, EmailTemplateUtil
```

---

## ✅ Phase 1 — Feature Progress

| # | Feature | Status | Notes |
|---|---|---|---|
| 1 | User Registration (Email + Password) | ✅ Done | BCrypt hash, JWT returned |
| 2 | Login (JWT) | ✅ Done | AuthenticationManager used |
| 3 | Forgot Password — OTP flow | ✅ Done | 3-step: forgot → verify → reset |
| 4 | Profile Create / Update | ✅ Done | One profile per user |
| 5 | Photo Upload (max 5) | ✅ Done | Saved locally under uploads/photos |
| 6 | Photo Delete | ✅ Done | Ownership check, auto-reassign primary |
| 7 | Partner Preference (gender filter) | ✅ Done | MALE / FEMALE / ANY |
| 8 | Browse / Discover Profiles | ✅ Done | Paginated, gender-pref filtered |
| 9 | View Another User's Profile | ✅ Done | Only complete profiles shown |
| 10 | Like a Profile (3/day free) | ✅ Done | Daily limit, no duplicate, no self-like |
| 11 | View Who Liked Me (safe view) | ✅ Done | No contact info in LikerSafeView |
| 12 | Send Interest Request | ✅ Done | PENDING / re-send if DECLINED |
| 13 | Accept / Decline Interest | ✅ Done | Receiver only, PENDING only |
| 14 | View Received Interests | ✅ Done | PENDING list |
| 15 | View Mutual Matches | ✅ Done | Both accepted each other |
| 16 | Email — OTP | ✅ Done | HTML email, async |
| 17 | Email — Like Notification | ✅ Done | Async |
| 18 | Email — Interest Notification | ✅ Done | Async |
| 19 | Email — Interest Accepted | ✅ Done | Async |
| 20 | JWT Auth Filter | ✅ Done | Stateless, validates every request |
| 21 | Global Exception Handler | ✅ Done | 400/403/404/500 mapped |
| 22 | LikeController | ✅ Done | Created 2026-06-27 |
| 23 | InterestController | ✅ Done | Created 2026-06-27 |
| 24 | @EnableAsync on main app | ✅ Done | Added 2026-06-27 |
| 25 | Circular dependency fix | ✅ Done | CustomUserDetailsService extracted 2026-06-27 |
| 26 | Admin Panel (basic) | ❌ Pending | Not started yet |

---

## 🚨 Known Issues / TODOs

### CODE QUALITY — Deferred (not blocking Phase 1)

1. **Duplicate `buildProfileResponse()` method**
   - Same logic in both `ProfileService` and `DiscoverService`
   - Fix: Extract to `ProfileMapper` utility class (DRY principle)
   - Deferred — tackle before Phase 2

2. **`deletePhoto` in ProfileController declares `throws IOException` unnecessarily**
   - Service method does not throw `IOException`
   - Minor cleanup only

3. **Photo upload MIME type validation is spoofable**
   - Currently checks `contentType.startsWith("image/")` only
   - Consider adding file extension whitelist (jpg, jpeg, png)
   - Deferred — low risk for Phase 1

---

## 🐛 Bugs Fixed

| Date | Bug | Fix |
|---|---|---|
| 2026-06-27 | Circular dependency: JwtAuthFilter ↔ SecurityConfig | Extracted `CustomUserDetailsService` as standalone `@Component`. `SecurityConfig` no longer defines `UserDetailsService` as a `@Bean`. `JwtAuthFilter` is now injected as method param in `securityFilterChain()`. |

---

## 🔒 Phase 2 — Locked (Not Started)

| Feature | Notes |
|---|---|
| Premium Filter Pack (Rs. 50 / 3 months) | Age range, height, city, religion, education, profession |
| View Contact Number (Rs. 100 / 3 months) | Reveal registered mobile number |
| Unlimited Likes per day | Included in any premium plan |
| Higher profile visibility | TBD |
| Payment Gateway | Razorpay or Stripe |
| `user_subscriptions` table | Needed to track plan type + expiry |
| Premium middleware / feature flags | Check subscription at Service layer |

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

## 🏗️ Architecture Notes

- **Layered strictly**: Controller → Service → Repository
- **No business logic in Controllers** — only HTTP in/out
- **No DB queries in Services** — only via Repository
- **Entities never returned directly** — always mapped to DTOs
- **JWT stateless** — no server session, every request validates token
- **@Async email** — enabled via `@EnableAsync` on `MatrimonialApplication`
- **UserDetailsService** — lives in `CustomUserDetailsService` (standalone `@Component`, not a `@Bean` in `SecurityConfig`)

---

## 🔄 Session History

| Date | What was done |
|---|---|
| 2026-06-27 | Full code review. All base structure verified. |
| 2026-06-27 | Created LikeController, InterestController, added @EnableAsync. Agent.md created. |
| 2026-06-27 | Fixed circular dependency (JwtAuthFilter ↔ SecurityConfig). Created CustomUserDetailsService. |

---

## ▶️ Next Session — Resume Here

**Phase 1 core is COMPLETE. App starts successfully.**

Next task: **Admin Panel (Basic)**
- List all users (paginated)
- View any user's profile
- Deactivate / reactivate a user account (`is_active` flag on `User` entity)
- Separate admin role or secured by a fixed admin credential in config

After Admin Panel → **Phase 1 is 100% done** → start Phase 2 planning.
