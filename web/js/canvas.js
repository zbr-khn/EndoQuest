// EndoQuest Canvas Graphics Engine
// High-DPI 3D Road Perspective, Characters, Obstacles, Coins, and Clinical Simulations

class CanvasRenderer {
  constructor() {
    this.coinAngle = 0;
  }

  // Draw 3D Highway Perspective
  drawRoadPerspective(ctx, width, height, roadOffset) {
    const horizonY = height * 0.38;
    const horizonX = width * 0.5;
    const roadWidthTop = width * 0.08;
    const roadWidthBottom = width * 0.94;
    const bottomY = height * 0.98;

    // 1. Sky Gradient
    const skyGrad = ctx.createLinearGradient(0, 0, 0, horizonY);
    skyGrad.addColorStop(0, '#29B6F6');
    skyGrad.addColorStop(0.5, '#4FC3F7');
    skyGrad.addColorStop(1, '#B3E5FC');
    ctx.fillStyle = skyGrad;
    ctx.fillRect(0, 0, width, horizonY);

    // 2. Sun with Corona
    const sunGrad = ctx.createRadialGradient(width * 0.78, horizonY * 0.38, 5, width * 0.78, horizonY * 0.38, 48);
    sunGrad.addColorStop(0, '#FFF9C4');
    sunGrad.addColorStop(0.35, '#FFEE58');
    sunGrad.addColorStop(0.7, 'rgba(255, 214, 0, 0.4)');
    sunGrad.addColorStop(1, 'rgba(255, 214, 0, 0)');
    ctx.fillStyle = sunGrad;
    ctx.beginPath();
    ctx.arc(width * 0.78, horizonY * 0.38, 48, 0, Math.PI * 2);
    ctx.fill();

    // 3. Distant Rolling Hills
    ctx.fillStyle = '#43A047';
    ctx.beginPath();
    ctx.moveTo(0, horizonY);
    ctx.quadraticCurveTo(width * 0.25, horizonY - 22, width * 0.5, horizonY);
    ctx.quadraticCurveTo(width * 0.75, horizonY - 26, width, horizonY);
    ctx.lineTo(width, height);
    ctx.lineTo(0, height);
    ctx.closePath();
    ctx.fill();

    // 4. Foreground Green Lawn Grass
    const grassGrad = ctx.createLinearGradient(0, horizonY, 0, height);
    grassGrad.addColorStop(0, '#2E7D32');
    grassGrad.addColorStop(1, '#1B5E20');
    ctx.fillStyle = grassGrad;
    ctx.fillRect(0, horizonY, width, height - horizonY);

    // 5. Road Surface (Trapezoid)
    ctx.fillStyle = '#263238';
    ctx.beginPath();
    ctx.moveTo(horizonX - roadWidthTop / 2, horizonY);
    ctx.lineTo(horizonX + roadWidthTop / 2, horizonY);
    ctx.lineTo(horizonX + roadWidthBottom / 2, bottomY);
    ctx.lineTo(horizonX - roadWidthBottom / 2, bottomY);
    ctx.closePath();
    ctx.fill();

    // 6. Red Border Highway Curbs
    const curbW = 10;
    ctx.fillStyle = '#D32F2F';
    // Left Curb
    ctx.beginPath();
    ctx.moveTo(horizonX - roadWidthTop / 2, horizonY);
    ctx.lineTo(horizonX - roadWidthTop / 2 - 2, horizonY);
    ctx.lineTo(horizonX - roadWidthBottom / 2 - curbW, bottomY);
    ctx.lineTo(horizonX - roadWidthBottom / 2, bottomY);
    ctx.closePath();
    ctx.fill();
    // Right Curb
    ctx.beginPath();
    ctx.moveTo(horizonX + roadWidthTop / 2, horizonY);
    ctx.lineTo(horizonX + roadWidthTop / 2 + 2, horizonY);
    ctx.lineTo(horizonX + roadWidthBottom / 2 + curbW, bottomY);
    ctx.lineTo(horizonX + roadWidthBottom / 2, bottomY);
    ctx.closePath();
    ctx.fill();

    // 7. Animated Lane Dash Stripes (3 Lanes -> 2 Dividers)
    const numDashes = 14;
    for (let i = 0; i < numDashes; i++) {
      const p = (i / numDashes + roadOffset) % 1.0;
      const depth = Math.pow(p, 1.8);
      const y1 = horizonY + (bottomY - horizonY) * depth;
      const stripeLen = 10 + depth * 32;
      const y2 = Math.min(y1 + stripeLen, bottomY);
      const roadW = roadWidthTop + (roadWidthBottom - roadWidthTop) * depth;
      const laneW = roadW / 3;
      const stripeW = 1.5 + depth * 5;

      ctx.strokeStyle = '#FFFFFF';
      ctx.lineWidth = stripeW;
      ctx.lineCap = 'round';

      // Left lane divider
      ctx.beginPath();
      ctx.moveTo(horizonX - laneW * 0.5, y1);
      ctx.lineTo(horizonX - laneW * 0.5, y2);
      ctx.stroke();

      // Right lane divider
      ctx.beginPath();
      ctx.moveTo(horizonX + laneW * 0.5, y1);
      ctx.lineTo(horizonX + laneW * 0.5, y2);
      ctx.stroke();
    }
  }

