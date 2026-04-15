package com.example.demo.task;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class TaskService {

    private final TaskRepository repository;
    private final AtomicLong sequence = new AtomicLong(0);

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public Task create(CreateTaskCommand command) {
        long id = sequence.incrementAndGet();
        Task task = new Task(id, command.title(), command.description(), command.status(), command.priority());
        return repository.save(task);
    }

    public Optional<Task> findById(long id) {
        return repository.findById(id);
    }

    public List<Task> findAll() {
        return repository.findAll();
    }

    public Optional<Task> update(long id, UpdateTaskCommand command) {
        return repository.findById(id)
                .map(existing -> new Task(id, command.title(), command.description(), command.status(), command.priority()))
                .map(repository::save);
    }

    public boolean delete(long id) {
        return repository.deleteById(id);
    }
}
