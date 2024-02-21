/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.javaee.wildfly.ide.commands;

import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.DEPLOYMENT;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.INCLUDE_RUNTIME;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.UNDEFINED;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLContext;
import javax.security.auth.callback.CallbackHandler;
import org.netbeans.modules.javaee.wildfly.WildflyClassLoader;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginUtils;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginUtils.Version;

/**
 *
 * @author Emmanuel Hugonnet (ehsavoie) <ehsavoie@netbeans.org>
 */
public class WildflyManagementAPI {

    private static final String SASL_DISALLOWED_MECHANISMS = "SASL_DISALLOWED_MECHANISMS";
    private static final String JBOSS_LOCAL_USER = "JBOSS-LOCAL-USER";

    private static final Map<String, String> ENABLED_LOCAL_AUTH = Collections.emptyMap();
    private static final int TIMEOUT = 1000;

    static Object createClient(WildflyClassLoader cl, Version version, final String serverAddress, final int serverPort,
            final CallbackHandler handler) throws ReflectiveOperationException, NoSuchAlgorithmException {
        Class<?> clazz = cl.loadClass("org.jboss.as.controller.client.ModelControllerClient$Factory"); // NOI18N
        if (version.compareTo(WildflyPluginUtils.WILDFLY_26_0_0) >= 0) {
            Class<?> configurationBuilderClazz = cl.loadClass("org.jboss.as.controller.client.ModelControllerClientConfiguration$Builder");
            Object configurationBuilder = configurationBuilderClazz.getConstructor().newInstance();
            configurationBuilderClazz.getDeclaredMethod("setHostName", String.class).invoke(configurationBuilder, serverAddress);
            configurationBuilderClazz.getDeclaredMethod("setPort", int.class).invoke(configurationBuilder, serverPort);
            configurationBuilderClazz.getDeclaredMethod("setHandler", CallbackHandler.class).invoke(configurationBuilder, handler);
            configurationBuilderClazz.getDeclaredMethod("setConnectionTimeout", int.class).invoke(configurationBuilder, TIMEOUT);
            configurationBuilderClazz.getDeclaredMethod("setSaslOptions", Map.class).invoke(configurationBuilder, ENABLED_LOCAL_AUTH);
            Method method = clazz.getDeclaredMethod("create", cl.loadClass("org.jboss.as.controller.client.ModelControllerClientConfiguration"));
            return method.invoke(null, configurationBuilderClazz.getDeclaredMethod("build").invoke(configurationBuilder));
        }
        if (version.compareTo(WildflyPluginUtils.WILDFLY_9_0_0) >= 0) {
            Method method = clazz.getDeclaredMethod("create", String.class, int.class, CallbackHandler.class, SSLContext.class, int.class, Map.class);
            return method.invoke(null, serverAddress, serverPort, handler, SSLContext.getDefault(), TIMEOUT, ENABLED_LOCAL_AUTH);
        }
        Method method = clazz.getDeclaredMethod("create", String.class, int.class, CallbackHandler.class, SSLContext.class, int.class);
        return method.invoke(null, serverAddress, serverPort, handler, SSLContext.getDefault(), TIMEOUT);
    }

    static void closeClient(WildflyClassLoader cl, Object client) throws ReflectiveOperationException {
        Method method = client.getClass().getMethod("close", new Class[]{});
        method.invoke(client, (Object[]) null);
    }

    // ModelNode
    static Object createDeploymentPathAddressAsModelNode(WildflyClassLoader cl, String name) throws ReflectiveOperationException {
        Class paClazz = cl.loadClass("org.jboss.as.controller.PathAddress"); // NOI18N
        Class peClazz = cl.loadClass("org.jboss.as.controller.PathElement"); // NOI18N

        Method peFactory = peClazz.getDeclaredMethod("pathElement",// NOI18N
                name != null ? new Class[]{String.class, String.class} : new Class[]{String.class});
        Object pe = peFactory.invoke(null,
                name != null ? new Object[]{DEPLOYMENT, name} : new Object[]{DEPLOYMENT});// NOI18N

        Object array = Array.newInstance(peClazz, 1);
        Array.set(array, 0, pe);
        Method paFactory = paClazz.getDeclaredMethod("pathAddress", array.getClass()); // NOI18N
        Object pa = paFactory.invoke(null, array);

        Method toModelNode = pa.getClass().getMethod("toModelNode", (Class<?>[]) null); // NOI18N
        return toModelNode.invoke(pa, (Object[]) null);
    }

