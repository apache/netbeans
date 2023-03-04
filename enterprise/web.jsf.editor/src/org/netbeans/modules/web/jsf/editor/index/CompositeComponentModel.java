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
package org.netbeans.modules.web.jsf.editor.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.jsfapi.spi.LibraryUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author marekfukala
 */
public class CompositeComponentModel extends JsfPageModel {

//    private static enum MavenProjectStandartDirectories {
//        
//        SOURCES("src/main/java"), //NOI18N
//        RESOURCES("src/main/resources"), //NOI18N
//        CONFIG("src/main/config"), //NOI18N
//        WEBAPP("src/main/webapp"); //NOI18N
//        
//        private String path;
//
//        private MavenProjectStandartDirectories(String path) {
//            this.path = path;
//        }
//        
//        public String getPath() {
//            return path;
//        }
//        
//    }
    private static final Logger LOGGER = Logger.getLogger(CompositeComponentModel.class.getSimpleName());
    
    //index keys
    static final String LIBRARY_NAME_KEY = "library"; //NOI18N
    static final String INTERFACE_ATTRIBUTES_KEY = "interface_attributes"; //NOI18N
    static final String INTERFACE_DESCRIPTION_KEY = "interface_description"; //NOI18N
    static final String INTERFACE_FACETS = "interface_facets"; //NOI18N
    static final String HAS_IMPLEMENTATION_KEY = "has_implementation"; //NOI18N
    //other
    private static final String WEBAPP_RESOURCES_DIRECTORY = "javax.faces.WEBAPP_RESOURCES_DIRECTORY";
    private static final String RESOURCES_FOLDER_NAME = "resources"; //NOI18N
    private static final char VALUES_SEPARATOR = ','; //NOI18N
    private static final char ATTRIBUTES_SEPARATOR = ';'; //NOI18N
    private static final char KEY_VALUE_SEPARATOR = '='; //NOI18N
    
    private static final String NOT_AVAILABLE_VALUE = "n/a"; //NOI18N
    
    private static final Collection<String> COMPOSITE_ATTRIBUTE_TAG_ATTRIBUTES =
            Arrays.asList(new String[]{"name", "targets", "default", "displayName", "expert",
                "method-signature", "preferred", "required", "shortDescription", "type"}); //NOI18N
    protected Collection<Map<String, String>> attributes;
    protected Collection<String> facetsDeclarations;
    protected boolean hasImplementation;
    protected FileObject sourceFile;
    protected String relativePath;
    protected String interfaceDisplayName, interfaceShortDescription;

    public CompositeComponentModel(FileObject file, Collection<Map<String, String>> attributes, 
            boolean hasImplementation, Collection<String> facetsDeclarations, 
            String interfaceDisplayName, String interfaceShortDescription) {
        
        this(file, null, attributes, hasImplementation, facetsDeclarations, 
                interfaceDisplayName, interfaceShortDescription);
    }

    public CompositeComponentModel(FileObject file, String relativePath, 
            Collection<Map<String, String>> attributes, boolean hasImplementation, 
            Collection<String> facetsDeclarations, String interfaceDisplayName, 
            String interfaceShortDescription) {
        super();
        this.attributes = attributes;
        this.hasImplementation = hasImplementation;
        this.sourceFile = file;
        this.relativePath = relativePath;
        this.facetsDeclarations = facetsDeclarations;
        this.interfaceDisplayName = interfaceDisplayName;
        this.interfaceShortDescription = interfaceShortDescription;
    }

    public String getComponentName() {
        return sourceFile.getName();
    }
    
    public String getDisplayName() {
        return interfaceDisplayName != null && NOT_AVAILABLE_VALUE.equals(interfaceDisplayName) 
                ? null : interfaceDisplayName;
    }
    
    public String getShortDescription() {
           return interfaceShortDescription != null && NOT_AVAILABLE_VALUE.equals(interfaceShortDescription) 
                ? null : interfaceShortDescription;
    }

