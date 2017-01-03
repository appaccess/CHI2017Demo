package xiaoyiz.interactionproxy;

import java.util.ArrayList;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class MyAccessibilityService extends AccessibilityService {
    private static final String TAG = "Xiaoyi";
    private ArrayList<Utility.unlabeledObject> listOfUnlabeledObjects;
    private ArrayList<AccessibilityNodeInfo> listOfNodes;
    private AccessibilityNodeInfo current_overlay_previous_node;
    private AccessibilityNodeInfo current_overlay_next_node;


    @Override
    public void onCreate() {
        super.onCreate();

        Utility.statusBarHeight = Utility.getStatusBarHeight(this);
        Utility.window_manager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName() == null) return;
        if (!event.getPackageName().equals("com.sirma.mobile.bible.android")
                && !event.getPackageName().equals("xiaoyiz.interactionproxy")) return;

        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED:
                if (event.getPackageName().equals("xiaoyiz.interactionproxy")
                        && event.getContentDescription() == null) {
                    String current_overlay_content = event.getText().toString();
                    current_overlay_content = current_overlay_content.substring(1, current_overlay_content.length()-1);
                    for (final Utility.unlabeledObject obj : listOfUnlabeledObjects) {
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
                    } else if (c.equals("and")) {
                        current_overlay_next_node.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                    }
                } else {
                    String text = event.getText().toString();
                    String content = null;
                    if (event.getContentDescription() != null) {
                        content = event.getContentDescription().toString();
                    }
                    String className = (String) event.getClassName();
                    if (text.equals("[]") && content == null && !className.equals("android.view.View")) {
                        Log.i(TAG, "EMPTY!!!");
                        /*
                        Context context = getApplicationContext();
                        CharSequence t = "Hello toast!";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, t, duration);
                        toast.show();
                        */
                        for (final Utility.unlabeledObject obj : listOfUnlabeledObjects) {
                            if (obj.unlabeledItem.equals(event.getSource())) {
                                Log.i(TAG, "Find!!!");
                                // sendAccessibilityEvent doesn't work!
                                obj.generatedButton.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                            }
                        }
                    }
                }
                break;

            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                Log.i("Xiaoyi", "Removed");
                // Remove all overlays
                Utility.removeOverlays(this);
                Utility.removeDetailOverlays(this);
                final Context context = this;
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                listOfNodes = new ArrayList<AccessibilityNodeInfo>();
                                Utility.refreshListOfNodes(listOfNodes, getRootInActiveWindow());
                                listOfUnlabeledObjects = new ArrayList<Utility.unlabeledObject>();
                                Utility.findUnlabeledItems(context, getRootInActiveWindow(),
                                        listOfNodes, listOfUnlabeledObjects);
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
