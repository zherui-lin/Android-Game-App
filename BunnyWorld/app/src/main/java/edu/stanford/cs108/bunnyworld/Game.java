package edu.stanford.cs108.bunnyworld;

import android.os.Handler;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.*;

public class Game implements Serializable{
    private static Game currentGame;
    private String name;
    private static boolean editorMode = true;

    private Shape shapeCopy;

    private List<Page> pageList;
    private int firstPage;
    private Shape selectedShape;
    private int currentPage;
    private int pageCount = 0;

    private Possession possession;
    public final int DEFAULT_CAPACITY = 10;

    public static final int SWITCH_DELAY = 750;


    public Game(String name) {
        // Game
        this.name = name;
        this.currentGame = this;

        // Initialize page
        this.pageList = new LinkedList<>();
        Page page1 = newPage();
        setFirstPage(page1);
        // Initialize possession
        this.possession = new Possession(DEFAULT_CAPACITY);

    }


    public static Game getCurrentGame() {
        return currentGame;
    }

    public static void setCurrentGame(Game game) {
        currentGame = game;
        PageView.setCurrentGame();
        PossessionView.setCurrentGame();
    }

    public void setCopyShape(Shape shape){
        shapeCopy = new Shape(shape);

    }

    public Shape getCopyShape(){
        return shapeCopy;
    }

    public static boolean isEditorMode() {
        return editorMode;
    }

    public static void setEditorMode() {
        editorMode = true;
    }

    public static void setPlayMode() {
        editorMode = false;
    }

    public String getName() {
        return name;
    }

    public Possession getPossession() {
        return possession;
    }

    public Page newPage(String name) {
        pageCount++;
        Page page = new Page(name);
        pageList.add(page);
        setCurrentPage(page);
        return page;
    }

    public Page newPage() {
        pageCount++;
        Page page = new Page(pageCount);
        pageList.add(page);
        setCurrentPage(page);
        return page;
    }

    public void deletePage(Page page) {
        int index = pageList.indexOf(page);
        if ((index <= currentPage && currentPage != 0)) {
            currentPage--;
        }
        if ((index <= firstPage && firstPage != 0)) {
            firstPage--;
        }
        pageList.remove(page);
    }

    // used for loading a game
    public void setFirstPage(Page page) {
        firstPage = pageList.indexOf(page);
    }

    // ONLY used for loading a game
    public void setPageList(List<Page> pageList) {
        this.pageList = pageList;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Page> getPageList() {
        return pageList;
    }

    @Nullable
    private Page findPage(String name) {
        for (Page page : pageList) {
            if (page.getName().equalsIgnoreCase(name)) {
                return page;
            }
        }
        return null;
    }

    @Nullable
    private Shape findShape(String shapeName) {
        for (Page page : pageList) {
            for (Shape shape : page.getShapeList()) {
                if (shape.getName().equalsIgnoreCase(shapeName)) {
                    return shape;
                }
            }
        }
        for (Shape shape : possession.getShape()) {
            if (shape != null) {
                if (shape.getName().equalsIgnoreCase(shapeName)) {
                    return shape;
                }
            }
        }
        return null;
    }

    /**
     * This function set the current page by name
     * @param name name of the page
     */
    public void setCurrentPage(String name) {
        Page page = null;
        if ((page = findPage(name)) != null) {
            setCurrentPage(page);
        }
    }

    /**
     * This function set the current page by pass in
     * page directly
     * @param page
     */
    public void setCurrentPage(Page page) {
        Page prevPage = null;
        if (isEditorMode()) {
            currentPage = pageList.indexOf(page);
        } else if (currentPage != pageList.indexOf(page)) {
            //pageList.get(currentPage).startFadingOut();
            prevPage = this.getCurrentPage();
            currentPage = pageList.indexOf(page);
            pageList.get(currentPage).startFadingIn();
            if (prevPage != null) {
                prevPage.changeFrontPaint(0);
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onEnter(page);
                }
            }, SWITCH_DELAY);
        }
    }

    /**
     * This function react to the on enter script of the shape
     * @param page
     */
    private void onEnter(Page page) {
        if (isEditorMode()) {
            return;
        }
        for (Shape shape : page.getShapeList()) {  // Add support for multiple action
            for (Script script : shape.getScriptList()) {
                if (script.getTrigger().equals("onenter")) {
                    startAction(script);
                }
            }
        }
    }

    public void addPage(Page page) {
        pageList.add(page);
        setCurrentPage(page);
    }

    public void setSelectShape(Shape shape) {
        selectedShape = shape;
    }

    public Shape getSelectShape() {
        return selectedShape;
    }

    public Page getCurrentPage() {
        return pageList.get(currentPage);
    }

    public Page getFirstPage() {
        return pageList.get(firstPage);
    }

    protected boolean startAction(Script script) {
        String object = script.getObject();
        switch (script.getAction()) {
            case "goto":
                return goTo(object);
            case "play":
                return play(object);
            case "hide":
                return hide(object);
            case "show":
                return show(object);
            //Mingxi-Add lock, unlock, animate, and stopanimate for script action
            case "lock":
                return lock(object);
            case "unlock":
                return unlock(object);
            case "animate":
                return animate(object);
            case "stopanimate":
                return stopAnimate(object);
        }
        return false;
    }

    protected boolean goTo(String pageName) {
        Page page = findPage(pageName);
        if (page == null) {
            return false;
        } else {
            setCurrentPage(page);
            return true;
        }
    }

    protected boolean play(String object) {
        switch (object) {
            case "carrotcarrotcarrot":
                PageView.carrotSound.start();
                break;
            case "evillaugh":
                PageView.evilLaughSound.start();
                break;
            case "fire":
                PageView.fireSound.start();
                break;
            case "hooray":
                PageView.hooraySound.start();
                break;
            case "munch":
                PageView.munchSound.start();
                break;
            case "munching":
                PageView.munchingSound.start();
                break;
            case "woof":
                PageView.woofSound.start();
                break;
            default:
                return false;
        }
        return true;
    }

    protected boolean hide(String shapeName) {
        Shape shape = findShape(shapeName);
        if (shape == null) {
            return false;
        } else {
            shape.setInvisible();
            return true;
        }
    }

    protected boolean show(String shapeName) {
        Shape shape = findShape(shapeName);
        if (shape == null) {
            return false;
        } else {
            shape.setVisible();
            return true;
        }
    }

    //Mingxi-Add lock, unlock, animate, and stopanimate for script action
    protected boolean lock(String shapeName) {
        Shape shape = findShape(shapeName);
        if (shape == null) {
            return false;
        } else {
            shape.setUnmovable();
            return true;
        }
    }

    protected boolean unlock(String shapeName) {
        Shape shape = findShape(shapeName);
        if (shape == null) {
            return false;
        } else {
            shape.setMovable();
            return true;
        }
    }

    protected boolean animate(String shapeName) {
        Shape shape = findShape(shapeName);
        if (shape == null) {
            return false;
        } else {
            if (!shape.getMovable()) {
                shape.setAnimatable();
            }
            return true;
        }
    }

    protected boolean stopAnimate(String shapeName) {
        Shape shape = findShape(shapeName);
        if (shape == null) {
            return false;
        } else {
            shape.setUnanimatable();
            return true;
        }
    }


}