    // ModelNode
    static Object createPathAddressAsModelNode(WildflyClassLoader cl, LinkedHashMap<Object, Object> elements) throws ReflectiveOperationException {
        Class paClazz = cl.loadClass("org.jboss.as.controller.PathAddress"); // NOI18N
        Class peClazz = cl.loadClass("org.jboss.as.controller.PathElement"); // NOI18N

        Method peFactory = peClazz.getDeclaredMethod("pathElement", new Class[]{String.class, String.class});
        Object array = Array.newInstance(peClazz, elements.size());
        int i = 0;
        for (Map.Entry<Object, Object> entry : elements.entrySet()) {
            Array.set(array, i, peFactory.invoke(null, new Object[]{entry.getKey(), entry.getValue()}));
            i++;
        }

        Method paFactory = paClazz.getDeclaredMethod("pathAddress", array.getClass()); // NOI18N
        Object pa = paFactory.invoke(null, array);

        Method toModelNode = pa.getClass().getMethod("toModelNode", (Class<?>[]) null); // NOI18N
        return toModelNode.invoke(pa, (Object[]) null);
    }

    // ModelNode
    static Object createOperation(WildflyClassLoader cl, Object name, Object modelNode) throws ReflectiveOperationException {
        Class<?> clazz = cl.loadClass("org.jboss.as.controller.client.helpers.Operations"); // NOI18N
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Method method = clazz.getDeclaredMethod("createOperation", new Class[]{String.class, modelClazz});
        return method.invoke(null, name, modelNode);
    }

    // ModelNode
    static Object createReadResourceOperation(WildflyClassLoader cl, Object modelNode, boolean recursive, boolean runtime) throws ReflectiveOperationException {
        Class clazz = cl.loadClass("org.jboss.as.controller.client.helpers.Operations"); // NOI18N
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Method method = clazz.getDeclaredMethod("createReadResourceOperation", new Class[]{modelClazz, boolean.class});
        Object op =  method.invoke(null, modelNode, recursive);
        setModelNodeChild(cl, getModelNodeChild(cl, op, INCLUDE_RUNTIME), runtime);
        return op;
    }

    // ModelNode
    static Object createRemoveOperation(WildflyClassLoader cl, Object modelNode) throws ReflectiveOperationException {
        Class<?> clazz = cl.loadClass("org.jboss.as.controller.client.helpers.Operations"); // NOI18N
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Method method = clazz.getDeclaredMethod("createRemoveOperation", new Class[]{modelClazz});
        return method.invoke(null, modelNode);
    }

    // ModelNode
    static Object createAddOperation(WildflyClassLoader cl, Object modelNode) throws ReflectiveOperationException {
        Class<?> clazz = cl.loadClass("org.jboss.as.controller.client.helpers.Operations"); // NOI18N
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Method method = clazz.getDeclaredMethod("createAddOperation", new Class[]{modelClazz});
        return method.invoke(null, modelNode);
    }

    // ModelNode
    static Object readResult(WildflyClassLoader cl, Object modelNode) throws ReflectiveOperationException {
        Class<?> clazz = cl.loadClass("org.jboss.as.controller.client.helpers.Operations"); // NOI18N
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Method method = clazz.getDeclaredMethod("readResult", new Class[]{modelClazz});
        return method.invoke(null, modelNode);
    }

    // ModelNode
    static Object getModelNodeChild(WildflyClassLoader cl, Object modelNode, Object name) throws ReflectiveOperationException {
        Method method = modelNode.getClass().getMethod("get", String.class);
        return method.invoke(modelNode, name);
    }

    // ModelNode
    static Object getModelNodeChildAtIndex(WildflyClassLoader cl, Object modelNode, int index) throws ReflectiveOperationException {
        Method method = modelNode.getClass().getMethod("get", int.class);
        return method.invoke(modelNode, index);
    }

    // ModelNode
    static Object getModelNodeChildAtPath(WildflyClassLoader cl, Object modelNode, Object[] path) throws ReflectiveOperationException {
        Method method = modelNode.getClass().getMethod("get", String[].class);
        Object array = Array.newInstance(String.class, path.length);
        for (int i = 0; i < path.length; i++) {
            Array.set(array, i, path[i]);
        }
        return method.invoke(modelNode, array);
    }

    // ModelNode
    static boolean modelNodeHasChild(WildflyClassLoader cl, Object modelNode, String child) throws ReflectiveOperationException {
        Method method = modelNode.getClass().getMethod("has", String.class);
        return (Boolean) method.invoke(modelNode, child);
    }

    // ModelNode
    static boolean modelNodeHasDefinedChild(WildflyClassLoader cl, Object modelNode, String child) throws ReflectiveOperationException {
        Method method = modelNode.getClass().getMethod("hasDefined", String.class);
        return (Boolean) method.invoke(modelNode, child);
    }

    // ModelNode
    static Object createModelNode(WildflyClassLoader cl) throws ReflectiveOperationException {
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        return modelClazz.getDeclaredConstructor().newInstance();
    }

    // ModelNode
    static Object setModelNodeChildString(WildflyClassLoader cl, Object modelNode, Object value) throws ReflectiveOperationException {
        assert value != null;
        Method method = modelNode.getClass().getMethod("set", String.class);
        return method.invoke(modelNode, value);
    }

    // ModelNode
    static Object setModelNodeChild(WildflyClassLoader cl, Object modelNode, Object value) throws ReflectiveOperationException {
        assert value != null;
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Method method = modelNode.getClass().getMethod("set", modelClazz);
        return method.invoke(modelNode, value);
    }

