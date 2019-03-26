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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class Timer {
  private static final int UPDATE_RATE = 50;

  private Timeline timeline;
  private ObservableList<String> presets;
  private ObservableList<Interval> intervals;
  private int current;
  private int remaining;

  private Stage stage;
  private Label remainingLabel;
  private Button startButton;
  private Button pauseButton;
  private ComboBox<String> presetComboBox;
  private ListView<Interval> listView;
  private Button newButton;
  private Button removeButton;

  public Timer(Stage stage) {
    timeline = new Timeline();
    presets = FXCollections.observableArrayList();
    intervals = FXCollections.observableArrayList();
    current = 0;
    remaining = 0;

    this.stage = stage;
    remainingLabel = new Label();
    startButton = new Button("START");
    pauseButton = new Button("PAUSE");
    presetComboBox = new ComboBox<>();
    listView = new ListView<>(intervals);
    newButton = new Button("New interval");
    removeButton = new Button("Remove selected");
  }

  public void open() {
    initialize();
    stage.show();
    stage.setMinHeight(stage.getHeight());
    stage.setMinWidth(stage.getWidth());
  }

  private void initialize() {
    // TODO maybe use multiple tabs instead of a single layout, i.e.
    //  a timer tab and a preset/interval tab

    File dir = new File("presets/");
    File[] files = dir.listFiles((file, name) -> name.endsWith(".timer"));

    presets.add("Default");
    if (files != null) {
      Arrays.sort(files);
      for (File f : files) {
        String name = f.getName();
        presets.add(name.substring(0, name.length() - IntervalFile.EXTENSION.length()));
      }
    }
    presets.add("<new preset>");

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

    newButton.setOnAction(actionEvent -> {
      intervals.add(new Interval());

      listView.getSelectionModel().select(-1);
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

    presetComboBox.setMaxWidth(Double.MAX_VALUE);
    presetComboBox.setItems(presets);
    presetComboBox.getSelectionModel().selectedIndexProperty().addListener(
      (observableValue, oldIndex, newIndex) -> changePreset(oldIndex.intValue(),
        newIndex.intValue()));

    presetComboBox.getSelectionModel().select(0);

    CheckBox checkBox = new CheckBox("Repeat");
    Button removePreset = new Button("Remove");
    Button renamePreset = new Button("Rename");

    HBox presetButtons = new HBox(5, renamePreset, removePreset);

    AnchorPane anchorPane = new AnchorPane(presetButtons, checkBox);
    AnchorPane.setRightAnchor(checkBox, 5.0);
    AnchorPane.setTopAnchor(checkBox, 5.0);

    VBox layout =
      new VBox(5, remainingLabel, new HBox(5, startButton, pauseButton), presetComboBox, anchorPane,
        new Separator(), listView, new HBox(5, newButton, removeButton));
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
      startUi();

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
    listView.scrollTo(0);
    startButton.setText("START");
    pauseButton.setText("PAUSE");
    stopUi();
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
      listView.scrollTo(Math.max(0, current - 3));
      updateLabel();
      updateIntervals();
    }

    clip.play();
  }

  private void startUi() {
    startButton.setDisable(true);
    pauseButton.setDisable(false);
    presetComboBox.setDisable(true);
    listView.setMouseTransparent(true);
    listView.setFocusTraversable(false);
    listView.getSelectionModel().select(-1);
    newButton.setDisable(true);
    removeButton.setDisable(true);
  }

  private void stopUi() {
    startButton.setDisable(false);
    pauseButton.setDisable(true);
    presetComboBox.setDisable(false);
    listView.setMouseTransparent(false);
    listView.setFocusTraversable(true);
    newButton.setDisable(false);
    removeButton.setDisable(false);
  }

  private void changePreset(int oldIndex, int newIndex) {
    if (oldIndex > 0) {
      savePreset(oldIndex);
    }

    if (newIndex >= 0) {
      loadPreset(newIndex);
    }
  }

  private void savePreset(int i) {
    if (i <= 0 || i >= presets.size()) {
      throw new IllegalArgumentException(
        "Index must be between 0 and the number of presets (excl.)");
    }

    String filename = "presets/" + presets.get(i) + IntervalFile.EXTENSION;
    IntervalFile.save(new ArrayList<>(intervals), filename);
  }

  private void loadPreset(int i) {
    if (i < 0 || i >= presets.size()) {
      throw new IllegalArgumentException(
        "Index must be between 1 and the number of presets (excl.)");
    }

    intervals.clear();

    if (i == 0) {
      for (int j = 1; j <= 3; j++) {
        String duration = String.format("00:%02d", 5 * j);
        intervals.add(new Interval("Interval #" + j, duration));
      }
    } else {
      String filename = "presets/" + presets.get(i) + IntervalFile.EXTENSION;
      intervals.addAll(IntervalFile.load(filename));
    }
  }
}
