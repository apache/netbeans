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
package org.netbeans.modules.web.core.syntax;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.web.core.syntax.tld.LibraryDescriptor;
import org.netbeans.modules.web.core.syntax.tld.LibraryDescriptorException;
import org.netbeans.modules.web.core.syntax.tld.TldLibrary;
import org.netbeans.modules.web.el.spi.ELPlugin;
import org.netbeans.modules.web.el.spi.Function;
import org.netbeans.modules.web.el.spi.ImplicitObject;
import org.netbeans.modules.web.el.spi.ImplicitObjectType;
import org.netbeans.modules.web.el.spi.ResolverContext;
import org.netbeans.modules.web.el.spi.ResourceBundle;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author petr, mfukala@netbeans.org
 */
@ServiceProvider(service = ELPlugin.class)
public class JSPELPlugin extends ELPlugin {

    private static final Logger LOGGER = Logger.getLogger(JSPELPlugin.class.getName());
    private static final String PLUGIN_NAME = "JSP EL Plugin"; //NOI18N
    private Collection<String> MIMETYPES = Arrays.asList(new String[]{"text/x-jsp", "text/x-tag"});

    private Collection<ImplicitObject> implicitObjects;


    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public Collection<String> getMimeTypes() {
        return MIMETYPES;
    }

