@echo off
setlocal
cd /d "%~dp0"
set "APP=target\josjismart-pos-1.0.0.jar"
echo.
echo  =====================================================
echo             JOSJISMART - DIAMOND REGISTER
 echo  =====================================================
if not exist "%APP%" (
  echo.
  echo  JAR belum ditemukan. Membangun proyek dengan Maven...
  where mvn >nul 2>&1
  if errorlevel 1 (
    echo  Perintah mvn tidak ada di PATH.
    echo  Buka NetBeans 31 ^> Open Project ^> Clean and Build.
    echo  Kemudian jalankan file ini kembali.
    pause
    exit /b 1
  )
  call mvn clean package
  if errorlevel 1 (
    echo  BUILD GAGAL. Lihat pesan error Maven di atas.
    pause
    exit /b 1
  )
)
echo  Menjalankan JOSJISMART...
java -jar "%APP%"
if errorlevel 1 (
 echo  Aplikasi berhenti karena kesalahan. Periksa Java 21 dan MySQL XAMPP.
 pause
)
endlocal
