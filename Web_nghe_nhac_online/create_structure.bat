@echo off
REM Create folder structure for WaveBeat Frontend Restructuring
cd /d "C:\Users\mrnha\Downloads\spring (1)\Web_nghe_nhac_online\src\main\resources\static"

REM Create pages directory
if not exist "pages" mkdir pages
if not exist "pages\admin" mkdir pages\admin

REM Create CSS directory
if not exist "css" mkdir css

REM Create JS directory
if not exist "js" mkdir js

echo Directories created successfully!
pause
