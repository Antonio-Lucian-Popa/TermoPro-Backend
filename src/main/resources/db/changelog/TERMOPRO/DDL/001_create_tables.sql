-- 1. Firme
CREATE TABLE company (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Utilizatori
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    password_hash TEXT NOT NULL,
    role VARCHAR(50) NOT NULL, -- OWNER, MANAGER, TECHNICIAN, OPERATOR
    keycloak_id UUID UNIQUE NOT NULL,
    company_id UUID REFERENCES company(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Echipe
CREATE TABLE team (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    company_id UUID REFERENCES company(id)
);

-- 4. Legătură utilizatori - echipe (many-to-many)
CREATE TABLE team_members (
    team_id UUID REFERENCES team(id),
    user_id UUID REFERENCES users(id),
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (team_id, user_id)
);

-- 5. Comenzi (customer_order)
CREATE TABLE customer_order (
    id UUID PRIMARY KEY,
    client_name VARCHAR(255) NOT NULL,
    client_phone VARCHAR(50),
    client_address TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    scheduled_date DATE,
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, IN_PROGRESS, DONE, CANCELLED
    company_id UUID REFERENCES company(id),
    team_id UUID REFERENCES team(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. Taskuri (pentru echipă SAU un lucrător individual)
CREATE TABLE task (
    id UUID PRIMARY KEY,
    order_id UUID REFERENCES customer_order(id),
    team_id UUID REFERENCES team(id),           -- NULL dacă e individual
    user_id UUID REFERENCES users(id),          -- NULL dacă e pentru echipă
    title VARCHAR(255) NOT NULL,
    description TEXT,
    task_type VARCHAR(50),                      -- MONTARE, MASURARE, LIVRARE etc.
    status VARCHAR(50) DEFAULT 'NOT_STARTED',   -- NOT_STARTED, IN_PROGRESS, COMPLETED, INCOMPLETE
    scheduled_date DATE NOT NULL,
    assigned_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Fie team_id, fie user_id trebuie să fie NOT NULL
    CHECK (team_id IS NOT NULL OR user_id IS NOT NULL)
);

-- 7. Actualizări status pentru task
CREATE TABLE task_update (
    id UUID PRIMARY KEY,
    task_id UUID REFERENCES task(id),
    user_id UUID REFERENCES users(id),
    status VARCHAR(50),         -- e.g. IN_PROGRESS, COMPLETED
    comment TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE task_update_photo (
    id UUID PRIMARY KEY,
    task_update_id UUID REFERENCES task_update(id) ON DELETE CASCADE,
    photo_url TEXT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- 8. Concedii & Invoiri
CREATE TABLE user_time_off (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    type VARCHAR(50),           -- CONCEDIU, INVOIRE
    start_time TIME,            -- pentru invoiri parțiale
    end_time TIME,
    approved BOOLEAN DEFAULT FALSE
);

-- 9. Invitații (pentru înregistrarea controlată a utilizatorilor)
CREATE TABLE invitation (
    id UUID PRIMARY KEY,
    token VARCHAR(255) UNIQUE NOT NULL,
    employee_email VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL, -- OWNER, MANAGER, TECHNICIAN, OPERATOR
    company_id UUID REFERENCES company(id),
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);