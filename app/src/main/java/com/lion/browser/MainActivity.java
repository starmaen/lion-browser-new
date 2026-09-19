package com.lion.browser;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private FrameLayout browserArea;
    private EditText addressBar;
    private TextView tabCount;

    private final ArrayList<WebView> tabs = new ArrayList<>();
    private int currentTab = 0;

    private int dp(int value) {
        return (int) (value *
                getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildInterface();
        newTab("https://www.google.com");
    }

    private TextView button(String text) {
        TextView button = new TextView(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(20);
        button.setGravity(Gravity.CENTER);
        button.setClickable(true);
        return button;
    }

    private void buildInterface() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(16,17,20));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(8), dp(6), dp(8), dp(6));

        TextView logo = button("L");
        logo.setTextSize(22);
        logo.setBackgroundColor(Color.rgb(139,92,246));

        top.addView(
                logo,
                new LinearLayout.LayoutParams(dp(42), dp(42))
        );

        addressBar = new EditText(this);
        addressBar.setSingleLine(true);
        addressBar.setTextColor(Color.WHITE);
        addressBar.setHintTextColor(Color.GRAY);
        addressBar.setHint("بحث أو أدخل عنوان موقع");
        addressBar.setTextSize(15);
        addressBar.setPadding(dp(12), 0, dp(12), 0);
        addressBar.setBackgroundColor(Color.rgb(31,32,37));

        LinearLayout.LayoutParams addressParams =
                new LinearLayout.LayoutParams(0, dp(42), 1);

        addressParams.setMargins(dp(8), 0, dp(6), 0);
        top.addView(addressBar, addressParams);

        tabCount = button("1");

        top.addView(
                tabCount,
                new LinearLayout.LayoutParams(dp(42), dp(42))
        );

        root.addView(top);

        browserArea = new FrameLayout(this);
        browserArea.setBackgroundColor(Color.WHITE);

        root.addView(
                browserArea,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        LinearLayout bottom = new LinearLayout(this);
        bottom.setGravity(Gravity.CENTER);
        bottom.setBackgroundColor(Color.rgb(20,21,25));

        TextView back = button("‹");
        TextView forward = button("›");
        TextView reload = button("↻");
        TextView home = button("⌂");
        TextView plus = button("+");

        bottom.addView(back,
                new LinearLayout.LayoutParams(0, dp(52), 1));

        bottom.addView(forward,
                new LinearLayout.LayoutParams(0, dp(52), 1));

        bottom.addView(reload,
                new LinearLayout.LayoutParams(0, dp(52), 1));

        bottom.addView(home,
                new LinearLayout.LayoutParams(0, dp(52), 1));

        bottom.addView(plus,
                new LinearLayout.LayoutParams(0, dp(52), 1));

        root.addView(bottom);

        back.setOnClickListener(v -> {
            WebView web = currentWeb();
            if (web != null && web.canGoBack()) {
                web.goBack();
            }
        });

        forward.setOnClickListener(v -> {
            WebView web = currentWeb();
            if (web != null && web.canGoForward()) {
                web.goForward();
            }
        });

        reload.setOnClickListener(v -> {
            WebView web = currentWeb();
            if (web != null) {
                web.reload();
            }
        });

        home.setOnClickListener(v -> {
            WebView web = currentWeb();
            if (web != null) {
                web.loadUrl("https://www.google.com");
            }
        });

        plus.setOnClickListener(v -> {
            newTab("https://www.google.com");
        });

        setContentView(root);
    }

    private WebView currentWeb() {
        if (tabs.isEmpty()) {
            return null;
        }
        return tabs.get(currentTab);
    }

    private void newTab(String url) {

        WebView web = new WebView(this);

        WebSettings settings = web.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(false);

        web.setBackgroundColor(Color.WHITE);

        web.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(
                    WebView view,
                    String url) {

                if (view == currentWeb()) {
                    addressBar.setText(url);
                }
            }
        });

        tabs.add(web);
        currentTab = tabs.size() - 1;

        browserArea.addView(
                web,
                new FrameLayout.LayoutParams(-1, -1)
        );

        showCurrentTab();

        web.loadUrl(url);
    }

    private void showCurrentTab() {

        for (int i = 0; i < tabs.size(); i++) {
            tabs.get(i).setVisibility(
                    i == currentTab
                            ? android.view.View.VISIBLE
                            : android.view.View.GONE
            );
        }

        WebView web = currentWeb();

        if (web != null && web.getUrl() != null) {
            addressBar.setText(web.getUrl());
        }

        tabCount.setText(
                String.valueOf(tabs.size())
        );
    }

    private void navigate(String text) {

        text = text.trim();

        if (text.isEmpty()) {
            return;
        }

        String url;

        if (text.startsWith("http://") ||
                text.startsWith("https://")) {

            url = text;

        } else if (text.contains(".") &&
                !text.contains(" ")) {

            url = "https://" + text;

        } else {

            url = "https://www.google.com/search?q="
                    + android.net.Uri.encode(text);
        }

        WebView web = currentWeb();

        if (web != null) {
            web.loadUrl(url);
        }
    }

    @Override
    public void onBackPressed() {

        WebView web = currentWeb();

        if (web != null && web.canGoBack()) {
            web.goBack();
            return;
        }

        if (tabs.size() > 1) {

            browserArea.removeView(web);
            web.destroy();
            tabs.remove(currentTab);

            if (currentTab >= tabs.size()) {
                currentTab = tabs.size() - 1;
            }

            showCurrentTab();
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {

        for (WebView web : tabs) {
            web.destroy();
        }

        tabs.clear();

        super.onDestroy();
    }
}
