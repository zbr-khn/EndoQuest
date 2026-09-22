// EndoQuest Single-Page Application (SPA) Controller
// State Management, Screen Transitions, 4th-Year BDS Pool, Simulation Stages

class AppController {
  constructor() {
    this.currentScreen = 'screen-menu';
    this.storySceneIndex = 0;
    this.runner = null;

    // BDS Question Pool Management
    this.usedQuestionIds = new Set();
    this.currentCheckpointQuestion = null;
    this.shuffledOptions = [];
    this.selectedOptionIndex = null;
    this.isCheckpointAnswerCorrect = null;

    // Simulation Stages Management
    this.currentStageIndex = 0;
    this.currentStageStep = 0; // 0 = Quiz, 1 = Slider
    this.stageSliderProgress = 0;
    this.stageQuizAnswered = false;
    this.simScore = 0;
    this.totalQuestionsAnswered = 0;
    this.totalQuestionsCorrect = 0;

    // Countdown Timer for $5k
    this.autoTransitionTimer = null;
    this.autoCountdown = 3;

    this.init();
  }

  init() {
    // 1. Setup Runner Canvas
    const runnerCanvas = document.getElementById('runner-canvas');
    this.runner = new ToothRunner(
      runnerCanvas,
      (money) => this.onMoneyUpdate(money),
      () => this.openPoliceCheckpoint(),
      () => this.showGoalCelebration(),
      () => this.onGameOver()
    );

    // 2. Setup Story Canvas
    this.storyCanvas = document.getElementById('story-canvas');

    // 3. Setup Sim Canvas
    this.simCanvas = document.getElementById('sim-canvas');

    // Window Resize handling
    window.addEventListener('resize', () => {
      if (this.currentScreen === 'screen-runner') {
        this.runner.resize();
      }
    });

    // PWA Service Worker Registration
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.register('./sw.js').catch((e) => console.log('SW error:', e));
    }

