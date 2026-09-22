// EndoQuest Curriculum & Narrative Data Bank

const STORY_SCENES = [
  {
    sceneNumber: 1,
    sceneTag: "SCENE 1: CLINIC EXTERIOR",
    title: "Radiant Smile Dental Clinic",
    speaker: "Narrator",
    dialogue: "\"A peaceful morning in the city, where state-of-the-art dental care awaits...\"",
    description: "The sparkling modern glass facade of Radiant Smile Dental Clinic gleams in the morning light.",
    type: "clinic_exterior"
  },
  {
    sceneNumber: 2,
    sceneTag: "SCENE 2: TOOTH PAIN",
    title: "Midnight Agony",
    speaker: "Alex",
    dialogue: "\"Owww! My molar feels like it's exploding! No painkiller is touching this!\"",
    description: "At 3:00 AM, Alex awakens in excruciating pain as throbbing pulpitis strikes the lower molar.",
    type: "tooth_pain"
  },
  {
    sceneNumber: 3,
    sceneTag: "SCENE 3: CLINIC ARRIVAL",
    title: "Emergency Arrival",
    speaker: "Alex",
    dialogue: "\"Doctor, please help! I can't take this throbbing pain any longer!\"",
    description: "Clutching the aching jaw in desperation, Alex charges through the clinic's automatic glass doors.",
    type: "clinic_arrival"
  },
  {
    sceneNumber: 4,
    sceneTag: "SCENE 4: DR. SMILE",
    title: "Dr. Smile's Operatory",
    speaker: "Dr. Smile",
    dialogue: "\"Welcome to Radiant Smile, Alex. Take a seat in the chair; let's see what's happening.\"",
    description: "Professional dentist Dr. Smile stands ready in pristine clinical scrubs beside advanced diagnostic equipment.",
    type: "dr_smile"
  },
  {
    sceneNumber: 5,
    sceneTag: "SCENE 5: CLINICAL EXAMINATION",
    title: "Dental Operatory Check",
    speaker: "Dr. Smile",
    dialogue: "\"Open wide. Let me inspect that painful molar with the dental mirror and explorer...\"",
    description: "Under the focused beam of the surgical operatory lamp, Dr. Smile examines the inflamed tooth.",
    type: "examination"
  },
  {
    sceneNumber: 6,
    sceneTag: "SCENE 6: DIAGNOSIS & COST",
    title: "Diagnosis: RCT Required",
    speaker: "Dr. Smile",
    dialogue: "\"The radiograph confirms Irreversible Pulpitis. You need an immediate Root Canal Treatment. Total cost: $5,000.\"",
    description: "Dr. Smile shows the periapical radiograph and hands over the clinical estimate card: $5,000.",
    type: "diagnosis"
  },
  {
    sceneNumber: 7,
    sceneTag: "SCENE 7: TOTAL SHOCK",
    title: "The $5,000 Sticker Shock",
    speaker: "Alex",
    dialogue: "\"WHAT?! $5,000?! For just one tooth?! I don't have that kind of cash on me!\"",
    description: "Alex leaps back in sheer disbelief, jaw dropping to the floor at the staggering $5,000 price tag.",
    type: "shock"
  },
  {
    sceneNumber: 8,
    sceneTag: "SCENE 8: THE RUNNER CALL",
    title: "\"I'll Be Back With the Money!\"",
    speaker: "Alex",
    dialogue: "\"Hold my appointment, Dr. Smile! I'll run through the city and collect every single dollar!\"",
    description: "Determined to save the tooth, Alex bolts out onto the open 3D highway to collect $25 gold coins!",
    type: "runner_call"
  }
];

