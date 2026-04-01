# WaveBeat Music Platform

WaveBeat la du an website nghe nhac truc tuyen duoc to chuc lai thanh mot ung dung **Spring Boot + REST API + static frontend**. Ban hien tai khong con la demo don le nua ma da co backend service, API, frontend tich hop va cau truc san sang de phat trien tiep thanh du an lon.

## Tinh nang da hoan thien

- Trang chu nghe nhac responsive.
- Backend REST API cho bai hat, playlist, user, favorite, dashboard.
- Luu tru du lieu bang SQL Server thong qua JDBC + JSON state store.
- Auth demo in-memory: dang ky, dang nhap, dang xuat, quen mat khau, reset mat khau, doi mat khau.
- Cap nhat ho so nguoi dung.
- Frontend goi API that thay vi du lieu hardcode.
- Tim kiem bai hat theo ten, ca si, album, the loai.
- Loc bai hat theo genre va theo cac nhom `all`, `trending`, `favorites`, `playlist`.
- Player co `play`, `pause`, `next`, `prev`, seek, volume, mute.
- Them va bo yeu thich.
- Tao playlist va them bai dang phat vao playlist.
- Lich su nghe nhac, binh luan va danh gia bai hat.
- Admin API cho CRUD song, artist, album, genre va khoa/mo khoa user.
- Dashboard thong ke nhanh va bai hat noi bat.
- Seed data in-memory de test ngay khong can database.
- Error handling co cau truc cho API.
- Test Spring Boot MockMvc va Playwright.

## Kien truc hien tai

### Backend

- `Spring Boot 3.3.x`
- `spring-boot-starter-web`
- `spring-boot-starter-validation`
- Service layer tach rieng de de nang cap thanh database that sau nay

### Frontend

- HTML, CSS, JavaScript thuan
- Static resources duoc serve truc tiep boi Spring Boot
- Giao dien va logic nam trong:
  - [src/main/resources/static/index.html](/C:/Users/mrnha/Downloads/spring%20(1)/Web_nghe_nhac_online/src/main/resources/static/index.html)
  - [src/main/resources/static/styles.css](/C:/Users/mrnha/Downloads/spring%20(1)/Web_nghe_nhac_online/src/main/resources/static/styles.css)
  - [src/main/resources/static/app.js](/C:/Users/mrnha/Downloads/spring%20(1)/Web_nghe_nhac_online/src/main/resources/static/app.js)

### Core source

- [src/main/java/com/wavebeat/music/WaveBeatApplication.java](/C:/Users/mrnha/Downloads/spring%20(1)/Web_nghe_nhac_online/src/main/java/com/wavebeat/music/WaveBeatApplication.java)
- [src/main/java/com/wavebeat/music/service/MusicLibraryService.java](/C:/Users/mrnha/Downloads/spring%20(1)/Web_nghe_nhac_online/src/main/java/com/wavebeat/music/service/MusicLibraryService.java)
- [src/main/java/com/wavebeat/music/controller/MusicApiController.java](/C:/Users/mrnha/Downloads/spring%20(1)/Web_nghe_nhac_online/src/main/java/com/wavebeat/music/controller/MusicApiController.java)
- [src/main/java/com/wavebeat/music/controller/ApiExceptionHandler.java](/C:/Users/mrnha/Downloads/spring%20(1)/Web_nghe_nhac_online/src/main/java/com/wavebeat/music/controller/ApiExceptionHandler.java)

## API chinh

- `GET /api/health`
- `GET /api/dashboard`
- `GET /api/songs?search=&genre=&filter=`
- `GET /api/songs/{songId}`
- `GET /api/genres`
- `GET /api/artists`
- `GET /api/albums`
- `GET /api/users/me`
- `GET /api/favorites`
- `POST /api/favorites/{songId}`
- `DELETE /api/favorites/{songId}`
- `GET /api/playlists`
- `POST /api/playlists`
- `POST /api/playlists/{playlistId}/songs/{songId}`
- `DELETE /api/playlists/{playlistId}/songs/{songId}`

## Bo script test chuc nang

Toi da them bo script test Playwright trong thu muc [tests](/C:/Users/mrnha/Downloads/spring%20(1)/Web_nghe_nhac_online/tests) va cac file cau hinh:

- [package.json](/C:/Users/mrnha/Downloads/spring%20(1)/Web_nghe_nhac_online/package.json)
- [playwright.config.js](/C:/Users/mrnha/Downloads/spring%20(1)/Web_nghe_nhac_online/playwright.config.js)
- [playwright.lambdatest.config.js](/C:/Users/mrnha/Downloads/spring%20(1)/Web_nghe_nhac_online/playwright.lambdatest.config.js)
- [tests/wavebeat-functional.spec.js](/C:/Users/mrnha/Downloads/spring%20(1)/Web_nghe_nhac_online/tests/wavebeat-functional.spec.js)

Trang thai hien tai:

- Da viet du 20 case theo danh sach chuc nang.
- Cac case `F01` den `F20` da duoc map thanh test Playwright hoac API flow thuc te.
- Suite duoc cau hinh chay tuan tu do backend demo hien dang su dung in-memory state.

## Backend test va CI

Toi da bo sung:

