from pathlib import Path
from PIL import Image

root = Path('/home/ubuntu/sih-2026-compliance-portal/client/public')
assets = [
    ('sih-ledger-hero.jpg', 'RGB', 82),
    ('sih-evidence-texture.jpg', 'RGB', 82),
    ('sih-field-ledger-mark.png', 'RGBA', None),
    ('sih-paper-grain.jpg', 'RGB', 82),
]
for name, mode, quality in assets:
    source = root / name
    image = Image.open(source).convert(mode)
    if name.endswith('.png') and max(image.size) > 1024:
        image.thumbnail((1024, 1024), Image.Resampling.LANCZOS)
    if name.endswith('.png'):
        image.save(source, format='PNG', optimize=True, compress_level=9)
    else:
        image.save(source, format='JPEG', quality=quality, optimize=True, progressive=True)
    print(name, image.size, source.stat().st_size)
