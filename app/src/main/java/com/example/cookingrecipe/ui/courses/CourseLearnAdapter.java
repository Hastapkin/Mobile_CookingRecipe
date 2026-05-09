package com.example.cookingrecipe.ui.courses;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import com.example.cookingrecipe.data.model.CourseLearningLesson;
import com.example.cookingrecipe.data.model.CourseLearningModule;
import com.example.cookingrecipe.R;
import com.example.cookingrecipe.databinding.ItemLessonBinding;
import com.example.cookingrecipe.databinding.ItemModuleHeaderBinding;

import java.util.ArrayList;
import java.util.List;

public class CourseLearnAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface LessonListener {
        void onLessonSelected(CourseLearningLesson lesson);
    }

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_LESSON = 1;

    private final List<Object> items = new ArrayList<>();
    private final LessonListener listener;
    private Integer selectedLessonId;

    public CourseLearnAdapter(LessonListener listener) {
        this.listener = listener;
    }

    public void setModules(List<CourseLearningModule> modules) {
        items.clear();
        if (modules != null) {
            for (CourseLearningModule module : modules) {
                items.add(module);
                if (module.lessons != null) {
                    items.addAll(module.lessons);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void setSelectedLessonId(int lessonId) {
        selectedLessonId = lessonId;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof CourseLearningModule ? TYPE_HEADER : TYPE_LESSON;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            ItemModuleHeaderBinding binding = ItemModuleHeaderBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false
            );
            return new ModuleHeaderViewHolder(binding);
        }
        ItemLessonBinding binding = ItemLessonBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new LessonViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);
        if (holder instanceof ModuleHeaderViewHolder) {
            ((ModuleHeaderViewHolder) holder).bind((CourseLearningModule) item);
        } else if (holder instanceof LessonViewHolder) {
            ((LessonViewHolder) holder).bind((CourseLearningLesson) item);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ModuleHeaderViewHolder extends RecyclerView.ViewHolder {
        private final ItemModuleHeaderBinding binding;

        ModuleHeaderViewHolder(ItemModuleHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CourseLearningModule module) {
            binding.moduleTitle.setText(module.title);
        }
    }

    class LessonViewHolder extends RecyclerView.ViewHolder {
        private final ItemLessonBinding binding;

        LessonViewHolder(ItemLessonBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CourseLearningLesson lesson) {
            binding.lessonTitle.setText(lesson.title);
            binding.lessonMeta.setText(lesson.contentType != null ? lesson.contentType : "Lesson");
            binding.lessonCompleteIcon.setVisibility(lesson.isCompleted ? View.VISIBLE : View.GONE);

            boolean isSelected = selectedLessonId != null && lesson.id == selectedLessonId;
            int strokeColor = ContextCompat.getColor(binding.getRoot().getContext(),
                    isSelected ? R.color.primary : R.color.neutral_300);
            binding.lessonCard.setStrokeColor(strokeColor);
            binding.lessonCard.setStrokeWidth(isSelected ? 2 : 1);
            binding.getRoot().setOnClickListener(v -> listener.onLessonSelected(lesson));
        }
    }
}
