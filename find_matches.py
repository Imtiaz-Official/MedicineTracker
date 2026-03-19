import json
import difflib

with open('app/src/main/assets/medicine_brands.json', 'r', encoding='utf-8') as f:
    brands = json.load(f)

with open('app/src/main/assets/medicine_generics.json', 'r', encoding='utf-8') as f:
    generics = json.load(f)

generic_names = [g['name'] for g in generics]
unlinked_names = list(set([b['generic'] for b in brands if b.get('genericId') is None and b.get('generic')]))

print(f'Total unlinked generic names: {len(unlinked_names)}')
print('Potential matches for unlinked generics (cutoff 0.8):')
for name in unlinked_names:
    matches = difflib.get_close_matches(name, generic_names, n=1, cutoff=0.8)
    if matches:
        print(f'"{name}" -> might be -> "{matches[0]}"')
