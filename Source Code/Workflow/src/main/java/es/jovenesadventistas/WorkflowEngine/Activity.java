package es.jovenesadventistas.WorkflowEngine;

public class Activity {
    String name;
    public Activity(String name){
        this.name = name;
    }

    public void execute(){

    }

    public String toString(){
        return this.name;
    }
}