// Pool of 9 Endodontics MCQs for 4th-Year BDS Students (Runner Checkpoints)
const BDS_QUESTION_POOL = [
  {
    id: "run_q1",
    questionText: "The primary objective of root canal instrumentation is to:",
    rawOptions: [
      "Remove only the pulp tissue",
      "Eliminate microorganisms and shape the canal for obturation",
      "Enlarge the apical foramen",
      "Remove the periodontal ligament"
    ],
    correctAnswerText: "Eliminate microorganisms and shape the canal for obturation",
    explanation: "The primary objective of root canal instrumentation is eliminating microorganisms and shaping the canal space for a fluid-tight 3D obturation.",
    category: "Canal Instrumentation"
  },
  {
    id: "run_q2",
    questionText: "The most commonly used irrigant in root canal treatment is:",
    rawOptions: [
      "Normal saline",
      "Chlorhexidine",
      "Sodium hypochlorite",
      "Hydrogen peroxide"
    ],
    correctAnswerText: "Sodium hypochlorite",
    explanation: "Sodium hypochlorite (NaOCl) is the most commonly used root canal irrigant due to its dual antimicrobial and organic tissue-dissolving properties.",
    category: "Root Canal Irrigants"
  },
  {
    id: "run_q3",
    questionText: "The ideal working length terminates approximately at the:",
    rawOptions: [
      "Anatomical apex",
      "Radiographic apex",
      "Apical constriction",
      "Cementoenamel junction"
    ],
    correctAnswerText: "Apical constriction",
    explanation: "The ideal working length terminates at the apical constriction (minor apical diameter), usually 0.5 to 1.0 mm coronal to the radiographic apex.",
    category: "Working Length Determination"
  },
  {
    id: "run_q4",
    questionText: "The most commonly used material for the core of root canal obturation is:",
    rawOptions: [
      "Zinc oxide eugenol",
      "Gutta-percha",
      "Glass ionomer cement",
      "Calcium hydroxide"
    ],
    correctAnswerText: "Gutta-percha",
    explanation: "Gutta-percha points combined with an endodontic sealer serve as the primary core material for root canal obturation.",
    category: "Obturation Materials"
  },
  {
    id: "run_q5",
    questionText: "Which instrument is primarily used to determine the working length electronically?",
    rawOptions: [
      "Apex locator",
      "Endodontic explorer",
      "Spreader",
      "Plugger"
    ],
    correctAnswerText: "Apex locator",
    explanation: "An electronic apex locator (EAL) uses electrical impedance across multi-frequencies to detect the apical constriction with high accuracy.",
    category: "Endodontic Instruments"
  },
  {
    id: "run_q6",
    questionText: "Sodium hypochlorite is particularly useful in endodontics because of its:",
    rawOptions: [
      "Chelating ability",
      "Tissue-dissolving and antimicrobial properties",
      "Ability to strengthen dentin",
      "Ability to completely remove the smear layer"
    ],
    correctAnswerText: "Tissue-dissolving and antimicrobial properties",
    explanation: "Sodium hypochlorite is uniquely valued in endodontics because it combines broad antimicrobial action with the ability to dissolve vital and necrotic pulp tissue.",
    category: "Endodontic Pharmacology"
  },
  {
    id: "run_q7",
    questionText: "The smear layer produced during root canal instrumentation consists mainly of:",
    rawOptions: [
      "Only bacteria",
      "Organic and inorganic debris",
      "Gutta-percha",
      "Salivary proteins only"
    ],
    correctAnswerText: "Organic and inorganic debris",
    explanation: "The endodontic smear layer consists of dentin shavings, cell fragments, pulp remnants, and bacteria, forming a layer of both organic and inorganic debris.",
    category: "Smear Layer Biology"
  },
  {
    id: "run_q8",
    questionText: "The most common cause of failure of root canal treatment is:",
    rawOptions: [
      "Adequate obturation",
      "Proper coronal seal",
      "Persistent intraradicular infection",
      "Correct working length"
    ],
    correctAnswerText: "Persistent intraradicular infection",
    explanation: "Persistent intraradicular infection (such as biofilms in uninstrumented canal recesses) is the primary etiology of endodontic treatment failure.",
    category: "Treatment Failure Etiology"
  },
  {
    id: "run_q9",
    questionText: "The main purpose of a coronal restoration following root canal therapy is to:",
    rawOptions: [
      "Increase pulp vitality",
      "Prevent coronal leakage and restore function",
      "Increase canal length",
      "Remove the smear layer"
    ],
    correctAnswerText: "Prevent coronal leakage and restore function",
    explanation: "A hermetic coronal restoration prevents coronal microleakage of bacteria and oral fluids into the root canal system and restores masticatory function.",
    category: "Post-Endodontic Restoration"
  }
];

