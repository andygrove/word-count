package wc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class WordCountIdiomatic {

  public static void main(String[] args) throws Exception {

    String filename = "/Users/andy/Documents//shakespeare.txt";

    final ConcurrentMap<String,AtomicInteger> map = new ConcurrentHashMap<>();
    long t1 = System.currentTimeMillis();
    try (FileReader fr = new FileReader(filename)) {
      try (BufferedReader r = new BufferedReader(fr, 64*1024)) {
        String line;
        while ((line = r.readLine()) != null) {
          Arrays.stream(line.split(" "))
              .map(WordCountIdiomatic::sanitize)
              .filter(word -> !word.isEmpty())
              .filter(word -> word.length() > 3)
              .map(String::toLowerCase)
              .forEach(word -> map.computeIfAbsent(word, key -> new AtomicInteger()).incrementAndGet());
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

  public static String sanitize(String in){
    StringBuilder b = new StringBuilder(in.length());
    for (int i = 0; i<in.length(); i++) {
      char ch = in.charAt(i);
      if (Character.isAlphabetic(ch)) {
        b.append(ch);
      }
    }
    return b.toString();
  }
}