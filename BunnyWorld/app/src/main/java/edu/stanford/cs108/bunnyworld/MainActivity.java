package edu.stanford.cs108.bunnyworld;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class MainActivity extends AppCompatActivity {

    private final String DEFAULT_GAME_NAME = "Bunny World";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        File dir = new File(this.getFilesDir(), "savedGame");
        if (createDefault()) {
            createDefaultGame();
        }
    }

    public void onEditor(View view) {
        Intent intent = new Intent(this, EditorGameCatalogActivity.class);
        startActivity(intent);
    }

    public void onPlayer(View view) {
        Intent intent = new Intent(this, PlayerGameCatalogActivity.class);
        startActivity(intent);
    }

    private void createDefaultGame() {
        Game game = new Game(DEFAULT_GAME_NAME);
        initPage(game);
        saveGame(game);
    }

    private boolean createDefault() {
        File dir = new File(this.getFilesDir(), "savedGame");
        String[] fileNames = dir.list();
        return fileNames == null || fileNames.length == 0;
    }

    private void initPage(Game game) {
        Script onClickGotoPage2 = new Script("on click goto page2");
        Script onClickGotoPage1 = new Script("on click goto page1");
        Script onEnterPlayFire = new Script("on enter play fire");
        Script onEnterShowPage1Btn2 = new Script("on enter show page1Btn2");
        Script onClickShowPage1Btn2 = new Script("on click show page1Btn2");
        Script onClickGotoPage3 = new Script("on click goto page3");
        Script onClickGotoPage4 = new Script("on click goto page4");
        Script script0 = new Script("on click show page1Text1"); //placeholder for empty script
        Script onEnterPlayLaugh = new Script("on enter play evillaugh");
        Script onClickHideCarrot = new Script("on click hide Carrot1");
        Script onClickPlayLaugh = new Script("on click play evillaugh");
        Script onClickGotoPage5 = new Script("on click goto page5");
        Script onEnterPlayVictory = new Script("on enter play hooray");
        Script onClickPlayMunching = new Script("on click play munching");

        // Page 1, how to pass empty script
        Shape page1Title = new Shape(0, 50, 300, 300, "", "Bunny World!", script0);
        page1Title.changeFont(100);
        Shape page1Text1 = new Shape(100, 200, 1000, 300, "",
                "You are in a maze", script0);
        Shape page1Text2 = new Shape(100, 300, 1000, 400, "",
                "of twisty little", script0);
        Shape page1Text3 = new Shape(100, 400, 1000, 500, "",
                "passages, all alike!", script0);
        Shape page1Btn1 = new Shape(100, 600, 200, 700, "door", "", onClickGotoPage2);
        Shape page1Btn2 = new Shape(500, 600, 600, 700, "door", "", onClickGotoPage3);
        Shape page1Btn3 = new Shape(900, 600, 1000, 700, "door", "", onClickGotoPage4);
        page1Btn2.setInvisible();
        page1Btn1.addScript(onClickShowPage1Btn2);
        page1Title.setName("page1Title");
        page1Text1.setName("page1Text1");
        page1Text2.setName("page1Text2");
        page1Text3.setName("page1Text3");
        page1Btn1.setName("page1Btn1");
        page1Btn2.setName("page1Btn2");
        page1Btn3.setName("page1Btn3");

        //text animation won't work due to design
//        page1Text1.setAnimatable();

        Page page1 = game.getCurrentPage();
        page1.addShape(page1Title);
        page1.addShape(page1Text1);
        page1.addShape(page1Text2);
        page1.addShape(page1Text3);
        page1.addShape(page1Btn1);
        page1.addShape(page1Btn2);
        page1.addShape(page1Btn3);

        // Page 2, need to review the onclick script for page2Img
        Shape page2Img = new Shape(500, 10, 1000, 500, "mystic",
                "", onEnterShowPage1Btn2);
        Shape page2Text1 = new Shape(500, 500, 800, 580, "",
                "Mystic Bunny - ", script0);
        Shape page2Text2 = new Shape(500, 580, 800, 640, "",
                "Rub my tummy ", script0);
        Shape page2Text3 = new Shape(500, 640, 800, 720, "",
                "for a big surprise!", script0);
        Shape page2Btn = new Shape(300, 600, 400, 700, "door", "", onClickGotoPage1);

        page2Img.addScript(onClickPlayMunching);
        page2Img.addScript(onClickHideCarrot);
        page2Img.setName("page2Img");
        page2Img.setAnimatable();
        page2Text1.setName("page2Text1");
        page2Text2.setName("page2Text2");
        page2Text3.setName("page2Text3");
        page2Btn.setName("page2Btn");
        page2Text1.changeFont(60);
        page2Text2.changeFont(60);
        page2Text3.changeFont(60);

        game.newPage();
        Page page2 = game.getPageList().get(1);
        page2.addShape(page2Img);
        page2.addShape(page2Text1);
        page2.addShape(page2Text2);
        page2.addShape(page2Text3);
        page2.addShape(page2Btn);

        // Page 3
        Shape page3Img = new Shape(300, 50, 700, 500, "fire",
                "", onEnterPlayFire);
        Shape page3Text1 = new Shape(300, 550, 800, 640, "",
                "Eek! Fire-Room. ", script0);
        Shape page3Text2 = new Shape(300, 640, 800, 730, "",
                "Run away!", script0);
        // those carrots can be in inventory and carry over to other pages
        Shape Carrot = new Shape(1100, 300, 1300, 500, "carrot2",
                "", script0);
        Shape Carrot1 = new Shape(1100, 100, 1300, 300, "carrot",
                "", script0);
        Shape page3Btn = new Shape(900, 600, 1000, 700, "door", "", onClickGotoPage2);
        Carrot.setMovable();


        Script onClickStopanimate = new Script("on click stopanimate page2Img");
        Script onClickLockCarrot = new Script("on click lock Carrot");
        Script onClickAnimateFire = new Script("on click animate page3Img");
        Carrot1.addScript(onClickStopanimate);
        Carrot1.addScript(onClickLockCarrot);
        Carrot1.addScript(onClickAnimateFire);
        page3Img.setName("page3Img");
        page3Text1.setName("page3Text1");
        page3Text2.setName("page3Text2");
        Carrot.setName("Carrot");
        Carrot.setMovable();
        Carrot1.setMovable();
        Carrot1.setName("Carrot1"); //hide this carrot

        game.newPage();
        Page page3 = game.getPageList().get(2);
        page3.addShape(page3Img);
        page3.addShape(page3Text1);
        page3.addShape(page3Text2);
        page3.addShape(page3Btn);
        page3.addShape(Carrot);
        page3.addShape(Carrot1);
        page3Text1.changeFont(60);
        page3Text2.changeFont(60);


        // Page 4
        Script onDropHideCarrot = new Script("on drop Carrot hide Carrot");
        Script onDropCarrotHideDeath = new Script("on drop Carrot hide page4Img");
        Script onDropCarrotPlayMunching = new Script("on drop Carrot play munching");
        Script onDropCarrotShowExit = new Script("on drop Carrot show page4Btn");

        Shape page4Img = new Shape(500, 10, 800, 500, "death",
                "", onDropCarrotShowExit);
        Shape page4Text1 = new Shape(400, 550, 800, 640, "",
                "You must appease the", script0);
        Shape page4Text2 = new Shape(400, 640, 800, 730, "",
                "Bunny of Death!", script0);
        Shape page4Btn = new Shape(900, 300, 1000, 400, "door", "", onClickGotoPage5);

        page4Img.setName("page4Img");
        page4Text1.setName("page4Text1");
        page4Text2.setName("page4Text2");
        page4Btn.setName("page4Btn");
        page4Img.addScript(onDropHideCarrot);
        page4Img.addScript(onDropCarrotHideDeath);
        page4Img.addScript(onEnterPlayLaugh);
        page4Img.addScript(onDropCarrotPlayMunching);
        page4Img.addScript(onClickPlayLaugh);
        page4Btn.setInvisible();
        page4Text1.changeFont(60);
        page4Text2.changeFont(60);

        game.newPage();
        Page page4 = game.getPageList().get(3);
        page4.addShape(page4Img);
        page4.addShape(page4Text1);
        page4.addShape(page4Text2);
        page4.addShape(page4Btn);

        // Page 5
        Shape page5Carrot1 = new Shape(100, 100, 400, 300, "carrot",
                "", script0);
        Shape page5Carrot2 = new Shape(400, 200, 800, 400, "carrot",
                "", script0);
        Shape page5Carrot3 = new Shape(800, 100, 1200, 300, "carrot2",
                "", script0);
        Shape page5Text = new Shape(500, 500, 1000, 600, "", "You Win! Yay!",
                onEnterPlayVictory);
        page5Text.changeFont(70);

        game.newPage();
        Page page5 = game.getPageList().get(4);
        page5.addShape(page5Text);
        page5.addShape(page5Carrot1);
        page5.addShape(page5Carrot2);
        page5.addShape(page5Carrot3);

        // set current page to first page
        game.setCurrentPage("page1");

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