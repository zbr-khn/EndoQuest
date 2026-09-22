// EndoQuest 3D Runner Game Loop & Physics Controller
// 60fps Animation, Smooth Lane Lerping, Touch Swipes, Obstacle Spawning, & Collision

class ToothRunner {
  constructor(canvas, onMoneyChange, onPoliceEncounter, onGoalReached, onGameOver) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
    this.onMoneyChange = onMoneyChange;
    this.onPoliceEncounter = onPoliceEncounter;
    this.onGoalReached = onGoalReached;
    this.onGameOver = onGameOver;

    // Player State
    this.playerLane = 1; // 0 = Left, 1 = Center, 2 = Right
    this.animatedLaneX = 0; // -1 to +1
    this.isJumping = false;
    this.isSliding = false;
    this.jumpTimer = null;
    this.slideTimer = null;
    this.runFrame = 0;
    this.frameTicker = 0;

    // Economy & Progress
    this.moneyCollected = 0;
    this.targetMoney = 5000;
    this.hasTriggeredPolice500 = false;
    this.isPoliceActive = false;
    this.policeSprint = 0;
    this.isSaluting = false;
    this.invulnerableUntil = Date.now() + 4000;
    this.isTargetMet = false;

    // Items (Coins & Obstacles)
    this.items = [];
    this.nextItemId = 1;
    this.roadOffset = 0;
    this.baseSpeed = 0.009;

    // Loop
    this.isRunning = false;
    this.animationId = null;

