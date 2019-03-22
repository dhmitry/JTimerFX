package timer;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;

public class Timer {
  private static final int UPDATE_RATE = 50;

  private Timeline timeline;
  private ObservableList<Interval> intervals;
  private int remaining;

  private Stage stage;
  private Label time;

  public Timer(Stage stage) {
    timeline = new Timeline();
    intervals = FXCollections.observableArrayList();
    remaining = 0;
    this.stage = stage;
    time = new Label();
  }

  public void open() {
    initialize();
    stage.show();
  }

  private void initialize() {
    time.setFont(Font.font("System", FontWeight.BOLD,60));
    time.setAlignment(Pos.CENTER);
    time.setMaxWidth(Double.MAX_VALUE);
    reset();

    Button start = new Button("START");
    Button pause = new Button("PAUSE");
    Button reset = new Button("RESET");
    HBox.setHgrow(start, Priority.ALWAYS);
    HBox.setHgrow(pause, Priority.ALWAYS);

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

    ListView<Interval> list = new ListView<>(intervals);
//    list.setMouseTransparent(true);
//    list.setFocusTraversable(false);
    VBox.setVgrow(list, Priority.ALWAYS);

    intervals.add(new Interval("Interval #1", "00:10"));
    intervals.add(new Interval("Interval #2", "00:15"));
    intervals.add(new Interval("Interval #3", "00:20"));



    VBox layout = new VBox(5, time, new HBox(5, start, pause), list);
    layout.setPadding(new Insets(5));

    Scene scene = new Scene(layout);

    stage.setScene(scene);
  }

  private void start() {
    if (remaining > 0) {
      pause();

      timeline.getKeyFrames()
        .add(new KeyFrame(Duration.millis(UPDATE_RATE), actionEvent -> update()));
      timeline.setCycleCount(Animation.INDEFINITE);
      timeline.play();
    }
  }

  private void pause() {
    timeline.stop();
    timeline.getKeyFrames().clear();
  }

  private void reset() {
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
    time.setText(Interval.msToString(remaining));
  }

  private void done() {
    AudioClip clip = new AudioClip(new File("done.wav").toURI().toString());
    clip.play();
  }
}
