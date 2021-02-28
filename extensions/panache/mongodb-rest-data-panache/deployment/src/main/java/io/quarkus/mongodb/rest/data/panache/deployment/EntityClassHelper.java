package io.quarkus.mongodb.rest.data.panache.deployment;

import java.util.ArrayList;
import java.util.List;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.quarkus.deployment.bean.JavaBeanUtil;
import io.quarkus.gizmo.MethodDescriptor;

public class EntityClassHelper {

    private static final DotName OBJECT_ID = DotName.createSimple(ObjectId.class.getName());

    private static final DotName BSON_ID_ANNOTATION = DotName.createSimple(BsonId.class.getName());

    private final IndexView index;

    public EntityClassHelper(IndexView index) {
        this.index = index;
    }

    public FieldInfo getIdField(String className) {
        return getIdField(index.getClassByName(DotName.createSimple(className)));
    }

    private FieldInfo getIdField(ClassInfo classInfo) {
        ClassInfo tmpClassInfo = classInfo;
        while (tmpClassInfo != null) {
            for (FieldInfo field : tmpClassInfo.fields()) {
                if (field.type().name().equals(OBJECT_ID) || field.hasAnnotation(BSON_ID_ANNOTATION)) {
                    return field;
                }
            }
            if (tmpClassInfo.superName() != null) {
                tmpClassInfo = index.getClassByName(tmpClassInfo.superName());
            } else {
                tmpClassInfo = null;
            }
        }
        throw new IllegalArgumentException("Couldn't find id field of " + classInfo);
    }

    public List<FieldInfo> getAllFields(String className) {
        return getAllFields(index.getClassByName(DotName.createSimple(className)));
    }

    private List<FieldInfo> getAllFields(ClassInfo classInfo) {
        List<FieldInfo> fields = new ArrayList<>();
        ClassInfo tmpClassInfo = classInfo;
        while (tmpClassInfo != null) {
            for (FieldInfo field : tmpClassInfo.fields()) {
                fields.add(field);
            }
            if (tmpClassInfo.superName() != null) {
                tmpClassInfo = index.getClassByName(tmpClassInfo.superName());
            } else {
                tmpClassInfo = null;
            }
        }
        return fields;

    }

    public MethodDescriptor getSetter(String className, FieldInfo field) {
        return getSetter(index.getClassByName(DotName.createSimple(className)), field);
    }

    private MethodDescriptor getSetter(ClassInfo entityClass, FieldInfo field) {
        if (entityClass == null) {
            return null;
        }
        MethodInfo methodInfo = entityClass.method(JavaBeanUtil.getSetterName(field.name()), field.type());
        if (methodInfo != null) {
            return MethodDescriptor.of(methodInfo);
        } else if (entityClass.superName() != null) {
            return getSetter(index.getClassByName(entityClass.superName()), field);
        }
        return null;
    }

    public MethodDescriptor getGetter(String className, FieldInfo field) {
        return getGetter(index.getClassByName(DotName.createSimple(className)), field);
    }

    private MethodDescriptor getGetter(ClassInfo entityClass, FieldInfo field) {
        MethodDescriptor getter = getMethod(entityClass, JavaBeanUtil.getGetterName(field.name(), field.type().name()));
        if (getter != null) {
            return getter;
        }
        return null;
    }

    private MethodDescriptor getMethod(ClassInfo entityClass, String name, Type... parameters) {
        if (entityClass == null) {
            return null;
        }
        MethodInfo methodInfo = entityClass.method(name, parameters);
        if (methodInfo != null) {
            return MethodDescriptor.of(methodInfo);
        } else if (entityClass.superName() != null) {
            return getMethod(index.getClassByName(entityClass.superName()), name, parameters);
        }
        return null;
    }
}
