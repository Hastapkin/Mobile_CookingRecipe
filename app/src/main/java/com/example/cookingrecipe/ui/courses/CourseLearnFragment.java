package com.example.cookingrecipe.ui.courses;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.DownloadManager;
import android.content.Context;
import android.os.Environment;
import android.text.Html;
import android.util.TypedValue;
import android.widget.CompoundButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingrecipe.R;
import com.example.cookingrecipe.data.model.AssignmentQuestion;
import com.example.cookingrecipe.data.model.AssignmentSubmitResponse;
import com.example.cookingrecipe.data.model.CourseLearningDetail;
import com.example.cookingrecipe.data.model.CourseLearningDetailResponse;
import com.example.cookingrecipe.data.model.CourseLearningLesson;
import com.example.cookingrecipe.data.model.CourseLearningModule;
import com.example.cookingrecipe.data.network.ApiClient;
import com.example.cookingrecipe.data.storage.SessionManager;
import com.example.cookingrecipe.databinding.FragmentCourseLearnBinding;
import com.google.android.material.radiobutton.MaterialRadioButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseLearnFragment extends Fragment implements CourseLearnAdapter.LessonListener {

    private FragmentCourseLearnBinding binding;
    private int courseId;
    private CourseLearningDetail detail;
    private CourseLearningLesson selectedLesson;
    private CourseLearnAdapter adapter;
    private final Map<String, Integer> answers = new HashMap<>();
    private boolean lessonsExpanded = true;

    private final CompoundButton.OnCheckedChangeListener markCompleteListener = (buttonView, isChecked) -> {
        if (selectedLesson != null) {
            updateProgress(selectedLesson, isChecked);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCourseLearnBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        courseId = getArguments() != null ? getArguments().getInt("courseId", 0) : 0;

        RecyclerView recycler = binding.lessonList;
        recycler.setNestedScrollingEnabled(false);
        recycler.setHasFixedSize(false);
        recycler.setOverScrollMode(View.OVER_SCROLL_NEVER);

        adapter = new CourseLearnAdapter(this);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        binding.lessonDropdownHeader.setOnClickListener(v -> setLessonsExpanded(!lessonsExpanded));
        setLessonsExpanded(true);

        binding.markComplete.setOnCheckedChangeListener(markCompleteListener);

        binding.downloadCertificate.setOnClickListener(v -> downloadCertificate());

        binding.submitAssignment.setOnClickListener(v -> submitAssignment());

        loadLearningDetail();
    }

    private void loadLearningDetail() {
        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getApiService().getCourseLearning(courseId).enqueue(new Callback<CourseLearningDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<CourseLearningDetailResponse> call, @NonNull Response<CourseLearningDetailResponse> response) {
                if (binding == null || !isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    detail = response.body().data;
                    adapter.setModules(detail.modules);
                    binding.lessonDropdownCount.setText(formatLessonCount(detail));
                    if (detail.modules != null && !detail.modules.isEmpty()) {
                        for (CourseLearningLesson lesson : detail.modules.get(0).lessons) {
                            selectLesson(lesson);
                            break;
                        }
                    }
                    updateProgressHeader();
                } else {
                    Toast.makeText(requireContext(), "Course access locked", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CourseLearningDetailResponse> call, @NonNull Throwable t) {
                if (binding == null || !isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Failed to load course", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Nullable
    private CourseLearningLesson findLessonById(int lessonId) {
        if (detail == null || detail.modules == null) {
            return null;
        }
        for (CourseLearningModule module : detail.modules) {
            if (module.lessons == null) {
                continue;
            }
            for (CourseLearningLesson lesson : module.lessons) {
                if (lesson.id == lessonId) {
                    return lesson;
                }
            }
        }
        return null;
    }

    private void applyLearningDetailFromServer(@Nullable CourseLearningDetail newDetail) {
        if (binding == null || !isAdded() || newDetail == null) {
            return;
        }
        detail = newDetail;
        adapter.setModules(detail.modules);
        binding.lessonDropdownCount.setText(formatLessonCount(detail));
        updateProgressHeader();
        if (selectedLesson != null) {
            CourseLearningLesson refreshed = findLessonById(selectedLesson.id);
            if (refreshed != null) {
                selectLesson(refreshed);
            }
        }
    }

    private void updateProgressHeader() {
        if (detail == null || detail.progress == null) return;
        binding.courseTitle.setText(detail.course.title);
        binding.courseProgress.setText(detail.progress.completedLessons + "/" + detail.progress.totalLessons + " lessons");
        binding.progressPercent.setText(String.format("%.0f%%", detail.progress.percent));
        binding.downloadCertificate.setVisibility(detail.progress.percent >= 95 ? View.VISIBLE : View.GONE);
    }

    private void setLessonsExpanded(boolean expanded) {
        lessonsExpanded = expanded;
        binding.lessonDropdownContent.setVisibility(expanded ? View.VISIBLE : View.GONE);
        binding.lessonDropdownIcon.setImageResource(expanded
                ? R.drawable.ic_expand_less_24
                : R.drawable.ic_expand_more_24);
    }

    private String formatLessonCount(@Nullable CourseLearningDetail detail) {
        if (detail == null || detail.modules == null) {
            return "0 lessons";
        }
        int count = 0;
        for (int i = 0; i < detail.modules.size(); i++) {
            if (detail.modules.get(i).lessons != null) {
                count += detail.modules.get(i).lessons.size();
            }
        }
        return count + (count == 1 ? " lesson" : " lessons");
    }

    private void downloadCertificate() {
        if (detail == null) return;
        String token = SessionManager.getInstance().getToken();
        if (token == null) {
            Toast.makeText(requireContext(), "Please log in", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = getString(com.example.cookingrecipe.R.string.api_base_url) + "/courses/" + detail.course.id + "/certificate";
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.addRequestHeader("Authorization", "Bearer " + token);
        request.setTitle("Course Certificate");
        request.setDescription("Downloading certificate");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                "course_certificate_" + detail.course.id + ".pdf");

        DownloadManager manager = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
        Toast.makeText(requireContext(), "Downloading certificate", Toast.LENGTH_SHORT).show();
    }

    private void selectLesson(CourseLearningLesson lesson) {
        selectedLesson = lesson;
        adapter.setSelectedLessonId(lesson.id);
        binding.lessonTitle.setText(lesson.title);
        binding.lessonDescription.setText(lesson.description != null ? lesson.description : "No description.");
        binding.markComplete.setOnCheckedChangeListener(null);
        binding.markComplete.setChecked(lesson.isCompleted);
        binding.markComplete.setOnCheckedChangeListener(markCompleteListener);
        renderLessonContent(lesson);
    }

    private void renderLessonContent(CourseLearningLesson lesson) {
        binding.articleContent.setVisibility(View.GONE);
        binding.openVideoButton.setVisibility(View.GONE);
        binding.assignmentContainer.setVisibility(View.GONE);
        binding.submitAssignment.setVisibility(View.GONE);
        binding.markComplete.setVisibility(View.GONE);

        binding.openVideoButton.setOnClickListener(null);

        String type = lesson.contentType != null ? lesson.contentType.toLowerCase() : "";
        if ("article".equals(type)) {
            binding.articleContent.setVisibility(View.VISIBLE);
            String html = lesson.content != null && lesson.content.articleText != null
                    ? lesson.content.articleText
                    : "<p>No article content available.</p>";
            binding.articleContent.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));
            binding.markComplete.setVisibility(View.VISIBLE);
        } else if ("assignment".equals(type)) {
            binding.articleContent.setVisibility(View.GONE);
            binding.assignmentContainer.setVisibility(View.VISIBLE);
            binding.markComplete.setVisibility(View.GONE);
            buildAssignmentQuestions(lesson);
            if (lesson.content != null && lesson.content.assignmentQuestions != null
                    && lesson.content.assignmentQuestions.length > 0) {
                binding.submitAssignment.setVisibility(View.VISIBLE);
            }
        } else {
            binding.articleContent.setVisibility(View.VISIBLE);
            binding.articleContent.setText(getString(R.string.video_lesson_intro));
            String url = lesson.content != null ? lesson.content.videoUrl : null;
            binding.openVideoButton.setVisibility(View.VISIBLE);
            binding.openVideoButton.setOnClickListener(v -> openLessonVideo(url));
            binding.markComplete.setVisibility(View.VISIBLE);
        }
    }

    private void openLessonVideo(@Nullable String url) {
        if (url == null || url.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Video link is missing", Toast.LENGTH_SHORT).show();
            return;
        }
        String normalized = url.trim();
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }
        Uri uri = Uri.parse(normalized);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            requireActivity().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), "No app can open this link", Toast.LENGTH_SHORT).show();
        }
    }

    private void buildAssignmentQuestions(CourseLearningLesson lesson) {
        binding.assignmentContainer.removeAllViews();
        answers.clear();
        if (lesson.content == null || lesson.content.assignmentQuestions == null) {
            binding.submitAssignment.setVisibility(View.GONE);
            return;
        }

        AssignmentQuestion[] questions = lesson.content.assignmentQuestions;
        int primaryAccent = ContextCompat.getColor(requireContext(), R.color.primary);
        float titleSp = 16f;

        for (int i = 0; i < questions.length; i++) {
            AssignmentQuestion question = questions[i];
            TextView title = new TextView(requireContext());
            title.setText((i + 1) + ". " + (question.question != null ? question.question : ""));
            title.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, titleSp);
            LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            titleLp.bottomMargin = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
            title.setLayoutParams(titleLp);
            binding.assignmentContainer.addView(title);

            String[] options = question.options;
            RadioGroup group = new RadioGroup(requireContext());
            group.setOrientation(RadioGroup.VERTICAL);

            LinearLayout.LayoutParams groupLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            groupLp.bottomMargin = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 18, getResources().getDisplayMetrics());
            group.setLayoutParams(groupLp);

            String key = lesson.id + "-" + i;
            if (options == null || options.length == 0) {
                TextView hint = new TextView(requireContext());
                hint.setText(R.string.assignment_missing_options_hint);
                hint.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
                group.addView(hint);
            } else {
                for (int optionIndex = 0; optionIndex < options.length; optionIndex++) {
                    MaterialRadioButton button = new MaterialRadioButton(requireContext());
                    CharSequence label = options[optionIndex] != null ? options[optionIndex] : "";
                    button.setText(label);
                    button.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
                    button.setButtonTintList(android.content.res.ColorStateList.valueOf(primaryAccent));

                    RadioGroup.LayoutParams rbLp = new RadioGroup.LayoutParams(
                            RadioGroup.LayoutParams.MATCH_PARENT,
                            RadioGroup.LayoutParams.WRAP_CONTENT);
                    int tb = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                    rbLp.setMargins(0, 0, 0, tb);
                    button.setLayoutParams(rbLp);

                    int finalOptionIndex = optionIndex;
                    button.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                        if (isChecked) {
                            answers.put(key, finalOptionIndex);
                        }
                    });
                    group.addView(button);
                }
            }

            binding.assignmentContainer.addView(group);
        }
    }

    private void updateProgress(CourseLearningLesson lesson, boolean completed) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("isCompleted", completed);
            RequestBody body = RequestBody.create(payload.toString(), MediaType.parse("application/json"));
            ApiClient.getApiService().updateLessonProgress(courseId, lesson.id, body)
                    .enqueue(new Callback<CourseLearningDetailResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<CourseLearningDetailResponse> call, @NonNull Response<CourseLearningDetailResponse> response) {
                            if (binding == null || !isAdded()) return;
                            if (response.isSuccessful() && response.body() != null) {
                                applyLearningDetailFromServer(response.body().data);
                            } else {
                                Toast.makeText(requireContext(), "Could not update progress", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<CourseLearningDetailResponse> call, @NonNull Throwable t) {
                            if (binding != null && isAdded()) {
                                Toast.makeText(requireContext(), "Could not update progress", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (Exception e) {
            if (binding != null && isAdded()) {
                Toast.makeText(requireContext(), "Could not update progress", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void submitAssignment() {
        if (selectedLesson == null || selectedLesson.content == null || selectedLesson.content.assignmentQuestions == null) {
            return;
        }

        AssignmentQuestion[] questions = selectedLesson.content.assignmentQuestions;
        JSONArray answerArray = new JSONArray();
        for (int i = 0; i < questions.length; i++) {
            AssignmentQuestion q = questions[i];
            boolean hasChoices = q.options != null && q.options.length > 0;
            if (!hasChoices) {
                continue;
            }

            String key = selectedLesson.id + "-" + i;
            Integer answer = answers.get(key);
            if (answer == null) {
                Toast.makeText(requireContext(), "Please answer all questions", Toast.LENGTH_SHORT).show();
                return;
            }
            answerArray.put(answer);
        }

        if (answerArray.length() == 0) {
            Toast.makeText(requireContext(), R.string.assignment_need_answers_hint, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject payload = new JSONObject();
            payload.put("answers", answerArray);
            RequestBody body = RequestBody.create(payload.toString(), MediaType.parse("application/json"));
            ApiClient.getApiService().submitAssignment(courseId, selectedLesson.id, body)
                    .enqueue(new Callback<AssignmentSubmitResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<AssignmentSubmitResponse> call, @NonNull Response<AssignmentSubmitResponse> response) {
                            if (binding == null || !isAdded()) return;
                            if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                                applyLearningDetailFromServer(response.body().data.learning);
                                Toast.makeText(requireContext(), "Score: " + response.body().data.score + "%", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Failed to submit", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<AssignmentSubmitResponse> call, @NonNull Throwable t) {
                            if (binding != null && isAdded()) {
                                Toast.makeText(requireContext(), "Failed to submit", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Failed to submit", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLessonSelected(CourseLearningLesson lesson) {
        selectLesson(lesson);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
