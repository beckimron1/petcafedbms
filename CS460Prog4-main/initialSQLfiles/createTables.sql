/* Membership tiers */

CREATE TABLE Membership_Tier (
    tier_id       NUMBER PRIMARY KEY,
    tier_name     VARCHAR2(50) NOT NULL,
    description   VARCHAR2(200),
    discount_rate NUMBER(3) CHECK (discount_rate BETWEEN 0 AND 100),
    visit_limit   NUMBER
);

/* Members */

CREATE TABLE Member (
    member_id         NUMBER PRIMARY KEY,
    name              VARCHAR2(100) NOT NULL,
    phone             VARCHAR2(20),
    email             VARCHAR2(100),
    date_of_birth     DATE,
    emergency_contact VARCHAR2(200),
    tier_id           NUMBER,
    CONSTRAINT fk_member_tier
        FOREIGN KEY (tier_id)
            REFERENCES Membership_Tier (tier_id)
            ON DELETE SET NULL
);

/* Staff */

CREATE TABLE Staff (
    staff_id  NUMBER PRIMARY KEY,
    name      VARCHAR2(100) NOT NULL,
    phone     VARCHAR2(20),
    email     VARCHAR2(100),
    hire_date DATE,
    is_active CHAR(1) DEFAULT 'Y'
        CHECK (is_active IN ('Y','N')),
    role      VARCHAR2(50)
);

/* Rooms */

CREATE TABLE Room (
    room_id        NUMBER PRIMARY KEY,
    room_name      VARCHAR2(100) NOT NULL,
    room_type      VARCHAR2(50),
    max_capacity   NUMBER NOT NULL,
    is_adoption_area CHAR(1) DEFAULT 'N'
        CHECK (is_adoption_area IN ('Y','N'))
);

/* Pets */

CREATE TABLE Pet (
    pet_id          NUMBER PRIMARY KEY,
    name            VARCHAR2(100) NOT NULL,
    species         VARCHAR2(50)  NOT NULL,
    breed           VARCHAR2(100),
    age             NUMBER,
    date_of_arrival DATE,
    temperament     VARCHAR2(200),
    special_needs   VARCHAR2(200),
    status          VARCHAR2(30) NOT NULL,
    current_room_id NUMBER,
    CONSTRAINT fk_pet_room
        FOREIGN KEY (current_room_id)
            REFERENCES Room (room_id)
            ON DELETE SET NULL
);

/* Health records */

CREATE TABLE Health_Record (
    record_id     NUMBER PRIMARY KEY,
    pet_id        NUMBER NOT NULL,
    staff_id      NUMBER,
    record_date   DATE NOT NULL,
    record_type   VARCHAR2(50),
    notes         VARCHAR2(500),
    next_due_date DATE,
    status        VARCHAR2(20) DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE','VOID','CORRECTED')),
            CONSTRAINT fk_health_pet
                FOREIGN KEY (pet_id)
                    REFERENCES Pet (pet_id)
                    ON DELETE CASCADE,
            CONSTRAINT fk_health_staff
                FOREIGN KEY (staff_id)
                    REFERENCES Staff (staff_id)
                    ON DELETE SET NULL
);

/* Reservations */

CREATE TABLE Reservation (
    reservation_id   NUMBER PRIMARY KEY,
    member_id        NUMBER NOT NULL,
    room_id          NUMBER NOT NULL,
    reservation_date DATE NOT NULL,
    start_time       TIMESTAMP NOT NULL,
    duration_minutes NUMBER NOT NULL,
    status           VARCHAR2(20) DEFAULT 'BOOKED'
        CHECK (status IN ('BOOKED','IN_PROGRESS','COMPLETED','CANCELLED')),
            check_in_time    TIMESTAMP,
            check_out_time   TIMESTAMP,
            tier_id          NUMBER,
            CONSTRAINT fk_res_member
                FOREIGN KEY (member_id)
                    REFERENCES Member (member_id)
                    ON DELETE CASCADE,
            CONSTRAINT fk_res_room
                FOREIGN KEY (room_id)
                    REFERENCES Room (room_id)
                    ON DELETE CASCADE,
            CONSTRAINT fk_res_tier
                FOREIGN KEY (tier_id)
                    REFERENCES Membership_Tier (tier_id)
                    ON DELETE SET NULL
);

/* Menu items */

CREATE TABLE Menu_Item (
    item_id    NUMBER PRIMARY KEY,
    name       VARCHAR2(100) NOT NULL,
    category   VARCHAR2(50),
    base_price NUMBER(6,2) NOT NULL
        CHECK (base_price >= 0),
            is_available CHAR(1) DEFAULT 'Y'
                CHECK (is_available IN ('Y','N'))
);

/* Customer orders */

