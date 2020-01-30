package app.milanherke.mystudiez

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

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

fun FragmentActivity.addFragment(fragment: Fragment, frameId: Int) {
    supportFragmentManager.inTransaction { add(frameId, fragment) }
}

fun FragmentActivity.replaceFragment(fragment: Fragment, frameId: Int) {
    supportFragmentManager.inTransaction { replace(frameId, fragment) }
}

fun FragmentActivity.removeFragment(fragment: Fragment) {
    supportFragmentManager.inTransaction { remove(fragment) }
}
//////////////////////////////////////////////////

fun Drawable.displayColor(@ColorRes colorRes: Int, context: Context) {
    DrawableCompat.setTint(
        DrawableCompat.wrap(this),
        ContextCompat.getColor(context, colorRes)
    )
}

fun Drawable.mutatedClone(): Drawable {
    return this.constantState!!.newDrawable().mutate()
}