  // Draw Protagonist Alex Character
  drawAlex(ctx, cx, cy, frame, isJumping, isSliding, scale = 1) {
    const s = scale;
    const skinColor = '#FFCC80';
    const hairColor = '#3E2723';
    const scrubTeal = '#00838F';
    const scrubPant = '#00695C';
    const shoeWhite = '#FAFAFA';
    const shoeSole = '#0288D1';

    const ySlideOffset = isSliding ? 24 * s : 0;
    const headCenter = { x: cx, y: cy - 76 * s + ySlideOffset };
    const torsoTop = cy - 46 * s + ySlideOffset;

    // 1. Head & Hair
    ctx.fillStyle = hairColor;
    this.roundRect(ctx, headCenter.x - 22 * s, headCenter.y - 28 * s, 44 * s, 32 * s, 14 * s);
    ctx.fill();

    ctx.fillStyle = skinColor;
    ctx.beginPath();
    ctx.arc(headCenter.x, headCenter.y, 20 * s, 0, Math.PI * 2);
    ctx.fill();

    // Eyes
    ctx.fillStyle = '#000000';
    ctx.beginPath();
    ctx.arc(headCenter.x - 6 * s, headCenter.y - 2 * s, 2.8 * s, 0, Math.PI * 2);
    ctx.arc(headCenter.x + 6 * s, headCenter.y - 2 * s, 2.8 * s, 0, Math.PI * 2);
    ctx.fill();

    // Eyebrows & Mouth
    ctx.strokeStyle = '#3E2723';
    ctx.lineWidth = 2 * s;
    ctx.beginPath();
    ctx.moveTo(headCenter.x - 9 * s, headCenter.y - 8 * s);
    ctx.lineTo(headCenter.x - 3 * s, headCenter.y - 6 * s);
    ctx.moveTo(headCenter.x + 3 * s, headCenter.y - 6 * s);
    ctx.lineTo(headCenter.x + 9 * s, headCenter.y - 8 * s);
    ctx.stroke();

    ctx.strokeStyle = '#B71C1C';
    ctx.lineWidth = 2.5 * s;
    ctx.beginPath();
    ctx.moveTo(headCenter.x - 4 * s, headCenter.y + 8 * s);
    ctx.lineTo(headCenter.x + 4 * s, headCenter.y + 8 * s);
    ctx.stroke();

    // 2. Torso (Teal scrubs)
    const torsoHeight = isSliding ? 32 * s : 62 * s;
    ctx.fillStyle = scrubTeal;
    this.roundRect(ctx, cx - 20 * s, torsoTop, 40 * s, torsoHeight, 8 * s);
    ctx.fill();

    // V-neck scrub opening
    ctx.fillStyle = skinColor;
    ctx.beginPath();
    ctx.moveTo(cx - 8 * s, torsoTop);
    ctx.lineTo(cx, torsoTop + 14 * s);
    ctx.lineTo(cx + 8 * s, torsoTop);
    ctx.closePath();
    ctx.fill();

    // Stethoscope tubing
    ctx.strokeStyle = '#B0BEC5';
    ctx.lineWidth = 3.5 * s;
    ctx.lineCap = 'round';
    ctx.beginPath();
    ctx.moveTo(headCenter.x - 10 * s, headCenter.y + 16 * s);
    ctx.quadraticCurveTo(cx, torsoTop + 10 * s, cx + 10 * s, headCenter.y + 16 * s);
    ctx.stroke();

    // Hospital ID Badge
    ctx.fillStyle = '#FFFFFF';
    ctx.fillRect(cx - 14 * s, torsoTop + 16 * s, 9 * s, 12 * s);
    ctx.fillStyle = '#0288D1';
    ctx.beginPath();
    ctx.arc(cx - 9.5 * s, torsoTop + 20 * s, 2 * s, 0, Math.PI * 2);
    ctx.fill();

    // 3. Arms & Hands
    let armSwing = 0;
    if (isJumping) armSwing = -24 * s;
    else if (isSliding) armSwing = 28 * s;
    else {
      const swings = [18, 6, -18, -6];
      armSwing = (swings[frame % 4] || 0) * s;
    }

    ctx.strokeStyle = scrubTeal;
    ctx.lineWidth = 8 * s;
    ctx.lineCap = 'round';

    // Left Arm
    ctx.beginPath();
    ctx.moveTo(cx - 20 * s, torsoTop + 6 * s);
    ctx.lineTo(cx - 32 * s + armSwing, torsoTop + 32 * s);
    ctx.stroke();
    ctx.fillStyle = skinColor;
    ctx.beginPath();
    ctx.arc(cx - 32 * s + armSwing, torsoTop + 32 * s, 5 * s, 0, Math.PI * 2);
    ctx.fill();

    // Right Arm
    ctx.strokeStyle = scrubTeal;
    ctx.beginPath();
    ctx.moveTo(cx + 20 * s, torsoTop + 6 * s);
    ctx.lineTo(cx + 32 * s - armSwing, torsoTop + 32 * s);
    ctx.stroke();
    ctx.fillStyle = skinColor;
    ctx.beginPath();
    ctx.arc(cx + 32 * s - armSwing, torsoTop + 32 * s, 5 * s, 0, Math.PI * 2);
    ctx.fill();

    // 4. Legs & Sneakers
    const torsoBottom = torsoTop + torsoHeight;

    if (isSliding) {
      ctx.strokeStyle = scrubPant;
      ctx.lineWidth = 9 * s;
      ctx.beginPath();
      ctx.moveTo(cx - 10 * s, torsoBottom);
      ctx.lineTo(cx + 28 * s, torsoBottom + 12 * s);
      ctx.stroke();
      ctx.fillStyle = '#212121';
      this.roundRect(ctx, cx + 26 * s, torsoBottom + 8 * s, 18 * s, 10 * s, 4 * s);
      ctx.fill();
    } else {
      let legSpread = 0;
      if (isJumping) legSpread = -14 * s;
      else {
        const spreads = [22, 8, -22, -8];
        legSpread = (spreads[frame % 4] || 0) * s;
      }
      const legLen = (isJumping ? 30 : 46) * s;

      // Left Leg
      ctx.strokeStyle = scrubPant;
      ctx.lineWidth = 9 * s;
      ctx.beginPath();
      ctx.moveTo(cx - 10 * s, torsoBottom);
      ctx.lineTo(cx - 12 * s + legSpread, torsoBottom + legLen);
      ctx.stroke();

      ctx.fillStyle = shoeWhite;
      this.roundRect(ctx, cx - 24 * s + legSpread, torsoBottom + legLen - 4 * s, 20 * s, 10 * s, 3 * s);
      ctx.fill();
      ctx.strokeStyle = shoeSole;
      ctx.lineWidth = 3 * s;
      ctx.beginPath();
      ctx.moveTo(cx - 24 * s + legSpread, torsoBottom + legLen + 6 * s);
      ctx.lineTo(cx - 4 * s + legSpread, torsoBottom + legLen + 6 * s);
      ctx.stroke();

      // Right Leg
      ctx.strokeStyle = scrubPant;
      ctx.lineWidth = 9 * s;
      ctx.beginPath();
      ctx.moveTo(cx + 10 * s, torsoBottom);
      ctx.lineTo(cx + 12 * s - legSpread, torsoBottom + legLen);
      ctx.stroke();

      ctx.fillStyle = shoeWhite;
      this.roundRect(ctx, cx + 4 * s - legSpread, torsoBottom + legLen - 4 * s, 20 * s, 10 * s, 3 * s);
      ctx.fill();
      ctx.strokeStyle = shoeSole;
      ctx.lineWidth = 3 * s;
      ctx.beginPath();
      ctx.moveTo(cx + 4 * s - legSpread, torsoBottom + legLen + 6 * s);
      ctx.lineTo(cx + 24 * s - legSpread, torsoBottom + legLen + 6 * s);
      ctx.stroke();
    }
  }

