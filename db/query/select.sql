SELECT id,
       email,
       password_hash,
       username,
       full_name,
       avatar_url,
       timezone,
       locale,
       email_verified,
       oauth_provider,
       oauth_provider_id,
       created_at,
       updated_at
FROM public.users;