/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.gradle.actions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import static org.netbeans.modules.gradle.actions.CustomActionRegistrationSupport.ACTION_PROP_PREFIX;
import org.netbeans.modules.gradle.api.execute.ActionMapping;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.openide.filesystems.FileObject;
import org.openide.util.EditableProperties;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Serialization and deserialization helpers to persist user mapping customizations for
 * individual configurations.
 * 
 * @author sdedic
 */
public class ActionPersistenceUtils {
    static final String ELEMENT_ACTIONS = "actions"; // NOI18N
    static final String ELEMENT_ACTION = "action"; // NOI18N
    static final String ELEMENT_ARGS = "args"; // NOI18N
    static final String ELEMENT_RELOAD = "reload"; // NOI18N
    
    static final String ATTRIBUTE_NAME = "name"; // NOI18N
    static final String ATTRIBUTE_REPEATABLE = "repeatable"; // NOI18N
    static final String ATTRIBUTE_RULE = "rule"; // NOI18N
    static final String ATTRIBUTE_DISPLAY_NAME = "displayName"; // NOI18N
    
    static final String SYSTEM_ID = "action-mapping.dtd"; // NOI18N
    
    /**
     * Prefix for the configuration-specific action mappings in the project directory. The 
     * entire filename is formed as {@link #NBACTIONS_CONFIG_PREFIX} + [configurationId] + {@link #NBACTIONS_XML_EXT}.
     */
    static final String NBACTIONS_CONFIG_PREFIX = "nb-actions-"; // NOI18N
    
    /**
     * Extension for the configuration-specific mappings.
     */
    static final String NBACTIONS_XML_EXT = ".xml"; // NOI18N
    
    /**
     * Derives the action config filename from config ID.
     * @param configId configuration ID
     * @return filename
     */
    public static String getActionsFileName(String configId) {
        if (configId == null || GradleExecConfiguration.DEFAULT.equals(configId)) {
            return GradleFiles.GRADLE_PROPERTIES_NAME;
        } else {
            return NBACTIONS_CONFIG_PREFIX + configId + NBACTIONS_XML_EXT;
        }
    }

    /**
     * Returns FileObject (if exists) for configuration's customizations.
     * @param projectDirectory project directory
     * @param configId configuration ID
     * @return file instance, if it exists.
     */
    public static FileObject findActionsFile(FileObject projectDirectory, String configId) {
        return projectDirectory.getFileObject(getActionsFileName(configId));
    }
    
    /**
     * Saves actions for the default configuration into gradle.properties.
     * @param projectDirectory project directory
     * @param actions actions to save.
     * @throws IOException on I/O error
     */
    public static void saveDefaultActions(FileObject projectDirectory, List<ActionMapping> actions) throws IOException {
        EditableProperties props = new EditableProperties(false);
        FileObject fo = findActionsFile(projectDirectory, null);
        if (fo != null) {
            try (InputStream is = fo.getInputStream()) {
                props.load(is);
            }
        }
        // Remove previously defined acltion, if any
        Iterator<String> it = props.keySet().iterator();
        while (it.hasNext()) {
            if (it.next().startsWith(ACTION_PROP_PREFIX)) {
                it.remove();
            }
        }
        // Add new actions, if any
        for (ActionMapping mapping : actions) {
            String prefix = ACTION_PROP_PREFIX + mapping.getName() + '.';
            if (mapping.getName().startsWith(ActionMapping.CUSTOM_PREFIX)) {
                props.setProperty(ACTION_PROP_PREFIX + mapping.getName(), mapping.getDisplayName());
            }
            if (!mapping.getArgs().isEmpty()) {
                props.setProperty(prefix + "args", mapping.getArgs()); //NOI18N
            }
            if (!mapping.getReloadArgs().isEmpty()) {
                props.setProperty(prefix + "reload.args", mapping.getReloadArgs()); //NOI18N
            }
            if (mapping.getReloadRule() != ActionMapping.ReloadRule.DEFAULT) {
                props.setProperty(prefix + "reload.rule", mapping.getReloadRule().name()); //NOI18N
            }
            if (!mapping.isRepeatable()) {
                props.setProperty(prefix + "repeatable", "false"); //NOI18N
            }
        }

        if (props.isEmpty()) {
            if (fo != null) {
                fo.delete();
            }
        }
        try (OutputStream os = (fo != null ? fo.getOutputStream() : projectDirectory.createAndOpen(getActionsFileName(null)))) {
            props.store(os);
        }
    }
    
    /**
     * Writes actions for the specific configuration. Handles default/nondefault dichotomy (gradle.properties / XMLs).
     * @param projectDirectory
     * @param configId
     * @param mappings
     * @throws IOException 
     */
    public static void writeActions(FileObject projectDirectory, String configId, List<ActionMapping> mappings) throws IOException {
        if (GradleExecConfiguration.DEFAULT.equals(configId)) {
            saveDefaultActions(projectDirectory, mappings);
            return;
        }
        FileObject fo = findActionsFile(projectDirectory, configId);
        if (mappings.isEmpty()) {
            if (fo != null && fo.isValid()) {
                fo.delete();
            }
            return;
        }
        
        Document doc = XMLUtil.createDocument(ELEMENT_ACTIONS, null, null, SYSTEM_ID);
        for (ActionMapping a : mappings) {
            Element e = doc.createElement(ELEMENT_ACTION);
            e.setAttribute(ATTRIBUTE_NAME, a.getName());
            if (a.getDisplayName() != null && !a.getDisplayName().equals(a.getName())) {
                e.setAttribute(ATTRIBUTE_DISPLAY_NAME, a.getDisplayName());
            }
            if (a.isRepeatable()) {
                e.setAttribute(ATTRIBUTE_REPEATABLE, Boolean.valueOf(a.isRepeatable()).toString());
            }
            if (a.getArgs() != null && !a.getArgs().trim().isEmpty()) {
                Element args = doc.createElement(ELEMENT_ARGS);
                args.setTextContent(a.getArgs().trim());
                e.appendChild(args);
            }
            Element r = doc.createElement(ELEMENT_RELOAD);
            boolean appendReload = false;
            if (a.getReloadRule() != null && a.getReloadRule() != ActionMapping.ReloadRule.DEFAULT) {
                r.setAttribute(ATTRIBUTE_RULE, a.getReloadRule().name());
                appendReload = true;
            }
            if (a.getReloadArgs() != null && !a.getReloadArgs().trim().isEmpty()) {
                Element args = doc.createElement(ELEMENT_ARGS);
                args.setTextContent(a.getArgs().trim());
                r.appendChild(args);
                appendReload = true;
            }
            if (appendReload) {
                e.appendChild(r);
            }
            
            doc.getDocumentElement().appendChild(e);
        }
        
        try (OutputStream os = (fo != null ? fo.getOutputStream() : projectDirectory.createAndOpen(getActionsFileName(configId)))) {
            XMLUtil.write(doc, os, "UTF-8");
        }
    }
}
