package playtox.test.task.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.log4j.Logger;

/**
 * Account (счет):
 *  1. ID (строковое) - идентификатор счета
 *  2. Money (целочисленное) - сумма средств на счете.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    private static final Logger logger = Logger.getLogger(Account.class);

    private String id;

    private Integer money;

    public void deposit(Integer amount) {
        Account.logger.info("Пополняю баланс счета " + this + " на сумму " + amount);
        money += amount;
    }

    public void withdraw(Integer amount) {
        Account.logger.info("Снимаю средства на сумму " + amount + " со счета " + this);
        money -= amount;
    }

    public static Boolean transfer(Account account1, Account account2, Integer amount) {
        // Если после списания будет отрицательный счет - завершить операцию
        if (account1.getMoney() < amount) {
            logger.info("Произвести списаниия средств в количестве " + amount + " с " + account1 + " - не удалось");
            return false;
        }

        logger.info("Выполняю перевод средств с " + account1 + " на " + account2 + " на сумму в размере " + amount);
        account1.withdraw(amount);
        account2.deposit(amount);
        logger.info("Перевод между счетами " + account1 + " и " + account2 + " прошел успешно!\n");
        return true;
    }
}