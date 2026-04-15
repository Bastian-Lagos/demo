package com.example.demo.task;

final class TaskValidation {

    private TaskValidation() {
    }

    static String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        String normalized = title.trim();
        if (normalized.length() > 80) {
            throw new IllegalArgumentException("title must be at most 80 characters");
        }
        return normalized;
    }

    static String normalizeDescription(String description) {
        if (description == null) {
            return "";
        }
        String normalized = description.trim();
        if (normalized.length() > 200) {
            throw new IllegalArgumentException("description must be at most 200 characters");
        }
        return normalized;
    }

    static TaskStatus requireStatus(TaskStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        return status;
    }

    static int requirePriority(int priority) {
        if (priority < 1 || priority > 5) {
            throw new IllegalArgumentException("priority must be between 1 and 5");
        }
        return priority;
    }
}
