package xiaoyiz.interactionproxy;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class TutorialService extends AccessibilityService {
    public int step = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        step = 0;
        Utility.statusBarHeight = Utility.getStatusBarHeight(this);
        Utility.window_manager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }


    public void createStencil(String text, String type) {
        List<AccessibilityNodeInfo> results = getRootInActiveWindow().findAccessibilityNodeInfosByText(text);
        if (results != null && results.size() > 0) {
            final AccessibilityNodeInfo node = results.get(0);
            final Rect bounds = new Rect();
            node.getBoundsInScreen(bounds);
            if (type.equals("Button")) {
                bounds.left = 0;
                bounds.right = 1080;
                bounds.top -= 40;
                bounds.bottom += 80;
            }
            if (type.equals("LargeButton")) {
                bounds.left = 0;
                bounds.right = 1080;
                bounds.top -= 40;
                bounds.bottom += 120;
            }
            if (type.equals("Checkbox")) {
            }

            final View temp = Utility.addStencilOverlay(this, bounds);

            temp.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, final MotionEvent event) {
                    boolean target_touched = bounds.contains((int)(event.getX()), (int)(event.getY()));
                    if (target_touched) {
                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        Utility.clickAtPosition((int) (event.getX()), (int) (event.getY()), getRootInActiveWindow());
                                    }
                                }, 500);
                        Utility.window_manager.removeView(temp);
                    }
                    return true;
                }
            });
            step++;

            /*
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    if (!gesture_demo_removed) {
                                        Utility.window_manager.removeView(temp);
                                    }
                                }
                            }, 3000);*/
        }
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName() == null) return;
        //Log.i("Xiaoyi", event.toString());
        if (!event.getPackageName().equals("com.android.settings")
                && !event.getPackageName().equals("xiaoyiz.interactionproxy")) return;

        if (getRootInActiveWindow() == null) return;

        switch (step) {
            case 0:
                if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                    createStencil("Display", "Button");
                }
                break;
            case 1:
                if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                    createStencil("Adaptive brightness", "LargeButton");
                }
                break;
            case 2:
                if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                    createStencil("Sleep", "Button");
                }
                break;
            case 3:
                if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                    createStencil("30 minutes", "Checkbox");
                }
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
