package timer;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class IntervalCell extends ListCell<Interval> {
  String prevText;

  protected void updateItem(Interval item, boolean empty) {
    super.updateItem(item, empty);

    if (item == null) {
      setText("");
      setBackground(Background.EMPTY);
      return;
    }

    setText(item.toString());
    setTextFill(Color.BLACK);

    Color color;

    if (isSelected()) {
      color = Color.web("#413E4A");
      setTextFill(Color.WHITE);
    } else {
      switch (item.getState()) {
        case Current:
          color = Color.web("#413E4A");
          setTextFill(Color.WHITE);
          break;
        case Previous:
        case Finished:
          color = Color.LIGHTGREY;
          break;
        default:
          color = Color.WHITE;
      }
    }

    setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
  }

  public void startEdit() {
    prevText = getText();
    super.startEdit();

    TextField durationField = new TextField(prevText.substring(0, 5));
    TextField labelField = new TextField(prevText.substring(6));
    durationField.setPromptText("mm:ss");
    labelField.setPromptText("label");

    HBox hbox = new HBox(5, durationField, labelField);

    durationField.setMaxWidth(55);
    labelField.setPrefWidth(50);

    HBox.setHgrow(labelField, Priority.ALWAYS);

    durationField.setOnAction(actionEvent -> {
      String duration = durationField.getText();
      if (Interval.isValidDuration(duration)) {
        commitEdit(new Interval(labelField.getText(), durationField.getText()));
      } else {
        cancelEdit();
      }
    });
    labelField.setOnAction(durationField.getOnAction());

    textProperty().set(null);
    graphicProperty().set(hbox);

    Platform.runLater(durationField::requestFocus);
  }

  public void cancelEdit() {
    super.cancelEdit();

    graphicProperty().set(null);
    textProperty().set(prevText);
  }

  public void commitEdit(Interval newInterval) {
    super.commitEdit(newInterval);

    graphicProperty().set(null);
    textProperty().set(newInterval.toString());
  }
}
