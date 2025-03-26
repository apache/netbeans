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

import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.ADD;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.ADDRESS;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.ATTRIBUTES_ONLY;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.BYTES;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.CHILD_TYPE;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.COMPOSITE;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.CONTENT;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.CONTROLLER_PROCESS_STATE_STARTING;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.CONTROLLER_PROCESS_STATE_STOPPING;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.CORE_SERVICE;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.DEPLOYMENT;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.DEPLOYMENT_REDEPLOY_OPERATION;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.DEPLOYMENT_UNDEPLOY_OPERATION;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.EXPRESSION;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.INCLUDE_DEFAULTS;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.INCLUDE_RUNTIME;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.JAXRS_SUBSYSTEM;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.OP;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.OP_ADDR;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.RESULT;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.SHUTDOWN;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.NAME;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.OUTCOME;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.READ_ATTRIBUTE_OPERATION;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.READ_CHILDREN_NAMES_OPERATION;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.READ_CHILDREN_RESOURCES_OPERATION;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.READ_RESOURCE_OPERATION;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.RECURSIVE;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.RECURSIVE_DEPTH;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.RELATIVE_TO_ONLY;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.RESOLVE_EXPRESSION;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.RESOLVE_PATH;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.RUNTIME_NAME;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.SHOW_RESOURCES;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.STEPS;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.SUBSYSTEM;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.SUCCESS;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.callback.CallbackHandler;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext;
import org.netbeans.modules.javaee.wildfly.WildflyDeploymentFactory;
import org.netbeans.modules.javaee.wildfly.WildflyTargetModuleID;
import org.netbeans.modules.javaee.wildfly.config.WildflyConnectionFactory;
import org.netbeans.modules.javaee.wildfly.config.WildflyDatasource;
import org.netbeans.modules.javaee.wildfly.config.WildflyMailSessionResource;
import org.netbeans.modules.javaee.wildfly.config.WildflyMessageDestination;
import org.netbeans.modules.javaee.wildfly.config.WildflyResourceAdapter;
import org.netbeans.modules.javaee.wildfly.config.WildflySocket;

import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.addModelNodeChild;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.addModelNodeChildString;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.closeClient;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.createAddOperation;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.createClient;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.createDeploymentPathAddressAsModelNode;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.createModelNode;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.createOperation;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.createPathAddressAsModelNode;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.createReadResourceOperation;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.createRemoveOperation;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.getModelNodeChild;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.getModelNodeChildAtIndex;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.getModelNodeChildAtPath;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.getPropertyName;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.getPropertyValue;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.isSuccessfulOutcome;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.modelNodeAsBoolean;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.modelNodeAsInt;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.modelNodeAsList;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.modelNodeAsPropertyForName;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.modelNodeAsPropertyForValue;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.modelNodeAsPropertyList;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.modelNodeAsString;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.modelNodeHasChild;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.modelNodeHasDefinedChild;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.modelNodeIsDefined;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.readResult;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.setModelNodeChild;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.setModelNodeChildBytes;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.setModelNodeChildEmptyList;
import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.setModelNodeChildString;
import static org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginUtils.WILDFLY_10_0_0;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.netbeans.modules.javaee.wildfly.WildflyClassLoader;
import org.netbeans.modules.javaee.wildfly.config.WildflyJaxrsResource;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginProperties;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginUtils.Version;
import org.netbeans.modules.javaee.wildfly.nodes.WildflyDatasourceNode;
import org.netbeans.modules.javaee.wildfly.nodes.WildflyDestinationNode;
import org.netbeans.modules.javaee.wildfly.nodes.WildflyEarApplicationNode;
import org.netbeans.modules.javaee.wildfly.nodes.WildflyEjbComponentNode;
import org.netbeans.modules.javaee.wildfly.nodes.WildflyEjbModuleNode;
import org.netbeans.modules.javaee.wildfly.nodes.WildflyJaxrsResourceNode;
import org.netbeans.modules.javaee.wildfly.nodes.WildflyWebModuleNode;
import org.netbeans.modules.javaee.wildfly.util.PathUtil;
import org.openide.util.Lookup;
import org.openide.util.Pair;

/**
 *
 * @author ehugonnet
 */
public class WildflyClient {

    private static final Logger LOGGER = Logger.getLogger(WildflyClient.class.getName());

    private static final String SERVER_STATE = "server-state"; // NOI18N
    private static final String SERVER_ENVIRONMENT = "server-environment";// NOI18N
    private static final String WEB_SUBSYSTEM = "undertow"; // NOI18N
    private static final String EJB3_SUBSYSTEM = "ejb3"; // NOI18N
    private static final String DATASOURCES_SUBSYSTEM = "datasources"; // NOI18N
    private static final String MAIL_SUBSYSTEM = "mail"; // NOI18N
    private static final String MESSAGING_SUBSYSTEM = "messaging"; // NOI18N
    private static final String MESSAGING_ACTIVEMQ_SUBSYSTEM = "messaging-activemq"; // NOI18N
    private static final String RESOURCE_ADAPTER_SUBSYSTEM = "resource-adapters"; // NOI18N

    private static final String DATASOURCE_TYPE = "data-source"; // NOI18N
    private static final String HORNETQ_SERVER_TYPE = "hornetq-server"; // NOI18N
    private static final String MESSAGING_ACTIVEMQ_SERVER_TYPE = "server"; // NOI18N
    private static final String MAIL_SESSION_TYPE = "mail-session"; // NOI18N
    private static final String JMSQUEUE_TYPE = "jms-queue"; // NOI18N
    private static final String JMSTOPIC_TYPE = "jms-topic"; // NOI18N
    private static final String CONNECTION_FACTORY_TYPE = "connection-factory"; // NOI18N
    private static final String RESOURCE_ADAPTER_TYPE = "resource-adapter"; // NOI18N

    private final String serverAddress;
    private final int serverPort;
    private final CallbackHandler handler;
    private final InstanceProperties ip;
    private Object client;
    private final Version version;

    /**
     * Get the value of serverPort
     *
     * @return the value of serverPort
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * Get the value of serverPort
     *
     * @return the value of serverPort
     */
    public String getServerLog() throws IOException {
        WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
        LinkedHashMap<Object, Object> values = new LinkedHashMap<>();
        values.put(SUBSYSTEM, "logging");
        values.put("periodic-rotating-file-handler", "FILE");
        return resolvePath(cl, values);
    }

    private String resolvePath(WildflyClassLoader cl, LinkedHashMap<Object, Object> values) throws IOException {
        try {
            final Object readPathOperation = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readPathOperation, OP), RESOLVE_PATH);
            // ModelNode
            Object scannerAddress = createPathAddressAsModelNode(cl, values);
            setModelNodeChild(cl, getModelNodeChild(cl, readPathOperation, ADDRESS), scannerAddress);
            setModelNodeChildString(cl, getModelNodeChild(cl, readPathOperation, RELATIVE_TO_ONLY), "false");
            Object response = executeOnModelNode(cl, readPathOperation);
            if (isSuccessfulOutcome(cl, response)) {
                return modelNodeAsString(cl, readResult(cl, response));
            }
            return "";
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Get the value of serverAddress
     *
     * @return the value of serverAddress
     */
    public String getServerAddress() {
        return serverAddress;
    }

