package cn.alvince.droidprism.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cn.alvince.droidprism.app.asLogPage
import cn.alvince.droidprism.sample.databinding.FragmentSecondBinding
import cn.alvince.droidprism.sample.trace.SampleTrace
import cn.alvince.droidprism.util.traceExpose

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        binding.buttonSecond.traceExpose(asLogPage(), SampleTrace("second_button"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
