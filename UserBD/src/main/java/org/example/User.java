package org.example;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table (name = "users")
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

    public User(String name, String email, int age) {
        update(name, email, age);
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

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getAge() {
        return age;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void update(String name, String email, int age){
        if (name == null || name.isBlank() || name.length() > 100){
            throw new IllegalArgumentException("Имя не должно быть пустым и должно содержать 1-100 символов.");
        }
        if (email == null || email.length() > 254 || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")){
            throw new IllegalArgumentException("Некорректный email");
        }
        if (age < 0 || age > 150){
            throw new IllegalArgumentException("Возраст должен быть от 0 до 150.");
        }

        this.name = name;
        this.email = email;
        this.age = age;
    }

    @PrePersist
    public void onCreated(){
        createdAt = Instant.now();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", createdAt=" + createdAt +
                '}';
    }
}
