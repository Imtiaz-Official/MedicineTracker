import sqlite3

def check_counts(db_path):
    try:
        conn = sqlite3.connect(db_path)
        c = conn.cursor()
        c.execute("SELECT name FROM sqlite_master WHERE type='table'")
        tables = [t[0] for t in c.fetchall()]
        for t in tables:
            c.execute(f"SELECT COUNT(*) FROM {t}")
            count = c.fetchone()[0]
            print(f"{t}: {count}")
        conn.close()
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    check_counts(r"..\..\..\Downloads\Compressed\bd-medicine-scraper-db\db.sqlite3")
