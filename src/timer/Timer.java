package timer;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
  private int current;
  private int remaining;

  private Stage stage;
  private Label remainingLabel;
  private Button startButton;
  private Button pauseButton;
  private ListView<Interval> listView;
  private Button newButton;
  private Button removeButton;

  public Timer(Stage stage) {
    timeline = new Timeline();
    intervals = FXCollections.observableArrayList();
    remaining = 0;
    current = 0;
    this.stage = stage;
    remainingLabel = new Label();
    startButton = new Button("START");
    pauseButton = new Button("PAUSE");
    listView = new ListView<>(intervals);
    newButton = new Button("New interval");
    removeButton = new Button("Remove selected");
  }

  public void open() {
    initialize();
    stage.show();
  }

  private void initialize() {
    remainingLabel.setFont(Font.font("System", FontWeight.BOLD, 80));
    remainingLabel.setAlignment(Pos.CENTER);
    remainingLabel.setMaxWidth(Double.MAX_VALUE);
    reset();

    HBox.setHgrow(startButton, Priority.ALWAYS);
    HBox.setHgrow(pauseButton, Priority.ALWAYS);
    startButton.setMaxWidth(Double.MAX_VALUE);
    pauseButton.setMaxWidth(Double.MAX_VALUE);

    startButton.setOnAction(actionEvent -> start());
    pauseButton.setOnAction(actionEvent -> pause());

    listView.setEditable(true);
    listView.setPrefHeight(150);
    listView.setCellFactory(intervalListView -> new IntervalCell());
    listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    VBox.setVgrow(listView, Priority.ALWAYS);

    for (int i = 0; i < 5; i++) {
      intervals.add(new Interval("Interval #" + (i + 1), "00:05"));
    }

    newButton.setOnAction(actionEvent -> {
      intervals.add(new Interval());

      listView.getSelectionModel().select(intervals.size() - 1);
      listView.layout();

      listView.edit(intervals.size() - 1);
    });

    removeButton.setOnAction(
      actionEvent -> intervals.removeAll(listView.getSelectionModel().getSelectedItems()));

    HBox.setHgrow(newButton, Priority.ALWAYS);
    HBox.setHgrow(removeButton, Priority.ALWAYS);
    newButton.setMaxWidth(Double.MAX_VALUE);
    removeButton.setMaxWidth(Double.MAX_VALUE);

    ComboBox<String> comboBox = new ComboBox<>();
    comboBox.setMaxWidth(Double.MAX_VALUE);
    comboBox.getItems().add("Default");
    comboBox.getSelectionModel().select(0);

    VBox layout =
      new VBox(5, remainingLabel, new HBox(5, startButton, pauseButton), new Separator(), comboBox,
        listView, new HBox(5, newButton, removeButton));
    layout.setPadding(new Insets(5));

    Scene scene = new Scene(layout);

    stage.setScene(scene);
  }

  private void start() {
    if (timeline.getStatus() == Animation.Status.PAUSED) {
      timeline.play();
      startButton.setText("START");
      startButton.setDisable(true);
      pauseButton.setText("PAUSE");
      pauseButton.setDisable(false);
    } else if (intervals.size() > 0) {
      reset();
      updateIntervals();
      disableUi();

      remaining = intervals.get(0).getDuration();

      timeline.getKeyFrames()
        .add(new KeyFrame(Duration.millis(UPDATE_RATE), actionEvent -> update()));
      timeline.setCycleCount(Animation.INDEFINITE);
      timeline.play();
    }
  }

  private void pause() {
    if (timeline.getStatus() == Animation.Status.RUNNING) {
      startButton.setText("RESUME");
      startButton.setDisable(false);
      pauseButton.setText("STOP");
    } else if (timeline.getStatus() == Animation.Status.PAUSED) {
      reset();
      pauseButton.setText("PAUSE");
    }

    timeline.pause();
    //timeline.getKeyFrames().clear();
  }

  private void reset() {
    timeline.stop();
    timeline.getKeyFrames().clear();
    remaining = 0;
    current = 0;
    updateLabel();
    for (Interval interval : intervals) {
      interval.setState(IntervalState.Default);
    }
    listView.refresh();
    startButton.setText("START");
    pauseButton.setText("PAUSE");
    enableUi();
  }

  private void update() {
    if (remaining <= 0) {
      done();
    } else {
      remaining -= UPDATE_RATE;
      updateLabel();
    }
  }

  private void updateLabel() {
    remainingLabel.setText(Interval.msToString(remaining + 1000 - UPDATE_RATE));
  }

  private void updateIntervals() {
    if (!intervals.isEmpty()) {
      if (current > 0) {
        intervals.get(current - 1).setState(IntervalState.Previous);
      }
      if (current > 1) {
        intervals.get(current - 2).setState(IntervalState.Finished);
      }

      intervals.get(current).setState(IntervalState.Current);

      if (current < intervals.size() - 1) {
        intervals.get(current + 1).setState(IntervalState.Next);
      }

      listView.refresh();
    }
  }

  private void done() {
    AudioClip clip = new AudioClip(new File("done.wav").toURI().toString());

    if (current == intervals.size() - 1) {
      reset();
      clip.setCycleCount(3);

      AudioClip clip2 = new AudioClip(new File("timer.wav").toURI().toString());
      clip2.setCycleCount(7);
      clip2.play();
    } else {
      remaining = intervals.get(++current).getDuration();
      updateLabel();
      updateIntervals();
    }

    clip.play();
  }

  private void disableUi() {
    startButton.setDisable(true);
    pauseButton.setDisable(false);
    listView.setMouseTransparent(true);
    listView.setFocusTraversable(false);
    listView.getSelectionModel().select(-1);
    newButton.setDisable(true);
    removeButton.setDisable(true);
  }

  private void enableUi() {
    startButton.setDisable(false);
    pauseButton.setDisable(true);
    listView.setMouseTransparent(false);
    listView.setFocusTraversable(true);
    newButton.setDisable(false);
    removeButton.setDisable(false);
  }
}