    public FileObject getSourceFile() {
        return sourceFile;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public boolean isHasImplementation() {
        return hasImplementation;
    }

    public Collection<Map<String, String>> getExistingInterfaceAttributes() {
        return attributes;
    }

    public Collection<String> getPossibleAttributeTagAttributes() {
        return COMPOSITE_ATTRIBUTE_TAG_ATTRIBUTES;
    }
    
    public Collection<String> getDeclaredFacets() {
        return facetsDeclarations;
    }

    @Override
    public String storeToIndex(IndexDocument document) {
        //store library name
        String libraryName = getLibraryPath();
        document.addPair(LIBRARY_NAME_KEY, libraryName, true, true);

        //store attributes
        StringBuilder buf = new StringBuilder();
        Iterator<Map<String, String>> itr = attributes.iterator();
        while (itr.hasNext()) {
            Map<String, String> attrs = itr.next();
            Iterator<Entry<String, String>> attrsKeysItr = attrs.entrySet().iterator();
            while (attrsKeysItr.hasNext()) {
                Entry<String, String> entry = attrsKeysItr.next();
                String key = entry.getKey();
                String value = entry.getValue();
                buf.append(key);
                buf.append(KEY_VALUE_SEPARATOR);
                buf.append(encode(value));
                if (attrsKeysItr.hasNext()) {
                    buf.append(VALUES_SEPARATOR);
                }
            }
            if (itr.hasNext()) {
                buf.append(ATTRIBUTES_SEPARATOR);
            }
        }
        document.addPair(INTERFACE_ATTRIBUTES_KEY, buf.toString(), false, true);

        //store implementation mark
        document.addPair(HAS_IMPLEMENTATION_KEY, Boolean.toString(hasImplementation), false, true);

        //facetes declarations
        Iterator<String> fitr = facetsDeclarations.iterator();
        StringBuilder sb = new StringBuilder();
        while(fitr.hasNext()) {
            sb.append(encode(fitr.next()));
            if(fitr.hasNext()) {
                sb.append(VALUES_SEPARATOR);
            }
        }
        document.addPair(INTERFACE_FACETS, sb.toString(), false, true);
        
        //interface displayname and short description
        StringBuilder dsb = new StringBuilder();
        dsb.append(encode(interfaceDisplayName != null ? interfaceDisplayName : NOT_AVAILABLE_VALUE));
        dsb.append(VALUES_SEPARATOR);
        dsb.append(encode(interfaceShortDescription != null ? interfaceShortDescription : NOT_AVAILABLE_VALUE));
        document.addPair(INTERFACE_DESCRIPTION_KEY, dsb.toString(), false, true);

        return LibraryUtils.getCompositeLibraryURL(libraryName, true); // the return value looks to be used nowhere
    }

    private String getLibraryPath() {
        FileObject sourceFileFolder = sourceFile.getParent();
        FileObject resources = getResourcesDirectory(sourceFile);

        assert resources != null;
        assert !sourceFileFolder.equals(resources);

        return FileUtil.getRelativePath(resources, sourceFileFolder);
    }

    static boolean isCompositeLibraryMember(FileObject file) {
        FileObject resourcesFolder = getResourcesDirectory(file);
        if (resourcesFolder != null) {
                //test if the file is an indirect ancestor of the resources folder.
                //the file cannot be in the resources folder itself
                if (FileUtil.isParentOf(resourcesFolder, file) && !file.getParent().equals(resourcesFolder)) {
                    return true;
                }
            }
        return false;
    }

    private static FileObject getResourcesDirectory(FileObject file) {
        WebModule wm = WebModule.getWebModule(file);
        if (wm != null) {
            //check webmodule's resources folder
            FileObject docRoot = wm.getDocumentBase();
            if(docRoot != null) { //document root may be null if the folder is deleted
                String relPath = RESOURCES_FOLDER_NAME;
                // issue #236035 - resource folder can be configurable
                FileObject dd = wm.getDeploymentDescriptor();
                if (dd != null) {
                    try {
                        WebApp ddRoot = DDProvider.getDefault().getDDRoot(dd);
                        if (ddRoot != null) {
                            InitParam[] parameters = ddRoot.getContextParam();
                            for (InitParam param: parameters) {
                                if (param.getParamName().contains(WEBAPP_RESOURCES_DIRECTORY)) {
                                    relPath = param.getParamValue().trim();
                                }
                            }
                        }
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, ex.getMessage());
                    }
                }

                FileObject resourcesFolder = docRoot.getFileObject(relPath);
                //check if the file is a descendant of the resources folder
                if(resourcesFolder != null && FileUtil.isParentOf(resourcesFolder, file)) {
                    return resourcesFolder;
                }
            }
        }
        
        //check project's sources - META-INF.*/resources
        //just check if the parent's parent directory is resources and then META-INF
        FileObject folder = file;
        do {
            if (folder.getName().equalsIgnoreCase("resources")) { //NOI18N
                //check if its parent is META-INF
                FileObject parent = folder.getParent();
                if (parent != null && parent.getName().startsWith("META-INF")) { //NOI18N
                    //the folder seems to be the right resources folder
                    return folder;
                }
            }
            folder = folder.getParent();
        } while (folder != null);


//        //look into the standart maven project locations
//        //src/main/webapp is covered by the 
//        Project project = FileOwnerQuery.getOwner(file);
//        if(project != null) {
//            FileObject projectRoot = project.getProjectDirectory();
//            FileObject resources = projectRoot.getFileObject("src/main/resources");
//            if(FileUtil.isParentOf(resources, file)) {
//                return resources;
//            }
//        }
//        
        return null;
    }

