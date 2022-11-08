package edu.stanford.cs108.bunnyworld;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class PossessionView extends View {
    // how to get PossesionView top and botton
    private static transient Game game = Game.getCurrentGame();

    float DRAW_SIZE = 5.0f;
    Paint drawPaint;

    static int viewWidth, viewHeight;
    static float toX, toY; // for draw moved shape to page view

    int gridSize;
    int capacity;

    Possession possession;
    protected float x;
    protected float y;
    private float offL, offR, offT, offB;
    int currentPosition;

    public PossessionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        drawPaint = new Paint();
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeWidth(DRAW_SIZE);
        drawPaint.setColor(Color.BLACK);

        possession = game.getPossession();
        gridSize = possession.getGridSize();
        capacity = possession.getCapacity();

        PageView.setPossessionView(this);
//        viewWidth = (int) this.getWidth();
//        viewHeight = (int) this.getHeight();
    }

    public static void setCurrentGame() {
        game = Game.getCurrentGame();
    }

    //onSizeChanged copied from Lecture 11 sample code - SimpleDrawView.java
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        possession.draw(canvas, drawPaint);
    }


    private int findPosition(float currentX, float currentY) {
        for (int i = 0; i < capacity; i++) {
            if (currentX >= i * gridSize && currentX < (i + 1) * gridSize) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Create a gesture detector to handle difference between a short click and long click.
     */
    final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        // Find select shape when a press is detect
        Shape currentShape;
        @Override
        public boolean onDown(MotionEvent event) {
            PossessionView.this.x = event.getX();
            PossessionView.this.y = event.getY();
            game.setSelectShape(null);
            currentPosition = findPosition(x, y);
            possession.setSelectShape(currentPosition, y);
            Shape currentShape = game.getSelectShape();
//            Toast.makeText(getContext(), currentPosition + "Clicked", Toast.LENGTH_SHORT).show();

            if (currentShape == null) {
                return false;
            }

            invalidate();
            return true;
        }
        // React to single tap
        @Override
        public boolean onSingleTapUp(MotionEvent event) {

            //Toast.makeText(getContext(), "Clicked", Toast.LENGTH_SHORT).show();
            return super.onSingleTapUp(event);
        }

        // React to a long press (start drag event)
        @Override
        public void onLongPress(MotionEvent event) {
//            Shape currentShape = Page.getCurrentPage().getSelectShape();
            PossessionView.this.x = event.getX();
            PossessionView.this.y = event.getY();
            currentPosition = findPosition(x, y);
            possession.setSelectShape(currentPosition, y);
            Shape currentShape = game.getSelectShape();
            if (currentShape == null) {
                return;
            }
            if (currentShape.getMovable() && currentShape.getVisible()) {
                //TODO: review this

                // Add possession index and original (x,y) to ClipData
                ClipData data = ClipData.newPlainText("index", String.valueOf(currentPosition));
                String offset = String.valueOf(x) + " " + String.valueOf(y);
                ClipData.Item offsetItem = new ClipData.Item((CharSequence) offset);
                data.addItem(offsetItem);

                View.DragShadowBuilder shadow = new ShapeShadow(PossessionView.this, x, y);
                startDrag(data, shadow, PossessionView.this, 0);
            }
        }
    });

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDragEvent(@NonNull DragEvent event) {
//        Page current = Page.getCurrentPage();
//        Shape shape = current.getSelectShape();
//        List<Shape> shapeList = current.getShapeList();

        currentPosition = findPosition(x, y);
//        possession.setSelectShape(currentPosition);
        Page currentPage = game.getCurrentPage();
        Shape shape = game.getSelectShape();

        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                if (shape == null) {
                    return false;
                } else if (!shape.getMovable()) {
                    return false;
                }
//                findOffSet(shape, x, y);  // Find the offset needs to place new shape
                invalidate();
                break;

            case DragEvent.ACTION_DRAG_ENTERED:
            case DragEvent.ACTION_DRAG_LOCATION:
            case DragEvent.ACTION_DRAG_EXITED:
                //possession.removeShape(currentPosition);
                invalidate();
                break;

            case DragEvent.ACTION_DROP:
                this.x = event.getX();
                this.y = event.getY();
                // Get where the drop action is initiated
                View dropView = (View) event.getLocalState();
                int position = findPosition(x, y);
                if (dropView != this) {
//                    Toast.makeText(getContext(),"Drag from Page",Toast.LENGTH_SHORT).show();
                    if (possession.addShape(position, shape)) {
                        currentPage.removeShape(shape);
                    } else if (!checkValidDrop(shape, possession.getShape()[position])) {
                        return false;
                    } else {
                        onDrop(possession.getShape()[position]);
                    }
                } else {
                    if (possession.addShape(position, shape)) {
                        possession.removeShape(currentPosition);
                    } else if (!checkValidDrop(shape, possession.getShape()[position])) {
                        return false;
                    } else {
                        onDrop(possession.getShape()[position]);
                    }
                }

                if (dropView != this) {
                    currentPage.removeShape(shape);
                } else {
                    possession.removeShape(currentPosition);
                }

                invalidate();
                break;

            case DragEvent.ACTION_DRAG_ENDED: //TODO:which event trigger the start and end?
                game.setSelectShape(null);
//                Shape dropShape = findShape(x, y);
//                if (dropShape == null) {
//                    break;
//                }
//                for (Script script : dropShape.getScriptList()) {
//                    if (script.getTrigger().equals("ondrop")) {
//                        Page.startAction(script);
//                    }
//                }
                invalidate();
                break;
        }
        return true;
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
}