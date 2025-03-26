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
package org.netbeans.modules.javascript2.nodejs.editor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.types.api.DeclarationScope;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;
import org.netbeans.modules.javascript2.nodejs.spi.NodeJsSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Petr Pisl
 */
@NbBundle.Messages({
    "doc.building=Loading NodeJS Documentation",
    "# {0} - the documentation URL",
    "doc.cannotGet=Cannot load NodeJS documentation from \"{0}\".",
    "doc.notFound=Documentation not found."
})
public class NodeJsDataProvider {

    private static final Logger LOG = Logger.getLogger(NodeJsDataProvider.class.getSimpleName());

    private static RequestProcessor RP = new RequestProcessor(NodeJsDataProvider.class);
    private boolean loadingStarted;
    private ProgressHandle progress;

    private static final String API_ALL_HTML_FILE = "all.html";
    private static final String CACHE_FOLDER_NAME = "nodejs-doc"; //NOI18N
    private static final String API_ALL_JSON_FILE = "all.json"; //NOI18N

    protected static final String BACKUP_API_FILE = new StringBuilder().append(CACHE_FOLDER_NAME).append("/latest/") //NOI18N
            .append(API_ALL_JSON_FILE).toString();
    private static final int URL_CONNECTION_TIMEOUT = 1000; //ms
    private static final int URL_READ_TIMEOUT = URL_CONNECTION_TIMEOUT * 3; //ms

    private static final String AP_STRING = "&#39;"; //NOI18N
    private static final String REQUIRE_STRING = "= require(" + AP_STRING;     //NOI18N

    private static final String JS_EXT = "js";  //NOI18N
    
    // name of the json fields in api file
    private static final String MODULES = "modules"; //NOI18N
    private static final String NAME = "name"; //NOI18N
    private static final String DESCRIPTION = "desc"; //NOI18N
    private static final String GLOBALS = "globals"; //NOI18N
    private static final String VARS = "vars";  //NOI18N
    private static final String PARAMS = "params";  //NOI18N
    private static final String METHODS = "methods";    //NOI18N
    private static final String PROPERTIES = "properties";  //NOI18N
    private static final String CLASSES = "classes";    //NOI18N
    private static final String EVENTS = "events";    //NOI18N

    // name of the json fields in package.json
    private static final String MODULE_VERSION = "version"; //NOI18N
    private static final String MODULE_DESCRIPTION = "description"; //NOI18N

    private static final WeakHashMap<Project, NodeJsDataProvider> cache = new WeakHashMap<>();
    private static NodeJsDataProvider noProjectInstance = null;

    private static String docApiFilePath = BACKUP_API_FILE;
    private FileObject docFolder;
    private String docUrl = "https://nodejs.org/api/"; //NOI18N  
    private boolean isSupportEnabled;

    private ProjectSupportChangeListener listener;
    
    /**
     * Caching the apifile from sources folder.
     */
    private File apiFile = null;
    
    private NodeJsDataProvider(Project project) {
        this.loadingStarted = false;
        this.isSupportEnabled = project == null;
        this.docFolder = null;
        if (project != null) {
            NodeJsSupport support = null;
            support = project.getLookup().lookup(NodeJsSupport.class);
            if (support != null) {
                listener = new ProjectSupportChangeListener(project);
                support.addChangeListener(WeakListeners.change(listener, support));
                this.isSupportEnabled = support.isSupportEnabled();
                this.docFolder = support.getDocumentationFolder();
                if (support.getDocumentationUrl() != null) {
                    this.docUrl = support.getDocumentationUrl();
                }
                if (support.getVersion() != null) {
                    docApiFilePath = new StringBuilder().append(CACHE_FOLDER_NAME).append(File.separator)
                            .append(support.getVersion().toString()).append(File.separator).append(API_ALL_JSON_FILE).toString();
                }
            }
        }
    }

    public static synchronized NodeJsDataProvider getDefault(FileObject fo) {
        assert fo != null;
        Project project = FileOwnerQuery.getOwner(fo);
        if (project == null) {
            if (noProjectInstance == null) {
                noProjectInstance = new NodeJsDataProvider(null);
            }
            return noProjectInstance;
        }
        NodeJsDataProvider instance = cache.get(project);
        if (instance == null) {
            instance = new NodeJsDataProvider(project);
            cache.put(project, instance);
        }
        
        return instance;
    }

