#!/usr/bin/env python3
from PIL import Image, ImageDraw, ImageFont
import os

def create_icon(size, path):
    # Cria uma imagem com cor de fundo azul da Microsoft
    img = Image.new('RGB', (size, size), color='#0078D4')
    draw = ImageDraw.Draw(img)
    
    # Adiciona um texto simples "MR" para Microsoft Rewards
    try:
        font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVu-Sans-Bold.ttf", size//3)
    except:
        font = ImageFont.load_default()
    
    text = "MR"
    bbox = draw.textbbox((0, 0), text, font=font)
    text_width = bbox[2] - bbox[0]
    text_height = bbox[3] - bbox[1]
    
    x = (size - text_width) // 2
    y = (size - text_height) // 2
    
    draw.text((x, y), text, fill='white', font=font)
    
    # Salva a imagem
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path, 'PNG')
    print(f"Created icon: {path}")

# Cria ícones para diferentes densidades
base_path = "/project/workspace/deivid22srk/MicrosoftRewards-Bot/app/src/main/res"

icons = [
    (36, f"{base_path}/mipmap-ldpi/ic_launcher.png"),
    (48, f"{base_path}/mipmap-mdpi/ic_launcher.png"),
    (72, f"{base_path}/mipmap-hdpi/ic_launcher.png"),
    (96, f"{base_path}/mipmap-xhdpi/ic_launcher.png"),
    (144, f"{base_path}/mipmap-xxhdpi/ic_launcher.png"),
    (192, f"{base_path}/mipmap-xxxhdpi/ic_launcher.png"),
]

for size, path in icons:
    create_icon(size, path)
    # Cria também a versão round
    round_path = path.replace("ic_launcher.png", "ic_launcher_round.png")
    create_icon(size, round_path)

print("All icons created successfully!")