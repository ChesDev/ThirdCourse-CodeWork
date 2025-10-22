-- 1. Возраст студента не может быть меньше 16 лет
ALTER TABLE students
ADD CONSTRAINT age_constraint CHECK (age >= 16);

-- 2. Имена студентов должны быть уникальными и не равны нулю
ALTER TABLE students
ALTER COLUMN name SET NOT NULL;

ALTER TABLE students
ADD CONSTRAINT uniq_name UNIQUE (name);

-- 3. Пара "значение названия" - "цвет факультета" должна быть уникальной
ALTER TABLE faculties
ADD CONSTRAINT uniq_name_color UNIQUE (name, color);

-- 4. При создании студента без возраста ему автоматически должно присваиваться 20 лет
ALTER TABLE students
ALTER COLUMN age SET DEFAULT 20;