package edu.stanford.cs108.bunnyworld;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ScriptEditorView extends View {
    private Paint blackOutlinePaint;
    private Paint blueOutlinePaint;
    private Paint textPaint;
    private Script selected;
    private Game game;
    private Shape currShape;
    private List<Script> scriptList = new ArrayList<>();

    private int screenWidth;
    private int canvasWidth;
    private int screenHeight;
    private int canvasHeight;

    public ScriptEditorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        game = Game.getCurrentGame();
        currShape = game.getSelectShape();
        scriptList = currShape.getScriptList();
        init();
    }

    private void init() {
        blackOutlinePaint = new Paint();
        blackOutlinePaint.setColor(Color.BLACK);
        blackOutlinePaint.setStyle(Paint.Style.STROKE);
        blackOutlinePaint.setStrokeWidth(5.0f);

        blueOutlinePaint = new Paint();
        blueOutlinePaint.setColor(Color.BLUE);
        blueOutlinePaint.setStyle(Paint.Style.STROKE);
        blueOutlinePaint.setStrokeWidth(15.0f);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(60);
    }

    public Script getSelected() {
        return selected;
    }

    public void setSelected(Script page) {
        selected = page;
    }

    public void clearSelected() {
        selected = null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Compute the height required to render the view
        // Assume Width will always be MATCH_PARENT.
        screenWidth = MeasureSpec.getSize(widthMeasureSpec);
        screenHeight = MeasureSpec.getSize(heightMeasureSpec);

        if(canvasHeight <= screenHeight){
            if(canvasWidth <= screenWidth){
                setMeasuredDimension(screenWidth, screenHeight);
            }
            else{
                setMeasuredDimension(canvasWidth, screenHeight);
            }
        }
        else if (canvasWidth <= screenWidth){
            setMeasuredDimension(screenWidth, canvasHeight);
        }
        else{
            setMeasuredDimension(canvasWidth, canvasHeight);
        }

    }

    private RectF getRectF(int i) {
        float recHeight = (float) (screenHeight / 9);
        float recWidth = screenWidth - 4 * recHeight;
        float left = recHeight;
        float top = recHeight * (i * 2 + 1);
        return new RectF(left, top, left + recWidth, top + recHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int UI = View.SYSTEM_UI_FLAG_FULLSCREEN;
        this.setSystemUiVisibility(UI);
        for (int i = 0; i < scriptList.size(); i++) {
            Script curr = scriptList.get(i);
            Paint outlinePaint = curr == selected ? blueOutlinePaint : blackOutlinePaint;
            String scriptString = curr.getTrigger() + " " + curr.getDropShape() + " " +
                    curr.getAction() + " " + curr.getObject() + ";";
            canvas.drawText(scriptString, getRectF(i + 1).left, getRectF(i + 1).bottom,
                    textPaint);
            canvas.drawRect(getRectF(i + 1), outlinePaint);
            if(i == scriptList.size() - 1){
                canvasHeight = (int) Math.ceil(getRectF(i + 1).bottom) + 50;
            }
        }

        if (currShape.scriptToString() != null) {
            String scriptString = currShape.scriptToString();
            canvas.drawText(scriptString, getRectF(0).left, getRectF(0).bottom, textPaint);
            canvasWidth = (int) Math.ceil(textPaint.measureText(scriptString)) + 150;
        }
        requestLayout();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX();
                float y = event.getY();
                boolean flag = false;
                for (int i = 0; i < scriptList.size(); i++) {
                    RectF r = getRectF(i + 1);
                    if (x > r.left && x < r.right && y > r.top && y < r.bottom) {
                        setSelected(scriptList.get(i));
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    clearSelected();
                }
                break;
        }
        invalidate();
        return true;
    }
}