    // Touch Swipe Detection
    this.touchStartX = 0;
    this.touchStartY = 0;
    this.setupControls();
  }

  setupControls() {
    // Keyboard
    window.addEventListener('keydown', (e) => {
      if (!this.isRunning || this.isPoliceActive) return;
      if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight', ' '].includes(e.key)) {
        e.preventDefault();
      }
      switch (e.key) {
        case 'ArrowLeft':
        case 'a':
        case 'A':
          this.moveLeft();
          break;
        case 'ArrowRight':
        case 'd':
        case 'D':
          this.moveRight();
          break;
        case 'ArrowUp':
        case 'w':
        case 'W':
        case ' ':
          this.jump();
          break;
        case 'ArrowDown':
        case 's':
        case 'S':
          this.slide();
          break;
      }
    });

    // Mobile Touch Swipes on Canvas
    this.canvas.addEventListener('touchstart', (e) => {
      const touch = e.touches[0];
      this.touchStartX = touch.clientX;
      this.touchStartY = touch.clientY;
    }, { passive: true });

    this.canvas.addEventListener('touchend', (e) => {
      if (!this.isRunning || this.isPoliceActive) return;
      const touch = e.changedTouches[0];
      const dx = touch.clientX - this.touchStartX;
      const dy = touch.clientY - this.touchStartY;
      const absDx = Math.abs(dx);
      const absDy = Math.abs(dy);

      if (Math.max(absDx, absDy) > 25) {
        if (absDx > absDy) {
          if (dx > 0) this.moveRight();
          else this.moveLeft();
        } else {
          if (dy < 0) this.jump();
          else this.slide();
        }
      }
    }, { passive: true });
  }

  moveLeft() {
    if (this.playerLane > 0) {
      this.playerLane--;
    }
  }

  moveRight() {
    if (this.playerLane < 2) {
      this.playerLane++;
    }
  }

  jump() {
    if (this.isJumping) return;
    this.isJumping = true;
    this.isSliding = false;
    clearTimeout(this.slideTimer);
    sounds.playJump();
    this.jumpTimer = setTimeout(() => {
      this.isJumping = false;
    }, 720);
  }

  slide() {
    if (this.isSliding) return;
    this.isSliding = true;
    this.isJumping = false;
    clearTimeout(this.jumpTimer);
    sounds.playSlide();
    this.slideTimer = setTimeout(() => {
      this.isSliding = false;
    }, 750);
  }

  addMoney(amount) {
    this.moneyCollected += amount;
    this.onMoneyChange(this.moneyCollected);

    // Milestone 500 Checkpoint Trigger
    if (this.moneyCollected >= 500 && !this.hasTriggeredPolice500) {
      this.hasTriggeredPolice500 = true;
      this.triggerPoliceEncounter();
    }

    // $5,000 Target Achieved
    if (this.moneyCollected >= this.targetMoney && !this.isTargetMet) {
      this.isTargetMet = true;
      sounds.playCelebration();
      this.onGoalReached();
    }
  }

  triggerPoliceEncounter() {
    this.isPoliceActive = true;
    this.policeSprint = 0;
    this.isSaluting = false;
    sounds.playSiren();

    // Sprint approach animation (800ms)
    const startTime = Date.now();
    const duration = 800;

    const animatePolice = () => {
      const elapsed = Date.now() - startTime;
      this.policeSprint = Math.min(1.0, elapsed / duration);
      if (this.policeSprint < 1.0) {
        requestAnimationFrame(animatePolice);
      } else {
        // Open Question Dialog
        this.onPoliceEncounter();
      }
    };
    requestAnimationFrame(animatePolice);
  }

  resumeFromPolice() {
    this.isSaluting = true;
    setTimeout(() => {
      this.isPoliceActive = false;
      this.isSaluting = false;
      this.invulnerableUntil = Date.now() + 4000;
      // Clear nearby obstacles
      this.items = this.items.filter(it => it.isCoin || it.depth < 0.45);
    }, 900);
  }

  start() {
    if (this.isRunning) return;
    this.isRunning = true;
    this.lastTime = Date.now();
    this.loop();
  }

  pause() {
    this.isRunning = false;
    if (this.animationId) {
      cancelAnimationFrame(this.animationId);
      this.animationId = null;
    }
  }

  reset() {
    this.playerLane = 1;
    this.animatedLaneX = 0;
    this.isJumping = false;
    this.isSliding = false;
    this.moneyCollected = 0;
    this.hasTriggeredPolice500 = false;
    this.isPoliceActive = false;
    this.isSaluting = false;
    this.isTargetMet = false;
    this.items = [];
    this.invulnerableUntil = Date.now() + 4000;
    this.onMoneyChange(0);
  }

  resize() {
    const dpr = window.devicePixelRatio || 1;
    const rect = this.canvas.getBoundingClientRect();
    this.canvas.width = rect.width * dpr;
    this.canvas.height = rect.height * dpr;
    this.ctx.resetTransform?.();
    this.ctx.scale(dpr, dpr);
    this.cssWidth = rect.width;
    this.cssHeight = rect.height;
  }

  loop() {
    if (!this.isRunning) return;

    const now = Date.now();
    const dt = (now - this.lastTime) / 1000;
    this.lastTime = now;

    this.update(dt);
    this.render();

    this.animationId = requestAnimationFrame(() => this.loop());
  }

  update(dt) {
    // 1. Frame Ticker & Coin Rotation
    this.frameTicker++;
    if (this.frameTicker % 7 === 0) {
      this.runFrame = (this.runFrame + 1) % 4;
    }
    renderer.coinAngle += 0.08;

    // 2. Smooth Lane Interpolation (Spring Lerp)
    const targetX = this.playerLane === 0 ? -1 : (this.playerLane === 1 ? 0 : 1);
    this.animatedLaneX += (targetX - this.animatedLaneX) * 0.22;

    if (this.isPoliceActive) return;

    // 3. Road Movement Offset
    this.roadOffset = (this.roadOffset + 0.025) % 1.0;

    // 4. Spawn Items (Coins & Obstacles)
    if (!this.isTargetMet && Math.random() < 0.038) {
      const lane = Math.floor(Math.random() * 3);
      const isCoin = Math.random() > 0.42;
      const obsTypes = ['LOW_TRAY', 'HIGH_LAMP', 'MOBILE_CART', 'TOOTH_BARRIER'];
      const obsType = isCoin ? 'NONE' : obsTypes[Math.floor(Math.random() * obsTypes.length)];

      this.items.push({
        id: this.nextItemId++,
        lane: lane,
        depth: 0.0,
        isCoin: isCoin,
        value: 25,
        obstacleType: obsType
      });
    }

    // 5. Update Items Depth & Collisions
    const updatedItems = [];
    const isImmune = Date.now() < this.invulnerableUntil;

    for (let item of this.items) {
      item.depth += this.baseSpeed * Math.min(2.5, Math.max(0.6, dt * 60));

      if (item.depth < 1.05) {
        const itemLaneX = item.lane - 1; // -1, 0, 1
        const laneDiff = Math.abs(itemLaneX - this.animatedLaneX);

        // Check Hit Window at depth 0.86 to 0.98
        if (item.depth >= 0.86 && item.depth <= 0.98 && laneDiff < 0.44) {
          if (item.isCoin) {
            sounds.playCoin();
            this.addMoney(item.value);
            // Collect coin, don't keep in updated
            continue;
          } else if (!this.isTargetMet && !isImmune) {
            let hit = false;
            if (item.obstacleType === 'LOW_TRAY') {
              hit = !this.isJumping;
            } else if (item.obstacleType === 'HIGH_LAMP') {
              hit = !this.isSliding;
            } else {
              hit = true; // Mobile Cart or Tooth Barrier
            }

            if (hit) {
              sounds.playCollision();
              this.pause();
              this.onGameOver();
              return;
            }
          }
        }
        updatedItems.push(item);
      }
    }
    this.items = updatedItems;
  }

  render() {
    const w = this.cssWidth || this.canvas.width;
    const h = this.cssHeight || this.canvas.height;
    const ctx = this.ctx;

    ctx.clearRect(0, 0, w, h);

    // 1. Draw 3D Perspective Highway
    renderer.drawRoadPerspective(ctx, w, h, this.roadOffset);

    const horizonY = h * 0.38;
    const horizonX = w * 0.5;
    const roadWidthTop = w * 0.08;
    const roadWidthBottom = w * 0.94;
    const bottomY = h * 0.98;

    // 2. Sort Items by Depth (Far to Near)
    const sorted = [...this.items].sort((a, b) => a.depth - b.depth);

    for (let item of sorted) {
      const roadW = roadWidthTop + (roadWidthBottom - roadWidthTop) * item.depth;
      const laneW = roadW / 3;
      const itemLaneX = item.lane - 1;
      const itemX = horizonX + (itemLaneX * laneW);
      const itemY = horizonY + (bottomY - horizonY) * item.depth;
      const scale = 0.35 + (0.95 * item.depth);

      if (item.isCoin) {
        renderer.drawCoin(ctx, itemX, itemY, scale);
      } else {
        renderer.drawObstacle(ctx, itemX, itemY, item.obstacleType, scale);
      }
    }

    // 3. Draw Police Officer
    if (this.isPoliceActive) {
      const pDepth = 0.3 + 0.58 * this.policeSprint;
      const pX = horizonX;
      const pY = horizonY + (bottomY - horizonY) * pDepth;
      const pScale = 0.4 + 0.95 * pDepth;
      renderer.drawPolice(ctx, pX, pY, pScale, this.isSaluting);
    }

    // 4. Draw Alex Player Character
    const playerDepth = 0.92;
    const roadWAtPlayer = roadWidthTop + (roadWidthBottom - roadWidthTop) * playerDepth;
    const laneWAtPlayer = roadWAtPlayer / 3;
    const playerX = horizonX + (this.animatedLaneX * laneWAtPlayer);
    const jumpOffset = this.isJumping ? 85 : 0;
    const playerY = horizonY + (bottomY - horizonY) * playerDepth - jumpOffset;

    // Ground Shadow
    ctx.fillStyle = 'rgba(0, 0, 0, 0.4)';
    ctx.beginPath();
    ctx.ellipse(playerX, horizonY + (bottomY - horizonY) * playerDepth + (this.isSliding ? 8 : 22), this.isJumping ? 22 : 36, 10, 0, 0, Math.PI * 2);
    ctx.fill();

    renderer.drawAlex(ctx, playerX, playerY, this.runFrame, this.isJumping, this.isSliding, 1.25);
  }
}
