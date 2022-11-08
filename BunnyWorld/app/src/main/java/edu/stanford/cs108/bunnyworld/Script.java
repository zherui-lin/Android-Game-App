package edu.stanford.cs108.bunnyworld;

import java.io.Serializable;

public class Script implements Serializable {
    private String trigger;
    private String dropShape;
    private String action;
    private String object;
    private String shapeName;

    public Script (String t, String a, String o){
        this.trigger = t;
        this.action = a;
        this.object = o;
        this.dropShape = "";
    }

    public Script (String t, String s, String a, String o){
        this.trigger = t;
        this.dropShape = s;
        this.action = a;
        this.object = o;
    }


    public Script(String script){
        String[] scriptSplit = script.split(" ");
        trigger = scriptSplit[0] + scriptSplit[1];
        if (trigger.equals("ondrop")) {
            dropShape = scriptSplit[2];
            action = scriptSplit[3];
            object = scriptSplit[4];
        } else {
            dropShape = "";
            action = scriptSplit[2];
            object = scriptSplit[3];
        }

    }

    public String getTrigger(){
        return trigger;
    }

    public String getAction(){
        return action;
    }

    public String getObject(){
        return object;
    }

    public String getDropShape() {
        return dropShape;
    }
}