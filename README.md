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