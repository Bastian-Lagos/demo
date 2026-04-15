package com.example.demo.task;

public record CreateTaskCommand(
        String title,
        String description,
        TaskStatus status,
        int priority
) {
    public CreateTaskCommand {
        title = TaskValidation.normalizeTitle(title);
        description = TaskValidation.normalizeDescription(description);
        status = TaskValidation.requireStatus(status);
        priority = TaskValidation.requirePriority(priority);
    }
}
