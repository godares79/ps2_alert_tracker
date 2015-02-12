package org.david.planetside.alertnotifier;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Shared information used in unit tests.
 */
public class TestData {
  public static final String DATA_LOCATION_PREFIX = "/home/david/AndroidStudioWorkspace/PS2_AlertNotifier/mobile/src/test/resources/";

  /**
   * Open the test resources and return a line.
   *
   * @param testClass  The class that this will be for. The resources file should be named *ClassName*.data
   * @param lineNumber The line to read -- 0 indexed.
   * @return The read line.
   */
  public static String openAndReadLine(Class<?> testClass, int lineNumber)
      throws IOException, JSONException {
    // There is a bug in Android Studio that results in resources not being added to the test
    // classpath. See https://code.google.com/p/android/issues/detail?id=64887.
    // Everything works if using gradlew test from the command line, but I want IDE support and the
    // workarounds that I've searched for no longer work.
    // Therefore, don't use resources and just load from a file path :(
    File dataFile = new File(DATA_LOCATION_PREFIX + testClass.getSimpleName() + ".data");
    try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
      for (int index = 0; index <= lineNumber; index++) {
        if (index == lineNumber) {
          return reader.readLine();
        }
        reader.readLine();
      }
    }

    throw new IllegalArgumentException("Data for test does not have a line number " + lineNumber);
  }
}
