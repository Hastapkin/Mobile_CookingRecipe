package com.example.cookingrecipe.ui.cart;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cookingrecipe.R;
import com.example.cookingrecipe.data.model.CartItem;
import com.example.cookingrecipe.databinding.ItemCartBinding;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartViewHolder> {

    public interface CartActionListener {
        void onRemoveItem(int courseId);
    }

    private final List<CartItem> items;
    private final CartActionListener listener;

    public CartItemAdapter(List<CartItem> items, CartActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private final ItemCartBinding binding;

        CartViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CartItem item) {
            binding.itemTitle.setText(item.title);
            binding.itemMeta.setText(item.difficulty != null ? item.difficulty : "Course");
            double price = item.discountedPrice != null ? item.discountedPrice : item.price;
            binding.itemPrice.setText(NumberFormat.getCurrencyInstance(Locale.US).format(price));

            Glide.with(binding.itemImage)
                    .load(item.thumbnail != null ? item.thumbnail : item.videoThumbnail)
                    .placeholder(R.drawable.placeholder_image)
                    .into(binding.itemImage);

            binding.removeButton.setOnClickListener(v -> listener.onRemoveItem(item.courseId));
        }
    }
}
