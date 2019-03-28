package timer;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
  private static final String PRESET_DIR = "presets/";

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
  private Button renamePresetButton;
  private Button removePresetButton;
  private CheckBox repeatCheckBox;
  private ListView<Interval> listView;
  private Button newIntervalButton;
  private Button removeIntervalButton;

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
    renamePresetButton = new Button("Rename");
    removePresetButton = new Button("Remove");
    repeatCheckBox = new CheckBox("Repeat");
    listView = new ListView<>(intervals);
    newIntervalButton = new Button("New interval");
    removeIntervalButton = new Button("Remove selected");
  }

  public void open() {
    initialize();
    stage.show();
    stage.setMinHeight(stage.getHeight());
    stage.setMinWidth(stage.getWidth());
  }

  private void initialize() {
    reset();

    initializePresets();
    initializeUi();
  }

  private void initializePresets() {
    File dir = new File(PRESET_DIR);
    if (!dir.exists()) {
      if (!dir.mkdir()) {
        Alert alert =
          new Alert(Alert.AlertType.ERROR, "Could not create an empty directory for presets");
        alert.showAndWait();
        Platform.exit();
      }
    }

    File[] files = dir.listFiles((file, name) -> name.endsWith(PresetFile.EXTENSION));

    presets.add("Default");
    if (files != null) {
      Arrays.sort(files);
      for (File f : files) {
        String name = f.getName();
        presets.add(name.substring(0, name.length() - PresetFile.EXTENSION.length()));
      }
    }
    presets.add("<new preset>");
  }

  private void initializeUi() {
    remainingLabel.setFont(Font.font("System", FontWeight.BOLD, 80));
    remainingLabel.setAlignment(Pos.CENTER);
    remainingLabel.setMaxWidth(Double.MAX_VALUE);

    HBox.setHgrow(startButton, Priority.ALWAYS);
    HBox.setHgrow(pauseButton, Priority.ALWAYS);
    startButton.setMaxWidth(Double.MAX_VALUE);
    pauseButton.setMaxWidth(Double.MAX_VALUE);

    listView.setEditable(true);
    listView.setPrefHeight(150);
    listView.setCellFactory(intervalListView -> new IntervalCell());
    listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    VBox.setVgrow(listView, Priority.ALWAYS);

    HBox.setHgrow(newIntervalButton, Priority.ALWAYS);
    HBox.setHgrow(removeIntervalButton, Priority.ALWAYS);
    newIntervalButton.setMaxWidth(Double.MAX_VALUE);
    removeIntervalButton.setMaxWidth(Double.MAX_VALUE);

    presetComboBox.setMaxWidth(Double.MAX_VALUE);
    presetComboBox.setItems(presets);

    HBox presetButtons = new HBox(5, renamePresetButton, removePresetButton);

    AnchorPane anchorPane = new AnchorPane(presetButtons, repeatCheckBox);
    AnchorPane.setRightAnchor(repeatCheckBox, 5.0);
    AnchorPane.setTopAnchor(repeatCheckBox, 5.0);

    VBox layout =
      new VBox(5, remainingLabel, new HBox(5, startButton, pauseButton), presetComboBox, anchorPane,
        new Separator(), listView, new HBox(5, newIntervalButton, removeIntervalButton));
    layout.setPadding(new Insets(5));

    // initialize button actions and set appropriate callbacks
    initializeActions();

    // needs to be called after initializing actions so there are some default intervals
    presetComboBox.getSelectionModel().select(0);

    Scene scene = new Scene(layout);
    stage.setScene(scene);
  }

  private void initializeActions() {
    // button actions
    startButton.setOnAction(actionEvent -> start());
    pauseButton.setOnAction(actionEvent -> pause());

    renamePresetButton.setOnAction(actionEvent -> renameCurrentPreset());
    removePresetButton.setOnAction(actionEvent -> removeCurrentPreset());

    newIntervalButton.setOnAction(actionEvent -> {
      intervals.add(new Interval());

      listView.getSelectionModel().select(-1);
      listView.getSelectionModel().select(intervals.size() - 1);
      listView.layout();

      listView.edit(intervals.size() - 1);
    });

    removeIntervalButton.setOnAction(
      actionEvent -> intervals.removeAll(listView.getSelectionModel().getSelectedItems()));

    // other callbacks
    presetComboBox.getSelectionModel().selectedIndexProperty().addListener(
      (observableValue, oldIndex, newIndex) -> changePreset(oldIndex.intValue(),
        newIndex.intValue()));
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
      //clip.setCycleCount(3);

      AudioClip clip2 = new AudioClip(new File("timer.wav").toURI().toString());
      clip2.setCycleCount(4);
      clip2.play();

      if (repeatCheckBox.isSelected()) {
        start();
      }
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
    renamePresetButton.setDisable(true);
    removePresetButton.setDisable(true);
    listView.setMouseTransparent(true);
    listView.setFocusTraversable(false);
    listView.getSelectionModel().select(-1);
    newIntervalButton.setDisable(true);
    removeIntervalButton.setDisable(true);
  }

  private void stopUi() {
    startButton.setDisable(false);
    pauseButton.setDisable(true);
    presetComboBox.setDisable(false);
    renamePresetButton.setDisable(false);
    removePresetButton.setDisable(false);
    listView.setMouseTransparent(false);
    listView.setFocusTraversable(true);
    newIntervalButton.setDisable(false);
    removeIntervalButton.setDisable(false);
  }

  private void changePreset(int oldIndex, int newIndex) {
    if (oldIndex > 0 && oldIndex < presets.size() - 1) {
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

    String filename = PRESET_DIR + presets.get(i) + PresetFile.EXTENSION;
    PresetFile.save(new ArrayList<>(intervals), filename);
  }

  public void saveCurrentPreset() {
    int currentIndex = presetComboBox.getSelectionModel().getSelectedIndex();
    if (currentIndex != 0 && currentIndex != presets.size() - 1) {
      savePreset(currentIndex);
    }
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
        intervals.add(new Interval(duration, "Interval #" + j));
      }
    } else if (i == presets.size() - 1) {
      presetComboBox.getSelectionModel().select(0);

      String name = getNewPresetName("");
      if (!name.equals("")) {
        // create an empty preset file
        PresetFile.save(null, PRESET_DIR + name + PresetFile.EXTENSION);

        presets.add(presets.size() - 1, name);
        presetComboBox.getSelectionModel().select(presets.size() - 2);
      }
    } else {
      String filename = PRESET_DIR + presets.get(i) + PresetFile.EXTENSION;
      try {
        intervals.addAll(PresetFile.load(filename));
      } catch (final Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
        alert.show();
        intervals.clear();
      }
    }
  }

  private void renameCurrentPreset() {
    int currentIndex = presetComboBox.getSelectionModel().getSelectedIndex();

    if (currentIndex != 0 && currentIndex != presets.size() - 1) {
      String newName = getNewPresetName(presetComboBox.getValue());
      if (!newName.equals("")) {
        String oldFilename = PRESET_DIR + presetComboBox.getValue() + PresetFile.EXTENSION;
        String newFilename = PRESET_DIR + newName + PresetFile.EXTENSION;

        File oldFile = new File(oldFilename);
        File newFile = new File(newFilename);

        if (!oldFile.renameTo(newFile)) {
          Alert alert = new Alert(Alert.AlertType.ERROR, "Could not rename the current preset");
          alert.show();
        } else {
          presets.set(currentIndex, newName);
        }
      }
    }
  }

  private void removeCurrentPreset() {
    int toDelete = presetComboBox.getSelectionModel().getSelectedIndex();

    if (toDelete != 0 && toDelete != presets.size() - 1) {
      String filename = PRESET_DIR + presetComboBox.getValue() + PresetFile.EXTENSION;
      File file = new File(filename);

      presetComboBox.getSelectionModel().select(0);
      presets.remove(toDelete);

      if (!file.delete()) {
        Alert alert = new Alert(Alert.AlertType.ERROR, "Could not remove the current preset");
        alert.show();
      }
    }
  }

  private String getNewPresetName(String filler) {
    TextInputDialog dialog = new TextInputDialog(filler);
    dialog.setTitle("New preset name");
    dialog.setGraphic(null);
    dialog.setHeaderText("Please enter a new name");

    return dialog.showAndWait().orElse("");
  }
}
