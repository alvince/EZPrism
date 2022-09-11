package cn.alvince.droidprism.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cn.alvince.droidprism.app.asLogPage
import cn.alvince.droidprism.sample.databinding.MainFragmentBinding
import cn.alvince.droidprism.sample.trace.SampleTrace
import cn.alvince.droidprism.util.traceExpose

/**
 * Create by bytedance on 2022/9/11
 *
 * @author zhangyang.alvince@bytedance.com
 */
class MainFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        MainFragmentBinding.inflate(inflater, container, false)
            .also {
                it.initView()
            }
            .root

    private fun MainFragmentBinding.initView() {
        btnSimpleCase.apply {
            setOnClickListener {
                findNavController().navigate(R.id.action_home_to_simple_case)
            }
            traceExpose(asLogPage(), SampleTrace("simple_case_button"))
        }
        btnListCase.apply {
            setOnClickListener {
                findNavController().navigate(R.id.action_home_to_list_case)
            }
            traceExpose(asLogPage(), SampleTrace("list_case_button"))
        }
        btnListInvisibleMode.apply {
            setOnClickListener {
                findNavController().navigate(R.id.action_home_to_list_case, bundleOf("__arg_expose_when_invisible" to true))
            }
            traceExpose(asLogPage(), SampleTrace("list_mode_invisible_button"))
        }
    }
}
