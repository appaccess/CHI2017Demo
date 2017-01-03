package xiaoyiz.interactionproxy;

import java.util.ArrayList;
import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class YelpService extends AccessibilityService {
    private static final String TAG = "Xiaoyi";
    private boolean justAddedOverlay = false;
    private boolean justAddedComposeOverlay = false;
    private View starOverlay;
    private View starOverlay_compose;
    private int star_selected = 0;
    private Utility.unlabeledObject unlabeledBackButton;
    private boolean searchFocused = false;
    private boolean nearbyFocused = false;

    private boolean postFocused = false;
    private boolean textOverviewFocused = false;

    private ArrayList<AccessibilityNodeInfo> listOfNodes;

    @Override
    public void onCreate() {
        super.onCreate();

        Utility.statusBarHeight = Utility.getStatusBarHeight(this);
        Utility.window_manager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName() == null) return;
        if (!event.getPackageName().equals("com.yelp.android")
                && !event.getPackageName().equals("xiaoyiz.interactionproxy")
                && !event.getPackageName().equals("com.android.systemui")
                && event.getPackageName().length() != 0
                && !event.getPackageName().toString().contains("com.google.android.inputmethod")) {
            justAddedOverlay = false;

            Log.i(TAG, "Quit" + event.getPackageName());
            if (starOverlay != null) {
                Utility.window_manager.removeView(starOverlay);
                starOverlay = null;
            }
            if (starOverlay_compose != null) {
                Utility.window_manager.removeView(starOverlay_compose);
                starOverlay_compose = null;
            }
            star_selected = 0;

            return;
        }

        Log.i("Xiaoyi", event.toString());

        /*
        AccessibilityNodeInfo backButton = Utility.findItemByContentDescription(getRootInActiveWindow(), "Navigate up");
        if (!backButton.getClassName().equals("android.widget.ImageButton")) {
            backButton = null;
        }*/

        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                if (justAddedOverlay) {
                    Log.i("Xiaoyi", "Called by our overlay, don't do anything now");
                    justAddedOverlay = false;
                    return;
                }

                /*
                unlabeledBackButton = new Utility.unlabeledObject(this, "Back", backButton, listOfNodes);
                com.yelp.android:id/add_review_next
                */

                AccessibilityNodeInfo stars = Utility.getNodeByViewId(getRootInActiveWindow(), "com.yelp.android:id/review_overview_stars");
                if (stars != null) {
                    final Rect bounds = new Rect();
                    stars.getBoundsInScreen(bounds);

                    View.OnClickListener clickListener = new View.OnClickListener() {
                        public void onClick(View v) {
                            Log.i(TAG, "Clicked");
                            if (starOverlay != null) {
                                Utility.window_manager.removeView(starOverlay);
                                starOverlay = null;
                            }
                            justAddedOverlay = true;
                            int[] star_location = new int[2];
                            v.getLocationOnScreen(star_location);
                            dispatchGesture(Utility.gestureTwoFingerClick(star_location[0] + 10, star_location[1] + 10, 0), null, null);

                            star_selected = ((star_location[0] - bounds.left) / (bounds.width()/5) + 1);
                        }
                    };

                    if (starOverlay == null) {
                        if (starOverlay_compose != null) {
                            Utility.window_manager.removeView(starOverlay_compose);
                            starOverlay_compose = null;
                        }

                        starOverlay = Utility.addYelpStarView(this, Utility.window_manager, bounds, clickListener, star_selected);
                        starOverlay.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                        justAddedOverlay = true;
                    }
                }

                AccessibilityNodeInfo compose_stars = Utility.getNodeByViewId(getRootInActiveWindow(), "com.yelp.android:id/review_compose_stars");
                if (compose_stars != null) {
                    final Rect bounds = new Rect();
                    compose_stars.getBoundsInScreen(bounds);

                    final Context context = this;
                    final View.OnClickListener clickListener = new View.OnClickListener() {
                        public void onClick(View v) {
                            Log.i(TAG, "Clicked");
                            if (starOverlay_compose != null) {
                                Utility.window_manager.removeView(starOverlay_compose);
                                starOverlay_compose = null;
                            }
                            justAddedComposeOverlay = true;
                            int[] star_location = new int[2];
                            v.getLocationOnScreen(star_location);
                            dispatchGesture(Utility.gestureTwoFingerClick(star_location[0] + 10, star_location[1] + 10, 0), null, null);
                            star_selected = ((star_location[0] - bounds.left) / (bounds.width()/5) + 1);

                            final View.OnClickListener temp = this;

                            new android.os.Handler().postDelayed(
                                    new Runnable() {
                                        public void run() {
                                            if (starOverlay_compose == null) {
                                                AccessibilityNodeInfo new_compose_stars = Utility.getNodeByViewId(getRootInActiveWindow(), "com.yelp.android:id/review_compose_stars");
                                                Rect new_bounds = new Rect();
                                                new_compose_stars.getBoundsInScreen(new_bounds);
                                                starOverlay_compose = Utility.addYelpStarView(context, Utility.window_manager, new_bounds, temp, star_selected);
                                                starOverlay_compose.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                                                justAddedComposeOverlay = true;
                                            }
                                        }
                                    },1000);
                        }
                    };

                    if (starOverlay_compose == null) {
                        if (starOverlay != null) {
                            Utility.window_manager.removeView(starOverlay);
                            starOverlay = null;
                        }

                        AccessibilityNodeInfo new_compose_stars = Utility.getNodeByViewId(getRootInActiveWindow(), "com.yelp.android:id/review_compose_stars");
                        Rect new_bounds = new Rect();
                        new_compose_stars.getBoundsInScreen(new_bounds);

                        starOverlay_compose = Utility.addYelpStarView(this, Utility.window_manager, new_bounds, clickListener, star_selected);
                        starOverlay_compose.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                        justAddedComposeOverlay = true;
                    }
                }

                if (stars == null && compose_stars == null) {
                    if (starOverlay != null) {
                        Utility.window_manager.removeView(starOverlay);
                        starOverlay = null;
                    }
                    if (starOverlay_compose != null) {
                        Utility.window_manager.removeView(starOverlay_compose);
                        starOverlay_compose = null;
                    }

                    star_selected = 0;
                }



                break;
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED:
                AccessibilityNodeInfo list = Utility.getNodeByViewId(getRootInActiveWindow(), "android:id/list");
                AccessibilityNodeInfo searchBar = Utility.getNodeByViewId(getRootInActiveWindow(), "com.yelp.android:id/search_text");
                AccessibilityNodeInfo nearbyButton = Utility.getNodeByViewId(getRootInActiveWindow(), "com.yelp.android:id/hot_button_nearby");

                if (list != null) {
                    AccessibilityNodeInfo searchPlaceHolder = list.getChild(0);
                    AccessibilityNodeInfo restaurantButton = list.getChild(1).getChild(0);
                    if (event.getSource() != null && event.getSource().equals(searchPlaceHolder)) {
                        if (searchBar!=null) {
                            searchBar.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                        }
                    }

                    if (searchBar != null && event.getSource() != null && event.getSource().equals(searchBar)) {
                        if (nearbyFocused) {
                            restaurantButton.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                        }
                        searchFocused = true;
                    }

                    if (nearbyButton != null && event.getSource() != null && event.getSource().equals(nearbyButton)) {
                        if (searchFocused) {
                            restaurantButton.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                        }
                        nearbyFocused = true;
                    }
                }

                if (searchBar == null || nearbyButton == null || event.getSource() == null
                        || (!event.getSource().equals(searchBar) && !event.getSource().equals(nearbyButton))) {
                    searchFocused = false;
                    nearbyFocused = false;
                }

                /*
                AccessibilityNodeInfo reviewWrapper = Utility.getNodeByViewId(getRootInActiveWindow(), "com.yelp.android:id/review_overview_content_wrapper");
                AccessibilityNodeInfo postButton = Utility.getNodeByViewId(getRootInActiveWindow(), "com.yelp.android:id/add_review_next");
                AccessibilityNodeInfo textOverview = Utility.getNodeByViewId(getRootInActiveWindow(), "com.yelp.android:id/review_overview_text");
                if (reviewWrapper != null) {
                    if (postButton != null && event.getSource() != null && event.getSource().equals(postButton)) {
                        if (textOverviewFocused) {
                            starOverlay.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                        }
                        postFocused = true;
                    }

                    if (textOverview != null && event.getSource() != null && event.getSource().equals(textOverview)) {
                        if (postFocused) {
                            starOverlay.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                        }
                        textOverviewFocused = true;
                    }
                }

                if (postButton == null || postButton == null || event.getSource() == null
                        || (!event.getSource().equals(postButton) && !event.getSource().equals(textOverview))) {
                    postFocused = false;
                    textOverviewFocused = false;
                }
                */



                /*
                if (event.getSource().equals(backButton)) {
                    if (unlabeledBackButton != null) {
                        unlabeledBackButton.generatedButton.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                    }
                }
                */
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
