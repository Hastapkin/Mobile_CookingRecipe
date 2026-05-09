package com.example.cookingrecipe.ui.cart;

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

import com.example.cookingrecipe.R;
import com.example.cookingrecipe.data.model.BasicResponse;
import com.example.cookingrecipe.data.model.CartData;
import com.example.cookingrecipe.data.model.CartResponse;
import com.example.cookingrecipe.data.network.ApiClient;
import com.example.cookingrecipe.data.storage.SessionManager;
import com.example.cookingrecipe.databinding.FragmentCartBinding;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartFragment extends Fragment implements CartItemAdapter.CartActionListener {

    private FragmentCartBinding binding;
    private CartItemAdapter adapter;
    private final List<com.example.cookingrecipe.data.model.CartItem> items = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new CartItemAdapter(items, this);
        binding.cartList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.cartList.setAdapter(adapter);

        binding.checkoutButton.setOnClickListener(v -> {
            if (items.isEmpty()) {
                Toast.makeText(requireContext(), "Your cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            NavHostFragment.findNavController(this).navigate(R.id.checkoutFragment);
        });

        binding.continueButton.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.coursesFragment));

        loadCart();
    }

    private void loadCart() {
        if (!SessionManager.getInstance().isAuthenticated()) {
            NavHostFragment.findNavController(this).navigate(R.id.loginFragment);
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getApiService().getCart().enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    CartData data = response.body().data;
                    items.clear();
                    if (data != null && data.items != null) {
                        items.addAll(data.items);
                    }
                    adapter.notifyDataSetChanged();
                    updateTotals(data);
                    toggleEmpty();
                } else {
                    Toast.makeText(requireContext(), "Failed to load cart", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Failed to load cart", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTotals(CartData data) {
        double total = 0;
        if (data != null) {
            total = data.totalPrice > 0 ? data.totalPrice : data.total;
        }
        binding.totalAmount.setText(NumberFormat.getCurrencyInstance(Locale.US).format(total));
    }

    private void toggleEmpty() {
        boolean empty = items.isEmpty();
        binding.emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.cartList.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.summarySection.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRemoveItem(int courseId) {
        ApiClient.getApiService().removeFromCart(courseId).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(@NonNull Call<BasicResponse> call, @NonNull Response<BasicResponse> response) {
                if (response.isSuccessful()) {
                    loadCart();
                } else {
                    Toast.makeText(requireContext(), "Failed to remove item", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BasicResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Failed to remove item", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
