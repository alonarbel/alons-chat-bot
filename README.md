# Alon's Chat Bot

ניסוי הכולל בקאנד Spring Boot ופרונט React לבוט צ'אט יומיומי.

## מבנה הפרויקט

- `backend/` – Spring Boot 3.5 עם REST API בנתיב `/api/messages`.
- `frontend/` – Vite + React עם ממשק צ'אט חד עמודי.

## הרצה מקומית

### דרישות
- Java 21
- Node.js 18+

### Backend
```bash
cd backend
./mvnw spring-boot:run
```
השרת יעלה על `http://localhost:8080`.

### Frontend
```bash
cd frontend
cp .env.example .env   # התאמת כתובת backend אם צריך
npm install            # פעם ראשונה בלבד
npm run dev            # יעלה בברירת מחדל ל- http://localhost:5173
```

במצב פרודקשן אפשר להריץ `npm run build` ולפרוס את תקיית `dist` מאחורי CDN פשוט.

## API קצר
- `GET /api/messages` – מחזיר היסטוריית הודעות אחרונה.
- `POST /api/messages` עם `{ "message": "..." }` – שומר הודעת משתמש, מחזיר תשובת בוט והצעות המשך.

הבקאנד מחזיק היסטוריה בזיכרון (עד 50 הודעות). זה מספיק להדגמה ואפשר בעתיד להחליף למסד נתונים.

## המשך עבודה
- חיבור למנוע AI אמיתי במקום תשובות כללים.
- התממשקות לאוטנטיקציה של גיטהאב לצורך פרסונליזציה.
- הוספת בדיקות יחידה גם לפרונט וגם לבקאנד.
