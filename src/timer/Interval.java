package timer;

public class Interval {
  // maximum duration is 59:59
  public static final int MAX_DURATION = 59 * 59 * 1000;

  private String label;
  private int duration;

  public Interval(String label, String duration) {
    this.label = label;

    if (isValidDuration(stringToMs(duration))) {
      this.duration = stringToMs(duration);
    } else {
      throw new IllegalArgumentException("Duration must be between 0-" + MAX_DURATION);
    }
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

  public void setDuration(int duration) {
    if(isValidDuration(duration)) {
      this.duration = duration;
    } else {
      throw new IllegalArgumentException("Duration must be between 0-" + MAX_DURATION);
    }
  }

  public String toString() {
    return "[" + msToString(duration) + "] " + label;
  }

  public static boolean isValidDuration(int duration) {
    return duration >= 0 && duration <= MAX_DURATION;
  }

  public static String msToString(int ms) {
    if (ms < 0 || ms > MAX_DURATION) {
      return "00:00";
    } else {
      int secs = (int) (ms / 1000 % 60);
      int mins = (int) (ms / 1000 / 60);

      return String.format("%02d:%02d", mins, secs);
    }
  }

  public static int stringToMs(String duration) {
    if (!duration.matches("\\d\\d:\\d\\d")) {
      return 0;
    }

    try {
      int mins = Integer.valueOf(duration.substring(0, 2));
      int secs = Integer.valueOf(duration.substring(3, 5));

      int ms = (mins * 60 + secs) * 1000;

      return ms > MAX_DURATION ? 0 : ms;
    } catch (NumberFormatException e) {
      return 0;
    }
  }
}
