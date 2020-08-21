package app.milanherke.mystudiez

import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer

/**
 * An abstract [RecyclerView.ViewHolder] subclass, which implements [LayoutContainer].
 * This class serves as a Base ViewHolder for other ViewHolders.
 * Other ViewHolders must extend this class.
 */
abstract class BaseViewHolder<T>(override val containerView: View) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {

    /**
     * Subclasses of this class must implement this function, which is called in [BaseAdapter].
     * This method should update the contents of the ViewHolder.
     *
     * @param data Binded data
     */
    abstract fun bind(data: T)

    /**
     * Returns the final floating point value of a given value, in integer.
     * This method must be only used to set new margins, programmatically.
     */
    protected fun setMargin(newMargin: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            newMargin,
            containerView.context.resources.displayMetrics
        ).toInt()
    }

}