  // Draw Dental Police Officer
  drawPolice(ctx, cx, cy, scale, isSaluting) {
    const s = scale;
    const skinColor = '#FFCC80';
    const uniformBlue = '#0D1B2A';
    const vestNeon = '#76FF03';
    const headCenter = { x: cx, y: cy - 70 * s };

    // Cap
    ctx.fillStyle = uniformBlue;
    this.roundRect(ctx, headCenter.x - 24 * s, headCenter.y - 32 * s, 48 * s, 18 * s, 4 * s);
    ctx.fill();
    ctx.fillStyle = '#FFD700';
    ctx.beginPath();
    ctx.arc(headCenter.x, headCenter.y - 24 * s, 5 * s, 0, Math.PI * 2);
    ctx.fill();
    ctx.fillStyle = '#000000';
    ctx.fillRect(headCenter.x - 22 * s, headCenter.y - 14 * s, 44 * s, 5 * s);

    // Head
    ctx.fillStyle = skinColor;
    ctx.beginPath();
    ctx.arc(headCenter.x, headCenter.y, 18 * s, 0, Math.PI * 2);
    ctx.fill();

    // Eyes & Mustache
    ctx.fillStyle = '#000000';
    ctx.beginPath();
    ctx.arc(headCenter.x - 6 * s, headCenter.y - 2 * s, 2.5 * s, 0, Math.PI * 2);
    ctx.arc(headCenter.x + 6 * s, headCenter.y - 2 * s, 2.5 * s, 0, Math.PI * 2);
    ctx.fill();
    ctx.strokeStyle = '#3E2723';
    ctx.lineWidth = 3 * s;
    ctx.beginPath();
    ctx.moveTo(headCenter.x - 8 * s, headCenter.y + 6 * s);
    ctx.lineTo(headCenter.x + 8 * s, headCenter.y + 6 * s);
    ctx.stroke();

    // Torso with reflective vest
    const torsoTop = cy - 50 * s;
    const torsoH = 50 * s;
    ctx.fillStyle = uniformBlue;
    this.roundRect(ctx, cx - 22 * s, torsoTop, 44 * s, torsoH, 6 * s);
    ctx.fill();
    ctx.fillStyle = vestNeon;
    ctx.fillRect(cx - 18 * s, torsoTop + 6 * s, 36 * s, torsoH - 12 * s);
    ctx.strokeStyle = '#FFFFFF';
    ctx.lineWidth = 4 * s;
    ctx.beginPath();
    ctx.moveTo(cx - 18 * s, torsoTop + 24 * s);
    ctx.lineTo(cx + 18 * s, torsoTop + 24 * s);
    ctx.stroke();

    // Arm with Stop Baton or Salute
    ctx.lineCap = 'round';
    if (isSaluting) {
      ctx.strokeStyle = uniformBlue;
      ctx.lineWidth = 7 * s;
      ctx.beginPath();
      ctx.moveTo(cx + 22 * s, torsoTop + 10 * s);
      ctx.lineTo(cx + 35 * s, torsoTop - 5 * s);
      ctx.stroke();
      ctx.strokeStyle = skinColor;
      ctx.beginPath();
      ctx.moveTo(cx + 35 * s, torsoTop - 5 * s);
      ctx.lineTo(headCenter.x + 18 * s, headCenter.y - 10 * s);
      ctx.stroke();
    } else {
      ctx.strokeStyle = uniformBlue;
      ctx.lineWidth = 7 * s;
      ctx.beginPath();
      ctx.moveTo(cx + 22 * s, torsoTop + 10 * s);
      ctx.lineTo(cx + 36 * s, torsoTop + 20 * s);
      ctx.stroke();

      // Glowing Stop Baton
      ctx.fillStyle = '#FF1744';
      ctx.fillRect(cx + 34 * s, torsoTop - 35 * s, 10 * s, 55 * s);
      ctx.fillStyle = 'rgba(255, 82, 82, 0.5)';
      ctx.beginPath();
      ctx.arc(cx + 39 * s, torsoTop - 25 * s, 16 * s, 0, Math.PI * 2);
      ctx.fill();
    }

    // Legs
    const legY = torsoTop + torsoH;
    ctx.strokeStyle = uniformBlue;
    ctx.lineWidth = 9 * s;
    ctx.beginPath();
    ctx.moveTo(cx - 10 * s, legY);
    ctx.lineTo(cx - 10 * s, legY + 38 * s);
    ctx.moveTo(cx + 10 * s, legY);
    ctx.lineTo(cx + 10 * s, legY + 38 * s);
    ctx.stroke();

    ctx.fillStyle = '#000000';
    ctx.fillRect(cx - 18 * s, legY + 34 * s, 16 * s, 12 * s);
    ctx.fillRect(cx + 2 * s, legY + 34 * s, 16 * s, 12 * s);
  }

