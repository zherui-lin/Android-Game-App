package edu.stanford.cs108.bunnyworld;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

public class ShapeEditorActivity extends AppCompatActivity {
    private ShapeEditorView shapeEditV;

    private String shapeName, imageName, text;
    private Script script;
    private float left = 1.0f, top = 1.0f, width = 201.0f, height = 201.0f;
    private float font;

    private transient Game game = Game.getCurrentGame();

    private CheckBox movableCheck;
    private CheckBox animationCheck;
    final List<String> typeList = Arrays.asList("box", "text", "carrot", "carrot2", "death",
            "duck", "fire", "mystic", "door");

    // add font and font style
    private String fontName;
    private String fontStyle;
    private int red=0, green=0, blue=0;
    final List<String> fontStyleList = Arrays.asList("NORMAL", "BOLD", "BOLD_ITALIC","ITALIC");
    final List<String> fontList = Arrays.asList("sans-serif", "sans-serif-thin", "sans-serif-light",
            "sans-serif-medium", "sans-serif-black", "sans-serif-condensed",
            "sans-serif-condensed-light", "sans-serif-condensed-medium",
            "serif","monospace", "serif-monospace","casual", "cursive", "sans-serif-smallcaps");

    // add Adapter and spinner for font
    ArrayAdapter<String> fontNameAdapter;
    ArrayAdapter<String> fontStyleAdapter;
    Spinner fontNameSpinner;
    Spinner fontStyleSpinner;

    ArrayAdapter<String> typeAdapter;
    AdapterView.OnItemSelectedListener listener;

    Spinner typeSpinner;