- Test API Spring Boot voi MockMvc:
  - [src/test/java/com/wavebeat/music/controller/MusicApiControllerTest.java](/C:/Users/mrnha/Downloads/spring%20(1)/Web_nghe_nhac_online/src/test/java/com/wavebeat/music/controller/MusicApiControllerTest.java)
- GitHub Actions pipeline:
  - [.github/workflows/ci.yml](/C:/Users/mrnha/Downloads/spring%20(1)/Web_nghe_nhac_online/.github/workflows/ci.yml)

Pipeline se:

1. Chay `mvn test`
2. Cai Playwright
3. Khoi dong Spring Boot app
4. Chay `npm run test:e2e`

## Cau truc thu muc

```text
Web_nghe_nhac_online/
|-- pom.xml
|-- src/
|   |-- main/
|   |   |-- java/com/wavebeat/music/
|   |   |   |-- controller/
|   |   |   |-- dto/
|   |   |   |-- model/
|   |   |   `-- service/
|   |   `-- resources/
|   |       |-- application.properties
|   |       `-- static/
|   |-- test/java/com/wavebeat/music/
`-- README.md
```

## Cach chay

Du an da co `pom.xml`, nhung moi truong hien tai cua repository **chua co Maven** va toi khong the tai them tool trong turn nay.

Khi may cua ban co Maven hoac Maven Wrapper, chay:

```bash
mvn spring-boot:run
```

Sau do mo:

- `http://localhost:8080`

## SQL Server va du lieu mau

Du an hien da duoc cau hinh de luu state vao SQL Server.

Tep lien quan:

- [docker-compose.sqlserver.yml](/C:/Users/mrnha/Downloads/spring%20(1)/Web_nghe_nhac_online/docker-compose.sqlserver.yml)
- [schema.sql](/C:/Users/mrnha/Downloads/spring%20(1)/Web_nghe_nhac_online/src/main/resources/schema.sql)
- [create-wavebeat-db.sql](/C:/Users/mrnha/Downloads/spring%20(1)/Web_nghe_nhac_online/sql/create-wavebeat-db.sql)

Cach dung nhanh:

1. Khoi dong SQL Server:
   `docker compose -f docker-compose.sqlserver.yml up -d`
2. Mac dinh app se ket noi vao database `master` tren `localhost:1433`.
3. Khi bang `app_state` chua co du lieu, backend se tu seed du lieu mau vao SQL Server.

Neu muon dung database rieng `WaveBeatDB`, tao DB bang script `sql/create-wavebeat-db.sql` roi chay app voi:

```powershell
$env:DB_URL="jdbc:sqlserver://localhost:1433;databaseName=WaveBeatDB;encrypt=true;trustServerCertificate=true"
$env:DB_USERNAME="sa"
$env:DB_PASSWORD="YourStrong!Passw0rd"
.\tools\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run
```

Neu ban dung IntelliJ IDEA:

1. Import project bang `pom.xml`.
2. Chay class [WaveBeatApplication.java](/C:/Users/mrnha/Downloads/spring%20(1)/Web_nghe_nhac_online/src/main/java/com/wavebeat/music/WaveBeatApplication.java).
3. Truy cap `http://localhost:8080`.

## Hai muoi chuc nang kiem thu chuc nang

20 nhom chuc nang uu tien cho kiem thu van giu nguyen gia tri va nay co the mapping truc tiep vao kien truc moi:

1. Dang ky tai khoan.
2. Dang nhap.
3. Dang xuat.
4. Quen mat khau.
5. Cap nhat ho so ca nhan.
6. Doi mat khau.
7. Xem danh sach bai hat.
8. Tim kiem bai hat.
9. Xem chi tiet bai hat.
10. Phat nhac truc tuyen.
11. Tam dung, tiep tuc, tua bai hat.
12. Chuyen bai truoc va bai sau.
13. Dieu chinh am luong.
14. Them bai hat vao yeu thich.
15. Tao playlist.
16. Them hoac xoa bai hat khoi playlist.
17. Lich su nghe nhac.
18. Binh luan hoac danh gia bai hat.
19. Quan tri them, sua, xoa bai hat.
20. Quan tri the loai, ca si, album va nguoi dung.

## Huong nang cap thanh du an lon

- Thay service in-memory bang `Spring Data JPA` va database PostgreSQL.
- Them `Spring Security` voi JWT, role `USER`, `ADMIN`, `MODERATOR`.
- Tach module `auth`, `catalog`, `playlist`, `analytics`, `admin`.
- Them upload file audio, image va luu tru object storage.
- Them lich su nghe, de xuat bai hat, thong ke thoi gian thuc.
- Them trang admin rieng de CRUD bai hat, album, ca si, the loai.
- Viet unit test, integration test, API test va UI test day du.
- Them Docker, CI/CD va moi truong staging.

## Ghi chu

- Ban hien tai la nen tang backend/frontend hoan chinh o muc demo ky thuat.
- Chua co database, xac thuc that, upload file, phan quyen hoac admin UI day du.
- Audio dang dung URL cong khai de mo phong streaming.

Neu can, buoc tiep theo hop ly nhat la toi se tiep tuc them `Spring Security + JWT + PostgreSQL schema + admin CRUD` de dua repo sang muc ung dung web day du hon.
