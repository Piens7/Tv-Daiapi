package com.daiapi.launcher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;

public class MainActivity extends Activity {
    static final String[] DOCK = {"com.netflix.ninja", "com.google.android.youtube.tv",
            "com.disney.disneyplus", "com.spotify.tv.android"};
    static final String[] GRID = {"com.crunchyroll.crunchyroid", "com.amazon.amazonvideo.livingroom",
            "com.xuper.amigonly.tv.new", "com.nuvio.tv", "com.zenith.tv", "com.yablio.sendfilestotv"};
    float d;

    int dp(int v) { return (int) (v * d + 0.5f); }

    TextView text(String s, int sp, int color) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextSize(sp); t.setTextColor(color);
        return t;
    }

    GradientDrawable glass(int color, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color); g.setCornerRadius(dp(radiusDp));
        return g;
    }

    void focusFx(final View v, final Runnable onFocus) {
        final GradientDrawable ring = glass(Color.TRANSPARENT, 14);
        ring.setStroke(dp(3), Color.WHITE);
        v.setFocusable(true);
        v.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View x, boolean g) {
                x.animate().scaleX(g ? 1.12f : 1f).scaleY(g ? 1.12f : 1f).setDuration(150).start();
                x.setTranslationZ(g ? dp(12) : 0);
                if (x instanceof FrameLayout) ((FrameLayout) x).setForeground(g ? ring : null);
                else x.setBackground(glass(g ? 0x66FFFFFF : 0x33FFFFFF, 20));
            }
        });
    }

    boolean launch(String pkg) {
        PackageManager pm = getPackageManager();
        Intent i = pm.getLeanbackLaunchIntentForPackage(pkg);
        if (i == null) i = pm.getLaunchIntentForPackage(pkg);
        if (i == null) return false;
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        return true;
    }

    View pill(String label, final String pkg, final String action) {
        TextView t = text(label, 14, Color.WHITE);
        t.setPadding(dp(16), dp(8), dp(16), dp(8));
        t.setBackground(glass(0x33FFFFFF, 20));
        focusFx(t, null);
        t.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (pkg == null || !launch(pkg)) startActivity(new Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } catch (Exception e) { }
            }
        });
        return t;
    }

    View card(final String pkg, int w, int h) {
        PackageManager pm = getPackageManager();
        Intent li = pm.getLeanbackLaunchIntentForPackage(pkg);
        if (li == null) li = pm.getLaunchIntentForPackage(pkg);
        if (li == null) return null;
        Drawable img = null;
        boolean banner = false;
        ResolveInfo ri = pm.resolveActivity(li, 0);
        if (ri != null) { img = ri.activityInfo.loadBanner(pm); banner = img != null; }
        if (img == null) { try { img = pm.getApplicationIcon(pkg); } catch (Exception e) { } }
        FrameLayout f = new FrameLayout(this);
        f.setBackground(glass(0xFF2A2A40, 14));
        ImageView iv = new ImageView(this);
        iv.setImageDrawable(img);
        iv.setScaleType(banner ? ImageView.ScaleType.CENTER_CROP : ImageView.ScaleType.FIT_CENTER);
        if (!banner) iv.setPadding(dp(16), dp(16), dp(16), dp(16));
        f.addView(iv, new FrameLayout.LayoutParams(-1, -1));
        f.setClipToOutline(true);
        f.setOutlineProvider(new ViewOutlineProvider() {
            public void getOutline(View v, Outline o) { o.setRoundRect(0, 0, v.getWidth(), v.getHeight(), dp(14)); }
        });
        focusFx(f, null);
        f.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { launch(pkg); }
        });
        f.setLayoutParams(new LinearLayout.LayoutParams(dp(w), dp(h)));
        return f;
    }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        d = getResources().getDisplayMetrics().density;
        FrameLayout root = new FrameLayout(this);
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFF0A0A14, 0xFF1E1E34}));
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(48), dp(28), dp(48), dp(28));
        col.setClipChildren(false); col.setClipToPadding(false);
        root.addView(col, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setClipChildren(false);
        top.addView(text("Inicio", 24, Color.WHITE), new LinearLayout.LayoutParams(0, -2, 1f));
        View p1 = pill("Entradas", "com.tcl.tvinput", Settings.ACTION_SETTINGS);
        View p2 = pill("Red", null, Settings.ACTION_WIFI_SETTINGS);
        View p3 = pill("Ajustes", null, Settings.ACTION_SETTINGS);
        View[] pills = {p1, p2, p3};
        for (View p : pills) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
            lp.setMargins(dp(8), 0, dp(8), 0);
            top.addView(p, lp);
        }
        TextClock clock = new TextClock(this);
        clock.setFormat24Hour("HH:mm"); clock.setFormat12Hour(null);
        clock.setTextSize(20); clock.setTextColor(Color.WHITE);
        clock.setPadding(dp(12), 0, 0, 0);
        top.addView(clock);
        col.addView(top);

        TextView t1 = text("Favoritas", 16, 0xAAFFFFFF);
        t1.setPadding(dp(8), dp(36), 0, dp(10));
        col.addView(t1);

        LinearLayout dock = new LinearLayout(this);
        dock.setBackground(glass(0x22FFFFFF, 28));
        dock.setPadding(dp(20), dp(20), dp(20), dp(20));
        dock.setClipChildren(false); dock.setClipToPadding(false);
        dock.setGravity(Gravity.CENTER_VERTICAL);
        for (String p : DOCK) {
            View c = card(p, 175, 98);
            if (c == null) continue;
            ((LinearLayout.LayoutParams) c.getLayoutParams()).setMargins(dp(12), 0, dp(12), 0);
            dock.addView(c);
        }
        col.addView(dock, new LinearLayout.LayoutParams(-2, -2));

        TextView t2 = text("Aplicaciones", 16, 0xAAFFFFFF);
        t2.setPadding(dp(8), dp(36), 0, dp(10));
        col.addView(t2);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(6);
        grid.setClipChildren(false); grid.setClipToPadding(false);
        for (String p : GRID) {
            View c = card(p, 125, 70);
            if (c == null) continue;
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = dp(125); lp.height = dp(70);
            lp.setMargins(dp(8), dp(8), dp(8), dp(8));
            c.setLayoutParams(lp);
            grid.addView(c);
        }
        col.addView(grid);

        setContentView(root);
        if (dock.getChildCount() > 0) dock.getChildAt(0).requestFocus();
    }

    @Override public void onBackPressed() { }
}
