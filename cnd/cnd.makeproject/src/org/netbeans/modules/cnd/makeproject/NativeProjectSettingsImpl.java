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
package org.netbeans.modules.cnd.makeproject;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.project.NativeProjectSettings;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.util.Mutex;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 */
public final class NativeProjectSettingsImpl implements NativeProjectSettings {

    private final Project project;
    private static final String CodeAssistanceData = "code-assistance-data"; //NOI18N
    private static final String CodeModelEnabled = "code-model-enabled"; //NOI18N
    private final String namespace;
    private final boolean shared;

    // constructors
    public NativeProjectSettingsImpl(MakeProject prj, String namespace, boolean shared) {
        this.shared = shared;
        this.project = prj;
        this.namespace = namespace;
    }

    // options
    @Override
    public boolean isCodeAssistanceEnabled() {
        String value = doLoad(CodeModelEnabled);
        return str2bool(value);
    }

    @Override
    public void setCodeAssistanceEnabled(boolean enabled) {
        doSave(CodeModelEnabled, Boolean.toString(enabled));
    }

    // private methods
    private boolean str2bool(String value) {
        return (value == null) || (value.length() == 0) || Boolean.parseBoolean(value);
    }

    private Element getConfigurationFragment() {
        AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(project);
        Element data = aux.getConfigurationFragment(CodeAssistanceData, namespace, shared);
        if (data == null) {
            data = createDocument(namespace, shared ? "project" : "project-private").createElementNS(namespace, CodeAssistanceData); //NOI18N
        }
        if (data == null) {
            System.err.println("CodeAssistanceOptions: Failed to load and create configuration fragment (" +
                    CodeAssistanceData + " : " + namespace + ")"); //NOI18N
        }
        return data;
    }

    private Element getNode(Element configurationFragment, String name) {
        NodeList nodes = configurationFragment.getElementsByTagNameNS(namespace, name);
        Element node;
        if (nodes.getLength() == 0) {
            node = configurationFragment.getOwnerDocument().createElementNS(namespace, name);
            configurationFragment.appendChild(node);
        } else {
            node = (Element) nodes.item(0);
        }
        return node;
    }

    private String doLoad(final String name) {
        return ProjectManager.mutex().readAccess((Mutex.Action<String>) () -> {
            Element configurationFragment = getConfigurationFragment();
            if (configurationFragment == null) {
                return null;
            }
            return getNode(configurationFragment, name).getTextContent();
        });
    }

    private void doSave(final String name, final String value) {
        ProjectManager.mutex().writeAccess(() -> {
            Element configurationFragment = getConfigurationFragment();
            if (configurationFragment != null) {
                Element el = getNode(configurationFragment, name);
                el.setTextContent(value);
                AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(project);
                aux.putConfigurationFragment(configurationFragment, shared);
            }
        });
    }

    // utility
    private static Document createDocument(String ns, String root) throws DOMException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            return factory.newDocumentBuilder().getDOMImplementation().createDocument(ns, root, null);
        } catch (ParserConfigurationException ex) {
            throw (DOMException) new DOMException(DOMException.NOT_SUPPORTED_ERR, "Cannot create parser").initCause(ex); // NOI18N
        }
    }
}

