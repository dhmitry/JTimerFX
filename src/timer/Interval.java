package timer;

public class Interval {
  // maximum duration is 59:59
  public static final int MIN_DURATION = 1000;
  public static final int MAX_DURATION = 59 * 59 * 1000;

  private String label;
  private int duration;
  private IntervalState state;

  public Interval(String label, String duration) {
    this.label = label;

    if (isValidDuration(duration)) {
      this.duration = stringToMs(duration);
    } else {
      throw new IllegalArgumentException("Duration must be between 0-" + MAX_DURATION);
    }

    this.state = IntervalState.Default;
  }

  public Interval() {
    this("Interval", msToString(MIN_DURATION));
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public int getDuration() {
    return duration;
  }

  public void setDuration(String duration) {
    if (isValidDuration(duration)) {
      this.duration = stringToMs(duration);
    } else {
      throw new IllegalArgumentException(
        "Duration must be between " + MIN_DURATION + "-" + MAX_DURATION);
    }
  }

  public IntervalState getState() {
    return state;
  }

  public void setState(IntervalState state) {
    this.state = state;
  }

  public String toString() {
    return "[" + msToString(duration) + "] " + label;
  }

  public static boolean isValidDuration(String duration) {
    try {
      int ms = stringToMs(duration);
      return ms >= MIN_DURATION && ms <= MAX_DURATION;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static String msToString(int ms) {
    if (ms < 0 || ms > MAX_DURATION) {
      throw new IllegalArgumentException("Duration must be between 0-" + MAX_DURATION);
    } else {
      int secs = ms / 1000 % 60;
      int mins = ms / 1000 / 60;

      return String.format("%02d:%02d", mins, secs);
    }
  }

  public static int stringToMs(String duration) {
    if (!duration.matches("\\d\\d:\\d\\d")) {
      throw new IllegalArgumentException("Invalid format");
    }

    try {
      int mins = Integer.valueOf(duration.substring(0, 2));
      int secs = Integer.valueOf(duration.substring(3, 5));

      int ms = (mins * 60 + secs) * 1000;

      if (ms < MIN_DURATION || ms > MAX_DURATION) {
        throw new IllegalArgumentException(
          "Duration must be between " + MIN_DURATION + "-" + MAX_DURATION);
      } else {
        return ms;
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid format");
    }
  }
}
