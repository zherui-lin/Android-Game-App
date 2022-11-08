package edu.stanford.cs108.bunnyworld;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScriptEditorActivity extends AppCompatActivity {
    ScriptEditorView scriptEditorV;
    private Shape currShape;
    private Page currPage;
    private List<Script> scriptList = new ArrayList<>();
    private String trigger, dropTo, action, object;
    private Script script;

    private transient Game game = Game.getCurrentGame();
    private List<Page> pageList = game.getPageList();
    private List<Shape> shapeList = new ArrayList<>();

    private static final float DEFAULT_SHAPE_LENGTH = 40;

    final List<String> triggerList = Arrays.asList("onclick", "onenter", "ondrop");

    final List<String> actionList = Arrays.asList("goto", "play", "hide", "show", "unlock", "lock",
            "animate", "stopanimate");
    final List<String> soundList = Arrays.asList("carrotcarrotcarrot", "evillaugh", "fire",
            "hooray", "munch", "munching", "woof");
    List<String> shapesCreated = new ArrayList<>();
    List<String> pagesCreated = new ArrayList<>();
    Spinner triggerSpinner, onDropSpinner, actionSpinner, objectSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_editor);

        scriptEditorV = (ScriptEditorView) findViewById(R.id.scriptEditView);

        currShape = game.getSelectShape();
        scriptList = currShape.getScriptList();
        //remove script holder
        for(Script script : scriptList){
            if(script.getTrigger().equals("triggerHolder")){
                scriptList.remove(script);
            }
        }
//        scriptList.clear();

        currPage = game.getCurrentPage();
        shapeList = currPage.getShapeList();

        //Create script trigger dropdown
        triggerSpinner = findViewById(R.id.triggerSpinner);
        ArrayAdapter<String> triggerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, triggerList);
        triggerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        triggerSpinner.setAdapter(triggerAdapter);

        onDropSpinner = findViewById(R.id.onDropTo);
        triggerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String triggerValue = adapterView.getItemAtPosition(i).toString();
                trigger = triggerValue;
//                Toast.makeText(adapterView.getContext(), triggerValue, Toast.LENGTH_SHORT).show();
                //When the trigger is on drop, user need to choose which shape to drop to
                if (triggerValue.equals("ondrop")) {
                    updateShapes();
                } else {
                    shapesCreated.clear();
                }
                setSpinner(onDropSpinner, shapesCreated);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //Set up listener for onDrop dropdown
        onDropSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String dropToValue = adapterView.getItemAtPosition(i).toString();
                dropTo = dropToValue;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Create action dropdown
        actionSpinner = findViewById(R.id.actionSpinner);
        objectSpinner = findViewById(R.id.objectSpinner);
        ArrayAdapter<String> actionAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, actionList);
        actionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actionSpinner.setAdapter(actionAdapter);
        actionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String actionValue = adapterView.getItemAtPosition(i).toString();
                action = actionValue;
                if(actionValue.equals("goto")){
                    updatePages();
                    setSpinner(objectSpinner, pagesCreated);
                }
                if(actionValue.equals("play")){
                    setSpinner(objectSpinner, soundList);
                }
                if(actionValue.equals("hide") || actionValue.equals("show") ||
                        actionValue.equals("lock") || actionValue.equals("unlock") ||
                        actionValue.equals("animate") || actionValue.equals("stopanimate")){
                    updateShapes();
                    //Add the shape itself from onDrop list
                    List<String> objectShapeCreated = new ArrayList<>(shapesCreated);
                    if(!objectShapeCreated.contains(currShape.getName())) {
                        objectShapeCreated.add(currShape.getName());
                    }
                    setSpinner(objectSpinner, objectShapeCreated);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Set up listener for object drop down
        objectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String objectValue = adapterView.getItemAtPosition(i).toString();
                object = objectValue;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setSpinner(Spinner spinner, List<String> items){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void updateShapes(){
        for(Page page : game.getPageList()){
            ArrayList<Shape> shapeList = new ArrayList<>(page.getShapeList());
            for(Shape s : shapeList){
                String currName = s.getName();
                if(!shapesCreated.contains(currName)){
                    //Shouldn't be able to call onDrop to itself
                    if(!currShape.getName().equals(currName)){
                        shapesCreated.add(currName);
                    }
                }
            }
        }
    }

    private void updatePages(){
        for(Page pg: pageList){
            String pgName = pg.getName();
            if(!pagesCreated.contains(pgName)){
                pagesCreated.add(pgName);
            }
        }
    }

    public void onAddScript(View view){
        if(dropTo == null){
            script = new Script(trigger, action, object);
        }
        else{
            script = new Script(trigger, dropTo, action, object);
        }
        currShape.addScript(script);
        saveGame(game);
        scriptEditorV.invalidate();
    }

    public void onDeleteScript(View view){
        if(scriptEditorV.getSelected() != null){
            scriptList.remove(scriptEditorV.getSelected());
            scriptEditorV.clearSelected();
            saveGame(game);
            scriptEditorV.invalidate();
        }
    }

    public void onBack(View view) {
        Intent intent = new Intent(this, ShapeEditorActivity.class);
        startActivity(intent);
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