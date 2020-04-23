package playtox.test.task;

import playtox.test.task.entities.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class App {

    public static void main(String[] args) {
        Runner runner = new Runner();
        runner.run();

        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            threadList.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    runner.doTransactions();
                }
            }));
        }

        long startTime = System.currentTimeMillis();

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
    private Lock counterLock = new ReentrantLock();

    private Integer transactionsCounter = 0;
    private Lock transactionsCounterLock = new ReentrantLock();

    private List<Account> accountList = new ArrayList<>();
    private static final List<Lock> lockList = new ArrayList<>();
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

    public int[] takeTwoAccounts() {

        int[] accountIndexes = new int[2];

        for (int accountIndexesCounter = 0; accountIndexesCounter < 2; incrementCounter()) {

            // Если получилось залочить
            if (lockList.get(counter).tryLock()) {
                // забираем индекс залоченного лока
                accountIndexes[accountIndexesCounter++] = counter;
            }
        }

        return accountIndexes;
    }

    public void doTransactions() {
        while (transactionsCounter < 30) {

            // получаем два индекса только что залоченных локов
            int[] indexes = takeTwoAccounts();

                // производим перевод средств между аккаунтами под полученными индексами
                Account.transfer(accountList.get(indexes[0]), accountList.get(indexes[1]), random.nextInt(100));
                // увеличиваем счетчик тразакций
                incrementTransactionsCounterLock();
                // особобождаем локи
                for (int i : indexes) {
                    lockList.get(i).unlock();
                }

        }
    }

    public void finished() {
        int total = 0;

        for (Account account : accountList) {
            System.out.print(account);
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