package app.milanherke.mystudiez

import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.recyclerview.widget.RecyclerView

class Animations {

    companion object {

        const val DEFAULT_TRANSITION_TIME = 50L
        const val DEFAULT_SHARED_ELEMENT_TRANSITION_TIME = 50L

        fun runLayoutAnimation(recycler: RecyclerView) {
            val animationResId = R.anim.recycler_layout_animation
            val animation: LayoutAnimationController =
                AnimationUtils.loadLayoutAnimation(recycler.context, animationResId)
            recycler.layoutAnimation = animation
        }
    }

}