    public boolean isSupportEnabled() {
        return isSupportEnabled;
    }
    
    /**
     *
     * @return URL or null if it's not available.
     */
    private URL getDocumentationURL() {
        URL result = null;
        try {
            result = new URL(docUrl);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result;
    }

    /**
     * 
     * @return folder with the sources of the runtime modules or null
     */
    public FileObject getFolderWithRuntimeSources () {
        if (docFolder != null) {
            return docFolder.getFileObject("../lib"); //NOI18N
        }
        return null;
    }
    
    /**
     * 
     * @return list of names of runtime modules. These names are obtained as names of
     * files from ${docfolder}/../lib or from the documentation if the doc folder doesn't
     * exist
     */
    public Collection<String> getRuntimeModules() {
        HashSet<String> modules = new HashSet<String>();
        if (docFolder != null) {
            FileObject libFolder = getFolderWithRuntimeSources();
            if (libFolder != null) {
                FileObject[] children = libFolder.getChildren();
                for (int i = 0; i < children.length; i++) {
                    FileObject module = children[i];
                    if (!module.isFolder() && JS_EXT.equals(module.getExt()) && module.getName().charAt(0) != '_' ) {
                        modules.add(module.getName());
                    }
                    
                }
                if (!modules.isEmpty()) {
                    return modules;
                }
            }
        }
        String content = getContentApiFile();
        if (content != null) {
            int index = 0;
            int lenghtOfRequire = REQUIRE_STRING.length();
            index = content.indexOf(REQUIRE_STRING, index);
            while (index != -1) {
                index += lenghtOfRequire;
                if (content.charAt(index) != '.') {
                    int end = content.indexOf(AP_STRING, index);
                    if (end > -1) {
                        String module = content.substring(index, end);
                        modules.add(module);
                    }
                }
                index = content.indexOf(REQUIRE_STRING, index);
            }
        }
        return modules;
    }

    /**
     * 
     * @return collection of local modules, that are obtained from the first node_modules folder
     */
    public Collection<FileObject> getLocalModules(FileObject forFile) {
        HashSet<FileObject> modules = new HashSet<FileObject>();
        Project project = FileOwnerQuery.getOwner(forFile);
        FileObject nodeModulesFolder = null;
        FileObject parent = forFile.getParent();
        if (project != null) {
            FileObject projectDirectory = project.getProjectDirectory();
            String pathToProject = projectDirectory.getPath();
            while (parent.getPath().startsWith(pathToProject) && nodeModulesFolder == null) {
                nodeModulesFolder = parent.getFileObject(NodeJsUtils.NODE_MODULES_NAME);
                parent = parent.getParent();
            }
        }
        if (nodeModulesFolder != null) {
            Enumeration<? extends FileObject> moduleFolders = nodeModulesFolder.getFolders(false);
            while (moduleFolders.hasMoreElements()) {
                FileObject moduleFolder = moduleFolders.nextElement();
                if (moduleFolder.getFileObject(NodeJsUtils.PACKAGE_NAME, NodeJsUtils.JSON_EXT) != null) {
                    modules.add(moduleFolder);
                }
            }
        }
        return modules;
    }
    
    public Map<String, Collection<String>> getAllEvents() {
        HashMap<String, Collection<String>> result = new HashMap<>();
        String content = getContentApiFile();
        if (content != null && !content.isEmpty()) {
            JSONObject root = (JSONObject) JSONValue.parse(content);
            JSONArray globals = getJSONArrayProperty(root, GLOBALS);
            if (globals != null) {
                for (Object jsonValue : globals) {
                    if (jsonValue instanceof JSONObject) {
                        getNameOfEventsRecursively((JSONObject)jsonValue, result);
                    }
                }
            }
            JSONArray modules = getJSONArrayProperty(root, MODULES);
            if (modules != null) {
                for (Object jsonValue : modules) {
                    if (jsonValue instanceof JSONObject) {
                        getNameOfEventsRecursively((JSONObject)jsonValue, result);
                    }
                }
            }
            JSONArray vars = getJSONArrayProperty(root, VARS);
            if (vars != null) {
                for (Object jsonValue : vars) {
                    if (jsonValue instanceof JSONObject) {
                        getNameOfEventsRecursively((JSONObject)jsonValue, result);
                    }
                }
            }
        }
        return result;
    }
    
    private void getNameOfEventsRecursively(JSONObject object, Map<String, Collection<String>> result) {
        JSONArray events = getJSONArrayProperty(object, EVENTS);
        if (events != null) {
            String objectName = getJSONStringProperty(object, NAME);
            StringBuilder docHeader = new StringBuilder();
            docHeader.append("<h2>").append(objectName).append("</h2>");    //NOI18N
            for (Object jsonValue : events) {
                if (jsonValue instanceof JSONObject) {
                    JSONObject event = (JSONObject) jsonValue;
                    String name = getJSONStringProperty(event, NAME);
                    Collection<String> documentations = result.get(name);
                    if (documentations == null) {
                        documentations = new ArrayList<String>();
                        result.put(name, documentations);
                    }
                    String documentation = getJSONStringProperty(event, DESCRIPTION);
                    if (documentation != null && !documentation.isEmpty()) {
                        documentations.add(docHeader.toString() + documentation);
                    }
                }
            }
        }
        JSONArray classes = getJSONArrayProperty(object, CLASSES);
        if (classes != null) {
            for (Object jsonValue : classes) {
                if (jsonValue instanceof JSONObject) {
                    getNameOfEventsRecursively((JSONObject)jsonValue, result);
                }
            }
        }
    }
    
    
    public String getDocForModule(final String moduleName) {
        Object jsonValue;
        JSONArray modules = getModules();
        if (modules != null) {
            for (int i = 0; i < modules.size(); i++) {
                jsonValue = modules.get(i);
                if (jsonValue instanceof JSONObject) {
                    JSONObject jsonModule = (JSONObject) jsonValue;
                    jsonValue = jsonModule.get(NAME);
                    if (jsonValue instanceof String && moduleName.equalsIgnoreCase(((String) jsonValue))) {
                        jsonValue = jsonModule.get(DESCRIPTION);
                        if (jsonValue instanceof String) {
                            return (String) jsonValue;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    @NbBundle.Messages({"NodeJsDataprovider.lbl.name=Name:", "NodeJsDataprovider.lbl.version=Version:"}) //NOI18N
    public String getDocForLocalModule(final FileObject moduleFolder) {
        FileObject packageFO = moduleFolder.getFileObject(NodeJsUtils.PACKAGE_NAME, NodeJsUtils.JSON_EXT);
        if (packageFO != null) {
            String content = null;
            try {
                content = getFileContent(FileUtil.toFile(packageFO));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (content != null && !content.isEmpty()) {
                JSONObject root = (JSONObject) JSONValue.parse(content);
                if (root != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(Bundle.NodeJsDataprovider_lbl_name()).append(" <b>").append(getJSONStringProperty(root, NAME)).append("</b><br/>");
                    sb.append(Bundle.NodeJsDataprovider_lbl_version()).append(" ").append(getJSONStringProperty(root, MODULE_VERSION)).append("<br/><br/>");
                    sb.append(getJSONStringProperty(root, MODULE_DESCRIPTION));
                    return sb.toString();
                }
            }
        }
        return null;
    }

    public Collection<JsObject> getGlobalObjects(ModelElementFactory factory) {
        String content = getContentApiFile();
        if (content != null && !content.isEmpty()) {
            File apiFile = getCachedAPIFile();
            JsObject globalObject = factory.newGlobalObject(FileUtil.toFileObject(apiFile), (int) apiFile.length());
            JSONObject root = (JSONObject) JSONValue.parse(content);
            if (root != null) {
                JSONArray globals = getJSONArrayProperty(root, GLOBALS);
                if (globals != null) {
                    for (Object jsonValue : globals) {
                        if (jsonValue instanceof JSONObject) {
                            JSONObject global = (JSONObject) jsonValue;
                            String name = getJSONStringProperty(global, NAME);
                            if (name != null) {
                                JsObject property = createProperty(factory, globalObject, global);
                                addProperties(factory, property, (DeclarationScope) globalObject, global);
                                addMethods(factory, property, (DeclarationScope) globalObject, global);
                            }
                        }
                    }
                }
                JSONArray vars = getJSONArrayProperty(root, VARS);
                if (vars != null) {
                    for (Object jsonValue : vars) {
                        if (jsonValue instanceof JSONObject) {
                            JSONObject var = (JSONObject) jsonValue;
                            String name = getJSONStringProperty(var, NAME);
                            if (name != null) {
//                                if (REQUIRE_STRING.equals(name)) {
//
//                                } else {
//                                    
//                                }
                                JsObject property = createProperty(factory, globalObject, var);
                                addProperties(factory, property, (DeclarationScope) globalObject, var);
                                addMethods(factory, property, (DeclarationScope) globalObject, var);
                            }
                        }
                    }
                }
                addMethods(factory, globalObject, (DeclarationScope) globalObject, root);
            }
            return Collections.singletonList(globalObject);
        }
        return Collections.emptyList();
    }

    private void addMethods(final ModelElementFactory factory, final JsObject toObject, final DeclarationScope scope, final JSONObject fromObject) {
        JSONArray methods = getJSONArrayProperty(fromObject, METHODS);
        if (methods != null) {
            for (Object methodO : methods) {
                if (methodO instanceof JSONObject) {
                    JSONObject method = (JSONObject) methodO;
                    String methodName = getJSONStringProperty(method, NAME);
                    JSONArray signatures = getJSONArrayProperty(method, "signatures");
                    String doc = getJSONStringProperty(method, DESCRIPTION);
                    if (methodName != null && signatures != null) {
                        for (Object signature : signatures) {
                            JSONArray params = getJSONArrayProperty((JSONObject) signature, PARAMS);
                            List<String> paramNames = new ArrayList<String>();
                            if (params != null && !params.isEmpty()) {
                                for (Object param : params) {
                                    String paramName = getJSONStringProperty((JSONObject) param, NAME);
                                    if (paramName != null) {
                                        paramNames.add(paramName);
                                    }
                                }
                            }
                            JsObject object = factory.newFunction(scope, toObject, methodName, paramNames, NodeJsUtils.NODEJS_NAME);
                            if(doc != null) {
                                object.setDocumentation(Documentation.create(doc, getDocumentationURL(methodName, paramNames)));
                            }
                            toObject.addProperty(object.getName(), object);
                            addProperties(factory, object, (DeclarationScope) object, method);
                            addMethods(factory, object, (DeclarationScope) object, method);
                        }
                    }
                }
            }
        }
    }

    private JsObject createProperty(final ModelElementFactory factory, final JsObject parent, final JSONObject jsonObject) {
        String propertyName = getJSONStringProperty(jsonObject, NAME);
        if (propertyName != null) {
            JsObject object = factory.newObject(parent, propertyName, OffsetRange.NONE, true, NodeJsUtils.NODEJS_NAME);
            parent.addProperty(object.getName(), object);
            String doc = getJSONStringProperty(jsonObject, DESCRIPTION);
            if(doc != null) {
                object.setDocumentation(Documentation.create(doc, getDocumentationURL(propertyName)));
            }
            return object;
        }
        return null;
    }

    private void addProperties(final ModelElementFactory factory, final JsObject toObject, final DeclarationScope scope, final JSONObject fromObject) {
        JSONArray properties = getJSONArrayProperty(fromObject, PROPERTIES);
        if (properties != null) {
            for (Object propertyO : properties) {
                if (propertyO instanceof JSONObject) {
                    JSONObject property = (JSONObject) propertyO;
                    JsObject newProperty = createProperty(factory, toObject, property);
                    if (newProperty != null) {
                        addProperties(factory, newProperty, scope, property);
                        addMethods(factory, newProperty, scope, property);
                    }
                }
            }
        }
    }

    private String getJSONStringProperty(final JSONObject object, final String property) {
        Object value = object.get(property);
        if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

    private JSONArray getJSONArrayProperty(final JSONObject object, final String property) {
        Object value = object.get(property);
        if (value instanceof JSONArray) {
            return (JSONArray) value;
        }
        return null;
    }

    private URL getDocumentationURL(String name) {
        StringBuilder sb = new StringBuilder();
        sb.append(docUrl).append(API_ALL_HTML_FILE).append("#all_");
        String alteredName = name;
        while (alteredName.charAt(0) == '_') {
            alteredName = alteredName.substring(1);
        }
        sb.append(alteredName.toLowerCase());
        URL result = null;
        try {
            result = new URL(sb.toString());
        } catch (MalformedURLException ex) {
            // Do nothing
        }
        return result;
    }

    private URL getDocumentationURL(String name, Collection<String> params) {
        URL result = getDocumentationURL(name);
        if (result != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(result.toExternalForm());
            for (String param : params) {
                sb.append('_').append(param);
            }
            result = null;
            try {
                result = new URL(sb.toString());
            } catch (MalformedURLException ex) {
                // Do nothing
            }
        }
        return result;
    }

    private JSONArray getModules() {
        String content = getContentApiFile();
        if (content != null && !content.isEmpty()) {
            JSONObject root = (JSONObject) JSONValue.parse(content);
            if (root != null) {
                Object jsonValue = root.get(MODULES);
                if (jsonValue instanceof JSONArray) {
                    return (JSONArray) jsonValue;
                }
            }
        }
        return null;
    }

    private void loadURL(URL url, Writer writer, Charset charset) throws IOException {
        if (charset == null) {
            charset = Charset.defaultCharset();
        }
        URLConnection con = url.openConnection();
        con.setConnectTimeout(URL_CONNECTION_TIMEOUT);
        con.setReadTimeout(URL_READ_TIMEOUT);
        con.connect();
        try (Reader r = new InputStreamReader(new BufferedInputStream(con.getInputStream()), charset)) {
            char[] buf = new char[2048];
            int read;
            while ((read = r.read(buf)) != -1) {
                writer.write(buf, 0, read);
            }
        }
    }

    private String getFileContent(File file) throws IOException {
        Reader r = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        try {
            char[] buf = new char[2048];
            int read;
            while ((read = r.read(buf)) != -1) {
                sb.append(buf, 0, read);
            }
        } finally {
            r.close();
        }
        return sb.toString();
    }

    private File getCachedAPIFile() {
        if (apiFile != null) {
            return apiFile;
        }
        if (docFolder != null) {
            for (FileObject folder : Collections.list(docFolder.getFolders(false))) {
                FileObject fo = folder.getFileObject(API_ALL_JSON_FILE);
                if (fo != null) {
                    apiFile = FileUtil.toFile(fo);
                    return apiFile;
                }
            }
        }
        File cacheFile = Places.getCacheSubfile(docApiFilePath);
        return cacheFile;
    }

    private String getContentApiFile() {
        String result = null;
        try {
            File cacheFile = getCachedAPIFile();
            if (!cacheFile.exists() && isSupportEnabled()) {

                //if any of the files is not loaded yet, start the loading process
                if (!loadingStarted) {
                    startLoading();
                }
                //load from web and cache locally
                loadDoc(cacheFile);
                stopLoading();

                LOG.log(Level.FINE, "Loading doc finished."); //NOI18N
            }
            result = cacheFile.exists() ? getFileContent(cacheFile) : null;
        } catch (URISyntaxException | IOException ex) {
            stopLoading();
            LOG.log(Level.INFO, "Cannot load NodeJS documentation from \"{0}\".", new Object[]{getDocumentationURL()}); //NOI18N
            LOG.log(Level.INFO, "", ex);
        }
        return result;
    }

    private void startLoading() {
        LOG.fine("start loading doc"); //NOI18N
        loadingStarted = true;
        progress = ProgressHandle.createHandle(Bundle.doc_building());
        progress.start(1);
    }
    
    private void stopLoading() {
        loadingStarted = false;
        if (progress != null) {
            progress.progress(1);
            progress.finish();
            progress = null;
        }
    }

    private void loadDoc(File cacheFile) throws URISyntaxException, MalformedURLException, IOException {
        LOG.fine("start loading doc"); //NOI18N
        URL url = new URL(getDocumentationURL().toExternalForm() + API_ALL_JSON_FILE);
        synchronized (cacheFile) {
            String tmpFileName = cacheFile.getAbsolutePath() + ".tmp";  //NOI18N
            File tmpFile = new File(tmpFileName);
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(tmpFile), StandardCharsets.UTF_8)) {
                loadURL(url, writer, StandardCharsets.UTF_8);
                writer.close();
                tmpFile.renameTo(cacheFile);
            } finally {
                if (tmpFile.exists()) {
                    tmpFile.delete();
                }
            }

        }
    }

    public String getDocumentationForGlobalObject(String nameObject) {
        String content = getContentApiFile();
        if (content != null && !content.isEmpty()) {
            File apiFile = getCachedAPIFile();
            JSONObject root = (JSONObject) JSONValue.parse(content);
            if (root != null) {
                JSONArray globals = getJSONArrayProperty(root, GLOBALS);
                if (globals != null) {
                    for (Object jsonValue : globals) {
                        if (jsonValue instanceof JSONObject) {
                            JSONObject global = (JSONObject) jsonValue;
                            String name = getJSONStringProperty(global, NAME);
                            if (name != null && name.equals(nameObject)) {
                                String doc = getJSONStringProperty(global, DESCRIPTION);
                                return doc;
                            }
                        }
                    }
                }
                JSONArray vars = getJSONArrayProperty(root, VARS);
                if (vars != null) {
                    for (Object jsonValue : vars) {
                        if (jsonValue instanceof JSONObject) {
                            JSONObject var = (JSONObject) jsonValue;
                            String name = getJSONStringProperty(var, NAME);
                            if (name != null && name.equals(nameObject)) {
                                String doc = getJSONStringProperty(var, DESCRIPTION);
                                return doc;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    /**
     *
     * @param fqn fully qualified name of the type.
     * @return
     */
    String getDocumentation(String fqn) {
        String moduleName = fqn.startsWith(NodeJsUtils.FAKE_OBJECT_NAME_PREFIX)
                ? fqn.substring(NodeJsUtils.FAKE_OBJECT_NAME_PREFIX.length()) : fqn;
        String[] parts = moduleName.split("\\.");
        if (parts.length > 2 && parts[0].equals(parts[1])) {
            // remove the first part of the fqn, because it's artificially added 
            // to the model to keep the global context clean
            parts = Arrays.copyOfRange(parts, 1, parts.length);
        }
        JSONArray modules = getModules();
        JSONObject module = null;
        if (modules != null) {
            for (Object moduleObject : modules) {
                module = (JSONObject) moduleObject;
                String name = getJSONStringProperty(module, NAME);
                if (name != null && name.equals(parts[0])) {
                    break;
                }
                module = null;
            }
        }
        if (module != null) {
            JSONObject property = module;
            for (int i = 1; i < parts.length; i++) {
                if (NodeJsUtils.PROTOTYPE.equals(parts[i])
                        || NodeJsUtils.EXPORTS.equals(parts[i])
                        || NodeJsUtils.MODULE.equals(parts[i])) {
                    continue;
                }
                property = findProperty(property, parts[i]);
                if (property == null) {
                    break;
                }
            }
            return property == null ? null : getJSONStringProperty(property, DESCRIPTION);
        }
        return null;
    }

    private JSONObject findProperty(final JSONObject parent, final String name) {
        JSONArray properties = getJSONArrayProperty(parent, PROPERTIES);
        if (properties != null) {
            for (Object propertyTmp : properties) {
                JSONObject property = (JSONObject) propertyTmp;
                String propertyName = getJSONStringProperty(property, NAME);
                if (propertyName != null && propertyName.equals(name)) {
                    return property;
                }
            }
        }
        properties = getJSONArrayProperty(parent, METHODS);
        if (properties != null) {
            for (Object propertyTmp : properties) {
                JSONObject property = (JSONObject) propertyTmp;
                String propertyName = getJSONStringProperty(property, NAME);
                if (propertyName != null && propertyName.equals(name)) {
                    return property;
                }
            }
        }
        properties = getJSONArrayProperty(parent, CLASSES);
        if (properties != null) {
            String className = getJSONStringProperty(parent, NAME) + '.' + name;
            for (Object propertyTmp : properties) {
                JSONObject property = (JSONObject) propertyTmp;
                String propertyName = getJSONStringProperty(property, NAME);
                if (propertyName != null && (propertyName.equals(className) || propertyName.equals(name))) {
                    return property;
                }
            }
        }
        return null;
    }
    
    private class ProjectSupportChangeListener implements ChangeListener {
        private final Project project;
        
        public ProjectSupportChangeListener(Project project) {
            this.project = project;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            cache.remove(project);
        }
        
    }
    
}
