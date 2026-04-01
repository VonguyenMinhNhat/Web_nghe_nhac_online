#!/usr/bin/env python3
import os

base_path = r"C:\Users\mrnha\Downloads\spring (1)\Web_nghe_nhac_online\src\main\resources\static"

directories = [
    os.path.join(base_path, "pages"),
    os.path.join(base_path, "pages", "admin"),
    os.path.join(base_path, "css"),
    os.path.join(base_path, "js")
]

for directory in directories:
    os.makedirs(directory, exist_ok=True)
    print(f"Created: {directory}")

print("All directories created successfully!")
