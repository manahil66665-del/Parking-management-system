INSERT OR IGNORE INTO roles(name, description) VALUES
('ADMIN', 'Full system access'),
('STAFF', 'Daily parking operations');

-- The Java initializer creates the default admin with PBKDF2:
-- username: admin
-- password: admin123

INSERT OR IGNORE INTO cars(registration_no, owner_name, model, car_type, color, status) VALUES
('ABC-123', 'Ali Khan', 'Toyota Corolla', 'SEDAN', 'White', 'PARKED'),
('LHR-7788', 'Sara Ahmed', 'Honda Civic', 'SEDAN', 'Black', 'ACTIVE'),
('ICT-550', 'Usman Raza', 'Suzuki Alto', 'OTHER', 'Silver', 'ACTIVE');

INSERT OR IGNORE INTO slots(slot_code, floor, type, status) VALUES
('A-01', 'Ground', 'ACCESSIBLE', 'AVAILABLE'),
('A-02', 'Ground', 'ACCESSIBLE', 'AVAILABLE'),
('A-03', 'Ground', 'STANDARD', 'AVAILABLE'),
('A-04', 'Ground', 'STANDARD', 'AVAILABLE'),
('A-05', 'Ground', 'STANDARD', 'AVAILABLE'),
('A-06', 'Ground', 'STANDARD', 'AVAILABLE'),
('A-07', 'Ground', 'STANDARD', 'AVAILABLE'),
('A-08', 'Ground', 'STANDARD', 'AVAILABLE'),
('A-09', 'Ground', 'STANDARD', 'AVAILABLE'),
('A-10', 'Ground', 'STANDARD', 'AVAILABLE'),
('A-11', 'Ground', 'STANDARD', 'AVAILABLE'),
('A-12', 'Ground', 'STANDARD', 'AVAILABLE');
