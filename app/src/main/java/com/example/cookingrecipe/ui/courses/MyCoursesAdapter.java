package com.example.cookingrecipe.ui.courses;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cookingrecipe.R;
import com.example.cookingrecipe.data.model.CourseOverview;
import com.example.cookingrecipe.databinding.ItemMyCourseBinding;

import java.util.List;

public class MyCoursesAdapter extends RecyclerView.Adapter<MyCoursesAdapter.MyCourseViewHolder> {

    public interface MyCourseListener {
        void onCourseSelected(int courseId);
    }

    private final List<CourseOverview> courses;
    private final MyCourseListener listener;

    public MyCoursesAdapter(List<CourseOverview> courses, MyCourseListener listener) {
        this.courses = courses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyCourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMyCourseBinding binding = ItemMyCourseBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new MyCourseViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyCourseViewHolder holder, int position) {
        holder.bind(courses.get(position));
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    class MyCourseViewHolder extends RecyclerView.ViewHolder {
        private final ItemMyCourseBinding binding;

        MyCourseViewHolder(ItemMyCourseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CourseOverview course) {
            binding.courseTitle.setText(course.title);
            binding.courseMeta.setText(course.moduleCount + " modules");

            Glide.with(binding.courseImage)
                    .load(course.thumbnail)
                    .placeholder(R.drawable.placeholder_image)
                    .into(binding.courseImage);

            binding.startButton.setOnClickListener(v -> listener.onCourseSelected(course.id));
            binding.getRoot().setOnClickListener(v -> listener.onCourseSelected(course.id));
        }
    }
}
