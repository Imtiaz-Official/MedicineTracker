import sqlite3

def check_medicine(db_path):
    conn = sqlite3.connect(db_path)
    c = conn.cursor()
    c.execute("SELECT * FROM crawler_medicine LIMIT 10")
    rows = c.fetchall()
    print("Sample rows from crawler_medicine:", rows)
    conn.close()

if __name__ == "__main__":
    check_medicine(r"..\..\..\Downloads\Compressed\bd-medicine-scraper-db\db.sqlite3")
