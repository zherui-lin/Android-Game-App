package edu.stanford.cs108.bunnyworld;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.util.Arrays;

public class PlayerGameCatalogActivity extends AppCompatActivity {

    private GameCatalogView gameCatalogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_game_catalog);
        gameCatalogView = findViewById(R.id.player_game_catalog_view);
        Game.setPlayMode();
    }

    public void onPlayGame(View view) {
        if (gameCatalogView.getSelected() != null) {
            Game loadedGame = loadGame(gameCatalogView.getSelected());
            Game.setCurrentGame(loadedGame);
            startActivity(new Intent(this, PlayGameActivity.class));
        }
    }

    public void onBack(View view) {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
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
}