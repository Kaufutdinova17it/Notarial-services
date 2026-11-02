package com.example.notary.patterns.structural.adapter;

/**
 * «Внешний» платёжный сервис-заглушка с несовместимым интерфейсом:
 * принимает строковый номер заказа и сумму в копейках,
 * возвращает числовой код результата. Изменять этот класс нельзя
 * (он имитирует стороннюю систему), поэтому используется адаптер.
 */
public class ExternalPaymentStub {

    public static final int CODE_SUCCESS = 0;
    public static final int CODE_DECLINED = 1;

    private final boolean simulateSuccess;

    public ExternalPaymentStub(boolean simulateSuccess) {
        this.simulateSuccess = simulateSuccess;
    }

    /** Имитация транзакции во внешней банковской системе. */
    public int makeTransaction(String orderReference, long amountInKopecks) {
        System.out.println("      [Платёжная заглушка] Транзакция " + orderReference
                + " на сумму " + amountInKopecks + " коп.");
        return simulateSuccess ? CODE_SUCCESS : CODE_DECLINED;
    }
}
