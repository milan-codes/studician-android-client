package app.milanherke.mystudiez

import android.content.Context
import java.util.*

/**
 * A simple internal cast extending [ArrayDeque] to store certain fragments
 * This class is used to determine which fragment should be loaded when the up button is pressed in certain fragments
 */
@Suppress("UNUSED_PARAMETER")
internal class FragmentsStack private constructor(context: Context) : ArrayDeque<Fragments>() {

    companion object : SingletonHolder<FragmentsStack, Context>(::FragmentsStack)

}