    //**undo
    private Stack<String> actionStack = new Stack<>();
    private Stack<Shape> addedShapeStack = new Stack<>();
    private Stack<Shape> deletedShapeStack = new Stack<>();
    private Stack<Shape> beforeEditedShapeStack = new Stack<>();
    private Stack<Shape> afterEditedShapeStack = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shape_editor);

        shapeEditV =(ShapeEditorView) findViewById(R.id.shapeEditView);

        movableCheck = (CheckBox) findViewById(R.id.movableCheck);
        animationCheck = (CheckBox) findViewById(R.id.animationCheck);

        //Movable and animation can't be checked at the same time
        animationCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    movableCheck.setChecked(false);
                    movableCheck.setEnabled(false);
                }
                else{movableCheck.setEnabled(true);}
            }
        });

        movableCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                animationCheck.setEnabled(true);

                if(isChecked){
                    animationCheck.setChecked(false);
                    animationCheck.setEnabled(false);
                }
                else {
                    animationCheck.setChecked(false);
                    animationCheck.setChecked(false);
                }
            }
        });

        //create shape type dropdown
        typeSpinner = findViewById(R.id.typeSpinner);
        typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, typeList);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setOnItemSelectedListener(listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String typeValue = adapterView.getItemAtPosition(i).toString();
                if (typeValue.equals("box") || typeValue.equals("text")){
                    imageName = "";
                }
                else{
                    imageName = typeValue;
                }

//                Toast.makeText(adapterView.getContext(), typeValue + "shape",
//                        Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //CH: add dropdown for font type
        fontNameSpinner = findViewById(R.id.fontNameSpinner);
        fontNameAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, fontList);
        fontNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fontNameSpinner.setAdapter(fontNameAdapter);
        fontNameSpinner.setOnItemSelectedListener(listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fontName = adapterView.getItemAtPosition(i).toString();

//                Toast.makeText(adapterView.getContext(), fontName + " font",
//                        Toast.LENGTH_SHORT).show();

            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //CH: add dropdown for font style
        fontStyleSpinner = findViewById(R.id.fontStyleSpinner);
        fontStyleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, fontStyleList);
        fontStyleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fontStyleSpinner.setAdapter(fontStyleAdapter);
        fontStyleSpinner.setOnItemSelectedListener(listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fontStyle = adapterView.getItemAtPosition(i).toString();
//                Toast.makeText(adapterView.getContext(), fontStyle + " font style",
//                        Toast.LENGTH_SHORT).show();

            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void onAdd(View view){
        // if there is a selected shape, nothing happens to prevent adding shape with the same name
        if(game.getSelectShape() != null){
            return;
        }
        EditText xEdit = (EditText) findViewById(R.id.leftBound);
        EditText yEdit = (EditText) findViewById(R.id.upBound);
        EditText widthEdit = (EditText) findViewById(R.id.widthBound);
        EditText heightEdit = (EditText) findViewById(R.id.heightBound);
        EditText nameEdit = (EditText) findViewById(R.id.nameEdit);
        EditText textEdit = (EditText) findViewById(R.id.textShape);
        EditText fontEdit = (EditText) findViewById(R.id.fontEdit);

        CheckBox visibleCheck = (CheckBox) findViewById(R.id.visibleCheck);
        //CheckBox movableCheck = (CheckBox) findViewById(R.id.movableCheck);

        //CH: add font color
        SeekBar redBar = (SeekBar) findViewById(R.id.red_bar);
        SeekBar greenBar = (SeekBar) findViewById(R.id.green_bar);
        SeekBar blueBar = (SeekBar) findViewById(R.id.blue_bar);
        red = redBar.getProgress();
        green = greenBar.getProgress();
        blue = blueBar.getProgress();

        shapeName = nameEdit.getText().toString();

        if (checkDuplicateName(shapeName)) {
            Toast.makeText(this, "Name already exists!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (xEdit.getText().toString().length() != 0){
            left = Float.parseFloat(xEdit.getText().toString());
        }

        if(yEdit.getText().toString().length() != 0){
            top = Float.parseFloat(yEdit.getText().toString());
        }
        if(widthEdit.getText().toString().length() != 0){
            width = Float.parseFloat(widthEdit.getText().toString());
        }
        if(heightEdit.getText().toString().length() != 0){
            height = Float.parseFloat(heightEdit.getText().toString());
        }

        float right = left + width;
        float bottom = top + height;


        text = textEdit.getText().toString();
        if(textEdit.getText().toString().length() == 0){
            text = "";
        }
        else{
            text = textEdit.getText().toString();
        }

        script = new Script("triggerHolder", "actionHolder", "objectHolder");

        Shape currShape = new Shape(left, top, right, bottom, imageName, text, script);
        game.setSelectShape(currShape);

        if(shapeName.length() != 0){
            currShape.setName(shapeName);
        }

        if(fontEdit.getText().toString().length() != 0){
            font = Float.parseFloat(fontEdit.getText().toString());
            currShape.changeFont(font);
        }

        //CH: set font style
        if (fontName.length() != 0 && fontStyle.length() != 0) {
            currShape.setFontStyle(fontName, fontStyle);
        }
        //CH: set font color
        currShape.setFontColor(red,green,blue);

        if(visibleCheck.isChecked()){
            currShape.setVisible();
        }
        else{
            currShape.setInvisible();
        }

        if(movableCheck.isChecked()){
            currShape.setMovable();
        }
        else{
            currShape.setUnmovable();
        }
        if(currShape.getText().length() == 0){
            if(animationCheck.isChecked()){
                currShape.setAnimatable();
            }
            else{currShape.setUnanimatable();}
        }

        game.setSelectShape(currShape);
        game.getCurrentPage().addShape(currShape);
        setEditView(currShape);
        Toast.makeText(view.getContext(), currShape.getName() + " saved",
                Toast.LENGTH_SHORT).show();

        //**undo
        actionStack.push("add");
        addedShapeStack.push(currShape);
        System.out.println("get here 1" + currShape.getName());

        saveGame(game);
        shapeEditV.invalidate();


    }

    private void setEditView(Shape shape){
        Spinner typeSpinner = (Spinner) findViewById(R.id.typeSpinner);
        ArrayAdapter<String> typeAdapter = (ArrayAdapter<String>) typeSpinner.getAdapter();
        EditText xEdit = (EditText) findViewById(R.id.leftBound);
        EditText yEdit = (EditText) findViewById(R.id.upBound);
        EditText widthEdit = (EditText) findViewById(R.id.widthBound);
        EditText heightEdit = (EditText) findViewById(R.id.heightBound);
        EditText nameEdit = (EditText) findViewById(R.id.nameEdit);
        EditText textEdit = (EditText) findViewById(R.id.textShape);
        EditText fontEdit = (EditText) findViewById(R.id.fontEdit);
        CheckBox visibleCheck = (CheckBox) findViewById(R.id.visibleCheck);
        CheckBox movableCheck = (CheckBox) findViewById(R.id.movableCheck);
        CheckBox animationCheck = (CheckBox) findViewById(R.id.animationCheck);

        //HC:added
        SeekBar redBar = (SeekBar) findViewById(R.id.red_bar);
        SeekBar greenBar = (SeekBar) findViewById(R.id.green_bar);
        SeekBar blueBar = (SeekBar) findViewById(R.id.blue_bar);

        typeSpinner.setSelection(typeAdapter.getPosition(game.getSelectShape().getImage()));

        xEdit.setText(Float.toString(shape.getLeft()));
        yEdit.setText(Float.toString(shape.getTop()));
        widthEdit.setText(Float.toString(shape.getWidth()));
        heightEdit.setText(Float.toString(shape.getHeight()));
        nameEdit.setText(shape.getName());
        textEdit.setText(shape.getText());
        if(shape.getText().length() == 0){
            fontEdit.setText("");
        }
        else {fontEdit.setText(Float.toString(shape.getFontSize()));}
        visibleCheck.setChecked(shape.getVisible());
        movableCheck.setChecked(shape.getMovable());
        animationCheck.setChecked(shape.getAnimatable());

        redBar.setProgress(shape.getRed());
        greenBar.setProgress(shape.getGreen());
        blueBar.setProgress(shape.getBlue());

    }

    public void onEditScript(View view){
        if(game.getSelectShape() == null){
            return;
        }
        startActivity(new Intent(this, ScriptEditorActivity.class));
    }

    public void onDelete(View view){
        if (game.getSelectShape() != null){

            game.getCurrentPage().removeShape(game.getSelectShape());


            //**undo
            actionStack.push("delete");
            deletedShapeStack.push(game.getSelectShape());
        }
        game.setSelectShape(null);

        saveGame(game);
        shapeEditV.invalidate();
        resetInput();
    }

    public void onUpdateShape(View view) {

        String typeNew = typeSpinner.getSelectedItem().toString();
        //HC: added for changing font style
        String fontNew = fontNameSpinner.getSelectedItem().toString();
        String fontStyleNew = fontStyleSpinner.getSelectedItem().toString();
        if(game.getSelectShape() == null){
            return;
        }
        if(typeNew != "box"){
            game.getSelectShape().setImage(typeNew);
        }
        else{game.getSelectShape().setImage("");}

        EditText xEdit = (EditText) findViewById(R.id.leftBound);
        EditText yEdit = (EditText) findViewById(R.id.upBound);
        EditText widthEdit = (EditText) findViewById(R.id.widthBound);
        EditText heightEdit = (EditText) findViewById(R.id.heightBound);
        EditText nameEdit = (EditText) findViewById(R.id.nameEdit);
        EditText textEdit = (EditText) findViewById(R.id.textShape);
        EditText fontEdit = (EditText) findViewById(R.id.fontEdit);
        CheckBox visibleCheck = (CheckBox) findViewById(R.id.visibleCheck);
        CheckBox movableCheck = (CheckBox) findViewById(R.id.movableCheck);
        CheckBox animationCheck = (CheckBox) findViewById(R.id.animationCheck);



        Shape selectedShape = game.getSelectShape();


        //**undo
        beforeEditedShapeStack.push(selectedShape);
        Shape afterEditedShape = new Shape(selectedShape);

        game.getCurrentPage().removeShape(selectedShape);
        game.getCurrentPage().addShape(afterEditedShape);

        //HC: font changes onEdit
        SeekBar redBar = (SeekBar) findViewById(R.id.red_bar);
        SeekBar greenBar = (SeekBar) findViewById(R.id.green_bar);
        SeekBar blueBar = (SeekBar) findViewById(R.id.blue_bar);
        red = redBar.getProgress();
        green = greenBar.getProgress();
        blue = blueBar.getProgress();
        afterEditedShape.setFontColor(red,green,blue);
        if (fontNew.length() != 0 && fontStyleNew.length() != 0) {
            afterEditedShape.setFontStyle(fontNew, fontStyleNew);
        }

        if(afterEditedShape != null){
            float newL = xEdit.getText().toString().length() != 0
                    ? Float.parseFloat(xEdit.getText().toString()) : afterEditedShape.getLeft();
            float newT = yEdit.getText().toString().length() != 0
                    ? Float.parseFloat(yEdit.getText().toString()) : afterEditedShape.getTop();

            float newR = widthEdit.getText().toString().length() != 0
                    ? newL + Float.parseFloat(widthEdit.getText().toString()) :
                    afterEditedShape.getRight();
            float newB = heightEdit.getText().toString().length() != 0
                    ? newT + Float.parseFloat(heightEdit.getText().toString()) :
                    afterEditedShape.getBottom();
            afterEditedShape.resize(newL, newT, newR, newB);
            if(fontEdit.getText().toString().length() != 0){
                Float newFont = Float.parseFloat(fontEdit.getText().toString());
                afterEditedShape.changeFont(newFont);
            }

            if (nameEdit.getText().toString().length() == 0) {
                Toast.makeText(this, "Name cannot be empty!", Toast.LENGTH_SHORT).show();
            } else if (checkDuplicateNameExcept(nameEdit.getText().toString(), afterEditedShape)) {
                Toast.makeText(this, "Name already exists!", Toast.LENGTH_SHORT).show();
            } else {
                String newName = nameEdit.getText().toString();
                afterEditedShape.setName(newName);
            }

            //HC: editted, text was not changing back to bitmaps on edit
            if(textEdit.getText().toString().length() == 0){
                text = "";
            }
            else{
                text = textEdit.getText().toString();
            }
            afterEditedShape.setText(text);

            if(visibleCheck.isChecked()){
                afterEditedShape.setVisible();
            }
            else{afterEditedShape.setInvisible();}

            if(movableCheck.isChecked()){
                afterEditedShape.setMovable();
            }
            else{afterEditedShape.setUnmovable();}

            if(afterEditedShape.getText().length() == 0){
                if(animationCheck.isChecked()){
                    afterEditedShape.setAnimatable();
                }
                else{afterEditedShape.setUnanimatable();}
            }

            game.setSelectShape(afterEditedShape);



            //**undo
            actionStack.push("edit");
            afterEditedShapeStack.push(afterEditedShape);

        }

        saveGame(game);
        shapeEditV.invalidate();
    }

    public void onCopy(View view){
        if(game.getSelectShape() == null){
            return;
        }
        game.setCopyShape(game.getSelectShape());
    }

    public void onPaste(View view){
        if(game.getCopyShape() == null){
            return;
        }
        Page currPage = game.getCurrentPage();
        Shape copied = game.getCopyShape();
        String currName = copied.getName();
        //HC: for duplicate copy name
        while(checkDuplicateName(currName)){
            currName += "Copy";
        }
        copied.setName(currName);
        game.setSelectShape(copied);
        currPage.getShapeList().add(copied);
        game.setCopyShape(copied);
        setEditView(game.getSelectShape());

        actionStack.push("add");
        addedShapeStack.push(copied);

        saveGame(game);
        shapeEditV.invalidate();
    }

    public void onBack(View view) {
        Intent intent = new Intent(this, PageCatalogActivity.class);
        startActivity(intent);
    }

    //**undo
    public void onUndo(View view) {
        if (actionStack.isEmpty()) {
            return;
        }
        switch (actionStack.pop()) {
            case "add":
                game.getCurrentPage().removeShape(addedShapeStack.pop());
                break;
            case "delete":
                game.getCurrentPage().addShape(deletedShapeStack.pop());
                break;
            case "edit":
                game.getCurrentPage().removeShape(afterEditedShapeStack.pop());
                game.getCurrentPage().addShape(beforeEditedShapeStack.pop());
                break;
        }
        saveGame(game);
        shapeEditV.invalidate();

        resetInput();
    }

    private boolean checkDuplicateNameExcept(String newName, Shape shape) {
        for (Page p : game.getPageList()) {
            for (Shape s : p.getShapeList()) {
                String currName = s.getName();
                if (currName.equals(newName) && s != shape) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkDuplicateName(String newName) {
        for (Page p : game.getPageList()) {
            for (Shape s : p.getShapeList()) {
                String currName = s.getName();
                if (currName.equals(newName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void resetInput(){
        EditText xEdit = (EditText) findViewById(R.id.leftBound);
        EditText yEdit = (EditText) findViewById(R.id.upBound);
        EditText widthEdit = (EditText) findViewById(R.id.widthBound);
        EditText heightEdit = (EditText) findViewById(R.id.heightBound);
        EditText nameEdit = (EditText) findViewById(R.id.nameEdit);
        EditText textEdit = (EditText) findViewById(R.id.textShape);
        EditText fontEdit = (EditText) findViewById(R.id.fontEdit);

        CheckBox visibleCheck = (CheckBox) findViewById(R.id.visibleCheck);
        CheckBox movableCheck = (CheckBox) findViewById(R.id.movableCheck);

        xEdit.setText("1.0");
        yEdit.setText("1.0");
        widthEdit.setText("200");
        heightEdit.setText("200");
        nameEdit.setText("");
        textEdit.setText("");
        fontEdit.setText("");
        visibleCheck.setChecked(true);
        movableCheck.setChecked(false);
    }

    private void saveGame(Game game) {
        // save in the files folder for the app
        File dir = this.getFilesDir();
        File gameDir = new File(dir, "savedGame");
        if (!gameDir.isDirectory()) {
            gameDir.mkdir();
        }
        File file = new File(gameDir, game.getName() + ".json");
        file.setWritable(true);
        try {
            FileOutputStream fileOutput = new FileOutputStream(file);
            ObjectOutputStream writer = new ObjectOutputStream(fileOutput);
            writer.writeObject(game);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}