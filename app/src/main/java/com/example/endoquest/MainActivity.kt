package com.example.endoquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.endoquest.navigation.Screen
import com.example.endoquest.ui.screens.*
import com.example.endoquest.ui.theme.EndoQuestTheme
import com.example.endoquest.ui.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EndoQuestTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val gameViewModel: GameViewModel = viewModel()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.MainMenu.route
                    ) {
                        composable(Screen.MainMenu.route) {
                            MainMenuScreen(
                                onStartGame = { navController.navigate(Screen.Story.route) },
                                onInstructions = { navController.navigate(Screen.Instructions.route) },
                                onAbout = { navController.navigate(Screen.About.route) }
                            )
                        }

                        composable(Screen.Story.route) {
                            StoryScreen(
                                onProceedToRunner = { navController.navigate(Screen.Runner.route) }
                            )
                        }

                        composable(Screen.Instructions.route) {
                            InstructionsScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.About.route) {
                            AboutScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // ROUND 1: RUNNER + DENTAL POLICE
                        composable(Screen.Runner.route) {
                            RunnerScreen(
                                viewModel = gameViewModel,
                                onProceedToMaze = {
                                    navController.navigate(Screen.Maze.route) {
                                        popUpTo(Screen.Runner.route) { inclusive = true }
                                    }
                                },
                                onRoundOver = { navController.navigate(Screen.RoundOver.route) },
                                onGameOver = { navController.navigate(Screen.GameOver.route) }
                            )
                        }

                        // ROUND 2: DENTAL MAZE
                        composable(Screen.Maze.route) {
                            MazeScreen(
                                viewModel = gameViewModel,
                                onProceedToZombie = {
                                    navController.navigate(Screen.ZombieChallenge.route) {
                                        popUpTo(Screen.Maze.route) { inclusive = true }
                                    }
                                },
                                onRoundOver = { navController.navigate(Screen.RoundOver.route) }
                            )
                        }

                        // ROUND 3: ZOMBIE DENTAL CHALLENGE
                        composable(Screen.ZombieChallenge.route) {
                            ZombieScreen(
                                viewModel = gameViewModel,
                                onProceedToClinic = {
                                    navController.navigate(Screen.RctIntro.route) {
                                        popUpTo(Screen.ZombieChallenge.route) { inclusive = true }
                                    }
                                },
                                onRoundOver = { navController.navigate(Screen.RoundOver.route) }
                            )
                        }

                        composable(Screen.Quiz.route) {
                            QuizScreen(
                                viewModel = gameViewModel,
                                onQuizFinished = { navController.popBackStack() }
                            )
                        }

                        // RETURN TO CLINIC & RCT SIMULATION
                        composable(Screen.RctIntro.route) {
                            RctIntroScreen(
                                viewModel = gameViewModel,
                                onStartRctStages = { navController.navigate(Screen.RctStage.route) }
                            )
                        }

                        composable(Screen.RctStage.route) {
                            RctStageScreen(
                                viewModel = gameViewModel,
                                onCompleteAllStages = { navController.navigate(Screen.Results.route) }
                            )
                        }

                        composable(Screen.Results.route) {
                            ResultsScreen(
                                viewModel = gameViewModel,
                                onPlayAgain = {
                                    navController.navigate(Screen.MainMenu.route) {
                                        popUpTo(Screen.MainMenu.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Screen.GameOver.route) {
                            GameOverScreen(
                                viewModel = gameViewModel,
                                onRetry = {
                                    navController.popBackStack(Screen.Runner.route, false)
                                },
                                onReturnToMenu = {
                                    navController.navigate(Screen.MainMenu.route) {
                                        popUpTo(Screen.MainMenu.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Screen.RoundOver.route) {
                            RoundOverScreen(
                                viewModel = gameViewModel,
                                onRestartRound = {
                                    val target = gameViewModel.roundOverInfo.targetRoute
                                    navController.navigate(target) {
                                        popUpTo(Screen.RoundOver.route) { inclusive = true }
                                    }
                                },
                                onReturnToMenu = {
                                    navController.navigate(Screen.MainMenu.route) {
                                        popUpTo(Screen.MainMenu.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
