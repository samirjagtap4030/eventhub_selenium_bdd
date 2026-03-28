# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
EventHub is a full-stack event ticket booking platform built for QA training. Users can browse events, book tickets, manage bookings, and create events. Each user operates in an isolated sandbox.

## Tech Stack
- **Frontend**: Next.js 14 (App Router), React 18, TypeScript, Tailwind CSS, React Query v5
- **Backend**: Express.js, Prisma ORM, MySQL 8+
- **Auth**: JWT (7-day expiry), bcryptjs
- **Testing**: Playwright E2E (Chromium only)

## Project Structure
```
eventhub/
├── frontend/          # Next.js 14 app (port 3000)
│   ├── app/           # Pages (App Router)
│   ├── components/    # React components
│   ├── lib/           # API clients, hooks, providers
│   └── types/         # TypeScript interfaces
├── backend/           # Express API (port 3001)
│   ├── src/
│   │   ├── routes/        # HTTP endpoints
│   │   ├── controllers/   # Request handlers
│   │   ├── services/      # Business logic
│   │   ├── repositories/  # Data access (Prisma)
│   │   ├── validators/    # Input validation
│   │   └── middleware/    # Auth, error handling
│   └── prisma/            # Schema + seed
├── tests/             # Playwright E2E tests
└── .claude/
    ├── commands/      # Custom slash commands (agents)
    └── skills/        # Skill documents (reference guides)
```

## Architecture Pattern
Backend follows layered architecture: Routes → Controllers → Services → Repositories → Database

## Commands to Run
```bash
npm run setup        # Install all dependencies (backend + frontend)
npm run dev          # Start frontend + backend concurrently
npm run seed         # Seed 10 static events
npm run migrate      # Run Prisma migrations (dev)
npm run test         # Run all Playwright tests
npm run test:ui      # Playwright with UI mode
npm run test:report  # Open last HTML test report
npx playwright test tests/<file>.spec.js --reporter=line  # Run single test
```

Backend-only commands (run from repo root):
```bash
npm run db:push                               # Push schema changes without migration
cd backend && npx prisma studio               # Open Prisma Studio (DB GUI)
cd backend && npx prisma migrate deploy       # Deploy migrations to production
```

## API Documentation
Swagger UI is available at `http://localhost:3001/api-docs` when the backend is running.

## Environment Setup
Copy and fill in:
- `backend/.env.example` → `backend/.env` (DATABASE_URL, JWT_SECRET, PORT, CORS_ORIGIN)
- `frontend/.env.local.example` → `frontend/.env.local` (NEXT_PUBLIC_API_URL)

## Testing Conventions
- Test files go in `tests/` as `<feature-name>.spec.js`
- **Tests run against `https://eventhub.rahulshettyacademy.com` (production) by default** — see `baseURL` in `playwright.config.ts`
- Follow guidelines in `.claude/skills/playwright-best-practices/SKILL.md`
- Locator priority: data-testid > role > label/placeholder > ID > CSS class
- No `page.waitForTimeout()` — use `expect().toBeVisible()`
- Tests must be self-contained (login → action → assert)
- Use test accounts: `rahulshetty1@gmail.com` / `Magiclife1!`

## Key Business Rules
- Max 6 user-created events (FIFO pruning on overflow)
- Max 9 bookings per user (FIFO pruning on overflow)
- Booking ref first character = event title first character (uppercase)
- Seat count reduces on booking, restores on cancellation
- Refund eligibility: 1 ticket = eligible, >1 ticket = not eligible (client-side)
- Cross-user booking access returns "Access Denied"
- Static events (seeded, `isStatic: true`) are immutable

## Custom Slash Commands (Agents)
- `/generate-tests <feature>` — AI Test Automation Engineer: generates Playwright tests
- `/review-tests <file>` — AI Code Reviewer: reviews test code quality
- `/create-scenarios <area>` — AI Functional Tester: creates test scenario documents
- `/test-strategy <scenarios>` — AI Test Architect: assigns tests to optimal pyramid layers

## Skill Documents
- `.claude/skills/playwright-best-practices/SKILL.md` — Playwright testing standards
- `.claude/skills/eventhub-domain/SKILL.md` — Domain knowledge and business rules

## Code Style
- Backend: JavaScript with JSDoc, Express patterns
- Frontend: TypeScript, React hooks, Tailwind utility classes
- Tests: JavaScript with Playwright test runner
- Use meaningful variable names, add step comments in tests
- Keep functions focused and single-responsibility
