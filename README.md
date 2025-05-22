# 🏗️ Termopan Manager — Platformă de gestionare firmă montaj termopane

Aceasta este o aplicație internă de tip SaaS pentru firmele care montează ferestre/termopane. Platforma permite gestionarea angajaților, programărilor, taskurilor zilnice, echipelor și istoricului de lucrări.

---

## 📦 Tech Stack

- 🔙 Backend: **Java 17 + Spring Boot**
- 🔐 Autentificare: **Keycloak** (cu integrare completă)
- 🗄️ Bază de date: **PostgreSQL**
- 🖼️ Salvare media: local în `uploads/images`
- 🧾 Documentație REST: Swagger (opțional)
- 💡 Frontend recomandat: **React.js / React Native**

---

## ⚙️ Funcționalități principale

✅ Gestionare utilizatori pe roluri:
- OWNER, MANAGER, TECHNICIAN, OPERATOR

✅ Firme independente cu angajați proprii:
- fiecare firmă are propriul cont și utilizatori

✅ Taskuri planificate:
- MONTARE, MĂSURARE, LIVRARE etc.
- atribuibile unei echipe sau unui lucrător individual

✅ Concedii și învoiri:
- zile libere sau ore parțiale

✅ Istoric status pentru taskuri:
- cu imagini atașate, în ordine cronologică

✅ Echipe de lucru:
- definire și asociere useri -> echipe

✅ Invitații de înregistrare:
- creare cont doar pe bază de token

---

## Structura de directoare a aplicației

```bash
📦 src
 ┣ 📂model               # Entități JPA (UUID-style, fără relații directe)
 ┣ 📂dto                 # DTO-uri de input/output
 ┣ 📂service             # Logica aplicației
 ┣ 📂repository          # Spring Data interfaces
 ┣ 📂controller          # REST API-uri
 ┣ 📂config              # Keycloak, Swagger, WebConfig
 ┗ 📂uploads/images      # Fișiere salvate (imagini atașate la taskuri)
```

## 🔐 Autentificare (Keycloak)

- `POST /api/v1/auth/register` — înregistrare user
- `POST /api/v1/auth/login` — login user
- `POST /api/v1/auth/login-with-invite` — înregistrare user cu invitație (token)
- `DELETE /api/v1/auth/delete/{keycloakId}` — ștergere cont

---

## 🏢 Companii

- `POST /api/v1/company` — creează firmă nouă (OWNER only)
- `GET /api/v1/company/{id}` — detalii companie
- `GET /api/v1/company/{id}/users` — listare angajați
- `DELETE /api/v1/company/{companyId}/user/{userId}` — elimină utilizator din companie

---

## 👥 Utilizatori

- `POST /api/v1/users` — creează cont nou în firmă
- `POST /api/v1/users/invite` — înregistrare cu invitație
- `GET /api/v1/users/keycloak/{id}` — info user logat
- `DELETE /api/v1/users/keycloak/{id}` — șterge user după Keycloak ID

---

## 📩 Invitații

- `POST /api/v1/invitations` — trimite invitație user (OWNER/MANAGER)
- `GET /api/v1/invitations/company/{companyId}` — listă invitații active
- `DELETE /api/v1/invitations/{invitationId}` — șterge invitație

---

## 🧑‍🤝‍🧑 ECHIPE

- `POST /api/v1/teams` — creează echipă
- `GET /api/v1/teams/company/{companyId}` — echipele din firmă
- `POST /api/v1/teams/{teamId}/add/{userId}` — adaugă user în echipă
- `DELETE /api/v1/teams/{teamId}/remove/{userId}` — elimină user din echipă
- `GET /api/v1/teams/{teamId}/members` — membri echipă

---

## 🔧 Taskuri

- `POST /api/v1/tasks` — creează task pentru echipă sau user individual
- `GET /api/v1/tasks/company/{companyId}` — listă taskuri firmă
- `GET /api/v1/tasks/team/{teamId}` — taskuri echipă
- `GET /api/v1/tasks/user/{userId}` — taskuri personale
- `PUT /api/v1/tasks/{id}/status` — actualizează status
- `DELETE /api/v1/tasks/{id}` — șterge task (doar cel care l-a creat)

---

## 🖼️ Task Updates

- `POST /api/v1/task-updates` — adaugă update (comentariu + poze)
- `GET /api/v1/task-updates/task/{taskId}` — listă updates task

---

## 📦 Comenzi (Customer Orders)

- `POST /api/v1/orders` — creează comandă
- `GET /api/v1/orders/company/{companyId}` — comenzi companie
- `GET /api/v1/orders/{orderId}` — detalii comandă
- `DELETE /api/v1/orders/{orderId}` — ștergere comandă (OWNER/MANAGER)
- `PUT /api/v1/orders/{id}/status` — actualizează status
- `GET /api/v1/orders/filter?companyId=...&date=...&status=...` — filtrare
- `GET /api/v1/orders/company/{id}/export/pdf` — export PDF
- `GET /api/v1/orders/company/{id}/export/excel` — export Excel

---

## 📆 Concedii & Învoiri

- `POST /api/v1/time-off` — trimite cerere
- `GET /api/v1/time-off/user/{userId}` — cererile unui user
- `GET /api/v1/time-off/by-date?date=...` — cereri într-o zi
- `GET /api/v1/time-off/pending/company/{companyId}` — doar pentru OWNER
- `PUT /api/v1/time-off/{requestId}/approve?keycloakId=...` — aprobare cerere
- `DELETE /api/v1/time-off/{requestId}/reject?keycloakId=...` — respingere cerere
- `GET /api/v1/time-off/user/{userId}/export/pdf` — export PDF
- `GET /api/v1/time-off/user/{userId}/export/excel` — export Excel

---

## TODO viitor
-  Dashboard cu statistici lunare
-  PDF/raport lucrări
- Push notifications în React Native

## 🧑‍💻 Cum pornești aplicația

1. Clonează repo-ul:
   ```bash
   git clone https://github.com/tu/termopan-manager.git
   cd termopan-manager
    ```