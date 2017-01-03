package xiaoyiz.interactionproxy;

import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.graphics.Typeface;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Utility {
    public static int statusBarHeight = 0;
    public static WindowManager window_manager;
    public static ArrayList<View> list_overlays = new ArrayList<>();
    public static ArrayList<View> list_detail_overlays = new ArrayList<>();
    public static ListView accessibility_rating_overlay;
    public static ListView FB_post_overlay;
    public static ListView ribbon_overlay;
    public static ArrayList<String> list_post = new ArrayList<>();
    public static ArrayAdapter<String> FB_list_adapter;
    public static MediaProjectionManager media_projection_manager;
    public static MediaProjection media_projection;
    public static VirtualDisplay virtual_display;
    public static ImageReader image_reader;
    public static ImageView screencast_view;
    public static Bitmap screenshot_bitmap;
    public static int compensate_width;
    public static int device_w = 1080;
    public static int device_h = 1920;
    public static int final_x;
    public static int final_y;

    public static int tab_num = 0;


    public static class unlabeledObject {
        public String id;
        public String overlay_content;
        public AccessibilityNodeInfo unlabeledItem;
        public Button generatedButton;
        public AccessibilityNodeInfo previous_node;
        public AccessibilityNodeInfo next_node;
        public unlabeledObject(Context context,
                               String alt_text,
                               AccessibilityNodeInfo info,
                               ArrayList<AccessibilityNodeInfo> list_nodes) {
            unlabeledItem = info;
            id = Utility.getIdOfNode(unlabeledItem);
            previous_node = Utility.getPrevNode(list_nodes, unlabeledItem);
            next_node = Utility.getNextNode(list_nodes, unlabeledItem);

            Rect bounds = new Rect();
            unlabeledItem.getBoundsInScreen(bounds);
            String content = unlabeledItem.getViewIdResourceName();
            if (content != null) {
                // ViewResourceId
                content = content.substring(content.lastIndexOf(":id/") + 4);
            } else {
                content = "No ID";
            }
            if (alt_text.length() > 0) content = alt_text;
            overlay_content = content;
            generatedButton = addOverlay(context, content, bounds);
        }
    }

    public static class annotationObject {
        public String overlay_content;
        public Button generatedButton;
        public AccessibilityNodeInfo previous_node;
        public AccessibilityNodeInfo next_node;
        public annotationObject(Context context,
                                AccessibilityNodeInfo anchor_node,
                                ArrayList<AccessibilityNodeInfo> list_nodes,
                                Rect bounds,
                                String content,
                                View.OnClickListener clickListener) {
            overlay_content = content;
            previous_node = Utility.getPrevNode(list_nodes, anchor_node);
            next_node = anchor_node;
            generatedButton = addOverlay(context, content, bounds);
            generatedButton.setOnClickListener(clickListener);
        }
    }

    public static void removeOverlays(Context context) {
        if (Utility.list_overlays.size() == 0) return;
        for (View overlay : Utility.list_overlays) {
            Utility.window_manager.removeView(overlay);
        }
        Utility.list_overlays = new ArrayList<>();
    }

    public static void removeDetailOverlays(Context context) {
        if (Utility.list_detail_overlays.size() == 0) return;
        for (View overlay : Utility.list_detail_overlays) {
            Utility.window_manager.removeView(overlay);
        }
        Utility.list_detail_overlays = new ArrayList<>();
    }

    private static Button addOverlay(Context context, String contentDescription, Rect rect) {
        LinearLayout overlay = new LinearLayout(context);
        overlay.setOrientation(LinearLayout.VERTICAL);
        overlay.setBackgroundColor(Color.argb(150, 0, 180, 0));
        overlay.setAlpha((float) 0.3);
        WindowManager.LayoutParams temp_params = new WindowManager.LayoutParams(
                rect.width(),
                rect.height(),
                rect.left,
                rect.top - statusBarHeight,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        temp_params.gravity = Gravity.TOP | Gravity.LEFT;
        window_manager.addView(overlay, temp_params);
        list_overlays.add(overlay);

        Button temp_previous = new Button(context);
        temp_previous.setContentDescription("prev");
        temp_previous.setLayoutParams(new LinearLayout.LayoutParams(1, 1));
        temp_previous.setPadding(0, 0, 0, 0);

        Button temp_next = new Button(context);
        temp_next.setContentDescription("next");
        temp_next.setLayoutParams(new LinearLayout.LayoutParams(1, 1));
        temp_next.setPadding(0, 0, 0, 0);

        Button temp_Button = new Button(context);
        temp_Button.setPadding(0, 0, 0, 0);
        temp_Button.setBackgroundColor(Color.argb(0, 0, 0, 0));
        temp_Button.setText(contentDescription);
        /*
        temp_Button.setTextColor(Color.BLACK);
        temp_Button.setTypeface(temp_Button.getTypeface(),Typeface.BOLD);
        */
        temp_Button.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, rect.height() - 2));

        overlay.addView(temp_previous);
        overlay.addView(temp_Button);
        overlay.addView(temp_next);

        return temp_Button;
    }


    public static View addImageViewOverlay(Context context, Rect bounds) {
        ImageView image_view = new ImageView(context);
        image_view.setImageResource(R.drawable.gesture);
        image_view.setBackgroundColor(Color.argb(200, 0, 180, 0));
        WindowManager.LayoutParams temp_params = new WindowManager.LayoutParams(
                bounds.width(),
                bounds.height(),
                bounds.left,
                bounds.top - statusBarHeight,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        temp_params.gravity = Gravity.TOP | Gravity.LEFT;
        Utility.window_manager.addView(image_view, temp_params);
        Utility.list_overlays.add(image_view);
        return image_view;
    }

    public static View addStencilOverlay(Context context, Rect bounds) {
        OverlayWithHoleImageView overlay = new OverlayWithHoleImageView(context, null);
        bounds.top -= statusBarHeight;
        bounds.bottom -= statusBarHeight;
        overlay.setRect(bounds);

        //overlay.setBackgroundColor(Color.argb(200, 0, 0, 0));
        WindowManager.LayoutParams temp_params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                0,
                0,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        temp_params.gravity = Gravity.TOP | Gravity.LEFT;
        Utility.window_manager.addView(overlay, temp_params);
        Utility.list_overlays.add(overlay);
        return overlay;
    }

    public static View addButtonsViewOverlay(Context context,
                                             ArrayList<Button> buttons,
                                             String overlay_content_description) {
        LinearLayout overlay = new LinearLayout(context);
        overlay.setBackgroundColor(Color.argb(200, 255, 0, 0));
        overlay.setContentDescription(overlay_content_description);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                0,
                0,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        for (Button button : buttons) {
            overlay.addView(button);
        }
        window_manager.addView(overlay, params);
        return overlay;
    }

    public static View addListViewOverlayStore(Context context,
                                          String[] list_content,
                                          String overlay_content_description) {
        ListView overlay = new ListView(context);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (context, android.R.layout.simple_list_item_1, list_content){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                /// Get the Item from ListView
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view.findViewById(android.R.id.text1);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = 80;
                view.setLayoutParams(params);
                return view;
            }
        };

        //ArrayAdapter<String> list_adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_2, android.R.id.text1, list_content);
        overlay.setAdapter(arrayAdapter);
        overlay.setBackgroundColor(Color.argb(255, 0, 180, 0));
        overlay.setContentDescription(overlay_content_description);
        overlay.setDivider(null);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                0,
                0,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        window_manager.addView(overlay, params);
        return overlay;
    }

    public static View addListViewOverlay(Context context,
                                          String[] list_content,
                                          String overlay_content_description) {
        ListView overlay = new ListView(context);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (context, android.R.layout.simple_list_item_1, list_content){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                /// Get the Item from ListView
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view.findViewById(android.R.id.text1);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40);
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = 220;
                view.setLayoutParams(params);
                return view;
            }
        };

        //ArrayAdapter<String> list_adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_2, android.R.id.text1, list_content);
        overlay.setAdapter(arrayAdapter);
        overlay.setBackgroundColor(Color.argb(255, 0, 180, 0));
        overlay.setContentDescription(overlay_content_description);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                0,
                0,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        window_manager.addView(overlay, params);
        return overlay;
    }

    public static LinearLayout addSuppleOverlay(Context context, String overlay_content_description) {
        LinearLayout overlay = new LinearLayout(context);
        overlay.setBackgroundColor(Color.rgb(240, 240, 240));
        overlay.setContentDescription(overlay_content_description);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                0,
                0,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        window_manager.addView(overlay, params);
        return overlay;
    }


    public static void addFBPostOverlay(Context context) {
        FB_post_overlay = new ListView(context);
        FB_list_adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, android.R.id.text1, list_post);
        FB_post_overlay.setAdapter(FB_list_adapter);
        FB_post_overlay.setBackgroundColor(Color.argb(200, 0, 255, 0));
        WindowManager.LayoutParams temp_params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                800,
                0,
                0,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        temp_params.gravity = Gravity.TOP | Gravity.LEFT;

        window_manager.addView(FB_post_overlay, temp_params);
    }


    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


    public static void clickAtPosition(int x, int y, AccessibilityNodeInfo node) {
        if (node == null) return;

        if (node.getChildCount() == 0) {
            Rect buttonRect = new Rect();
            node.getBoundsInScreen(buttonRect);
            if (buttonRect.contains(x, y)) {
                // Maybe we need to think if a large view covers item?
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.i("Xiaoyi", node.toString());
            }
        } else {
            Rect buttonRect = new Rect();
            node.getBoundsInScreen(buttonRect);
            if (buttonRect.contains(x, y)) {
                // Maybe we need to think if a large view covers item?
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.i("Xiaoyi", node.toString());
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                clickAtPosition(x, y, node.getChild(i));
            }
        }
    }


    public static String getIdOfNode(AccessibilityNodeInfo info) {
        String s = info.toString();
        int start_id = s.indexOf("AccessibilityNodeInfo@") + 22;
        return s.substring(start_id, start_id + 8);
    }


    public static void refreshListOfNodes(ArrayList<AccessibilityNodeInfo> list_nodes,
                                          AccessibilityNodeInfo info) {
        if (info == null) return;
        if (info.getChildCount() == 0) {
            list_nodes.add(info);
        } else {
            list_nodes.add(info);
            for (int i = 0; i < info.getChildCount(); i++) {
                refreshListOfNodes(list_nodes, info.getChild(i));
            }
        }
    }


    public static void printAllNodes(AccessibilityNodeInfo root) {
        ArrayList<AccessibilityNodeInfo> list_nodes = new ArrayList<>();
        refreshListOfNodes(list_nodes, root);
        for (AccessibilityNodeInfo info : list_nodes) {
            Log.i("Xiaoyi", info.toString());
            //Log.i("Xiaoyi", info.getClassName().toString());
        }
    }


    public static int getNodeIndex(ArrayList<AccessibilityNodeInfo> list_nodes,
                                   AccessibilityNodeInfo info) {
        for (int i=0; i<list_nodes.size(); i++) {
            if (list_nodes.get(i).equals(info)) return i;
        }
        return -1;
    }


    public static AccessibilityNodeInfo getPrevNode(ArrayList<AccessibilityNodeInfo> list_nodes,
                                                    AccessibilityNodeInfo info) {
        int node_index = getNodeIndex(list_nodes, info);
        if (node_index == -1) return null;
        if (node_index == 0) {
            return list_nodes.get(list_nodes.size() - 1);
        } else {
            return list_nodes.get(node_index - 1);
        }
    }


    public static AccessibilityNodeInfo getNextNode(ArrayList<AccessibilityNodeInfo> list_nodes,
                                                    AccessibilityNodeInfo info) {
        int node_index = getNodeIndex(list_nodes, info);
        if (node_index == -1) return null;
        if (node_index == list_nodes.size() - 1) {
            return list_nodes.get(0);
        } else {
            return list_nodes.get(node_index + 1);
        }
    }


    public static void findUnlabeledItems(Context context,
                                          AccessibilityNodeInfo info,
                                          ArrayList<AccessibilityNodeInfo> list_nodes,
                                          ArrayList unlabeledItems) {
        if (info == null) return;
        if (info.getChildCount() == 0) {
            if (info.getClassName().equals("android.view.View")) return;
            if (info.getText() == null && info.getContentDescription() == null) {
                String viewId = info.getViewIdResourceName();
                switch (tab_num) {
                    case 0:
                        unlabeledItems.add(new unlabeledObject(context, "Home", info, list_nodes));
                        break;
                    case 1:
                        unlabeledItems.add(new unlabeledObject(context, "Read", info, list_nodes));
                        break;
                    case 2:
                        unlabeledItems.add(new unlabeledObject(context, "Plan", info, list_nodes));
                        break;
                    case 3:
                        unlabeledItems.add(new unlabeledObject(context, "Me", info, list_nodes));
                        break;
                }
                tab_num++;
                if (viewId != null) {
                    viewId = viewId.substring(viewId.lastIndexOf(":id/") + 4);
                    if (viewId.equals("btn_settings"))
                        unlabeledItems.add(new unlabeledObject(context, "Settings", info, list_nodes));
                    if (viewId.equals("btn_image_share"))
                        unlabeledItems.add(new unlabeledObject(context, "Image Share", info, list_nodes));
                    if (viewId.equals("btn_share"))
                        unlabeledItems.add(new unlabeledObject(context, "Share", info, list_nodes));
                }
            }
        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                findUnlabeledItems(context, info.getChild(i), list_nodes, unlabeledItems);
            }
        }
    }

    public static AccessibilityNodeInfo findItemByClass(AccessibilityNodeInfo info,
                                                        String class_name) {
        ArrayList<AccessibilityNodeInfo> list_nodes = new ArrayList<>();
        refreshListOfNodes(list_nodes, info);
        for (AccessibilityNodeInfo node : list_nodes) {
            Log.i("Xiaoyi", node.getClassName().toString());
            if (node.getClassName().toString().equals(class_name)) {
                Log.i("Xiaoyi", node.getClassName().toString());
                return info;
            }
        }
        return null;
    }

    public static AccessibilityNodeInfo findItemByContentDescription(AccessibilityNodeInfo root,
                                                                     String contentDescription) {
        ArrayList<AccessibilityNodeInfo> list_nodes = new ArrayList<>();
        Utility.refreshListOfNodes(list_nodes, root);
        for (AccessibilityNodeInfo node : list_nodes) {
            if (node.getContentDescription() != null && node.getContentDescription().toString().equals(contentDescription)) {
                return node;
            }
        }
        return null;
    }

    public static void findScrollableItem(AccessibilityNodeInfo info,
                                          ArrayList<AccessibilityNodeInfo> list_scrollable_nodes) {
        if (info == null) return;
        if (info.isScrollable()) {
            list_scrollable_nodes.add(info);
        }
        if (info.getChildCount() != 0) {
            for (int i = 0; i < info.getChildCount(); i++) {
                findScrollableItem(info.getChild(i), list_scrollable_nodes);
            }
        }
    }

    public static void findPost(AccessibilityNodeInfo info) {
        if (info == null) return;
        if (info.getChildCount() != 0) {
            if (info.getViewIdResourceName() != null && info.getClassName().equals("android.view.View") && info.getViewIdResourceName().equals("com.facebook.katana:id/feed_story_message")) {
                list_post.add(info.getContentDescription().toString());
            }
            for (int i = 0; i < info.getChildCount(); i++) {
                findPost(info.getChild(i));
            }
        }
    }


    public static class viewInfo {
        public String overlay_content;
        public Button generatedButton;
        public AccessibilityNodeInfo previous_node;
        public AccessibilityNodeInfo next_node;
        public viewInfo(Context context,
                                AccessibilityNodeInfo anchor_node,
                                ArrayList<AccessibilityNodeInfo> list_nodes,
                                Rect bounds,
                                String content,
                                View.OnClickListener clickListener) {
        }
    }


    public static Bitmap getScreenShot() {
        //Long starttime = System.currentTimeMillis();
        final Image image = Utility.image_reader.acquireLatestImage();
        if (image == null) return null;
        if (image.getPlanes() == null) return null;
        Image.Plane plane = image.getPlanes()[0];
        if (plane == null) return null;
        final ByteBuffer buffer = plane.getBuffer();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final int pixelStride = plane.getPixelStride();
        final int rowStride = plane.getRowStride();
        final int rowPadding = rowStride - pixelStride * width;
        Utility.compensate_width = rowPadding / pixelStride;
        final Bitmap bmp = Bitmap.createBitmap(width + Utility.compensate_width, height, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(buffer);
//        final Bitmap bitmap = Bitmap.createScaledBitmap(bmp, bmp.getWidth() / 2, bmp.getHeight() / 2, false);
        // bmp.recycle();
        //Long endtime = System.currentTimeMillis();
        //Log.i("Xiaoyi", String.valueOf(endtime - starttime));
        image.close();
        return bmp;
    }


    public static GestureDescription gestureTwoFingerClick(int center_x, int center_y, int delay_ms){
        Path scroll = new Path();
        scroll.moveTo(center_x-3, center_y);
        scroll.lineTo(center_x-3, center_y+5);

        Path scroll2 = new Path();
        scroll2.moveTo(center_x + 3, center_y - 3);

        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(scroll, delay_ms, 300);
        GestureDescription.StrokeDescription stroke2 = new GestureDescription.StrokeDescription(scroll2, delay_ms, 300);
        GestureDescription.Builder gesture_builder = new GestureDescription.Builder();
        gesture_builder.addStroke(stroke);
        gesture_builder.addStroke(stroke2);
        GestureDescription gest_descript = gesture_builder.build();

        return gest_descript;
    }

    public static AccessibilityNodeInfo getNodeByViewId(AccessibilityNodeInfo rootNode, String viewId) {
        if (rootNode == null) { return null; }
        List<AccessibilityNodeInfo> results = rootNode.findAccessibilityNodeInfosByViewId(viewId);
        if (results.size() == 0) {
            return null;
        }
        return results.get(0);
    }

    public static View addListViewOverlayWellsFargo(Context context,
                                          WindowManager window_manager,
                                          String[] list_content,
                                          String overlay_content_description,
                                          final Rect bounds) {
        ListView overlay = new ListView(context);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (context, android.R.layout.simple_list_item_1, list_content) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                /// Get the Item from ListView
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view.findViewById(android.R.id.text1);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 1);
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = (bounds.height()-70)/11;
                view.setLayoutParams(params);
                return view;
            }
        };

        overlay.setAdapter(arrayAdapter);
        overlay.setBackgroundColor(Color.argb(50, 0, 255, 0));
        overlay.setContentDescription(overlay_content_description);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                bounds.width(),
                bounds.height(),
                bounds.left,
                bounds.top,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        window_manager.addView(overlay, params);
        return overlay;
    }

    public static View addYelpStarView(Context context,
                                       WindowManager window_manager,
                                       Rect bounds,
                                       View.OnClickListener clickListener,
                                       int current_star) {
        LinearLayout overlay = new LinearLayout(context);
        overlay.setBackgroundColor(Color.argb(30, 255, 0, 0));
        overlay.setContentDescription(current_star + " Star Selected");

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                bounds.width(),
                bounds.height(),
                bounds.left,
                bounds.top - statusBarHeight,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        window_manager.addView(overlay, params);

        View star_1 = new View(context);
        View star_2 = new View(context);
        View star_3 = new View(context);
        View star_4 = new View(context);
        View star_5 = new View(context);

        ArrayList<View> stars = new ArrayList();
        stars.add(star_1);
        stars.add(star_2);
        stars.add(star_3);
        stars.add(star_4);
        stars.add(star_5);

        /*
        star_1.setContentDescription("1 Star");
        star_2.setContentDescription("2 Stars");
        star_3.setContentDescription("3 Stars");
        star_1.setContentDescription("4 Stars");
        star_1.setContentDescription("5 stars");
        */


        for (int i = 0; i < stars.size(); i++) {
            View star = stars.get(i);
            star.setLayoutParams(new LinearLayout.LayoutParams(bounds.width() / 5, bounds.height()));
            star.setBackgroundColor(Color.argb(i * 10, 0, 255, 0));
            star.setContentDescription((i + 1) + " Star");
            star.setClickable(true);
            star.setOnClickListener(clickListener);
            overlay.addView(star, i);
        }

        return overlay;
    }

    public void getVersion(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = pm.getPackageInfo(packageName, 0);
            Log.i("Xiaoyi", "VersionName: "+info.versionName);
            Log.i("Xiaoyi", "VersionCode: "+info.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    static public class WindowTreeNode {
        private List<WindowTreeNode> children = new ArrayList<WindowTreeNode>();
        private AccessibilityNodeInfo accessibilityNode = null;
        private Rect boundsInParent = new Rect();
        private Rect boundsInScreen = new Rect();
        private String className = null;
        private String text = null;
        private String contentDescription = null;
        private String viewIdResName = null;

        public WindowTreeNode(AccessibilityNodeInfo node) {
            this.setData(node);
        }

        public List<WindowTreeNode> getChildren() {
            return children;
        }

        public WindowTreeNode addChild(AccessibilityNodeInfo data) {
            WindowTreeNode child = new WindowTreeNode(data);
            this.children.add(child);
            return child;
        }

        public AccessibilityNodeInfo getData() {
            return this.accessibilityNode;
        }

        public void printData() {
            Log.i("Xiaoyi", "" + this.className + ", " + this.text + ", " + this.contentDescription + ", " + this.viewIdResName);
        }

        public void setData(AccessibilityNodeInfo node) {
            this.accessibilityNode = node;
            node.getBoundsInParent(this.boundsInParent);
            node.getBoundsInScreen(this.boundsInScreen);
            if (node.getClassName()          != null) { this.className          = node.getClassName().toString(); }
            if (node.getText()               != null) { this.text               = node.getText().toString(); }
            if (node.getContentDescription() != null) { this.contentDescription = node.getContentDescription().toString(); }
            if (node.getViewIdResourceName() != null) { this.viewIdResName      = node.getViewIdResourceName().toString(); }
        }

        public boolean isLeaf() {
            if (this.children.size() == 0)
                return true;
            else
                return false;
        }
    }

    public static ArrayList<WindowTree> list_windowTree = new ArrayList<>();

    public static class WindowTree {
        public int id = -1;
        public String packageName = "";
        public WindowTreeNode root;
        public WindowTree(AccessibilityNodeInfo rootAccessibilityNode) {
            id = (int) new Date().getTime();
            if (rootAccessibilityNode.getPackageName() != null) {
                packageName = rootAccessibilityNode.getPackageName().toString();
            }

            root = new WindowTreeNode(rootAccessibilityNode);
            createTree(rootAccessibilityNode, root);
        }

        private void createTree(AccessibilityNodeInfo accessibilityNode, WindowTreeNode node) {
            if (accessibilityNode == null) return;
            node.setData(accessibilityNode);
            for (int i = 0; i < accessibilityNode.getChildCount(); i++) {
                createTree(accessibilityNode.getChild(i), node.addChild(accessibilityNode.getChild(i)));
            }
        }

        private void printTree(WindowTreeNode node) {
            node.printData();
            for (WindowTreeNode child: node.getChildren()) {
                printTree(child);
            }
        }
    }

    static public int countSimilarity(WindowTreeNode node1, WindowTreeNode node2) {
        if (node1.className.equals(node2.className)) {
            int sum = 1;
            for (int i = 0; i < Math.min(node1.children.size(), node2.children.size()); i++) {
                sum += countSimilarity(node1.children.get(i), node2.children.get(i));
            }
            return sum;
        } else {
            return 0;
        }
    }

    static public void getWindowId(AccessibilityNodeInfo root) {
        if (root == null) return;

        WindowTree newTree = new WindowTree(root);
        int selfSimilarity = countSimilarity(newTree.root, newTree.root);
        int maxSimilarity = -1;
        WindowTree maxTree = null;
        for (WindowTree tree: list_windowTree) {
            if (tree.packageName.equals(newTree.packageName)) {
                int similarity = countSimilarity(newTree.root, tree.root);
                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    maxTree = tree;
                }
            }
        }
        // If no tree 90% similar, let's create a new tree
        if (maxSimilarity < selfSimilarity * 9/10) {
            list_windowTree.add(newTree);
            Log.i("Xiaoyi", "New: " + newTree.packageName + ", " + newTree.id + ", " + selfSimilarity);
        } else {
            Log.i("Xiaoyi", "Old: " + maxTree.packageName + ", " + maxTree.id + ", " + maxSimilarity);
        }
    }
}
