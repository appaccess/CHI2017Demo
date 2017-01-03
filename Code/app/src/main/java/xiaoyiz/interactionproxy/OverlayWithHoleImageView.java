package xiaoyiz.interactionproxy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class OverlayWithHoleImageView extends ImageView {

    private Rect rect;

    public OverlayWithHoleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //In versions > 3.0 need to define layer Type
        if (android.os.Build.VERSION.SDK_INT >= 11)
        {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    public void setRect(Rect rect) {
        this.rect = rect;
        //Redraw
        postInvalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(rect != null) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.argb(180, 0, 0, 0));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPaint(paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawRect(rect, paint);
        }
    }
}