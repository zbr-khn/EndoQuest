package com.example.endoquest.data

import com.example.endoquest.model.QuizQuestion
import com.example.endoquest.model.RunQuizQuestion

data class QuestionOption(
    val id: String,
    val text: String,
    val isCorrect: Boolean,
    val explanation: String
)

data class EndoQuestion(
    val id: String,
    val title: String,
    val questionText: String,
    val options: List<QuestionOption>,
    val category: String,
    val difficulty: String = "4th-Year BDS"
) {
    val correctOption: QuestionOption
        get() = options.first { it.isCorrect }

    val explanation: String
        get() = correctOption.explanation

    /**
     * Returns a new instance with the answer options shuffled independently.
     * Preserves each option's correctness and explanation regardless of position.
     */
    fun shuffledInstance(): EndoQuestion {
        return copy(options = options.shuffled())
    }

    /**
     * Converts to RunQuizQuestion for gameplay components, ensuring options are shuffled
     * and correctness is anchored to the exact answer text.
     */
    fun toRunQuizQuestion(shuffleOptions: Boolean = true): RunQuizQuestion {
        val targetOptions = if (shuffleOptions) options.shuffled() else options
        return RunQuizQuestion(
            id = id,
            questionText = questionText,
            rawOptions = targetOptions.map { it.text },
            correctAnswerText = correctOption.text,
            explanation = correctOption.explanation,
            difficulty = difficulty,
            category = category
        )
    }

    fun toQuizQuestion(shuffleOptions: Boolean = true): QuizQuestion {
        val targetOptions = if (shuffleOptions) options.shuffled() else options
        val correctIdx = targetOptions.indexOfFirst { it.isCorrect }.coerceAtLeast(0)
        return QuizQuestion(
            id = id,
            questionText = questionText,
            options = targetOptions.map { it.text },
            correctAnswerIndex = correctIdx,
            explanation = correctOption.explanation,
            difficulty = difficulty,
            category = category
        )
    }
}

object QuestionRepository {

    // =========================================================================
    // MASTER 20-QUESTION ENDODONTICS CURRICULUM BANK (Questions 1–20)
    // Preserves exact clinical terminology, mechanisms, and explanations.
    // =========================================================================