  // Draw Spinning Metallic $25 Gold Coin
  drawCoin(ctx, cx, cy, scale) {
    const s = scale;
    const r = 24 * s;
    const widthFactor = Math.abs(Math.cos(this.coinAngle));

    ctx.save();
    ctx.translate(cx, cy);
    ctx.scale(Math.max(0.12, widthFactor), 1);

    // Coin outer ring
    const grad = ctx.createLinearGradient(-r, -r, r, r);
    grad.addColorStop(0, '#FFF9C4');
    grad.addColorStop(0.3, '#FFD700');
    grad.addColorStop(0.7, '#FFA000');
    grad.addColorStop(1, '#FF6F00');
    ctx.fillStyle = grad;
    ctx.beginPath();
    ctx.arc(0, 0, r, 0, Math.PI * 2);
    ctx.fill();

    // Inner embossed circle
    ctx.strokeStyle = '#FFE082';
    ctx.lineWidth = 2.5 * s;
    ctx.beginPath();
    ctx.arc(0, 0, r * 0.78, 0, Math.PI * 2);
    ctx.stroke();

    // Value text
    ctx.fillStyle = '#422700';
    ctx.font = `bold ${Math.round(11 * s)}px sans-serif`;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText('$25', 0, 0);

    ctx.restore();
  }

