package com.life4.imagetotext.feature.result

import androidx.navigation.fragment.navArgs
import com.life4.imagetotext.R
import com.life4.imagetotext.base.BaseFragment
import com.life4.imagetotext.databinding.FragmentResultBinding

class ResultFragment : BaseFragment<FragmentResultBinding>(R.layout.fragment_result) {
    private val args: ResultFragmentArgs by navArgs()

    override fun onCreateView() {
        binding.etResult.setText(args.textResult)
    }

}