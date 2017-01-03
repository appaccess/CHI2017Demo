package xiaoyiz.interactionproxy;

import java.util.ArrayList;
import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

public class WindowIdService extends AccessibilityService {
    private static final String TAG = "Xiaoyi";
    private static int state = -1;

    @Override
    public void onCreate() {
        super.onCreate();

        Utility.statusBarHeight = Utility.getStatusBarHeight(this);
        Utility.window_manager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName() == null) return;
        /*
        if (!event.getPackageName().equals("com.yelp.android")
                && !event.getPackageName().equals("xiaoyiz.interactionproxy")) {
            return;
        }*/

        //Log.i("Xiaoyi", event.toString());

        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                if (state == 0) {
                    Utility.getWindowId(getRootInActiveWindow());
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                state = -1;
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Utility.getWindowId(getRootInActiveWindow());
                        state = 0;
                    }
                }, 500);
                break;
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                break;
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
