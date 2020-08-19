package app.milanherke.mystudiez

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

private const val IS_EMPTY = 0
private const val IS_NOT_EMPTY = 1

abstract class BaseAdapter<T>(
    private var dataList: ArrayList<T>
) : RecyclerView.Adapter<BaseViewHolder<T>>() {

    abstract fun setViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T>

    fun swapDataList(dataList: ArrayList<T>) {
        this.dataList = dataList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T> {
        return setViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int = dataList.size

    override fun getItemViewType(position: Int): Int = if (dataList.size == 0) IS_EMPTY else IS_NOT_EMPTY

}