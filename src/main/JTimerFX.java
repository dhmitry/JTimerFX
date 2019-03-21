package main;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;

public class JTimerFX extends Application {
  private static final int UPDATE_RATE = 50;

  private Timeline timer;
  private long remaining;

  private Stage stage;
  private Label label;

  public static void main(String[] args) {
    launch(args);
  }

  public void start(Stage stage) {
    this.stage = stage;
    stage.setTitle("JTimerFX");
    initialize();

    stage.show();
  }

  private void initialize() {
    timer = new Timeline();

    label = new Label();
    label.setFont(new Font(60));

    reset();

    Button start = new Button("START");
    Button pause = new Button("PAUSE");
    Button reset = new Button("RESET");

    Image playImg = new Image("file:play.png");
    start.setGraphic(new ImageView(playImg));

    start.setOnAction(actionEvent -> start());
    pause.setOnAction(actionEvent -> pause());
    reset.setOnAction(actionEvent -> reset());

    VBox.setVgrow(start, Priority.ALWAYS);
    VBox.setVgrow(pause, Priority.ALWAYS);
    VBox.setVgrow(reset, Priority.ALWAYS);
    start.setMaxWidth(Double.MAX_VALUE);
    pause.setMaxWidth(Double.MAX_VALUE);
    reset.setMaxWidth(Double.MAX_VALUE);

    HBox hbox = new HBox(5, label, new VBox(5, start, pause, reset));

    ListView<String> list = new ListView<>();

    VBox.setVgrow(list, Priority.ALWAYS);

    VBox layout = new VBox(5, hbox, list);
    layout.setPadding(new Insets(5));

    Scene scene = new Scene(layout);

    stage.setScene(scene);
  }

  public void start() {
    if (remaining > 0) {
      pause();

      timer.getKeyFrames().add(new KeyFrame(Duration.millis(UPDATE_RATE), actionEvent -> update()));
      timer.setCycleCount(Animation.INDEFINITE);
      timer.play();
    }
  }

  public void pause() {
    timer.stop();
    timer.getKeyFrames().clear();
  }

  public void reset() {
    pause();
    remaining = 5000;
    updateLabel();
  }

  private void update() {
    if (remaining <= 0) {
      reset();
      done();
    } else {
      remaining -= UPDATE_RATE;
      updateLabel();
    }
  }

  private void updateLabel() {
    label.setText(formatMilliseconds(remaining));
  }

  private String formatMilliseconds(long ms) {
    int secs = (int) (ms / 1000 % 60);
    int mins = (int) (ms / 1000 / 60);

    return String.format("%02d:%02d", mins, secs);
  }

  private void done() {
    AudioClip clip = new AudioClip(new File("done.wav").toURI().toString());
    clip.play();
  }
}
