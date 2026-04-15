package com.example.demo.task;

public record Task(
        long id,
        String title,
        String description,
        TaskStatus status,
        int priority
) {
    public Task {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
        title = TaskValidation.normalizeTitle(title);
        description = TaskValidation.normalizeDescription(description);
        status = TaskValidation.requireStatus(status);
        priority = TaskValidation.requirePriority(priority);
    }
}
