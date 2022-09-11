package cn.alvince.droidprism.sample.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.alvince.droidprism.app.asLogPage
import cn.alvince.droidprism.log.ExposureStateHelper
import cn.alvince.droidprism.sample.R
import cn.alvince.droidprism.sample.databinding.TraceListFragmentBinding
import cn.alvince.droidprism.sample.trace.SampleInvisibleTrace
import cn.alvince.droidprism.sample.trace.SampleTrace
import cn.alvince.droidprism.util.exposeWith
import cn.alvince.droidprism.util.getTraceHelper
import cn.alvince.zanpakuto.core.dimens.dp

/**
 * Create by bytedance on 2022/9/10
 *
 * @author zhangyang.alvince@bytedance.com
 */
class TraceListFragment : Fragment() {

    private val defaultModeInvisible: Boolean get() = arguments?.getBoolean("__arg_expose_when_invisible", false) == true

    private lateinit var listAdapter: ListAdapter

    private var _binding: TraceListFragmentBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listAdapter = ListAdapter(object : DiffUtil.ItemCallback<ListItem>() {
            override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean = oldItem === newItem

            override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean = oldItem == newItem
        }, asLogPage().exposureStateHelper)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        TraceListFragmentBinding.inflate(inflater, container, false)
            .also {
                _binding = it
                it.initView()
            }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyListItem(!defaultModeInvisible)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun TraceListFragmentBinding.initView() {
        switchExposureMode.apply {
            isChecked = defaultModeInvisible
            setOnCheckedChangeListener { _, isChecked ->
                tvModeExplain.text = getString(if (isChecked) R.string.trace_list_exposure_mode_invisible else R.string.trace_list_exposure_mode_normal)
                applyListItem(!isChecked)
            }
        }
        rvList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = listAdapter
        }
    }

    private fun applyListItem(normalOrInvisible: Boolean) {
        listAdapter.submitList(arrayListOf<ListItem>().apply {
            for (i in 0 until 100) {
                add(ListItem("list item $i", i.toString(), !normalOrInvisible))
            }
        })
    }

    class ListAdapter(callback: DiffUtil.ItemCallback<ListItem>, private val exposureStateHelper: ExposureStateHelper) :
        androidx.recyclerview.widget.ListAdapter<ListItem, ListViewHolder>(callback) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
            return ListViewHolder(AppCompatTextView(parent.context).apply {
                textSize = 18F
                val (horiz, vertical) = 16.dp.toPxInt(context) to 12.dp.toPxInt(context)
                setPaddingRelative(horiz, vertical, horiz, vertical)
                exposeWith(exposureStateHelper, null)
            })
        }

        override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
            getItem(position)?.also { item ->
                holder.view.apply {
                    text = item.content
                    getTraceHelper().trace = item.trace
                }
            }
        }
    }

    class ListViewHolder(val view: TextView) : RecyclerView.ViewHolder(view)

    class ListItem(val content: String, val id: Any, val traceWhenInvisible: Boolean) {

        val trace: SampleTrace = if (traceWhenInvisible) SampleInvisibleTrace("item_$id") else SampleTrace("item_$id")

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ListItem) return false

            if (content != other.content) return false
            if (id != other.id) return false
            if (traceWhenInvisible != other.traceWhenInvisible) return false

            return true
        }

        override fun hashCode(): Int {
            var result = content.hashCode()
            result = 31 * result + id.hashCode()
            result = 31 * result + traceWhenInvisible.hashCode()
            return result
        }
    }
}
