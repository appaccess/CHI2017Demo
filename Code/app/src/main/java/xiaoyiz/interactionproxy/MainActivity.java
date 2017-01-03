package xiaoyiz.interactionproxy;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.media.ImageReader;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {

	public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE= 5469;
	public static DisplayMetrics display_metrics;
	public static Bitmap zoomed_screenshot_bitmap;
	public static Boolean zoomed_in = false;
	public static int start_x = 0;
	public static int start_y = 0;

	@TargetApi(23)
	public void testOverlayPermission() {
		if (!Settings.canDrawOverlays(this)) {
			Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
					Uri.parse("package:" + getPackageName()));
			startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
		}
	}

	@TargetApi(23)
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
			if (Settings.canDrawOverlays(this)) {
				System.out.println("WowICan");
			}
		} else {
			System.out.println("Intent");
			display_metrics = new DisplayMetrics();
			Utility.window_manager = (WindowManager) getSystemService(WINDOW_SERVICE);
			Utility.window_manager.getDefaultDisplay().getMetrics(display_metrics);
			Utility.image_reader = ImageReader.newInstance(Utility.device_w, Utility.device_h, PixelFormat.RGBA_8888, 2);

			Utility.media_projection = Utility.media_projection_manager.getMediaProjection(Activity.RESULT_OK, data);
			Utility.virtual_display = Utility.media_projection.createVirtualDisplay("capingress", Utility.device_w, Utility.device_h, display_metrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, Utility.image_reader.getSurface(), null, null);

			Utility.screencast_view = new ImageView(this);
			//Utility.screencast_view.setBackgroundColor(Color.argb(100, 255, 0, 0));
			WindowManager.LayoutParams temp_params = new WindowManager.LayoutParams(
					WindowManager.LayoutParams.MATCH_PARENT,
					WindowManager.LayoutParams.MATCH_PARENT,
					0,
					0,
					WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
					WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
					PixelFormat.TRANSLUCENT);
			temp_params.gravity = Gravity.TOP | Gravity.LEFT;
			Utility.window_manager.addView(Utility.screencast_view, temp_params);

			final Context context = this;
			final View upper_view = new View(context);
			final View bottom_view = new View(context);

			Utility.screencast_view.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						if (!zoomed_in) {
							zoomed_in = true;
							Utility.screenshot_bitmap = Utility.getScreenShot();

							upper_view.setBackgroundColor(Color.BLACK);
							upper_view.setAlpha((float) 0.9);
							WindowManager.LayoutParams temp_params_1 = new WindowManager.LayoutParams(
									WindowManager.LayoutParams.MATCH_PARENT,
									320,
									0,
									0,
									WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
									WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
									PixelFormat.TRANSLUCENT);
							temp_params_1.gravity = Gravity.TOP | Gravity.LEFT;
							Utility.window_manager.addView(upper_view, temp_params_1);

							bottom_view.setBackgroundColor(Color.BLACK);
							bottom_view.setAlpha((float) 0.9);
							WindowManager.LayoutParams temp_params_2 = new WindowManager.LayoutParams(
									WindowManager.LayoutParams.MATCH_PARENT,
									320,
									0,
									1500,
									WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
									WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
									PixelFormat.TRANSLUCENT);
							temp_params_2.gravity = Gravity.TOP | Gravity.LEFT;
							Utility.window_manager.addView(bottom_view, temp_params_2);

							if (Utility.screenshot_bitmap != null) {
								float zoom_rate = 2;
								int side = (int)(Utility.device_w/zoom_rate);
								int bitmap_w = Utility.screenshot_bitmap.getWidth();
								int bitmap_h = Utility.screenshot_bitmap.getHeight();
								start_x = (int) event.getX() - side/2;
								start_y = (int) event.getY() - side/2 + Utility.statusBarHeight;
								if (start_x < 0) start_x = 0;
								if (start_y < 0) start_y = 0;
								if (start_x + side > bitmap_w - Utility.compensate_width) start_x = bitmap_w - Utility.compensate_width - side;
								if (start_y + side > bitmap_h) start_y = bitmap_h - side;
								zoomed_screenshot_bitmap = Bitmap.createBitmap(Utility.screenshot_bitmap, start_x, start_y, side, side);
								Utility.screencast_view.setImageBitmap(zoomed_screenshot_bitmap);
							}
						} else {
							zoomed_in = false;

							Utility.window_manager.removeView(upper_view);
							Utility.window_manager.removeView(bottom_view);

							int top_divider = (Utility.device_h - Utility.device_w)/2 - Utility.statusBarHeight;
							if (event.getY() < top_divider || event.getY() > top_divider + Utility.device_w) {
								// Touch outside zoomed in area.
								Utility.screencast_view.setImageBitmap(null);
							} else {
								// Almost perfect...don't waste too much time on math :)
								Utility.final_x = start_x + (int)(event.getX()/2);
								Utility.final_y = start_y + (int)((event.getY() - top_divider)/2);
								Utility.screencast_view.setImageBitmap(null);
								// In order to trigger Accessibility Event...
								Utility.screencast_view.setSelected(true);
								Utility.screencast_view.setSelected(false);
							}
						}
					}
					return true;
				}
			});
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Utility.statusBarHeight = Utility.getStatusBarHeight(this);

		if (Build.VERSION.SDK_INT >= 23){
			testOverlayPermission();
		}

        this.findViewById(R.id.activateAccessibilityButton).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
			}
		});

		Utility.media_projection_manager = (MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        this.findViewById(R.id.openAppButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivityForResult(Utility.media_projection_manager.createScreenCaptureIntent(), 1);
			}
		});

	}

    @Override
    protected void onResume() {
		super.onResume();
	}
}
