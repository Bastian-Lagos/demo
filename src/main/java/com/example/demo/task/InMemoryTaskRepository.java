package com.example.demo.task;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTaskRepository implements TaskRepository {

    private final ConcurrentHashMap<Long, Task> storage = new ConcurrentHashMap<>();

    @Override
    public Task save(Task task) {
        storage.put(task.id(), task);
        return task;
    }

    @Override
    public Optional<Task> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Task> findAll() {
        return storage.values()
                .stream()
                .sorted(Comparator.comparingLong(Task::id))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    @Override
    public boolean deleteById(long id) {
        return storage.remove(id) != null;
    }
}
