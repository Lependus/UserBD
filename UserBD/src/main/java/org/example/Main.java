package org.example;

import org.hibernate.SessionFactory;

import java.util.NoSuchElementException;
import java.util.Scanner;


public class Main {

    public static void main(String[] args) {
        try (SessionFactory factory = HibernateUtil.createSessionFactory();
             Scanner scanner = new Scanner(System.in)) {

            UserDao dao = new HibernateUserDao(factory);
            runMenu(scanner, dao);

        } catch (RuntimeException e) {
            System.err.println(
                    "[ERROR] Не удалось запустить или корректно закрыть приложение."
            );
            System.err.println("Не удалось запустить или корректно закрыть приложение.");
        }
    }

    private static void runMenu(Scanner scanner, UserDao dao) {
        while (true) {
            System.out.println("""
                    
                    1. Создать пользователя
                    2. Найти по ID
                    3. Показать всех
                    4. Обновить
                    5. Удалить
                    0. Выход
                    """);

            try {
                switch (read(scanner, "Выбор: ")) {
                    case "1" -> {
                        User user = new User(
                                read(scanner, "Имя: "),
                                read(scanner, "Email: "),
                                Integer.parseInt(read(scanner, "Возраст: "))
                        );
                        dao.create(user);
                        System.out.println("Создан пользователь, ID: " + user.getId());
                    }
                    case "2" -> System.out.println(
                            dao.findById(readId(scanner))
                                    .map(User::toString)
                                    .orElse("Пользователь не найден.")
                    );
                    case "3" -> {
                        var users = dao.findAll();
                        if (users.isEmpty()) {
                            System.out.println("Пользователей пока нет.");
                        } else {
                            users.forEach(System.out::println);
                        }
                    }
                    case "4" -> {
                        boolean updated = dao.update(
                                readId(scanner),
                                read(scanner, "Новое имя: "),
                                read(scanner, "Новый email: "),
                                Integer.parseInt(read(scanner, "Возраст: "))
                        );
                        System.out.println(updated
                                ? "Обновлено." : "Пользователь не найден.");
                    }
                    case "5" -> System.out.println(
                            dao.delete(readId(scanner))
                                    ? "Удалено." : "Пользователь не найден."
                    );
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("Неизвестная команда.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Введите корректное целое число.");
            } catch (IllegalArgumentException | DaoException e) {
                System.out.println(e.getMessage());
            } catch (NoSuchElementException e) {
                return; // Конец консольного ввода.
            }
        }
    }

    private static String read(Scanner scanner, String line) {
        System.out.print(line);
        return scanner.nextLine().strip();
    }

    private static long readId(Scanner scanner) {
        long id = Long.parseLong(read(scanner, "ID: "));
        if (id <= 0) {
            throw new IllegalArgumentException("ID должен быть положительным.");
        }
        return id;
    }
}