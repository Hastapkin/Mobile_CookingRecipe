package com.example.cookingrecipe.ui.orders;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingrecipe.data.model.Transaction;
import com.example.cookingrecipe.databinding.ItemOrderBinding;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    public interface OrderActionListener {
        void onContinuePayment(Transaction order);
    }

    private final List<Transaction> orders;
    private final OrderActionListener listener;

    public OrdersAdapter(List<Transaction> orders, OrderActionListener listener) {
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new OrderViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(orders.get(position));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private final ItemOrderBinding binding;
        private final OrderActionListener listener;

        OrderViewHolder(ItemOrderBinding binding, OrderActionListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        void bind(Transaction order) {
            binding.orderId.setText("Order #" + order.id);
            binding.orderStatus.setText(order.status != null ? order.status.toUpperCase(Locale.US) : "");
            binding.orderTotal.setText(NumberFormat.getCurrencyInstance(Locale.US).format(order.totalAmount));
            binding.orderDate.setText(order.createdAt != null ? order.createdAt : "");

            boolean isPending = order.status != null && "pending".equalsIgnoreCase(order.status);
            boolean canContinue = isPending && (order.paymentProof == null || order.paymentProof.isEmpty());
            binding.continuePaymentButton.setVisibility(canContinue ? View.VISIBLE : View.GONE);
            binding.continuePaymentButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onContinuePayment(order);
                }
            });
        }
    }
}
