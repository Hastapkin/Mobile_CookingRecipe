package com.example.cookingrecipe.ui.courses;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.cookingrecipe.R;
import com.example.cookingrecipe.data.model.AddToCartResponse;
import com.example.cookingrecipe.data.model.BasicResponse;
import com.example.cookingrecipe.data.model.CourseOverviewDetail;
import com.example.cookingrecipe.data.model.CourseOverviewDetailResponse;
import com.example.cookingrecipe.data.model.CourseReviewsResponse;
import com.example.cookingrecipe.data.model.PurchasesResponse;
import com.example.cookingrecipe.data.model.SaveReviewRequest;
import com.example.cookingrecipe.data.network.ApiClient;
import com.example.cookingrecipe.data.storage.SessionManager;
import com.example.cookingrecipe.databinding.FragmentCourseDetailBinding;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseDetailFragment extends Fragment {

    private FragmentCourseDetailBinding binding;
    private int courseId;
    private CourseOverviewDetail detail;
    private boolean ownsCourse = false;
    private boolean hasReview = false;

    private ModuleAdapter moduleAdapter;
    private ReviewAdapter reviewAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCourseDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        courseId = getArguments() != null ? getArguments().getInt("courseId", 0) : 0;
        moduleAdapter = new ModuleAdapter();
        reviewAdapter = new ReviewAdapter();

        binding.modulesList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.modulesList.setAdapter(moduleAdapter);

        binding.reviewsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.reviewsList.setAdapter(reviewAdapter);

        binding.addToCartButton.setOnClickListener(v -> handleAddToCart());
        binding.viewCartButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.cartFragment)
        );
        binding.startLearningButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("courseId", courseId);
            NavHostFragment.findNavController(this).navigate(R.id.courseLearnFragment, args);
        });
        binding.saveReviewButton.setOnClickListener(v -> handleSaveReview());
        binding.deleteReviewButton.setOnClickListener(v -> handleDeleteReview());

        loadCourseDetail();
        loadOwnership();
        loadReviews();
    }

    private void loadCourseDetail() {
        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getApiService().getCourseDetail(courseId).enqueue(new Callback<CourseOverviewDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<CourseOverviewDetailResponse> call, @NonNull Response<CourseOverviewDetailResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    detail = response.body().data;
                    bindDetail();
                } else {
                    Toast.makeText(requireContext(), "Course not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CourseOverviewDetailResponse> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Failed to load course", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOwnership() {
        if (!SessionManager.getInstance().isAuthenticated()) {
            ownsCourse = false;
            updateOwnershipUi();
            return;
        }

        ApiClient.getApiService().getPurchasedCourseIds().enqueue(new Callback<PurchasesResponse>() {
            @Override
            public void onResponse(@NonNull Call<PurchasesResponse> call, @NonNull Response<PurchasesResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    ownsCourse = response.body().data.courseIds != null && response.body().data.courseIds.contains(courseId);
                    updateOwnershipUi();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PurchasesResponse> call, @NonNull Throwable t) {
                updateOwnershipUi();
            }
        });
    }

    private void loadReviews() {
        ApiClient.getApiService().getCourseReviews(courseId).enqueue(new Callback<CourseReviewsResponse>() {
            @Override
            public void onResponse(@NonNull Call<CourseReviewsResponse> call, @NonNull Response<CourseReviewsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    reviewAdapter.setReviews(response.body().data.reviews);
                    if (response.body().data.myReview != null) {
                        binding.reviewComment.setText(response.body().data.myReview.comment);
                        int stars = response.body().data.myReview.rating;
                        binding.reviewRatingBar.setRating(Math.max(1f, Math.min(5f, stars)));
                        binding.deleteReviewButton.setVisibility(View.VISIBLE);
                        hasReview = true;
                    } else {
                        binding.reviewRatingBar.setRating(5f);
                        binding.deleteReviewButton.setVisibility(View.GONE);
                        hasReview = false;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<CourseReviewsResponse> call, @NonNull Throwable t) {
            }
        });
    }

    private void bindDetail() {
        if (detail == null || detail.course == null) return;
        binding.courseTitle.setText(detail.course.title);
        binding.courseSubtitle.setText(detail.course.description != null ? detail.course.description : "No description provided.");
        binding.courseMeta.setText(buildMeta());
        binding.courseRating.setText(String.format(Locale.getDefault(), "%.1f", detail.course.rating != null ? detail.course.rating : 0));
        binding.coursePrice.setText(NumberFormat.getCurrencyInstance(Locale.US).format(detail.course.price != null ? detail.course.price : 0));

        Glide.with(binding.courseImage)
                .load(detail.course.thumbnail)
                .placeholder(R.drawable.placeholder_image)
                .into(binding.courseImage);

        moduleAdapter.setModules(detail.modules);
    }

    private String buildMeta() {
        String duration = detail.course.duration == null || detail.course.duration <= 0
                ? "Self-paced" : detail.course.duration + "m";
        int modules = detail.course.moduleCount != null ? detail.course.moduleCount : 0;
        return duration + " • " + modules + " modules";
    }

    private void updateOwnershipUi() {
        binding.startLearningButton.setVisibility(ownsCourse ? View.VISIBLE : View.GONE);
        binding.addToCartButton.setVisibility(ownsCourse ? View.GONE : View.VISIBLE);
        binding.viewCartButton.setVisibility(ownsCourse ? View.GONE : View.VISIBLE);
    }

    private void handleAddToCart() {
        if (!SessionManager.getInstance().isAuthenticated()) {
            NavHostFragment.findNavController(this).navigate(R.id.loginFragment);
            return;
        }

        try {
            JSONObject payload = new JSONObject();
            payload.put("courseId", courseId);
            RequestBody body = RequestBody.create(payload.toString(), MediaType.parse("application/json"));
            ApiClient.getApiService().addToCart(body).enqueue(new Callback<AddToCartResponse>() {
                @Override
                public void onResponse(@NonNull Call<AddToCartResponse> call, @NonNull Response<AddToCartResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Added to cart", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to add to cart", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<AddToCartResponse> call, @NonNull Throwable t) {
                    Toast.makeText(requireContext(), "Failed to add to cart", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Failed to add to cart", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSaveReview() {
        if (!SessionManager.getInstance().isAuthenticated()) {
            NavHostFragment.findNavController(this).navigate(R.id.loginFragment);
            return;
        }

        int rating = (int) binding.reviewRatingBar.getRating();
        if (rating < 1) {
            Toast.makeText(requireContext(), "Please choose a star rating", Toast.LENGTH_SHORT).show();
            return;
        }
        String comment = binding.reviewComment.getText().toString().trim();
        SaveReviewRequest request = new SaveReviewRequest(rating, comment);

        if (hasReview) {
            ApiClient.getApiService().updateCourseReview(courseId, request).enqueue(new Callback<BasicResponse>() {
                @Override
                public void onResponse(@NonNull Call<BasicResponse> call, @NonNull Response<BasicResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Review updated", Toast.LENGTH_SHORT).show();
                        loadReviews();
                    } else {
                        Toast.makeText(requireContext(), "Failed to update review", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<BasicResponse> call, @NonNull Throwable t) {
                    Toast.makeText(requireContext(), "Failed to update review", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        ApiClient.getApiService().saveCourseReview(courseId, request).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(@NonNull Call<BasicResponse> call, @NonNull Response<BasicResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Review saved", Toast.LENGTH_SHORT).show();
                    loadReviews();
                } else {
                    Toast.makeText(requireContext(), "Failed to save review", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BasicResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Failed to save review", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleDeleteReview() {
        ApiClient.getApiService().deleteCourseReview(courseId).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(@NonNull Call<BasicResponse> call, @NonNull Response<BasicResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Review deleted", Toast.LENGTH_SHORT).show();
                    binding.reviewComment.setText("");
                    binding.reviewRatingBar.setRating(5f);
                    loadReviews();
                } else {
                    Toast.makeText(requireContext(), "Failed to delete review", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BasicResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Failed to delete review", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
