import sqlite3
import json
import os

def extract_generics(db_path):
    try:
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()
        
        print("Extracting generics...")
        cursor.execute("""
            SELECT 
                generic_id, generic_name, indication_description, therapeutic_class_description,
                pharmacology_description, dosage_description, administration_description,
                interaction_description, contraindications_description, side_effects_description,
                pregnancy_and_lactation_description, precautions_description, storage_conditions_description
            FROM crawler_generic
        """)
        generics = []
        for row in cursor.fetchall():
            generics.append({
                "id": row[0],
                "name": row[1],
                "indication": row[2],
                "therapeuticClass": row[3],
                "pharmacology": row[4],
                "dosage": row[5],
                "administration": row[6],
                "interaction": row[7],
                "contraindications": row[8],
                "sideEffects": row[9],
                "pregnancyLactation": row[10],
                "precautions": row[11],
                "storage": row[12]
            })
            
        assets_dir = 'app/src/main/assets'
        if not os.path.exists(assets_dir):
            os.makedirs(assets_dir)
            
        with open(os.path.join(assets_dir, 'medicine_generics.json'), 'w', encoding='utf-8') as f:
            json.dump(generics, f, ensure_ascii=False, indent=2)
            
        print(f"Extracted {len(generics)} generics.")
        conn.close()
        
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    db_path = r"..\..\..\Downloads\Compressed\bd-medicine-scraper-db\db.sqlite3"
    extract_generics(db_path)
