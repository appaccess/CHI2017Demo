package xiaoyiz.interactionproxy;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;

import java.util.List;

public class TestHack extends AccessibilityService {
    public int step = 0;

    public View addOverlay(Context context, Rect bounds) {
        ImageView image_view = new ImageView(context);
        image_view.setImageResource(R.drawable.gesture);
        image_view.setBackgroundColor(Color.argb(200, 0, 180, 0));
        WindowManager.LayoutParams temp_params = new WindowManager.LayoutParams(
                bounds.width(),
                bounds.height(),
                bounds.left,
                bounds.top,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        temp_params.gravity = Gravity.TOP | Gravity.LEFT;
        Utility.window_manager.addView(image_view, temp_params);
        Utility.list_overlays.add(image_view);
        return image_view;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Utility.statusBarHeight = Utility.getStatusBarHeight(this);
        Utility.window_manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Rect test = new Rect(0, 0, 900, 1920);
        addOverlay(this, test);
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }
    }


    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        return true;
    }


    @Override
    public void onInterrupt() {
    }
}
