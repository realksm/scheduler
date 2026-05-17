-- ScheduleFlow Database Initialization Script
-- This script runs automatically when Postgres container starts

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- CORE TABLES
-- ============================================

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    username VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500),
    timezone VARCHAR(100) DEFAULT 'UTC',
    locale VARCHAR(10) DEFAULT 'en',
    email_verified BOOLEAN DEFAULT FALSE,
    oauth_provider VARCHAR(50),
    oauth_provider_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Teams table
CREATE TABLE IF NOT EXISTS teams (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    logo_url VARCHAR(500),
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Team members table
CREATE TABLE IF NOT EXISTS team_members (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(team_id, user_id)
);

-- Event types table
CREATE TABLE IF NOT EXISTS event_types (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    slug VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    duration_minutes INTEGER NOT NULL DEFAULT 30,
    color VARCHAR(7) DEFAULT '#3b82f6',
    location VARCHAR(500),
    video_meeting_app VARCHAR(50) DEFAULT 'google_meet',
    is_private BOOLEAN DEFAULT FALSE,
    custom_questions JSONB DEFAULT '[]',
    requires_confirmation BOOLEAN DEFAULT FALSE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    team_id UUID REFERENCES teams(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, slug)
);

-- Bookings table
CREATE TABLE IF NOT EXISTS bookings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    uid VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    timezone VARCHAR(100) NOT NULL,
    status VARCHAR(50) DEFAULT 'CONFIRMED',
    location VARCHAR(500),
    meeting_link VARCHAR(500),
    cancellation_reason TEXT,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_type_id UUID NOT NULL REFERENCES event_types(id) ON DELETE CASCADE,
    canceler_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    cancel_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Booking attendees table
CREATE TABLE IF NOT EXISTS booking_attendees (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    timezone VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Calendars table
CREATE TABLE IF NOT EXISTS calendars (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    integration VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    access_token TEXT,
    refresh_token TEXT,
    token_expires_at TIMESTAMP WITH TIME ZONE,
    external_calendar_id VARCHAR(255),
    is_primary BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Calendar events table (synced events)
CREATE TABLE IF NOT EXISTS calendar_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    calendar_id UUID NOT NULL REFERENCES calendars(id) ON DELETE CASCADE,
    external_event_id VARCHAR(255) NOT NULL,
    title VARCHAR(255),
    description TEXT,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    location VARCHAR(500),
    attendees JSONB DEFAULT '[]',
    is_recurring BOOLEAN DEFAULT FALSE,
    recurrence_rule VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(calendar_id, external_event_id)
);

-- Availability rules table
CREATE TABLE IF NOT EXISTS availability_rules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    days_of_week INTEGER[] NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    dateOverride DATE,
    is_recurring BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Time slots table (for caching generated slots)
CREATE TABLE IF NOT EXISTS time_slots (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    event_type_id UUID NOT NULL REFERENCES event_types(id) ON DELETE CASCADE,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(event_type_id, start_time)
);

-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    booking_id UUID REFERENCES bookings(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    channel VARCHAR(20) DEFAULT 'EMAIL',
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    content TEXT,
    status VARCHAR(50) DEFAULT 'PENDING',
    scheduled_at TIMESTAMP WITH TIME ZONE,
    sent_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- INDEXES
-- ============================================

CREATE INDEX IF NOT EXISTS idx_bookings_user_id ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_event_type_id ON bookings(event_type_id);
CREATE INDEX IF NOT EXISTS idx_bookings_start_time ON bookings(start_time);
CREATE INDEX IF NOT EXISTS idx_event_types_user_id ON event_types(user_id);
CREATE INDEX IF NOT EXISTS idx_availability_rules_user_id ON availability_rules(user_id);
CREATE INDEX IF NOT EXISTS idx_time_slots_event_type_id ON time_slots(event_type_id);
CREATE INDEX IF NOT EXISTS idx_calendar_events_calendar_id ON calendar_events(calendar_id);

-- ============================================
-- TRIGGER FOR UPDATED_AT
-- ============================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_teams_updated_at BEFORE UPDATE ON teams FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_event_types_updated_at BEFORE UPDATE ON event_types FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_bookings_updated_at BEFORE UPDATE ON bookings FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_calendars_updated_at BEFORE UPDATE ON calendars FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_calendar_events_updated_at BEFORE UPDATE ON calendar_events FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_availability_rules_updated_at BEFORE UPDATE ON availability_rules FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_notifications_updated_at BEFORE UPDATE ON notifications FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- DUMMY DATA
-- ============================================

-- Insert dummy users
INSERT INTO users (id, email, username, full_name, email_verified, timezone) VALUES
('11111111-1111-1111-1111-111111111111', 'alice@example.com', 'alice', 'Alice Johnson', true, 'America/New_York'),
('22222222-2222-2222-2222-222222222222', 'bob@example.com', 'bob', 'Bob Smith', true, 'America/Los_Angeles'),
('33333333-3333-3333-3333-333333333333', 'carol@example.com', 'carol', 'Carol Williams', true, 'Europe/London');

-- Insert dummy team
INSERT INTO teams (id, name, slug, owner_id) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Acme Corp', 'acme-corp', '11111111-1111-1111-1111-111111111111');

-- Add team members
INSERT INTO team_members (team_id, user_id, role) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'OWNER'),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222', 'MEMBER'),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '33333333-3333-3333-3333-333333333333', 'ADMIN');

-- Insert dummy event types
INSERT INTO event_types (slug, title, description, duration_minutes, color, user_id, video_meeting_app) VALUES
('15-minute-call', '15 Minute Call', 'Quick chat to discuss your needs', 15, '#10b981', '11111111-1111-1111-1111-111111111111', 'google_meet'),
('30-minute-meeting', '30 Minute Meeting', 'Standard meeting duration', 30, '#3b82f6', '11111111-1111-1111-1111-111111111111', 'zoom'),
('60-minute-consultation', '60 Minute Consultation', 'In-depth consultation session', 60, '#8b5cf6', '11111111-1111-1111-1111-111111111111', 'google_meet'),
('team-sync', 'Team Sync', 'Weekly team synchronization meeting', 45, '#f59e0b', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'google_meet');

-- Insert availability rules (Mon-Fri, 9am-5pm)
INSERT INTO public.availability_rules (
    user_id, 
    days_of_week, 
    start_time, 
    end_time, 
    dateoverride, 
    is_recurring
) VALUES 
-- 1. Alice: Standard Mon-Fri Work Week (Recurring)
(
    '11111111-1111-1111-1111-111111111111', 
    '{1,2,3,4,5}', 
    '09:00:00', 
    '17:00:00', 
    NULL, 
    TRUE
),

-- 2. Alice: Specific Date Override (Working a special shift on New Year's Day)
-- Note: Uses the empty array '{}' to satisfy your NOT NULL constraint
(
    '11111111-1111-1111-1111-111111111111', 
    '{}', 
    '10:00:00', 
    '15:00:00', 
    '2026-01-01', 
    FALSE
),

-- 3. Bob: Part-Time Mon, Wed, Fri Shift (Recurring)
(
    '22222222-2222-2222-2222-222222222222', 
    '{1,3,5}', 
    '08:00:00', 
    '12:00:00', 
    NULL, 
    TRUE
),

-- 4. Carol: Weekend Shift (Recurring Sat/Sun)
(
    '33333333-3333-3333-3333-333333333333', 
    '{6,0}', 
    '10:00:00', 
    '16:00:00', 
    NULL, 
    TRUE
),

-- 5. Carol: Specific Date Override (Christmas Day custom availability)
(
    '33333333-3333-3333-3333-333333333333', 
    '{}', 
    '13:00:00', 
    '19:00:00', 
    '2026-12-25', 
    FALSE
);

-- Insert dummy bookings
INSERT INTO bookings (uid, title, start_time, end_time, timezone, status, user_id, event_type_id) VALUES
('book-001', 'Discovery Call', '2026-05-11 10:00:00+00', '2026-05-11 10:30:00+00', 'America/New_York', 'CONFIRMED', '11111111-1111-1111-1111-111111111111', (SELECT id FROM event_types WHERE slug = '15-minute-call')),
('book-002', 'Product Demo', '2026-05-12 14:00:00+00', '2026-05-12 14:30:00+00', 'America/Los_Angeles', 'CONFIRMED', '11111111-1111-1111-1111-111111111111', (SELECT id FROM event_types WHERE slug = '30-minute-meeting')),
('book-003', 'Strategy Session', '2026-05-13 09:00:00+00', '2026-05-13 10:00:00+00', 'Europe/London', 'PENDING', '33333333-3333-3333-3333-333333333333', (SELECT id FROM event_types WHERE slug = '60-minute-consultation'));

-- Insert booking attendees
INSERT INTO booking_attendees (booking_id, email, name, timezone) VALUES
((SELECT id FROM bookings WHERE uid = 'book-001'), 'client1@example.com', 'Client One', 'America/New_York'),
((SELECT id FROM bookings WHERE uid = 'book-002'), 'client2@example.com', 'Client Two', 'America/Los_Angeles'),
((SELECT id FROM bookings WHERE uid = 'book-003'), 'client3@example.com', 'Client Three', 'Europe/London');

-- Insert pending notifications
INSERT INTO notifications (user_id, booking_id, type, recipient, subject) VALUES
('11111111-1111-1111-1111-111111111111', (SELECT id FROM bookings WHERE uid = 'book-001'), 'BOOKING_CONFIRMATION', 'client1@example.com', 'Your meeting is confirmed'),
('11111111-1111-1111-1111-111111111111', (SELECT id FROM bookings WHERE uid = 'book-002'), 'BOOKING_REMINDER', 'client2@example.com', 'Meeting reminder: Product Demo');

INSERT INTO public.calendars (
    user_id,
    integration,
    email,
    access_token,
    refresh_token,
    token_expires_at,
    external_calendar_id,
    is_primary,
    is_active
) VALUES 
-- Row 1: Alice's Primary Google Work Calendar
(
    '11111111-1111-1111-1111-111111111111', -- Alice
    'google',
    'alice@example.com',
    'ya29.v1_alice_google_access_token_abc123',
    '1//0_alice_google_refresh_token_xyz987',
    NOW() + INTERVAL '1 hour',
    'alice@example.com',
    TRUE,
    TRUE
),

-- Row 2: Alice's Secondary Shared Team Calendar (Testing multi-calendar sync)
(
    '11111111-1111-1111-1111-111111111111', -- Alice
    'google',
    'alice@example.com',
    'ya29.v1_alice_google_access_token_abc123',
    '1//0_alice_google_refresh_token_xyz987',
    NOW() + INTERVAL '1 hour',
    'company.marketing#holiday@group.v.calendar.google.com',
    FALSE, -- Secondary
    TRUE
),

-- Row 3: Bob's Primary Outlook Calendar
(
    '22222222-2222-2222-2222-222222222222', -- Bob
    'outlook',
    'bob@example.com',
    'eyJ0eXAiOiJKV_bob_token...',
    'MC9_bob_outlook_refresh_token',
    NOW() + INTERVAL '1 hour',
    'AAMkAGI2b_bob_calendar_id...',
    TRUE,
    TRUE
),

-- Row 4: Carol's Primary Outlook Calendar (Expired Token - Needs Refresh testing)
(
    '33333333-3333-3333-3333-333333333333', -- Carol
    'outlook',
    'carol@example.com',
    'expired_carol_access_token_456',
    'carol_refresh_me_token_789',
    NOW() - INTERVAL '45 minutes', -- Expired 45 mins ago
    'AAMkAGI3c_carol_calendar_id...',
    TRUE,
    TRUE
),

-- Row 5: Carol's Secondary Google Calendar (Deactivated/Paused by user)
(
    '33333333-3333-3333-3333-333333333333', -- Carol
    'google',
    'carol.personal@gmail.com',
    'ya29.carol_personal_token',
    '1//0_carol_personal_refresh',
    NOW() - INTERVAL '10 days',
    'carol.personal@gmail.com',
    FALSE,
    FALSE -- Explicitly inactive
);

INSERT INTO public.calendar_events (
    calendar_id,
    external_event_id,
    title,
    description,
    start_time,
    end_time,
    location,
    attendees,
    is_recurring,
    recurrence_rule
) VALUES 
-- Row 1: A standard 1-on-1 Sync on Alice's primary Google calendar
(
    (SELECT id FROM public.calendars WHERE email = 'alice@example.com' AND is_primary = TRUE LIMIT 1),
    'gcal_evt_998123abc',
    'Project Kickoff Sync',
    'Initial alignment meeting to finalize project scope and milestones.',
    '2026-06-01 10:00:00 -0400', -- Respecting Alice's America/New_York timezone
    '2026-06-01 11:00:00 -0400',
    'Zoom Video Call',
    '[
        {"name": "Alice Johnson", "email": "alice@example.com", "response": "accepted"},
        {"name": "Bob Smith", "email": "bob@example.com", "response": "tentative"}
     ]'::jsonb,
    FALSE,
    NULL
),

-- Row 2: A Marketing holiday event on Alice's secondary shared calendar
(
    (SELECT id FROM public.calendars WHERE email = 'alice@example.com' AND is_primary = FALSE LIMIT 1),
    'gcal_evt_holiday_xyz789',
    'Company Summer Picnic',
    'Annual team outing! Food and drinks provided.',
    '2026-07-04 12:00:00 -0400',
    '2026-07-04 17:00:00 -0400',
    'Central Park, Sector 4',
    '[]'::jsonb, -- Empty JSONB array fallback
    FALSE,
    NULL
),

-- Row 3: A recurring design standup on Bob's primary Outlook calendar
(
    (SELECT id FROM public.calendars WHERE email = 'bob@example.com' AND is_primary = TRUE LIMIT 1),
    'outlook_evt_772183hd',
    'Daily Design Standup',
    'Quick check-in on UI design review progress.',
    '2026-06-02 09:00:00 -0700', -- Respecting Bob's America/Los_Angeles timezone
    '2026-06-02 09:30:00 -0700',
    'Microsoft Teams',
    '[
        {"name": "Bob Smith", "email": "bob@example.com", "response": "accepted"}
     ]'::jsonb,
    TRUE,
    'FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR' -- iCalendar RFC 5545 recurrence string format
),

-- Row 4: An external medical appointment on Carol's primary Outlook calendar
(
    (SELECT id FROM public.calendars WHERE email = 'carol@example.com' AND is_primary = TRUE LIMIT 1),
    'outlook_evt_990111xx',
    'Dentist Checkup',
    'Routine cleaning and checkup.',
    '2026-06-05 14:00:00 +0100', -- Respecting Carol's Europe/London timezone
    '2026-06-05 15:00:00 +0100',
    'Dental Care Clinic, London',
    '[]'::jsonb,
    FALSE,
    NULL
),

-- Row 5: A lunch interview block on Carol's primary Outlook calendar
(
    (SELECT id FROM public.calendars WHERE email = 'carol@example.com' AND is_primary = TRUE LIMIT 1),
    'outlook_evt_110222yy',
    'Lunch & Interview - Senior Engineer',
    'Review technical assessment during the final round panel.',
    '2026-06-10 12:30:00 +0100',
    '2026-06-10 13:30:00 +0100',
    'Meeting Room 3B',
    '[
        {"name": "Carol Williams", "email": "carol@example.com", "response": "accepted"},
        {"name": "Candidate X", "email": "candidate@external.com", "response": "accepted"}
     ]'::jsonb,
    FALSE,
    NULL
);

-- ============================================
-- AUTH MODULE TABLES
-- ============================================

-- Password Reset Tokens
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Refresh Tokens (Session Management)
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- OAuth Accounts (link OAuth to users)
CREATE TABLE IF NOT EXISTS oauth_accounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    provider VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    access_token TEXT,
    refresh_token TEXT,
    token_expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(provider, provider_user_id)
);

-- Indexes for Auth Tables
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_token ON password_reset_tokens(token);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_oauth_accounts_user_id ON oauth_accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_oauth_accounts_provider ON oauth_accounts(provider);