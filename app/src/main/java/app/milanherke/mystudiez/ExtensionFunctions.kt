package app.milanherke.mystudiez

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionSet
import kotlinx.android.synthetic.main.activity_main.*

fun FragmentActivity.findFragmentById(id: Int): Fragment? {
    return supportFragmentManager.findFragmentById(id)
}

//////////////////////////////////////////////////
/**
 * Extension function from Dinesh Babuhunky
 */

inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().func().commit()
}

fun FragmentActivity.replaceFragment(fragment: Fragment, frameId: Int) {
    supportFragmentManager.inTransaction { replace(frameId, fragment) }
}

fun FragmentActivity.removeFragment(fragment: Fragment) {
    supportFragmentManager.inTransaction { remove(fragment) }
}
//////////////////////////////////////////////////

fun FragmentActivity.replaceFragmentWithTransition(
    fragment: Fragment,
    frameId: Int,
    exit: Transition? = null,
    enter: Transition? = null,
    @IdRes sharedElementTransitionId: Int? = null
) {
    this.toolbar.transitionName = "toolbarTransition"
    val exitTransition = exit ?: Fade()
    val enterTransition = enter ?: Fade()
    val sharedElementTransitionType = sharedElementTransitionId ?: android.R.transition.move
    val previousFragment: Fragment? =
        supportFragmentManager.findFragmentById(R.id.fragment_container)
    val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()

    exitTransition.duration = Animations.DEFAULT_TRANSITION_TIME
    previousFragment!!.exitTransition = exitTransition

    val enterTransitionSet = TransitionSet()
    enterTransitionSet.addTransition(
        TransitionInflater.from(this).inflateTransition(
            sharedElementTransitionType
        )
    )
    enterTransitionSet.duration = Animations.DEFAULT_SHARED_ELEMENT_TRANSITION_TIME
    fragment.sharedElementEnterTransition = enterTransitionSet

    enterTransition.startDelay =
        Animations.DEFAULT_SHARED_ELEMENT_TRANSITION_TIME + Animations.DEFAULT_TRANSITION_TIME
    enterTransition.duration = Animations.DEFAULT_TRANSITION_TIME
    fragment.enterTransition = enterTransition

    fragmentTransaction.addSharedElement(this.toolbar, this.toolbar.transitionName)
    fragmentTransaction.replace(frameId, fragment)
    fragmentTransaction.commitAllowingStateLoss()
}

fun Drawable.displayColor(@ColorRes colorRes: Int, context: Context) {
    DrawableCompat.setTint(
        DrawableCompat.wrap(this),
        ContextCompat.getColor(context, colorRes)
    )
}

fun Drawable.mutatedClone(): Drawable {
    return this.constantState!!.newDrawable().mutate()
}