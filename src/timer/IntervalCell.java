package timer;

import javafx.application.Platform;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class IntervalCell extends ListCell<Interval> {
  String prevText;

  protected void updateItem(Interval item, boolean empty) {
    super.updateItem(item, empty);

    setText(item == null ? "" : item.toString());
  }

  public void startEdit() {
    prevText = getText();
    super.startEdit();

    TextField durationField = new TextField();
    TextField labelField = new TextField();
    durationField.setPromptText("mm:ss");
    labelField.setPromptText("label");

    HBox hbox = new HBox(5, durationField, labelField);

    durationField.setMaxWidth(50);
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

    graphicProperty().set(hbox);
    textProperty().set(null);

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
