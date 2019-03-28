package main;

import javafx.application.Application;
import javafx.stage.Stage;
import timer.Timer;

public class JTimerFX extends Application {
  private Timer timer;

  public static void main(String[] args) {
    launch(args);
  }

  public void start(Stage stage) {
    stage.setTitle("JTimerFX");

    timer = new Timer(stage);
    timer.open();
  }

  public void stop() {
    timer.saveCurrentPreset();
  }
}
