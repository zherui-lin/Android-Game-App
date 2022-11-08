package edu.stanford.cs108.bunnyworld;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

public class PageCatalogActivity extends AppCompatActivity {

    private PageCatalogView pageCatalogView;
    private Game game;
    private Stack<String> actionStack = new Stack<>();
    private Stack<Page> addedPageStack = new Stack<>();
    private Stack<Page> deletedPageStack = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_catalog);
        pageCatalogView = findViewById(R.id.page_catalog_view);
        game = Game.getCurrentGame();
        Game.setEditorMode();
        actionStack = new Stack<>();
        addedPageStack = new Stack<>();
        deletedPageStack = new Stack<>();
    }

    public void onCreatePage(View view) {
        Page addedPage = game.newPage();
        actionStack.push("add");
        addedPageStack.push(addedPage);
        saveGame(game);
        pageCatalogView.invalidate();
    }

    public void onEditPage(View view) {
        if (pageCatalogView.getSelected() != null) {
            game.setCurrentPage(pageCatalogView.getSelected().getName());
            startActivity(new Intent(this, ShapeEditorActivity.class));
        }
    }

    public void onBack(View view) {
        Intent intent = new Intent(this, EditorGameCatalogActivity.class);
        startActivity(intent);
    }

    public void onSetFirstPage(View view) {
        if (pageCatalogView.getSelected() != null) {
            game.setFirstPage(pageCatalogView.getSelected());
            saveGame(game);
            pageCatalogView.invalidate();
        }
    }

    public void onDeletePage(View view) {
        if (pageCatalogView.getSelected() != null) {
            game.deletePage(pageCatalogView.getSelected());
            actionStack.push("delete");
            deletedPageStack.push(pageCatalogView.getSelected());
            pageCatalogView.clearSelected();
            saveGame(game);
            pageCatalogView.invalidate();
        }
    }

    public void onChangePageName(View view) {
        if (pageCatalogView.getSelected() != null) {
            EditText newName = findViewById(R.id.page_new_name);
            String changeName = newName.getText().toString();
            for (int i = changeName.length() - 1; i >= 0; i--) {
                Character curChar = changeName.charAt(i);
                if (!Character.isDigit(curChar)) {
                    break;
                }
                if (Character.isDigit(changeName.charAt(i))) {
                    Toast.makeText(this, "Invalid Names!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (newName.getText().toString().length() == 0) {
                Toast.makeText(this, "Name cannot be empty!", Toast.LENGTH_SHORT).show();
            } else if (checkDuplicateName(newName.getText().toString())) {
                Toast.makeText(this, "Name already exists!", Toast.LENGTH_SHORT).show();
            } else {
                pageCatalogView.getSelected().setName(newName.getText().toString());
                newName.setText("");
                saveGame(game);
            }
            pageCatalogView.invalidate();
        }
    }

    public void onUndoPages(View view) {
        if (actionStack.isEmpty()) {
            return;
        }
        switch (actionStack.pop()) {
            case "add":
                game.deletePage(addedPageStack.pop());
                break;
            case "delete":
                game.addPage(deletedPageStack.pop());
                break;
        }
        saveGame(game);
        pageCatalogView.invalidate();
    }

    private boolean checkDuplicateName(String newName){
        for(Page p : game.getPageList()){
            String currName = p.getName();
            if(currName.equals(newName)){
                return true;
            }
        }
        return false;
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