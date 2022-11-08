package edu.stanford.cs108.bunnyworld;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextPaint;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Shape implements Serializable {
    private boolean isMovable = false;
    private boolean isVisible = true;
    private float fontSize;
    private transient Bitmap image;
    private String shapeName;
    private String imageName;
    private String text;
    private boolean isAnimatable = false;

    private int red = 0;
    private int green = 0;
    private int blue = 0;

    //ZL: store typeface as fontName and fontStyle instead
    private String fontName;
    private int fontStyle;

    private float possessionScale;
//    private String shapeScript;

    private List<Script> scriptList;


    //bounding rectangle
    private float left;
    private float right;
    private float top;
    private float bottom;

    //animation rectagnle
    private float animationL, animationR, animationT, animationB;

    private transient Paint shapePaint;
    private transient Paint boundary;
    private final float BOUNDARY_WIDTH = 5;

    private static int id = 0;
    private static final float DEFAULT_FONT = 50;
    private static final String TEXT_SHAPE = "isText";
    private static final String IMAGE_SHAPE = "isImage";
    private static final String RECT_SHAPE = "isRect";

    public Shape(float l, float t, float r, float b, String imgName, String txt, Script script) {
        id++;
        this.shapeName = "shape" + Integer.toString(id);
        this.left = l;
        this.right = r;
        this.top = t;
        this.bottom = b;
        this.imageName = imgName;
        this.text = txt;
        scriptList = new ArrayList<Script>();
        scriptList.add(script);
        fontSize = DEFAULT_FONT;
        //Change the shape boundary for text
        adjustBound();
        possessionScale = 1;
        initBoundary();

        animationL = this.left;
        animationR = this.right;
        animationT = this.top;
        animationB = this.bottom;
    }

    public Shape(Shape another){
        this.shapeName = another.shapeName;
        this.left = another.left;
        this.right = another.right;
        this.top = another.top;
        this.bottom = another.bottom;
        this.imageName = another.imageName;
        this.text = another.text;
        this.scriptList = new ArrayList<>(another.scriptList);
        this.fontSize = another.fontSize;
        this.isAnimatable = another.getAnimatable();
        this.isVisible = another.getVisible();
        this.isMovable = another.getMovable();
        this.fontName = another.fontName;
        this.fontStyle = another.fontStyle;
        this.red = another.red;
        this.blue = another.blue;
        this.green = another.green;

        adjustBound();
        possessionScale = another.possessionScale;
        initBoundary();
        animationL = another.animationL;
        animationR = another.animationR;
        animationT = another.animationT;
        animationB = another.animationB;
    }

    public String scriptToString(){
        String scriptText = "";
        for(Script script : getScriptList()){
            String sTrigger = script.getTrigger();
            String sAction = script.getAction();
            String sObject = script.getObject();
            String sDrop = script.getDropShape();
            String combined = sTrigger + " " + sAction + " " + sObject + ";";
            if(sTrigger.equals("ondrop")){
                sDrop = script.getDropShape();
                combined = sTrigger + " " + sDrop + " " + sAction + " " + sObject + ";";
            }

            if(scriptText == null){
                scriptText = combined;
            }
            if(scriptText.contains(sTrigger)){
                if(!sTrigger.equals("ondrop")){
                    //insert after the space of the trigger
                    //HC: script reverse order
                    int indexTrigger = scriptText.indexOf(sTrigger);
                    int index = scriptText.indexOf(";", indexTrigger);
                    String after = scriptText.substring(index);
                    scriptText = scriptText.substring(0, index) + " " + sAction + " " + sObject +
                            after;
                }
                else if((!sDrop.equals("")) && (scriptText.contains(sDrop + " "))){
                    //HC: script reverse order
                    int indexDrop = scriptText.indexOf(sDrop);
                    int index = scriptText.indexOf(";", indexDrop);
                    String after = scriptText.substring(index);
                    scriptText = scriptText.substring(0, index) + " " + sAction + " " + sObject +
                            after;
                }
                else {
                    scriptText = scriptText + combined;
                }

            }
            else {scriptText = scriptText + combined;}
        }
        return scriptText;
    }

    /**
     * Adjust the boundary according to text size.
     */
    private void adjustBound() {
        setPaint();
        if (!text.equals("")) {
            Rect textRect = new Rect();
            shapePaint.getTextBounds(text, 0, text.length(), textRect);
            this.right = left + textRect.width();
            this.bottom = top + textRect.height();
        }
    }

    public void initBoundary() {
        boundary = new Paint();
        boundary.setStyle(Paint.Style.STROKE);
        boundary.setColor(Color.TRANSPARENT);
        boundary.setStrokeWidth(BOUNDARY_WIDTH);
    }

    public void resize(float lNew, float tNew, float rNew, float bNew) {
        this.left = lNew;
        this.top = tNew;
        this.right = rNew;
        this.bottom = bNew;
        adjustBound();
    }

    public String getType() {
        if (!text.equals("")) {
            return TEXT_SHAPE;
        } else if (!imageName.equals("")) {
            return IMAGE_SHAPE;
        } else {
            return RECT_SHAPE;
        }
    }

    public void changeFont(float size){
        this.fontSize = size;
        adjustBound();
    }

    public boolean isClicked(float x, float y) {
        return (x > left && x < right && y > top && y < bottom && (isVisible || Game.isEditorMode()));
    }

    public void showBoundary() {
        if (boundary == null) {
            initBoundary();
        }
        boundary.setColor(Color.GREEN);
    }

    public void hideBoundary() {
        if (boundary == null) {
            initBoundary();
        }
        boundary.setColor(Color.TRANSPARENT);
    }

    public void setScale(float scale) {
        possessionScale = scale;
    }

    public void addScript(Script script){
        this.scriptList.add(script);
    }

    public void setName(String name) {
        this.shapeName = name;
    }

    public void setImage(String img){
        this.imageName = img;
    }

    public void setText(String txt){
        this.text = txt;
        adjustBound();
    }

    public String getName() {
        return shapeName;
    }

    public float getPossessionScale() {
        return possessionScale;
    }

    public List<Script> getScriptList() {
        return scriptList;
    }

    public String getText() {
        return text;
    }

    public float getFontSize() {
        return fontSize;
    }

    public Paint getPaint() {
        return shapePaint;
    }

    public String getImage() {
        return imageName;
    }

    public float getLeft() {
        return left;
    }

    public float getTop() {
        return top;
    }

    public float getRight() {
        return right;
    }

    public float getBottom() {
        return bottom;
    }

    public float getWidth() {
        return right - left;
    }

    public float getHeight() {
        return bottom - top;
    }

    public boolean getMovable() {
        return isMovable;
    }

    public boolean getVisible(){
        return isVisible;
    }

    public Bitmap getBitmap() {
        return image;
    }

    public void setMovable() {
        this.isMovable = true;
    }

    public void setVisible() {
        this.isVisible = true;
    }

    public void setUnmovable() {
        this.isMovable = false;
    }

    public void setInvisible() {
        this.isVisible = false;
    }

    public void setFontColor(int red, int green, int blue){
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public int getRed(){
        return this.red;
    }

    public int getGreen(){
        return this.green;
    }

    public int getBlue(){
        return this.blue;
    }

    public String getFontName() {
        return this.fontName;
    }

    public int getFontStyle() {
        return this.fontStyle;
    }

    public void setFontStyle(String name, String style) {
        fontName = name;
        fontStyle = Typeface.NORMAL;
        switch (style) {
            case "BOLD":
                fontStyle = Typeface.BOLD;
                break;
            case "BOLD_ITALIC":
                fontStyle = Typeface.BOLD_ITALIC;
                break;
            case "ITALIC":
                fontStyle = Typeface.ITALIC;
                break;
            case "NORMAL":
                fontStyle = Typeface.NORMAL;
                break;
        }
    }

    private Paint setPaint() {
//        https://developer.android.com/reference/android/graphics/Typeface
        if (this.getType().equals("isText")) {
            shapePaint = new TextPaint();
            shapePaint.setTextSize(fontSize);
            shapePaint.setColor(Color.BLACK);

            shapePaint.setColor(Color.rgb(red,green,blue));
            if (fontName != null) {
                shapePaint.setTypeface(Typeface.create(fontName, fontStyle));
            }
        } else if (this.getType().equals("isRect")) {
            shapePaint = new Paint();
            shapePaint.setStyle(Paint.Style.FILL);
            shapePaint.setColor(Color.LTGRAY);
        }
        return shapePaint;
    }

    private void calculateAnamationRect() {

        float centerX = (this.left + this.right) / 2;
        float centerY = (this.top + this.bottom) / 2;

        float width = Game.isEditorMode() ? (this.right - this.left) * ShapeEditorView.radius :
                (this.right - this.left) * PageView.radius;
        float height = Game.isEditorMode() ? (this.bottom - this.top) * ShapeEditorView.radius :
                (this.bottom - this.top) * PageView.radius;
        animationL = centerX - width/2;
        animationR = centerX + width/2;
        animationT = centerY - height/2;
        animationB = centerY + height/2;
    }

    public boolean getAnimatable(){
        return isAnimatable;
    }

    public void setAnimatable() {
        this.isAnimatable = true;
    }

    public void setUnanimatable() {
        this.isAnimatable = false;
    }

    public void draw(Canvas canvas) {

        if (isVisible || Game.isEditorMode()) {
            setPaint();
            if (isAnimatable) {
                calculateAnamationRect();
            }
            //text takes precedence than image
            if (!text.equals("")) {
                canvas.drawText(text, left, bottom, shapePaint);
            } else if (!imageName.equals("")) {
                switch (imageName) {
                    case "carrot":
                        image = Game.isEditorMode() ? ShapeEditorView.carrotDrawable.getBitmap() :
                                PageView.carrotDrawable.getBitmap();
                        break;
                    case "carrot2":
                        image = Game.isEditorMode() ? ShapeEditorView.carrotDrawable2.getBitmap() :
                                PageView.carrotDrawable2.getBitmap();
                        break;
                    case "death":
                        image = Game.isEditorMode() ? ShapeEditorView.deathDrawable.getBitmap() :
                                PageView.deathDrawable.getBitmap();
                        break;
                    case "duck":
                        image = Game.isEditorMode() ? ShapeEditorView.duckDrawable.getBitmap() :
                                PageView.duckDrawable.getBitmap();
                        break;
                    case "fire":
                        image = Game.isEditorMode() ? ShapeEditorView.fireDrawable.getBitmap() :
                                PageView.fireDrawable.getBitmap();
                        break;
                    case "mystic":
                        image = Game.isEditorMode() ? ShapeEditorView.mysticDrawable.getBitmap() :
                                PageView.mysticDrawable.getBitmap();
                        break;
                    case "door":
                        image = Game.isEditorMode() ? ShapeEditorView.doorDrawable.getBitmap() :
                                PageView.doorDrawable.getBitmap();
                        break;
                }
                if (isAnimatable) {
                    canvas.drawBitmap(image, null, new RectF(animationL, animationT,
                            animationR, animationB), null);
                } else {
                    canvas.drawBitmap(image, null, new RectF(left, top, right, bottom), null);
                }
            } else {
                //neither image or text, draw light gray rectangle

                canvas.drawRect(new RectF(left, top, right, bottom), shapePaint);
            }

            if (boundary == null) {
                initBoundary();
            }

            canvas.drawRect(new RectF(left, top, right, bottom), boundary);
        }
    }
}