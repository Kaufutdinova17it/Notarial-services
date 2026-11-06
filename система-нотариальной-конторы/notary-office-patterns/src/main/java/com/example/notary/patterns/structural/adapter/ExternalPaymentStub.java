package com.example.notary.patterns.structural.adapter;

/**
 * Внешний платёжный сервис-заглушка с несовместимым интерфейсом:
 * сумма передаётся в копейках, результат возвращается числовым кодом.
 * Изменять этот класс нельзя — он имитирует стороннюю систему,
 * поэтому используется паттерн «Адаптер».
 */
public class ExternalPaymentStub {

    public static final int CODE_SUCCESS = 0;
    public static final int CODE_INSUFFICIENT_FUNDS = 1;
    public static final int CODE_INVALID_AMOUNT = 2;

    private long transactionSequence = 100000;

    /**
     * Проводит платёж.
     *
     * @param amountKopecks  сумма в копейках
     * @param orderReference строковый идентификатор заказа
     * @return числовой код результата: 0 — успех, иначе ошибка
     */
    public int makePayment(long amountKopecks, String orderReference) {
        if (amountKopecks <= 0) {
            return CODE_INVALID_AMOUNT;
        }
        transactionSequence++;
        System.out.printf("      [ПЛАТЁЖНЫЙ СТУБ] Проведён платёж %d коп. по заказу %s, транзакция %s%n",
                amountKopecks, orderReference, lastTransactionId());
        return CODE_SUCCESS;
    }

    /** Идентификатор последней транзакции. */
    public String lastTransactionId() {
        return "TX-" + transactionSequence;
    }

    /** Расшифровка числового кода результата. */
    public String describeCode(int code) {
        return switch (code) {
            case CODE_SUCCESS -> "Операция выполнена";
            case CODE_INSUFFICIENT_FUNDS -> "Недостаточно средств";
            case CODE_INVALID_AMOUNT -> "Некорректная сумма платежа";
            default -> "Неизвестный код результата: " + code;
        };
    }
}
