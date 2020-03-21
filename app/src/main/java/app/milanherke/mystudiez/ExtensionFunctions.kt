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

/**
 * Find a fragment by its ID.
 * Shorter way of supportFragmentManager.findFragmentById
 *
 * @param id Fragment's ID
 * @return Fragment, or null if it couldn't find anything
 */
fun FragmentActivity.findFragmentById(id: Int): Fragment? {
    return supportFragmentManager.findFragmentById(id)
}

/**
 * Opens a new fragment with transition.
 *
 * @param fragment Fragment which we want to open
 * @param frameId Fragment container
 * @param enter Enter transition
 * @param exit Exit transition
 * @param sharedElementId Resource ID of the shared element, null if there is none
 */
fun FragmentActivity.replaceFragmentWithTransition(
    fragment: Fragment,
    frameId: Int,
    enter: Transition? = null,
    exit: Transition? = null,
    @IdRes sharedElementId: Int? = null
) {
    this.toolbar.transitionName = "toolbarTransition"
    val exitTransition = exit ?: Fade()
    val enterTransition = enter ?: Fade()
    val sharedElementTransitionType = sharedElementId ?: android.R.transition.move
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

/**
 * Sets the color of a drawable.
 *
 * @param colorRes Resource of the color which we want to apply on the drawable
 * @param context Context is needed to apply the color
 */
fun Drawable.setColor(@ColorRes colorRes: Int, context: Context) {
    DrawableCompat.setTint(
        DrawableCompat.wrap(this),
        ContextCompat.getColor(context, colorRes)
    )
}

/**
 * Creates a clone of a drawable.
 * Does not affect the other instances
 * of the drawable this function was called on.
 *
 * @return Clone of the original drawable
 */
fun Drawable.mutatedClone(): Drawable {
    return this.constantState!!.newDrawable().mutate()
}

/**
 * Extension functions from Dinesh Babuhunky
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
