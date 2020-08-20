package app.milanherke.mystudiez

import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer

abstract class BaseViewHolder<T>(override val containerView: View) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {

    abstract fun bind(data: T)

    protected fun setMargin(newMargin: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            newMargin,
            containerView.context.resources.displayMetrics
        ).toInt()
    }

}