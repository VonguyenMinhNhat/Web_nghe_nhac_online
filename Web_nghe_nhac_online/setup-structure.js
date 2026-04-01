const fs = require('fs');
const path = require('path');

const basePath = path.join(__dirname, 'src', 'main', 'resources', 'static');

const directories = [
    path.join(basePath, 'pages'),
    path.join(basePath, 'pages', 'admin'),
    path.join(basePath, 'css'),
    path.join(basePath, 'js')
];

directories.forEach(dir => {
    if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true });
        console.log(`✓ Created: ${dir}`);
    } else {
        console.log(`✓ Exists: ${dir}`);
    }
});

console.log('\n✓ Directory structure created successfully!');
