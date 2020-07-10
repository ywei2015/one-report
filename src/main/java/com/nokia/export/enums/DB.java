package com.nokia.export.enums;

public enum DB {
    db1("db1","FAST_ORACLE_1"),
    db2("db2","FAST_ORACLE_2");

    private String name;
    private String fileName;

    DB(String name,String fileName) {
        this.name = name;
        this.fileName = fileName;
    }

    public static DB fromName(String name){
        for (DB db : DB.values()){
            if (db.getName().equalsIgnoreCase(name)){
                return db;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