    // ModelNode
    static Object setModelNodeChild(WildflyClassLoader cl, Object modelNode, int value) throws ReflectiveOperationException {
        Method method = modelNode.getClass().getMethod("set", int.class);
        return method.invoke(modelNode, value);
    }

    // ModelNode
    static Object setModelNodeChild(WildflyClassLoader cl, Object modelNode, boolean value) throws ReflectiveOperationException {
        Method method = modelNode.getClass().getMethod("set", boolean.class);
        return method.invoke(modelNode, value);
    }

    // ModelNode
    static Object setModelNodeChildEmptyList(WildflyClassLoader cl, Object modelNode) throws ReflectiveOperationException {

        Method method = modelNode.getClass().getMethod("setEmptyList", (Class<?>[]) null);
        return method.invoke(modelNode, (Object[]) null);
    }

    // ModelNode
    static Object setModelNodeChildBytes(WildflyClassLoader cl, Object modelNode, byte[] value) throws ReflectiveOperationException {

        Method method = modelNode.getClass().getMethod("set", byte[].class);
        return method.invoke(modelNode, value);
    }

    // ModelNode
    static Object addModelNodeChild(WildflyClassLoader cl, Object modelNode, Object toAddModelNode) throws ReflectiveOperationException {
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Method method = modelNode.getClass().getMethod("add", modelClazz);
        return method.invoke(modelNode, toAddModelNode);
    }

    static Object addModelNodeChildString(WildflyClassLoader cl, Object modelNode, String toAddModelNode) throws ReflectiveOperationException {
        Method method = modelNode.getClass().getMethod("add", String.class);
        return method.invoke(modelNode, toAddModelNode);
    }

    static boolean modelNodeIsDefined(WildflyClassLoader cl, Object modelNode) throws ReflectiveOperationException {
        Method method = modelNode.getClass().getMethod("isDefined", (Class<?>[]) null);
        return (Boolean) method.invoke(modelNode, (Object[]) null);
    }

    static String modelNodeAsString(WildflyClassLoader cl, Object modelNode) throws IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Method method = modelNode.getClass().getMethod("asString", (Class<?>[]) null);
        return (String) method.invoke(modelNode, (Object[]) null);
    }

    static String modelNodeAsPropertyForName(WildflyClassLoader cl, Object modelNode) throws ReflectiveOperationException {
        Method method = modelNode.getClass().getMethod("asProperty", (Class<?>[]) null);
        Object property = method.invoke(modelNode, (Object[]) null);
        return getPropertyName(cl, property);
    }

    static Object modelNodeAsPropertyForValue(WildflyClassLoader cl, Object modelNode) throws ReflectiveOperationException {
        Method method = modelNode.getClass().getMethod("asProperty", (Class<?>[]) null);
        Object property = method.invoke(modelNode, (Object[]) null);
        return getPropertyValue(cl, property);
    }

    static String getPropertyName(WildflyClassLoader cl, Object property) throws ReflectiveOperationException {
        Method method = property.getClass().getMethod("getName", (Class<?>[]) null);
        return (String) method.invoke(property, (Object[]) null);
    }

    static Object getPropertyValue(WildflyClassLoader cl, Object property) throws ReflectiveOperationException {
        Method method = property.getClass().getMethod("getValue", (Class<?>[]) null);
        return method.invoke(property, (Object[]) null);
    }


    // List<ModelNode>
    static List modelNodeAsList(WildflyClassLoader cl, Object modelNode) throws ReflectiveOperationException {
        Method method = modelNode.getClass().getMethod("asList", (Class<?>[]) null);
        return (List) method.invoke(modelNode, (Object[]) null);
    }

    static List modelNodeAsPropertyList(WildflyClassLoader cl, Object modelNode) throws ReflectiveOperationException {
        Method method = modelNode.getClass().getMethod("asPropertyList", (Class<?>[]) null);
        return (List) method.invoke(modelNode, (Object[]) null);
    }

    static boolean modelNodeAsBoolean(WildflyClassLoader cl, Object modelNode) throws ReflectiveOperationException {
        Method method = modelNode.getClass().getMethod("asBoolean", (Class<?>[]) null);
        return (boolean) method.invoke(modelNode, (Object[]) null);
    }

    static int modelNodeAsInt(WildflyClassLoader cl, Object modelNode) throws ReflectiveOperationException {
        Method method = modelNode.getClass().getMethod("asInt", (Class<?>[]) null);
        return (int) method.invoke(modelNode, (Object[]) null);
    }

    static boolean isSuccessfulOutcome(WildflyClassLoader cl, Object modelNode) throws ReflectiveOperationException {
        Class<?> clazz = cl.loadClass("org.jboss.as.controller.client.helpers.Operations"); // NOI18N
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Method method = clazz.getDeclaredMethod("isSuccessfulOutcome", modelClazz);
        return (Boolean) method.invoke(null, modelNode);
    }

    static boolean isDefined(String value) {
        return value != null && !value.isEmpty() && !UNDEFINED.equalsIgnoreCase(value);
    }
}
