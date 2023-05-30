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

package org.netbeans.modules.java.lsp.server.explorer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.netbeans.modules.java.lsp.server.protocol.CodeActionsProvider;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Hurka
 */
@ServiceProvider(service = CodeActionsProvider.class)
public class NodePropertiesProvider extends CodeActionsProvider {

    private static final String COMMAND_GET_NODE_PROPERTIES = "java.node.properties.get";      // NOI18N
    private static final String COMMAND_SET_NODE_PROPERTIES = "java.node.properties.set";      // NOI18N

    private static final String PROP_NAME = "propName";      // NOI18N
    private static final String PROP_DNAME = "propDispName";      // NOI18N
    private static final String PROP_HTML_NAME = "propHtmlName";      // NOI18N
    private static final String PROP_SHORT_NAME = "propShortName";      // NOI18N
    private static final String PROP_PREF = "propPref";      // NOI18N
    private static final String PROP_EXPERT = "propExpert";      // NOI18N
    private static final String PROP_HIDDEN = "propHidden";      // NOI18N
    private static final String PROP_CAN_READ = "propRead";      // NOI18N
    private static final String PROP_CAN_WRITE = "propWrite";      // NOI18N
    private static final String PROP_VAL_TYPE = "propType";      // NOI18N
    private static final String PROP_VALUE = "propValue";      // NOI18N
    private static final String PROPS = "props";      // NOI18N

    private static final Set<String> COMMANDS = new HashSet<>(Arrays.asList(
            COMMAND_GET_NODE_PROPERTIES, COMMAND_SET_NODE_PROPERTIES
    ));

    private final Gson gson = new Gson();

    @Override
    public List<CodeAction> getCodeActions(ResultIterator resultIterator, CodeActionParams params) throws Exception {
        return Collections.emptyList();
    }

    @Override
    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
        if (!COMMANDS.contains(command)) {
            return CompletableFuture.completedFuture(null);
        }
        if (arguments == null || arguments.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        TreeNodeRegistry r = Lookup.getDefault().lookup(TreeNodeRegistry.class);
        if (r == null) {
            return CompletableFuture.completedFuture(null);
        }
        int nodeId = ((JsonPrimitive) arguments.get(0)).getAsInt();
        TreeViewProvider nodeProvider = r.providerOf(nodeId);
        Node node = null;
        if (nodeProvider != null) {
            node = nodeProvider.findNode(nodeId);
        }
        if (node == null) {
            return CompletableFuture.completedFuture(null);
        }
        boolean getProps = COMMAND_GET_NODE_PROPERTIES.equals(command);
        Node.PropertySet[] propertySets = node.getPropertySets();

        if (getProps) {
            return CompletableFuture.completedFuture(getAllPropertiesMap(propertySets));
        }
        if (arguments.size() == 2) {
            Object propJson = arguments.get(1);
            if (propJson instanceof JsonNull) {
                return CompletableFuture.completedFuture(null);
            }
            List m = gson.fromJson((JsonElement) propJson, List.class);
            setAllProperties(propertySets, m);
        }
        return CompletableFuture.completedFuture(null);
    }

    Map<String, ?>[] getAllPropertiesMap(Node.PropertySet[] propertySets) {
        Map<String, Object>[] allPropertiesMap = new Map[propertySets.length];

        for (int i = 0; i < propertySets.length; i++) {
            Map<String, Object> propertiesMap = new HashMap<>();
            Node.PropertySet ps = propertySets[i];
            propertiesMap.put(PROP_NAME, ps.getName());
            propertiesMap.put(PROP_DNAME, ps.getDisplayName());
            propertiesMap.put(PROP_HTML_NAME, ps.getHtmlDisplayName());
            propertiesMap.put(PROP_SHORT_NAME, ps.getShortDescription());
            propertiesMap.put(PROP_PREF, ps.isPreferred());
            propertiesMap.put(PROP_EXPERT, ps.isExpert());
            propertiesMap.put(PROP_HIDDEN, ps.isHidden());
            propertiesMap.put(PROPS, getProperties(ps.getProperties()));
            allPropertiesMap[i] = propertiesMap;
        }
        return allPropertiesMap;
    }

    private Map<String, Object>[] getProperties(Node.Property<?>[] properties) {
        Map<String, Object>[] props = new Map[properties.length];
        for (int i = 0; i < properties.length; i++) {
            Map<String, Object> propMap = new HashMap<>();
            Node.Property<?> property = properties[i];
            if (property.canRead()) {
                propMap.put(PROP_DNAME, property.getDisplayName());
                propMap.put(PROP_HTML_NAME, property.getHtmlDisplayName());
                propMap.put(PROP_SHORT_NAME, property.getShortDescription());
                propMap.put(PROP_NAME, property.getName());
                propMap.put(PROP_PREF, property.isPreferred());
                propMap.put(PROP_EXPERT, property.isExpert());
                propMap.put(PROP_HIDDEN, property.isHidden());
                propMap.put(PROP_CAN_WRITE, property.canWrite());
                propMap.put(PROP_VAL_TYPE, property.getValueType().getName());
                propMap.put(PROP_VALUE, getPropertyValue(property));
                props[i] = propMap;
            }
        }
        return props;
    }

    private Object getPropertyValue(Node.Property<?> prop) {
        try {
            return prop.getValue();
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private void setAllProperties(Node.PropertySet[] propertySets, List m) {
        assert m.size() <= propertySets.length;

        for (int i = 0; i < m.size(); i++) {
            Map pm = (Map) m.get(i);
            Node.PropertySet p = propertySets[i];
            setProperties(p.getProperties(), (List<Map<String, Object>>) pm.get(PROPS));
        }
    }

    private void setProperties(Node.Property[] properties, List<Map<String, Object>> props) {
        Map<String, Node.Property> names = new HashMap<>();

        for (Node.Property p : properties) {
            names.put(p.getName(), p);
        }
        for (Map pm : props) {
            Node.Property prop = names.get((String) pm.get(PROP_NAME));
            if (prop != null && prop.canWrite()) {
                try {
                    Object val = pm.get(PROP_VALUE);
                    Object oldVal = prop.getValue();

                    if (!Objects.equals(val, oldVal)) {
                        prop.setValue(val);
                    }
                } catch (IllegalAccessException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    @Override
    public Set<String> getCommands() {
        return COMMANDS;
    }

}