    public static class Factory extends JsfPageModelFactory {

        private static final String COMPOSITE_ATTRIBUTE_TAG_NAME = "attribute"; //NOI18N
        private static final String INTERFACE_TAG_NAME = "interface"; //NOI18N
        private static final String IMPLEMENTATION_TAG_NAME = "implementation"; //NOI18N
        private static final String FACET_TAG_NAME = "facet"; //NOI18N

        @Override
        public JsfPageModel getModel(HtmlParserResult result) {
            Node node = result.root(LibraryUtils.COMPOSITE_LIBRARY_NS);
            if (node == null || node.children().isEmpty()) {
                node = result.root(LibraryUtils.COMPOSITE_LIBRARY_LEGACY_NS);
            }
            if (node == null) {
                return null; //no composite library declaration
            }
            FileObject file = result.getSnapshot().getSource().getFileObject();

            //check whether the file lies in appropriate library folder
            if (!isCompositeLibraryMember(file)) {
                return null;
            }

            //composite:attribute tag -> map of its attributes
            final Collection<Map<String, String>> interfaceAttrs = new ArrayList<>();
            final Collection<String> facetDeclarations = new ArrayList<>();
            final boolean[] hasInterface = new boolean[1];
            final boolean[] hasImplementation = new boolean[1];
            final AtomicReference<String> interfaceDisplayName = new AtomicReference<>();
            final AtomicReference<String> interfaceShortDescrption = new AtomicReference<>();

            ElementUtils.visitChildren(node, new ElementVisitor() {

                private boolean inInterface = false;
                
                @Override
                public void visit(Element node) {
                    switch(node.type()) {
                        case OPEN_TAG:
                            OpenTag openTag = (OpenTag)node;
                            if(LexerUtils.equals(INTERFACE_TAG_NAME, openTag.unqualifiedName(), true, true)) {
                                inInterface = true;
                                
                                Attribute displayName = openTag.getAttribute("displayName"); //NOI18N
                                if(displayName != null) {
                                    interfaceDisplayName.set(displayName.unquotedValue().toString());
                                }
                                Attribute shortDescription = openTag.getAttribute("shortDescription"); //NOI18N
                                if(shortDescription != null) {
                                    interfaceShortDescrption.set(shortDescription.unquotedValue().toString());
                                }
                                
                                hasInterface[0] = true;
                                for (Element child : openTag.children(ElementType.OPEN_TAG)) {
                                    OpenTag openTagInner = (OpenTag)child;
                                    if(LexerUtils.equals(COMPOSITE_ATTRIBUTE_TAG_NAME, openTagInner.unqualifiedName(), true, true)) {
                                        //found composite:attribute tag
                                        Map<String, String> attrs = new HashMap<>();
                                        for (Attribute attr : openTagInner.attributes()) {
                                            String value = attr.unquotedValue() == null ? null : attr.unquotedValue().toString();
                                            attrs.put(attr.unqualifiedName().toString(), 
                                                    value);
                                        }
                                        interfaceAttrs.add(attrs);
                                    }
                                }
                            } else if (LexerUtils.equals(IMPLEMENTATION_TAG_NAME, openTag.unqualifiedName(), true, true)) {
                                hasImplementation[0] = true;
                            } else if (LexerUtils.equals(FACET_TAG_NAME, openTag.unqualifiedName(), true, true)) {
                                if(inInterface) {
                                    //<cc:interface>
                                    //<cc:facet name="header" />
                                    //</cc:interface>
                                    //
                                    Attribute facetName = openTag.getAttribute("name"); //NOI18N
                                    if(facetName != null) {
                                        CharSequence value = facetName.unquotedValue();
                                        String strVal = value == null ? null : value.toString().trim();
                                        if(strVal != null && strVal.length() > 0) {
                                            facetDeclarations.add(strVal);
                                        }
                                    }
                                }
                            }
                            break;
                        case CLOSE_TAG:
                            CloseTag ct = (CloseTag)node;
                            if(LexerUtils.equals(INTERFACE_TAG_NAME, ct.unqualifiedName(), true, true)) {
                                inInterface = false;
                            }
                            break;
                        default:
                            break;
                    }
                    
                }

            });

            //#176807 - The component file itself doesn't have to declare the interface or
            //implementation, it can be done in another referred page
//            if (hasInterface[0]) {
            return new CompositeComponentModel(file, interfaceAttrs, hasImplementation[0], 
                    facetDeclarations, interfaceDisplayName.get(), interfaceShortDescrption.get());
        }

