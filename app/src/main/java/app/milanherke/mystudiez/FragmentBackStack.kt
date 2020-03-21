package app.milanherke.mystudiez

import android.content.Context
import java.util.*

/**
 * An internal class extending [ArrayDeque]
 * creating a fragment back stack.
 */
@Suppress("UNUSED_PARAMETER")
internal class FragmentBackStack private constructor(context: Context) : ArrayDeque<Fragments>() {

    companion object : SingletonHolder<FragmentBackStack, Context>(::FragmentBackStack)

}