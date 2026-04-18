# Property-Based Testing - Task Management System
## Índice

- [Descripción del proyecto](#descripción-del-proyecto)
- [Arquitectura del sistema](#arquitectura-del-sistema)
- [Modelo de dominio](#modelo-de-dominio)
  - [Task](#task)
  - [Validaciones del dominio](#validaciones-del-dominio)
- [Comandos (Commands)](#comandos-commands)
- [Persistencia](#persistencia)
- [Resultados de pruebas](#resultados-de-las-pruebas)
- [Generación de datos](#generación-de-datos)
- [Ejecución](#ejecución)
- [Conclusion](#conclusion)
## Descripción del proyecto

Este proyecto implementa un sistema simple de gestión de tareas (**Tasks**) utilizando **Java + Spring Boot**, con almacenamiento en memoria.

El objetivo principal es validar el comportamiento del sistema mediante **Property-Based Testing**, utilizando la librería **jqwik**, definiendo propiedades generales del dominio en lugar de casos específicos.

---

## Arquitectura del sistema

El sistema sigue una arquitectura simple basada en capas:

- **Dominio**: `Task`, `TaskStatus`, `TaskValidation`
- **Aplicación**: `TaskService`
- **Persistencia**: `TaskRepository`, `InMemoryTaskRepository`
- **Comandos**: `CreateTaskCommand`, `UpdateTaskCommand`

---

## Modelo de dominio

###  Task

```java
public record Task(
        long id,
        String title,
        String description,
        TaskStatus status,
        int priority
)
```

### Validaciones del dominio

Las reglas del dominio se centralizan en la clase final:

**`TaskValidation`**

Principales invariantes:

- El `id` debe ser positivo
- El `title` no puede ser nulo ni vacío, se normaliza (trim) y tiene máximo 80 caracteres
- El `description` puede ser vacío y tiene máximo 200 caracteres
- El `status` no puede ser nulo
- La `priority` debe estar entre 1 y 5

```java
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
```

---

## Comandos (Commands)

Contienen los mismos campos que Task, excepto el id. Además, permiten desacoplar la entrada de datos del modelo de dominio, facilitando la validación y manipulación de la información antes de crear o actualizar una entidad.

También resultan especialmente útiles en los tests, ya que permiten simular fácilmente entradas del sistema mediante generación automática de datos en Property-Based Testing.

`CreateTaskCommand`
```java
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
```
`UpdateTaskCommand`
```java
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

```
---

## Persistencia

El repositorio es utilizado a través del TaskService para gestionar la persistencia de las tareas, permitiendo simular las operaciones principales del sistema (crear, leer, actualizar y eliminar) sin necesidad de exponer endpoints reales ni utilizar una base de datos.

`TaskService`
```java
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
```

---


## Resultados de las pruebas
![alt text](<Evidencia-Tests-1.png>)
![alt text](<Evidencia-Tests-2.png>)
---

## Generación de datos

- Titles: strings aleatorios
- Descriptions: opcionales
- Status: enum
- Priority: 1 a 5

```java
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
```
---

## Ejecución

### Requisitos

- Java 17+
- Maven

### Run

```
./mvnw spring-boot:run
```

### Test

```
./mvnw test
```

## Conclusion

Property-Based Testing permitió validar el sistema de forma robusta mediante la ejecución de múltiples combinaciones de datos generados automáticamente, lo que aseguró el cumplimiento consistente de los invariantes definidos en el dominio. Este enfoque resultó especialmente útil para detectar comportamientos inesperados que podrían no haberse identificado utilizando pruebas tradicionales basadas en casos específicos, ya que amplía significativamente la cobertura de escenarios evaluados.

Además, el uso de la librería jqwik fue un aporte relevante durante el desarrollo, no solo por su integración con el ecosistema de Java, sino también por su capacidad de ejecutar pruebas de manera concurrente utilizando múltiples threads. Esto permitió agilizar considerablemente los tiempos de ejecución, facilitando iteraciones más rápidas durante el proceso de desarrollo y validación del sistema.
