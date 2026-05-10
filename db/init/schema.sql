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
INSERT INTO availability_rules (user_id, days_of_week, start_time, end_time) VALUES
('11111111-1111-1111-1111-111111111111', ARRAY[1,2,3,4,5], '09:00', '17:00'),
('22222222-2222-2222-2222-222222222222', ARRAY[1,2,3,4,5], '08:00', '16:00'),
('33333333-3333-3333-3333-333333333333', ARRAY[1,2,3,4,5], '10:00', '18:00');

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