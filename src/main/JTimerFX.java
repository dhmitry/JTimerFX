package main;

import javafx.application.Application;
import javafx.stage.Stage;
import timer.Timer;

public class JTimerFX extends Application {

  public static void main(String[] args) {
    launch(args);
  }

  public void start(Stage stage) {
    stage.setTitle("JTimerFX");

    Timer timer = new Timer(stage);
    timer.open();
  }
}
