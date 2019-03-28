package timer;

import java.io.*;
import java.util.ArrayList;

/**
 * This class contains static functions for loading and saving timer preset files.
 */
public class PresetFile {
  public static final String EXTENSION = ".timer";

  /**
   * Loads intervals from the specified file. Each line must be in the format:
   * </p>mm:ss INTERVAL_NAME</p>.
   *
   * @param path the path to the preset file
   *
   * @return an ArrayList of Interval objects loaded from the given file
   *
   * @throws IllegalArgumentException if the file is invalid or the format is not correct
   */
  public static ArrayList<Interval> load(String path) {
    try (var in = new BufferedReader(new FileReader(path))) {
      ArrayList<Interval> intervals = new ArrayList<>();

      String line;
      while ((line = in.readLine()) != null) {
        if (!line.matches("\\d\\d:\\d\\d .*")) {
          throw new IllegalArgumentException("Invalid format");
        }

        String duration = line.substring(0, 5);
        String label = line.substring(6);
        intervals.add(new Interval(duration, label));
      }

      return intervals;
    } catch (final IOException e) {
      throw new IllegalArgumentException("Invalid path");
    }
  }

  /**
   * Saves the given intervals to the specified file. Each interval is written in the format - ss:mm
   * INTERVAL_NAME.
   *
   * @param intervals an ArrayList of Interval objects
   * @param path      the path of the file
   *
   * @throws IllegalArgumentException if the specified file is invalid
   */
  public static void save(ArrayList<Interval> intervals, String path) {
    try (var out = new BufferedWriter(new FileWriter(path))) {
      if (intervals != null) {
        for (Interval i : intervals) {
          out.write(i.toString() + "\n");
        }
      }
    } catch (final IOException e) {
      throw new IllegalArgumentException("Invalid path");
    }
  }
}
