package com.olehel.cameraview.ui

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.olehel.cameraview.R
import com.olehel.cameraview.databinding.FragmentUserIdBinding
import com.olehel.cameraview.onKeyboardDoneClick
import com.olehel.cameraview.textChanged

class UserIdFragment : Fragment(R.layout.fragment_user_id) {
    private var binding: FragmentUserIdBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserIdBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onResume() {
        super.onResume()
        binding?.userIdInput?.requestFocus()
        val imm: InputMethodManager =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding?.userIdInput, 0)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.userIdInput?.textChanged {
            if (it) {
                binding?.userIdConfirm?.isEnabled = true
                binding?.userIdConfirm?.isClickable = true
            } else {
                binding?.userIdConfirm?.isEnabled = false
                binding?.userIdConfirm?.isClickable = false
            }
        }
        binding?.userIdInput?.onKeyboardDoneClick {
            findNavController().navigate(R.id.action_userIdFragment_to_cameraFragment)
        }
        binding?.userIdConfirm?.setOnClickListener {
            findNavController().navigate(R.id.action_userIdFragment_to_cameraFragment)
        }
    }
}