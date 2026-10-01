package com.example.endoquest.navigation

sealed class Screen(val route: String) {
    object MainMenu : Screen("main_menu")
    object Story : Screen("story")
    object Instructions : Screen("instructions")
    object About : Screen("about")
    object Runner : Screen("runner")
    object Maze : Screen("maze")
    object ZombieChallenge : Screen("zombie_challenge")
    object Quiz : Screen("quiz")
    object RctIntro : Screen("rct_intro")
    object RctStage : Screen("rct_stage")
    object Results : Screen("results")
    object GameOver : Screen("game_over")
    object RoundOver : Screen("round_over")
}
