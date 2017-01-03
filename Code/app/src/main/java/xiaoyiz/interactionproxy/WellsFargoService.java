package xiaoyiz.interactionproxy;

import java.util.ArrayList;
import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class WellsFargoService extends AccessibilityService {
    private static final String TAG = "Xiaoyi";
    private ArrayList<AccessibilityNodeInfo> listOfNodes;
    private AccessibilityNodeInfo current_overlay_previous_node;
    private AccessibilityNodeInfo current_overlay_next_node;
    private boolean justAddedOverlay = false;
    private Utility.unlabeledObject unlabeledMenuButton;
    private View menuContentOverlay;
    private AccessibilityNodeInfo originalNextNode;


    @Override
    public void onCreate() {
        super.onCreate();

        Utility.statusBarHeight = Utility.getStatusBarHeight(this);
        Utility.window_manager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        if (event.getPackageName() == null) return;
        if (!event.getPackageName().equals("com.wf.wellsfargomobile")
                && !event.getPackageName().equals("xiaoyiz.interactionproxy")
                && !event.getPackageName().equals("com.android.systemui")) {
            Utility.removeOverlays(this);
            justAddedOverlay = false;
            unlabeledMenuButton = null;
            menuContentOverlay = null;
            return;
        }

        //Log.i("Xiaoyi", event.toString());

        final String[] list_content = new String[] { "ATMs/Locations", "Make an Appointment", "Products & Services", "Check Rates",
                "Contact Us", "About Wells Fargo", "My Favorites", "Enroll in Wells Fargo Online", "Forgot Password/Username?",
                "Privacy & Cookie Policy", "Online & Mobile Security"};

        final AccessibilityNodeInfo menuButton = Utility.getNodeByViewId(getRootInActiveWindow(), "com.wf.wellsfargomobile:id/hamburger_icon");
        final AccessibilityNodeInfo menuContent = Utility.getNodeByViewId(getRootInActiveWindow(), "com.wf.wellsfargomobile:id/slidingMenuWebViewFragment");

        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED:
                if (event.getSource().equals(menuButton)) {
                    if (unlabeledMenuButton != null) {
                        unlabeledMenuButton.generatedButton.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                    }
                }

                if (event.getSource().equals(menuContent)) {
                    if (menuContentOverlay != null) {
                        menuContentOverlay.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                    }
                }

                if (event.getPackageName().equals("xiaoyiz.interactionproxy") && event.getContentDescription() == null) {
                    listOfNodes = new ArrayList<>();
                    Utility.refreshListOfNodes(listOfNodes, getRootInActiveWindow());

                    if (unlabeledMenuButton != null) {
                        current_overlay_previous_node = unlabeledMenuButton.previous_node;
                    }

                    current_overlay_next_node = Utility.findItemByContentDescription(getRootInActiveWindow(), "Online Security Guarantee");
                    if (originalNextNode == null) {
                        originalNextNode = Utility.findItemByContentDescription(getRootInActiveWindow(), "Online Security Guarantee");
                    }
                    if (menuContentOverlay != null) {
                        current_overlay_next_node = null;
                    }

                    //Utility.printAllNodes(getRootInActiveWindow());
                    //Log.i(TAG, current_overlay_next_node.toString());
                }

                if (event.getPackageName().equals("xiaoyiz.interactionproxy") && event.getContentDescription() != null) {
                    String c = event.getContentDescription().toString();
                    if (c.equals("prev") && current_overlay_previous_node != null) {
                        current_overlay_previous_node.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                    } else if (c.equals("next") && current_overlay_next_node != null) {
                        current_overlay_next_node.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                    } else if (c.equals("next") && current_overlay_next_node == null && menuContentOverlay != null) {
                        menuContentOverlay.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                    }
                }
                break;

            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                if (justAddedOverlay) {
                    Log.i("Xiaoyi", "Called by our overlay, don't do anything now");
                    justAddedOverlay = false;
                    return;
                }

                // Remove all overlays
                if (Utility.getNodeByViewId(getRootInActiveWindow(), "com.wf.wellsfargomobile:id/scrollMain") == null) {
                    Log.i("Xiaoyi", "remove");
                    Utility.removeOverlays(this);
                    unlabeledMenuButton = null;
                } else {
                    Log.i("Xiaoyi", "Add");
                    if (menuButton != null && unlabeledMenuButton == null) {
                        listOfNodes = new ArrayList<>();
                        Utility.refreshListOfNodes(listOfNodes, getRootInActiveWindow());
                        unlabeledMenuButton = new Utility.unlabeledObject(this, "Menu", menuButton, listOfNodes);
                        justAddedOverlay = true;

                        View.OnClickListener clickListener = new View.OnClickListener() {
                            public void onClick(View v) {
                                Log.i("Xiaoyi", "Click");
                                menuButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                if (menuContentOverlay == null) {
                                    current_overlay_next_node = null;
                                } else {
                                    current_overlay_next_node = originalNextNode;
                                }
                            }
                        };

                        unlabeledMenuButton.generatedButton.setOnClickListener(clickListener);
                    }
                    if (menuContent != null && menuContentOverlay == null) {
                        Rect bounds = new Rect();
                        menuContent.getBoundsInScreen(bounds);
                        menuContentOverlay = Utility.addListViewOverlayWellsFargo(this, Utility.window_manager, list_content, "Menu Content", bounds);

                        ((ListView)menuContentOverlay).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int position, long id) {
                                final int[] location = new int[2];
                                view.getLocationOnScreen(location);
                                Log.i("Xiaoyi", location[0] + "," + location[1]);
                                Utility.window_manager.removeView(menuContentOverlay);
                                menuContentOverlay = null;
                                justAddedOverlay = true;
                                dispatchGesture(Utility.gestureTwoFingerClick(location[0] + 20, location[1] + 20, 0), null, null);
                            }
                        });

                        //Utility.list_overlays.add(menuContentOverlay);
                        //dispatchGesture(Utility.gestureTwoFingerClick(600, 300, 0), null, null);
                    } else if (menuContent == null && menuContentOverlay != null) {
                        Utility.window_manager.removeView(menuContentOverlay);
                        menuContentOverlay = null;
                    }
                }

                break;
            /*
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if (justAddedOverlay) {
                    Log.i("Xiaoyi", "Called by our overlay, don't do anything now");
                    justAddedOverlay = false;
                    return;
                }

                Log.i("Xiaoyi", "Let's add an overlay now");
                if (menuButton != null && unlabeledMenuButton == null) {
                    listOfNodes = new ArrayList<>();
                    Utility.refreshListOfNodes(listOfNodes, getRootInActiveWindow());
                    unlabeledMenuButton = new Utility.unlabeledObject(this, "Menu", menuButton, listOfNodes);
                    justAddedOverlay = true;

                    View.OnClickListener clickListener = new View.OnClickListener() {
                        public void onClick(View v) {
                            Log.i("Xiaoyi", "Click");
                            menuButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    };

                    unlabeledMenuButton.generatedButton.setOnClickListener(clickListener);
                }*/

                                /*
                final Context context = this;
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                justAddedOverlay = true;
                                AccessibilityNodeInfo menuContent = Utility.getNodeByViewId(getRootInActiveWindow(), "com.wf.wellsfargomobile:id/slidingMenuWebViewFragment");
                                if (menuContent != null) {
                                    Rect bounds = new Rect();
                                    menuContent.getBoundsInScreen(bounds);
                                    Log.i("Xiaoyi", bounds.toString());
                                    //View list = Utility.addListViewOverlay(context, Utility.window_manager, list_content, "Menu Content List", bounds);
                                    //Utility.list_overlays.add(list);
                                    //dispatchGesture(Utility.gestureTwoFingerClick(600, 300, 0), null, null);
                                }

                                Utility.printAllNodes(getRootInActiveWindow());
                            }
                        },
                        1000);
                                */

                /*
                List<AccessibilityNodeInfo> bar_results =
                        getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.toggl.timer:id/PreviousFrameLayout");
                if (bar_results.size() != 0) {
                    Log.i("Xiaoyi", "Bar: " + bar_results.get(0).toString());
                    Log.i("Xiaoyi", "Visible? " + bar_results.get(0).isVisibleToUser());
                }
                List<AccessibilityNodeInfo> pie_results =
                        getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.toggl.timer:id/ReportsViewPager");
                if (pie_results.size() != 0) {
                    Log.i("Xiaoyi", "Pie: " + pie_results.get(0).toString());
                    Log.i("Xiaoyi", "Visible? " + pie_results.get(0).isVisibleToUser());
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
