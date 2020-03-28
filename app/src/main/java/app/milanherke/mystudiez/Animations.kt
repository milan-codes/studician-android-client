package app.milanherke.mystudiez

import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.recyclerview.widget.RecyclerView
import app.milanherke.mystudiez.Animations.Companion.DEFAULT_SHARED_ELEMENT_TRANSITION_TIME
import app.milanherke.mystudiez.Animations.Companion.DEFAULT_TRANSITION_TIME
import app.milanherke.mystudiez.Animations.Companion.runLayoutAnimation

/**
 * Simple class that holds the following in a companion object:
 * [DEFAULT_TRANSITION_TIME]
 * [DEFAULT_SHARED_ELEMENT_TRANSITION_TIME]
 * And a function to animate RecyclerViews ([runLayoutAnimation]).
 */
class Animations {

    companion object {

        const val DEFAULT_TRANSITION_TIME = 50L
        const val DEFAULT_SHARED_ELEMENT_TRANSITION_TIME = 50L

        /**
         * This function takes in a recycler view as a parameter
         * and applies an animation ([R.anim.recycler_layout_animation]) to it.
         *
         * @param recycler The recycler view that we want to animate
         */
        fun runLayoutAnimation(recycler: RecyclerView) {
            val animationResId = R.anim.recycler_layout_animation
            val animation: LayoutAnimationController =
                AnimationUtils.loadLayoutAnimation(recycler.context, animationResId)
            recycler.layoutAnimation = animation
        }
    }

}