CREATE TABLE Customer_Order (
    order_id       NUMBER PRIMARY KEY,
    member_id      NUMBER NOT NULL,
    reservation_id NUMBER,
    order_time     TIMESTAMP DEFAULT SYSTIMESTAMP,
    total_price    NUMBER(8,2) DEFAULT 0
        CHECK (total_price >= 0),
            payment_status VARCHAR2(20) DEFAULT 'UNPAID'
        CHECK (payment_status IN ('UNPAID','PAID')),
            CONSTRAINT fk_order_member
                FOREIGN KEY (member_id)
                    REFERENCES Member (member_id)
                    ON DELETE CASCADE,
            CONSTRAINT fk_order_reservation
                FOREIGN KEY (reservation_id)
                    REFERENCES Reservation (reservation_id)
                    ON DELETE CASCADE
);

/* Order items */

CREATE TABLE Order_Item (
    order_id   NUMBER,
    item_id    NUMBER,
    quantity   NUMBER NOT NULL
        CHECK (quantity > 0),
    unit_price NUMBER(6,2) NOT NULL
        CHECK (unit_price >= 0),
            CONSTRAINT pk_order_item
                PRIMARY KEY (order_id, item_id),
            CONSTRAINT fk_oi_order
                FOREIGN KEY (order_id)
                    REFERENCES Customer_Order (order_id)
                    ON DELETE CASCADE,
            CONSTRAINT fk_oi_item
                FOREIGN KEY (item_id)
                    REFERENCES Menu_Item (item_id)
                    ON DELETE CASCADE
);

/* Events */

CREATE TABLE Event (
    event_id      NUMBER PRIMARY KEY,
    title         VARCHAR2(100) NOT NULL,
    description   VARCHAR2(200),
    room_id       NUMBER NOT NULL,
    event_date    DATE NOT NULL,
    start_time    TIMESTAMP NOT NULL,
    end_time      TIMESTAMP,
    max_attendees NUMBER NOT NULL,
    event_type    VARCHAR2(50),
    staff_id      NUMBER,
    CONSTRAINT fk_event_room
        FOREIGN KEY (room_id)
            REFERENCES Room (room_id)
            ON DELETE CASCADE,
    CONSTRAINT fk_event_staff
        FOREIGN KEY (staff_id)
            REFERENCES Staff (staff_id)
            ON DELETE SET NULL
);

/* Event registrations (bookings) */

CREATE TABLE Event_Registration (
    member_id         NUMBER,
    event_id          NUMBER,
    registration_date DATE NOT NULL,
    attendance_status VARCHAR2(30) DEFAULT 'REGISTERED'
        CHECK (attendance_status IN ('REGISTERED','ATTENDED','NO_SHOW','CANCELLED')),
            payment_status    VARCHAR2(20) DEFAULT 'UNPAID'
        CHECK (payment_status IN ('UNPAID','PAID','REFUNDED')),
            CONSTRAINT pk_event_reg
                PRIMARY KEY (member_id, event_id),
            CONSTRAINT fk_er_member
                FOREIGN KEY (member_id)
                    REFERENCES Member (member_id)
                    ON DELETE CASCADE,
            CONSTRAINT fk_er_event
                FOREIGN KEY (event_id)
                    REFERENCES Event (event_id)
                    ON DELETE CASCADE
);

/* Adoption applications */

CREATE TABLE Adoption_Application (
    application_id NUMBER PRIMARY KEY,
    member_id      NUMBER NOT NULL,
    pet_id         NUMBER NOT NULL,
    submitted_date DATE NOT NULL,
    status         VARCHAR2(20) DEFAULT 'PENDING'
        CHECK (status IN ('PENDING','APPROVED','REJECTED','WITHDRAWN')),
    reviewed_by    NUMBER,
    review_date    DATE,
    notes          VARCHAR2(500),
    CONSTRAINT fk_app_member
        FOREIGN KEY (member_id)
            REFERENCES Member (member_id)
            ON DELETE CASCADE,
    CONSTRAINT fk_app_pet
        FOREIGN KEY (pet_id)
            REFERENCES Pet (pet_id)
            ON DELETE CASCADE,
    CONSTRAINT fk_app_staff
        FOREIGN KEY (reviewed_by)
            REFERENCES Staff (staff_id)
            ON DELETE SET NULL
);

/* Adoptions */

CREATE TABLE Adoption (
    adoption_id       NUMBER PRIMARY KEY,
    application_id    NUMBER NOT NULL,
    pet_id            NUMBER NOT NULL,
    member_id         NUMBER NOT NULL,
    adoption_date     DATE NOT NULL,
    adoption_fee      NUMBER(8,2),
    follow_up_schedule DATE,
    CONSTRAINT fk_adopt_app
        FOREIGN KEY (application_id)
            REFERENCES Adoption_Application (application_id)
            ON DELETE CASCADE,
    CONSTRAINT fk_adopt_pet
        FOREIGN KEY (pet_id)
            REFERENCES Pet (pet_id)
            ON DELETE CASCADE,
    CONSTRAINT fk_adopt_member
        FOREIGN KEY (member_id)
            REFERENCES Member (member_id)
            ON DELETE CASCADE
);
