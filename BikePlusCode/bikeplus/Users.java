package com.mg.mig.bikeplus;

/**
 * Created by miguelguevara on 4/25/16.
 */
public class Users {
    private int id;
    private String username;
    private String password;

    public Users(){

    }

    public Users(int id, String username, String password){
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public Users(String username, String password){
        this.username = username;
        this.password = password;
    }
    public void setId(int id){
        this.id = id;
    }
    public int getId(){
        return this.id;
    }
    public void setUsername(String username){
        this.username = username;
    }
    public String getUsername(){
        return this.username;
    }
    public void setPassword(String password){
        this.password = password;
    }
    public String getPassword(){
        return this.password;
    }


}
