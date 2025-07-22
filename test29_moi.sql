create database swp_test29
use swp_test29

CREATE TABLE Users (
    user_id BIGINT PRIMARY KEY IDENTITY(1,1),
    username NVARCHAR(50) UNIQUE NOT NULL,
    password NVARCHAR(100) NOT NULL,
    email NVARCHAR(100) UNIQUE NOT NULL,
    first_name NVARCHAR(50),
    last_name NVARCHAR(50),
    gender NVARCHAR(10) CHECK (gender IN ('Male', 'Female')),
    date_of_birth DATE,
    role NVARCHAR(10) NOT NULL DEFAULT 'Guest' CHECK (role IN ('Guest', 'Member', 'Coach', 'Admin')),
    member_plan NVARCHAR(10) NULL CHECK (member_plan IN ('FREE', 'VIP', 'PREMIUM')),
    created_at DATE DEFAULT GETDATE(),
    status NVARCHAR(10) NOT NULL DEFAULT 'OFFLINE' CHECK (status IN ('ONLINE', 'OFFLINE'))
);
CREATE TABLE notification (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    user_id BIGINT NOT NULL,
    content NVARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    is_read BIT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

CREATE TABLE message_home (
    message_id BIGINT PRIMARY KEY IDENTITY(1,1),
    user_id BIGINT NOT NULL,
    content NVARCHAR(500) NOT NULL,
    sent_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE Orders (
    order_id BIGINT PRIMARY KEY IDENTITY(1,1),
    user_id BIGINT NOT NULL FOREIGN KEY REFERENCES Users(user_id),
    member_plan VARCHAR(10) NULL CHECK (member_plan IN ('FREE', 'VIP', 'PREMIUM')),
    amount DECIMAL(12,2),
	 order_code BIGINT UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME DEFAULT GETDATE(),
    confirmed_at DATETIME NULL
);

CREATE TABLE password_reset_token (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    token NVARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    expiry_date DATETIME2 NOT NULL,
    CONSTRAINT FK_User_PasswordReset FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

CREATE TABLE question (
    id INT IDENTITY(1,1) PRIMARY KEY,
    question_text NVARCHAR(255) NOT NULL
);
CREATE TABLE question_option (
    id INT IDENTITY(1,1) PRIMARY KEY,
    question_id INT FOREIGN KEY REFERENCES question(id),
    option_text NVARCHAR(255) NOT NULL
);

CREATE TABLE user_answer (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL FOREIGN KEY REFERENCES Users(user_id),
    question_id INT NOT NULL FOREIGN KEY REFERENCES question(id),
    option_id INT NOT NULL FOREIGN KEY REFERENCES question_option(id),
    created_at DATETIME DEFAULT GETDATE()
);
CREATE TABLE analysis_result (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL FOREIGN KEY REFERENCES users(user_id),
    analysis NVARCHAR(255) NOT NULL,
    recommendation NVARCHAR(MAX) NOT NULL,
    created_at DATETIME DEFAULT GETDATE()
);
-- Bảng chính lưu kế hoạch bỏ thuốc
CREATE TABLE quit_plan (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    start_date DATE,
    target_date DATE,
    stages NVARCHAR(255),
    custom_plan NVARCHAR(2000),
    method NVARCHAR(50) CHECK (method IN ('quit_abruptly', 'reduce_gradually')) NOT NULL DEFAULT 'reduce_gradually',
    daily_smoking_cigarettes INT,
    daily_spending DECIMAL(12,2),
    user_id BIGINT,
    CONSTRAINT FK_quit_plan_user FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- Bảng chính lưu kế hoạch bỏ thuốc
CREATE TABLE user_plan_step (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    date DATE NOT NULL,
    day_index INT NOT NULL,
    target_cigarettes INT NOT NULL,
    actual_cigarettes INT NULL,
    avoided_cigarettes INT NULL,
    money_saved INT NULL,
    completed BIT NOT NULL DEFAULT 0,
    quit_plan_id BIGINT NOT NULL,
    CONSTRAINT fk_user_plan_step_quit_plan 
        FOREIGN KEY (quit_plan_id) REFERENCES quit_plan(id) ON DELETE CASCADE
);
-- Bảng phụ lưu danh sách lý do (1 kế hoạch có nhiều lý do)
CREATE TABLE quit_plan_reasons (
    plan_id BIGINT NOT NULL,
    reason NVARCHAR(255) NOT NULL,
    CONSTRAINT FK_quit_plan_reasons_plan FOREIGN KEY (plan_id) REFERENCES quit_plan(id) ON DELETE CASCADE
);

CREATE TABLE chat_message (
    id INT IDENTITY(1,1) PRIMARY KEY,
    sender_id NVARCHAR(255) NOT NULL,
    receiver_id NVARCHAR(255) NOT NULL,
    content NVARCHAR(MAX) NOT NULL,
    timestamp DATETIME2 NOT NULL,
    
);

CREATE TABLE Feedback (
    feedback_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(255),
    rating INT,
    comment NVARCHAR(MAX),
    create_at DATETIME2,
    user_id BIGINT,   -- Changed from INT to BIGINT
    coach_id BIGINT,  -- Already BIGINT, now consistent

    CONSTRAINT FK_Feedback_User
        FOREIGN KEY (user_id)
        REFERENCES Users(user_id), -- Now BIGINT references BIGINT

    CONSTRAINT FK_Feedback_Coach
        FOREIGN KEY (coach_id)
        REFERENCES Users(user_id) -- Now BIGINT references BIGINT
        ON DELETE SET NULL
);





-- Question 1
INSERT INTO question (question_text) VALUES ('How old are you?');
INSERT INTO question_option (question_id, option_text) VALUES
    (SCOPE_IDENTITY(), 'Under 18'),
    (SCOPE_IDENTITY(), '18-24'),
    (SCOPE_IDENTITY(), '25-34'),
    (SCOPE_IDENTITY(), '35-44'),
    (SCOPE_IDENTITY(), '45+');

-- Question 2
INSERT INTO question (question_text) VALUES ('How many cigarettes do you smoke per day?');
INSERT INTO question_option (question_id, option_text) VALUES
    (SCOPE_IDENTITY(), 'None, I don''t smoke daily'),
    (SCOPE_IDENTITY(), '1-5'),
    (SCOPE_IDENTITY(), '6-10'),
    (SCOPE_IDENTITY(), '11-20'),
    (SCOPE_IDENTITY(), 'More than 20');

-- Question 3
INSERT INTO question (question_text) VALUES ('At what age did you start smoking?');
INSERT INTO question_option (question_id, option_text) VALUES
    (SCOPE_IDENTITY(), 'Under 16'),
    (SCOPE_IDENTITY(), '16-18'),
    (SCOPE_IDENTITY(), '19-25'),
    (SCOPE_IDENTITY(), 'Over 25');

-- Question 4
INSERT INTO question (question_text) VALUES ('How much money do you spend on cigarettes per week?');
INSERT INTO question_option (question_id, option_text) VALUES
    (SCOPE_IDENTITY(), 'Less than 50,000 VND'),
    (SCOPE_IDENTITY(), '50,000 - 150,000 VND'),
    (SCOPE_IDENTITY(), '150,000 - 300,000 VND'),
    (SCOPE_IDENTITY(), 'More than 300,000 VND');

-- Question 5
INSERT INTO question (question_text) VALUES ('Have you tried to quit smoking before?');
INSERT INTO question_option (question_id, option_text) VALUES
    (SCOPE_IDENTITY(), 'Yes, multiple times'),
    (SCOPE_IDENTITY(), 'Yes, once'),
    (SCOPE_IDENTITY(), 'No, I have never tried');

-- Question 6
INSERT INTO question (question_text) VALUES ('Do you smoke more when you''re stressed?');
INSERT INTO question_option (question_id, option_text) VALUES
    (SCOPE_IDENTITY(), 'Yes, significantly more'),
    (SCOPE_IDENTITY(), 'Yes, slightly more'),
    (SCOPE_IDENTITY(), 'No, about the same'),
    (SCOPE_IDENTITY(), 'I smoke less when stressed');

-- Question 7
INSERT INTO question (question_text) VALUES ('Do you smoke indoors or outdoors?');
INSERT INTO question_option (question_id, option_text) VALUES
    (SCOPE_IDENTITY(), 'Primarily indoors'),
    (SCOPE_IDENTITY(), 'Primarily outdoors'),
    (SCOPE_IDENTITY(), 'Both equally');

-- Question 8
INSERT INTO question (question_text) VALUES ('Do you live with someone who also smokes?');
INSERT INTO question_option (question_id, option_text) VALUES
    (SCOPE_IDENTITY(), 'Yes'),
    (SCOPE_IDENTITY(), 'No');




INSERT INTO Users (username, password, email, first_name, last_name, gender, date_of_birth, role, member_plan, created_at)
VALUES
('johnsmith', 'password123', 'john.smith@example.com', 'John', 'Smith', 'Male', '1990-05-15', 'Member', 'FREE', '2024-02-11'),
('janedoe', 'securepass', 'jane.doe@example.com', 'Jane', 'Doe', 'Female', '1992-08-23', 'Member', 'VIP', '2024-03-29'),
('michaelbrown', 'pass123', 'michael.brown@example.com', 'Michael', 'Brown', 'Male', '1985-12-11', 'Coach', 'PREMIUM', '2024-07-17'),
('emilywhite', 'mypassword', 'emily.white@example.com', 'Emily', 'White', 'Female', '1995-04-30', 'Guest', NULL, '2024-09-03'),
('robertjohnson', 'password456', 'robert.johnson@example.com', 'Robert', 'Johnson', 'Male', '1988-09-19', 'Member', 'FREE', '2024-11-21'),
('oliviamartin', 'olivia123', 'olivia.martin@example.com', 'Olivia', 'Martin', 'Female', '1993-06-07', 'Guest', NULL, '2024-12-08'),
('williamlee', 'wlee2025', 'william.lee@example.com', 'William', 'Lee', 'Male', '1980-03-22', 'Admin', NULL, '2025-01-13'),
('sophiagarcia', 'sophiaG!', 'sophia.garcia@example.com', 'Sophia', 'Garcia', 'Female', '1997-11-14', 'Member', 'PREMIUM', '2025-02-26'),
('davidclark', 'clark987', 'david.clark@example.com', 'David', 'Clark', 'Male', '1991-02-28', 'Coach', 'VIP', '2025-03-15'),
('isabellawilson', 'bellaPass', 'isabella.wilson@example.com', 'Isabella', 'Wilson', 'Female', '1999-10-01', 'Guest', NULL, '2025-04-05'),
('aliceblue', 'alice123', 'alice.blue@example.com', 'Alice', 'Blue', 'Female', '1990-04-10', 'Member', 'FREE', '2024-02-03'),
('bobbrown', 'bobpass', 'bob.brown@example.com', 'Bob', 'Brown', 'Male', '1987-07-22', 'Member', 'VIP', '2024-06-12'),
('carolgreen', 'carolG2025', 'carol.green@example.com', 'Carol', 'Green', 'Female', '1992-03-19', 'Coach', 'PREMIUM', '2024-08-05'),
('danielblack', 'danielB', 'daniel.black@example.com', 'Daniel', 'Black', 'Male', '1985-09-05', 'Guest', NULL, '2024-10-19'),
('ellaorange', 'ellaPass', 'ella.orange@example.com', 'Ella', 'Orange', 'Female', '1994-11-29', 'Member', 'VIP', '2024-12-27'),
('frankgray', 'frank123', 'frank.gray@example.com', 'Frank', 'Gray', 'Male', '1989-06-14', 'Admin', NULL, '2025-01-22'),
('gracewhite', 'gracePW', 'grace.white@example.com', 'Grace', 'White', 'Female', '1998-12-03', 'Member', 'PREMIUM', '2025-03-03'),
('henryred', 'henryR45', 'henry.red@example.com', 'Henry', 'Red', 'Male', '1983-01-27', 'Coach', 'VIP', '2025-05-09'),
('irislime', 'iris789', 'iris.lime@example.com', 'Iris', 'Lime', 'Female', '1996-08-16', 'Guest', NULL, '2025-05-27'),
('jackcyan', 'jack2025', 'jack.cyan@example.com', 'Jack', 'Cyan', 'Male', '1991-10-02', 'Member', 'FREE', '2025-06-20');
INSERT INTO Users (username, password, email, first_name, last_name, role)
VALUES

('member', '123', 'member@example.com', 'Member', 'User', 'Member'),
('member2', '123', 'member2@example.com', 'Member', 'User', 'Member'),
('coach', '123', 'coach@example.com', 'Coach', 'User', 'Coach'),
('coach2', '123', 'coach2@example.com', 'Coach', 'User', 'Coach'),
('admin', '123', 'admin@example.com', 'Admin', 'User', 'Admin');

ALTER TABLE user_plan_step ADD avoided_cigarettes INT;
ALTER TABLE user_plan_step ADD money_saved INT;