// 3 Clinical RCT Stages for Simulation Masterclass
const RCT_STAGES = [
  {
    stageNumber: 1,
    title: "The Pulp Chamber (Tissue & Smear Layer)",
    educationalDescription: "During chemo-mechanical debridement, tissue dissolution and smear layer removal are paramount. Organic pulp debris and the inorganic smear layer require distinct chemical agents: Sodium Hypochlorite (NaOCl) for dissolving organic pulp remnants, and 17% EDTA for demineralizing the inorganic smear layer.",
    procedurePlaceholderName: "Dissolve or Demineralize: Chemomechanical Debridement",
    quizQuestion: {
      questionText: "Why does EDTA need to follow NaOCl rather than replace it entirely?",
      options: [
        "A) EDTA causes tooth discolouration",
        "B) EDTA has a higher antibacterial effect than NaOCl",
        "C) EDTA has zero organic tissue dissolving capability",
        "D) EDTA dissolves gutta-percha"
      ],
      correctAnswerIndex: 2,
      explanation: "EDTA is a chelating agent that removes the inorganic smear layer debris, but has zero organic tissue dissolving capability. Therefore, it must follow NaOCl, which dissolves organic necrotic and vital pulp tissue."
    }
  },
  {
    stageNumber: 2,
    title: "The Apical Danger Zone (Chemical Safety & Hazards)",
    educationalDescription: "Irrigation in the apical third demands meticulous chemical safety. Using a side-vented safety needle positioned 2-3 mm short of the apex ensures gentle coronal reflux and avoids catastrophic periapical extrusion (hypochlorite accident). Furthermore, NaOCl and Chlorhexidine must never be mixed directly inside the canal.",
    procedurePlaceholderName: "The Side-Vent Safe Zone & Apex Locator Stop",
    quizQuestion: {
      questionText: "You accidentally mix NaOCl and 2% Chlorhexidine directly inside the canal. What harmful compound precipitates?",
      options: [
        "A) Para-chloroaniline (PCA)",
        "B) Calcium chloride",
        "C) Sodium chloride crystals",
        "D) Urea peroxide"
      ],
      correctAnswerIndex: 0,
      explanation: "Mixing NaOCl and 2% Chlorhexidine directly inside the canal creates an insoluble brown-orange precipitate of Para-chloroaniline (PCA), which is cytotoxic, potentially carcinogenic, and occludes dentinal tubules."
    }
  },
  {
    stageNumber: 3,
    title: "The Inter-Appointment Vault (Medicaments)",
    educationalDescription: "Intracanal medicaments placed between appointments eradicate persisting microbes, neutralize lipopolysaccharides, and alleviate acute symptoms. Calcium Hydroxide [Ca(OH)₂] provides prolonged antibacterial action via high alkaline pH, Ledermix treats severe irreversible pulpitis flare-ups, and Metapex serves as a resorbable filler in primary pulpectomy.",
    procedurePlaceholderName: "Match the Medicament & Alkaline Hydroxyl Seal",
    quizQuestion: {
      questionText: "How does Calcium Hydroxide primarily destroy bacterial cell membranes inside the root canal?",
      options: [
        "A) By producing extreme acidic pH below 3.0",
        "B) By chelating calcium ions from the dentin",
        "C) By rapid freezing of the pulp space",
        "D) By releasing hydroxyl (OH⁻) ions at an alkaline pH of ~12.5"
      ],
      correctAnswerIndex: 3,
      explanation: "Calcium Hydroxide [Ca(OH)₂] maintains an intense alkaline pH (~12.5). The release of hydroxyl (OH⁻) ions induces lipid peroxidation, destroying bacterial cellular membranes and denaturing bacterial proteins and DNA."
    }
  }
];
