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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementUtils;
import org.netbeans.modules.html.editor.lib.api.elements.ElementVisitor;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Model for the stored resources mappings (outputScripts and outputStylesheets)
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ResourcesMappingModel extends JsfPageModel {

    // index - keys
    static final String STATIC_RESOURCES_KEY = "static_resources";  //NOI18N

    // index - other
    private static final char RESOURCES_SEPARATOR = ';';            //NOI18N
    private static final char NAME_SEPARATOR = '@';                 //NOI18N
    private static final char LIB_SEPARATOR = ':';                  //NOI18N

    private static final Pattern EL_STATEMENT_PATTERN = Pattern.compile("^[$#]+[{].*[}]");
    private static final Pattern JSF_RESOURCE_PATTERN = Pattern.compile(".*[#$][{].*(resource)[\\[].*[\\]].*[}]");

    private final FileObject file;
    private final List<Resource> staticResources;

    public ResourcesMappingModel(FileObject file, List<Resource> resources) {
        this.file = file;
        this.staticResources = resources;
    }

    @Override
    public String storeToIndex(IndexDocument document) {
        // store resources
        StringBuilder resString = new StringBuilder();
        for (Iterator<Resource> it = staticResources.iterator(); it.hasNext();) {
            Resource resource = it.next();
            resString.append(resource.type.toString());
            resString.append(NAME_SEPARATOR);
            resString.append(resource.name);
            resString.append(LIB_SEPARATOR);
            resString.append(resource.library);
            if (it.hasNext()) {
                resString.append(RESOURCES_SEPARATOR);
            }
        }
        document.addPair(STATIC_RESOURCES_KEY, resString.toString(), true, true);

        return ""; // the return value looks to be used nowhere
    }

    public static Collection<? extends Resource> parseResourcesFromString(String resString) {
        List<Resource> resources = new ArrayList<>();
        // parse static resources
        for (String resource : resString.split("[" + RESOURCES_SEPARATOR + "]")) {
            int nameSepIndex = resource.indexOf(NAME_SEPARATOR);
            int libSepIndex = resource.indexOf(LIB_SEPARATOR);
            if (nameSepIndex != -1 && libSepIndex != -1) {
                resources.add(new Resource(
                        ResourceType.fromString(resource.substring(0, nameSepIndex)),
                        resource.substring(nameSepIndex + 1, libSepIndex),
                        resource.substring(libSepIndex + 1)));
            }
        }
        return resources;
    }

    private static String getAttributeELValue(Snapshot htmlSnapshot, String originalContent, Attribute attribute) {
        int startOffset = htmlSnapshot.getOriginalOffset(attribute.valueOffset());
        int endOffset = htmlSnapshot.getOriginalOffset(attribute.valueOffset() + attribute.value().length());
        if (startOffset == -1 || endOffset == -1 || startOffset > endOffset) {
            return null;
        } else {
            String quotedValue = originalContent.substring(startOffset, endOffset);
            if (quotedValue.startsWith("'") || quotedValue.startsWith("\"")) {
                return quotedValue.substring(1, quotedValue.length() - 1);
            }
            return quotedValue;
        }
    }

    /* tests */ static boolean isELExpression(String statement) {
        return EL_STATEMENT_PATTERN.matcher(statement).matches();
    }

    /* tests */ static boolean isJsfResource(String elExpression) {
        return JSF_RESOURCE_PATTERN.matcher(elExpression).matches();
    }

    /* tests */ static Resource parseResource(ResourceType resourceType, String expression) {
        // XXX - would be faster to parse it using common ELParser?
        int resourceStartBracketOffset = expression.indexOf("resource[");
        int resourceEndBracketOffset = expression.indexOf("]", resourceStartBracketOffset);
        String resourceValue = expression.substring(resourceStartBracketOffset + 9, resourceEndBracketOffset);
        if (resourceValue.startsWith("'") || resourceValue.startsWith("\"")) {
            resourceValue = resourceValue.substring(1, resourceValue.length() - 1);
        }
        if (resourceValue.isEmpty()) {
            return null;
        }

        int colonOffset = resourceValue.indexOf(":");
        return new Resource(resourceType,
                colonOffset == -1 ? resourceValue : resourceValue.substring(colonOffset + 1),
                colonOffset == -1 ? "" : resourceValue.substring(0, colonOffset));
    }

    public static class Factory extends JsfPageModelFactory {

        private static final String VIRTUAL_SOURCE = "@@@";                             //NOI18N

        private static final String OUTPUT_STYLESHEET_TAG_NAME = "outputStylesheet";    //NOI18N
        private static final String OUTPUT_SCRIPT_TAG_NAME = "outputScript";            //NOI18N
        private static final String LINK_TAG_NAME = "link";                             //NOI18N
        private static final String SCRIPT_TAG_NAME = "script";                         //NOI18N

        @Override
        public JsfPageModel getModel(HtmlParserResult result) {
            List<Resource> resources = new ArrayList<>();
            FileObject file = result.getSnapshot().getSource().getFileObject();
            resources.addAll(getResourcesDefinedByJsfComponents(result));
            resources.addAll(getResourcesDefinedByHtmlTags(result));
            return new ResourcesMappingModel(file, resources);
        }

        @Override
        public JsfPageModel loadFromIndex(IndexResult result) {
            List<Resource> resources = new ArrayList<>();
            String resString = result.getValue(STATIC_RESOURCES_KEY);
            resources.addAll(parseResourcesFromString(resString));
            return new ResourcesMappingModel(result.getFile(), resources);
        }

        private Collection<Resource> getResourcesDefinedByJsfComponents(HtmlParserResult result) {
            final List<Resource> resources = new ArrayList<>();
            Node node = result.root(DefaultLibraryInfo.HTML.getNamespace());
            if (node == null || node.children().isEmpty()) {
                node = result.root(DefaultLibraryInfo.HTML.getLegacyNamespace());
            }
            if (node == null || node.children().isEmpty()) {
                return resources; //no HTML Basic component in the page
            }

            // looks for all h:outputStylesheet and h:outputScript elements
            ElementUtils.visitChildren(node, new ElementVisitor() {
                @Override
                public void visit(Element node) {
                    switch(node.type()) {
                        case OPEN_TAG:
                            OpenTag openTag = (OpenTag) node;
                            if (LexerUtils.equals(OUTPUT_STYLESHEET_TAG_NAME, openTag.unqualifiedName(), false, true)
                                    || LexerUtils.equals(OUTPUT_SCRIPT_TAG_NAME, openTag.unqualifiedName(), false, true)) {
                                Attribute name = openTag.getAttribute("name");          //NOI18N
                                Attribute library = openTag.getAttribute("library");    //NOI18N
                                if (name == null) {
                                    break;
                                }
                                Resource resource = new Resource(LexerUtils.equals(OUTPUT_SCRIPT_TAG_NAME, openTag.unqualifiedName(), false, true) ?
                                        ResourceType.SCRIPT : ResourceType.STYLESHEET,
                                        name.unquotedValue().toString(),
                                        library == null ? "" : library.unquotedValue().toString()); //NOI18N
                                resources.add(resource);
                            }
                            break;
                        default:
                            break;
                    }
                }
            });

            return resources;
        }

        private Collection<Resource> getResourcesDefinedByHtmlTags(final HtmlParserResult result) {
            final List<Resource> resources = new ArrayList<>();
            Node node = result.root();
            if (node == null || node.children().isEmpty()) {
                return resources; //no tags in the file
            }
            final String[] originalContent = new String[1];
            try {
                ParserManager.parse(Arrays.asList(result.getSnapshot().getSource()), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        originalContent[0] = resultIterator.getSnapshot().getText().toString();
                    }
                });
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }

            // Looks for all <script>, <style> elements. Procesed should be all values with EL resource definition via #{resource['']}.
            ElementUtils.visitChildren(node, new ElementVisitor() {
                @Override
                public void visit(Element node) {
                    switch(node.type()) {
                        case OPEN_TAG:
                            OpenTag openTag = (OpenTag) node;
                            if (LexerUtils.equals(LINK_TAG_NAME, openTag.unqualifiedName(), true, true)) {
                                Attribute rel = openTag.getAttribute("rel");            //NOI18N
                                Attribute type = openTag.getAttribute("type");          //NOI18N
                                Attribute href = openTag.getAttribute("href");          //NOI18N
                                if (rel == null || type == null || href == null
                                        // cases not like <link href="#{resource['anyFolder:test3.css']}" ...
                                        || !CharSequenceUtilities.equals(href.unquotedValue(), VIRTUAL_SOURCE)) {
                                    break;
                                }
                                String elResource = getAttributeELValue(result.getSnapshot(), originalContent[0], href);
                                if (elResource != null && isELExpression(elResource) && isJsfResource(elResource)) {
                                    Resource resource = parseResource(ResourceType.STYLESHEET, elResource);
                                    if (resource != null) {
                                        resources.add(resource);
                                    }
                                }
                            } else if (LexerUtils.equals(SCRIPT_TAG_NAME, openTag.unqualifiedName(), true, true)) {
                                Attribute type = openTag.getAttribute("type");          //NOI18N
                                Attribute src = openTag.getAttribute("src");            //NOI18N
                                if (type == null || src == null
                                        // cases not like <script src="#{resource['jsLib:test3.js']}" ...
                                        || !CharSequenceUtilities.equals(src.unquotedValue(), VIRTUAL_SOURCE)) {
                                    break;
                                }
                                String elResource = getAttributeELValue(result.getSnapshot(), originalContent[0], src);
                                if (elResource != null && isELExpression(elResource) && isJsfResource(elResource)) {
                                    Resource resource = parseResource(ResourceType.SCRIPT, elResource);
                                    if (resource != null) {
                                        resources.add(resource);
                                    }
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }
            });

            return resources;
        }
    }

    public static class Resource {

        private final ResourceType type;
        private final String name;
        private final String library;

        public Resource(ResourceType type, String name, String library) {
            this.type = type;
            this.name = name;
            this.library = library;
        }

        public ResourceType getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getLibrary() {
            return library;
        }

    }

    public static enum ResourceType {
        SCRIPT("script"),
        STYLESHEET("stylesheet");

        private final String value;

        private ResourceType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        public static ResourceType fromString(String string) {
            for (ResourceType resourceType : ResourceType.values()) {
                if (resourceType.value.equals(string)) {
                    return resourceType;
                }
            }
            return null;
        }

    }

}
