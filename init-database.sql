-- Создание таблиц (если не созданы через Hibernate)
CREATE TABLE IF NOT EXISTS "user" (
                                      id BIGSERIAL PRIMARY KEY,
                                      email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('USER', 'ADMIN'))
    );

CREATE TABLE IF NOT EXISTS card (
                                    id BIGSERIAL PRIMARY KEY,
                                    user_id BIGINT NOT NULL REFERENCES "user"(id),
    encrypted_pan TEXT NOT NULL,
    owner_name VARCHAR(255) NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'BLOCKED', 'EXPIRED')),
    balance NUMERIC(19,2) DEFAULT 0.00 NOT NULL
    );

CREATE TABLE IF NOT EXISTS card_audit (
                                          id BIGSERIAL PRIMARY KEY,
                                          card_id BIGINT NOT NULL REFERENCES card(id),
    user_id BIGINT NOT NULL REFERENCES "user"(id),
    action VARCHAR(50) NOT NULL,
    description TEXT,
    old_balance NUMERIC(19,2),
    new_balance NUMERIC(19,2),
    old_status VARCHAR(20),
    new_status VARCHAR(20),
    performed_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Создание тестовых пользователей
-- Пароли закодированы BCrypt: 'password123'
INSERT INTO "user" (email, password, role) VALUES
                                               ('admin@bank.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN'),
                                               ('user1@bank.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER'),
                                               ('user2@bank.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER')
    ON CONFLICT (email) DO NOTHING;

-- Создание тестовых карт (PAN будет зашифрован приложением)
-- PAN в реальности будет зашифрован, здесь для демонстрации
INSERT INTO card (user_id, encrypted_pan, owner_name, expiry_date, status, balance) VALUES
                                                                                        ((SELECT id FROM "user" WHERE email = 'user1@bank.com'), 'encrypted_pan_1', 'Иван Иванов', '2026-12-01', 'ACTIVE', 5000.00),
                                                                                        ((SELECT id FROM "user" WHERE email = 'user1@bank.com'), 'encrypted_pan_2', 'Иван Иванов', '2027-06-01', 'ACTIVE', 3000.00),
                                                                                        ((SELECT id FROM "user" WHERE email = 'user2@bank.com'), 'encrypted_pan_3', 'Петр Петров', '2025-03-01', 'ACTIVE', 10000.00),
                                                                                        ((SELECT id FROM "user" WHERE email = 'user2@bank.com'), 'encrypted_pan_4', 'Петр Петров', '2024-01-01', 'EXPIRED', 0.00)
    ON CONFLICT DO NOTHING;

-- Индексы для производительности
CREATE INDEX IF NOT EXISTS idx_card_user_id ON card(user_id);
CREATE INDEX IF NOT EXISTS idx_card_status ON card(status);
CREATE INDEX IF NOT EXISTS idx_card_audit_card_id ON card_audit(card_id);
CREATE INDEX IF NOT EXISTS idx_card_audit_user_id ON card_audit(user_id);
CREATE INDEX IF NOT EXISTS idx_user_email ON "user"(email);

-- Информация о созданных данных
SELECT 'База данных инициализирована!' as message;
SELECT 'Созданы пользователи:' as info;
SELECT id, email, role FROM "user";
SELECT 'Созданы карты:' as info;
SELECT c.id, u.email as user_email, c.owner_name, c.status, c.balance
FROM card c
         JOIN "user" u ON c.user_id = u.id;