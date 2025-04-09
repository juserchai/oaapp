-- Insert initial admin user (password is 'password')
INSERT INTO users (id, username, password, name, email, role, phone, department, position, avatar, enabled, created_at, updated_at) 
VALUES ('1', 'admin', '$2a$10$yH7tpTsn9QXHnCqPJ/F6/.w/wCsmwQIpKrTtl0JFb3RsTcWmkqpnq', 'Administrator', 'admin@example.com', 'ADMIN', NULL, NULL, NULL, NULL, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- Insert user roles
INSERT INTO user_roles (user_id, role) VALUES ('1', 'ROLE_ADMIN');
INSERT INTO user_roles (user_id, role) VALUES ('1', 'ROLE_USER');

-- Insert normal user (password is 'password')
INSERT INTO users (id, username, password, name, email, role, phone, department, position, avatar, enabled, created_at, updated_at) 
VALUES ('2', 'user', '$2a$10$yH7tpTsn9QXHnCqPJ/F6/.w/wCsmwQIpKrTtl0JFb3RsTcWmkqpnq', 'Regular User', 'user@example.com', 'USER', NULL, NULL, NULL, NULL, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- Insert user roles
INSERT INTO user_roles (user_id, role) VALUES ('2', 'ROLE_USER'); 