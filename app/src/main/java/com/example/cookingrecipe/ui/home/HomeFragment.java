package com.example.cookingrecipe.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.cookingrecipe.R;
import com.example.cookingrecipe.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.heroCta.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.coursesFragment)
        );

        binding.heroCart.setOnClickListener(v ->
            NavHostFragment.findNavController(this).navigate(R.id.cartFragment)
        );

        binding.heroOrders.setOnClickListener(v ->
            NavHostFragment.findNavController(this).navigate(R.id.myOrdersFragment)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
