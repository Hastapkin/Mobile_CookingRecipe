package com.example.cookingrecipe.ui.misc;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cookingrecipe.databinding.FragmentContactBinding;

public class ContactFragment extends Fragment {

    private FragmentContactBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentContactBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ScrollView scrollHost = (ScrollView) binding.getRoot();
        View.OnFocusChangeListener collapseAfterIme = (v, hasFocus) -> {
            if (hasFocus) return;
            scrollHost.post(() -> {
                if (binding == null || !isAdded()) return;
                scrollHost.scrollTo(0, 0);
                scrollHost.setTranslationY(0f);
            });
        };
        binding.emailInput.setOnFocusChangeListener(collapseAfterIme);
        binding.subjectInput.setOnFocusChangeListener(collapseAfterIme);
        binding.messageInput.setOnFocusChangeListener(collapseAfterIme);

        binding.sendMessageButton.setOnClickListener(v -> {
            String email = binding.emailInput.getText().toString().trim();
            String subject = binding.subjectInput.getText().toString().trim();
            String message = binding.messageInput.getText().toString().trim();

            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:phamtuan301104@gmail.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, "From: " + email + "\n\n" + message);
            startActivity(Intent.createChooser(intent, "Send Email"));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
