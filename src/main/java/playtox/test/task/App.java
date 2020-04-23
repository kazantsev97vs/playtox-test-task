package playtox.test.task;

import playtox.test.task.entities.Account;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class App {
    private static final Lock lock = new ReentrantLock();

    public static void main(String[] args) throws InterruptedException {
        long startTime = System.currentTimeMillis();

        Runner runner = new Runner();
        runner.run();

//        Thread thread1 = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                System.out.println("Thread 1 " + lock.tryLock());
//                lock.unlock();
//                lock.unlock();
//
//            }
//        });
//        Thread thread2 = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                System.out.println("Thread 2 " + lock.tryLock());
//            }
//        });
//
//        thread1.start();
//        thread2.start();
//
//        thread1.join();
//        thread2.join();



        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            threadList.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    runner.doTransactions();
                }
            }));
        }



        for (Thread thread : threadList) {
            thread.start();
        }

        for (Thread thread : threadList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        runner.finished();

        long endTime = System.currentTimeMillis();
        System.out.println("Время исполнения: " + (endTime - startTime) + " ms");
    }
}

class Runner {

    private int counter = 0;
    private final Lock counterLock = new ReentrantLock();

    private Integer transactionsCounter = 0;
    private final Lock transactionsCounterLock = new ReentrantLock();

    private List<Account> accountList = new ArrayList<>();
    private static List<Lock> lockList = new ArrayList<>();
    private static final int ACCOUNTS_LIMIT = 10;

    private static final Random random = new Random();

    /**
     * При запуске приложение должно создать четыре (или более) экземпляров объекта Account
     * со случайными значениями ID и значениями money равным 10000.
     */
    public void run () {
        createAccounts();
    }

    private void incrementCounter() {
        counterLock.lock();
        counter++;
        // Если мы дошли до конца списка - начать сначала
        if (counter == ACCOUNTS_LIMIT) counter = 0;
        counterLock.unlock();
    }

    private void incrementTransactionsCounterLock() {
        transactionsCounterLock.lock();
        transactionsCounter++;
        System.out.println(transactionsCounter);
        transactionsCounterLock.unlock();
    }



    public void doTransactions() {
        while (transactionsCounter < 130) {
            doTransaction();
        }
    }

    public synchronized void doTransaction() {
        // получаем два индекса только что залоченных локов
        List<Integer> indexes = takeTwoAccounts();

        try {
            // производим перевод средств между аккаунтами под полученными индексами
            Account.transfer(accountList.get(indexes.get(0)), accountList.get(indexes.get(1)), random.nextInt(100));

            // увеличиваем счетчик тразакций
            incrementTransactionsCounterLock();

        } finally {
            // особобождаем локи
            for (Integer i : indexes) {
                lockList.get(i).unlock();
            }
        }
    }

    public List<Integer> takeTwoAccounts() {

        Set<Integer> accountIndexes = new LinkedHashSet<>();

        while (accountIndexes.size() < 2) {
            // Если получилось залочить
            if (lockList.get(counter).tryLock()) {
                // забираем индекс залоченного лока
                accountIndexes.add(counter);
                incrementCounter();
            }
        }

        return new ArrayList<>(accountIndexes);
    }

    public void finished() {
        int total = 0;

        for (Account account : accountList) {
            System.out.print(account + "\n");
            total += account.getMoney();
        }

        System.out.println("Total money is " + total);
    }

    public void createAccounts() {
        for (int i = 1; i <= ACCOUNTS_LIMIT; i++) {
            // Создаем аккаунты
            accountList.add(new Account(i, 10_000));
            // и локи для них
            lockList.add(new ReentrantLock());
        }
        // for (Account acc : accountList) System.out.println(acc);
        // for (Lock lock   : lockList)    System.out.println(lock);
    }
}