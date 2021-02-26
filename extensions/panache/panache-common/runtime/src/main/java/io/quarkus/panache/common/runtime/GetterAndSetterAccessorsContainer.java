package io.quarkus.panache.common.runtime;

import java.util.HashMap;
import java.util.Map;

public class GetterAndSetterAccessorsContainer {

    private final Map<String, Map<String, GetterAccessor>> getterAccessors = new HashMap<>();

    private final Map<String, Map<String, SetterAccessor>> setterAccessors = new HashMap<>();

    public GetterAccessor getGetter(String className, String fieldName) {
        return getterAccessors.get(className).get(fieldName);
    }

    public SetterAccessor getSetter(String className, String fieldName) {
        return setterAccessors.get(className).get(fieldName);
    }

    public void putGetter(String className, String fieldName, GetterAccessor getterAccessor) {
        if (!getterAccessors.containsKey(className)) {
            getterAccessors.put(className, new HashMap<>());
        }
        Map<String, GetterAccessor> getterAccessorsByField = getterAccessors.get(className);
        if (!getterAccessorsByField.containsKey(fieldName)) {
            getterAccessorsByField.put(fieldName, getterAccessor);
        }
    }

    public void putSetter(String className, String fieldName, SetterAccessor setterAccessor) {
        if (!setterAccessors.containsKey(className)) {
            setterAccessors.put(className, new HashMap<>());
        }
        Map<String, SetterAccessor> setterAccessorsByField = setterAccessors.get(className);
        if (!setterAccessorsByField.containsKey(fieldName)) {
            setterAccessorsByField.put(fieldName, setterAccessor);
        }
    }
}
