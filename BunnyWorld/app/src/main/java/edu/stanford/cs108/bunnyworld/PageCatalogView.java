package edu.stanford.cs108.bunnyworld;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

import java.util.List;

public class PageCatalogView extends View {

    private Paint blackOutlinePaint;
    private Paint blueOutlinePaint;
    private Paint regularTextPaint;
    private Paint boldTextPaint;
    private Page selected;
    private Game game;

    public PageCatalogView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        game = Game.getCurrentGame();
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

        regularTextPaint = new Paint();
        regularTextPaint.setColor(Color.BLACK);
        regularTextPaint.setTextSize(60);

        boldTextPaint = new Paint();
        boldTextPaint.setColor(Color.BLACK);
        boldTextPaint.setTextSize(60);
        boldTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    public Page getSelected() {
        return selected;
    }

    public void setSelected(Page page) {
        selected = page;
        game.setCurrentPage(page);
    }

    public void clearSelected() {
        selected = null;
    }

    private RectF getRectF(int i) {
        float recHeight = (float) (getHeight() / 9);
        float recWidth = (getWidth() - 4 * recHeight) / 3;
        float left = recHeight + (recHeight + recWidth) * (i / 4);
        float top = recHeight * (i % 4 * 2 + 1);
        return new RectF(left, top, left + recWidth, top + recHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int UI = View.SYSTEM_UI_FLAG_FULLSCREEN;
        this.setSystemUiVisibility(UI);
        List<Page> pageList = game.getPageList();
        for (int i = 0; i < pageList.size(); i++) {
            Page page = pageList.get(i);
            Paint outlinePaint = page == selected ? blueOutlinePaint : blackOutlinePaint;
            Paint textPaint = page == game.getFirstPage() ? boldTextPaint : regularTextPaint;
            canvas.drawText(page.getName(), getRectF(i).left, getRectF(i).bottom, textPaint);
            canvas.drawRect(getRectF(i), outlinePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX();
                float y = event.getY();
                boolean flag = false;
                List<Page> pageList = game.getPageList();
                for (int i = 0; i < pageList.size(); i++) {
                    RectF r = getRectF(i);
                    if (x > r.left && x < r.right && y > r.top && y < r.bottom) {
                        setSelected(pageList.get(i));
                        EditText newName = ((Activity) getContext()).findViewById(R.id.page_new_name);
                        if (newName != null) {
                            String pageName = pageList.get(i).getName();
                            newName.setText(pageName);
                        }
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

