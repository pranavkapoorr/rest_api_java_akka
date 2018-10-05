package com.pranavkapoorr;

import com.pranavkapoorr.myhttp.MyRestHttp;

import akka.actor.ActorSystem;

public class App {

  public static void main(String[] args) throws Exception {
    MyRestHttp restServer = MyRestHttp.getInstance(ActorSystem.create(),8080);
    restServer.bind();
    
  }
}