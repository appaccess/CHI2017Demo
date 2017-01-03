package xiaoyiz.interactionproxy;

import android.accessibilityservice.AccessibilityService;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class SuppleService extends AccessibilityService {
    public int step = 0;
    public static AccessibilityNodeInfo switchButton;

    @Override
    public void onCreate() {
        super.onCreate();
        step = 0;
        Utility.statusBarHeight = Utility.getStatusBarHeight(this);
        Utility.window_manager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    public static void findItemByClass(AccessibilityNodeInfo info,
                                                        String class_name) {
        if (info == null) return;
        if (((String)(info.getClassName())).equals(class_name)) {
            switchButton = info;
        }
        if (info.getChildCount() != 0) {
            if (((String)(info.getClassName())).equals(class_name)) {
                switchButton = info;
            }
            for (int i = 0; i < info.getChildCount(); i++) {
                findItemByClass(info.getChild(i), class_name);
            }
        }
    }

    public void addSuppleOverlay() {
        final LinearLayout supple_overlay = Utility.addSuppleOverlay(this, "Supple");
        supple_overlay.setOrientation(LinearLayout.VERTICAL);

        final AccessibilityNodeInfo root = getRootInActiveWindow();

        LinearLayout.LayoutParams params;

        LinearLayout close = new LinearLayout(this);
        Button close_button = new Button(this);
        close_button.setText("Remove Personalized Interface");
        close.addView(close_button, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        close_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.window_manager.removeView(supple_overlay);
            }
        });
        close.setBackgroundColor(Color.rgb(255, 140, 0));
        supple_overlay.addView(close);

        LinearLayout tabs = new LinearLayout(this);
        Button tabs_button = new Button(this);
        tabs_button.setText("Tabs");
        tabs.addView(tabs_button, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tabs.setBackgroundColor(Color.rgb(255,140,0));
        supple_overlay.addView(tabs);

        LinearLayout buttons = new LinearLayout(this);
        Button requests_button = new Button(this);
        requests_button.setText("Request");
        Button challenges_button = new Button(this);
        challenges_button.setText("Challenge");
        Button messages_button = new Button(this);
        messages_button.setText("Message");

        buttons.addView(requests_button, 350, 200);
        buttons.addView(challenges_button, 350, 200);
        buttons.addView(messages_button, 350, 200);
        buttons.setBackgroundColor(Color.rgb(255, 140, 0));
        buttons.setPadding(0, 30, 0, 30);
        supple_overlay.addView(buttons);

        Button list_button = new Button(this);
        list_button.setText("View Records");
        list_button.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        list_button.setTextSize(30);
        supple_overlay.addView(list_button);

        CheckBox checkbox = new CheckBox(this);
        checkbox.setText("Log Complete");
        checkbox.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        checkbox.setScaleX(1.5f);
        checkbox.setScaleY(1.5f);
        checkbox.setBackgroundColor(Color.BLACK);
        checkbox.setChecked(true);
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                findItemByClass(root, "android.widget.Switch");
                if (buttonView.isChecked()) {
                    if(switchButton!=null) switchButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                } else {
                    if(switchButton!=null) switchButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }

            }
        });

        supple_overlay.addView(checkbox);

        LinearLayout switch_day = new LinearLayout(this);
        params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                300);
        params.gravity = Gravity.BOTTOM | Gravity.LEFT;
        Button prev_day_button = new Button(this);
        prev_day_button.setText("Go Prev Day");
        Button next_day_button = new Button(this);
        next_day_button.setText("Go Next Day");

        switch_day.addView(prev_day_button, 500, 200);
        switch_day.addView(next_day_button, 500, 200);
        switch_day.setPadding(0, 50, 0, 50);
        supple_overlay.addView(switch_day, params);

        TextView budget = new TextView(this);
        budget.setText("Calorie Budget: 1487");
        budget.setTextSize(22);
        budget.setTextColor(Color.BLACK);
        supple_overlay.addView(budget);
        TextView food = new TextView(this);
        food.setText("Food Today: 370");
        food.setTextSize(22);
        food.setTextColor(Color.BLACK);
        supple_overlay.addView(food);
        TextView exercise = new TextView(this);
        exercise.setText("Exercise Today: 500");
        exercise.setTextSize(22);
        exercise.setTextColor(Color.BLACK);
        supple_overlay.addView(exercise);
        TextView result = new TextView(this);
        result.setText("Remaining: 1617");
        result.setTextSize(22);
        result.setTextColor(Color.BLACK);
        supple_overlay.addView(result);


        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) checkbox.getLayoutParams();
        mlp.setMargins(200, 40, 10, 10);
        checkbox.setLayoutParams(mlp);
        checkbox.setPadding(0, 0, 50, 0);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName() == null) return;
        if (!event.getPackageName().equals("com.fitnow.loseit")) return;

        if (getRootInActiveWindow() == null) return;
        Log.i("Xiaoyi", event.toString());

        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                //Utility.printAllNodes(getRootInActiveWindow());
                addSuppleOverlay();
                /*
                List<AccessibilityNodeInfo> results =
                        getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.fitnow.loseit:id/menu_requests");

                AccessibilityNodeInfo search_button = results.get(0);
                Log.i("Xiaoyi", search_button.toString());
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
