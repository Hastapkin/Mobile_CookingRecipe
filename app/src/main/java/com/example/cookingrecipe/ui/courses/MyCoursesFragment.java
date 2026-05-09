package com.example.cookingrecipe.ui.courses;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cookingrecipe.R;
import com.example.cookingrecipe.data.model.CourseOverview;
import com.example.cookingrecipe.data.model.CoursesOverviewResponse;
import com.example.cookingrecipe.data.model.PurchasesResponse;
import com.example.cookingrecipe.data.network.ApiClient;
import com.example.cookingrecipe.data.storage.SessionManager;
import com.example.cookingrecipe.databinding.FragmentMyCoursesBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyCoursesFragment extends Fragment implements MyCoursesAdapter.MyCourseListener {

    private FragmentMyCoursesBinding binding;
    private MyCoursesAdapter adapter;
    private final List<CourseOverview> allCourses = new ArrayList<>();
    private final List<CourseOverview> filtered = new ArrayList<>();
    private final Set<Integer> owned = new HashSet<>();
    private String currentSort = "newest";
    private String currentSearch = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyCoursesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new MyCoursesAdapter(filtered, this);
        binding.coursesList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.coursesList.setAdapter(adapter);

        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.my_courses_sort_options, android.R.layout.simple_spinner_item
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.sortSpinner.setAdapter(sortAdapter);
        binding.sortSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> {
            switch (position) {
                case 1:
                    currentSort = "rating";
                    break;
                case 2:
                    currentSort = "popular";
                    break;
                case 3:
                    currentSort = "price";
                    break;
                case 0:
                default:
                    currentSort = "newest";
                    break;
            }
            applyFilters();
        }));

        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s.toString().trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.loginButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.loginFragment));

        binding.searchInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) return;
            final NestedScrollView scroll = binding.authenticatedGroup;
            if (scroll.getVisibility() != View.VISIBLE) return;
            scroll.post(() -> {
                if (binding == null || !isAdded()) return;
                scroll.scrollTo(0, 0);
                scroll.setTranslationY(0f);
            });
        });

        bindAuthStateUi();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding == null) return;
        boolean guestPromptVisible = binding.loginPrompt.getVisibility() == View.VISIBLE;
        bindAuthStateUi();
        if (SessionManager.getInstance().isAuthenticated()) {
            if (guestPromptVisible || allCourses.isEmpty()) {
                loadPurchasedCourses();
            }
        }
    }

    private void bindAuthStateUi() {
        if (binding == null) return;
        if (!SessionManager.getInstance().isAuthenticated()) {
            binding.authenticatedGroup.setVisibility(View.GONE);
            binding.loginPrompt.setVisibility(View.VISIBLE);
            allCourses.clear();
            filtered.clear();
            if (adapter != null) adapter.notifyDataSetChanged();
            binding.progressBar.setVisibility(View.GONE);
        } else {
            binding.loginPrompt.setVisibility(View.GONE);
            binding.authenticatedGroup.setVisibility(View.VISIBLE);
        }
    }

    private void loadPurchasedCourses() {
        if (!SessionManager.getInstance().isAuthenticated()) return;

        binding.progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService().getPurchasedCourseIds().enqueue(new Callback<PurchasesResponse>() {
            @Override
            public void onResponse(@NonNull Call<PurchasesResponse> call, @NonNull Response<PurchasesResponse> response) {
                if (binding == null || !isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    owned.clear();
                    if (response.body().data.courseIds != null) {
                        owned.addAll(response.body().data.courseIds);
                    }
                    loadAllCourses();
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PurchasesResponse> call, @NonNull Throwable t) {
                if (binding == null || !isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void loadAllCourses() {
        ApiClient.getApiService().getCourses(null, "newest", 1, 200)
                .enqueue(new Callback<CoursesOverviewResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CoursesOverviewResponse> call, @NonNull Response<CoursesOverviewResponse> response) {
                        if (binding == null || !isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                            allCourses.clear();
                            if (response.body().data.courses != null) {
                                for (CourseOverview course : response.body().data.courses) {
                                    if (owned.contains(course.id)) {
                                        allCourses.add(course);
                                    }
                                }
                            }
                            applyFilters();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CoursesOverviewResponse> call, @NonNull Throwable t) {
                        if (binding == null || !isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void applyFilters() {
        if (binding == null || !isAdded()) return;
        filtered.clear();
        for (CourseOverview course : allCourses) {
            if (currentSearch.isEmpty() || (course.title != null && course.title.toLowerCase().contains(currentSearch.toLowerCase()))) {
                filtered.add(course);
            }
        }
        filtered.sort((a, b) -> {
            switch (currentSort) {
                case "price":
                    return Double.compare(a.price, b.price);
                case "rating":
                    return Double.compare(b.rating, a.rating);
                case "popular":
                    return Integer.compare(b.moduleCount, a.moduleCount);
                case "newest":
                default:
                    return b.createdAt != null && a.createdAt != null ? b.createdAt.compareTo(a.createdAt) : 0;
            }
        });
        adapter.notifyDataSetChanged();
        binding.emptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCourseSelected(int courseId) {
        Bundle args = new Bundle();
        args.putInt("courseId", courseId);
        NavHostFragment.findNavController(this).navigate(R.id.courseLearnFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
