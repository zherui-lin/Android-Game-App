package edu.stanford.cs108.bunnyworld;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ShapeShadow extends View.DragShadowBuilder {

    private transient Game game = Game.getCurrentGame();

    private Shape shape;
    private String text;
    private Drawable shadow;

    private float x;
    private float y;

    private static final int DEFAULT_SELECT_COLOR = Color.RED;

    public static final int PAGE = 0;
    public static final int POSSESSION = 1;

    /**
     * The constructor takes in four parameters
     * @param view the current view
     * @param x
     * @param y
     */
    public ShapeShadow(View view, float x, float y) {
        // Get the shape according to the area parameter
        shape = game.getSelectShape();

        Bitmap image = shape.getBitmap();
        text = shape.getText();

        if (!text.equals("")) {
            // Create a drawable object for text
            shadow = new Drawable() {
                Paint paint = new TextPaint();
                @Override
                public void draw(@NonNull Canvas canvas) {
                    init();
                    canvas.drawText(text,0, getBounds().height(), paint);
                }
                @Override
                public void setAlpha(int i) {
                    paint.setAlpha(i);
                }
                @Override
                public void setColorFilter(@Nullable ColorFilter colorFilter) {
                }
                @Override
                public int getOpacity() {
                    return PixelFormat.UNKNOWN;
                }
                private void init() {
                    paint.setTextSize(shape.getFontSize());
                    paint.setColor(Color.BLACK);
                    paint.setShadowLayer(2,0,0,DEFAULT_SELECT_COLOR);
                }
            };
        } else if (image != null) {
            shadow = new BitmapDrawable(Resources.getSystem(), image);
        } else {
            shadow = new ColorDrawable(Color.LTGRAY);
        }

        shadow.setColorFilter(DEFAULT_SELECT_COLOR, PorterDuff.Mode.LIGHTEN);

        this.x = x;
        this.y = y;
    }

    @Override
    public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
        int width = (int) (shape.getRight() - shape.getLeft());
        int height = (int) (shape.getBottom() - shape.getTop());
        shadow.setBounds(0,0, width, height);
        outShadowSize.set(width, height);
        outShadowTouchPoint.set((int)(x - shape.getLeft()), (int)(y - shape.getTop()));
    }

    @Override
    public void onDrawShadow(Canvas canvas) {
        shadow.draw(canvas);
    }
}
