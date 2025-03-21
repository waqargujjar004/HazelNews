//import android.view.View
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.AsyncListDiffer
//import androidx.recyclerview.widget.RecyclerView
//
//abstract class BaseAdapter<T : Any>(
//    diffCallback: DiffUtil.ItemCallback<T>
//) : RecyclerView.Adapter<BaseAdapter<T>.BaseViewHolder<T>>() {
//
//    val differ = AsyncListDiffer(this, diffCallback)
//
//    abstract inner class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        abstract fun bind(item: T)
//    }
//
//    override fun getItemCount(): Int = differ.currentList.size
//
//    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
//        val item = differ.currentList[position]
//        holder.bind(item)
//    }
//
//    // Click listener setup
//    private var onItemClickListener: ((T) -> Unit)? = null
//
//    fun setOnItemClickListener(listener: (T) -> Unit) {
//        onItemClickListener = listener
//    }
//
//    protected fun invokeClickListener(item: T) {
//        onItemClickListener?.invoke(item)
//    }
//}
