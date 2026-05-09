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
import com.example.cookingrecipe.data.model.LoginRequest;
import com.example.cookingrecipe.data.network.ApiClient;
import com.example.cookingrecipe.data.storage.SessionManager;
import com.example.cookingrecipe.databinding.FragmentLoginBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(binding.loginScroll, (v, windowInsets) -> {
            Insets navBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(0, 0, 0, navBars.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(binding.loginScroll);

        binding.loginButton.setOnClickListener(v -> handleLogin());
        binding.registerLink.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.registerFragment)
        );
    }

    private void handleLogin() {
        String username = binding.usernameInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.loginButton.setEnabled(false);
        ApiClient.getApiService().login(new LoginRequest(username, password))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                        binding.loginButton.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null && response.body().token != null) {
                            SessionManager.getInstance().saveSession(response.body().user, response.body().token);
                            Toast.makeText(requireContext(), "Welcome back", Toast.LENGTH_SHORT).show();
                            NavController nav = NavHostFragment.findNavController(LoginFragment.this);
                            if (!nav.popBackStack()) {
                                nav.navigate(R.id.homeFragment);
                            }
                        } else {
                            Toast.makeText(requireContext(), "Login failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                        binding.loginButton.setEnabled(true);
                        Toast.makeText(requireContext(), "Login failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
