package com.example.cookingrecipe.ui.courses;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingrecipe.data.model.CourseModule;
import com.example.cookingrecipe.databinding.ItemModuleBinding;

import java.util.ArrayList;
import java.util.List;

public class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder> {

    private final List<CourseModule> modules = new ArrayList<>();

    public void setModules(List<CourseModule> newModules) {
        modules.clear();
        if (newModules != null) {
            modules.addAll(newModules);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ModuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemModuleBinding binding = ItemModuleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ModuleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleViewHolder holder, int position) {
        holder.bind(modules.get(position));
    }

    @Override
    public int getItemCount() {
        return modules.size();
    }

    static class ModuleViewHolder extends RecyclerView.ViewHolder {
        private final ItemModuleBinding binding;

        ModuleViewHolder(ItemModuleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CourseModule module) {
            binding.moduleTitle.setText(module.title);
            int lessonCount = module.lessons != null ? module.lessons.size() : 0;
            binding.moduleMeta.setText(lessonCount + " lessons");
        }
    }
}
