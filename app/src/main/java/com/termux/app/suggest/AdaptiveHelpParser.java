package com.termux.app.suggest;

import android.content.Context;
import com.termux.view.SuggestionOverlayView;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AdaptiveHelpParser {
    private Context context;
    private SuggestionOverlayView overlayView;

    public AdaptiveHelpParser(Context context, SuggestionOverlayView overlayView) {
        this.context = context;
        this.overlayView = overlayView;
    }

    public void parseAndSuggestAsync(final String commandToken, final int cursorX, final int cursorY, final android.view.View anchorView) {
        new Thread(() -> {
            List<String> suggestions = fetchHelpOptions(commandToken);
            if (!suggestions.isEmpty() && overlayView != null) {
                // Post to main thread to update UI
                overlayView.getContentView().post(() -> {
                    overlayView.setSuggestions(context, suggestions);
                    if (!overlayView.isShowing()) {
                        overlayView.showAtCursor(anchorView, cursorX, cursorY);
                    }
                });
            } else if (overlayView != null && overlayView.isShowing()) {
                overlayView.getContentView().post(() -> overlayView.dismiss());
            }
        }).start();
    }

    private List<String> fetchHelpOptions(String commandToken) {
        List<String> options = new ArrayList<>();
        try {
            // Very basic heuristic: run command --help and extract dashed parameters
            Process process = Runtime.getRuntime().exec(new String[] { commandToken, "--help" });
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                for (String token : tokens) {
                    if (token.startsWith("-") && token.length() > 1) {
                        // Clean up trailing commas or equals
                        String cleanToken = token.replaceAll("[,=].*", "");
                        if (!options.contains(cleanToken)) {
                            options.add(cleanToken);
                        }
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return options;
    }
}
