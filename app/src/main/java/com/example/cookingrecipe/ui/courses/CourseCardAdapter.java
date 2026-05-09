package com.example.cookingrecipe.ui.courses;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cookingrecipe.R;
import com.example.cookingrecipe.data.model.CourseOverview;
import com.example.cookingrecipe.databinding.ItemCourseCardBinding;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CourseCardAdapter extends RecyclerView.Adapter<CourseCardAdapter.CourseViewHolder> {

    public interface CourseActionListener {
        void onCourseSelected(int courseId);
        void onAddToCart(int courseId);
    }

    private final List<CourseOverview> courses;
    private final Set<Integer> ownedCourseIds;
    private final CourseActionListener listener;

    public CourseCardAdapter(List<CourseOverview> courses, Set<Integer> ownedCourseIds, CourseActionListener listener) {
        this.courses = courses;
        this.ownedCourseIds = ownedCourseIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCourseCardBinding binding = ItemCourseCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new CourseViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        CourseOverview course = courses.get(position);
        holder.bind(course);
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    class CourseViewHolder extends RecyclerView.ViewHolder {
        private final ItemCourseCardBinding binding;

        CourseViewHolder(ItemCourseCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CourseOverview course) {
            binding.courseTitle.setText(course.title);
            binding.courseDescription.setText(course.description != null ? course.description : "A structured cooking course with guided lessons.");
            binding.courseMeta.setText(formatMeta(course));
            binding.courseRating.setText(String.format(Locale.getDefault(), "%.1f", course.rating));

            String price = NumberFormat.getCurrencyInstance(Locale.US).format(course.price);
            binding.coursePrice.setText(price);

            Glide.with(binding.courseImage)
                    .load(course.thumbnail)
                    .placeholder(R.drawable.placeholder_image)
                    .into(binding.courseImage);

            boolean owns = ownedCourseIds.contains(course.id);
            binding.courseAction.setEnabled(!owns);
            binding.courseAction.setText(owns ? "Purchased" : "Add to cart");

            binding.courseAction.setOnClickListener(v -> {
                if (!owns) {
                    listener.onAddToCart(course.id);
                }
            });

            binding.getRoot().setOnClickListener(v -> listener.onCourseSelected(course.id));
        }

        private String formatMeta(CourseOverview course) {
            String duration = course.duration == null || course.duration <= 0
                    ? "Self-paced"
                    : course.duration + "m";
            return duration + " • " + course.moduleCount + " modules";
        }
    }
}