    val masterBank: List<EndoQuestion> = listOf(
        // Question 1: Effect of Pre-Heating Sodium Hypochlorite
        EndoQuestion(
            id = "endo_q01",
            title = "Effect of Pre-Heating Sodium Hypochlorite",
            questionText = "Heating a 0.5% or 2.5% Sodium Hypochlorite (NaOCl) solution to 50°C produces which primary clinical outcome?",
            category = "Endodontic Pharmacology & Irrigants",
            options = listOf(
                QuestionOption(
                    id = "q1_optA",
                    text = "Enhances tissue dissolution and bactericidal capacity to match 5.25% solution",
                    isCorrect = true,
                    explanation = "Increasing temperature accelerates the kinetic energy and chemical reaction rate of NaOCl. It lowers surface tension and viscosity, increasing the rate of organic tissue saponification and bacterial membrane oxidation. Pre-heating a lower concentration (0.5%–2.5%) to 50°C can improve tissue-dissolving and antibacterial efficacy, potentially approaching that of an unheated 5.25% solution while using a lower concentration."
                ),
                QuestionOption(
                    id = "q1_optB",
                    text = "Completely converts hypochlorous acid (HOCl) into toxic chlorine gas",
                    isCorrect = false,
                    explanation = "Chlorine gas release occurs under strong acidic conditions (pH below 4). Heating NaOCl to 50°C in an alkaline solution does not rapidly convert it into toxic chlorine gas under clinical irrigation conditions."
                ),
                QuestionOption(
                    id = "q1_optC",
                    text = "Causes rapid neutralization of alkalinity, rendering it chemically inert",
                    isCorrect = false,
                    explanation = "Prolonged high-temperature storage over weeks can accelerate degradation, but acute clinical heating increases immediate reactivity. Heating enhances chemical activity during clinical delivery rather than neutralizing or inactivating the solution."
                ),
                QuestionOption(
                    id = "q1_optD",
                    text = "Prevents the formation of hypochlorous acid during aqueous equilibrium",
                    isCorrect = false,
                    explanation = "NaOCl in water participates in an equilibrium involving HOCl and NaOH. HOCl remains an important active oxidizing species. Heating increases its reaction rate with organic substrates rather than preventing its formation."
                )
            )
        ),

        // Question 2: Mechanism of Passive Ultrasonic Irrigation (PUI)
        EndoQuestion(
            id = "endo_q02",
            title = "Mechanism of Passive Ultrasonic Irrigation (PUI)",
            questionText = "What is the primary physical mechanism through which Passive Ultrasonic Irrigation (PUI) enhances canal debridement?",
            category = "Hydrodynamic Irrigation Dynamics",
            options = listOf(
                QuestionOption(
                    id = "q2_optA",
                    text = "Production of acoustic streaming and cavitation micro-jets in irrigant",
                    isCorrect = true,
                    explanation = "PUI utilizes a non-cutting file or smooth wire oscillating at ultrasonic frequencies, typically around 25–30 kHz. This oscillation induces two hydrodynamic phenomena: 1. Acoustic streaming: High-velocity circular fluid movement around the oscillating instrument. 2. Cavitation: Growth and implosive collapse of microscopic gas bubbles, potentially generating localized micro-jets and shear forces that help disrupt biofilm, debris, and smear layer from root canal walls."
                ),
                QuestionOption(
                    id = "q2_optB",
                    text = "Direct mechanical scraping and enlargement of dentinal walls",
                    isCorrect = false,
                    explanation = "Mechanical enlargement requires cutting instruments, such as NiTi rotary files. PUI is performed passively, without intentionally cutting or enlarging dentinal walls, reducing the risk of canal transportation, gouging, or file separation."
                ),
                QuestionOption(
                    id = "q2_optC",
                    text = "Thermal melting and liquefaction of organic pulpal proteins",
                    isCorrect = false,
                    explanation = "Protein denaturation can occur chemically or through sufficiently high thermal energy. PUI primarily enhances cleaning through irrigant movement and acoustic effects, not thermal liquefaction of proteins."
                ),
                QuestionOption(
                    id = "q2_optD",
                    text = "Electrochemical reduction of dentinal calcium hydroxyapatite",
                    isCorrect = false,
                    explanation = "Demineralization requires chemical chelators, such as EDTA, or acids, such as citric acid. Ultrasonic vibration supplies mechanical acoustic energy and does not perform electrochemical reduction reactions."
                )
            )
        ),

        // Question 3: Clinical Hallmark of a Sodium Hypochlorite Accident
        EndoQuestion(
            id = "endo_q03",
            title = "Clinical Hallmark of a Sodium Hypochlorite Accident",
            questionText = "What is the immediate clinical hallmark indicating that a Sodium Hypochlorite (NaOCl) accident has occurred during irrigation?",
            category = "Clinical Chemical Safety & Hazards",
            options = listOf(
                QuestionOption(
                    id = "q3_optA",
                    text = "Immediate severe burning pain accompanied by rapid facial swelling",
                    isCorrect = true,
                    explanation = "Extrusion of NaOCl through the apical foramen into periapical soft tissues can cause tissue injury through chemical damage, including lipid saponification and cellular destruction. Clinical Hallmark: Immediate, severe burning pain accompanied by rapid localized soft-tissue edema, profuse canal bleeding, and subsequent ecchymosis or bruising."
                ),
                QuestionOption(
                    id = "q3_optB",
                    text = "Gradual asymptomatic discoloration of the clinical crown over several months",
                    isCorrect = false,
                    explanation = "Crown discoloration may result from intracanal medicaments, such as minocycline in TAP, or degraded pulp tissue remaining in the pulp chamber. It is a delayed cosmetic issue, not an acute painful periapical accident."
                ),
                QuestionOption(
                    id = "q3_optC",
                    text = "Sudden onset of profound anesthesia in the distribution of the mental nerve",
                    isCorrect = false,
                    explanation = "Paresthesia can occur secondarily if severe swelling compresses nerve trunks. Intense acute pain and rapid swelling are the immediate hallmark features, rather than immediate anesthesia."
                ),
                QuestionOption(
                    id = "q3_optD",
                    text = "Immediate development of subcutaneous emphysema with crackling crepitus",
                    isCorrect = false,
                    explanation = "Subcutaneous emphysema occurs when compressed air is forced into soft tissues, for example, by using an air syringe inside the canal. A NaOCl accident primarily causes a chemical injury and inflammatory tissue reaction, producing edema rather than air trapped in tissue."
                )
            )
        ),

        // Question 4: Chemical Composition of BioPure MTAD
        EndoQuestion(
            id = "endo_q04",
            title = "Chemical Composition of BioPure MTAD",
            questionText = "Which three chemical constituents comprise the final root canal irrigant known as BioPure MTAD?",
            category = "Endodontic Pharmacology & Irrigants",
            options = listOf(
                QuestionOption(
                    id = "q4_optA",
                    text = "3% Doxycycline, 4.25% Citric Acid, and Polysorbate 80",
                    isCorrect = true,
                    explanation = "Composition and Functions: 3% Doxycycline hyclate: A tetracycline derivative providing broad-spectrum antibacterial activity and inhibition of host matrix metalloproteinases (MMPs). 4.25% Citric acid: An organic acid that chelates minerals and helps dissolve the inorganic component of the smear layer. Polysorbate 80 (Tween 80): A non-ionic surfactant or detergent that reduces surface tension and enhances fluid penetration into dentinal tubules."
                ),
                QuestionOption(
                    id = "q4_optB",
                    text = "2% Chlorhexidine, 17% EDTA, and Benzalkonium chloride",
                    isCorrect = false,
                    explanation = "This combination is not the formulation of MTAD. Directly combining CHX and EDTA may also produce a precipitate."
                ),
                QuestionOption(
                    id = "q4_optC",
                    text = "5.25% NaOCl, 10% Maleic Acid, and Sodium Lauryl Sulfate",
                    isCorrect = false,
                    explanation = "NaOCl is an oxidizing tissue-dissolving irrigant commonly used before a final rinse, not a constituent of the MTAD formulation."
                ),
                QuestionOption(
                    id = "q4_optD",
                    text = "1% Metronidazole, 17% EDTA, and Propylene glycol",
                    isCorrect = false,
                    explanation = "Metronidazole is an antibiotic used in intracanal antibiotic pastes such as DAP or TAP, not a standard ingredient of liquid MTAD."
                )
            )
        ),

        // Question 5: Chemical Precipitate Formation (EDTA + CHX)
        EndoQuestion(
            id = "endo_q05",
            title = "Chemical Precipitate Formation (EDTA + CHX)",
            questionText = "When 17% EDTA is directly combined with 2% Chlorhexidine (CHX) without intermediate rinsing, what type of chemical precipitate forms?",
            category = "Chemical Reactions & Precipitates",
            options = listOf(
                QuestionOption(
                    id = "q5_optA",
                    text = "A cloudy white salt precipitate of EDTA and CHX",
                    isCorrect = true,
                    explanation = "EDTA can exist in negatively charged forms, while chlorhexidine is a strongly basic cationic bisbiguanide. Direct mixing can cause an interaction that forms a cloudy precipitate, reducing the availability of the active agents."
                ),
                QuestionOption(
                    id = "q5_optB",
                    text = "Para-chloroaniline (PCA), a dark orange-brown cytotoxic compound",
                    isCorrect = false,
                    explanation = "PCA has historically been associated with the reaction between NaOCl and CHX. The orange-brown precipitate is associated with NaOCl and CHX, not EDTA and CHX. The precise chemical identity of precipitates formed by NaOCl and CHX has been debated in the literature."
                ),
                QuestionOption(
                    id = "q5_optC",
                    text = "Calcium hydroxide mineral crystal suspension",
                    isCorrect = false,
                    explanation = "Calcium hydroxide is a separate chemical compound used as an intracanal medicament. Neither EDTA nor CHX supplies hydroxyl ions in quantities that would generate a calcium hydroxide suspension through this interaction."
                ),
                QuestionOption(
                    id = "q5_optD",
                    text = "Insoluble calcium phosphate apatite crystals",
                    isCorrect = false,
                    explanation = "Calcium phosphate precipitation requires appropriate calcium and phosphate ions. The described interaction is between two organic chemical agents, not a direct calcium phosphate precipitation reaction."
                )
            )
        ),

        // Question 6: Calcium Hydroxide Neutralization of Endotoxins
        EndoQuestion(
            id = "endo_q06",
            title = "Calcium Hydroxide Neutralization of Endotoxins",
            questionText = "Through what specific biochemical mechanism does Calcium Hydroxide [Ca(OH)₂] inactivate bacterial Lipopolysaccharide (LPS) endotoxins?",
            category = "Intracanal Medicaments & Biochemistry",
            options = listOf(
                QuestionOption(
                    id = "q6_optA",
                    text = "Hydrolysis of the toxic Lipid A moiety of lipopolysaccharide",
                    isCorrect = true,
                    explanation = "LPS in the outer membrane of Gram-negative bacteria consists of a polysaccharide chain attached to a Lipid A core. The hydroxyl ions released by Ca(OH)₂ create a highly alkaline environment (approximately pH 12.5). This can hydrolyze ester bonds in Lipid A, altering its structure and reducing endotoxin activity."
                ),
                QuestionOption(
                    id = "q6_optB",
                    text = "Direct oxidation of extracellular polysaccharide matrix (EPS)",
                    isCorrect = false,
                    explanation = "Oxidation of microbial components is associated with oxidizing agents such as NaOCl and HOCl. Ca(OH)₂ acts primarily through high alkalinity, not direct oxidation."
                ),
                QuestionOption(
                    id = "q6_optC",
                    text = "Chelation of lipid membrane phosphate molecules",
                    isCorrect = false,
                    explanation = "Chelation involves binding metal ions, as occurs with EDTA. Ca(OH)₂ supplies calcium and hydroxyl ions; it is not a chelating agent."
                ),
                QuestionOption(
                    id = "q6_optD",
                    text = "Competitive inhibition of bacterial DNA gyrase enzymes",
                    isCorrect = false,
                    explanation = "DNA gyrase inhibition is a mechanism of certain antibiotics, including fluoroquinolones such as ciprofloxacin. Ca(OH)₂ acts through alkaline chemical disruption rather than specific DNA gyrase inhibition."
                )
            )
        ),

        // Question 7: Double Antibiotic Paste (DAP)
        EndoQuestion(
            id = "endo_q07",
            title = "Double Antibiotic Paste (DAP)",
            questionText = "Double Antibiotic Paste (DAP), used in regenerative endodontic procedures to avoid crown staining, consists of which two antibiotics?",
            category = "Regenerative Endodontics",
            options = listOf(
                QuestionOption(
                    id = "q7_optA",
                    text = "Ciprofloxacin and Metronidazole",
                    isCorrect = true,
                    explanation = "DAP is a modification of Triple Antibiotic Paste (TAP) used in regenerative endodontic procedures. Ciprofloxacin: A fluoroquinolone targeting Gram-negative aerobic and facultative bacteria. Metronidazole: A nitroimidazole targeting obligate anaerobic endodontic pathogens. Clinical Benefit: Omitting minocycline reduces the risk of coronal tooth staining while retaining antibacterial activity."
                ),
                QuestionOption(
                    id = "q7_optB",
                    text = "Minocycline and Amoxicillin",
                    isCorrect = false,
                    explanation = "Minocycline is associated with coronal tooth discoloration, which DAP is designed to avoid."
                ),
                QuestionOption(
                    id = "q7_optC",
                    text = "Clindamycin and Vancomycin",
                    isCorrect = false,
                    explanation = "These antibiotics are not the standard combination identified in the supplied DAP formulation."
                ),
                QuestionOption(
                    id = "q7_optD",
                    text = "Doxycycline and Augmentin",
                    isCorrect = false,
                    explanation = "Doxycycline is used in irrigants such as MTAD, while Augmentin is an oral systemic antibiotic combination. Neither defines the supplied DAP formulation."
                )
            )
        ),

        // Question 8: Calcium Hydroxide Action in Apexification
        EndoQuestion(
            id = "endo_q08",
            title = "Calcium Hydroxide Action in Apexification",
            questionText = "How does the high alkaline pH of Calcium Hydroxide [Ca(OH)₂] stimulate hard tissue barrier formation during apexification?",
            category = "Apexification & Periapical Healing",
            options = listOf(
                QuestionOption(
                    id = "q8_optA",
                    text = "By activating host alkaline phosphatase enzymes in periapical tissues",
                    isCorrect = true,
                    explanation = "The high pH and release of calcium ions from Ca(OH)₂ create an alkaline environment in periapical connective tissues. Biological Effect: Alkaline phosphatase helps release inorganic phosphate ions from organic phosphate esters. These phosphate ions can react with calcium to promote calcific barrier or hard-tissue bridge formation across the open apex."
                ),
                QuestionOption(
                    id = "q8_optB",
                    text = "By inducing complete ischemic necrosis of periapical stem cells",
                    isCorrect = false,
                    explanation = "Hard-tissue formation depends on viable cells capable of repair and differentiation. Complete ischemic necrosis would destroy regenerative capacity and impede barrier formation."
                ),
                QuestionOption(
                    id = "q8_optC",
                    text = "By suppressing host osteoblast and cementoblast differentiation",
                    isCorrect = false,
                    explanation = "Ca(OH)₂ is associated with conditions that can support hard-tissue formation; suppression of these cell populations is not the intended mechanism."
                ),
                QuestionOption(
                    id = "q8_optD",
                    text = "By lowering local tissue pH to induce acid-etch enamel bonding",
                    isCorrect = false,
                    explanation = "Acid etching uses acidic agents to condition enamel surfaces. Ca(OH)₂ raises local pH to strongly alkaline levels. Apexification involves tissues around an open root apex, not enamel bonding."
                )
            )
        ),

        // Question 9: Bleaching Agent to Minimize Cervical Resorption
        EndoQuestion(
            id = "endo_q09",
            title = "Bleaching Agent to Minimize Cervical Resorption",
            questionText = "Which intracanal bleaching agent, when mixed with distilled water, is preferred in the “Walking Bleach” technique to minimize cervical resorption?",
            category = "Non-Vital Bleaching & Tooth Discoloration",
            options = listOf(
                QuestionOption(
                    id = "q9_optA",
                    text = "Sodium perborate",
                    isCorrect = true,
                    explanation = "Sodium perborate mixed with distilled water or saline releases hydrogen peroxide gradually. Compared with high-concentration liquid hydrogen peroxide, this approach can reduce the risk of external cervical root resorption."
                ),
                QuestionOption(
                    id = "q9_optB",
                    text = "35% Hydrogen peroxide (H₂O₂)",
                    isCorrect = false,
                    explanation = "High-concentration hydrogen peroxide is a powerful bleaching agent. High concentrations can diffuse through dentinal tubules and increase the risk of external cervical root resorption, particularly under unfavorable clinical conditions."
                ),
                QuestionOption(
                    id = "q9_optC",
                    text = "Carbamide peroxide 10%",
                    isCorrect = false,
                    explanation = "Although it is a bleaching agent, sodium perborate mixed with water is the classic formulation described in the supplied question for internal non-vital bleaching."
                ),
                QuestionOption(
                    id = "q9_optD",
                    text = "17% Ethylenediaminetetraacetic acid (EDTA)",
                    isCorrect = false,
                    explanation = "EDTA chelates calcium and removes the inorganic portion of the smear layer. EDTA is used as a chelating irrigant, not as an intracanal bleaching agent."
                )
            )
        ),

        // Question 10: Needle Design to Prevent Apical Extrusion
        EndoQuestion(
            id = "endo_q10",
            title = "Needle Design to Prevent Apical Extrusion",
            questionText = "Which feature of an endodontic irrigation needle is specifically designed to minimize the risk of apical extrusion of irrigants?",
            category = "Clinical Irrigation Instruments",
            options = listOf(
                QuestionOption(
                    id = "q10_optA",
                    text = "Side-vented closed-ended safety tip",
                    isCorrect = true,
                    explanation = "Side-vented or double-side-vented needles have a closed, rounded apical tip and lateral discharge ports. Clinical Action: Irrigant exits sideways against the canal walls rather than directly through the apical opening. This can reduce apical fluid pressure and help minimize the risk of NaOCl extrusion."
                ),
                QuestionOption(
                    id = "q10_optB",
                    text = "Open-ended flat-beveled tip",
                    isCorrect = false,
                    explanation = "An open-ended needle directs fluid through its terminal opening. Directing fluid toward the apical foramen under pressure can increase extrusion risk."
                ),
                QuestionOption(
                    id = "q10_optC",
                    text = "Open-ended needle with a 45° bevel",
                    isCorrect = false,
                    explanation = "The bevel changes the shape and direction of the needle opening. It remains an open-ended design and can direct fluid toward the apical region under pressure."
                ),
                QuestionOption(
                    id = "q10_optD",
                    text = "Flexible plastic tip wedged tightly into the apical third",
                    isCorrect = false,
                    explanation = "Wedging the tip can obstruct the space needed for fluid to return coronally. A tightly wedged tip can create a pressure buildup and increase the risk of irrigant extrusion."
                )
            )
        ),

        // Question 11: Tissue Dissolution Capabilities
        EndoQuestion(
            id = "endo_q11",
            title = "Tissue Dissolution Capabilities",
            questionText = "Which of the following endodontic irrigants is uniquely capable of dissolving vital and necrotic organic pulp tissue remnants within the root canal system?",
            category = "Endodontic Pharmacology & Irrigants",
            options = listOf(
                QuestionOption(
                    id = "q11_optA",
                    text = "2.5% Sodium hypochlorite (NaOCl)",
                    isCorrect = true,
                    explanation = "NaOCl produces sodium hydroxide (NaOH) and hypochlorous acid (HOCl) in aqueous solution. NaOH contributes to saponification of fatty acids into soluble products, while HOCl participates in oxidation and chloramination reactions involving organic material. Clinical Action: NaOCl is the principal endodontic irrigant with clinically useful organic tissue-dissolving capability, including action on pulp remnants and organic components of the smear layer."
                ),
                QuestionOption(
                    id = "q11_optB",
                    text = "2% Chlorhexidine gluconate (CHX)",
                    isCorrect = false,
                    explanation = "CHX is a cationic bisbiguanide antimicrobial agent that disrupts bacterial cell membranes. CHX has antimicrobial activity but does not have meaningful pulp tissue-dissolving or saponification capability."
                ),
                QuestionOption(
                    id = "q11_optC",
                    text = "17% Ethylenediaminetetraacetic acid (EDTA)",
                    isCorrect = false,
                    explanation = "EDTA is a chelating agent that binds calcium ions from dentinal hydroxyapatite. It primarily removes inorganic mineral components and does not dissolve organic pulpal tissue."
                ),
                QuestionOption(
                    id = "q11_optD",
                    text = "Normal Saline (0.9% NaCl)",
                    isCorrect = false,
                    explanation = "Isotonic physiological solution used for mechanical flushing and lubrication. It does not possess the chemical tissue-dissolving capability of NaOCl."
                )
            )
        ),

        // Question 12: Mechanism of Action of EDTA
        EndoQuestion(
            id = "endo_q12",
            title = "Mechanism of Action of EDTA",
            questionText = "What is the primary mechanism of action of 17% Ethylenediaminetetraacetic acid (EDTA) during root canal chemomechanical preparation?",
            category = "Smear Layer & Chelation",
            options = listOf(
                QuestionOption(
                    id = "q12_optA",
                    text = "Chelation of calcium ions from hydroxyapatite",
                    isCorrect = true,
                    explanation = "EDTA forms stable, soluble complexes with calcium (Ca²⁺) ions in dentinal hydroxyapatite. Clinical Action: It demineralizes the superficial dentin and removes the inorganic debris of the smear layer, helping expose dentinal tubules."
                ),
                QuestionOption(
                    id = "q12_optB",
                    text = "Direct proteolytic degradation of collagen",
                    isCorrect = false,
                    explanation = "Proteolytic degradation requires enzymes such as collagenases or other suitable chemical processes. EDTA does not directly hydrolyze proteins or degrade the organic collagen matrix of dentin."
                ),
                QuestionOption(
                    id = "q12_optC",
                    text = "Oxidation of cellular sulfhydryl groups",
                    isCorrect = false,
                    explanation = "Oxidation of sulfhydryl groups is associated with oxidizing agents such as HOCl. EDTA is a chelating agent, not an oxidizing agent."
                ),
                QuestionOption(
                    id = "q12_optD",
                    text = "Saponification of fatty acids and lipopolysaccharides",
                    isCorrect = false,
                    explanation = "Saponification is promoted by strong alkaline conditions. EDTA primarily removes inorganic mineral components rather than dissolving lipids or organic tissue through saponification."
                )
            )
        ),

        // Question 13: Chemical Precipitate Formation
        EndoQuestion(
            id = "endo_q13",
            title = "Chemical Precipitate Formation (NaOCl + CHX)",
            questionText = "When sequential irrigation using Sodium Hypochlorite (NaOCl) and Chlorhexidine (CHX) is performed without an intermediate flush, an orange-brown chemical precipitate forms. What is this cytotoxic precipitate?",
            category = "Chemical Reactions & Precipitates",
            options = listOf(
                QuestionOption(
                    id = "q13_optA",
                    text = "Para-chloroaniline (PCA)",
                    isCorrect = true,
                    explanation = "The interaction between NaOCl and CHX can produce an orange-brown precipitate. Clinical Impact: The precipitate may stain dentin, obstruct dentinal tubules, and interfere with sealer adhesion. Its exact chemical composition, including the presence and amount of PCA, remains a subject of scientific debate."
                ),
                QuestionOption(
                    id = "q13_optB",
                    text = "Calcium phosphate precipitate",
                    isCorrect = false,
                    explanation = "Calcium phosphate precipitation occurs when suitable calcium and phosphate ions interact. It is not the characteristic orange-brown precipitate associated with NaOCl and CHX."
                ),
                QuestionOption(
                    id = "q13_optC",
                    text = "Chloramine-T complex",
                    isCorrect = false,
                    explanation = "Chloramines can form during reactions involving hypochlorous acid and nitrogen-containing compounds. This is not the specific precipitate named in the question."
                ),
                QuestionOption(
                    id = "q13_optD",
                    text = "Sodium hydroxide deposit",
                    isCorrect = false,
                    explanation = "Sodium hydroxide is present in NaOCl solutions and contributes to their alkalinity. NaOH is water-soluble and does not ordinarily form the characteristic orange-brown precipitate described."
                )
            )
        ),

        // Question 14: Antimicrobial Substantivity
        EndoQuestion(
            id = "endo_q14",
            title = "Antimicrobial Substantivity",
            questionText = "Which specific property allows Chlorhexidine gluconate to maintain prolonged antimicrobial activity within the root canal system for days to weeks after application?",
            category = "Endodontic Pharmacology & Irrigants",
            options = listOf(
                QuestionOption(
                    id = "q14_optA",
                    text = "Substantivity",
                    isCorrect = true,
                    explanation = "CHX is positively charged and can adsorb onto negatively charged hydroxyapatite and dentinal components. Clinical Action: Bound CHX can be released gradually as the surrounding concentration decreases, allowing antimicrobial activity to persist after application."
                ),
                QuestionOption(
                    id = "q14_optB",
                    text = "Effervescence",
                    isCorrect = false,
                    explanation = "Effervescence involves the rapid release of gas bubbles. Gas release can create short-term physical agitation but does not explain prolonged antimicrobial retention."
                ),
                QuestionOption(
                    id = "q14_optC",
                    text = "High pH dissociation",
                    isCorrect = false,
                    explanation = "High-pH antimicrobial action is characteristic of agents such as Ca(OH)₂. CHX does not rely on extreme alkalinity to provide its antimicrobial substantivity."
                ),
                QuestionOption(
                    id = "q14_optD",
                    text = "Saponification",
                    isCorrect = false,
                    explanation = "Saponification is the chemical conversion of fats into soap-like products under alkaline conditions. It is associated with tissue dissolution by NaOCl, not CHX binding to dentinal structures."
                )
            )
        ),

        // Question 15: Mechanism of Calcium Hydroxide
        EndoQuestion(
            id = "endo_q15",
            title = "Mechanism of Calcium Hydroxide",
            questionText = "What is the primary antimicrobial mechanism of Calcium Hydroxide [Ca(OH)₂] when used as an inter-appointment intracanal medicament?",
            category = "Intracanal Medicaments & Biochemistry",
            options = listOf(
                QuestionOption(
                    id = "q15_optA",
                    text = "Release of hydroxyl ions creating a high pH (≈ 12.5)",
                    isCorrect = true,
                    explanation = "Ca(OH)₂ dissociates into Ca²⁺ and OH⁻ ions in aqueous media. Biological Action: The high alkalinity damages bacterial membranes and proteins and can contribute to the inactivation of bacterial endotoxins."
                ),
                QuestionOption(
                    id = "q15_optB",
                    text = "Direct chemical reduction of dentinal collagen networks",
                    isCorrect = false,
                    explanation = "Reduction involves gaining electrons or removing oxygen from chemical structures. Ca(OH)₂ does not act primarily through chemical reduction of collagen."
                ),
                QuestionOption(
                    id = "q15_optC",
                    text = "Gas release leading to effervescent mechanical clearance",
                    isCorrect = false,
                    explanation = "Gas generation occurs with certain effervescent agents. Ca(OH)₂ paste does not function by releasing gas to mechanically flush the canal."
                ),
                QuestionOption(
                    id = "q15_optD",
                    text = "Chelation of divalent transition metal ions",
                    isCorrect = false,
                    explanation = "Metal-ion chelation is characteristic of agents such as EDTA. Ca(OH)₂ releases calcium and hydroxyl ions rather than acting as a chelating agent."
                )
            )
        ),

        // Question 16: Composition of Triple Antibiotic Paste (TAP)
        EndoQuestion(
            id = "endo_q16",
            title = "Composition of Triple Antibiotic Paste (TAP)",
            questionText = "Which combination of antibacterial agents constitutes traditional Triple Antibiotic Paste (TAP) used in regenerative endodontic procedures?",
            category = "Regenerative Endodontics",
            options = listOf(
                QuestionOption(
                    id = "q16_optA",
                    text = "Ciprofloxacin, Metronidazole, and Minocycline",
                    isCorrect = true,
                    explanation = "Traditional TAP uses three antibiotics, often described in equal proportions in the original formulation: Ciprofloxacin: A fluoroquinolone targeting Gram-negative aerobic and facultative bacteria. Metronidazole: A nitroimidazole targeting obligate anaerobic endodontic pathogens. Minocycline: A tetracycline derivative with antibacterial activity against susceptible Gram-positive organisms. Clinical Consideration: Minocycline can cause tooth discoloration, which has led to alternative formulations."
                ),
                QuestionOption(
                    id = "q16_optB",
                    text = "Amoxicillin, Clindamycin, and Vancomycin",
                    isCorrect = false,
                    explanation = "These are antibiotics used in other clinical contexts, but they do not constitute the traditional TAP formulation described in the question."
                ),
                QuestionOption(
                    id = "q16_optC",
                    text = "Penicillin G, Erythromycin, and Neomycin",
                    isCorrect = false,
                    explanation = "This combination is not the traditional three-antibiotic formulation used in the described regenerative endodontic protocol."
                ),
                QuestionOption(
                    id = "q16_optD",
                    text = "Doxycycline, Augmentin, and Metronidazole",
                    isCorrect = false,
                    explanation = "Doxycycline is associated with certain irrigant formulations, and Augmentin is amoxicillin/clavulanate. Neither combination represents traditional TAP."
                )
            )
        ),

        // Question 17: Tooth Discoloration from TAP
        EndoQuestion(
            id = "endo_q17",
            title = "Tooth Discoloration from TAP",
            questionText = "What is the main clinical drawback associated with the inclusion of Minocycline in traditional Triple Antibiotic Paste (TAP)?",
            category = "Regenerative Endodontics & Aesthetics",
            options = listOf(
                QuestionOption(
                    id = "q17_optA",
                    text = "Severe coronal tooth discoloration",
                    isCorrect = true,
                    explanation = "Minocycline can bind to calcium-containing dental structures and contribute to discoloration. Clinical Consequence: The tooth may develop gray or brown staining, particularly problematic in anterior teeth. Alternative formulations may omit minocycline or replace it with another antibiotic."
                ),
                QuestionOption(
                    id = "q17_optB",
                    text = "Rapid dissolution of dentin collagen matrix",
                    isCorrect = false,
                    explanation = "Degradation of dentin collagen requires appropriate enzymatic or chemical processes. Rapid collagen dissolution is not the defining clinical drawback of minocycline in TAP."
                ),
                QuestionOption(
                    id = "q17_optC",
                    text = "Systemic drug toxicity at standard intracanal doses",
                    isCorrect = false,
                    explanation = "Systemic toxicity depends on factors such as dose, absorption, and drug properties. The characteristic drawback specifically associated with minocycline in TAP is tooth discoloration, not systemic toxicity at standard intracanal doses."
                ),
                QuestionOption(
                    id = "q17_optD",
                    text = "Complete inhibition of stem cell proliferation",
                    isCorrect = false,
                    explanation = "High concentrations of intracanal antibiotics may adversely affect stem cells of the apical papilla. Although cytotoxicity is an important concentration-dependent consideration, tooth discoloration is the principal drawback specifically associated with minocycline."
                )
            )
        ),

        // Question 18: Resistant Endodontic Pathogen
        EndoQuestion(
            id = "endo_q18",
            title = "Resistant Endodontic Pathogen",
            questionText = "Which micro-organism is an opportunistic, Gram-positive facultative anaerobe frequently implicated in persistent endodontic infections and failed root canals, against which CHX and Ca(OH)₂ are directed?",
            category = "Endodontic Microbiology & Biofilms",
            options = listOf(
                QuestionOption(
                    id = "q18_optA",
                    text = "Enterococcus faecalis",
                    isCorrect = true,
                    explanation = "Pathology: It has frequently been isolated in secondary endodontic infections and retreatment cases. Survival Mechanisms: 1. Can invade deep into dentinal tubules. 2. Can maintain intracellular pH homeostasis through proton-pumping mechanisms, helping it tolerate alkaline stress. 3. Can form protective biofilms that contribute to persistence."
                ),
                QuestionOption(
                    id = "q18_optB",
                    text = "Lactobacillus acidophilus",
                    isCorrect = false,
                    explanation = "An acid-producing bacterium associated with the progression of dental caries. It is more closely associated with caries than with the classic persistent root canal pathogen described in the question."
                ),
                QuestionOption(
                    id = "q18_optC",
                    text = "Streptococcus mutans",
                    isCorrect = false,
                    explanation = "A major cariogenic organism associated with glucan production and acid formation. It is primarily associated with dental caries rather than being the classic organism implicated in post-treatment root canal failure."
                ),
                QuestionOption(
                    id = "q18_optD",
                    text = "Actinomyces israelii",
                    isCorrect = false,
                    explanation = "A filamentous bacterium associated with actinomycosis and some extra-radicular endodontic infections. It is not the classic intra-radicular facultative anaerobe identified in the question."
                )
            )
        ),

        // Question 19: Smear Layer Removal Protocol
        EndoQuestion(
            id = "endo_q19",
            title = "Smear Layer Removal Protocol",
            questionText = "To achieve effective removal of both organic and inorganic components of the smear layer, which irrigation protocol is clinically recommended?",
            category = "Smear Layer & Chelation",
            options = listOf(
                QuestionOption(
                    id = "q19_optA",
                    text = "Irrigation with 17% EDTA followed by a final rinse with NaOCl",
                    isCorrect = true,
                    explanation = "1. 17% EDTA: Chelates calcium and removes the inorganic mineral component of the smear layer. 2. NaOCl: Dissolves organic tissue remnants and organic components exposed during cleaning. Clinical Result: Sequential use can improve smear-layer removal and expose dentinal tubules. The duration and order of irrigation should follow an appropriate clinical protocol."
                ),
                QuestionOption(
                    id = "q19_optB",
                    text = "Sole reliance on distilled water to prevent dentin demineralization",
                    isCorrect = false,
                    explanation = "Distilled water provides mechanical flushing. It lacks the chelating and tissue-dissolving capabilities needed for effective chemical smear-layer removal."
                ),
                QuestionOption(
                    id = "q19_optC",
                    text = "2% Chlorhexidine rinse followed immediately by 17% EDTA",
                    isCorrect = false,
                    explanation = "CHX provides antimicrobial activity, while EDTA chelates calcium. Neither agent dissolves organic pulp fragments effectively, and this sequence does not provide the same complementary organic and inorganic tissue-dissolving actions. Irrigant compatibility must also be considered."
                ),
                QuestionOption(
                    id = "q19_optD",
                    text = "Continuous flushing with 5.25% NaOCl without any chelating agents",
                    isCorrect = false,
                    explanation = "NaOCl dissolves organic soft tissue and has antimicrobial activity. It does not effectively dissolve the inorganic mineral component of the smear layer; a chelating agent such as EDTA is used for that purpose."
                )
            )
        ),

        // Question 20: Active Bactericidal Moiety of Hypochlorite
        EndoQuestion(
            id = "endo_q20",
            title = "Active Bactericidal Moiety of Hypochlorite",
            questionText = "Which active chemical moiety formed during the dynamic equilibrium of Sodium Hypochlorite in water is primarily responsible for its potent bactericidal oxidation reactions?",
            category = "Endodontic Pharmacology & Irrigants",
            options = listOf(
                QuestionOption(
                    id = "q20_optA",
                    text = "Hypochlorous acid (HOCl)",
                    isCorrect = true,
                    explanation = "NaOCl in water participates in an equilibrium between hypochlorite species, including HOCl and OCl⁻. Action: HOCl is an uncharged molecule that can penetrate microbial cell envelopes and oxidize important cellular components, including enzyme sulfhydryl groups."
                ),
                QuestionOption(
                    id = "q20_optB",
                    text = "Sodium Hydroxide (NaOH)",
                    isCorrect = false,
                    explanation = "NaOH contributes to the high alkalinity of NaOCl solutions. Alkalinity supports tissue dissolution, but HOCl is a key active species responsible for rapid antimicrobial oxidation."
                ),
                QuestionOption(
                    id = "q20_optC",
                    text = "Hypochlorite ion (OCl⁻)",
                    isCorrect = false,
                    explanation = "OCl⁻ is the negatively charged form in the HOCl/OCl⁻ equilibrium and becomes more predominant at higher pH. The charged ion generally penetrates microbial cell envelopes less readily than uncharged HOCl. Its antimicrobial contribution is not zero, however."
                ),
                QuestionOption(
                    id = "q20_optD",
                    text = "Hydroxyl radical (OH•)",
                    isCorrect = false,
                    explanation = "Hydroxyl radicals are highly reactive species generated in certain oxidation processes. Free hydroxyl radicals are not the primary disinfectant species in ordinary aqueous NaOCl irrigation."
                )
            )
        )
    )

