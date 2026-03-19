import csv
import json
import os

def restore_brands():
    brands = []
    # Read generics to potentially find matches if generic id is missing
    generics_path = 'app/src/main/assets/medicine_generics.json'
    generics_map = {}
    if os.path.exists(generics_path):
        with open(generics_path, 'r', encoding='utf-8') as f:
            gens = json.load(f)
            # Map generic name (lowercase) to its ID
            for g in gens:
                generics_map[g['name'].lower()] = g['id']

    print("Restoring brands from medicine.csv...")
    with open('medicine.csv', mode='r', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        for row in reader:
            try:
                gen_name = row['generic'].strip()
                # Try to find generic ID from the map
                gen_id = generics_map.get(gen_name.lower())
                
                brand = {
                    "id": int(row['brand id']),
                    "name": row['brand name'],
                    "dosageForm": row['dosage form'],
                    "generic": gen_name,
                    "strength": row['strength'],
                    "manufacturer": row['manufacturer'],
                    "genericId": gen_id
                }
                brands.append(brand)
            except (KeyError, ValueError):
                continue
    
    assets_dir = 'app/src/main/assets'
    with open(os.path.join(assets_dir, 'medicine_brands.json'), 'w', encoding='utf-8') as f:
        json.dump(brands, f, ensure_ascii=False, indent=2)
    
    print(f"Successfully restored {len(brands)} brands.")

if __name__ == "__main__":
    restore_brands()
