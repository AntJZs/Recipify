package com.progweb.recipify.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.progweb.recipify.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
// Este es el Inicio, tiene los placeholders en el XML todavía.
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup your views here if needed
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}