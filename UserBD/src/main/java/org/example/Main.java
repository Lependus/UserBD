package org.example;

import org.hibernate.SessionFactory;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (SessionFactory factory = HibernateSessionFactoryUtil.getSessionFactory()   ;
             Scanner scanner = new Scanner(System.in)) {

            UserDaoImpl userDao = new UserDaoImpl(factory);
            UserService service = new UserService(userDao);
            runMenu(scanner, service);

        } catch (RuntimeException e) {
            System.err.println(
                    "[ERROR] Не удалось запустить или корректно закрыть приложение."
            );
            System.err.println("Не удалось запустить или корректно закрыть приложение.");
        }
    }

    private static void runMenu(Scanner scanner, UserService userService){
        while (true){
            System.out.println("""
                    
                    1. Добавить пользователя
                    2. Найти по ID
                    3. Показать всех
                    4. Обновить
                    5. Удалить
                    0. Выход
                    """);

            try {
                switch (read(scanner, "Выберете действие:")){
                    case "1" -> {
                        User user = new User(
                                read(scanner, "Имя: "),
                                read(scanner, "Email: "),
                                Integer.parseInt(read(scanner, "Возраст: "))
                        );
                        userService.saveUser(user);
                        System.out.println("Создан пользователь, ID: " + user.getId());
                    }
                    case "2" -> System.out.println(
                            userService.findUser(readId(scanner,"Введите ID:"))
                                    .map(User::toString)
                                    .orElse("Пользователь не найден.")
                    );
                    case "3" -> {
                        var users = userService.findAllUsers();
                        if (users.isEmpty()){
                            System.out.println("Пользователей пока нет");
                        } else {
                            users.forEach(System.out::println);
                        }
                    }
                    case "4" -> {
                        boolean updatead = userService.updateUser(readId(scanner, "Введите ID:"),
                                read(scanner, "Имя: "),
                                read(scanner, "Email: "),
                                Integer.parseInt(read(scanner, "Возраст: ")));
                        System.out.println(updatead ? "Пользователь обновлен." : "Пользователь не найден.");
                    }
                    case "5" -> {
                        boolean deleted = userService.deleteUser(readId(scanner, "Введите ID:"));
                        System.out.println(deleted ? "Пользователь удален." : "Пользователь не найден.");
                    }
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("Некорректная операция");
                }
            } catch (NumberFormatException e) {
                System.out.println("Введите корректное целое число.");

            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());

            } catch (NoSuchElementException e) {
                return;

            } catch (RuntimeException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private static String read (Scanner scanner, String line){
        System.out.println(line);
        return scanner.nextLine().strip();
    }

    private static long readId(Scanner scanner, String line){
        System.out.println(line);
        long id = Long.parseLong(read(scanner, "ID:"));
        if (id < 0){
            throw new IllegalArgumentException("ID должен быть больше 0.");
        }
        return id;
    }
}