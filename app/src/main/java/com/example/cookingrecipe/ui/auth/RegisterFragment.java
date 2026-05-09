package com.example.cookingrecipe.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.cookingrecipe.R;
import com.example.cookingrecipe.data.model.AuthResponse;
import com.example.cookingrecipe.data.model.RegisterRequest;
import com.example.cookingrecipe.data.network.ApiClient;
import com.example.cookingrecipe.data.storage.SessionManager;
import com.example.cookingrecipe.databinding.FragmentRegisterBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(binding.registerScroll, (v, windowInsets) -> {
            Insets navBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(0, 0, 0, navBars.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(binding.registerScroll);

        binding.registerButton.setOnClickListener(v -> handleRegister());
        binding.loginLink.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
    }

    private void handleRegister() {
        String name = binding.nameInput.getText().toString().trim();
        String email = binding.emailInput.getText().toString().trim();
        String username = binding.usernameInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.registerButton.setEnabled(false);
        ApiClient.getApiService().register(new RegisterRequest(name, email, username, password))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                        binding.registerButton.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null && response.body().token != null) {
                            SessionManager.getInstance().saveSession(response.body().user, response.body().token);
                            Toast.makeText(requireContext(), "Account created", Toast.LENGTH_SHORT).show();
                            NavController nav = NavHostFragment.findNavController(RegisterFragment.this);
                            if (!nav.popBackStack(R.id.loginFragment, true)) {
                                nav.navigate(R.id.homeFragment);
                            }
                        } else {
                            Toast.makeText(requireContext(), "Registration failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                        binding.registerButton.setEnabled(true);
                        Toast.makeText(requireContext(), "Registration failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
