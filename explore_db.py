import sqlite3

def explore_db(db_path):
    try:
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()
        
        # List tables
        cursor.execute("SELECT name FROM sqlite_master WHERE type='table';")
        tables = cursor.fetchall()
        print("Tables:", [t[0] for t in tables])
        
        for table in [t[0] for t in tables]:
            print(f"\n--- Columns in {table} ---")
            cursor.execute(f"PRAGMA table_info({table})")
            columns = cursor.fetchall()
            for col in columns:
                print(f"  {col[1]} ({col[2]})")
            
            # Show a sample row if it's a data table
            if 'medicine' in table.lower() or 'brand' in table.lower() or 'generic' in table.lower():
                cursor.execute(f"SELECT * FROM {table} LIMIT 1")
                row = cursor.fetchone()
                if row:
                    print(f"  Sample row: {row}")
        
        conn.close()
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    explore_db(r"..\..\..\Downloads\Compressed\bd-medicine-scraper-db\db.sqlite3")
