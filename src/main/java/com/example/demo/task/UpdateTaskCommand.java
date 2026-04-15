package com.example.demo.task;

public record UpdateTaskCommand(
        String title,
        String description,
        TaskStatus status,
        int priority
) {
    public UpdateTaskCommand {
        title = TaskValidation.normalizeTitle(title);
        description = TaskValidation.normalizeDescription(description);
        status = TaskValidation.requireStatus(status);
        priority = TaskValidation.requirePriority(priority);
    }
}
