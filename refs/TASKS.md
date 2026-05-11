# ScheduleFlow - Team Assignment Plan
## Architecture Overview
- **Frontend**: Next.js 14 (App Router), React, TypeScript, TailwindCSS, shadcn/ui
- **Backend**: Spring Boot 3.x (Java 21), PostgreSQL, Redis
- **Infrastructure**: Docker Compose (MVP), Kubernetes-ready
## Team Structure
| Member | Focus Area | Primary Responsibility |
|--------|-----------|----------------------|
| Member A | Auth + User Management | Login, profiles, OAuth, settings |
| Member B | Availability + Calendar | Sync, slot engine, availability rules |
| Member C | Booking + Events | Event types, booking flow, notifications |
## Phase 1: Foundation (Weeks 1-3)
### Week 1: Project Setup
| Task | Assigned | Deliverable |
|------|----------|--------------|
| Initialize Spring Boot backend | Member A | Backend repo |
| Setup PostgreSQL + Redis + Docker | Member C | Local dev env |
| Initialize Next.js frontend | Member B | Frontend repo |
| Configure Tailwind + design tokens | Member B | Design system |
### Week 2: Authentication
- Member A: User entity, JWT auth, OAuth flows
- Member B: Login/Signup pages, auth context
- Member C: Team entity
### Week 3: User Management
- Member A: Profile API, password reset
- Member B: Profile settings UI
- Member C: Team workspace UI
## Phase 2: Calendar Integration (Weeks 4-6)
- Member B: Calendar OAuth, sync service, availability engine
- Member C: Override UI, availability settings
## Phase 3: Booking Core (Weeks 7-9)
- Member C: Event types, booking API, notifications
- Member A: Booking flow UI, confirmation pages
- Member B: Slot selection, double-booking prevention
## Phase 4: Video + Polish (Weeks 10-11)
- Member B: Google Meet, Zoom, Teams integration
- Member A: Video settings UI, polish
- Member C: UX refinements
## Phase 5: Enterprise (Week 12)
- All: Analytics, embed widget, API docs, MVP ship
## Key Dependencies
- Member A's auth before B/C test protected routes
- Member B's slot API before A builds booking page
- Member C's Docker ready Week 1
---