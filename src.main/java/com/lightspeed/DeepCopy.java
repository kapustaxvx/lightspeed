package com.lightspeed;

import sun.misc.Unsafe;

import java.lang.reflect.Array;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class DeepCopy {

    private static final Unsafe unsafe;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Unsafe instance", e);
        }
    }

    public static <T> T deepCopy(T obj) {
        try {
            if (obj == null) {
                return null;
            }

            if (obj instanceof String
                    || obj instanceof Integer
                    || obj instanceof Boolean
                    || obj instanceof Double
                    || obj instanceof Float
                    || obj instanceof Long
                    || obj instanceof Character
                    || obj instanceof Byte
                    || obj instanceof Short) {
                return obj;
            }

            if (obj.getClass().isArray()) {
                Class<?> componentType = obj.getClass().getComponentType();
                int length = Array.getLength(obj);
                Object copy = Array.newInstance(componentType, length);
                for (int i = 0; i < length; i++) {
                    Array.set(copy, i, deepCopy(Array.get(obj, i)));
                }
                return (T) copy;
            }

            if (obj instanceof Collection) {
                Collection<?> collection = (Collection<?>) obj;
                Collection<Object> copy = collection instanceof List ? new ArrayList<>() : new HashSet<>();
                for (Object item : collection) {
                    copy.add(deepCopy(item));
                }
                return (T) copy;
            }

            if (obj instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) obj;
                Map<Object, Object> copy = new HashMap<>();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    copy.put(deepCopy(entry.getKey()), deepCopy(entry.getValue()));
                }
                return (T) copy;
            }

            Class<?> clazz = obj.getClass();
            Object copy = unsafe.allocateInstance(clazz);
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                field.set(copy, deepCopy(field.get(obj)));
            }
            return (T) copy;

        } catch (Exception e) {
            throw new RuntimeException("Failed to deep copy object", e);
        }
    }

    public static void main(String[] args) {
        List<String> books = new ArrayList<>(List.of("Book 1", "Book 2"));
        Man original = new Man("John", 30, books);
        Man copy = deepCopy(original);

        System.out.println("Original: " + original.getName() + ", Books: " + original.getFavoriteBooks());
        System.out.println("Copy: " + copy.getName() + ", Books: " + copy.getFavoriteBooks());

        // Modify original object to test deep copy
        original.setName("Mike");
        original.getFavoriteBooks().add("Book 3");

        System.out.println("Original: " + original.getName() + ", Books: " + original.getFavoriteBooks());
        System.out.println("Copy: " + copy.getName() + ", Books: " + copy.getFavoriteBooks());
    }
}

class Man {
    private String name;
    private int age;
    private List<String> favoriteBooks;

    public Man(String name, int age, List<String> favoriteBooks) {
        this.name = name;
        this.age = age;
        this.favoriteBooks = favoriteBooks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public List<String> getFavoriteBooks() {
        return favoriteBooks;
    }

    public void setFavoriteBooks(List<String> favoriteBooks) {
        this.favoriteBooks = favoriteBooks;
    }
}