        @Override
        public JsfPageModel loadFromIndex(IndexResult result) {
            String attrs = result.getValue(INTERFACE_ATTRIBUTES_KEY);
            boolean hasImplementation = Boolean.parseBoolean(result.getValue(HAS_IMPLEMENTATION_KEY));
            Collection<Map<String, String>> parsedAttrs = new ArrayList<>();
            Collection<String> facetDeclarations = new ArrayList<>();
            //parse attributes
            StringTokenizer st = new StringTokenizer(attrs, Character.valueOf(ATTRIBUTES_SEPARATOR).toString());
            while (st.hasMoreTokens()) {
                String attrText = st.nextToken();
                Map<String, String> pairs = new HashMap<>();
                StringTokenizer st2 = new StringTokenizer(attrText, Character.valueOf(VALUES_SEPARATOR).toString());
                while (st2.hasMoreTokens()) {
                    String pair = st2.nextToken();
                    int key_value_separatorIndex = pair.indexOf(KEY_VALUE_SEPARATOR);
                    if(key_value_separatorIndex == -1) {
                        LOGGER.log(Level.INFO, "Bad interface_attributes section in IndexResult: ", attrs); //NOI18N
                    } else {
                        String key = pair.substring(0, key_value_separatorIndex);
                        String value = decode(pair.substring(pair.indexOf(KEY_VALUE_SEPARATOR) + 1));
                        pairs.put(key, value);
                    }
                }
                parsedAttrs.add(pairs);
            }
            
            //parse facets
            String facets = result.getValue(INTERFACE_FACETS);
            StringTokenizer fst = new StringTokenizer(facets, Character.valueOf(VALUES_SEPARATOR).toString());
            while (fst.hasMoreTokens()) {
                facetDeclarations.add(decode(fst.nextToken()));
                
            }
            
            //interface display name and short description
            String displayName = null;
            String shortDescription = null;
            String descr = result.getValue(INTERFACE_DESCRIPTION_KEY);
            int separatorIndex = descr.indexOf(VALUES_SEPARATOR);
            if(separatorIndex >= 0) {
                displayName = descr.substring(0, separatorIndex);
                shortDescription = descr.substring(separatorIndex + 1);
            } else {
                LOGGER.log(Level.INFO, "Bad interface_description section in IndexResult: ", descr); //NOI18N
            }
            
            return new CompositeComponentModel(result.getFile(), result.getRelativePath(), 
                    parsedAttrs, hasImplementation, facetDeclarations,
                    displayName, shortDescription);
        }
    }


    static String encode(String attributeValue) {
        //comma and equal sign needs to be encoded
        StringBuilder out = new StringBuilder();
        for(int i = 0; i < attributeValue.length(); i++) {
            char c = attributeValue.charAt(i);
            switch(c) {
                case ',':
                    out.append("\\c");
                    break;
                case '=':
                    out.append("\\e");
                    break;
                case ';':
                    out.append("\\m");
                    break;
                case '\\':
                    out.append("\\s");
                    break;
                default:
                    out.append(c);

            }
        }
        return out.toString();
    }

    static String decode(String attributeValue) {
        //comma and equal sign needs to be encoded
        StringBuilder out = new StringBuilder();
        boolean encodeChar = false;
        for(int i = 0; i < attributeValue.length(); i++) {
            char c = attributeValue.charAt(i);
            if(encodeChar) {
                switch(c) {
                    case 'c':
                        out.append(',');
                        break;
                    case 'e':
                        out.append('=');
                        break;
                    case 'm':
                        out.append(';');
                        break;
                    case 's':
                        out.append('\\');
                        break;
                    default:
                        assert false;
                }
                encodeChar = false;
            } else {
                if(c == '\\') {
                    encodeChar = true;
                } else {
                    out.append(c);
                }
            }
        }
        return out.toString();
    }
}
