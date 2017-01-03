package xiaoyiz.interactionproxy;

import java.util.ArrayList;
import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class TogglService extends AccessibilityService {
    private static final String TAG = "Xiaoyi";
    private ArrayList<Utility.unlabeledObject> listOfUnlabeledObjects;
    private ArrayList<AccessibilityNodeInfo> listOfNodes;
    private AccessibilityNodeInfo current_overlay_previous_node;
    private AccessibilityNodeInfo current_overlay_next_node;
    private Utility.unlabeledObject unlabeledPrevButton;
    private Utility.unlabeledObject unlabeledNextButton;
    private boolean justAddedOverlay = false;
    private boolean timerStarted = false;

    @Override
    public void onCreate() {
        super.onCreate();

        Utility.statusBarHeight = Utility.getStatusBarHeight(this);
        Utility.window_manager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i("Xiaoyi", event.toString());
        if (event.getPackageName() == null) return;
        if (!event.getPackageName().equals("com.toggl.timer")
                && !event.getPackageName().equals("xiaoyiz.interactionproxy")
                && !event.getPackageName().equals("com.android.systemui")) {
            Utility.removeOverlays(this);
            justAddedOverlay = false;
            listOfUnlabeledObjects = new ArrayList<>();;
            return;
        }


        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED:
                /*
                AccessibilityNodeInfo prev_button = Utility.getNodeByViewId(getRootInActiveWindow(), "com.toggl.timer:id/PreviousFrameLayout");
                if (prev_button != null && event.getSource() != null
                        && event.getSource().equals(prev_button)) {

                }
                */

                if (event.getPackageName().equals("xiaoyiz.interactionproxy")
                        && event.getContentDescription() == null) {
                    Log.i(TAG, event.getSource().toString());
                    Rect source_bounds = new Rect();
                    event.getSource().getBoundsInScreen(source_bounds);
                    for (final Utility.unlabeledObject obj : listOfUnlabeledObjects) {
                        Rect obj_bounds = new Rect();
                        obj.unlabeledItem.getBoundsInScreen(obj_bounds);
                        if (Rect.intersects(source_bounds, obj_bounds)) {
                            current_overlay_previous_node = obj.previous_node;
                            current_overlay_next_node = obj.next_node;
                            Log.i(TAG, current_overlay_previous_node.toString());
                            Log.i(TAG, current_overlay_next_node.toString());
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
                } else {
                    String text = event.getText().toString();
                    String content = null;
                    if (event.getContentDescription() != null) {
                        content = event.getContentDescription().toString();
                    }
                    String className = (String) event.getClassName();

                    if (text.equals("[]") && content == null && !className.equals("android.view.View")
                            || text.equals("[Toggl]") || text.equals("[Navigate up]")) {
                        Log.i(TAG, "EMPTY!!!");
                        for (final Utility.unlabeledObject obj : listOfUnlabeledObjects) {
                            if (obj.unlabeledItem.equals(event.getSource())) {
                                Log.i(TAG, "Find!!!");
                                // sendAccessibilityEvent doesn't work!
                                obj.generatedButton.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                            }
                        }
                    }
                    if (text.equals("[Timer]") && !timerStarted && listOfUnlabeledObjects.size() > 2) {
                        listOfUnlabeledObjects.get(listOfUnlabeledObjects.size() - 1).generatedButton.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                    }
                }

                break;

            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if (Utility.getNodeByViewId(getRootInActiveWindow(), "com.toggl.timer:id/ContinueImageButton") == null) {
                    Utility.removeOverlays(this);
                    listOfUnlabeledObjects = new ArrayList<>();
                    justAddedOverlay = false;
                }
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                if (justAddedOverlay || (listOfUnlabeledObjects != null && listOfUnlabeledObjects.size() != 0)) {
                    Log.i("Xiaoyi", "Called by our overlay, don't do anything now, 1");
                    justAddedOverlay = false;
                    return;
                }

                final Context context = this;
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                if (justAddedOverlay || (listOfUnlabeledObjects != null && listOfUnlabeledObjects.size() != 0)) {
                                    Log.i("Xiaoyi", "Called by our overlay, don't do anything now, 2");
                                    justAddedOverlay = false;
                                    return;
                                }
                                Log.i("Xiaoyi", "Add");
                                listOfNodes = new ArrayList<>();
                                Utility.refreshListOfNodes(listOfNodes, getRootInActiveWindow());
                                Utility.printAllNodes(getRootInActiveWindow());
                                listOfUnlabeledObjects = new ArrayList<>();
                                for (AccessibilityNodeInfo node:listOfNodes) {
                                    if (node.getViewIdResourceName() != null && node.getViewIdResourceName().equals("com.toggl.timer:id/ContinueImageButton")) {
                                        justAddedOverlay = true;
                                        final Utility.unlabeledObject temp = new Utility.unlabeledObject(context, " ", node, listOfNodes);
                                        listOfUnlabeledObjects.add(temp);
                                        temp.generatedButton.setOnClickListener(new View.OnClickListener() {
                                            public void onClick(View v) {
                                                Log.i("Xiaoyi", "Click");
                                                Utility.removeOverlays(context);
                                                listOfUnlabeledObjects = new ArrayList<>();
                                                if (!timerStarted) {
                                                    timerStarted = true;
                                                }

                                                int[] star_location = new int[2];
                                                v.getLocationOnScreen(star_location);
                                                dispatchGesture(Utility.gestureTwoFingerClick(star_location[0] + 10, star_location[1] + 10, 0), null, null);
                                            }
                                        });
                                    }
                                }

                                final AccessibilityNodeInfo timerText = Utility.getNodeByViewId(getRootInActiveWindow(), "com.toggl.timer:id/TimerTitleTextView");

                                if (timerText != null) {
                                    justAddedOverlay = true;
                                    final AccessibilityNodeInfo menu = Utility.getPrevNode(listOfNodes, timerText);
                                    if (menu == null) return;
                                    final Utility.unlabeledObject labeledMenu = new Utility.unlabeledObject(context, " ", menu, listOfNodes);
                                    listOfUnlabeledObjects.add(labeledMenu);
                                    labeledMenu.generatedButton.setOnClickListener(new View.OnClickListener() {
                                        public void onClick(View v) {
                                            Log.i("Xiaoyi", "Click");
                                            menu.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                            Utility.removeOverlays(context);
                                            listOfUnlabeledObjects = new ArrayList<>();
                                        }
                                    });

                                    final AccessibilityNodeInfo floatingButton = Utility.getNodeByViewId(getRootInActiveWindow(), "com.toggl.timer:id/StartStopBtn");

                                    if (floatingButton != null) {
                                        justAddedOverlay = true;

                                        final Utility.unlabeledObject labeledFloatingButton = new Utility.unlabeledObject(context, " ", floatingButton, listOfNodes);
                                        labeledFloatingButton.previous_node = menu;
                                        AccessibilityNodeInfo next = Utility.getNodeByViewId(getRootInActiveWindow(), "com.toggl.timer:id/newItem");
                                        if (next!=null) labeledFloatingButton.next_node = next;


                                        listOfUnlabeledObjects.add(labeledFloatingButton);
                                        labeledFloatingButton.generatedButton.setOnClickListener(new View.OnClickListener() {
                                            public void onClick(View v) {
                                                Log.i("Xiaoyi", "Click");
                                                floatingButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                                Utility.removeOverlays(context);
                                                listOfUnlabeledObjects = new ArrayList<>();
                                            }
                                        });
                                    }
                                }

                                if (listOfUnlabeledObjects != null && listOfUnlabeledObjects.size() > 0) {
                                    listOfUnlabeledObjects.get(0).generatedButton.setOnClickListener(new View.OnClickListener() {
                                        public void onClick(View v) {
                                            Log.i("Xiaoyi", "Click");
                                            Utility.removeOverlays(context);
                                            listOfUnlabeledObjects = new ArrayList<>();
                                            timerStarted = !timerStarted;

                                            int[] star_location = new int[2];
                                            v.getLocationOnScreen(star_location);
                                            dispatchGesture(Utility.gestureTwoFingerClick(star_location[0] + 10, star_location[1] + 10, 0), null, null);
                                        }
                                    });
                                }

                                new android.os.Handler().postDelayed(
                                        new Runnable() {
                                            public void run() {
                                                if (listOfUnlabeledObjects == null) return;
                                                for (Utility.unlabeledObject obj : listOfUnlabeledObjects) {
                                                    int node_index = Utility.getNodeIndex(listOfNodes, obj.unlabeledItem);
                                                    if (node_index >= 3 && listOfNodes.get(node_index - 3).getText() != null) {
                                                        String desc = "Start Timer";
                                                        obj.generatedButton.setText(desc);
                                                    }
                                                }

                                                if (!timerStarted) {
                                                    if (listOfUnlabeledObjects.size() > 2) {
                                                        listOfUnlabeledObjects.get(listOfUnlabeledObjects.size() - 2).generatedButton.setText("Menu");
                                                        listOfUnlabeledObjects.get(listOfUnlabeledObjects.size() - 1).generatedButton.setText("Create Timer");
                                                    }
                                                } else {
                                                    if (listOfUnlabeledObjects.size() > 0) {
                                                        Utility.unlabeledObject obj = listOfUnlabeledObjects.get(0);
                                                        obj.generatedButton.setText("Stop timer");
                                                    }
                                                }
                                            }
                                        }, 500);
                            }
                        },
                        1000);
                break;

                /*
                // Remove all overlays
                Utility.removeOverlays(this);
                Utility.removeDetailOverlays(this);
                final Context context = this;
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                List<AccessibilityNodeInfo> timer_list_results =
                                        getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.toggl.timer:id/LogRecyclerView");
                                if (timer_list_results.size() != 0) {
                                    Log.i("Xiaoyi", "List find");
                                    AccessibilityNodeInfo timer_list = timer_list_results.get(0);
                                    for (int i = 0; i < timer_list.getChildCount(); i++) {
                                        AccessibilityNodeInfo timer = timer_list.getChild(i);
                                        if (timer.getClassName().equals("android.widget.RelativeLayout")) {
                                            List<AccessibilityNodeInfo> continue_results = timer.findAccessibilityNodeInfosByViewId("com.toggl.timer:id/ContinueImageButton");
                                            if (continue_results.size() != 0) {
                                                AccessibilityNodeInfo continueButton = continue_results.get(0);
                                                Log.i("Xiaoyi", continueButton.toString());
                                            }
                                            List<AccessibilityNodeInfo> tag_results = timer.findAccessibilityNodeInfosByViewId("com.toggl.timer:id/TagsIcon");
                                            if (tag_results.size() != 0) {
                                                AccessibilityNodeInfo tag = tag_results.get(0);
                                                Log.i("Xiaoyi", tag.toString());
                                            }
                                        }
                                    }
                                }
                                listOfUnlabeledObjects = new ArrayList<Utility.unlabeledObject>();
                                Utility.findUnlabeledItems(context, getRootInActiveWindow(),
                                        listOfNodes, listOfUnlabeledObjects);
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
