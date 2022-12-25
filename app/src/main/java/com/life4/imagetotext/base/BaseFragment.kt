package com.life4.imagetotext.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<T : ViewDataBinding>(private val layoutResId: Int) :
    Fragment() {
    lateinit var binding: T

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<T>(layoutInflater, layoutResId, container, false)
        binding.apply {
            this.lifecycleOwner = viewLifecycleOwner
        }
        onCreateView()
        return binding.root
    }

    open fun onCreateView(){}
    open fun onCreatedView(){}
}