  // Draw Obstacles
  drawObstacle(ctx, cx, cy, type, scale) {
    const s = scale;
    const r = 32 * s;

    switch (type) {
      case 'LOW_TRAY': {
        const trayW = r * 2.2;
        const trayH = r * 0.7;
        const trayY = cy - trayH / 2;

        ctx.fillStyle = '#78909C';
        this.roundRect(ctx, cx - trayW / 2, trayY, trayW, trayH, 6 * s);
        ctx.fill();

        ctx.fillStyle = '#ECEFF1';
        this.roundRect(ctx, cx - trayW / 2 + 3 * s, trayY + 3 * s, trayW - 6 * s, trayH - 6 * s, 4 * s);
        ctx.fill();

        // Instruments on tray
        ctx.strokeStyle = '#455A64';
        ctx.lineWidth = 2.5 * s;
        ctx.beginPath();
        ctx.moveTo(cx - trayW * 0.35, trayY + trayH * 0.5);
        ctx.lineTo(cx - trayW * 0.05, trayY + trayH * 0.5);
        ctx.moveTo(cx + trayW * 0.05, trayY + trayH * 0.5);
        ctx.lineTo(cx + trayW * 0.35, trayY + trayH * 0.5);
        ctx.stroke();

        // Banner label
        ctx.fillStyle = '#FF5252';
        ctx.font = `bold ${Math.round(11 * s)}px sans-serif`;
        ctx.textAlign = 'center';
        ctx.fillText('⬆️ JUMP', cx, trayY - 8 * s);
        break;
      }

      case 'HIGH_LAMP': {
        const lampW = r * 2.4;
        const lampH = r * 0.9;
        const lampY = cy - r * 1.6;

        ctx.fillStyle = '#B0BEC5';
        this.roundRect(ctx, cx - lampW / 2, lampY, lampW, lampH, 10 * s);
        ctx.fill();

        // Yellow glowing bulb
        ctx.fillStyle = '#FFF59D';
        ctx.beginPath();
        ctx.ellipse(cx, lampY + lampH * 0.7, lampW * 0.35, lampH * 0.4, 0, 0, Math.PI * 2);
        ctx.fill();

        // Downward light beam
        const beamGrad = ctx.createLinearGradient(cx, lampY + lampH, cx, cy);
        beamGrad.addColorStop(0, 'rgba(255, 235, 59, 0.4)');
        beamGrad.addColorStop(1, 'rgba(255, 235, 59, 0)');
        ctx.fillStyle = beamGrad;
        ctx.beginPath();
        ctx.moveTo(cx - lampW * 0.35, lampY + lampH);
        ctx.lineTo(cx + lampW * 0.35, lampY + lampH);
        ctx.lineTo(cx + lampW * 0.6, cy);
        ctx.lineTo(cx - lampW * 0.6, cy);
        ctx.closePath();
        ctx.fill();

        ctx.fillStyle = '#00E5FF';
        ctx.font = `bold ${Math.round(11 * s)}px sans-serif`;
        ctx.textAlign = 'center';
        ctx.fillText('⬇️ SLIDE', cx, lampY - 8 * s);
        break;
      }

      case 'MOBILE_CART': {
        const cartW = r * 2.0;
        const cartH = r * 2.4;
        const cartGrad = ctx.createLinearGradient(0, cy - cartH / 2, 0, cy + cartH / 2);
        cartGrad.addColorStop(0, '#0288D1');
        cartGrad.addColorStop(1, '#01579B');
        ctx.fillStyle = cartGrad;
        this.roundRect(ctx, cx - cartW / 2, cy - cartH / 2, cartW, cartH, 8 * s);
        ctx.fill();

        ctx.fillStyle = '#FFFFFF';
        ctx.font = `bold ${Math.round(11 * s)}px sans-serif`;
        ctx.textAlign = 'center';
        ctx.fillText('DODGE ⚠️', cx, cy - cartH * 0.55);
        break;
      }

      case 'TOOTH_BARRIER': {
        const toothW = r * 2.2;
        const toothH = r * 2.0;
        const toothGrad = ctx.createLinearGradient(0, cy - toothH / 2, 0, cy + toothH / 2);
        toothGrad.addColorStop(0, '#FFF9C4');
        toothGrad.addColorStop(0.5, '#FFF59D');
        toothGrad.addColorStop(1, '#EEEEEE');
        ctx.fillStyle = toothGrad;
        this.roundRect(ctx, cx - toothW / 2, cy - toothH / 2, toothW, toothH, 14 * s);
        ctx.fill();

        ctx.fillStyle = '#E65100';
        ctx.font = `bold ${Math.round(11 * s)}px sans-serif`;
        ctx.textAlign = 'center';
        ctx.fillText('⬆️ JUMP', cx, cy - toothH * 0.55);
        break;
      }
    }
  }

