package xiaoyiz.interactionproxy;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class SummarizeService extends AccessibilityService {
    private static final String TAG = "Xiaoyi";

    @Override
    public void onCreate() {
        super.onCreate();

        Utility.statusBarHeight = Utility.getStatusBarHeight(this);
        Utility.window_manager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName() == null) return;
        if (!event.getPackageName().equals("com.facebook.katana")) return;
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                Utility.removeOverlays(this);
                Utility.removeDetailOverlays(this);
                final Context context = this;
                final Timer timer = new Timer();
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                final ArrayList<AccessibilityNodeInfo> list_scrollable_nodes =
                                        new ArrayList<AccessibilityNodeInfo>();

                                AccessibilityNodeInfo temp = null;
                                Utility.findScrollableItem(getRootInActiveWindow(), list_scrollable_nodes);
                                for (AccessibilityNodeInfo node : list_scrollable_nodes) {
                                    if (node.getClassName().equals("android.support.v7.widget.RecyclerView")) {
                                        temp = node;
                                    }
                                }
                                final AccessibilityNodeInfo scrollable_node = temp;

                                timer.scheduleAtFixedRate(new TimerTask() {
                                    @Override
                                    public void run() {
                                        Utility.findPost(getRootInActiveWindow());
                                        if (scrollable_node != null) {
                                            scrollable_node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                                        }
                                    }
                                }, 0, 100);
                            }
                        },
                        1000);
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                timer.cancel();
                                Set<String> s = new LinkedHashSet<>(Utility.list_post);
                                Utility.list_post.clear();
                                Utility.list_post.addAll(s);
                                Utility.addFBPostOverlay(context);
                                Utility.FB_list_adapter.notifyDataSetChanged();
                            }
                        },
                        3000);


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
