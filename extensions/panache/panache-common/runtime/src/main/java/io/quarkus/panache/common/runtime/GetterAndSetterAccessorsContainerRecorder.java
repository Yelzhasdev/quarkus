package io.quarkus.panache.common.runtime;

import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class GetterAndSetterAccessorsContainerRecorder {
    /**
     * Create new getter accessors container.
     */
    public RuntimeValue<GetterAndSetterAccessorsContainer> newContainer() {
        return new RuntimeValue<>(new GetterAndSetterAccessorsContainer());
    }

    /**
     * Add a getter accessor to a container.
     */
    public void addGetterAccessor(RuntimeValue<GetterAndSetterAccessorsContainer> container, String className, String fieldName,
            String accessorName) {
        try {
            // Create a new accessor object early
            GetterAccessor accessor = (GetterAccessor) Thread.currentThread()
                    .getContextClassLoader()
                    .loadClass(accessorName)
                    .getDeclaredConstructor()
                    .newInstance();
            container.getValue().putGetter(className, fieldName, accessor);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize " + accessorName + ": " + e.getMessage());
        }
    }

    /**
     * Add a setter accessor to a container.
     */
    public void addSetterAccessor(RuntimeValue<GetterAndSetterAccessorsContainer> container, String className, String fieldName,
            String accessorName) {
        try {
            // Create a new accessor object early
            SetterAccessor accessor = (SetterAccessor) Thread.currentThread()
                    .getContextClassLoader()
                    .loadClass(accessorName)
                    .getDeclaredConstructor()
                    .newInstance();
            container.getValue().putSetter(className, fieldName, accessor);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize " + accessorName + ": " + e.getMessage());
        }
    }
}
