package timer;

import javafx.scene.control.ListCell;

public class IntervalCell extends ListCell<Interval> {
  protected void updateItem(Interval item, boolean empty) {
    super.updateItem(item, empty);

    setText(item == null ? "" : item.toString());
  }
}
