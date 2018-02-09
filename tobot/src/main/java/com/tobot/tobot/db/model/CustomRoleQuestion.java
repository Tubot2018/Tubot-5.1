package com.tobot.tobot.db.model;

import rx.functions.FuncN;

public class CustomRoleQuestion {

    public static final String FIELD_ID="id";
    public static final String FIELD_QUESTION="question";

    private int id;
    private String question;

    private CustomRoleProject customRoleProject;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public CustomRoleProject getCustomRoleProject() {
        return customRoleProject;
    }

    public void setCustomRoleProject(CustomRoleProject customRoleProject) {
        this.customRoleProject = customRoleProject;
    }

}
