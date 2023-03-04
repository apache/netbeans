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
package org.netbeans.modules.html.custom.conf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * TODO: project's config should be merged with the default one in $userdir/conf.
 *
 * @author marek
 */
public class Configuration {

    private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());

    private static final String CONF_FILE_NAME = "customs.json"; //NOI18N

    private static final Map<Project, Configuration> MAP = new WeakHashMap<>();

    //json file keys
    public static final String DESCRIPTION = "description";
    public static final String CONTEXT = "context";
    public static final String DOC = "doc";
    public static final String DOC_URL = "doc_url";
    public static final String REQUIRED = "required";
    public static final String ELEMENTS = "elements";
    public static final String ATTRIBUTES = "attributes";
    public static final String TYPE = "type";

    private final Map<String, Tag> tags = new HashMap<>();
    private final Map<String, Attribute> attrs = new HashMap<>();

    public static final Configuration EMPTY = new Configuration();

    public static Configuration get(@NonNull FileObject file) {
        Project owner = FileOwnerQuery.getOwner(file);
        return owner == null ? EMPTY : get(owner);
    }

    @NonNull
    public static Configuration get(@NonNull Project project) {
        synchronized (MAP) {
            Configuration conf = MAP.get(project);
            if (conf == null) {
                conf = new Configuration(project);
                MAP.put(project, conf);
            }
            return conf;
        }
    }

    private FileObject configFile;
    private FileObject configFileDir;
    private JSONObject root;

    private Configuration() {
    }

    public Configuration(Project project) {
        //TODO fix the conf location in maven and other project types
        FileObject nbprojectDir = project.getProjectDirectory().getFileObject("nbproject"); //NOI18N
        configFileDir = nbprojectDir == null ? project.getProjectDirectory() : nbprojectDir;
        configFile = configFileDir.getFileObject(CONF_FILE_NAME);
        if (configFile != null) {
            configFile.addFileChangeListener(new ConfigFileChangeListener());
            try {
                reload();
            } catch (IOException ex) {
                handleIOEFromReload(ex);
            }
        }
    }

    private void initConfigFile() throws IOException {
        if (configFile == null) {
            configFile = configFileDir.createData(CONF_FILE_NAME); //create one if doesn't exist
            configFile.addFileChangeListener(new ConfigFileChangeListener());
        }
    }

    public FileObject getProjectsConfigurationFile() {
        return configFile;
    }

    private void handleIOEFromReload(IOException e) {
        Project project = FileOwnerQuery.getOwner(getProjectsConfigurationFile());
        String projectDisplayName = project != null ? ProjectUtils.getInformation(project).getDisplayName() : "???"; //NOI18N
        String msg = String.format("An error found in the configuration file %s in the project %s: %s", getProjectsConfigurationFile().getNameExt(), projectDisplayName, e.getMessage());

        NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notifyLater(d);
    }

    /**
     * Gets a collection of the tags registered to the root context.
     *
     * @return
     */
    public Collection<String> getTagsNames() {
        return tags.keySet();
    }

    public Collection<Tag> getTags() {
        return tags.values();
    }

    /**
     * Gets a collection of the attributes registered to the root context.
     *
     * @return
     */
    public Collection<String> getAttributesNames() {
        return attrs.keySet();
    }

    public Collection<Attribute> getAttributes() {
        return attrs.values();
    }

    public Tag getTag(String tagName) {
        return tags.get(tagName);
    }

    public Attribute getAttribute(String name) {
        return attrs.get(name);
    }

    public void add(Tag t) {
        tags.put(t.getName(), t);
    }

    public void remove(Tag t) {
        tags.remove(t.getName());
    }

    public void add(Attribute a) {
        attrs.put(a.getName(), a);
    }

    public void remove(Attribute a) {
        attrs.remove(a.getName());
    }

    private void reload() throws IOException {
        FileObject projectsConfigurationFile = getProjectsConfigurationFile();
        if (projectsConfigurationFile != null) {
            //if something goes wrong, the data will be empty until the problem is corrected
            tags.clear();
            attrs.clear();

            final Document document = getDocument(projectsConfigurationFile);
            final AtomicReference<String> docContentRef = new AtomicReference<>();
            final AtomicReference<BadLocationException> bleRef = new AtomicReference<>();
            assert document != null : "Problem with configuration file: " + projectsConfigurationFile.getPath(); //NOI18N
            document.render(new Runnable() {

                @Override
                public void run() {
                    try {
                        docContentRef.set(document.getText(0, document.getLength()));
                    } catch (BadLocationException ex) {
                        bleRef.set(ex);
                    }
                }

            });
            if (bleRef.get() != null) {
                throw new IOException(bleRef.get());
            }

            String content = docContentRef.get();
            root = (JSONObject) JSONValue.parse(content);
            if (root != null) {
                JSONObject elements = (JSONObject) root.get(ELEMENTS);
                if (elements != null) {
                    Collection<Tag> rootTags = loadTags(elements, null);
                    for (Tag rootTag : rootTags) {
                        tags.put(rootTag.getName(), rootTag);
                    }
                }
                JSONObject attributes = (JSONObject) root.get(ATTRIBUTES);
                if (attributes != null) {
                    Collection<Attribute> rootAttrs = loadAttributes(attributes, null);
                    for (Attribute a : rootAttrs) {
                        attrs.put(a.getName(), a);
                    }
                }
            }
        }
    }

    private Document getDocument(FileObject file) throws IOException {
        DataObject dobj = DataObject.find(file);
        EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
        if (ec != null) {
            return ec.openDocument();
        }
        return null;
    }

    public JSONObject store() throws IOException {
        initConfigFile();
        JSONObject node = new JSONObject();
        storeTags(node, tags.values());
        storeAttributes(node, attrs.values());

        //serialize the current model
        final String newContent = node.toJSONString();

        //and save it to the underlying document/file
        DataObject dobj = DataObject.find(getProjectsConfigurationFile());
        EditorCookie editorCookie = dobj.getLookup().lookup(EditorCookie.class);
        Document doc = editorCookie.openDocument();
        //depends on javascript2.editor
        if (doc instanceof BaseDocument) {
            final BaseDocument document = (BaseDocument) doc;
            final AtomicReference<BadLocationException> bleRef = new AtomicReference<>();

            final Reformat reformat = Reformat.get(document);
            Preferences preferences = CodeStylePreferences.get(document).getPreferences();

            preferences.put("wrapArrayInit", "WRAP_ALWAYS"); //NOI18N
            preferences.put("wrapArrayInitItems", "WRAP_ALWAYS"); //NOI18N
            preferences.put("wrapObjects", "WRAP_ALWAYS"); //NOI18N
            preferences.put("wrapProperties", "WRAP_ALWAYS"); //NOI18N

            //modify
            document.runAtomic(new Runnable() {

                @Override
                public void run() {
                    try {
                        //TODO apply just changes via diff!
                        document.remove(0, document.getLength());
                        document.insertString(0, newContent, null);
                    } catch (BadLocationException ex) {
                        bleRef.set(ex);
                    }
                }

            });
            if (bleRef.get() != null) {
                throw new IOException(bleRef.get());
            }

            //reformat
            try {
                reformat.lock();
                document.runAtomic(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            reformat.reformat(0, document.getLength());
                        } catch (BadLocationException ex) {
                            bleRef.set(ex);
                        }
                    }

                });
                if (bleRef.get() != null) {
                    throw new IOException(bleRef.get());
                }
            } finally {
                reformat.unlock();
            }
        } else {
            // FIXME atomic lock
            try {
                doc.remove(0, doc.getLength());
                doc.insertString(0, newContent, null);
            } catch (BadLocationException ex) {
                throw new IOException(ex);
            }
        }

        //save changes
        editorCookie.saveDocument();

        //TODO reindex all affected indexers!
        return node;
    }

    private void storeTags(JSONObject node, Collection<Tag> tags) {
        JSONObject el = new JSONObject();
        node.put(ELEMENTS, el);

        for (Tag t : tags) {
            storeTag(el, t);
        }
    }

    private void storeTag(JSONObject node, Tag t) {
        JSONObject ctn = new JSONObject();
        node.put(t.getName(), ctn);

        storeElement(ctn, t);

        Collection<Tag> children = t.getTags();
        if (!children.isEmpty()) {
            storeTags(ctn, children);
        }
        Collection<Attribute> attributes = t.getAttributes();
        if (!attributes.isEmpty()) {
            storeAttributes(ctn, attributes);
        }
    }

    private void storeAttributes(JSONObject node, Collection<Attribute> attributes) {
        JSONObject el = new JSONObject();
        node.put(ATTRIBUTES, el);

        for (Attribute t : attributes) {
            storeAttribute(el, t);
        }
    }

    private void storeAttribute(JSONObject node, Attribute t) {
        JSONObject ctn = new JSONObject();
        node.put(t.getName(), ctn);
        String type = t.getType();
        if (type != null) {
            ctn.put(TYPE, type);
        }
        storeElement(ctn, t);
    }

    private void storeElement(JSONObject ctn, Element t) {
        if (t.getDescription() != null) {
            ctn.put(DESCRIPTION, t.getDescription());
        }
        if (t.getDocumentation() != null) {
            ctn.put(DOC, t.getDocumentation());
        }
        if (t.getDocumentationURL() != null) {
            ctn.put(DOC_URL, t.getDocumentationURL());
        }
        if (t.isRequired()) {
            ctn.put(REQUIRED, Boolean.TRUE.toString());
        }

        //filter parent from contexts for storing
        Collection<String> contexts = t.getContexts();
        if (t.getParent() != null) {
            Collection<String> noParentInContexts = new ArrayList<>();
            for (String ctx : contexts) {
                if (!ctx.equals(t.getParent().getName())) {
                    noParentInContexts.add(ctx);
                }
            }
            contexts = noParentInContexts;
        }

        if (!contexts.isEmpty()) {
            if (contexts.size() == 1) {
                //as string
                ctn.put(CONTEXT, contexts.iterator().next());
            } else {
                //as array
                ctn.put(CONTEXT, contexts);
            }
        }
    }

    private List<Tag> loadTags(JSONObject node, Tag parent) {
        List<Tag> innerTags = new ArrayList<>();
        for (Object key : node.keySet()) {
            String name = (String) key;
            JSONObject val = (JSONObject) node.get(name);

            LOGGER.log(Level.FINE, "element {0}: {1}", new Object[]{name, val});

            ArrayList<String> contexts = new ArrayList<>();
            Object ctx = val.get(CONTEXT);
            if (ctx instanceof JSONObject) {
                //????
            } else if (ctx instanceof String) {
                contexts.add((String) ctx);
            } else if (ctx instanceof JSONArray) {
                //list of string values
                contexts = (JSONArray) ctx;
            }

            String description = null;
            Object jsonDescription = val.get(DESCRIPTION);
            if (jsonDescription != null) {
                if (jsonDescription instanceof String) {
                    description = (String) jsonDescription;
                } else {
                    LOGGER.log(Level.WARNING, "The ''{0}'' key needs to have a string value!", DESCRIPTION);
                }
            }

            String doc = null;
            Object jsonDoc = val.get(DOC);
            if (jsonDoc != null) {
                if (jsonDoc instanceof String) {
                    doc = (String) jsonDoc;
                } else {
                    LOGGER.log(Level.WARNING, "The ''{0}'' key needs to have a string value!", DOC);
                }
            }

            String docURL = null;
            Object jsonDocURL = val.get(DOC_URL);
            if (jsonDocURL != null) {
                if (jsonDocURL instanceof String) {
                    docURL = (String) jsonDocURL;
                } else {
                    LOGGER.log(Level.WARNING, "The ''{0}'' key needs to have a string value!", DOC_URL);
                }
            }

            boolean required = false;
            Object jsonRequired = val.get(REQUIRED);
            if (jsonRequired != null) {
                if (jsonRequired instanceof String) {
                    required = Boolean.parseBoolean((String) jsonRequired);
                } else {
                    LOGGER.log(Level.WARNING, "The ''{0}'' key needs to have a string value!", REQUIRED);
                }
            }

            Tag tag = new Tag(name, description, doc, docURL, parent, required, contexts.toArray(new String[0]));

            //process nested elements
            Object jsonElements = val.get(ELEMENTS);
            if (jsonElements != null) {
                if (jsonElements instanceof JSONObject) {
                    JSONObject els = (JSONObject) jsonElements;
                    Collection<Tag> elements = loadTags(els, tag);
                    tag.setChildren(elements);
                } else {
                    LOGGER.log(Level.WARNING, "The ''{0}'' key needs to have a map value!", ELEMENTS);
                }
            }

            //process nested attributes
            JSONObject jsonAttributes = (JSONObject) val.get(ATTRIBUTES);

            Collection<Attribute> attributes = jsonAttributes != null
                    ? loadAttributes(jsonAttributes, tag)
                    : Collections.<Attribute>emptyList();

            tag.setAttributes(attributes);

            innerTags.add(tag);

        }
        return innerTags;
    }

    private List<Attribute> loadAttributes(JSONObject node, Tag tag) {
        List<Attribute> attributes = new ArrayList<>();
        for (Object key : node.keySet()) {
            String name = (String) key;
            Object value = node.get(key);
            LOGGER.log(Level.FINE, "attribute {0}: {1}", new Object[]{key, value});

            if (value instanceof String) {
                //the string value specifies just the type - boolean, string etc.
                String type = (String) value;
                Attribute a = new Attribute(name, type, null, null, null, tag, false);
                attributes.add(a);

            } else if (value instanceof JSONObject) {
                //map
                JSONObject val = (JSONObject) value;

                ArrayList<String> contexts = new ArrayList<>();
                Object ctx = val.get(CONTEXT);
                if (ctx instanceof JSONObject) {
                    //????
                } else if (ctx instanceof String) {
                    contexts.add((String) ctx);
                } else if (ctx instanceof JSONArray) {
                    //list of string values
                    contexts = (JSONArray) ctx;
                }

                String type = null;
                Object jsonType = val.get(TYPE);
                if (jsonType != null) {
                    if (jsonType instanceof String) {
                        type = (String) jsonType;
                    } else {
                        LOGGER.log(Level.WARNING, "The ''{0}'' key needs to have string value!", TYPE);
                    }
                }

                String description = null;
                Object jsonDescription = val.get(DESCRIPTION);
                if (jsonDescription != null) {
                    if (jsonDescription instanceof String) {
                        description = (String) jsonDescription;
                    } else {
                        LOGGER.log(Level.WARNING, "The ''{0}'' key needs to have string value!", DESCRIPTION);
                    }
                }

                String doc = null;
                Object jsonDoc = val.get(DOC);
                if (jsonDoc != null) {
                    if (jsonDoc instanceof String) {
                        doc = (String) jsonDoc;
                    } else {
                        LOGGER.log(Level.WARNING, "The ''{0}'' key needs to have string value!", DOC);
                    }
                }

                String docURL = null;
                Object jsonDocURL = val.get(DOC_URL);
                if (jsonDocURL != null) {
                    if (jsonDocURL instanceof String) {
                        docURL = (String) jsonDocURL;
                    } else {
                        LOGGER.log(Level.WARNING, "The ''{0}'' key needs to have string value!", DOC_URL);
                    }
                }

                boolean required = false;
                Object jsonRequired = val.get(REQUIRED);
                if (jsonRequired != null) {
                    if (jsonRequired instanceof String) {
                        required = Boolean.parseBoolean((String) jsonRequired);
                    } else {
                        LOGGER.log(Level.WARNING, "The ''{0}'' key needs to have a string value!", REQUIRED);
                    }
                }

                Attribute a = new Attribute(name, type, description, doc, docURL, tag, required, contexts.toArray(new String[0]));
                attributes.add(a);
            }

        }
        return attributes;
    }

    private final class ConfigFileChangeListener extends FileChangeAdapter {

        @Override
        public void fileChanged(FileEvent fe) {
            LOGGER.log(Level.INFO, "Config file {0} changed - reloading configuration.", configFile.getPath()); //NOI18N
            try {
                reload();
            } catch (IOException ex) {
                handleIOEFromReload(ex);
            }
        }

    }

}
