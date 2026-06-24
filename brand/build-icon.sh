#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
S=1024
# periwinkle radial-gradient background (full square; iOS masks corners itself)
magick -size ${S}x${S} radial-gradient:'#8B7BF0'-'#5A4FD0' bg.png
# white mark on transparent: full ring + teardrop (path scaled 120->1024, factor 8.5333)
magick -size ${S}x${S} xc:none -fill none -stroke white -strokewidth 64 \
  -draw "circle 512,512 512,77" \
  -fill white -stroke none \
  -draw "path 'M512 222 C512 222 717 461 717 632 C717 745 631 836 512 836 C393 836 307 745 307 632 C307 461 512 222 512 222 Z'" \
  mark.png
magick bg.png mark.png -compose over -composite icon-1024.png
rm -f bg.png mark.png
echo "wrote icon-1024.png"