    data class RunQuestionSet(
        val round1RunnerQuestions: List<EndoQuestion>,
        val round2MazeQuestions: List<EndoQuestion>,
        val round3ZombieQuestions: List<EndoQuestion>
    ) {
        val allQuestions: List<EndoQuestion>
            get() = round1RunnerQuestions + round2MazeQuestions + round3ZombieQuestions
    }

    /**
     * Prepares a brand new, completely randomized question partition for a gameplay run.
     * 1. Shuffles the 20-question bank without replacement using Fisher-Yates / Kotlin shuffled.
     * 2. Selects 3 questions for Round 1 (Dental Police Inspections at 15s, 30s, 45s).
     * 3. Selects 3 questions for Round 2 (Three-Door Maze Gates 1, 2, 3).
     * 4. Selects 3 questions for Round 3 (Zombie Combat Challenges 1, 2, 3).
     * 5. Independently shuffles the 4 MCQ options for each selected question so that correct
     *    answers are distributed unpredictably across positions A, B, C, D without ever relying on option letters.
     */
    fun createRandomizedRunSet(): RunQuestionSet {
        val shuffledBank = masterBank.shuffled()
        val r1 = shuffledBank.subList(0, 3).map { it.shuffledInstance() }
        val r2 = shuffledBank.subList(3, 6).map { it.shuffledInstance() }
        val r3 = shuffledBank.subList(6, 9).map { it.shuffledInstance() }

        return RunQuestionSet(
            round1RunnerQuestions = r1,
            round2MazeQuestions = r2,
            round3ZombieQuestions = r3
        )
    }
}
