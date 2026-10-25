package com.bl4ckswordsman.cerberustiles.navbar

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.navigation.NavBackStackEntry

/**
 * Material 3 motion curves, durations, and transition specifications
 * for the application's navigation system.
 */
object NavTransitions {

    val M3EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val M3EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

    // Damped easing with initial resistance to counteract Android's steep BackEvent curve,
    // keeping screen movement natural and proportional to the user's thumb distance
    val PredictiveBackEasing = CubicBezierEasing(0.35f, 0.0f, 0.5f, 1.0f)

    const val DURATION_SLIDE_ENTER = 380
    const val DURATION_SLIDE_EXIT = 300
    const val DURATION_FADE_ENTER = 240
    const val DURATION_FADE_EXIT = 150
    const val DURATION_TAB_ENTER = 180
    const val DURATION_TAB_EXIT = 120

    /**
     * Checks if the given route is a hierarchical sub-screen (Licenses or Permissions).
     */
    fun isSubScreen(route: String?): Boolean =
        route == Screen.Licenses.route || route == Screen.Permissions.route

    /**
     * Snappy fade-in for top-level tab destinations.
     */
    fun tabEnterTransition(): EnterTransition =
        fadeIn(animationSpec = tween(DURATION_TAB_ENTER, easing = FastOutSlowInEasing))

    /**
     * Fast fade-out for top-level tab destinations.
     */
    fun tabExitTransition(): ExitTransition =
        fadeOut(animationSpec = tween(DURATION_TAB_EXIT, easing = FastOutLinearInEasing))

    /**
     * Slide-in from right with subtle fade for forward sub-screen navigation.
     */
    fun AnimatedContentTransitionScope<NavBackStackEntry>.subScreenEnterTransition(): EnterTransition =
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(DURATION_SLIDE_ENTER, easing = M3EmphasizedDecelerate)
        ) + fadeIn(animationSpec = tween(DURATION_FADE_ENTER, delayMillis = 40))

    /**
     * Parallax slide-out to left with fade when a sub-screen pushes over another screen.
     */
    fun AnimatedContentTransitionScope<NavBackStackEntry>.subScreenExitTransition(): ExitTransition =
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            targetOffset = { it / 4 },
            animationSpec = tween(DURATION_SLIDE_EXIT, easing = M3EmphasizedDecelerate)
        ) + fadeOut(animationSpec = tween(DURATION_FADE_EXIT))

    /**
     * Re-entry slide from left parallax with fade when returning from a sub-screen.
     */
    fun AnimatedContentTransitionScope<NavBackStackEntry>.subScreenPopEnterTransition(): EnterTransition =
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            initialOffset = { it / 4 },
            animationSpec = tween(DURATION_SLIDE_ENTER, easing = M3EmphasizedDecelerate)
        ) + fadeIn(animationSpec = tween(DURATION_FADE_ENTER))

    /**
     * Slide-out to right with fade when popping a sub-screen off the backstack.
     */
    fun AnimatedContentTransitionScope<NavBackStackEntry>.subScreenPopExitTransition(): ExitTransition =
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(DURATION_SLIDE_EXIT, easing = M3EmphasizedAccelerate)
        ) + fadeOut(animationSpec = tween(DURATION_FADE_EXIT))

    /**
     * Exit transition for the Settings screen: applies parallax slide if drilling into a sub-screen,
     * or a fast fade if switching peer tabs.
     */
    fun AnimatedContentTransitionScope<NavBackStackEntry>.settingsExitTransition(): ExitTransition {
        return if (isSubScreen(targetState.destination.route)) {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                targetOffset = { it / 4 },
                animationSpec = tween(DURATION_SLIDE_ENTER, easing = M3EmphasizedDecelerate)
            ) + fadeOut(animationSpec = tween(DURATION_FADE_EXIT))
        } else {
            tabExitTransition()
        }
    }

    /**
     * Pop enter transition for the Settings screen: applies parallax glide if returning from a sub-screen,
     * or a snappy tab fade if returning to Settings tab.
     */
    fun AnimatedContentTransitionScope<NavBackStackEntry>.settingsPopEnterTransition(): EnterTransition {
        return if (isSubScreen(initialState.destination.route)) {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                initialOffset = { it / 4 },
                animationSpec = tween(DURATION_SLIDE_EXIT, easing = M3EmphasizedDecelerate)
            ) + fadeIn(animationSpec = tween(DURATION_FADE_ENTER, delayMillis = 40))
        } else {
            tabEnterTransition()
        }
    }

    /**
     * Predictive back gesture enter transition: parallax reveal from left with fade.
     */
    fun AnimatedContentTransitionScope<NavBackStackEntry>.predictiveBackEnterTransition(): EnterTransition =
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            initialOffset = { it / 4 },
            animationSpec = tween(400, easing = PredictiveBackEasing)
        ) + fadeIn(animationSpec = tween(400, easing = PredictiveBackEasing))

    /**
     * Predictive back gesture exit transition: calibrated horizontal slide to right following finger swipe.
     */
    fun AnimatedContentTransitionScope<NavBackStackEntry>.predictiveBackExitTransition(): ExitTransition =
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(400, easing = PredictiveBackEasing)
        )

    /**
     * Animated transition specification for the TopAppBar title.
     */
    fun AnimatedContentTransitionScope<Screen>.topBarTitleTransition(): ContentTransform {
        val isTargetSubScreen = targetState is Screen.Licenses || targetState is Screen.Permissions
        val isInitialSubScreen = initialState is Screen.Licenses || initialState is Screen.Permissions

        return if (isTargetSubScreen && !isInitialSubScreen) {
            (slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(DURATION_SLIDE_ENTER, easing = M3EmphasizedDecelerate)
            ) + fadeIn(animationSpec = tween(DURATION_FADE_ENTER))).togetherWith(
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    targetOffset = { it / 3 },
                    animationSpec = tween(DURATION_SLIDE_ENTER, easing = M3EmphasizedDecelerate)
                ) + fadeOut(animationSpec = tween(DURATION_FADE_EXIT))
            )
        } else if (!isTargetSubScreen && isInitialSubScreen) {
            (slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                initialOffset = { it / 3 },
                animationSpec = tween(DURATION_SLIDE_EXIT, easing = M3EmphasizedDecelerate)
            ) + fadeIn(animationSpec = tween(DURATION_FADE_ENTER))).togetherWith(
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(DURATION_SLIDE_EXIT, easing = M3EmphasizedAccelerate)
                ) + fadeOut(animationSpec = tween(DURATION_FADE_EXIT))
            )
        } else {
            fadeIn(animationSpec = tween(DURATION_TAB_ENTER)).togetherWith(
                fadeOut(animationSpec = tween(DURATION_TAB_EXIT))
            )
        }
    }

    /**
     * Enter transition for the TopAppBar back navigation icon.
     */
    fun topBarIconEnterTransition(): EnterTransition =
        fadeIn(animationSpec = tween(150)) + scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(200, easing = M3EmphasizedDecelerate)
        )

    /**
     * Exit transition for the TopAppBar back navigation icon.
     */
    fun topBarIconExitTransition(): ExitTransition =
        fadeOut(animationSpec = tween(100)) + scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(150)
        )
}
