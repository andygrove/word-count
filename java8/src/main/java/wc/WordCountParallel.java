package wc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static wc.WordCountParallel.*;

public class WordCountParallel {

  static final int MIN_WORD_SIZE = 6;

  static final ConcurrentMap<String,AtomicInteger> map = new ConcurrentHashMap<>();

  static final ExecutorService exec = Executors.newFixedThreadPool(4);

  static final String POISON_PILL = "";

  public static void main(String[] args) throws Exception {

    String filename = "/Users/andy/Documents//shakespeare.txt";

    long t1 = System.currentTimeMillis();
    try (FileReader fr = new FileReader(filename)) {
      try (BufferedReader r = new BufferedReader(fr, 64*1024)) {

        final Worker worker[] = new Worker[4];
        for (int i=0; i<4; i++) {
          worker[i] = new Worker();
          exec.execute(worker[i]);
        }

        int workerIndex = 0;
        while (true) {

          final String line = r.readLine();
          if (line == null) {
            for (Worker w : worker) {
              w.getQueue().put(POISON_PILL);
            }
            break;
          }

          worker[workerIndex++].getQueue().put(line);
          if (workerIndex >= worker.length) {
            workerIndex = 0;
          }
        }

        // wait for workers to complete
        exec.shutdown();
        exec.awaitTermination(30, TimeUnit.SECONDS);

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

}

class Worker implements Runnable {

  final StringBuilder word = new StringBuilder(1024);

  final BlockingQueue<String> queue = new ArrayBlockingQueue<>(100);

  public BlockingQueue<String> getQueue() {
    return queue;
  }

  @Override
  public void run() {
    List<String> list = new ArrayList<>(100);
    while (true) {
      try {
        // block until there is at least one item
        list.clear();
        list.add(queue.take());
        // drain any further items
        queue.drainTo(list);
        for (String line : list) {
          if (line == POISON_PILL) {
            return;
          }
          processLine(line);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  void processLine(String line) {
    word.setLength(0);
    for (int i = 0; i < line.length(); i++) {
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

  public static void processWord(String word) {
    map.computeIfAbsent(word.toLowerCase(), key -> new AtomicInteger()).incrementAndGet();
  }

}


