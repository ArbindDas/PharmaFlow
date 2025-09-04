package com.JSR.PharmaFlow;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        Thread vthread = Thread.ofVirtual ().start (() -> {
            for (int i = 1; i <= 10; i++) {
                try {
                    Thread.sleep (1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException (e);
                }
                System.out.println (" i am virtual thread" + i);
            }
        });
        try {
            vthread.join ();
        } catch (RuntimeException | InterruptedException e) {
            throw new RuntimeException (e);
        }


        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        for (int i = 1; i <=5 ; i++) {
            int id = i;
            Thread.sleep(500);

            executor.submit(() -> {
                System.out.println("Task "+ id + " running in virtual thread");
                return null;
            });
        }
        executor.shutdown();
    }
}
