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

import java.util.ArrayList;
import java.util.List;


public class MacroService extends AccessibilityService {
    private boolean annotation_overlay_exist = false;

    @Override
    public void onCreate() {
        super.onCreate();

        Utility.statusBarHeight = Utility.getStatusBarHeight(this);
        Utility.window_manager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    public void addOverlayButton() {
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
            bounds.left -= 320;
            bounds.right -= 150;
            View.OnClickListener clickListener = new View.OnClickListener() {
                public void onClick(View v) {
                    ArrayList<AccessibilityNodeInfo> list_scrollable_nodes = new ArrayList<>();
                    Utility.findScrollableItem(getRootInActiveWindow(), list_scrollable_nodes);
                    for (int i = 0; i < 100; i++) {
                        list_scrollable_nodes.get(0).performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
                    }
                }
            };
            new Utility.annotationObject(context, search_button, listOfNodes, bounds, "Scroll to Top", clickListener);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName() == null) return;
        //if (!event.getPackageName().equals("com.reddit.frontpage")) return;

        if (getRootInActiveWindow() == null) return;
        List<AccessibilityNodeInfo> results = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.reddit.frontpage:id/action_search");
        //Utility.printAllNodes(getRootInActiveWindow());

        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                Log.i("X", event.getSource().toString());
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if (results == null || results.size() == 0) {
                    // Remove all overlays
                    Utility.removeOverlays(this);
                    Utility.removeDetailOverlays(this);
                } else if (Utility.list_overlays.size() == 0) {
                    addOverlayButton();
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
