package com.example.demo.task;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TaskServicePropertyTest {

    @Property
    void createShouldPersistATaskThatRespectsTheSubmittedData(@ForAll("createCommands") CreateTaskCommand command) {
        TaskService service = new TaskService(new InMemoryTaskRepository());

        Task created = service.create(command);

        assertThat(created.id()).isPositive();
        assertThat(service.findById(created.id())).contains(created);
        assertThat(created.title()).isEqualTo(command.title());
        assertThat(created.description()).isEqualTo(command.description());
        assertThat(created.status()).isEqualTo(command.status());
        assertThat(created.priority()).isEqualTo(command.priority());
    }

    @Property
    void readShouldReturnExactlyTheTasksThatWereCreated(@ForAll("createCommandLists") List<CreateTaskCommand> commands) {
        TaskService service = new TaskService(new InMemoryTaskRepository());

        List<Task> created = commands.stream()
                .map(service::create)
                .toList();

        List<Task> loaded = service.findAll();

        assertThat(loaded).containsExactlyElementsOf(created);
    }

    @Property
    void updateShouldPreserveTheIdAndReplaceMutableFields(
            @ForAll("createCommands") CreateTaskCommand originalCommand,
            @ForAll("updateCommands") UpdateTaskCommand updateCommand
    ) {
        TaskService service = new TaskService(new InMemoryTaskRepository());
        Task original = service.create(originalCommand);

        Task updated = service.update(original.id(), updateCommand).orElseThrow();

        assertThat(updated.id()).isEqualTo(original.id());
        assertThat(updated.title()).isEqualTo(updateCommand.title());
        assertThat(updated.description()).isEqualTo(updateCommand.description());
        assertThat(updated.status()).isEqualTo(updateCommand.status());
        assertThat(updated.priority()).isEqualTo(updateCommand.priority());
        assertThat(service.findById(original.id())).contains(updated);
    }

    @Property
    void deleteShouldMakeTheTaskUnavailableForFutureReads(@ForAll("createCommands") CreateTaskCommand command) {
        TaskService service = new TaskService(new InMemoryTaskRepository());
        Task created = service.create(command);

        boolean deleted = service.delete(created.id());

        assertThat(deleted).isTrue();
        assertThat(service.findById(created.id())).isEmpty();
        assertThat(service.findAll()).doesNotContain(created);
    }

    @Property
    void idsShouldAlwaysBeUniqueAndIncreasing(@ForAll("createCommandLists") List<CreateTaskCommand> commands) {
        TaskService service = new TaskService(new InMemoryTaskRepository());

        List<Long> ids = commands.stream()
                .map(service::create)
                .map(Task::id)
                .toList();

        assertThat(ids).doesNotHaveDuplicates();
        assertThat(ids).isSorted();
    }

    @Provide
    Arbitrary<CreateTaskCommand> createCommands() {
        return validTaskData().as(CreateTaskCommand::new);
    }

    @Provide
    Arbitrary<UpdateTaskCommand> updateCommands() {
        return validTaskData().as(UpdateTaskCommand::new);
    }

    @Provide
    Arbitrary<List<CreateTaskCommand>> createCommandLists() {
        return createCommands().list().ofMinSize(0).ofMaxSize(30);
    }

    private Combinators.Combinator4<String, String, TaskStatus, Integer> validTaskData() {
        return Combinators.combine(validTitles(), validDescriptions(), validStatuses(), validPriorities());
    }

    private Arbitrary<String> validTitles() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(40)
                .map(value -> " " + value + " ");
    }

    private Arbitrary<String> validDescriptions() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(0)
                .ofMaxLength(80)
                .map(value -> value.isBlank() ? "" : " " + value + " ");
    }

    private Arbitrary<TaskStatus> validStatuses() {
        return Arbitraries.of(TaskStatus.values());
    }

    private Arbitrary<Integer> validPriorities() {
        return Arbitraries.integers().between(1, 5);
    }
}
