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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import java.util.ArrayList;
import java.util.List;


public class RibbonService extends AccessibilityService {
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
            bounds.right = 1080;
            View.OnClickListener clickListener = new View.OnClickListener() {
                public void onClick(View v) {
                    String[] list_content = new String[] { "Search", "Sort", "More" };
                    final ListView detail_overlay = (ListView) Utility.addListViewOverlay(context, list_content, "Tools");
                    Utility.list_detail_overlays.add(detail_overlay);
                    detail_overlay.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id)
                        {
                            String str = (String)(((TextView)view).getText());
                            List<AccessibilityNodeInfo> results;
                            switch (str) {
                                case "Search":
                                    results = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.reddit.frontpage:id/action_search");
                                    if (results.size() > 0) results.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    break;
                                case "Inbox":
                                    results = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.reddit.frontpage:id/action_inbox");
                                    if (results.size() > 0) results.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    break;
                                case "More":
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
        if (!event.getPackageName().equals("com.reddit.frontpage")
                && !event.getPackageName().equals("xiaoyiz.interactionproxy")) return;

        if (getRootInActiveWindow() == null) return;
        List<AccessibilityNodeInfo> results = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.reddit.frontpage:id/action_search");

        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
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
