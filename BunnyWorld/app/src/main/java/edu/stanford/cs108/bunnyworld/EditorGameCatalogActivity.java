package edu.stanford.cs108.bunnyworld;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.util.Arrays;

public class EditorGameCatalogActivity extends AppCompatActivity {

    private GameCatalogView gameCatalogView;

    private static String DEFAULT_NEW_GAME_NAME = "New Game";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_game_catalog);
        gameCatalogView = findViewById(R.id.game_catalog_view);
        Game.setEditorMode();
    }

    public void onCreateGame(View view) {
        if (checkDefaultName()) {
            Toast.makeText(this, "Please rename existed 'New Game'!", Toast.LENGTH_SHORT).show();
        } else {
            Game game = new Game(DEFAULT_NEW_GAME_NAME);
            saveGame(game);
            Game.setCurrentGame(game);
        }
        gameCatalogView.invalidate();
    }

    public void onEditGame(View view) {
        if (gameCatalogView.getSelected() != null) {
            Game game = loadGame(gameCatalogView.getSelected());
            Game.setCurrentGame(game);
            startActivity(new Intent(this, PageCatalogActivity.class));
        }
    }

    public void onBack(View view) {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    public void onDeleteGame(View view) {
        if (gameCatalogView.getSelected() != null) {
            deleteGame(gameCatalogView.getSelected());
            gameCatalogView.clearSelected();
            gameCatalogView.invalidate();
        }
    }

    public void onChangeGameName(View view) {
        if (gameCatalogView.getSelected() != null) {
            EditText newName = findViewById(R.id.game_new_name);
            if (newName.getText().toString().length() == 0) {
                Toast.makeText(this, "Name cannot be empty!", Toast.LENGTH_SHORT).show();
            } else if (checkDuplicateName(newName.getText().toString())) {
                Toast.makeText(this, "Name already exists!", Toast.LENGTH_SHORT).show();
            } else {
                renameGame(gameCatalogView.getSelected(), newName.getText().toString());
                newName.setText("");
            }
            gameCatalogView.invalidate();
        }
    }

    private boolean checkDefaultName() {
        String[] fileNames = getFileNames();
        for (String fileName : fileNames) {
            String gameName = fileName.substring(0, fileName.length() - 5);
            if(gameName.equals(DEFAULT_NEW_GAME_NAME)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkDuplicateName(String newGameName) {
        String[] fileNames = getFileNames();
        for (String fileName : fileNames) {
            String gameName = fileName.substring(0, fileName.length() - 5);
            if(gameName.equals(newGameName)) {
                return true;
            }
        }
        return false;
    }

    private String[] getFileNames() {
        File dir = new File(this.getFilesDir(), "savedGame");
        String[] fileNames = dir.list(); //file order is by saved time
        return fileNames;
    }

    private Game loadGame(String gameName) {
        File gameFile = findGameFileByName(gameName);
        Log.i("data - found game", gameName);
        Game game = null;
        if (gameFile != null) {
            try {
                FileInputStream inputFile = new FileInputStream(gameFile);
                ObjectInputStream gameInput = new ObjectInputStream(inputFile);
                game = (Game) gameInput.readObject();
                gameInput.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            game.setName(gameFile.getName().substring(0, gameFile.getName().length() - 5));
            game.setCurrentPage(game.getFirstPage());
            return game;
        }
        return null;
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

    private void deleteGame(String gameName) {
        File gameFile = findGameFileByName(gameName);
        if (gameFile != null) {
            if (gameFile.isFile()) {
                gameFile.delete();
            }
        }
    }

    private File findGameFileByName(String gameName) {
        String[] fileNames = getFileNames();

        File dir = new File(this.getFilesDir(), "savedGame");
        File[] files = dir.listFiles();
        int gameIdx = Arrays.asList(fileNames).indexOf(gameName);
        if (gameIdx != -1 && files != null) {
            return files[gameIdx];
        } else {
            return null;
        }
    }

    private void renameGame(String oldName, String newName) {
        File oldGameFile = findGameFileByName(oldName);
        if (oldGameFile == null) {
            return;
        }
        File gameDir = new File(this.getFilesDir(), "savedGame");
        File newNameFile = new File(gameDir, newName + ".json");
        newNameFile.setWritable(true);
        oldGameFile.renameTo(newNameFile);
    }

}