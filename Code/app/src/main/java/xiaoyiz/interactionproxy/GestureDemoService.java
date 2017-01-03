package xiaoyiz.interactionproxy;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.KeyEvent;
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


public class GestureDemoService extends AccessibilityService {
    public boolean gesture_demo_shown = false;
    public boolean gesture_demo_removed = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Utility.statusBarHeight = Utility.getStatusBarHeight(this);
        Utility.window_manager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    public void addOverlay() {
        final Context context = this;
        List<AccessibilityNodeInfo> results =
                getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.reddit.frontpage:id/action_search");
        if (results.size() > 0) {
            AccessibilityNodeInfo search_button = results.get(0);
            ArrayList<AccessibilityNodeInfo> listOfNodes = new ArrayList<>();
            Utility.refreshListOfNodes(listOfNodes, getRootInActiveWindow());
            Rect bounds = new Rect();
            search_button.getBoundsInScreen(bounds);
            bounds.top -= Utility.statusBarHeight;
            bounds.bottom -= Utility.statusBarHeight - 25;
            bounds.right = 1080;
            View.OnClickListener clickListener = new View.OnClickListener() {
                public void onClick(View v) {
                    String[] list_content = new String[] { "SEARCH", "INBOX", "MORE" };
                    final ListView detail_overlay = (ListView) Utility.addListViewOverlay(context, list_content, "Tools");
                    Utility.list_detail_overlays.add(detail_overlay);
                    detail_overlay.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
                        {
                            String str = ((TextView) arg1).getText().toString();
                            List<AccessibilityNodeInfo> results;
                            switch (str) {
                                case "SEARCH":
                                    results = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.reddit.frontpage:id/action_search");
                                    if (results.size() > 0) results.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    break;
                                case "INBOX":
                                    results = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.reddit.frontpage:id/action_inbox");
                                    if (results.size() > 0) results.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    break;
                                case "MORE":
                                    results = getRootInActiveWindow().findAccessibilityNodeInfosByText("More options");
                                    if (results.size() > 0) results.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    break;
                            }
                            Utility.removeOverlays(context);
                            Utility.removeDetailOverlays(context);
                        }
                    });
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    detail_overlay.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                                }
                            },
                            1000);
                }
            };
            new Utility.annotationObject(context, search_button, listOfNodes, bounds, "TOOLS", clickListener);
        }
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName() == null) return;
        if (!event.getPackageName().equals("com.google.android.keep")
                && !event.getPackageName().equals("xiaoyiz.interactionproxy")) return;

        if (getRootInActiveWindow() == null) return;
        List<AccessibilityNodeInfo> results = getRootInActiveWindow().findAccessibilityNodeInfosByText("Welcome to Google Keep;");

        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if (results != null && results.size() > 0 && !gesture_demo_shown) {
                    AccessibilityNodeInfo note = results.get(0);
                    Rect bounds = new Rect();
                    note.getBoundsInScreen(bounds);
                    final View temp = Utility.addImageViewOverlay(this, bounds);
                    gesture_demo_shown = true;

                    temp.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Utility.window_manager.removeView(temp);
                            gesture_demo_removed = true;
                        }
                    });

                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    if (!gesture_demo_removed) {
                                        Utility.window_manager.removeView(temp);
                                    }
                                }
                            }, 3000);
                }
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
