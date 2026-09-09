INSERT INTO users (id, email, password_hash)
VALUES ('00000000-0000-0000-0000-000000000001', 'demo3@postpi.dev', '$2a$10$GHDxuqos7.NKFYQnBZyItejhhKdJnVHb8xnMWs7E4U5J4JPimihfG');

INSERT INTO projects (id, name, owner_id)
VALUES ('00000000-0000-0000-0000-000000000002', 'Demo Project', '00000000-0000-0000-0000-000000000001');

INSERT INTO table_policies (table_name, operation, column_name)
VALUES
    ('projects', 'SELECT', 'owner_id'),
    ('projects', 'INSERT', 'owner_id'),
    ('users', 'SELECT', 'id');