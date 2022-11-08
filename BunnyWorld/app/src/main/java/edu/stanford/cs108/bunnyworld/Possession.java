package edu.stanford.cs108.bunnyworld;


import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap;
import java.io.Serializable;


public class Possession implements Serializable {
    private int capacity;
    private Shape shapeArr[];
    private int gridSize;
    private int gridHeight;
    private static Rect[] rectArray;
    private static int margin = 10;
    private transient Game game;

    public Possession(int capacity){
        this.capacity = capacity;
        shapeArr = new Shape[capacity];

        this.gridSize = Resources.getSystem().getDisplayMetrics().widthPixels / capacity;
        this.gridHeight = (int) (Resources.getSystem().getDisplayMetrics().heightPixels * 0.3);
        this.game = Game.getCurrentGame();
    }

    public void setCurrentGame() {
        this.game = Game.getCurrentGame();
    }

    // Method to change capacity?

    public int getCapacity() {
        return capacity;
    }

    public Shape[] getShape() {return shapeArr;}

    public int getGridSize() {return gridSize;}

    public boolean addShape(int position, Shape shape) {
        if (position >= 0 && position < capacity && shapeArr[position] == null) {
            float scale = calcScale(shape);
            shape.setScale(scale);
            shape.changeFont(shape.getFontSize() / scale);
            float newWidth = shape.getWidth() / scale;
            float newHeight = shape.getHeight() / scale;
            float newL = position * gridSize + (gridSize - newWidth) / 2;
            float newT = gridHeight / 2 - newHeight / 2;
            shape.resize(newL,newT,
                    newL + newWidth, newT + newHeight);
            shapeArr[position] = shape;
            return true;
        }
        return false;
    }

    private float calcScale(Shape shape) {
        float widthScale = (float) ((shape.getWidth() / gridSize) * 1.2);
        float heightScale = (float) ((shape.getHeight() / gridHeight) * 1.2);
        return (widthScale > heightScale) ? widthScale : heightScale;
    }

    public void removeShape(int position) {
        if (position >= 0 && position < capacity) {
            shapeArr[position] = null;
        }
    }

    public void setSelectShape(int position, float y) {
        game = Game.getCurrentGame();
        if (position >= 0 && position < capacity && shapeArr[position] != null) {
            if (y > shapeArr[position].getTop() && y < shapeArr[position].getBottom()) {
                game.setSelectShape(shapeArr[position]);
            }
        }
    }

    public void draw(Canvas canvas, Paint drawPaint) {
        canvas.drawColor(Color.LTGRAY);  //Debug purpose
        rectArray = new Rect[capacity];
        for (int i = 0; i < capacity; i++) {
            rectArray[i] = new Rect(i * gridSize, 0 + margin, (i + 1) * gridSize, PossessionView.viewHeight);
        }

        for (int i = 0; i < capacity; i++) {
            // draw frame
            canvas.drawRect(i * gridSize, 0, (i + 1) * gridSize, PossessionView.viewHeight, drawPaint);

            // draw item
//            Bitmap image = null;
//            String imageName = "";
            Shape shape = this.getShape()[i];
            if (shape != null && shape.getVisible()) {
                shape.draw(canvas);
            }

        }
    }
}
