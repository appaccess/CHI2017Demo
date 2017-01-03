package xiaoyiz.interactionproxy;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import java.nio.ByteBuffer;

public class MagnifierService extends AccessibilityService {

    @Override
    public void onCreate() {
        super.onCreate();
    }



    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName() == null) return;

        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                if (event.getPackageName().equals("xiaoyiz.interactionproxy")) {
                    Log.i("Xiaoyi", Utility.final_x + ", " + Utility.final_y);
                    Utility.clickAtPosition(Utility.final_x, Utility.final_y, getRootInActiveWindow());

                }
                break;
                /*
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if (Utility.screencast_view != null) {
                    Utility.screencast_view.setImageBitmap(null);
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    Utility.screenshot_bitmap = Utility.getScreenShot();
                                }
                            }, 1000);
                }
                */
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
