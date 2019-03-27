package timer;

import java.io.*;
import java.util.ArrayList;

public class IntervalFile {
  public static final String EXTENSION = ".timer";

  public static ArrayList<Interval> load(String filename) {
    try (var in = new BufferedReader(new FileReader(filename))) {
      ArrayList<Interval> intervals = new ArrayList<>();

      String line;
      while ((line = in.readLine()) != null) {
        if (!line.matches("\\d\\d:\\d\\d .*")) {
          throw new IllegalArgumentException("Invalid line format");
        }

        String label = line.substring(6);
        String duration = line.substring(0, 5);
        intervals.add(new Interval(label, duration));
      }

      return intervals;
    } catch (final IOException e) {
      throw new IllegalArgumentException("Invalid timer preset file");
    }
  }

  public static void save(ArrayList<Interval> intervals, String filename) {
    try (var out = new BufferedWriter(new FileWriter(filename))) {
      for (Interval i : intervals) {
        out.write(i.toString() + "\n");
      }
    } catch (final IOException e) {
      throw new IllegalArgumentException("Invalid timer preset filename");
    }
  }
}
