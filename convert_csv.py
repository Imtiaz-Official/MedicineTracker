import csv
import json

def convert_medicine_csv():
    brands = []
    with open('medicine.csv', mode='r', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        for row in reader:
            # Match the fields in medicine_brands.json:
            # id, name, dosageForm, generic, strength, manufacturer
            try:
                brand = {
                    "id": int(row['brand id']),
                    "name": row['brand name'],
                    "dosageForm": row['dosage form'],
                    "generic": row['generic'],
                    "strength": row['strength'],
                    "manufacturer": row['manufacturer']
                }
                brands.append(brand)
            except (KeyError, ValueError):
                continue
    
    with open('app/src/main/assets/medicine_brands.json', 'w', encoding='utf-8') as f:
        json.dump(brands, f, ensure_ascii=False, indent=2)
    
    print(f"Successfully converted {len(brands)} medicines to JSON.")

if __name__ == "__main__":
    convert_medicine_csv()
