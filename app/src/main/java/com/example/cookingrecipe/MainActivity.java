package com.example.cookingrecipe;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.cookingrecipe.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        applyWindowInsets();

        WindowInsetsControllerCompat barStyle = WindowCompat.getInsetsController(getWindow(), binding.getRoot());
        if (barStyle != null) {
            barStyle.setAppearanceLightStatusBars(true);
            barStyle.setAppearanceLightNavigationBars(true);
        }

        setSupportActionBar(binding.toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment,
                R.id.coursesFragment,
                R.id.myCoursesFragment,
                R.id.myOrdersFragment,
                R.id.profileFragment
        ).build();

        if (navController != null) {
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            binding.toolbar.setNavigationIconTint(ContextCompat.getColor(this, R.color.text_primary));

            binding.bottomNav.setOnItemSelectedListener(item -> {
                if (navController.getCurrentDestination() != null
                        && navController.getCurrentDestination().getId() == item.getItemId()) {
                    return true;
                }
                NavOptions navOptions = new NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setPopUpTo(navController.getGraph().getStartDestinationId(), false)
                        .build();
                navController.navigate(item.getItemId(), null, navOptions);
                return true;
            });
            binding.bottomNav.setOnItemReselectedListener(item -> {
                // no-op
            });

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int id = destination.getId();
                boolean authScreen = id == R.id.loginFragment || id == R.id.registerFragment;
                boolean showUp = authScreen
                        || (id != R.id.homeFragment
                        && id != R.id.coursesFragment
                        && id != R.id.myCoursesFragment
                        && id != R.id.myOrdersFragment
                        && id != R.id.profileFragment);
                setBottomNavigationVisible(!authScreen);
                binding.appBarLayout.setVisibility(View.VISIBLE);

                if (destination.getLabel() != null) {
                    binding.toolbar.setTitle(destination.getLabel());
                }

                if (showUp) {
                    binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24);
                    binding.toolbar.setNavigationIconTint(ContextCompat.getColor(this, R.color.text_primary));
                    binding.toolbar.setNavigationOnClickListener(v -> onSupportNavigateUp());
                } else {
                    binding.toolbar.setNavigationIcon(null);
                    binding.toolbar.setNavigationOnClickListener(null);
                }

                MenuItem item = binding.bottomNav.getMenu().findItem(id);
                if (item != null) {
                    item.setChecked(true);
                } else if (id == R.id.courseLearnFragment) {
                    MenuItem myCoursesTab = binding.bottomNav.getMenu().findItem(R.id.myCoursesFragment);
                    if (myCoursesTab != null) {
                        myCoursesTab.setChecked(true);
                    }
                }
                ViewCompat.requestApplyInsets(binding.getRoot());
            });
        }

        binding.toolbar.setOnMenuItemClickListener(null);
    }

    private void setBottomNavigationVisible(boolean visible) {
        binding.bottomNav.setVisibility(visible ? View.VISIBLE : View.GONE);
        updateBottomNavSpacing(visible);
    }

    private void updateBottomNavSpacing(boolean bottomNavVisible) {
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) binding.navHostFragment.getLayoutParams();
        int bottomMargin = 0;
        if (bottomNavVisible) {
            int navHeight = binding.bottomNav.getHeight();
            if (navHeight > 0) {
                bottomMargin = navHeight;
            } else {
                /* BottomNavigationView handles gesture inset via its own padding; only reserve bar height here. */
                bottomMargin = getResources().getDimensionPixelSize(R.dimen.bottom_nav_bar_height);
            }
        }
        lp.bottomMargin = bottomMargin;
        binding.navHostFragment.setLayoutParams(lp);
    }

    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            int topInset = systemBars.top;
            int bottomInset = systemBars.bottom;

            int navHostTopPadding = binding.appBarLayout.getVisibility() == View.VISIBLE ? 0 : topInset;
            binding.toolbar.setPadding(0, binding.appBarLayout.getVisibility() == View.VISIBLE ? topInset : 0, 0, 0);
            binding.navHostFragment.setPadding(0, navHostTopPadding, 0, 0);
            binding.navHostFragment.setTranslationY(0f);

            binding.bottomNav.setPadding(0, 0, 0, bottomInset);

            updateBottomNavSpacing(binding.bottomNav.getVisibility() == View.VISIBLE);
            /* IME + adjustResize can leave stale layout; BottomNav measured height settles after layout. */
            binding.bottomNav.post(() ->
                    updateBottomNavSpacing(binding.bottomNav.getVisibility() == View.VISIBLE));

            return insets;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && navController.navigateUp() || super.onSupportNavigateUp();
    }
}