    public WildflyClient(InstanceProperties ip, Version version, String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.ip = ip;
        this.version = version;
        handler = new Authentication().getCallbackHandler();
    }

    public WildflyClient(InstanceProperties ip, Version version, String serverAddress, int serverPort, String login,
            String password) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.ip = ip;
        this.version = version;
        handler = new Authentication(login, password.toCharArray()).getCallbackHandler();
    }

    // ModelControllerClient
    private synchronized Object getClient(WildflyClassLoader cl) {
        if (client == null) {
            try {
                this.client = createClient(cl, version, serverAddress, serverPort, handler);
            } catch (Throwable ex) {
                LOGGER.log(Level.WARNING, null, ex);
                return null;
            }
        }
        return this.client;
    }

    private synchronized void close() {
        try {
            if (this.client != null) {
                closeClient(WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip), client);
            }
            this.client = null;
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

    public synchronized void shutdownServer(int timeout) throws IOException, InterruptedException, TimeoutException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            // ModelNode
            Object shutdownOperation = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, shutdownOperation, OP), SHUTDOWN);
            try {
                executeAsync(cl, shutdownOperation, null).get(timeout, TimeUnit.MILLISECONDS);
            } catch (ExecutionException ex) {
                throw new IOException(ex);
            }
            close();
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    public synchronized boolean isServerRunning(String homeDir, String configFile) {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            // ModelNode
            Object statusOperation = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, statusOperation, OP), READ_ATTRIBUTE_OPERATION);
            setModelNodeChildEmptyList(cl, getModelNodeChild(cl, statusOperation, OP_ADDR));
            setModelNodeChildString(cl, getModelNodeChild(cl, statusOperation, NAME), SERVER_STATE);
            // ModelNode
            Object response = executeAsync(cl, statusOperation, null).get();
            if (SUCCESS.equals(modelNodeAsString(cl, getModelNodeChild(cl, response, OUTCOME)))
                    && !CONTROLLER_PROCESS_STATE_STARTING.equals(modelNodeAsString(cl, getModelNodeChild(cl, response, RESULT)))
                    && !CONTROLLER_PROCESS_STATE_STOPPING.equals(modelNodeAsString(cl, getModelNodeChild(cl, response, RESULT)))) {
                Pair<String, String> paths = getServerPaths(cl);
                if (paths != null) {
                    String homeDirNorm = homeDir == null ? null : PathUtil.normalizePath(homeDir);
                    String configFileNorm = configFile == null ? null : PathUtil.normalizePath(configFile);
                    return paths.first().equals(homeDirNorm) && paths.second().equals(configFileNorm);
                }
                return true;
            } else {
                return false;
            }
        } catch (InvocationTargetException ex) {
            LOGGER.log(Level.FINE, null, ex.getTargetException());
            close();
            return false;
        } catch (ReflectiveOperationException | IOException | InterruptedException | ExecutionException ex) {
            LOGGER.log(Level.FINE, null, ex);
            close();
            return false;
        }
    }

    // ModelNode
    private synchronized Object executeOnModelNode(WildflyClassLoader cl, Object modelNode) throws IOException, ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Object clientLocal = getClient(cl);
        if (clientLocal == null) {
            throw new IOException("Not connected to WildFly server");
        }
        Method method = clientLocal.getClass().getMethod("execute", modelClazz);
        return method.invoke(clientLocal, modelNode);
    }

    // ModelNode
    private synchronized Object executeOnOperation(WildflyClassLoader cl, Object operation) throws IOException, ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        Class operationClazz = cl.loadClass("org.jboss.as.controller.client.Operation"); // NOI18N
        Object clientLocal = getClient(cl);
        if (clientLocal == null) {
            throw new IOException("Not connected to WildFly server");
        }
        Method method = clientLocal.getClass().getMethod("execute", operationClazz);
        return method.invoke(clientLocal, operation);
    }

    private synchronized Future<?> executeAsync(WildflyClassLoader cl, Object modelNode, Object operationMessageHandler) throws IOException, ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Class handlerClazz = cl.loadClass("org.jboss.as.controller.client.OperationMessageHandler"); // NOI18N
        Object clientLocal = getClient(cl);
        if (clientLocal == null) {
            throw new IOException("Not connected to WildFly server");
        }
        Method method = clientLocal.getClass().getMethod("executeAsync", modelClazz, handlerClazz);
        return (Future) method.invoke(clientLocal, modelNode, operationMessageHandler);
    }

    public Collection<WildflyModule> listAvailableModules() throws IOException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<WildflyModule> modules = new ArrayList<>();
            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, null);
            // ModelNode
            Object readDeployments = createReadResourceOperation(cl, deploymentAddressModelNode, true, true);
            // ModelNode
            Object response = executeOnModelNode(cl, readDeployments);
            if (isSuccessfulOutcome(cl, response)) {
                // ModelNode
                Object result = readResult(cl, response);
                // List<ModelNode>
                List webapps = modelNodeAsList(cl, result);
                for (Object application : webapps) {
                    String applicationName = modelNodeAsString(cl, getModelNodeChild(cl, readResult(cl, application), NAME));
                    // ModelNode
                    Object deployment = getModelNodeChild(cl, getModelNodeChild(cl, readResult(cl, application), SUBSYSTEM), WEB_SUBSYSTEM);
                    WildflyModule module = new WildflyModule(applicationName, true);
                    if (modelNodeIsDefined(cl, deployment)) {
                        String url = "http://" + serverAddress + ':' + getHttpPort() + modelNodeAsString(cl, getModelNodeChild(cl, deployment, "context-root"));
                        module.setUrl(url);
                    }
                    modules.add(module);
                }
            }
            return modules;
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    public Collection<WildflyWebModuleNode> listWebModules(Lookup lookup) throws IOException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<WildflyWebModuleNode> modules = new ArrayList<>();
            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, null);
            // ModelNode
            Object readDeployments = createReadResourceOperation(cl, deploymentAddressModelNode, true, true);
            // ModelNode
            Object response = executeOnModelNode(cl, readDeployments);   
            if (isSuccessfulOutcome(cl, response)) {
                // ModelNode
                Object result = readResult(cl, response);
                // List<ModelNode>
                List webapps = modelNodeAsList(cl, result);
                for (Object application : webapps) {
                    String applicationName = modelNodeAsString(cl, getModelNodeChild(cl, readResult(cl, application), NAME));
                    if (applicationName.endsWith(".war")) {
                        // ModelNode
                        Object deployment = getModelNodeChild(cl, getModelNodeChild(cl, readResult(cl, application), SUBSYSTEM), WEB_SUBSYSTEM);
                        if (modelNodeIsDefined(cl, deployment)) {
                            String url = "http://" + serverAddress + ':' + getHttpPort() + modelNodeAsString(cl, getModelNodeChild(cl, deployment, "context-root"));
                            modules.add(new WildflyWebModuleNode(applicationName, lookup, url));
                        } else {
                            modules.add(new WildflyWebModuleNode(applicationName, lookup, null));
                        }
                    }
                }
            }
            return modules;
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    public String getWebModuleURL(String webModuleName) throws IOException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, webModuleName);
            // ModelNode
            Object readDeployments = createReadResourceOperation(cl, deploymentAddressModelNode, true, true);
            // ModelNode
            Object response = executeOnModelNode(cl, readDeployments);
            if (isSuccessfulOutcome(cl, response)) {
                // ModelNode
                Object result = readResult(cl, response);
                String applicationName = modelNodeAsString(cl, getModelNodeChild(cl, result, NAME));
                if (applicationName.endsWith(".war")) { // NOI18N
                    // ModelNode
                    Object deployment = getModelNodeChild(cl, getModelNodeChild(cl, result, SUBSYSTEM), WEB_SUBSYSTEM);
                    if (modelNodeIsDefined(cl, deployment)) {
                        return "http://" + serverAddress + ':' + getHttpPort() + modelNodeAsString(cl, getModelNodeChild(cl, deployment, "context-root")); // NOI18N
                    }
                } else if (applicationName.endsWith(".ear")) { // NOI18N
                    Object subdeployment = getModelNodeChild(cl, result, Constants.SUBDEPLOYMENT);
                    if (modelNodeIsDefined(cl, subdeployment)) {
                        // ModelNode
                        for (Object node : modelNodeAsList(cl, subdeployment)) {
                            Object child = getModelNodeChildAtIndex(cl, node, 0);
                            if (modelNodeIsDefined(cl, child)) {
                                Object deployment = getModelNodeChild(cl, getModelNodeChild(cl, child, SUBSYSTEM), WEB_SUBSYSTEM);
                                if (modelNodeIsDefined(cl, deployment)) {
                                    return "http://" + serverAddress + ':' // NOI18N
                                            + getHttpPort() + modelNodeAsString(cl, getModelNodeChild(cl, deployment, "context-root")); // NOI18N
                                }
                            }
                        }
                    }
                }
            }
            return "";
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    public Collection<WildflyEjbModuleNode> listEJBModules(Lookup lookup) throws IOException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<WildflyEjbModuleNode> modules = new ArrayList<>();
            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, null);
            // ModelNode
            Object readDeployments = createReadResourceOperation(cl, deploymentAddressModelNode, true, true);
            // ModelNode
            Object response = executeOnModelNode(cl, readDeployments);
            if (isSuccessfulOutcome(cl, response)) {
                // ModelNode
                Object result = readResult(cl, response);
                // List<ModelNode>
                List ejbs = modelNodeAsList(cl, result);
                for (Object ejb : ejbs) {
                    // ModelNode
                    Object deployment = getModelNodeChild(cl, getModelNodeChild(cl, readResult(cl, ejb), SUBSYSTEM), EJB3_SUBSYSTEM);
                    if (modelNodeIsDefined(cl, deployment)) {
                        List<WildflyEjbComponentNode> ejbInstances = new ArrayList<>();
                        ejbInstances.addAll(listEJBs(cl, deployment, WildflyEjbComponentNode.Type.ENTITY));
                        ejbInstances.addAll(listEJBs(cl, deployment, WildflyEjbComponentNode.Type.MDB));
                        ejbInstances.addAll(listEJBs(cl, deployment, WildflyEjbComponentNode.Type.SINGLETON));
                        ejbInstances.addAll(listEJBs(cl, deployment, WildflyEjbComponentNode.Type.STATEFULL));
                        ejbInstances.addAll(listEJBs(cl, deployment, WildflyEjbComponentNode.Type.STATELESS));
                        modules.add(new WildflyEjbModuleNode(modelNodeAsString(cl, getModelNodeChild(cl, readResult(cl, ejb), NAME)), lookup, ejbInstances, true));
                    }
                }
            }
            return modules;
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    public boolean startModule(WildflyTargetModuleID tmid) throws IOException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, tmid.getModuleID());
            // ModelNode
            Object enableDeployment = createOperation(cl, DEPLOYMENT_REDEPLOY_OPERATION, deploymentAddressModelNode);
            Object result = executeOnModelNode(cl, enableDeployment);
            if (isSuccessfulOutcome(cl, result)) {
                tmid.setContextURL(getWebModuleURL(tmid.getModuleID()));
                return true;
            }
            return false;
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    public boolean startModule(String moduleName) throws IOException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, moduleName);
            // ModelNode
            Object enableDeployment = createOperation(cl, DEPLOYMENT_REDEPLOY_OPERATION, deploymentAddressModelNode);
            Object result = executeOnModelNode(cl, enableDeployment);
            return isSuccessfulOutcome(cl, result);
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    public boolean stopModule(String moduleName) throws IOException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, moduleName);
            // ModelNode
            Object enableDeployment = createOperation(cl, DEPLOYMENT_UNDEPLOY_OPERATION, deploymentAddressModelNode);
            return isSuccessfulOutcome(cl, executeOnModelNode(cl, enableDeployment));
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    public boolean undeploy(String fileName) throws IOException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, fileName);

            // ModelNode
            final Object undeploy = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, undeploy, OP), COMPOSITE);
            setModelNodeChildEmptyList(cl, getModelNodeChild(cl, undeploy, ADDRESS));
            // ModelNode
            Object steps = getModelNodeChild(cl, undeploy, STEPS);
            addModelNodeChild(cl, steps, createOperation(cl, DEPLOYMENT_UNDEPLOY_OPERATION, deploymentAddressModelNode));
            addModelNodeChild(cl, steps, createRemoveOperation(cl, deploymentAddressModelNode));
            return isSuccessfulOutcome(cl, executeOnModelNode(cl, undeploy));
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    public boolean deploy(DeploymentContext deployment) throws IOException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            String fileName = deployment.getModuleFile().getName();
            undeploy(fileName);

            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, fileName);

            // ModelNode
            final Object deploy = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, deploy, OP), COMPOSITE);
            setModelNodeChildEmptyList(cl, getModelNodeChild(cl, deploy, ADDRESS));
            // ModelNode
            Object steps = getModelNodeChild(cl, deploy, STEPS);
            // ModelNode
            Object addModule = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, addModule, OP), ADD);
            setModelNodeChildString(cl, getModelNodeChildAtPath(cl, addModule,
                    new Object[]{ADDRESS, DEPLOYMENT}), fileName);
            setModelNodeChildString(cl, getModelNodeChild(cl, addModule, RUNTIME_NAME), fileName);
            setModelNodeChildBytes(cl, getModelNodeChild(cl, getModelNodeChildAtIndex(cl, getModelNodeChild(cl, addModule, CONTENT), 0),
                    BYTES), deployment.getModule().getArchive().asBytes());

            addModelNodeChild(cl, steps, addModule);
            addModelNodeChild(cl, steps, createOperation(cl, DEPLOYMENT_REDEPLOY_OPERATION, deploymentAddressModelNode));
            // ModelNode
            Object result = executeOnModelNode(cl, deploy);
            return isSuccessfulOutcome(cl, result);
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    public Collection<WildflyDatasourceNode> listDatasources(Lookup lookup) throws IOException {
        Set<Datasource> datasources = listDatasources();
        List<WildflyDatasourceNode> modules = new ArrayList<>(datasources.size());
        for (Datasource ds : datasources) {
            modules.add(new WildflyDatasourceNode(((WildflyDatasource) ds).getName(), ds, lookup));
        }
        return modules;
    }

    private Set<Datasource> listDatasources() throws IOException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            Set<Datasource> listedDatasources = new HashSet<>();
            // ModelNode
            final Object readDatasources = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readDatasources, OP), READ_CHILDREN_NAMES_OPERATION);

            LinkedHashMap<Object, Object> values = new LinkedHashMap<>();
            values.put(SUBSYSTEM, DATASOURCES_SUBSYSTEM);
            // ModelNode
            Object path = createPathAddressAsModelNode(cl, values);
            setModelNodeChild(cl, getModelNodeChild(cl, readDatasources, ADDRESS), path);
            setModelNodeChild(cl, getModelNodeChild(cl, readDatasources, RECURSIVE_DEPTH), 0);
            setModelNodeChildString(cl, getModelNodeChild(cl, readDatasources, CHILD_TYPE), DATASOURCE_TYPE);

            // ModelNode
            Object response = executeOnModelNode(cl, readDatasources);
            if (isSuccessfulOutcome(cl, response)) {
                // List<ModelNode>
                List names = modelNodeAsList(cl, readResult(cl, response));
                for (Object datasourceName : names) {
                    listedDatasources.add(getDatasource(cl, modelNodeAsString(cl, datasourceName)));
                }
            }
            return listedDatasources;
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    private WildflyDatasource getDatasource(WildflyClassLoader cl, String name) throws IOException {
        try {
            // ModelNode
            final Object readDatasource = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readDatasource, OP), READ_RESOURCE_OPERATION);
            LinkedHashMap<Object, Object> values = new LinkedHashMap<>();
            values.put(SUBSYSTEM, DATASOURCES_SUBSYSTEM);
            values.put(DATASOURCE_TYPE, name);
            // ModelNode
            Object path = createPathAddressAsModelNode(cl, values);
            setModelNodeChild(cl, getModelNodeChild(cl, readDatasource, ADDRESS), path);
            setModelNodeChild(cl, getModelNodeChild(cl, readDatasource, RECURSIVE_DEPTH), 0);
            // ModelNode
            Object response = executeOnModelNode(cl, readDatasource);
            if (isSuccessfulOutcome(cl, response)) {
                // ModelNode
                Object datasource = readResult(cl, response);
                return new WildflyDatasource(name, modelNodeAsString(cl, getModelNodeChild(cl, datasource, "jndi-name")),
                        modelNodeAsString(cl, getModelNodeChild(cl, datasource, "connection-url")),
                        modelNodeAsString(cl, getModelNodeChild(cl, datasource, "user-name")),
                        modelNodeAsString(cl, getModelNodeChild(cl, datasource, "password")),
                        modelNodeAsString(cl, getModelNodeChild(cl, datasource, "driver-class")));
            }
            return null;
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    public boolean removeDatasource(String name) throws IOException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            LinkedHashMap<Object, Object> values = new LinkedHashMap<>();
            values.put(SUBSYSTEM, DATASOURCES_SUBSYSTEM);
            values.put(DATASOURCE_TYPE, name);
            // ModelNode
            Object address = createPathAddressAsModelNode(cl, values);
            Object operation = createRemoveOperation(cl, address);
            // ModelNode
            Object response = executeOnModelNode(cl, operation);
            return (isSuccessfulOutcome(cl, response));
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    private Pair<String, String> getServerPaths(WildflyClassLoader cl) {
        try {
            // ModelNode
            final Object readEnvironment = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readEnvironment, OP), READ_RESOURCE_OPERATION);
            LinkedHashMap<Object, Object> values = new LinkedHashMap<>();
            values.put(CORE_SERVICE, SERVER_ENVIRONMENT);
            // ModelNode
            Object path = createPathAddressAsModelNode(cl, values);
            setModelNodeChild(cl, getModelNodeChild(cl, readEnvironment, ADDRESS), path);
            setModelNodeChild(cl, getModelNodeChild(cl, readEnvironment, INCLUDE_RUNTIME), true);
            setModelNodeChild(cl, getModelNodeChild(cl, readEnvironment, INCLUDE_DEFAULTS), true);
            setModelNodeChild(cl, getModelNodeChild(cl, readEnvironment, ATTRIBUTES_ONLY), true);
            // ModelNode
            Object response = executeOnModelNode(cl, readEnvironment);
            if (isSuccessfulOutcome(cl, response)) {
                // ModelNode
                Object environment = readResult(cl, response);
                String homeDir = modelNodeAsString(cl, getModelNodeChild(cl, environment, "home-dir"));
                String configDir = modelNodeAsString(cl, getModelNodeChild(cl, environment, "config-file"));

                if (homeDir != null && configDir != null) {
                    return Pair.of(PathUtil.normalizePath(homeDir), PathUtil.normalizePath(configDir));
                }
            }
            return null;
        } catch (IOException | ReflectiveOperationException ex) {
            return null;
        }
    }

    public Collection<WildflyDestinationNode> listDestinations(Lookup lookup) throws IOException {
        List<WildflyMessageDestination> destinations = listDestinations();
        List<WildflyDestinationNode> modules = new ArrayList<>(destinations.size());
        for (WildflyMessageDestination destination : destinations) {
            modules.add(new WildflyDestinationNode(destination.getName(), destination, lookup));
        }
        return modules;
    }

    public List<WildflyDestinationNode> listDestinationForDeployment(Lookup lookup, String jeeDeploymentName) throws IOException {
        List<WildflyMessageDestination> destinations = listDestinationForDeployment(jeeDeploymentName);
        List<WildflyDestinationNode> modules = new ArrayList<>(destinations.size());
        for (WildflyMessageDestination destination : destinations) {
            modules.add(new WildflyDestinationNode(destination.getName(), destination, lookup));
        }
        return modules;
    }

    public List<WildflyMessageDestination> listDestinationForDeployment(String deployment) throws IOException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<WildflyMessageDestination> destinations = new ArrayList<>();
            // ModelNode
            final Object readMessagingServers = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readMessagingServers, OP), READ_CHILDREN_NAMES_OPERATION);

            LinkedHashMap<Object, Object> values = new LinkedHashMap<>();
            values.put(DEPLOYMENT, deployment);
            values.put(SUBSYSTEM, getMessagingSubsystem());
            // ModelNode
            Object path = createPathAddressAsModelNode(cl, values);
            setModelNodeChild(cl, getModelNodeChild(cl, readMessagingServers, ADDRESS), path);
            setModelNodeChild(cl, getModelNodeChild(cl, readMessagingServers, RECURSIVE_DEPTH), 0);
            setModelNodeChildString(cl, getModelNodeChild(cl, readMessagingServers, CHILD_TYPE), getMessagingServerType());

            // ModelNode
            Object response = executeOnModelNode(cl, readMessagingServers);
            if (isSuccessfulOutcome(cl, response)) {
                // List<ModelNode>
                List names = modelNodeAsList(cl, readResult(cl, response));
                for (Object messagingServer : names) {
                    String messagingServerName = modelNodeAsString(cl, messagingServer);
                    destinations.addAll(getJMSDestinationForServerDeployment(deployment, messagingServerName, Type.QUEUE));
                    destinations.addAll(getJMSDestinationForServerDeployment(deployment, messagingServerName, Type.TOPIC));
                }
            }
            return destinations;
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    public List<WildflyMessageDestination> listDestinations() throws IOException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<WildflyMessageDestination> destinations = new ArrayList<>();
            // ModelNode
            final Object readMessagingServers = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readMessagingServers, OP), READ_CHILDREN_NAMES_OPERATION);

            LinkedHashMap<Object, Object> values = new LinkedHashMap<Object, Object>();
            values.put(SUBSYSTEM, getMessagingSubsystem());
            // ModelNode
            Object path = createPathAddressAsModelNode(cl, values);
            setModelNodeChild(cl, getModelNodeChild(cl, readMessagingServers, ADDRESS), path);
            setModelNodeChild(cl, getModelNodeChild(cl, readMessagingServers, RECURSIVE_DEPTH), 0);
            setModelNodeChildString(cl, getModelNodeChild(cl, readMessagingServers, CHILD_TYPE), getMessagingServerType());

            // ModelNode
            Object response = executeOnModelNode(cl, readMessagingServers);
            if (isSuccessfulOutcome(cl, response)) {
                // List<ModelNode>
                List names = modelNodeAsList(cl, readResult(cl, response));
                for (Object messagingServer : names) {
                    String messagingServerName = modelNodeAsString(cl, messagingServer);
                    destinations.addAll(getJMSDestinationForServer(messagingServerName, Type.QUEUE));
                    destinations.addAll(getJMSDestinationForServer(messagingServerName, Type.TOPIC));
                }
            }
            return destinations;
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    private List<WildflyMessageDestination> getJMSDestinationForServerDeployment(String deployment, String serverName, Type messageType) throws IOException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<WildflyMessageDestination> listedDestinations = new ArrayList<>();
            // ModelNode
            final Object readQueues = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readQueues, OP), READ_CHILDREN_RESOURCES_OPERATION);

            LinkedHashMap<Object, Object> values = new LinkedHashMap<>();
            values.put(DEPLOYMENT, deployment);
            values.put(SUBSYSTEM, getMessagingSubsystem());
            values.put(getMessagingServerType(), serverName);
            // ModelNode
            Object path = createPathAddressAsModelNode(cl, values);
            setModelNodeChild(cl, getModelNodeChild(cl, readQueues, ADDRESS), path);
            setModelNodeChild(cl, getModelNodeChild(cl, readQueues, RECURSIVE_DEPTH), 0);
            if (messageType == Type.QUEUE) {
                setModelNodeChildString(cl, getModelNodeChild(cl, readQueues, CHILD_TYPE), JMSQUEUE_TYPE);
            } else {
                setModelNodeChildString(cl, getModelNodeChild(cl, readQueues, CHILD_TYPE), JMSTOPIC_TYPE);
            }
            setModelNodeChildString(cl, getModelNodeChild(cl, readQueues, INCLUDE_RUNTIME), "true");

            // ModelNode
            Object response = executeOnModelNode(cl, readQueues);
            if (isSuccessfulOutcome(cl, response)) {
                // List<ModelNode>
                List destinations = modelNodeAsList(cl, readResult(cl, response));
                for (Object destination : destinations) {
                    Object value = modelNodeAsPropertyForValue(cl, destination);
                    WildflyMessageDestination wildflyDestination = new WildflyMessageDestination(modelNodeAsPropertyForName(cl, destination), messageType);
                    if (modelNodeHasChild(cl, value, "entries")) {
                        List entries = modelNodeAsList(cl, getModelNodeChild(cl, modelNodeAsPropertyForValue(cl, destination), "entries"));
                        for (Object entry : entries) {
                            wildflyDestination.addEntry(modelNodeAsString(cl, entry));
                        }
                    }
                    listedDestinations.add(wildflyDestination);
                }
            }
            return listedDestinations;
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    private List<WildflyMessageDestination> getJMSDestinationForServer(String serverName, Type messageType) throws IOException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<WildflyMessageDestination> listedDestinations = new ArrayList<>();
            // ModelNode
            final Object readQueues = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readQueues, OP), READ_CHILDREN_RESOURCES_OPERATION);

            LinkedHashMap<Object, Object> values = new LinkedHashMap<>();
            values.put(SUBSYSTEM, getMessagingSubsystem());
            values.put(getMessagingServerType(), serverName);
            // ModelNode
            Object path = createPathAddressAsModelNode(cl, values);
            setModelNodeChild(cl, getModelNodeChild(cl, readQueues, ADDRESS), path);
            setModelNodeChild(cl, getModelNodeChild(cl, readQueues, RECURSIVE_DEPTH), 0);
            if (messageType == Type.QUEUE) {
                setModelNodeChildString(cl, getModelNodeChild(cl, readQueues, CHILD_TYPE), JMSQUEUE_TYPE);
            } else {
                setModelNodeChildString(cl, getModelNodeChild(cl, readQueues, CHILD_TYPE), JMSTOPIC_TYPE);
            }

            // ModelNode
            Object response = executeOnModelNode(cl, readQueues);
            if (isSuccessfulOutcome(cl, response)) {
                // List<ModelNode>
                List destinations = modelNodeAsList(cl, readResult(cl, response));
                for (Object destination : destinations) {
                    Object value = modelNodeAsPropertyForValue(cl, destination);
                    WildflyMessageDestination wildflyDestination = new WildflyMessageDestination(modelNodeAsPropertyForName(cl, destination), messageType);
                    if (modelNodeHasChild(cl, value, "entries")) {
                        List entries = modelNodeAsList(cl, getModelNodeChild(cl, modelNodeAsPropertyForValue(cl, destination), "entries"));
                        for (Object entry : entries) {
                            wildflyDestination.addEntry(modelNodeAsString(cl, entry));
                        }
                    }
                    listedDestinations.add(wildflyDestination);
                }
            }
            return listedDestinations;
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    public boolean addMessageDestinations(final Collection<WildflyMessageDestination> destinations, InstanceProperties ip) throws IOException {
        boolean result = isServerRunning(ip.getProperty(WildflyPluginProperties.PROPERTY_ROOT_DIR),
                ip.getProperty(WildflyPluginProperties.PROPERTY_CONFIG_FILE));
        if (result) {
            for (WildflyMessageDestination destination : destinations) {
                result = result && addMessageDestination(destination);
            }
        }
        return result;
    }

    public boolean addMessageDestination(WildflyMessageDestination destination) throws IOException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            LinkedHashMap<Object, Object> values = new LinkedHashMap<>();
            values.put(SUBSYSTEM, getMessagingSubsystem());
            values.put(getMessagingServerType(), "default");
            if (destination.getType() == Type.QUEUE) {
                values.put("jms-queue", destination.getName());
            } else {
                values.put("jms-topic", destination.getName());
            }
            Object address = createPathAddressAsModelNode(cl, values);
            Object operation = createAddOperation(cl, address);
            if (destination.getJndiNames().isEmpty()) {
                destination.addEntry(destination.getName());
            }
            for (String jndiName : destination.getJndiNames()) {
                addModelNodeChildString(cl, getModelNodeChild(cl, operation, "entries"), jndiName);
            }
            Object response = executeOnModelNode(cl, operation);
            return (isSuccessfulOutcome(cl, response));
        } catch (ReflectiveOperationException ex) {
            return false;
        }
    }

    public boolean removeMessageDestination(WildflyMessageDestination destination) throws IOException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            LinkedHashMap<Object, Object> values = new LinkedHashMap<>();
            values.put(SUBSYSTEM, getMessagingSubsystem());
            values.put(getMessagingServerType(), "default");
            if (destination.getType() == Type.QUEUE) {
                values.put("jms-queue", destination.getName());
            } else {
                values.put("jms-topic", destination.getName());
            }
            Object address = createPathAddressAsModelNode(cl, values);
            Object operation = createRemoveOperation(cl, address);
            Object response = executeOnModelNode(cl, operation);
            return (isSuccessfulOutcome(cl, response));
        } catch (ReflectiveOperationException ex) {
            return false;
        }
    }

    public Collection<WildflyJaxrsResourceNode> listJaxrsResources(Lookup lookup, String deployment) throws IOException {
        Collection<WildflyJaxrsResource> jaxrsResources = listJaxrsResources(deployment);
        List<WildflyJaxrsResourceNode> modules = new ArrayList<>(jaxrsResources.size());
        for (WildflyJaxrsResource jaxrsResource : jaxrsResources) {
            modules.add(new WildflyJaxrsResourceNode(jaxrsResource, lookup));
        }
        return modules;
    }

    private Collection<WildflyJaxrsResource> listJaxrsResources(String deployment) throws IOException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            Map<String, WildflyJaxrsResource> jaxrsResources = new HashMap<>();
            // ModelNode
            final Object readJaxrsResources = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readJaxrsResources, OP), SHOW_RESOURCES);
            LinkedHashMap<Object, Object> address = new LinkedHashMap<>();
            address.put(DEPLOYMENT, deployment);
            address.put(SUBSYSTEM, JAXRS_SUBSYSTEM);
            // ModelNode
            Object path = createPathAddressAsModelNode(cl, address);
            setModelNodeChild(cl, getModelNodeChild(cl, readJaxrsResources, ADDRESS), path);
            // ModelNode
            Object response = executeOnModelNode(cl, readJaxrsResources);
            if (isSuccessfulOutcome(cl, response)) {
                String serverUrl = "http://" + serverAddress + ':' + getHttpPort();
                // List<ModelNode>
                Object result = readResult(cl, response);
                if (modelNodeIsDefined(cl, result)) {
                    List names = modelNodeAsList(cl, result);
                    for (Object jaxrsResource : names) {
                        String resourceClass = modelNodeAsString(cl, getModelNodeChild(cl, jaxrsResource, Constants.JAXRS_RESOURCE_CLASSNAME));
                        String resourcePath = modelNodeAsString(cl, getModelNodeChild(cl, jaxrsResource, Constants.JAXRS_RESOURCE_PATH));
                        List methods = modelNodeAsList(cl, getModelNodeChild(cl, jaxrsResource, Constants.JAXRS_RESOURCE_METHODS));
                        String key = resourceClass + "___" + resourcePath;
                        if (jaxrsResources.containsKey(key)) {
                            jaxrsResources.get(key).addMethods(methods);
                        } else {
                            jaxrsResources.put(key, new WildflyJaxrsResource(resourceClass, resourcePath, serverUrl, methods));
                        }
                    }
                }
            }
            return jaxrsResources.values();
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    public Collection<WildflyMailSessionResource> listMailSessions() throws IOException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<WildflyMailSessionResource> modules = new ArrayList<>();
            LinkedHashMap<Object, Object> values = new LinkedHashMap<>();
            values.put(SUBSYSTEM, MAIL_SUBSYSTEM);
            Object address = createPathAddressAsModelNode(cl, values);
            final Object readMailSessions = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readMailSessions, OP),
                    READ_CHILDREN_RESOURCES_OPERATION);
            setModelNodeChild(cl, getModelNodeChild(cl, readMailSessions, ADDRESS), address);
            setModelNodeChildString(cl, getModelNodeChild(cl, readMailSessions, CHILD_TYPE), MAIL_SESSION_TYPE);
            setModelNodeChild(cl, getModelNodeChild(cl, readMailSessions, RECURSIVE_DEPTH), 0);
            setModelNodeChildString(cl, getModelNodeChild(cl, readMailSessions, INCLUDE_RUNTIME), "true");
            setModelNodeChildString(cl, getModelNodeChild(cl, readMailSessions, RECURSIVE), "true");
            Object response = executeOnModelNode(cl, readMailSessions);
            if (isSuccessfulOutcome(cl, response)) {
                Object result = readResult(cl, response);
                List mailSessions = modelNodeAsList(cl, result);
                for (Object mailSession : mailSessions) {
                    String sessionName = modelNodeAsPropertyForName(cl, mailSession);
                    modules.add(fillMailSession(sessionName, mailSession));
                }
            }
            return modules;
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    public Collection listEarApplications(Lookup lookup) throws IOException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<WildflyEarApplicationNode> modules = new ArrayList<>();
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, null);
            Object readDeployments = createReadResourceOperation(cl, deploymentAddressModelNode, true, true);
            Object response = executeOnModelNode(cl, readDeployments);
            if (isSuccessfulOutcome(cl, response)) {
                Object result = readResult(cl, response);
                List applications = modelNodeAsList(cl, result);
                for (Object application : applications) {
                    String applicationName = modelNodeAsString(cl, getModelNodeChild(cl, readResult(cl, application), NAME));
                    if (applicationName.endsWith(".ear")) {
                        modules.add(new WildflyEarApplicationNode(applicationName, lookup));
                    }
                }
            }
            return modules;
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    public Collection listEarSubModules(Lookup lookup, String jeeApplicationName) throws IOException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List modules = new ArrayList();
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, jeeApplicationName);
            Object readDeployments = createReadResourceOperation(cl, deploymentAddressModelNode, true, true);
            Object response = executeOnModelNode(cl, readDeployments);
            if (isSuccessfulOutcome(cl, response)) {
                int httpPort = getHttpPort();
                Object result = readResult(cl, response);
                List subDeployments = modelNodeAsList(cl, getModelNodeChild(cl, result, "subdeployment"));
                for (Object subDeployment : subDeployments) {
                    String applicationName = modelNodeAsPropertyForName(cl, subDeployment);
                    if (applicationName.endsWith(".war")) {
                        // ModelNode
                        Object deployment = getModelNodeChild(cl, getModelNodeChild(cl, modelNodeAsPropertyForValue(cl, subDeployment), SUBSYSTEM), WEB_SUBSYSTEM);
                        if (modelNodeIsDefined(cl, deployment)) {
                            String url = "http://" + serverAddress + ':' + httpPort + modelNodeAsString(cl, getModelNodeChild(cl, deployment, "context-root"));
                            modules.add(new WildflyWebModuleNode(applicationName, lookup, url));
                        } else {
                            modules.add(new WildflyWebModuleNode(applicationName, lookup, null));
                        }
                    } else if (applicationName.endsWith(".jar")) {
                        // ModelNode
                        Object deployment = getModelNodeChild(cl, getModelNodeChild(cl, modelNodeAsPropertyForValue(cl, subDeployment), SUBSYSTEM), EJB3_SUBSYSTEM);
                        if (modelNodeIsDefined(cl, deployment)) {
                            List<WildflyEjbComponentNode> ejbs = new ArrayList<>();
                            ejbs.addAll(listEJBs(cl, deployment, WildflyEjbComponentNode.Type.ENTITY));
                            ejbs.addAll(listEJBs(cl, deployment, WildflyEjbComponentNode.Type.MDB));
                            ejbs.addAll(listEJBs(cl, deployment, WildflyEjbComponentNode.Type.SINGLETON));
                            ejbs.addAll(listEJBs(cl, deployment, WildflyEjbComponentNode.Type.STATEFULL));
                            ejbs.addAll(listEJBs(cl, deployment, WildflyEjbComponentNode.Type.STATELESS));
                            modules.add(new WildflyEjbModuleNode(applicationName, lookup, ejbs, true));
                        }
                    }
                }
            }
            return modules;
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    public Collection listEJBForDeployment(Lookup lookup, String applicationName) throws IOException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List modules = new ArrayList();
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, applicationName);
            Object readDeployments = createReadResourceOperation(cl, deploymentAddressModelNode, true, true);
            Object response = executeOnModelNode(cl, readDeployments);
            if (isSuccessfulOutcome(cl, response)) {
                Object result = readResult(cl, response);
                Object deployment = getModelNodeChild(cl, getModelNodeChild(cl, result, SUBSYSTEM), EJB3_SUBSYSTEM);
                if (modelNodeIsDefined(cl, deployment)) {
                    List<WildflyEjbComponentNode> ejbs = new ArrayList<>();
                    ejbs.addAll(listEJBs(cl, deployment, WildflyEjbComponentNode.Type.ENTITY));
                    ejbs.addAll(listEJBs(cl, deployment, WildflyEjbComponentNode.Type.MDB));
                    ejbs.addAll(listEJBs(cl, deployment, WildflyEjbComponentNode.Type.SINGLETON));
                    ejbs.addAll(listEJBs(cl, deployment, WildflyEjbComponentNode.Type.STATEFULL));
                    ejbs.addAll(listEJBs(cl, deployment, WildflyEjbComponentNode.Type.STATELESS));
                    modules.add(new WildflyEjbModuleNode(applicationName, lookup, ejbs, true));
                }
            }

            return modules;
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    private List<WildflyEjbComponentNode> listEJBs(WildflyClassLoader cl,
            Object deployment, WildflyEjbComponentNode.Type type) throws ReflectiveOperationException {
        List<WildflyEjbComponentNode> modules = new ArrayList<>();
        if (modelNodeHasDefinedChild(cl, deployment, type.getPropertyName())) {
            List ejbs = modelNodeAsList(cl, getModelNodeChild(cl, deployment, type.getPropertyName()));
            for (Object ejb : ejbs) {
                modules.add(new WildflyEjbComponentNode(modelNodeAsPropertyForName(cl, ejb), type));
            }
        }
        return modules;
    }

    private WildflySocket fillSocket(String name, boolean outBound) throws ReflectiveOperationException, IOException {
        WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
        WildflySocket socket = new WildflySocket();
        LinkedHashMap<Object, Object> values = new LinkedHashMap<>();
        values.put("socket-binding-group", "standard-sockets");
        if (outBound) {
            values.put("remote-destination-outbound-socket-binding", name);
        } else {
            values.put("socket-binding", name);
        }
        Object address = createPathAddressAsModelNode(cl, values);
        final Object readSocket = createModelNode(cl);
        setModelNodeChildString(cl, getModelNodeChild(cl, readSocket, OP), READ_RESOURCE_OPERATION);
        setModelNodeChild(cl, getModelNodeChild(cl, readSocket, ADDRESS), address);
        setModelNodeChild(cl, getModelNodeChild(cl, readSocket, RECURSIVE_DEPTH), 0);
        setModelNodeChildString(cl, getModelNodeChild(cl, readSocket, INCLUDE_RUNTIME), "true");
        setModelNodeChildString(cl, getModelNodeChild(cl, readSocket, RECURSIVE), "true");
        Object response = executeOnModelNode(cl, readSocket);
        if (isSuccessfulOutcome(cl, response)) {
            Object binding = readResult(cl, response);
            if (modelNodeHasDefinedChild(cl, binding, "fixed-source-port")) {
                socket.setFixedSourcePort(modelNodeAsBoolean(cl, getModelNodeChild(cl, binding, "fixed-source-port")));
            }
            if (modelNodeHasDefinedChild(cl, binding, "host")) {
                socket.setHost(modelNodeAsString(cl, getModelNodeChild(cl, binding, "host")));
            }
            if (modelNodeHasDefinedChild(cl, binding, "port")) {
                socket.setPort(modelNodeAsInt(cl, getModelNodeChild(cl, binding, "port")));
            }
        }
        return socket;
    }

    public Collection<WildflyConnectionFactory> listConnectionFactories() throws IOException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<WildflyConnectionFactory> connectionFactories = new ArrayList<>();
            // ModelNode
            final Object readMessagingServers = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readMessagingServers, OP), READ_CHILDREN_NAMES_OPERATION);

            LinkedHashMap<Object, Object> values = new LinkedHashMap<>();
            values.put(SUBSYSTEM, getMessagingSubsystem());
            // ModelNode
            Object path = createPathAddressAsModelNode(cl, values);
            setModelNodeChild(cl, getModelNodeChild(cl, readMessagingServers, ADDRESS), path);
            setModelNodeChild(cl, getModelNodeChild(cl, readMessagingServers, RECURSIVE_DEPTH), 0);
            setModelNodeChildString(cl, getModelNodeChild(cl, readMessagingServers, CHILD_TYPE), getMessagingServerType());

            // ModelNode
            Object response = executeOnModelNode(cl, readMessagingServers);
            if (isSuccessfulOutcome(cl, response)) {
                // List<ModelNode>
                List names = modelNodeAsList(cl, readResult(cl, response));
                for (Object messagingServer : names) {
                    String messagingServerName = modelNodeAsString(cl, messagingServer);
                    connectionFactories.addAll(getConnectionFactoriesForServer(messagingServerName));
                }
            }
            return connectionFactories;
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    private Collection<? extends WildflyConnectionFactory> getConnectionFactoriesForServer(String messagingServerName) throws IOException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<WildflyConnectionFactory> listedConnectionFactories = new ArrayList<>();
            // ModelNode
            final Object readConnectionFactories = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readConnectionFactories, OP),
                    READ_CHILDREN_RESOURCES_OPERATION);

            LinkedHashMap<Object, Object> values = new LinkedHashMap<>();
            values.put(SUBSYSTEM, getMessagingSubsystem());
            values.put(getMessagingServerType(), messagingServerName);
            // ModelNode
            Object path = createPathAddressAsModelNode(cl, values);
            setModelNodeChild(cl, getModelNodeChild(cl, readConnectionFactories, ADDRESS), path);
            setModelNodeChild(cl, getModelNodeChild(cl, readConnectionFactories, RECURSIVE_DEPTH), 0);
            setModelNodeChildString(cl, getModelNodeChild(cl, readConnectionFactories, CHILD_TYPE), CONNECTION_FACTORY_TYPE);
            setModelNodeChildString(cl, getModelNodeChild(cl, readConnectionFactories, INCLUDE_RUNTIME), "true");

            // ModelNode
            Object response = executeOnModelNode(cl, readConnectionFactories);
            if (isSuccessfulOutcome(cl, response)) {
                // List<ModelNode>
                List connectionFactories = modelNodeAsPropertyList(cl, readResult(cl, response));
                for (Object connectionFactory : connectionFactories) {
                    listedConnectionFactories.add(fillConnectionFactory(
                            getPropertyName(cl, connectionFactory),
                            getPropertyValue(cl, connectionFactory)));

                }
            }
            return listedConnectionFactories;
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    private WildflyConnectionFactory fillConnectionFactory(String name, Object configuration) throws ReflectiveOperationException, IOException {
        WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
        List properties = modelNodeAsPropertyList(cl, configuration);
        Map<String, String> attributes = new HashMap<>(properties.size());
        for (Object property : properties) {
            String propertyName = getPropertyName(cl, property);
            Object propertyValue = getPropertyValue(cl, property);
            if (modelNodeIsDefined(cl, propertyValue)) {
                attributes.put(propertyName, modelNodeAsString(cl, propertyValue));
            }
        }
        return new WildflyConnectionFactory(attributes, name);
    }

    private WildflyMailSessionResource fillMailSession(String name, Object mailSession) throws ReflectiveOperationException, IOException {
        WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);

        Object configuration = modelNodeAsPropertyForValue(cl, mailSession);
        List properties = modelNodeAsPropertyList(cl, configuration);
        Map<String, String> attributes = new HashMap<>(properties.size());
        for (Object property : properties) {
            String propertyName = getPropertyName(cl, property);
            Object propertyValue = getPropertyValue(cl, property);
            if (!"debug".equals(propertyName) && !"jndi-name".equals(propertyName) && modelNodeIsDefined(cl, propertyValue)) {
                attributes.put(propertyName, modelNodeAsString(cl, propertyValue));
            }
        }
        WildflyMailSessionResource session = new WildflyMailSessionResource(attributes, name);
        List serverProperties = modelNodeAsList(cl, getModelNodeChild(cl, configuration, "server"));
        for (Object property : serverProperties) {
            if (modelNodeIsDefined(cl, property)) {
                Object settings = modelNodeAsPropertyForValue(cl, property);
                if (modelNodeHasDefinedChild(cl, settings, "username")) {
                    session.setUserName(modelNodeAsString(cl, getModelNodeChild(cl, settings, "username")));
                }
                if (modelNodeHasDefinedChild(cl, settings, "outbound-socket-binding-ref")) {
                    session.setSocket(fillSocket(modelNodeAsString(cl, getModelNodeChild(cl, settings, "outbound-socket-binding-ref")), true));
                }

            }
        }
        session.setIsDebug(modelNodeAsString(cl, getModelNodeChild(cl, configuration, "debug")));
        session.setJndiName(modelNodeAsString(cl, getModelNodeChild(cl, configuration, "jndi-name")));
        return session;
    }

    public String getDeploymentDirectory() throws IOException {
        WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
        LinkedHashMap<Object, Object> values = new LinkedHashMap<>();
        values.put(SUBSYSTEM, "deployment-scanner");
        values.put("scanner", "default");
        return resolvePath(cl, values);
    }

    private String resolveExpression(String unresolvedString) throws IOException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            final Object resolveExpression = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, resolveExpression, OP), RESOLVE_EXPRESSION);
            Object rootAddress = createPathAddressAsModelNode(cl, new LinkedHashMap<>());
            setModelNodeChild(cl, getModelNodeChild(cl, resolveExpression, ADDRESS), rootAddress);
            String testedExpression;
            if (unresolvedString.startsWith("${") && unresolvedString.endsWith("}")) {
                testedExpression = unresolvedString;
            } else {
                testedExpression = "${" + unresolvedString + "}";
            }
            setModelNodeChild(cl, getModelNodeChild(cl, resolveExpression, EXPRESSION), testedExpression);
            Object response = executeOnModelNode(cl, resolveExpression);
            if (isSuccessfulOutcome(cl, response)) {
                Object resolvedExpression = readResult(cl, response);
                return modelNodeAsString(cl, resolvedExpression);
            }
            return unresolvedString;
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    public Collection<WildflyResourceAdapter> listResourceAdapters() throws IOException {
        try {
            WildflyClassLoader cl = WildflyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<WildflyResourceAdapter> resourceAdapters = new ArrayList<>();
            // ModelNode
            final Object readResourceAdapters = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readResourceAdapters,
                    OP), READ_CHILDREN_RESOURCES_OPERATION);

            LinkedHashMap<Object, Object> values = new LinkedHashMap<>();
            values.put(SUBSYSTEM, RESOURCE_ADAPTER_SUBSYSTEM);
            // ModelNode
            Object path = createPathAddressAsModelNode(cl, values);
            setModelNodeChild(cl, getModelNodeChild(cl, readResourceAdapters, ADDRESS), path);
            setModelNodeChild(cl, getModelNodeChild(cl, readResourceAdapters, RECURSIVE_DEPTH), 0);
            setModelNodeChildString(cl, getModelNodeChild(cl, readResourceAdapters, INCLUDE_RUNTIME), "true");
            setModelNodeChildString(cl, getModelNodeChild(cl, readResourceAdapters, CHILD_TYPE), RESOURCE_ADAPTER_TYPE);

            // ModelNode
            Object response = executeOnModelNode(cl, readResourceAdapters);
            if (isSuccessfulOutcome(cl, response)) {
                // List<ModelNode>
                List ressources = modelNodeAsList(cl, readResult(cl, response));
                for (Object resource : ressources) {
                    Object configuration = modelNodeAsPropertyForValue(cl, resource);
                    List properties = modelNodeAsPropertyList(cl, configuration);
                    Map<String, String> attributes = new HashMap<>(properties.size());
                    for (Object property : properties) {
                        String propertyName = getPropertyName(cl, property);
                        Object propertyValue = getPropertyValue(cl, property);
                        if (modelNodeIsDefined(cl, propertyValue)) {
                            attributes.put(propertyName, modelNodeAsString(cl, propertyValue));
                        }
                    }
                    WildflyResourceAdapter resourceAdapter = new WildflyResourceAdapter(attributes, modelNodeAsPropertyForName(cl, resource));
                    resourceAdapters.add(resourceAdapter);
                }
            }
            return resourceAdapters;
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    private String getMessagingSubsystem() {
        if (version.compareTo(WILDFLY_10_0_0) >= 0) {
            return MESSAGING_ACTIVEMQ_SUBSYSTEM;
        }
        return MESSAGING_SUBSYSTEM;
    }

    private String getMessagingServerType() {
        if (version.compareTo(WILDFLY_10_0_0) >= 0) {
            return MESSAGING_ACTIVEMQ_SERVER_TYPE;
        }
        return HORNETQ_SERVER_TYPE;
    }

    private int getHttpPort() {
        String httpPort = ip.getProperty(WildflyPluginProperties.PROPERTY_PORT);
        String offSet = ip.getProperty(WildflyPluginProperties.PROPERTY_PORT_OFFSET);
        int port = Integer.parseInt(httpPort);
        if (offSet != null) {
            port = port + Integer.parseInt(offSet);
        }
        return port;
    }
}
