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


public class AnnotationService extends AccessibilityService {
    private static final String TAG = "Xiaoyi";
    private ArrayList<Utility.annotationObject> listOfAnnotationObjects;
    private ArrayList<AccessibilityNodeInfo> listOfNodes;
    private AccessibilityNodeInfo current_overlay_previous_node;
    private AccessibilityNodeInfo current_overlay_next_node;
    private boolean annotation_overlay_exist = false;
    private boolean accessibility_rating_overlay_exist = false;

    @Override
    public void onCreate() {
        super.onCreate();

        Utility.statusBarHeight = Utility.getStatusBarHeight(this);
        Utility.window_manager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName() == null) return;
        if (!event.getPackageName().equals("com.android.vending")
                && !event.getPackageName().equals("xiaoyiz.interactionproxy")) return;

        if (getRootInActiveWindow() == null) return;

        Log.i(TAG, event.toString());
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED:
                if (event.getPackageName().equals("xiaoyiz.interactionproxy")
                        && event.getContentDescription() == null) {
                    String current_overlay_content = event.getText().toString();
                    current_overlay_content = current_overlay_content.substring(1, current_overlay_content.length()-1);
                    for (final Utility.annotationObject obj : listOfAnnotationObjects) {
                        if (obj.overlay_content.equals(current_overlay_content)) {
                            current_overlay_previous_node = obj.previous_node;
                            current_overlay_next_node = obj.next_node;
                        }
                    }
                }
                if (event.getPackageName().equals("xiaoyiz.interactionproxy")
                        && event.getContentDescription() != null) {
                    String c = event.getContentDescription().toString();
                    AccessibilityNodeInfo source = event.getSource();
                    Log.i(TAG, source.toString());
                    if (c.equals("prev")) {
                        current_overlay_previous_node.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                    } else if (c.equals("next")) {
                        current_overlay_next_node.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                    }
                }
                if (accessibility_rating_overlay_exist
                        && !event.getPackageName().equals("xiaoyiz.interactionproxy")) {
                    accessibility_rating_overlay_exist = false;
                    Utility.window_manager.removeView(Utility.accessibility_rating_overlay);
                }
                break;

            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                List<AccessibilityNodeInfo> results =
                        getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.android.vending:id/buy_button");
                if (results.size() == 0 && annotation_overlay_exist) {
                    for (View overlay : Utility.list_detail_overlays) {
                        Utility.window_manager = (WindowManager) getSystemService(WINDOW_SERVICE);
                        Utility.window_manager.removeView(overlay);
                        annotation_overlay_exist = false;
                    }
                }
                break;

            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                if (event.getPackageName().equals("xiaoyiz.interactionproxy")) {
                    return;
                }

            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                // Remove all overlays
                Utility.removeOverlays(this);
                Utility.removeDetailOverlays(this);
                final Context context = this;
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                List<AccessibilityNodeInfo> results =
                                        getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.android.vending:id/buy_button");
                                if (results.size() > 0) {
                                    AccessibilityNodeInfo install_button = results.get(0);
                                    listOfNodes = new ArrayList<AccessibilityNodeInfo>();
                                    Utility.refreshListOfNodes(listOfNodes, getRootInActiveWindow());
                                    Rect bounds = new Rect();
                                    install_button.getBoundsInScreen(bounds);
                                    bounds.top -= 150;
                                    bounds.bottom -= 150;
                                    listOfAnnotationObjects = new ArrayList<>();
                                    View.OnClickListener clickListener = new View.OnClickListener() {
                                        public void onClick(View v) {
                                            String[] list_content = new String[] { "Button Labeling", "All buttons are clearly labeled", "" , "Target Size", "4 targets needs to be enlarged", "", "Usability", "There are some minor accessibility", "issues with this app" };
                                            final View overlay = Utility.addListViewOverlayStore(context, list_content, "Rating");
                                            accessibility_rating_overlay_exist = true;
                                            new android.os.Handler().postDelayed(
                                                    new Runnable() {
                                                        public void run() {
                                                            overlay.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                                                        }
                                                    },
                                                    1000);
                                        }
                                    };
                                    listOfAnnotationObjects.add(new Utility.annotationObject(context, install_button, listOfNodes, bounds, "Accessibility Ratings", clickListener));
                                    annotation_overlay_exist = true;
                                }
                            }
                        },
                        1000);

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
