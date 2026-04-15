package com.example.demo.task;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {

    Task save(Task task);

    Optional<Task> findById(long id);

    List<Task> findAll();

    boolean deleteById(long id);
}
