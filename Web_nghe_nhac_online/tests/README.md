# WaveBeat Test Scripts

Bo script nay duoc viet bang Playwright de phuc vu test chuc nang cho WaveBeat local va co the chay tren LambdaTest.

## So luong chuc nang

- Tong cong: 20 case
- Da duoc map day du: F01 den F20
- Suite chay tuan tu vi backend demo hien dung in-memory state

## Cai dat

```bash
npm install
npx playwright install
```

## Chay local

Dam bao app dang chay tai `http://127.0.0.1:8080`, sau do:

```bash
npm run test:e2e
```

Neu URL khac:

```bash
BASE_URL=http://localhost:8081 npm run test:e2e
```

## Chay tren LambdaTest

Can cac bien moi truong:

```bash
LT_USERNAME=your_username
LT_ACCESS_KEY=your_access_key
BASE_URL=http://your-public-url-or-tunnel-host:8080
```

Sau do:

```bash
npm run test:e2e:lambda
```

Neu test local qua tunnel, can mo LambdaTest Tunnel truoc khi chay script.
