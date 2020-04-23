package playtox.test.task.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Account (счет):
 *  1. ID (строковое) - идентификатор счета
 *  2. Money (целочисленное) - сумма средств на счете.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    private Integer id;

    private Integer money;

    public void deposit(Integer amount) {
        money += amount;
    }

    public void withdraw(Integer amount) {
        money -= amount;
    }

    public static void transfer(Account account1, Account account2, Integer amount) {
        account1.withdraw(amount);
        account2.deposit(amount);
    }
}