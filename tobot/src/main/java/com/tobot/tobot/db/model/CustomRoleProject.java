package com.tobot.tobot.db.model;

import java.util.List;

public class CustomRoleProject {

    public static final String FIELD_ID="projectId";
    public static final String FIELD_CONTENT="content";
    public static final String FIELD_ANSWER="answer";

    private int id;
    private String content;
    private String answer;

    private List<CustomRoleQuestion>questionList;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<CustomRoleQuestion> getQuestionList() {
        return questionList;
    }

    public void setQuestionList(List<CustomRoleQuestion> questionList) {
        this.questionList = questionList;
    }
}
