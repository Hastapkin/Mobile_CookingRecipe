package com.example.cookingrecipe.data.network;

import com.example.cookingrecipe.data.model.AssignmentSubmitResponse;
import com.example.cookingrecipe.data.model.AuthResponse;
import com.example.cookingrecipe.data.model.BasicResponse;
import com.example.cookingrecipe.data.model.CartResponse;
import com.example.cookingrecipe.data.model.CourseLearningDetailResponse;
import com.example.cookingrecipe.data.model.CourseOverviewDetailResponse;
import com.example.cookingrecipe.data.model.CourseReviewsResponse;
import com.example.cookingrecipe.data.model.CoursesOverviewResponse;
import com.example.cookingrecipe.data.model.CreateTransactionResponse;
import com.example.cookingrecipe.data.model.LoginRequest;
import com.example.cookingrecipe.data.model.ProfileResponse;
import com.example.cookingrecipe.data.model.PurchasesResponse;
import com.example.cookingrecipe.data.model.RegisterRequest;
import com.example.cookingrecipe.data.model.SubmitPaymentResponse;
import com.example.cookingrecipe.data.model.TransactionResponse;
import com.example.cookingrecipe.data.model.TransactionsResponse;
import com.example.cookingrecipe.data.model.UpdateProfileRequest;
import com.example.cookingrecipe.data.model.AddToCartResponse;
import com.example.cookingrecipe.data.model.SaveReviewRequest;
import com.example.cookingrecipe.data.model.UploadProfilePictureResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @GET("auth/profile")
    Call<ProfileResponse> getProfile();

    @PUT("auth/profile")
    Call<ProfileResponse> updateProfile(@Body UpdateProfileRequest request);

    @Multipart
    @POST("images/profile")
        Call<UploadProfilePictureResponse> uploadProfilePicture(@Part MultipartBody.Part image);

    @GET("courses")
    Call<CoursesOverviewResponse> getCourses(
            @Query("search") String search,
            @Query("sortBy") String sortBy,
            @Query("page") Integer page,
            @Query("limit") Integer limit
    );

    @GET("courses/{id}")
    Call<CourseOverviewDetailResponse> getCourseDetail(@Path("id") int id);

    @GET("courses/{id}/reviews")
    Call<CourseReviewsResponse> getCourseReviews(@Path("id") int id);

    @POST("courses/{id}/reviews")
        Call<BasicResponse> saveCourseReview(@Path("id") int id, @Body SaveReviewRequest request);

    @PUT("courses/{id}/reviews")
        Call<BasicResponse> updateCourseReview(@Path("id") int id, @Body SaveReviewRequest request);

    @DELETE("courses/{id}/reviews")
    Call<BasicResponse> deleteCourseReview(@Path("id") int id);

    @GET("courses/me/purchases")
    Call<PurchasesResponse> getPurchasedCourseIds();

    @GET("courses/{id}/learn")
    Call<CourseLearningDetailResponse> getCourseLearning(@Path("id") int id);

    @PUT("courses/{courseId}/lessons/{lessonId}/progress")
    Call<CourseLearningDetailResponse> updateLessonProgress(
            @Path("courseId") int courseId,
            @Path("lessonId") int lessonId,
            @Body RequestBody body
    );

    @POST("courses/{courseId}/lessons/{lessonId}/assignment/submit")
    Call<AssignmentSubmitResponse> submitAssignment(
            @Path("courseId") int courseId,
            @Path("lessonId") int lessonId,
            @Body RequestBody body
    );

    @GET("cart")
    Call<CartResponse> getCart();

    @POST("cart")
    Call<AddToCartResponse> addToCart(@Body RequestBody body);

    @DELETE("cart/{courseId}")
    Call<BasicResponse> removeFromCart(@Path("courseId") int courseId);

    @POST("transactions")
    Call<CreateTransactionResponse> createTransaction();

    @GET("transactions")
    Call<TransactionsResponse> getTransactions(@Query("status") String status);

    @GET("transactions/{id}")
    Call<TransactionResponse> getTransaction(@Path("id") int id);

    @Multipart
    @PUT("transactions/{id}/payment")
    Call<SubmitPaymentResponse> submitPayment(
            @Path("id") int id,
            @Part("paymentMethod") RequestBody paymentMethod,
            @Part MultipartBody.Part paymentProof
    );
}
