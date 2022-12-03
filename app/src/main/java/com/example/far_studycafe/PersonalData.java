package com.example.far_studycafe;

public class PersonalData {

    private String member_id;
    private String member_title;
    private String member_snippet;
    private String member_latitude;
    private String member_longitude;

    public String getMember_id() {
        return member_id;
    }

    public String getMember_title() {
        return member_title;
    }

    public String getMember_snippet() {
        return member_snippet;
    }

    public String getMember_latitude() {
        return member_latitude;
    }

    public String getMember_longitude() {
        return member_longitude;
    }

    public void setMember_id(String member_id) {
        this.member_id = member_id;
    }

    public void setMember_title(String member_title) {
        this.member_title = member_title;
    }

    public void setMember_snippet(String member_snippet) {
        this.member_snippet = member_snippet;
    }

    public void setMember_latitude(String member_latitude) { this.member_latitude = member_latitude; }

    public void setMember_longitude(String member_longitude) { this.member_longitude = member_longitude; }

}
