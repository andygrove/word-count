package wc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.in;

public class WordCountOptimized {

  static final int MIN_WORD_SIZE = 6;

  static final ConcurrentMap<String,AtomicInteger> map = new ConcurrentHashMap<>();

  public static void main(String[] args) throws Exception {

    String filename = "/Users/andy/Documents//shakespeare.txt";

    long t1 = System.currentTimeMillis();
    try (FileReader fr = new FileReader(filename)) {
      try (BufferedReader r = new BufferedReader(fr, 64*1024)) {
        String line;
        StringBuilder word = new StringBuilder(1024);
        while ((line = r.readLine()) != null) {

          // rather than call split, lets walk over the line re-using a single StringBuilder to form each word
          word.setLength(0);
          for (int i = 0; i<line.length(); i++) {
            char ch = line.charAt(i);
            if (Character.isAlphabetic(ch)) {
              word.append(ch);
            } else {
              // end of a word
              if (word.length() >= MIN_WORD_SIZE) {
                processWord(word.toString());
              }
              word.setLength(0);
            }
          }

          // final word
          if (word.length() >= MIN_WORD_SIZE) {
            processWord(word.toString());
          }

        }

        // show top N words
        map.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue().get(), a.getValue().get()))
            .limit(10)
            .forEach(System.out::println);

      }
    }
    long t2 = System.currentTimeMillis();
    System.out.println("Took " + (t2-t1)/1000.0 + " seconds");
  }

  private static void processWord(String word) {
    map.computeIfAbsent(word.toLowerCase(), key -> new AtomicInteger()).incrementAndGet();
  }

}
