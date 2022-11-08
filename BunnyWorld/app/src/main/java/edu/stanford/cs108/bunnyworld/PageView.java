package edu.stanford.cs108.bunnyworld;

import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class PageView extends View {
    private static transient Game game = Game.getCurrentGame();

    public static BitmapDrawable carrotDrawable, carrotDrawable2, deathDrawable,
            duckDrawable, fireDrawable, mysticDrawable, doorDrawable;

    public static MediaPlayer carrotSound, evilLaughSound, fireSound, hooraySound,
            munchSound, munchingSound, woofSound;

    protected float x;
    protected float y;
    private float offL, offR, offT, offB;
    private ValueAnimator animator;
    public static float radius;
    private static PossessionView possessionView;

    public PageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initBitmap();
        initSound();
        startAnimation();
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(Game.SWITCH_DELAY);
        this.startAnimation(fadeIn);
        Page.setPageView(this);
    }

    public static void setPossessionView(PossessionView view) {
        possessionView = view;
    }

    public static void setCurrentGame() {
        game = Game.getCurrentGame();
    }

    private void initBitmap(){
        carrotDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.carrot);
        carrotDrawable2 = (BitmapDrawable) getResources().getDrawable(R.drawable.carrot2);
        deathDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.death);
        duckDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.duck);
        fireDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.fire);
        mysticDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.mystic);
        doorDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.door);
    }

    private void initSound(){
        carrotSound = MediaPlayer.create(getContext(), R.raw.carrotcarrotcarrot);
        evilLaughSound = MediaPlayer.create(getContext(), R.raw.evillaugh);
        fireSound = MediaPlayer.create(getContext(), R.raw.fire);
        hooraySound = MediaPlayer.create(getContext(), R.raw.hooray);
        munchSound = MediaPlayer.create(getContext(), R.raw.munch);
        munchingSound = MediaPlayer.create(getContext(), R.raw.munching);
        woofSound = MediaPlayer.create(getContext(), R.raw.woof);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int UI = View.SYSTEM_UI_FLAG_FULLSCREEN;
        this.setSystemUiVisibility(UI);
        if (game.getCurrentPage() != null) {
            game.getCurrentPage().draw(canvas);
        } else {
            game.getFirstPage().draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    /**
     * Create a gesture detector to handle difference between a short click and long click.
     */
    final GestureDetector gestureDetector = new GestureDetector(getContext(),
            new GestureDetector.SimpleOnGestureListener() {
                // Find select shape when a press is detect
                Shape currentShape;
                @Override
                public boolean onDown(MotionEvent event) {
                    PageView.this.x = event.getX();
                    PageView.this.y = event.getY();
                    selectShape(x,y);
                    currentShape = game.getSelectShape();
                    if (currentShape == null) {
                        return false;
                    }
                    return true;
                }
                // React to single tap
                @Override
                public boolean onSingleTapUp(MotionEvent event) {
                    // Add method for on click
                    if (!Game.isEditorMode()) {
                        for (Script script : currentShape.getScriptList()) {
                            if (script.getTrigger().equals("onclick")) {
                                game.startAction(script);
                                invalidate();
                            }
                        }

                        //Toast.makeText(getContext(), "Clicked", Toast.LENGTH_SHORT).show();
                    }
                    possessionView.invalidate();
                    return super.onSingleTapUp(event);
                }

                // React to a long press (start drag event)
                @Override
                public void onLongPress(MotionEvent event) {
                    PageView.this.x = event.getX();
                    PageView.this.y = event.getY();
                    selectShape(x,y);
                    Shape currentShape = game.getSelectShape();
                    if (currentShape == null) {
                        return;
                    }
                    if (currentShape.getMovable()) {
                        View.DragShadowBuilder shadow = new ShapeShadow(PageView.this, x, y);
                        startDrag(null, shadow, PageView.this, 0);
                    }
                }
            });

    @Override
    public boolean onDragEvent(@NonNull DragEvent event) {
        Page current = game.getCurrentPage();
        Shape shape = game.getSelectShape();
        Shape onDropShape = null;
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                if (shape == null) {
                    return false;
                } else if (!game.getSelectShape().getMovable()) {
                    return false;
                }
                showBoundary(shape);
                findOffSet(shape, x, y);
                invalidate();// Find the offset needs to place new shape
                break;

            case DragEvent.ACTION_DRAG_ENDED:
                hideBoundary();
            case DragEvent.ACTION_DRAG_ENTERED:
            case DragEvent.ACTION_DRAG_LOCATION:
            case DragEvent.ACTION_DRAG_EXITED:
                invalidate();
                break;

            case DragEvent.ACTION_DROP:
                this.x = event.getX();
                this.y = event.getY();
                // Get where the drop action is initiated
                View dropView = (View) event.getLocalState();
                onDropShape = findShape(x, y);

                // Check a valid drop;
                if (onDropShape != null) {
                    if (!checkValidDrop(shape, onDropShape)) {
                        return false;
                    }
                }

                if (dropView != this) {  // If is from possession
                    dropFromPossession(event, shape);
                    //Toast.makeText(getContext(),"Drag from Inventory",Toast.LENGTH_SHORT).show();
                } else {
                    // Dragging within PageView
                    dropFromPage(shape);
                }
                hideBoundary();  // Hide the green boundary after dropped
                if (onDropShape != null) {  // Initiate onDrop Script
                    onDrop(onDropShape);
                }
                //game.setSelectShape(null);  // Clear the select Shape

                invalidate();
                break;
        }
        return true;
    }

    /**
     * This function generates the reaction of PageView when the shape is dropped from
     * possession
     * @param event
     * @param shape
     */

    private void dropFromPossession(DragEvent event, Shape shape) {
        game.getCurrentPage().addShape(shape);
        float scale = shape.getPossessionScale();
        // Get Clip Data from possession for finding the new position of shape
        float[] data = getDropData(event);

        findOffSet(shape, data[1], data[2]);
        shape.changeFont(shape.getFontSize() * scale);

        scaleOffSet(scale);  // Scale the offset

        checkBoundary();
        shape.resize(x - offL, y - offT,
                x - offR, y - offB);
        // After drop, remove shape from possession
        game.getPossession().removeShape((int) data[0]);
    }

    private void dropFromPage(Shape shape) {
        game.getCurrentPage().removeShape(shape);
        game.getCurrentPage().addShape(shape);
        // Make sure the new location is within View
        checkBoundary();
        shape.resize(x - offL, y - offT,
                x - offR, y - offB);
    }


    /**
     * This function gets the ClipData from PossesionView and convert them to float
     * @param event
     * @return float[] of length 3. First element is the index of shape in possession,
     * second is the x coordinate when the drag start, and third is the y coord.
     */
    private float[] getDropData(DragEvent event) {
        ClipData index = event.getClipData();
        String possessionIndex = (String) index.getItemAt(0).getText();
        String coord = (String) index.getItemAt(1).getText();
        possessionIndex = possessionIndex + " " + coord;
        String[] dataString = possessionIndex.split(" ");
        float[] data = new float[dataString.length];
        for (int i = 0; i < data.length; i++) {
            data[i] = Float.valueOf(dataString[i]);
        }
        return data;
    }

    private void scaleOffSet(float scale) {
        offL *= scale;
        offT *= scale;
        offR *= scale;
        offB *= scale;
    }

    private boolean checkValidDrop(Shape draggingShape, Shape onDrop) {
        if (Game.isEditorMode()) {
            return false;
        } else {
            for (Script script : onDrop.getScriptList()) {
                if (script.getTrigger().equals("ondrop")
                        && script.getDropShape().equalsIgnoreCase(draggingShape.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void onDrop(Shape onDropShape) {
        if (Game.isEditorMode()) {
            return;
        }
        for (Script script : onDropShape.getScriptList()) {
            if (script.getTrigger().equals("ondrop")) {
                game.startAction(script);
            }
        }
    }


    /**
     * Shows the boundary of reacting shapes when a shape is being dragged
     * @param dropShape
     */
    private void showBoundary(Shape dropShape) {
        Page page = game.getCurrentPage();
        Shape[] possessionShapes = game.getPossession().getShape();
        List<Shape> shapeList = page.getShapeList();
        for (Shape shape : shapeList) {
            for (Script script : shape.getScriptList()) {
                if (script.getDropShape().equalsIgnoreCase(dropShape.getName())) {
                    shape.showBoundary();
                }
            }
        }
        for (Shape shape : possessionShapes) {
            if (shape != null) {
                for (Script script : shape.getScriptList()) {
                    if (script.getDropShape().equalsIgnoreCase(dropShape.getName())) {
                        shape.showBoundary();
                    }
                }
            }
        }
    }

    /**
     * Hide the boundary of shapes
     */
    private void hideBoundary() {
        Page page = game.getCurrentPage();
        for (Shape shape : page.getShapeList()) {
            shape.hideBoundary();
        }
        for (Shape shape : game.getPossession().getShape()) {
            if (shape != null) {
                shape.hideBoundary();
            }
        }
    }

    /**
     * find the shape by coordinate x, y from newest to earliest.
     * if no shape is select, return null
     * @param x
     * @param y
     */
    @Nullable
    private Shape findShape(float x, float y) {
        Page current = game.getCurrentPage();
        Shape[] possessionShapes = game.getPossession().getShape();
        List<Shape> shapeList = current.getShapeList();
        for (int i = shapeList.size() - 1; i >= 0; i--) {
            Shape shape = shapeList.get(i);
            if (shape.isClicked(x, y)) {
                return shape;
            }
        }
        for (Shape shape : possessionShapes) {
            if (shape != null) {
                if (shape.isClicked(x, y)) {
                    return shape;
                }
            }
        }
        return null;
    }

    /**
     * Set currentPage's selectShape
     * if no shape is select, set the selectShape to null.
     * @param x
     * @param y
     */
    private void selectShape(float x, float y) {
        Shape shape = findShape(x, y);
        game.setSelectShape(shape);
    }

    /**
     * find the offset of a certain points to the four boundary of a given shape
     * and parse the values to: offL, offR, offT, offB;
     * @param shape
     * @param x
     * @param y
     */
    private void findOffSet(@NonNull Shape shape, float x, float y) {
        float oldL = shape.getLeft();
        float oldR = shape.getRight();
        float oldT = shape.getTop();
        float oldB = shape.getBottom();

        offL = x - oldL;
        offR = x - oldR;
        offT = y - oldT;
        offB = y - oldB;
    }

    private void checkBoundary() {
        float top = getTop();
        float bottom = getBottom();
        float left = getLeft();
        float right = getRight();
        if (x - offL < left) {
            x = left + offL;
        }
        if (x - offR > right) {
            x = right + offR;
        }
        if (y - offT < top) {
            y = top + offT;
        }
        if (y - offB > bottom) {
            y = bottom + offB;
        }
    }

    //    reference: https://developer.android.com/guide/topics/graphics/prop-animation
    private void startAnimation() {
        animator = new ValueAnimator();
        animator = ValueAnimator.ofFloat(0.5f, 1.0f);
        animator.setDuration(2000);
        animator.setRepeatCount(100);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                radius = (float) animation.getAnimatedValue();
//                Log.i("data: radius", String.valueOf(radius));
                invalidate();
            }
        });
//        Log.i("data: page ", String.valueOf(game.getCurrentPage().getName()));
        animator.start();

    }
}
