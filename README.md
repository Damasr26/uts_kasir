# JOSJISMART — Diamond Register POS

**Aplikasi kasir desktop Java Swing bergaya skeuomorphism (permukaan timbul, bevel, kilau kaca, gradasi biru-logam)** untuk **Apache NetBeans 31 + JDK 21 + Maven + MySQL dari XAMPP**.

## FITUR

- **Tanpa login:** jalankan aplikasi dan langsung masuk dashboard; tidak ada username atau password aplikasi.
- Dashboard: omzet harian, banyak transaksi, total unit, stok menipis, tren omzet 7 hari, produk terlaris 30 hari.
- Produk: tambah, lihat, ubah, **hapus permanen**, stok, SKU unik, kategori, satuan, harga, pencarian langsung, pengurutan tabel.
- POS: cari produk, tambahkan ke keranjang, tambah/ubah jumlah, hapus item, kosongkan keranjang.
- Form **nama pembeli**, uang yang dibayarkan, total otomatis, perhitungan kembalian/kekurangan.
- Checkout menggunakan **MySQL transaction**: `SELECT ... FOR UPDATE`, validasi data dan stok, simpan nota + snapshot item, kurangi stok, commit atau rollback seluruhnya.
- Struk: pratinjau dan cetak melalui dialog printer Java; struk transaksi lama dapat dicetak ulang dari laporan.
- Laporan: filter tanggal inklusif, cari invoice/pembeli/kasir, daftar historis, detail item, ekspor CSV terperinci yang dapat dibuka Excel.
- Analisis: omzet periode, jumlah transaksi, rata-rata nilai nota, total unit, grafik omzet 14 hari, barang terlaris.
- Snapshot historis: **hapus atau edit data master produk tidak mengubah informasi barang di transaksi sebelumnya**.
- Demo 8 produk dimasukkan **sekali** pada pemasangan awal.

## SYARAT

- Windows 10/11, Java **JDK 21** (bukan hanya JRE).
- **NetBeans 31** dan dukungan Maven yang disertakan.
- **XAMPP** dengan MySQL/MariaDB (port umumnya `3306`).
- Internet pada build Maven **pertama** untuk mengunduh MariaDB Connector/J (kompatibel dengan MySQL di XAMPP) serta plugin Maven. Tidak perlu menambahkan JDBC JAR manual. Driver MariaDB Connector/J dipilih karena XAMPP umumnya memakai MariaDB, dan driver ini juga mendukung server MySQL.

## CARA MENJALANKAN DI NETBEANS 31

1. Ekstrak ZIP. Jangan langsung membuka ZIP dari NetBeans.
2. Buka **XAMPP Control Panel**, jalankan **MySQL** (Apache tidak perlu untuk aplikasi desktop).
3. Buka `config.properties` di folder root proyek (sefolder dengan `pom.xml`), sesuaikan parameter:

   ```properties
   db.host=127.0.0.1
   db.port=3306
   db.name=josjismart_db
   db.user=root
   db.password=
   ```

   Konfigurasi tersebut cocok bila MySQL XAMPP menggunakan `root` tanpa password. Jika password root sudah diatur, isi `db.password`. Jangan memasukkan karakter spasi di belakang nilai.

4. Buka **NetBeans 31** → **File > Open Project** → pilih folder **JOSJISMART** yang memiliki `pom.xml` → **Open Project**.
5. Pastikan **Project Properties > Libraries > Java Platform** menggunakan **JDK 21**.
6. Klik kanan proyek → **Clean and Build**. Tunggu Maven mengunduh dependensi pada build pertama.
7. Klik kanan proyek → **Run**, atau jalankan `id.josjismart.App` sebagai main class.
8. Jika NetBeans bertanya **Select Main Class**, pilih `id.josjismart.App`.
9. Database `josjismart_db`, tabel, dan demo produk akan dibuat **otomatis** saat pertama aplikasi dibuka. Tidak wajib import SQL.
10. **Dashboard JOSJISMART langsung terbuka tanpa login.**
11. Jika ingin mengganti nama kasir pada struk, buka **Pengaturan > Nama Kasir** lalu klik Simpan.

> Catatan path: aplikasi membaca `config.properties` dari *working directory* Java. NetBeans biasanya menggunakan folder proyek. Bila berbeda, buka Project Properties > Run > Working Directory dan arahkan ke folder proyek. Bila menjalankan JAR dari command prompt, jalankan perintah **dari folder yang berisi `config.properties`**.

## JALANKAN DARI TERMINAL / CMD

Buka terminal di folder proyek (folder yang berisi `pom.xml` dan `config.properties`).

```bat
mvn clean package
java -jar target/josjismart-pos-1.0.0.jar
```

Atau:

```bat
mvn exec:java
```

Di Windows, Anda juga bisa klik dua kali `run-windows.bat` setelah proses build. Jika JAR belum ada, skrip tersebut mencoba membangunnya menggunakan Maven yang tersedia di PATH.

Untuk mengetahui versi Java:

```bat
java -version
mvn -version
```

## OPSI IMPOR DATABASE MANUAL

Jika akun MySQL tidak memiliki izin `CREATE DATABASE`, admin MySQL perlu membuat database `josjismart_db` lebih dahulu atau memberi izin yang sesuai. SQL referensi ada di `database/schema.sql` dan boleh diimpor lewat **phpMyAdmin > SQL** atau **Import**. Tetap jalankan aplikasi untuk pengisian produk demo. Tidak diperlukan akun pengguna aplikasi.

Import SQL tidak akan menghapus tabel atau data yang sudah ada. Gunakan hanya untuk database baru atau sesuai prosedur migrasi data Anda.