    @Override
    public Collection<ImplicitObject> getImplicitObjects(FileObject file) {
        if (file != null && JspUtils.isJSPOrTagFile(file)) {
            return getImplicitObjects();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<ResourceBundle> getResourceBundles(FileObject file, ResolverContext context) {
        return Collections.emptyList();
    }

    static class PageContextObject extends ELImplicitObject {

        public PageContextObject(String name) {
            super(name);
            setType(ImplicitObjectType.OBJECT_TYPE);
            setClazz("javax.servlet.jsp.PageContext"); //NOI18N
        }
    }

    private synchronized Collection<ImplicitObject> getImplicitObjects() {
        if(implicitObjects == null) {
            initImplicitObjects();
        }
        return implicitObjects;
    }

    private synchronized void initImplicitObjects() {
        implicitObjects = new ArrayList<ImplicitObject>(11);
        implicitObjects.addAll(getScopeObjects());
        implicitObjects.add(new PageContextObject("pageContext")); // NOI18N
        implicitObjects.add(new ELImplicitObject("param")); // NOI18N
        implicitObjects.add(new ELImplicitObject("paramValues")); // NOI18N
        implicitObjects.add(new ELImplicitObject("header")); // NOI18N
        implicitObjects.add(new ELImplicitObject("headerValues")); // NOI18N
        implicitObjects.add(new ELImplicitObject("initParam")); // NOI18N
        implicitObjects.add(new ELImplicitObject("cookie")); // NOI18N
    }

    /**
     * @return the implicit scope objects, i.e. {@code requestScope, sessionScope} etc.
     */
    private static Collection<ELImplicitObject> getScopeObjects() {
        Collection<ELImplicitObject> result = new ArrayList<ELImplicitObject>(4);
        result.add(new ELImplicitObject("pageScope")); // NOI18N
        result.add(new ELImplicitObject("sessionScope")); // NOI18N
        result.add(new ELImplicitObject("applicationScope")); // NOI18N
        result.add(new ELImplicitObject("requestScope"));
        for (ELImplicitObject each : result) {
            each.setType(ImplicitObjectType.SCOPE_TYPE);
        }
        return result;

    }

    @Override
    public List<Function> getFunctions(FileObject file) {
        List<Function> functions =  new ArrayList<Function>();
        Document document = getDocumentForFile(file);
        if (!(document instanceof BaseDocument)) {
            return functions;
        }

        JspSyntaxSupport ss = JspSyntaxSupport.get(document);
        Map prefixMapper = ss.getPrefixMapper();
        if (prefixMapper == null || prefixMapper.isEmpty()) {
            return functions;
        }
        Map<String, String> urlToPrefixMapper = transposeMap(prefixMapper);

        Map<String, String[]> tagLibMappings = JspUtils.getTaglibMap(file);
        for (Entry<String, String[]> entry : tagLibMappings.entrySet()) {
            String url = entry.getKey();
            if (urlToPrefixMapper.containsKey(url)) {
                // entry.getValue[0]==jarPath, entry.getValue[1]==tldPath
                functions.addAll(getFunctionsForUrl(
                        entry.getValue()[0],
                        entry.getValue()[1],
                        urlToPrefixMapper.get(url)));
            }
        }

        return functions;
    }

    private static Map<String, String> transposeMap(Map<String, String> original) {
        Map<String, String> newMap = new HashMap<String, String>(original.size());
        for (Map.Entry<String, String> entry : original.entrySet()) {
            newMap.put(entry.getValue(), entry.getKey());
        }
        return newMap;
    }

    private static Document getDocumentForFile(FileObject fo) {
        try {
            EditorCookie ec = DataObject.find(fo).getLookup().lookup(EditorCookie.class);
            return (ec == null) ? null : ec.getDocument();
        } catch (DataObjectNotFoundException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return null;
    }

    private static List<Function> getFunctionsForUrl(String jarPath, String tldPath, String prefix) {
        List<Function> functions = new ArrayList<Function>();

        String FILE_PREFIX = "file:/"; //NOI18N

        File f;
        if (jarPath.startsWith(FILE_PREFIX)) {
            URI u = URI.create(jarPath);
            f = new File(u);
        } else {
            f = new File(jarPath);
        }

        try {
            FileObject tldFile = null;
            if (tldPath != null && tldPath.endsWith(jarPath)) {
                // tld is not inside any .JAR file
                tldFile = FileUtil.toFileObject(new File(tldPath));
            } else {
                JarFileSystem jfs = new JarFileSystem(FileUtil.normalizeFile(f));
                tldFile = jfs.getRoot().getFileObject(tldPath);
            }
            if (tldFile != null) {
                TldLibrary tldLib = TldLibrary.create(tldFile);

                Iterator<Entry<String, LibraryDescriptor.Function>> iterator = tldLib.getFunctions().entrySet().iterator();
                while (iterator.hasNext()) {
                    Entry<String, LibraryDescriptor.Function> entry = iterator.next();
                    functions.add(new Function(
                            prefix + ":" + entry.getKey(), //NOI18N
                            getReturnTypeForSignature(entry.getValue().getSignature()),
                            getParametersForSignature(entry.getValue().getSignature()),
                            getDescription(entry.getValue().getDescription(), entry.getValue().getExample())));
                }
            } else {
                LOGGER.log(Level.FINE, "No FileObject for {0}:{1}", new Object[]{jarPath, tldPath});
            }
        } catch (IOException ioe) {
            LOGGER.log(Level.INFO, null, ioe);
        } catch (LibraryDescriptorException lde) {
            LOGGER.log(Level.INFO, null, lde);
        }
        return functions;
    }

    private static String getReturnTypeForSignature(String signature) {
        String returnType = signature.substring(0, signature.indexOf(" ")); //NOI18N
        return getSimpleNameForType(returnType.trim());
    }

    private static List<String> getParametersForSignature(String signature) {
        List<String> params = new ArrayList<String>();
        String paramString = signature.substring(signature.indexOf("(") + 1, signature.indexOf(")")); //NOI18N
        for (String param : paramString.split(",")) { //NOI18N
            params.add(getSimpleNameForType(param.trim()));
        }
        return params;
    }

    private static String getSimpleNameForType(String fqn) {
        return fqn.substring(fqn.lastIndexOf(".") + 1); //NOI18N
    }

    private static String getDescription(String description, String example) {
        return description;
        //TODO - complete the doc with example (requires HTML escaped String)
//        if (example.isEmpty()) {
//            return description;
//        } else {
//            return description + "<br><br><font color='#ce7b00'>Example: " + example + "</font>"; //NOI18N
//        }
    }

    private static class ELImplicitObject implements ImplicitObject {

        private String myName;
        private ImplicitObjectType myType;
        private String myClazz;

        /** Creates a new instance of ELImplicitObject */
        public ELImplicitObject(String name) {
            myName = name;
            myType = ImplicitObjectType.MAP_TYPE;
        }

        @Override
        public String getName() {
            return myName;
        }

        @Override
        public ImplicitObjectType getType() {
            return myType;
        }

        public void setType(ImplicitObjectType type) {
            myType = type;
        }

        @Override
        public String getClazz() {
            return myClazz;
        }

        public void setClazz(String clazz) {
            myClazz = clazz;
        }
    }
}
