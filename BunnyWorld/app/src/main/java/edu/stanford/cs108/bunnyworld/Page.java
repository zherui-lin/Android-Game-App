package edu.stanford.cs108.bunnyworld;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Page implements Serializable {
    private String name;
    private static Paint backGround;
    private static Paint frontPaint;
    private List<Shape> shapeList;
    private List<Shape> onDropList;
    private ValueAnimator fadeAnimator;

    private static PageView pageView;

    static{
        backGround = new Paint();
        backGround.setColor(Color.WHITE);
        frontPaint = new Paint();
        frontPaint.setColor(Color.WHITE);
        frontPaint.setAlpha(0);
    }

    public Page(String name) {
        this.name = name;
        shapeList = new ArrayList<Shape>();
        onDropList = new ArrayList<Shape>();
    }

    public static void setPageView(PageView pageView) {
        Page.pageView = pageView;
    }

    /**
     * If no name is given, the default Page will be named "pageX", where X is the
     * current count of pages
     */
    public Page(int count) {
        this("page" + String.valueOf(count));
    }

    public void addShape(Shape shape) {
        shapeList.add(shape);
        for (Script script : shape.getScriptList()) {
            if (script.getTrigger().equals("ondrop")) {
                onDropList.add(shape);
            }
        }
    }

    /**
     * This function removes the given shape from shapeList and
     * onDropList
     * @param shape
     */
    public void removeShape(Shape shape) {
        shapeList.remove(shape);
        onDropList.remove(shape);
    }

    /**
     * This function returns the shape by index.
     * @param i
     * @return Shape at the ith index of shapeList
     */
    public Shape getShape(int i) {
        return shapeList.get(i);
    }

    /**
     * This function returns the shape by name.
     * @param name
     * @return Shape with given name
     */
    @Nullable
    public Shape getShape(String name) {
        for (Shape shape : shapeList) {
            if (shape.getName().equalsIgnoreCase(name)) {
                return shape;
            }
        }
        return null;
    }

    public void draw(@NonNull Canvas canvas) {
        canvas.drawPaint(backGround);
        for (Shape shape: this.shapeList) {
            shape.draw(canvas);
        }
        canvas.drawPaint(frontPaint);
    }

    public void changeFrontPaint(int Alpha) {
        frontPaint.setAlpha(Alpha);

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Shape> getShapeList() {
        return shapeList;
    }

    public List<Shape> getOnDropList() {
        return onDropList;
    }

    public void startFadingOut() {
        fadeAnimator = new ValueAnimator();
        fadeAnimator = ValueAnimator.ofInt(0, 255);
        fadeAnimator.setDuration(Game.SWITCH_DELAY);
        fadeAnimator.setRepeatCount(0);
        fadeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int alpha = (int) fadeAnimator.getAnimatedValue();
                changeFrontPaint(alpha);
                if (pageView != null) {
                    pageView.invalidate();
                }
            }
        });
        fadeAnimator.start();
    }

    protected void startFadingIn() {
        fadeAnimator = new ValueAnimator();
        fadeAnimator = ValueAnimator.ofInt(255, 0);
        fadeAnimator.setDuration(Game.SWITCH_DELAY);
        fadeAnimator.setRepeatCount(0);
        fadeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int alpha = (int) fadeAnimator.getAnimatedValue();
                changeFrontPaint(alpha);
                if (pageView != null) {
                    pageView.invalidate();
                }
            }
        });
        fadeAnimator.start();
    }
}