## DESAIN DATABASE: RIWAYAT TIDAK BERPENGARUH OLEH PERUBAHAN PRODUK

**Struktur:**

```text
products  (data master yang dapat diubah / dihapus)
  id | sku | name | category | unit | price | stock

sales     (nota permanen)
  id | invoice_no | customer_name | cashier_name_snapshot
     | total | paid | change_amount | created_at

sale_items  (snapshot isi nota saat pembelian)
  id | sale_id (FK -> sales.id)
     | sku_snapshot | name_snapshot | category_snapshot
     | unit_snapshot | qty | price_snapshot | line_total
```

`sale_items` **tidak** mempunyai relasi ke `products.id`. Laporan dan cetak ulang membaca data **dari `sale_items`**, bukan dari `products`. Misalnya barang dibeli bernama **Air Mineral Rp4.000** lalu nama dan harga produk diubah menjadi **Air Mineral Premium Rp5.000**, maka riwayat lama akan tetap menunjukkan **Air Mineral Rp4.000**. Bahkan jika produk master dihapus, struk transaksi lama tetap dapat dibuka.

Setiap pembayaran dilakukan sebagai 1 transaksi SQL. Kalau stok tidak cukup, produk sudah dihapus, atau harga berubah saat item masih di keranjang, transaksi ditolak sebelum mengurangi stok; pengguna perlu memperbarui keranjang.

## PETUNJUK PEMAKAIAN

1. Tidak perlu login. Aplikasi langsung membuka dashboard. Buka **Data Produk** untuk menambah atau mengubah stok/harga.
2. Buka **Point of Sale**. Cari barang, pilih baris, lalu klik **Masukkan ke Keranjang** (atau klik dua kali).
3. Atur Qty di keranjang, isi **Nama Pembeli** (boleh gunakan `Pelanggan Umum`) dan masukkan angka uang bayar tanpa `Rp`, contoh: `50000`.
4. Klik **Proses Pembayaran** dan konfirmasi. Stok berkurang, nota tercatat, dan struk ditampilkan.
5. Buka **Laporan & Analisis** untuk mencari transaksi sebelumnya, cetak ulang struk, ekspor CSV, lihat grafik/produk laris.
6. Di menu **Pengaturan**, nama kasir bisa diubah tanpa password. Nama kasir yang tersimpan dalam transaksi lama tidak ikut berubah.
7. Ekspor CSV: klik **Ekspor CSV Rincian** pada tab Riwayat. File mengekspor data transaksi dalam **periode dan hasil pencarian yang sedang aktif** (maksimal 2.000 transaksi per tampilan), termasuk snapshot detail item.

## PEMECAHAN MASALAH

| Masalah | Penyelesaian |
| --- | --- |
| `Communications link failure` | Start MySQL di XAMPP; cek port `3306` / host di `config.properties`. |
| `Access denied for user 'root'` | Periksa `db.user` dan `db.password` sesuai akses MySQL XAMPP Anda. |
| `Unknown database` atau tidak bisa create | Impor `database/schema.sql` atau beri hak untuk membuat database. |
| `No suitable driver` / dependensi tidak terunduh | Di NetBeans klik **Reload POM**, lalu **Clean and Build** dengan internet aktif. |
| Output muncul program lain / Hello World | Klik kanan proyek JOSJISMART (bukan proyek lain) → **Run**. Main class harus `id.josjismart.App`. |
| `invalid target release: 21` | NetBeans/Maven sedang memakai JDK lama; atur Java Platform dan `JAVA_HOME` ke JDK 21. |
| `Address already in use` MySQL XAMPP | Periksa port MySQL yang aktif dan sesuaikan `db.port`. |
| Invoice tidak muncul hari ini | Lihat rentang tanggal di laporan, zona waktu Windows, dan tombol **Tampilkan Laporan**. |
| Uang bayar ditolak | Isi hanya angka rupiah, misalnya `100000` atau `100.000` (tanpa `Rp`). |

## CATATAN KEAMANAN DAN BATASAN

- **Tidak ada login atau batasan peran:** siapa pun yang dapat membuka aplikasi/komputer dan mengakses MySQL dapat menggunakan fitur CRUD, kasir, dan laporan. Batasi akses komputer dan database bila dipakai dalam operasional.
- Jangan unggah file `config.properties` berisi password ke repositori publik. Untuk operasional, disarankan pengguna database khusus dengan hak terbatas.
- Program contoh ini merupakan POS lokal untuk pembelajaran/pengembangan; belum mendukung retur, multi-gudang, pajak, diskon, audit trail pengubahan stok, printer thermal ESC/POS otomatis, atau sinkronisasi cloud.
- Harga disimpan sebagai `DECIMAL`/`BigDecimal`, bukan floating point.
- Struk cetak menggunakan sistem dialog cetak Java standar; ukuran printer dapat disesuaikan pada pengaturan printer.
- SQLite, akses internet, server web, atau Apache XAMPP **tidak digunakan**.

**Kompatibilitas data:** pada database yang pernah digunakan versi login, tabel `users` lama mungkin tetap ada tetapi tidak dipakai. Proyek ini tidak menghapus tabel lama untuk mencegah kehilangan data.

**Lisensi:** Contoh proyek pembelajaran bebas dimodifikasi untuk kebutuhan sendiri. Dependensi JDBC dari MySQL memiliki lisensi tersendiri.

## HASIL VERIFIKASI

Lihat `docs/VERIFIKASI.txt` untuk ringkasan pengujian yang sudah dilakukan dan pengujian XAMPP yang perlu dijalankan di komputer Anda. Aplikasi membuka dashboard langsung tanpa layar login.
