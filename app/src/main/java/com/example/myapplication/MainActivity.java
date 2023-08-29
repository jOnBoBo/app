package com.example.myapplication; // Replace with your actual package name
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.util.Collections;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_USAGE_STATS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView usageDataTextView = findViewById(R.id.usageDataTextView);

// Check if permission is granted
        if (!hasUsageStatsPermission()) {
            // Inform the user to manually grant permission in settings
            Toast.makeText(this, "Please grant usage access permission in settings", Toast.LENGTH_LONG).show();

            // Open usage access settings page
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        } else {
            // Permission already granted
            displayUsageStats(usageDataTextView);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_USAGE_STATS) {
            // Check if permission was granted after user interaction
            if (hasUsageStatsPermission()) {
                TextView usageDataTextView = findViewById(R.id.usageDataTextView);
                displayUsageStats(usageDataTextView);
            }
        }
    }

    private boolean hasUsageStatsPermission() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager == null) {
            return false;
        }

        try {
            long endTime = System.currentTimeMillis();
            long startTime = endTime - (24 * 60 * 60 * 1000); // Example: last 24 hours
            usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    private void displayUsageStats(TextView textView) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long currentTime = System.currentTimeMillis();
        long startTime = currentTime - (1000); 

        List<UsageStats> statsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startTime, currentTime);

        // Sort the list by usage time (in descending order)
        Collections.sort(statsList, (stats1, stats2) ->
                Long.compare(stats2.getTotalTimeInForeground(), stats1.getTotalTimeInForeground()));

        StringBuilder usageData = new StringBuilder();
        int count = 0;
        for (UsageStats stats : statsList) {
            String packageName = stats.getPackageName();
            long totalUsageTimeMillis = stats.getTotalTimeInForeground(); // in milliseconds

            if (totalUsageTimeMillis > 0 && count < 5) {
                long totalUsageTimeSeconds = totalUsageTimeMillis / 1000;
                long hours = totalUsageTimeSeconds / 3600;
                long minutes = (totalUsageTimeSeconds % 3600) / 60;
                long seconds = totalUsageTimeSeconds % 60;

                usageData.append("Package: ").append(packageName)
                        .append(", Usage Time: ").append(hours).append(" hours ")
                        .append(minutes).append(" minutes ").append(seconds).append(" seconds\n");

                count++;
            }

            if (count >= 5) {
                break; // Display top 5 apps
            }
        }

        textView.setText(usageData.toString());
    }




}