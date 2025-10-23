-- Создание таблицы для машин
CREATE TABLE cars (
    id SERIAL PRIMARY KEY,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    price DECIMAL(12,2) NOT NULL
);

-- Создание таблицы для людей
CREATE TABLE people (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INTEGER NOT NULL,
    has_license BOOLEAN NOT NULL,
    car_id INTEGER,
    FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE SET NULL
);