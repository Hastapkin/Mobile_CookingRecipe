package com.example.cookingrecipe.ui.orders;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cookingrecipe.R;
import com.example.cookingrecipe.data.model.Transaction;
import com.example.cookingrecipe.data.model.TransactionsResponse;
import com.example.cookingrecipe.data.network.ApiClient;
import com.example.cookingrecipe.data.storage.SessionManager;
import com.example.cookingrecipe.databinding.FragmentMyOrdersBinding;
import com.example.cookingrecipe.ui.courses.SimpleItemSelectedListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyOrdersFragment extends Fragment implements OrdersAdapter.OrderActionListener {

    private static final String[] ORDER_STATUS_API = {"", "pending", "verified", "rejected"};

    private FragmentMyOrdersBinding binding;
    private OrdersAdapter adapter;
    private final List<Transaction> transactions = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new OrdersAdapter(transactions, this);
        binding.ordersList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.ordersList.setAdapter(adapter);

        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.order_status_filter_labels,
                R.layout.spinner_item_contrast
        );
        filterAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_contrast);
        binding.orderStatusFilter.setAdapter(filterAdapter);
        binding.orderStatusFilter.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> {
            if (binding == null || !isAdded()) return;
            if (!SessionManager.getInstance().isAuthenticated()) return;
            String apiStatus = ORDER_STATUS_API[position];
            loadOrders(apiStatus.isEmpty() ? null : apiStatus);
        }));

        binding.loginButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.loginFragment));

        loadOrders(null);
    }

    private void loadOrders(String status) {
        if (!SessionManager.getInstance().isAuthenticated()) {
            binding.progressBar.setVisibility(View.GONE);
            binding.loginPrompt.setVisibility(View.VISIBLE);
            binding.ordersList.setVisibility(View.GONE);
            binding.orderStatusFilter.setEnabled(false);
            return;
        }

        binding.loginPrompt.setVisibility(View.GONE);
        binding.ordersList.setVisibility(View.VISIBLE);
        binding.orderStatusFilter.setEnabled(true);

        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getApiService().getTransactions(status).enqueue(new Callback<TransactionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<TransactionsResponse> call, @NonNull Response<TransactionsResponse> response) {
                if (binding == null || !isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    transactions.clear();
                    if (response.body().data != null) {
                        transactions.addAll(response.body().data);
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(requireContext(), "Failed to load orders", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TransactionsResponse> call, @NonNull Throwable t) {
                if (binding == null || !isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Failed to load orders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onContinuePayment(Transaction order) {
        Bundle args = new Bundle();
        args.putInt("transactionId", order.id);
        NavHostFragment.findNavController(this).navigate(R.id.checkoutFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
