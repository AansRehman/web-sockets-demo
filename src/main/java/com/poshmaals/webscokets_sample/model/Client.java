package com.poshmaals.webscokets_sample.model;


public class Client {
    private Integer id;
    private String name;
    private String phNo;

    public Client(Integer id, String name, String phNo) {
        this.id = id;
        this.name = name;
        this.phNo = phNo;
    }

    public Client() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhNo() {
        return phNo;
    }

    public void setPhNo(String phNo) {
        this.phNo = phNo;
    }
}
