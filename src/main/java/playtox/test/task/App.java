package playtox.test.task;

import org.apache.log4j.Logger;
import playtox.test.task.entities.Account;
import playtox.test.task.models.TransactionsExecutor;
import playtox.test.task.utils.RandomValuesGenerator;

import java.util.*;

public class App {

    private static final Logger logger = Logger.getLogger(App.class);

    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();

        final int TRANSACTIONS_LIMIT = 30;
        final int ACCOUNTS_LIMIT = 4;   // можно задать больше
        final int ACCOUNTS_BALANCE = 10_000;
        final int THREADS_LIMIT = 2;    // можно задать больше

        logger.info("Заданные параметры:"
                + "\nКоличество транзакций - " + TRANSACTIONS_LIMIT
                + "\nКоличество аккаунтов - " + ACCOUNTS_LIMIT
                + "\nКоличество потоков - " + THREADS_LIMIT);


        List<Account> accountList = createAccountList(ACCOUNTS_LIMIT, ACCOUNTS_BALANCE);

        TransactionsExecutor transactionsExecutor = new TransactionsExecutor();
        transactionsExecutor.setTransactionsLimit(TRANSACTIONS_LIMIT);
        transactionsExecutor.setAccountList(accountList);
        transactionsExecutor.setAmountRange(5_000);

        List<Thread> threadList = createThreadList(THREADS_LIMIT, transactionsExecutor);

        for (Thread thread : threadList) {
            thread.start();
        }

        for (Thread thread : threadList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        }

        transactionsExecutor.showResult();

        long endTime = System.currentTimeMillis();
        logger.info("Время выполнения программы: " + (endTime - startTime) + " ms");
    }

    public static List<Account> createAccountList(final int ACCOUNTS_LIMIT, final int ACCOUNTS_BALANCE) {
        List<Account> accountList = new ArrayList<>();
        for (int i = 0; i < ACCOUNTS_LIMIT; i++) {
            accountList.add(new Account(RandomValuesGenerator.generateRandomStringValue(), ACCOUNTS_BALANCE));
        }
        return accountList;
    }

    public static List<Thread> createThreadList(final int THREADS_LIMIT, TransactionsExecutor transactionsExecutor) {
        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < THREADS_LIMIT; i++) {
            threadList.add(new Thread(() -> {
                logger.info(Thread.currentThread().getName() + " has been started");
                transactionsExecutor.run();
            }));
        }
        return threadList;
    }

}
