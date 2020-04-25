package playtox.test.task.models;


import lombok.Data;
import org.apache.log4j.Logger;
import playtox.test.task.entities.Account;
import playtox.test.task.utils.RandomValuesGenerator;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Data
public class TransactionsExecutor {

    private static final Logger logger = Logger.getLogger(TransactionsExecutor.class);

    private int transactionsLimit;  // число транзакций, которые нужно выполнить

    private int accountsRunner;                                     // бегунок по списку аккаунтов
    private final Lock accountsRunnerLock = new ReentrantLock();    // лок для бегунка по списку аккаунтов

    private Integer transactionsCounter = 0;                            // счетчик транзакций
    private final Lock transactionsCounterLock = new ReentrantLock();   // лок для счетчика транзакций

    private List<Account> accountList;  // список аккаунтов
    private List<Lock> accountLockList; // список локов - каждый лок соответсвует одному аккаунту из списка аккаунтов

    private Integer amountRange = 10_000;    // диапазон разброса сумм для перевода

    /**
     * Запомнить список аккаунтов,
     * к каждому аккаунту задать соответсвующий лок
     * @param accountList - список аккаунтов
     */
    public void setAccountList(List<Account> accountList) {
        this.accountList = accountList;
        this.accountLockList = new ArrayList<>();
        logger.info("Получен список аккаунтов, с которыми нужно провести транзакции:");
        logger.info(accountList + "\n");
        for (int i = 0; i < this.accountList.size(); i++) {
            accountLockList.add(new ReentrantLock());
        }
    }

    /**
     * Выполнить заданное количествое транзакций
     */
    public void run() {
        logger.info("Поток " + Thread.currentThread().getName() + " начинает выполнение " + transactionsLimit + " транзакций..");
        while (transactionsCounter < transactionsLimit) {
            completeTransaction();
            try {
                // Потоки спят 1000 - 2000 мс
                int sleepTime = RandomValuesGenerator.generateRandomIntegerValue(1000, 2000);
                logger.info("Поток " + Thread.currentThread().getName() + " засыпает на время " + sleepTime + " ms..\n");
                Thread.sleep(sleepTime);
                logger.info("Поток " + Thread.currentThread().getName() + " проснулся и продолжает выполнение транзакций..\n");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        logger.info("Поток " + Thread.currentThread().getName() + " выполнение транзакций закончил");
    }

    /**
     * Перейти к следующему аккаунту
     */
    private void goToNextAccount() {
        accountsRunnerLock.lock();
        accountsRunner++;
        // Если мы дошли до конца списка - начать сначала
        if (accountsRunner == accountList.size()) accountsRunner = 0;
        accountsRunnerLock.unlock();
    }

    /**
     * Выполнить одну транзакцию
     */
    public void completeTransaction() {
        transactionsCounterLock.lock();

        logger.info("Поток " + Thread.currentThread().getName() + " начинает выполнение транзакции №" + ++transactionsCounter);

        List<Integer> indexes = takeTwoAccounts();
        Account account1 = accountList.get(indexes.get(0));
        Account account2 = accountList.get(indexes.get(1));
        Integer amount = RandomValuesGenerator.random.nextInt(amountRange);

        try {
            Boolean isTransferSuccessful = Account.transfer(account1, account2, amount);

            if (!isTransferSuccessful) logger.info("Транзакция №" + transactionsCounter + " не удалась..\n");

        } finally {
            returnAccounts(indexes);
        }

        transactionsCounterLock.unlock();
    }

    /**
     * Залочить аккаунты
     * @return список индексов, залоченных аккаунтов
     */
    public List<Integer> takeTwoAccounts() {

        Set<Integer> accountPositionIndexes = new LinkedHashSet<>();

        while (accountPositionIndexes.size() < 2) {
            // Если получилось залочить
            if (accountLockList.get(accountsRunner).tryLock()) {
                // забираем индекс залоченного лока
                accountPositionIndexes.add(accountsRunner);
                goToNextAccount();
            }
        }

        return new ArrayList<>(accountPositionIndexes);
    }

    /**
     * Разлочить аккаунты
     * @param indexes - список индексов залоченных аккаунтов
     */
    public void returnAccounts(List<Integer> indexes) {
        for (Integer i : indexes) {
            try {
                accountLockList.get(i).unlock();
            } catch (IllegalMonitorStateException ex) {
                logger.error(ex + " - не удалось разлочить лок " + accountList.get(i) + " похоже он уже был разлочен..");
            }
        }
    }

    /**
     * Показать результат
     */
    public void showResult() {
        int total = 0;

        logger.info("Вывод результатов:\n");
        for (Account account : accountList) {
            logger.info(account);
            total += account.getMoney();
        }

        logger.info("Total money is " + total);
    }

}
