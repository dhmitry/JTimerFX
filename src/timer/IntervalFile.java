package timer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class IntervalFile {
  public static final String EXTENSION = ".timer";

  public static ArrayList<Interval> load(String filename) {
    System.out.println("LOADING " + filename);

    ArrayList<Interval> intervals = new ArrayList<>();
    try (var in = new BufferedReader(new FileReader(filename))) {
      String line;
      while ((line = in.readLine()) != null) {
        System.out.println(line);
      }
    } catch (IOException e) {
      System.out.println("Invalid timer preset file: " + e.getMessage());
    }

    return intervals;
  }

  public static void save(ArrayList<Interval> intervals, String filename) {
    System.out.println("SAVING " + filename);
    for (Interval i : intervals) {
      System.out.println(i);
    }
  }
}
