package com.example.cookingrecipe.ui.profile;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.cookingrecipe.R;
import com.example.cookingrecipe.data.model.ProfileResponse;
import com.example.cookingrecipe.data.model.UploadProfilePictureResponse;
import com.example.cookingrecipe.data.model.UpdateProfileRequest;
import com.example.cookingrecipe.data.network.ApiClient;
import com.example.cookingrecipe.data.storage.SessionManager;
import com.example.cookingrecipe.databinding.FragmentProfileBinding;
import com.example.cookingrecipe.util.FileUtils;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null && binding != null) {
                    selectedImageUri = uri;
                    Glide.with(binding.profileImage)
                            .load(uri)
                            .placeholder(R.drawable.placeholder_image)
                            .into(binding.profileImage);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.loginButton.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.loginFragment));
        binding.logoutButton.setOnClickListener(v -> {
            SessionManager.getInstance().clearSession();
            bindGuest();
        });
        binding.saveProfileButton.setOnClickListener(v -> updateProfile());
        binding.uploadPhotoButton.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        binding.aboutButton.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.aboutFragment));
        binding.contactButton.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.contactFragment));

        ScrollView scrollHost = (ScrollView) binding.getRoot();
        View.OnFocusChangeListener collapseAfterIme = (v, hasFocus) -> {
            if (hasFocus) return;
            scrollHost.post(() -> {
                if (binding == null || !isAdded()) return;
                scrollHost.scrollTo(0, 0);
                scrollHost.setTranslationY(0f);
            });
        };
        binding.nameInput.setOnFocusChangeListener(collapseAfterIme);
        binding.emailInput.setOnFocusChangeListener(collapseAfterIme);

        if (SessionManager.getInstance().isAuthenticated()) {
            loadProfile();
        } else {
            bindGuest();
        }
    }

    private void bindGuest() {
        binding.authenticatedGroup.setVisibility(View.GONE);
        binding.guestGroup.setVisibility(View.VISIBLE);
    }

    private void loadProfile() {
        binding.guestGroup.setVisibility(View.GONE);
        binding.authenticatedGroup.setVisibility(View.VISIBLE);

        ApiClient.getApiService().getProfile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileResponse> call, @NonNull Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().user != null) {
                    binding.nameInput.setText(response.body().user.name);
                    binding.emailInput.setText(response.body().user.email);
                    Glide.with(binding.profileImage)
                            .load(response.body().user.profilePicture)
                            .placeholder(R.drawable.placeholder_image)
                            .into(binding.profileImage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileResponse> call, @NonNull Throwable t) {
            }
        });
    }

    private void updateProfile() {
        String name = binding.nameInput.getText().toString().trim();
        String email = binding.emailInput.getText().toString().trim();

        ApiClient.getApiService().updateProfile(new UpdateProfileRequest(name, email))
                .enqueue(new Callback<ProfileResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ProfileResponse> call, @NonNull Response<ProfileResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to update", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ProfileResponse> call, @NonNull Throwable t) {
                        Toast.makeText(requireContext(), "Failed to update", Toast.LENGTH_SHORT).show();
                    }
                });

        if (selectedImageUri != null) {
            uploadProfilePicture();
        }
    }

    private void uploadProfilePicture() {
        try {
            File imageFile = FileUtils.copyToTempFile(requireContext(), selectedImageUri, "profile");
            RequestBody fileBody = RequestBody.create(imageFile, MediaType.parse("image/*"));
            MultipartBody.Part part = MultipartBody.Part.createFormData("image", imageFile.getName(), fileBody);

            ApiClient.getApiService().uploadProfilePicture(part)
                    .enqueue(new Callback<UploadProfilePictureResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<UploadProfilePictureResponse> call, @NonNull Response<UploadProfilePictureResponse> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                                Glide.with(binding.profileImage)
                                        .load(response.body().data.imageUrl)
                                        .placeholder(R.drawable.placeholder_image)
                                        .into(binding.profileImage);
                                Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<UploadProfilePictureResponse> call, @NonNull Throwable t) {
                            Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