    this.bindEvents();
    this.showScreen('screen-menu');
  }

  bindEvents() {
    // Menu Buttons
    document.getElementById('btn-play-story').onclick = () => {
      sounds.init();
      this.startStory();
    };
    document.getElementById('btn-how-to-play').onclick = () => this.showScreen('screen-instructions');
    document.getElementById('btn-guide').onclick = () => this.showScreen('screen-guide');
    document.getElementById('btn-menu-from-instructions').onclick = () => this.showScreen('screen-menu');
    document.getElementById('btn-menu-from-guide').onclick = () => this.showScreen('screen-menu');

    // Audio Toggle
    const audioBtn = document.getElementById('btn-toggle-audio');
    if (audioBtn) {
      audioBtn.onclick = () => {
        const muted = sounds.toggleMute();
        audioBtn.textContent = muted ? '🔇' : '🔊';
      };
    }

    // Story Navigation
    document.getElementById('btn-story-next').onclick = () => this.advanceStory();
    document.getElementById('btn-story-skip').onclick = () => this.startRunner();

    // Runner Testing Cheats
    document.getElementById('btn-cheat-500').onclick = () => {
      this.runner.addMoney(500);
    };
    document.getElementById('btn-cheat-5k').onclick = () => {
      this.runner.addMoney(5000);
    };
    document.getElementById('btn-clinic-direct').onclick = () => {
      this.goToClinicIntro();
    };
    document.getElementById('btn-persistent-clinic').onclick = () => {
      this.goToClinicIntro();
    };

    // Runner Touch Controls (On-Screen Accessibility)
    document.getElementById('ctrl-left')?.addEventListener('click', () => this.runner.moveLeft());
    document.getElementById('ctrl-right')?.addEventListener('click', () => this.runner.moveRight());
    document.getElementById('ctrl-jump')?.addEventListener('click', () => this.runner.jump());
    document.getElementById('ctrl-slide')?.addEventListener('click', () => this.runner.slide());

    // Checkpoint Retry / Resume
    document.getElementById('btn-try-question-again').onclick = () => this.retryCheckpointQuestion();
    document.getElementById('btn-resume-runner').onclick = () => this.resumeFromCheckpoint();

    // Goal Reached Actions
    document.getElementById('btn-enter-clinic-now').onclick = () => this.goToClinicIntro();

    // Clinic Intro
    document.getElementById('btn-start-rct-stages').onclick = () => this.startClinicalStages();

    // Sim Stage Navigation
    document.getElementById('btn-sim-to-slider').onclick = () => {
      this.currentStageStep = 1;
      this.renderStageView();
    };

    // Sim Slider Drag Listener
    const slider = document.getElementById('sim-procedure-slider');
    slider.oninput = (e) => {
      this.stageSliderProgress = parseFloat(e.target.value) / 100;
      this.updateSimSliderUI();
    };

    document.getElementById('btn-advance-sim-stage').onclick = () => this.advanceSimStage();

    // Results Actions
    document.getElementById('btn-play-again').onclick = () => {
      this.resetGame();
      this.showScreen('screen-menu');
    };
  }

  showScreen(screenId) {
    document.querySelectorAll('.app-screen').forEach((el) => {
      el.classList.add('hidden');
    });
    const target = document.getElementById(screenId);
    if (target) target.classList.remove('hidden');
    this.currentScreen = screenId;

    if (screenId === 'screen-runner') {
      setTimeout(() => {
        this.runner.resize();
        this.runner.start();
      }, 50);
    } else {
      if (this.runner) this.runner.pause();
    }
  }

  // Story Mode Flow
  startStory() {
    this.storySceneIndex = 0;
    this.showScreen('screen-story');
    this.renderStoryScene();
  }

  renderStoryScene() {
    const scene = STORY_SCENES[this.storySceneIndex];
    document.getElementById('story-scene-tag').textContent = scene.sceneTag;
    document.getElementById('story-scene-title').textContent = scene.title;
    document.getElementById('story-speaker').textContent = scene.speaker;
    document.getElementById('story-dialogue').textContent = scene.dialogue;
    document.getElementById('story-description').textContent = scene.description;

    // Next Button Label
    const nextBtn = document.getElementById('btn-story-next');
    if (this.storySceneIndex === STORY_SCENES.length - 1) {
      nextBtn.textContent = 'Start 3D Runner! 🏃💨';
    } else {
      nextBtn.textContent = 'Next Scene ➡️';
    }

    // Render Scene Canvas Illustration
    const dpr = window.devicePixelRatio || 1;
    const rect = this.storyCanvas.getBoundingClientRect();
    this.storyCanvas.width = rect.width * dpr;
    this.storyCanvas.height = rect.height * dpr;
    const ctx = this.storyCanvas.getContext('2d');
    ctx.resetTransform?.();
    ctx.scale(dpr, dpr);
    renderer.drawStoryScene(ctx, rect.width, rect.height, scene.type);
  }

  advanceStory() {
    if (this.storySceneIndex < STORY_SCENES.length - 1) {
      this.storySceneIndex++;
      this.renderStoryScene();
    } else {
      this.startRunner();
    }
  }

  startRunner() {
    this.resetGame();
    this.showScreen('screen-runner');
  }

  onMoneyUpdate(money) {
    document.getElementById('hud-money').textContent = `$${money}`;
    document.getElementById('hud-coins').textContent = `(${Math.floor(money / 25)} coins)`;

    // Check $5,000 target visibility for persistent button
    const persistentBtn = document.getElementById('btn-persistent-clinic');
    const directBtn = document.getElementById('btn-clinic-direct');
    if (money >= 5000) {
      if (persistentBtn) persistentBtn.classList.remove('hidden');
      if (directBtn) directBtn.classList.remove('hidden');
    }
  }

  // 4th-Year BDS Question Bank Checkpoint System
  openPoliceCheckpoint() {
    // 1. Select unused question from the 9 BDS MCQs
    const unused = BDS_QUESTION_POOL.filter((q) => !this.usedQuestionIds.has(q.id));
    const pool = unused.length > 0 ? unused : BDS_QUESTION_POOL;
    this.currentCheckpointQuestion = pool[Math.floor(Math.random() * pool.length)];

    // 2. Independent Option Shuffling
    this.shuffledOptions = [...this.currentCheckpointQuestion.rawOptions].sort(() => Math.random() - 0.5);
    this.selectedOptionIndex = null;
    this.isCheckpointAnswerCorrect = null;

    this.renderCheckpointModal();
    document.getElementById('modal-checkpoint').classList.remove('hidden');
  }

  renderCheckpointModal() {
    const q = this.currentCheckpointQuestion;
    document.getElementById('checkpoint-category').textContent = q.category;
    document.getElementById('checkpoint-question-text').textContent = q.questionText;

    const optionsContainer = document.getElementById('checkpoint-options-container');
    optionsContainer.innerHTML = '';

    const prefixes = ['A. ', 'B. ', 'C. ', 'D. '];
    this.shuffledOptions.forEach((optText, idx) => {
      const btn = document.createElement('button');
      btn.className = 'quiz-option-btn';
      btn.textContent = `${prefixes[idx]}${optText}`;

      if (this.selectedOptionIndex !== null) {
        if (this.isCheckpointAnswerCorrect && idx === this.selectedOptionIndex) {
          btn.classList.add('correct');
        } else if (!this.isCheckpointAnswerCorrect && idx === this.selectedOptionIndex) {
          btn.classList.add('wrong');
        }
        if (optText === q.correctAnswerText) {
          btn.classList.add('highlight-correct');
        }
      }

      btn.onclick = () => {
        if (this.isCheckpointAnswerCorrect) return; // already solved
        this.submitCheckpointAnswer(idx, optText);
      };
      optionsContainer.appendChild(btn);
    });

    // Feedback Container
    const feedbackBox = document.getElementById('checkpoint-feedback-container');
    const tryAgainBtn = document.getElementById('btn-try-question-again');
    const resumeBtn = document.getElementById('btn-resume-runner');

    if (this.isCheckpointAnswerCorrect === true) {
      feedbackBox.className = 'feedback-card correct';
      feedbackBox.innerHTML = `
        <strong>✅ INSPECTION PASSED! Officer Salutes: +$50 Reward!</strong>
        <p>${q.explanation}</p>
      `;
      feedbackBox.classList.remove('hidden');
      tryAgainBtn.classList.add('hidden');
      resumeBtn.classList.remove('hidden');
    } else if (this.isCheckpointAnswerCorrect === false) {
      feedbackBox.className = 'feedback-card wrong';
      feedbackBox.innerHTML = `
        <strong>❌ ROAD INSPECTION FAILED. Progression Locked!</strong>
        <p>${q.explanation}</p>
      `;
      feedbackBox.classList.remove('hidden');
      tryAgainBtn.classList.remove('hidden');
      resumeBtn.classList.add('hidden');
    } else {
      feedbackBox.classList.add('hidden');
      tryAgainBtn.classList.add('hidden');
      resumeBtn.classList.add('hidden');
    }
  }

  submitCheckpointAnswer(idx, optText) {
    this.selectedOptionIndex = idx;
    const isCorrect = optText === this.currentCheckpointQuestion.correctAnswerText;
    this.isCheckpointAnswerCorrect = isCorrect;
    this.totalQuestionsAnswered++;

    if (isCorrect) {
      this.totalQuestionsCorrect++;
      sounds.playCorrect();
      this.usedQuestionIds.add(this.currentCheckpointQuestion.id);
      this.runner.addMoney(50); // +$50 bonus
    } else {
      sounds.playWrong();
    }
    this.renderCheckpointModal();
  }

  retryCheckpointQuestion() {
    this.selectedOptionIndex = null;
    this.isCheckpointAnswerCorrect = null;
    this.renderCheckpointModal();
  }

  resumeFromCheckpoint() {
    document.getElementById('modal-checkpoint').classList.add('hidden');
    this.runner.resumeFromPolice();
  }

  // $5,000 Target Celebration & Auto-Countdown
  showGoalCelebration() {
    const modal = document.getElementById('modal-celebration');
    modal.classList.remove('hidden');
    this.autoCountdown = 3;
    const countdownEl = document.getElementById('celebration-countdown');
    countdownEl.textContent = `⏳ Automatically entering clinic in ${this.autoCountdown}s...`;

    clearInterval(this.autoTransitionTimer);
    this.autoTransitionTimer = setInterval(() => {
      this.autoCountdown--;
      if (this.autoCountdown > 0) {
        countdownEl.textContent = `⏳ Automatically entering clinic in ${this.autoCountdown}s...`;
      } else {
        clearInterval(this.autoTransitionTimer);
        this.goToClinicIntro();
      }
    }, 1000);
  }

  goToClinicIntro() {
    clearInterval(this.autoTransitionTimer);
    document.getElementById('modal-celebration').classList.add('hidden');
    document.getElementById('clinic-funds-collected').textContent = `$${this.runner.moneyCollected}`;
    this.showScreen('screen-rct-intro');
  }

  startClinicalStages() {
    this.currentStageIndex = 0;
    this.currentStageStep = 0;
    this.stageSliderProgress = 0;
    this.stageQuizAnswered = false;
    this.showScreen('screen-rct-stage');
    this.renderStageView();
  }

  renderStageView() {
    const stage = RCT_STAGES[this.currentStageIndex];
    document.getElementById('sim-stage-counter').textContent = `RCT STAGE ${stage.stageNumber} OF 3`;
    document.getElementById('sim-stage-title').textContent = stage.title;

    const step1Pill = document.getElementById('pill-step-1');
    const step2Pill = document.getElementById('pill-step-2');
    const quizCard = document.getElementById('sim-quiz-card');
    const sliderCard = document.getElementById('sim-slider-card');

    if (this.currentStageStep === 0) {
      step1Pill.classList.add('active');
      step2Pill.classList.remove('active');
      quizCard.classList.remove('hidden');
      sliderCard.classList.add('hidden');
      this.renderStageQuiz(stage);
    } else {
      step1Pill.classList.remove('active');
      step2Pill.classList.add('active');
      quizCard.classList.add('hidden');
      sliderCard.classList.remove('hidden');
      this.renderStageSlider(stage);
    }
  }

  renderStageQuiz(stage) {
    const q = stage.quizQuestion;
    document.getElementById('sim-edu-desc').textContent = stage.educationalDescription;
    document.getElementById('sim-question-text').textContent = q.questionText;

    const optionsContainer = document.getElementById('sim-quiz-options');
    optionsContainer.innerHTML = '';
    const feedbackBox = document.getElementById('sim-quiz-feedback');
    const proceedBtn = document.getElementById('btn-sim-to-slider');
    proceedBtn.disabled = true;

    q.options.forEach((opt, idx) => {
      const btn = document.createElement('button');
      btn.className = 'quiz-option-btn';
      btn.textContent = opt;

      btn.onclick = () => {
        if (this.stageQuizAnswered) return;
        const correct = idx === q.correctAnswerIndex;
        this.totalQuestionsAnswered++;

        if (correct) {
          this.totalQuestionsCorrect++;
          this.stageQuizAnswered = true;
          sounds.playCorrect();
          btn.classList.add('correct');
          feedbackBox.className = 'feedback-card correct';
          feedbackBox.innerHTML = `<strong>✅ Correct! Clinical Task Understood.</strong><p>${q.explanation}</p>`;
          feedbackBox.classList.remove('hidden');
          proceedBtn.disabled = false;
        } else {
          sounds.playWrong();
          btn.classList.add('wrong');
          feedbackBox.className = 'feedback-card wrong';
          feedbackBox.innerHTML = `<strong>❌ Incorrect. Try Again!</strong><p>${q.explanation}</p>`;
          feedbackBox.classList.remove('hidden');
        }
      };
      optionsContainer.appendChild(btn);
    });
  }

  renderStageSlider(stage) {
    document.getElementById('sim-slider-subtitle').textContent = stage.procedurePlaceholderName;
    const slider = document.getElementById('sim-procedure-slider');
    slider.value = 0;
    this.stageSliderProgress = 0;
    this.updateSimSliderUI();
  }

  updateSimSliderUI() {
    const p = this.stageSliderProgress;
    document.getElementById('sim-slider-percent').textContent = `${Math.round(p * 100)}%`;

    const statusText = document.getElementById('sim-slider-status');
    const advanceBtn = document.getElementById('btn-advance-sim-stage');

    if (p >= 0.99) {
      statusText.textContent = '✨ Procedure Step Completed 100%!';
      statusText.className = 'slider-status completed';
      advanceBtn.disabled = false;
    } else {
      statusText.textContent = '👉 Slide the slider up to 100% to complete this procedure';
      statusText.className = 'slider-status';
      advanceBtn.disabled = true;
    }

    // Render Canvas Simulation
    const dpr = window.devicePixelRatio || 1;
    const rect = this.simCanvas.getBoundingClientRect();
    this.simCanvas.width = rect.width * dpr;
    this.simCanvas.height = rect.height * dpr;
    const ctx = this.simCanvas.getContext('2d');
    ctx.resetTransform?.();
    ctx.scale(dpr, dpr);
    renderer.drawRctSimulation(ctx, rect.width, rect.height, this.currentStageIndex + 1, p);
  }

  advanceSimStage() {
    if (this.currentStageIndex < RCT_STAGES.length - 1) {
      this.currentStageIndex++;
      this.currentStageStep = 0;
      this.stageQuizAnswered = false;
      this.stageSliderProgress = 0;
      this.renderStageView();
    } else {
      this.showResultsScreen();
    }
  }

  showResultsScreen() {
    sounds.playCelebration();
    const accuracy = this.totalQuestionsAnswered > 0
      ? Math.round((this.totalQuestionsCorrect / this.totalQuestionsAnswered) * 100)
      : 100;

    document.getElementById('res-money').textContent = `$${this.runner.moneyCollected}`;
    document.getElementById('res-accuracy').textContent = `${accuracy}%`;
    document.getElementById('res-grade').textContent = accuracy >= 80 ? 'Distinction (4th-Year BDS)' : 'Pass (Competent)';
    this.showScreen('screen-results');
  }

  onGameOver() {
    alert('Collision with dental obstacle! Restarting run...');
    this.resetGame();
    this.showScreen('screen-runner');
  }

  resetGame() {
    this.usedQuestionIds.clear();
    this.currentStageIndex = 0;
    this.currentStageStep = 0;
    this.totalQuestionsAnswered = 0;
    this.totalQuestionsCorrect = 0;
    if (this.runner) {
      this.runner.reset();
    }
  }
}

// Instantiate on window load
window.addEventListener('DOMContentLoaded', () => {
  window.app = new AppController();
});
