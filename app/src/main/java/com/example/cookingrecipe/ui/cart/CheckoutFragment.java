package com.example.cookingrecipe.ui.cart;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.cookingrecipe.R;
import com.example.cookingrecipe.data.model.CartResponse;
import com.example.cookingrecipe.data.model.CreateTransactionResponse;
import com.example.cookingrecipe.data.model.SubmitPaymentResponse;
import com.example.cookingrecipe.data.network.ApiClient;
import com.example.cookingrecipe.data.storage.SessionManager;
import com.example.cookingrecipe.databinding.FragmentCheckoutBinding;
import com.example.cookingrecipe.util.FileUtils;

import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutFragment extends Fragment {

    private FragmentCheckoutBinding binding;
    private Uri selectedProofUri;
    private Integer transactionId;
    private Double transactionTotal;
    private boolean hasExistingTransaction;

    private static final String VIETQR_BANK = "sacombank";
    private static final String VIETQR_ACCOUNT = "050121382447";
    private static final String VIETQR_NAME = "Ngo Thanh Trung";
    private static final int USD_TO_VND = 25000;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedProofUri = uri;
                    Glide.with(binding.paymentProofPreview)
                            .load(uri)
                            .into(binding.paymentProofPreview);
                    binding.paymentProofPreview.setVisibility(View.VISIBLE);
                    binding.submitPaymentButtonTop.setVisibility(View.GONE);
                    binding.submitPaymentButton.setVisibility(View.VISIBLE);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            int argTransactionId = getArguments().getInt("transactionId", -1);
            hasExistingTransaction = argTransactionId > 0;
            if (hasExistingTransaction) {
                transactionId = argTransactionId;
            }
        }

        binding.paymentMethodGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.method_vietqr) {
                if (hasExistingTransaction && transactionId != null && transactionTotal != null) {
                    showQrForTransaction(transactionId, transactionTotal);
                } else {
                    createTransactionAndQr();
                }
            } else {
                binding.vietqrImage.setVisibility(View.GONE);
            }
        });

        binding.uploadProofButton.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        binding.submitPaymentButton.setOnClickListener(v -> submitPayment());
        binding.submitPaymentButtonTop.setOnClickListener(v -> submitPayment());

        if (hasExistingTransaction && transactionId != null) {
            loadExistingTransaction(transactionId);
        } else {
            loadCartSummary();
        }
    }

    private void loadCartSummary() {
        if (!SessionManager.getInstance().isAuthenticated()) {
            NavHostFragment.findNavController(this).navigate(R.id.loginFragment);
            return;
        }

        ApiClient.getApiService().getCart().enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    double total = response.body().data.totalPrice > 0 ? response.body().data.totalPrice : response.body().data.total;
                    binding.totalAmount.setText(NumberFormat.getCurrencyInstance(Locale.US).format(total));
                    if (binding.paymentMethodGroup.getCheckedRadioButtonId() == R.id.method_vietqr) {
                        createTransactionAndQr();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Failed to load cart", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadExistingTransaction(int id) {
        ApiClient.getApiService().getTransaction(id).enqueue(new Callback<com.example.cookingrecipe.data.model.TransactionResponse>() {
            @Override
            public void onResponse(@NonNull Call<com.example.cookingrecipe.data.model.TransactionResponse> call,
                                   @NonNull Response<com.example.cookingrecipe.data.model.TransactionResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    transactionId = response.body().data.id;
                    transactionTotal = response.body().data.totalAmount;
                    binding.totalAmount.setText(NumberFormat.getCurrencyInstance(Locale.US).format(transactionTotal));
                    if (binding.paymentMethodGroup.getCheckedRadioButtonId() == R.id.method_vietqr) {
                        showQrForTransaction(transactionId, transactionTotal);
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load order", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<com.example.cookingrecipe.data.model.TransactionResponse> call,
                                  @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Failed to load order", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createTransactionAndQr() {
        ApiClient.getApiService().createTransaction().enqueue(new Callback<CreateTransactionResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreateTransactionResponse> call, @NonNull Response<CreateTransactionResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    transactionId = response.body().data.id;
                    transactionTotal = response.body().data.totalAmount;
                    showQrForTransaction(transactionId, transactionTotal);
                } else {
                    Toast.makeText(requireContext(), "Failed to create transaction", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CreateTransactionResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Failed to create transaction", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showQrForTransaction(int id, double total) {
        int amountVnd = (int) Math.round(total * USD_TO_VND);
        String addInfo = "payment for order " + id;
        String qrUrl = "https://img.vietqr.io/image/" + VIETQR_BANK + "-" + VIETQR_ACCOUNT
                + "-compact2.jpg?amount=" + amountVnd
                + "&addInfo=" + Uri.encode(addInfo)
                + "&accountName=" + Uri.encode(VIETQR_NAME);
        binding.vietqrImage.setVisibility(View.VISIBLE);
        Glide.with(binding.vietqrImage).load(qrUrl).into(binding.vietqrImage);
    }

    private void submitPayment() {
        if (selectedProofUri == null) {
            Toast.makeText(requireContext(), "Please upload payment proof", Toast.LENGTH_SHORT).show();
            return;
        }

        if (transactionId == null) {
            Toast.makeText(requireContext(), "Transaction not created", Toast.LENGTH_SHORT).show();
            return;
        }

        String method = binding.paymentMethodGroup.getCheckedRadioButtonId() == R.id.method_paypal
                ? "PayPal" : "VietQR";

        try {
            File proofFile = FileUtils.copyToTempFile(requireContext(), selectedProofUri, "payment");
            RequestBody methodBody = RequestBody.create(method, MediaType.parse("text/plain"));
            RequestBody fileBody = RequestBody.create(proofFile, MediaType.parse("image/*"));
            MultipartBody.Part part = MultipartBody.Part.createFormData("paymentProof", proofFile.getName(), fileBody);

            ApiClient.getApiService().submitPayment(transactionId, methodBody, part)
                    .enqueue(new Callback<SubmitPaymentResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<SubmitPaymentResponse> call, @NonNull Response<SubmitPaymentResponse> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(requireContext(), "Payment submitted", Toast.LENGTH_SHORT).show();
                                NavHostFragment.findNavController(CheckoutFragment.this)
                                        .navigate(R.id.myOrdersFragment);
                            } else {
                                Toast.makeText(requireContext(), "Payment failed", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<SubmitPaymentResponse> call, @NonNull Throwable t) {
                            Toast.makeText(requireContext(), "Payment failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Payment failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
