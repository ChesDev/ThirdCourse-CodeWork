-- Первый запрос: все студенты Хогвартса с названиями факультетов
SELECT s.name AS student_name, s.age, f.name AS faculty_name
FROM students s
JOIN faculties f ON s.faculty_id = f.id;

-- Второй запрос: студенты, у которых есть аватарки
SELECT s.name AS student_name, s.age
FROM students s
JOIN avatar a ON s.id = a.student_id;