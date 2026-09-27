package org.example;

import java.sql.SQLException;

public class DaoException extends RuntimeException {

    public DaoException(Throwable cause) {
        super(resolveMessage(cause), cause);
    }

    private static String resolveMessage(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql) {
                String state = sql.getSQLState();

                if ("23505".equals(state)) {
                    return "Нарушена уникальность данных: проверьте email.";
                }
                if (state != null && state.startsWith("08")) {
                    return "Ошибка соединения с PostgreSQL.";
                }
                if (state != null && state.startsWith("23")) {
                    return "Данные нарушают ограничения БД.";
                }
            }
        }
        return "Ошибка работы с БД. Подробности в логе.";
    }
}