  // Draw Clinical RCT Procedural Simulations (Synchronized with 0.0 to 1.0 slider)
  drawRctSimulation(ctx, width, height, stageNumber, progress) {
    const cx = width / 2;
    const cy = height / 2;
    ctx.clearRect(0, 0, width, height);

    // Background Container
    ctx.fillStyle = '#0A0F1D';
    ctx.fillRect(0, 0, width, height);

    switch (stageNumber) {
      case 1: {
        // Pulp Chamber Debridement
        const toothW = 160;
        const toothH = 180;
        const toothTop = cy - 70;

        // Tooth outline & crown
        ctx.fillStyle = '#FFF9C4';
        this.roundRect(ctx, cx - toothW / 2, toothTop, toothW, toothH, 24);
        ctx.fill();

        ctx.fillStyle = '#ECEFF1';
        this.roundRect(ctx, cx - toothW / 2 - 8, toothTop - 10, toothW + 16, 50, 20);
        ctx.fill();

        // Pulp Chamber
        const pulpW = 70;
        const pulpH = 60;
        ctx.fillStyle = '#D32F2F';
        this.roundRect(ctx, cx - pulpW / 2, toothTop + 40, pulpW, pulpH, 16);
        ctx.fill();

        // Irrigant wash dissolution progress (EDTA + NaOCl cleaning out pulp)
        if (progress > 0) {
          ctx.fillStyle = '#00E5FF';
          const cleanH = pulpH * progress;
          this.roundRect(ctx, cx - pulpW / 2, toothTop + 40, pulpW, cleanH, 16);
          ctx.fill();
        }

        // Irrigation Needle Cannula coming down
        const needleY = toothTop - 30 + progress * 60;
        ctx.strokeStyle = '#B0BEC5';
        ctx.lineWidth = 5;
        ctx.beginPath();
        ctx.moveTo(cx, toothTop - 50);
        ctx.lineTo(cx, needleY);
        ctx.stroke();

        // Irrigant bubble spray
        if (progress > 0.05 && progress < 0.98) {
          ctx.fillStyle = '#E0F7FA';
          for (let i = 0; i < 6; i++) {
            const bx = cx + (Math.sin(i * 1.5 + progress * 10) * 22);
            const by = needleY + 12 + (i * 5);
            ctx.beginPath();
            ctx.arc(bx, by, 3.5, 0, Math.PI * 2);
            ctx.fill();
          }
        }
        break;
      }

      case 2: {
        // Apical Danger Zone & Side-Vent Safety Stop
        const canalW = 40;
        const canalH = 190;
        const canalTop = cy - 100;

        // Canal wall dentin
        ctx.fillStyle = '#FFE082';
        this.roundRect(ctx, cx - 70, canalTop, 140, canalH, 16);
        ctx.fill();

        ctx.fillStyle = '#263238';
        this.roundRect(ctx, cx - canalW / 2, canalTop, canalW, canalH, 8);
        ctx.fill();

        // Apical Constriction Stop Line (2mm short)
        const stopY = canalTop + canalH - 25;
        ctx.strokeStyle = '#00E676';
        ctx.lineWidth = 3;
        ctx.setLineDash([4, 4]);
        ctx.beginPath();
        ctx.moveTo(cx - 35, stopY);
        ctx.lineTo(cx + 35, stopY);
        ctx.stroke();
        ctx.setLineDash([]);

        // Side-vent needle advancing
        const tipY = canalTop + progress * (canalH - 25);
        ctx.strokeStyle = '#90A4AE';
        ctx.lineWidth = 6;
        ctx.beginPath();
        ctx.moveTo(cx, canalTop - 20);
        ctx.lineTo(cx, tipY);
        ctx.stroke();

        // Side-vent port reflux spray
        ctx.fillStyle = '#40C4FF';
        ctx.beginPath();
        ctx.arc(cx - 8, tipY - 8, 4, 0, Math.PI * 2);
        ctx.arc(cx + 8, tipY - 8, 4, 0, Math.PI * 2);
        ctx.fill();

        // Apex Locator Digital Gauge
        ctx.fillStyle = progress >= 0.95 ? '#00E676' : '#FFD54F';
        ctx.font = 'bold 12px sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText(progress >= 0.95 ? 'APICAL CONSTRICTION 0.0' : `EAL: ${(1.0 - progress).toFixed(1)} mm`, cx, canalTop + canalH + 20);
        break;
      }

      case 3: {
        // Calcium Hydroxide Intracanal Medicament Delivery
        const canalW = 44;
        const canalH = 180;
        const canalTop = cy - 90;

        ctx.fillStyle = '#FFE082';
        this.roundRect(ctx, cx - 65, canalTop, 130, canalH, 16);
        ctx.fill();

        ctx.fillStyle = '#1A237E';
        this.roundRect(ctx, cx - canalW / 2, canalTop, canalW, canalH, 8);
        ctx.fill();

        // Opaque white Ca(OH)2 paste filling canal from apical upward
        if (progress > 0) {
          const fillH = canalH * progress;
          ctx.fillStyle = '#FFFFFF';
          this.roundRect(ctx, cx - canalW / 2, canalTop + canalH - fillH, canalW, fillH, 6);
          ctx.fill();

          // Diffusing Hydroxyl (OH-) ions
          ctx.fillStyle = '#FFD54F';
          ctx.font = 'bold 10px sans-serif';
          for (let i = 0; i < 4; i++) {
            const ox = (i % 2 === 0) ? cx - 48 : cx + 24;
            const oy = canalTop + canalH - (i * 35 * progress) - 10;
            ctx.fillText('OH⁻', ox, oy);
          }
        }

        ctx.fillStyle = '#00E5FF';
        ctx.font = 'bold 12px sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText('pH 12.5 HIGH ALKALINE SEAL', cx, canalTop + canalH + 22);
        break;
      }
    }
  }

