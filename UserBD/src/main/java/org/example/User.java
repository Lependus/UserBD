package org.example;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(nullable = false)
    private int age;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public User() {
    }

    public User(String name, String email, int age, String created_at) {
        this.name = name;
        this.email = email;
        this.age = age;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public User(String name, String email, int age) {
        update(name, email, age);
    }

    public void update(String name, String email, int age) {
        if (name == null || name.isBlank() || name.strip().length() > 100) {
            throw new IllegalArgumentException("Имя должно содержать 1–100 символов.");
        }
        if (email == null || email.length() > 254
                || !email.contains("@")) {
            throw new IllegalArgumentException("Некорректный email.");
        }
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Возраст должен быть от 0 до 150.");
        }

        this.name = name.strip();
        this.email = email;
        this.age = age;
    }

    @PrePersist
    private void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "id=%s, name=%s, email=%s, age=%d, created_at=%s"
                .formatted(id, name, email, age, createdAt);
    }
}
