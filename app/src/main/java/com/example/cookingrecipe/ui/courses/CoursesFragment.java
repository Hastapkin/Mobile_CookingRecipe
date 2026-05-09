package com.example.cookingrecipe.ui.courses;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingrecipe.R;
import com.example.cookingrecipe.data.model.AddToCartResponse;
import com.example.cookingrecipe.data.model.CoursesOverviewResponse;
import com.example.cookingrecipe.data.model.CourseOverview;
import com.example.cookingrecipe.data.model.PurchasesResponse;
import com.example.cookingrecipe.data.network.ApiClient;
import com.example.cookingrecipe.data.storage.SessionManager;
import com.example.cookingrecipe.databinding.FragmentCoursesBinding;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CoursesFragment extends Fragment implements CourseCardAdapter.CourseActionListener {

    private FragmentCoursesBinding binding;
    private CourseCardAdapter adapter;
    private final List<CourseOverview> courses = new ArrayList<>();
    private final Set<Integer> ownedCourseIds = new HashSet<>();

    private int currentPage = 1;
    private int totalPages = 1;
    private String currentSort = "newest";
    private String currentSearch = "";
    private boolean loading = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCoursesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new CourseCardAdapter(courses, ownedCourseIds, this);
        binding.coursesList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.coursesList.setHasFixedSize(false);
        binding.coursesList.setNestedScrollingEnabled(false);
        binding.coursesList.setItemViewCacheSize(10);
        binding.coursesList.setAdapter(adapter);

        binding.swipeRefresh.setOnChildScrollUpCallback((parent, child) ->
                child instanceof NestedScrollView && ((NestedScrollView) child).getScrollY() > 0);

        binding.searchInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) return;
            final View scroll = binding.coursesScroll;
            final View swipe = binding.swipeRefresh;
            final View root = binding.getRoot();
            scroll.post(() -> {
                if (binding == null || !isAdded()) return;
                scroll.scrollTo(0, 0);
                swipe.setTranslationY(0f);
                scroll.setTranslationY(0f);
                root.setTranslationY(0f);
            });
        });

        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.sort_options,
                android.R.layout.simple_spinner_item
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.sortSpinner.setAdapter(sortAdapter);

        binding.sortSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> {
            switch (position) {
                case 1:
                    currentSort = "popular";
                    break;
                case 2:
                    currentSort = "rating";
                    break;
                case 3:
                    currentSort = "price";
                    break;
                case 0:
                default:
                    currentSort = "newest";
                    break;
            }
            reloadCourses();
        }));

        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s.toString().trim();
            }

            @Override
            public void afterTextChanged(Editable s) {
                reloadCourses();
            }
        });

        binding.loadMoreButton.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                loadCourses(currentPage + 1, false);
            }
        });

        binding.swipeRefresh.setOnRefreshListener(this::reloadCourses);

        loadOwnedCourses();
        loadCourses(1, true);
    }

    private void reloadCourses() {
        loadCourses(1, true);
    }

    private void loadOwnedCourses() {
        if (!SessionManager.getInstance().isAuthenticated()) {
            ownedCourseIds.clear();
            adapter.notifyDataSetChanged();
            return;
        }

        ApiClient.getApiService().getPurchasedCourseIds().enqueue(new Callback<PurchasesResponse>() {
            @Override
            public void onResponse(@NonNull Call<PurchasesResponse> call, @NonNull Response<PurchasesResponse> response) {
                if (binding == null || !isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    ownedCourseIds.clear();
                    if (response.body().data.courseIds != null) {
                        ownedCourseIds.addAll(response.body().data.courseIds);
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PurchasesResponse> call, @NonNull Throwable t) {
            }
        });
    }

    private void loadCourses(int page, boolean reset) {
        if (binding == null || !isAdded()) return;
        if (loading) return;
        loading = true;
        if (reset) {
            currentPage = 1;
            totalPages = 1;
            courses.clear();
            adapter.notifyDataSetChanged();
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getApiService().getCourses(currentSearch, currentSort, page, 9)
                .enqueue(new Callback<CoursesOverviewResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CoursesOverviewResponse> call, @NonNull Response<CoursesOverviewResponse> response) {
                        if (binding == null || !isAdded()) {
                            loading = false;
                            return;
                        }
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        loading = false;
                        if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                            List<CourseOverview> incoming = response.body().data.courses;
                            if (incoming != null) {
                                courses.addAll(incoming);
                            }
                            if (response.body().data.pagination != null) {
                                currentPage = response.body().data.pagination.page;
                                totalPages = response.body().data.pagination.totalPages;
                            }
                            adapter.notifyDataSetChanged();
                            binding.coursesList.requestLayout();
                            updateLoadMoreVisibility();
                        } else {
                            Toast.makeText(requireContext(), "Failed to load courses", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CoursesOverviewResponse> call, @NonNull Throwable t) {
                        if (binding == null || !isAdded()) {
                            loading = false;
                            return;
                        }
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        loading = false;
                        Toast.makeText(requireContext(), "Failed to load courses", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateLoadMoreVisibility() {
        if (binding == null) return;
        binding.loadMoreButton.setVisibility(currentPage < totalPages ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCourseSelected(int courseId) {
        Bundle args = new Bundle();
        args.putInt("courseId", courseId);
        NavHostFragment.findNavController(this)
                .navigate(R.id.courseDetailFragment, args);
    }

    @Override
    public void onAddToCart(int courseId) {
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
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(requireContext(), response.body().alreadyInCart ? "Already in cart" : "Added to cart", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