  // Draw Story Scene Graphic
  drawStoryScene(ctx, width, height, type) {
    const cx = width / 2;
    const cy = height / 2;
    ctx.clearRect(0, 0, width, height);

    // Deep blue background
    const bgGrad = ctx.createLinearGradient(0, 0, 0, height);
    bgGrad.addColorStop(0, '#0F2027');
    bgGrad.addColorStop(0.5, '#203A43');
    bgGrad.addColorStop(1, '#2C5364');
    ctx.fillStyle = bgGrad;
    ctx.fillRect(0, 0, width, height);

    switch (type) {
      case 'clinic_exterior':
        // Clinic glass facade
        ctx.fillStyle = '#E0F7FA';
        this.roundRect(ctx, cx - 110, cy - 70, 220, 140, 16);
        ctx.fill();
        ctx.strokeStyle = '#00838F';
        ctx.lineWidth = 4;
        ctx.stroke();

        // Red cross dental symbol
        ctx.fillStyle = '#D32F2F';
        ctx.fillRect(cx - 10, cy - 50, 20, 45);
        ctx.fillRect(cx - 22, cy - 38, 44, 20);

        ctx.fillStyle = '#00838F';
        ctx.font = 'bold 14px sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText('RADIANT SMILE CLINIC', cx, cy + 30);
        break;

      case 'tooth_pain':
        // Aching tooth with lightning zaps
        ctx.fillStyle = '#FFF9C4';
        this.roundRect(ctx, cx - 60, cy - 60, 120, 120, 24);
        ctx.fill();

        // Electrical zaps
        ctx.strokeStyle = '#FF1744';
        ctx.lineWidth = 3.5;
        ctx.beginPath();
        ctx.moveTo(cx - 70, cy - 40);
        ctx.lineTo(cx - 40, cy - 10);
        ctx.lineTo(cx - 65, cy);
        ctx.lineTo(cx - 30, cy + 35);

        ctx.moveTo(cx + 70, cy - 40);
        ctx.lineTo(cx + 40, cy - 10);
        ctx.lineTo(cx + 65, cy);
        ctx.lineTo(cx + 30, cy + 35);
        ctx.stroke();

        ctx.fillStyle = '#D50000';
        ctx.font = 'bold 15px sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText('⚡ ACUTE PULPITIS ⚡', cx, cy + 85);
        break;

      case 'dr_smile':
      case 'examination':
      case 'diagnosis':
        // Dr. Smile portrait
        this.drawAlex(ctx, cx, cy + 30, 0, false, false, 1.25);
        ctx.fillStyle = '#FFD700';
        ctx.font = 'bold 14px sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText('👨‍⚕️ Dr. Smile, Endodontist', cx, cy + 110);
        break;

      default:
        // Alex ready to run
        this.drawAlex(ctx, cx, cy + 30, 0, false, false, 1.25);
        break;
    }
  }

  roundRect(ctx, x, y, width, height, radius) {
    ctx.beginPath();
    ctx.moveTo(x + radius, y);
    ctx.lineTo(x + width - radius, y);
    ctx.quadraticCurveTo(x + width, y, x + width, y + radius);
    ctx.lineTo(x + width, y + height - radius);
    ctx.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
    ctx.lineTo(x + radius, y + height);
    ctx.quadraticCurveTo(x, y + height, x, y + height - radius);
    ctx.lineTo(x, y + radius);
    ctx.quadraticCurveTo(x, y, x + radius, y);
    ctx.closePath();
  }
}

const renderer = new CanvasRenderer();
