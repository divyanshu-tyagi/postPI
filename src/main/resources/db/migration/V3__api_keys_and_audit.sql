CREATE TABLE api_keys (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          key_hash VARCHAR(255) NOT NULL UNIQUE,
                          name VARCHAR(255) NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE audit_log (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           table_name VARCHAR(255) NOT NULL,
                           operation VARCHAR(20) NOT NULL,
                           principal_type VARCHAR(20) NOT NULL,
                           principal_id VARCHAR(255) NOT NULL,
                           row_id VARCHAR(255),
                           created_at TIMESTAMP NOT NULL DEFAULT now()
);