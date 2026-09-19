package com.lion.browser;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private FrameLayout pages;
    private EditText address;
    private TextView tabsButton;

    private final ArrayList<WebView> tabs = new ArrayList<>();
    private int current = 0;
    private ValueCallback<Uri[]> fileCallback;

    private static final int FILE_PICKER = 1001;

    private int dp(int v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        buildUI();
        addTab("https://www.google.com");
    }

    private TextView button(String text) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextColor(Color.WHITE);
        v.setTextSize(19);
        v.setGravity(Gravity.CENTER);
        v.setClickable(true);
        return v;
    }

    private void buildUI() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(12,13,16));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(8), dp(7), dp(8), dp(7));

        TextView lion = button("L");
        lion.setTextSize(21);
        lion.setBackgroundColor(Color.rgb(139,92,246));

        top.addView(lion,
                new LinearLayout.LayoutParams(dp(42), dp(42)));

        address = new EditText(this);
        address.setSingleLine(true);
        address.setTextSize(15);
        address.setTextColor(Color.WHITE);
        address.setHintTextColor(Color.rgb(160,160,165));
        address.setHint("بحث أو إدخال عنوان");
        address.setPadding(dp(14), 0, dp(14), 0);
        address.setBackgroundColor(Color.rgb(30,31,37));

        LinearLayout.LayoutParams ap =
                new LinearLayout.LayoutParams(0, dp(42), 1);
        ap.setMargins(dp(8), 0, dp(7), 0);

        top.addView(address, ap);

        tabsButton = button("1");
        top.addView(tabsButton,
                new LinearLayout.LayoutParams(dp(42), dp(42)));

        root.addView(top);

        pages = new FrameLayout(this);
        pages.setBackgroundColor(Color.WHITE);

        root.addView(pages,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1));

        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setBackgroundColor(Color.rgb(18,19,23));

        TextView back = button("‹");
        TextView forward = button("›");
        TextView refresh = button("↻");
        TextView home = button("⌂");
        TextView plus = button("+");

        TextView[] controls = {
                back, forward, refresh, home, plus
        };

        for (TextView c : controls) {
            nav.addView(c,
                    new LinearLayout.LayoutParams(
                            0, dp(52), 1));
        }

        root.addView(nav);

        address.setOnEditorActionListener((v, action, event) -> {
            navigate(address.getText().toString());
            return true;
        });

        back.setOnClickListener(v -> {
            WebView w = currentWeb();
            if (w != null && w.canGoBack()) w.goBack();
        });

        forward.setOnClickListener(v -> {
            WebView w = currentWeb();
            if (w != null && w.canGoForward()) w.goForward();
        });

        refresh.setOnClickListener(v -> {
            WebView w = currentWeb();
            if (w != null) w.reload();
        });

        home.setOnClickListener(v -> {
            WebView w = currentWeb();
            if (w != null) w.loadUrl("https://www.google.com");
        });

        plus.setOnClickListener(v -> addTab("https://www.google.com"));

        tabsButton.setOnClickListener(v -> addTab("https://www.google.com"));

        setContentView(root);
    }

    private void addTab(String url) {

        WebView web = new WebView(this);

        WebSettings s = web.getSettings();

        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setSupportMultipleWindows(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setSupportZoom(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(false);
        s.setMediaPlaybackRequiresUserGesture(false);

        web.setBackgroundColor(Color.WHITE);

        web.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request) {
                return false;
            }

            @Override
            public void onPageFinished(
                    WebView view,
                    String url) {

                if (view == currentWeb()) {
                    address.setText(url);
                    address.setSelection(address.length());
                }
            }
        });

        web.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onShowFileChooser(
                    WebView webView,
                    ValueCallback<Uri[]> callback,
                    FileChooserParams params) {

                fileCallback = callback;

                Intent intent = params.createIntent();

                try {
                    startActivityForResult(intent, FILE_PICKER);
                } catch (Exception e) {
                    fileCallback = null;
                    return false;
                }

                return true;
            }
        });

        tabs.add(web);
        current = tabs.size() - 1;

        pages.addView(web,
                new FrameLayout.LayoutParams(-1, -1));

        showCurrent();

        web.loadUrl(url);
    }

    private void navigate(String text) {

        text = text.trim();

        if (text.length() == 0) return;

        String url;

        if (text.startsWith("http://") ||
            text.startsWith("https://")) {

            url = text;

        } else if (text.contains(".") &&
                   !text.contains(" ")) {

            url = "https://" + text;

        } else {

            url = "https://www.google.com/search?q="
                    + Uri.encode(text);
        }

        WebView w = currentWeb();

        if (w != null) w.loadUrl(url);
    }

    private WebView currentWeb() {
        if (tabs.isEmpty()) return null;
        return tabs.get(current);
    }

    private void showCurrent() {

        for (int i = 0; i < tabs.size(); i++) {
            tabs.get(i).setVisibility(
                    i == current
                            ? View.VISIBLE
                            : View.GONE);
        }

        WebView w = currentWeb();

        if (w != null && w.getUrl() != null) {
            address.setText(w.getUrl());
        }

        tabsButton.setText(String.valueOf(tabs.size()));
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data);

        if (requestCode == FILE_PICKER) {

            if (fileCallback == null) return;

            Uri[] result = null;

            if (resultCode == RESULT_OK && data != null) {
                Uri uri = data.getData();

                if (uri != null) {
                    result = new Uri[]{uri};
                }
            }

            fileCallback.onReceiveValue(result);
            fileCallback = null;
        }
    }

    @Override
    public void onBackPressed() {

        WebView w = currentWeb();

        if (w != null && w.canGoBack()) {
            w.goBack();
            return;
        }

        if (tabs.size() > 1) {

            pages.removeView(w);
            w.destroy();

            tabs.remove(current);

            if (current >= tabs.size()) {
                current = tabs.size() - 1;
            }

            showCurrent();
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {

        for (WebView w : tabs) {
            w.destroy();
        }

        tabs.clear();

        super.onDestroy();
    }
}
