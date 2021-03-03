package io.quarkus.rest.data.panache.deployment.methods;

import static io.quarkus.gizmo.MethodDescriptor.ofMethod;

import java.util.HashMap;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.gizmo.BranchResult;
import io.quarkus.gizmo.BytecodeCreator;
import io.quarkus.gizmo.ClassCreator;
import io.quarkus.gizmo.FieldDescriptor;
import io.quarkus.gizmo.MethodCreator;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;
import io.quarkus.rest.data.panache.deployment.ResourceMetadata;
import io.quarkus.rest.data.panache.deployment.properties.ResourceProperties;
import io.quarkus.rest.data.panache.deployment.utils.ResponseImplementor;

public final class PatchMethodImplementor extends StandardMethodImplementor {

    private static final String METHOD_NAME = "patch";

    private static final String RESOURCE_UPDATE_PATCH_METHOD_NAME = "patch";

    private static final String RESOURCE_UPDATE_METHOD_NAME = "update";

    private static final String RESOURCE_GET_METHOD_NAME = "get";

    private static final String REL = "patch";

    @Override
    protected void implementInternal(ClassCreator classCreator, ResourceMetadata resourceMetadata,
            ResourceProperties resourceProperties, FieldDescriptor resourceField) {
        MethodCreator methodCreator = classCreator.getMethodCreator(METHOD_NAME, Response.class.getName(),
                resourceMetadata.getIdType(), String.class);

        // Add method annotations
        addPathAnnotation(methodCreator,
                appendToPath(resourceProperties.getPath(RESOURCE_UPDATE_PATCH_METHOD_NAME), "{id}"));
        addTransactionalAnnotation(methodCreator);
        addPatchAnnotation(methodCreator);
        addPathParamAnnotation(methodCreator.getParameterAnnotations(0), "id");
        addConsumesAnnotation(methodCreator, APPLICATION_JSON);
        addProducesAnnotation(methodCreator, APPLICATION_JSON);
        addLinksAnnotation(methodCreator, resourceMetadata.getEntityType(), REL);

        // Invoke resource methods
        ResultHandle resource = methodCreator.readInstanceField(resourceField, methodCreator.getThis());
        ResultHandle id = methodCreator.getMethodParam(0);
        ResultHandle rawJson = methodCreator.getMethodParam(1);
        ResultHandle jsonMap = rawJsonToMap(methodCreator, rawJson);
        BranchResult entityExists = doesEntityExist(methodCreator, resourceMetadata.getResourceClass(), resource, id);
        updateAndReturn(entityExists.trueBranch(), resourceMetadata.getResourceClass(), resource, id, jsonMap);
        updateAndReturn(entityExists.falseBranch(), resourceMetadata.getResourceClass(), resource, id, jsonMap);
        //        createAndReturn(entityExists.falseBranch(), resourceMetadata.getResourceClass(), resource, id, entityToSave);
        methodCreator.close();
    }

    @Override
    protected String getResourceMethodName() {
        return RESOURCE_UPDATE_PATCH_METHOD_NAME;
    }

    private BranchResult doesEntityExist(BytecodeCreator creator, String resourceClass, ResultHandle resource,
            ResultHandle id) {
        ResultHandle entity = creator.invokeVirtualMethod(
                ofMethod(resourceClass, RESOURCE_GET_METHOD_NAME, Object.class, Object.class), resource, id);
        return creator.ifNotNull(entity);
    }

    private ResultHandle rawJsonToMap(BytecodeCreator creator, ResultHandle rawJson) {
        MethodDescriptor initializeMapper = MethodDescriptor.ofConstructor(ObjectMapper.class);
        ResultHandle objectMapper = creator.newInstance(initializeMapper);
        ResultHandle map = creator.invokeVirtualMethod(
                ofMethod(ObjectMapper.class, "readValue", Object.class, String.class, Class.class), objectMapper,
                rawJson, creator.loadClass(HashMap.class));
        return map;
    }

    private void createAndReturn(BytecodeCreator creator, String resourceClass, ResultHandle resource, ResultHandle id,
            ResultHandle entityToSave) {
        ResultHandle entity = creator.invokeVirtualMethod(
                ofMethod(resourceClass, RESOURCE_UPDATE_METHOD_NAME, Object.class, Object.class, Object.class),
                resource, id, entityToSave);
        creator.returnValue(ResponseImplementor.created(creator, entity));
    }

    private void updateAndReturn(BytecodeCreator creator, String resourceClass, ResultHandle resource, ResultHandle id,
            ResultHandle entityToSave) {
        creator.invokeVirtualMethod(
                ofMethod(resourceClass, RESOURCE_UPDATE_PATCH_METHOD_NAME, Object.class, Object.class, HashMap.class),
                resource, id, entityToSave);
        creator.returnValue(ResponseImplementor.noContent(creator));
    }
}
