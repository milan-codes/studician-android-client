package app.milanherke.mystudiez

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * An abstract, [RecyclerView.Adapter] subclass.
 * This class serves as a Base Adapter for other RecyclerViewAdapters.
 * Other RecyclerViewAdapters must extend this class.
 */
abstract class BaseAdapter<T>(
    private var dataList: ArrayList<T>
) : RecyclerView.Adapter<BaseViewHolder<T>>() {

    /**
     * Subclasses of this class must implement this function,
     * which is basically just a wrapper function for [onCreateViewHolder].
     *
     * @param parent The [ViewGroup] into which the new View will be added after it is bound to an adapter position.
     */
    abstract fun setViewHolder(parent: ViewGroup): BaseViewHolder<T>

    /**
     * Swaps in a new ArrayList and notifies observers
     * about the change of the data set.
     *
     * @param dataList New [ArrayList] that has type [T]
     */
    protected fun swapDataList(dataList: ArrayList<T>) {
        this.dataList = dataList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T> {
        return setViewHolder(parent)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int = dataList.size

}