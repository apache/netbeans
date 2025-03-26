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
package org.netbeans.modules.web.jsfapi.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.html.editor.lib.api.HtmlParsingResult;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.jsfapi.api.JsfSupport;
import org.netbeans.modules.web.jsfapi.api.JsfVersion;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.api.NamespaceUtils;

/**
 *
 * @author marekfukala
 */
public class LibraryUtils {

    public static final String COMPOSITE_LIBRARY_JAKARTA_NS = "jakarta.faces.composite"; //NOI18N
    public static final String COMPOSITE_LIBRARY_JCP_NS = "http://xmlns.jcp.org/jsf/composite"; //NOI18N
    public static final String COMPOSITE_LIBRARY_SUN_NS = "http://java.sun.com/jsf/composite"; //NOI18N
    public static final String XHTML_NS = "http://www.w3.org/1999/xhtml"; //NOI18N

    public static String getCompositeLibraryURL(String libraryFolderPath, JsfVersion jsfVersion) {
        if (jsfVersion.isAtLeast(JsfVersion.JSF_4_0)) {
            return COMPOSITE_LIBRARY_JAKARTA_NS + "/" + libraryFolderPath;
        } else if (jsfVersion.isAtLeast(JsfVersion.JSF_2_2)) {
            return COMPOSITE_LIBRARY_JCP_NS + "/" + libraryFolderPath;
        } else {
            return COMPOSITE_LIBRARY_SUN_NS + "/" + libraryFolderPath;
        }
    }

    public static Set<String> getAllCompositeLibraryNamespaces(String libraryName, JsfVersion jsfVersion) {
        Set<String> namespaces = new LinkedHashSet<>();
        if (jsfVersion.isAtLeast(JsfVersion.JSF_4_0)) {
            namespaces.add(COMPOSITE_LIBRARY_JAKARTA_NS + "/" + libraryName);
        }
        if (jsfVersion.isAtLeast(JsfVersion.JSF_2_2)) {
            namespaces.add(COMPOSITE_LIBRARY_JCP_NS + "/" + libraryName);
        }

        namespaces.add(COMPOSITE_LIBRARY_SUN_NS + "/" + libraryName);
        return namespaces;
    }

    public static boolean importLibrary(Document document, Library library, String prefix) {
        return !importLibrary(document, Collections.singletonMap(library, prefix)).isEmpty();
    }

    /**
     * Imports a facelets libraries
     *
     * @param document
     * @param libraries2prefixes a map of Library to prefix to declare. The prefix may be null, in
     * such case the default library prefix is used.
     *
     * @return a map of library2declared prefixes which contains just the imported pairs
     */
    public static Map<Library, String> importLibrary(Document document, Map<Library, String> libraries2prefixes) {
        assert document instanceof BaseDocument;

        final Map<Library, String> imports = new LinkedHashMap<>(libraries2prefixes);

        //verify and update the imports map
        Iterator<Library> libsIterator = imports.keySet().iterator();
        while (libsIterator.hasNext()) {
            Library l = libsIterator.next();
            String prefix = imports.get(l);
            if (prefix == null) {
                //not explicitly specified prefix, we may take the library's default one
                String defaultPrefix = l.getDefaultPrefix();
                if (defaultPrefix != null) {
                    imports.put(l, defaultPrefix); //update the map - add the default prefix, I recon no ConcurrentModificationException is thrown since the keyset remains the same
                } else {
                    //remove the library from the imports, we have no enough information
                    libsIterator.remove();
                }
            }
        }

        final BaseDocument bdoc = (BaseDocument) document;
        try {
            Source source = Source.create(bdoc);
            final HtmlParsingResult[] _result = new HtmlParsingResult[1];
            ParserManager.parse(Collections.singleton(source), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ResultIterator ri = WebUtils.getResultIterator(resultIterator, "text/html"); //NOI18N
                    if (ri != null) {
                        _result[0] = (HtmlParsingResult) ri.getParserResult();
                    }
                }
            });

            if (_result[0] == null) {
                //no html code
                return Collections.emptyMap();
            }

            //try find the html root node first
            final HtmlParsingResult result = _result[0];
            Element root = null;
            //no html root node, we need to find a root node of some other ast tree
            //belonging to some namespace
            Collection<Node> roots = new ArrayList<>();
            roots.addAll(result.roots().values());
            roots.add(result.rootOfUndeclaredTagsParseTree());

            for (Node r : roots) {
                //find first open tag node

                Collection<Element> chs = r.children(new ElementFilter() {

                    @Override
                    public boolean accepts(Element node) {
                        if(node.type() == ElementType.OPEN_TAG) {
                            OpenTag openTag = (OpenTag)node;
                            return !openTag.isEmpty() && !ElementUtils.isVirtualNode(node);
                        }
                        return false;
                        
                    }
                });

                List<Element> chsList = new ArrayList<>(chs);
                
                if (!chsList.isEmpty()) {
                    Element top = chsList.get(0);
                    if (root == null) {
                        root = top;
                    } else {
                        if (top.from() < root.to()) {
                            root = top;
                        }
                    }
                }
            }


            final Element rootNode = root;
            if (rootNode == null) {
                //TODO we may want to add a root node in such case
                return Collections.emptyMap();
            }

            //TODO decide whether to add a new line before or not based on other attrs - could be handled by the formatter!?!?!
            //first check if the library is already declared

            //XXX please note that the htmlresult.getNamespaces() returns a context free namespaces
            //declarations which is wrong. If any of the nested elements declares the namespace
            //the namespaces map will contain it and we will not add a new declaration which will
            //result into an invalid page

            //eliminate already declared libraries
            Iterator<Library> librariesIterator = imports.keySet().iterator();
            while (librariesIterator.hasNext()) {
                Library library = librariesIterator.next();
                Map<String, String> declaredNamespaces = result.getNamespaces();
                String alreadyDeclaredPrefix = NamespaceUtils.getForNs(declaredNamespaces, library.getNamespace());
                if (alreadyDeclaredPrefix == null) {
                    //try composite component library default prefix
                    String defaultNS = library.getDefaultNamespace();
                    alreadyDeclaredPrefix = declaredNamespaces.get(defaultNS);
                }
                if (alreadyDeclaredPrefix != null) {
                    //already declared, remove the library from the imports
                    librariesIterator.remove();
                }
            }

            //else add the declaration
            final Indent indent = Indent.get(bdoc);
            indent.lock();
            try {
                bdoc.runAtomic(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            OpenTag ot = (OpenTag)rootNode;
                            boolean noAttributes = ot.attributes().isEmpty();
                            //if there are no attributes, just add the new one at the end of the tag,
                            //if there are some, add the new one on a new line and reformat the tag

                            int offset_shift = 0;
                            Iterator<Library> libsItr = imports.keySet().iterator();
                            Parser.Result parsingApiResult = (Parser.Result)result;
                            int originalInsertPosition = parsingApiResult.getSnapshot().getOriginalOffset(rootNode.to() - 1); //just before the closing symbol
                            if (originalInsertPosition == -1) {
                                //error, cannot recover
                                imports.clear();
                                return;
                            }

                            while (libsItr.hasNext()) {
                                Library library = libsItr.next();
                                String prefixToDeclare = imports.get(library);
                                int insertPosition = originalInsertPosition + offset_shift;

                                String namespace = library.getNamespace();
                                String text = (!noAttributes ? "\n" : "") + " xmlns:" + prefixToDeclare + //NOI18N
                                        "=\"" + namespace + "\""; //NOI18N

                                bdoc.insertString(insertPosition, text, null);

                                offset_shift += text.length();
                            }


                            //reformat the tag so the new attribute gets aligned with the previous one/s
                            indent.reindent(originalInsertPosition, originalInsertPosition + offset_shift);

                        } catch (BadLocationException ex) {
                            Logger.getAnonymousLogger().log(Level.INFO, null, ex);
                        }
                    }
                });
            } finally {
                indent.unlock();
            }

            return imports; //return the remained libraries which should be those really imported

        } catch (ParseException ex) {
            Logger.getAnonymousLogger().log(Level.INFO, null, ex);
        }

        return Collections.emptyMap();
    }

    /** returns map of library namespace to library instance for all facelet libraries declared in the document/file. */
    public static Map<String, Library> getDeclaredLibraries(HtmlParsingResult result) {
        //find all usages of composite components tags for this page
        Collection<String> declaredNamespaces = result.getNamespaces().keySet();
        Map<String, Library> declaredLibraries = new HashMap<>();
        JsfSupport jsfSupport = JsfSupportProvider.get(result.getSyntaxAnalyzerResult().getSource().getSourceFileObject());
        if (jsfSupport != null) {
            Map<String, ? extends Library> libs = jsfSupport.getLibraries();

            for (String namespace : declaredNamespaces) {
                Library lib = NamespaceUtils.getForNs(libs, namespace);
                if (lib != null) {
                    declaredLibraries.put(namespace, lib);
                }
            }
        }
        return declaredLibraries;
    }

    /**
     * Generates and guesses prefix from its namespace.
     *
     * @param namespace of the library
     * @return generated prefix for given namespace
     * @since 1.27
     */
    @NonNull
    public static String generateDefaultPrefix(@NonNull String namespace) {
        final String HTTP_PREFIX = "http://"; //NOI18N
        if (namespace.startsWith(HTTP_PREFIX)) {
            namespace = namespace.substring(HTTP_PREFIX.length());
        }

        String[] tokens = namespace.split("[/.]"); //NOI18N
        if(tokens.length == 0) {
            //shoult not happen for normal URLs
            return "lib"; //NOI18N
        }

        StringBuilder sb = new StringBuilder();
        for(String token : tokens) {
            if(token.length() > 0) {
                sb.append(token.charAt(0));
            }
        }
        return sb.toString();
    }

    /**
     * Returns an array of opened projects which have associated a JsfSupport object.
     * 
     * @since 1.19
     * @return array of {@link Project}, never null.
     */
    public static Project[] getOpenedJSFProjects() {
        //may not return all projects as they are weakly held in the cache.
       return JsfSupportProvider.CACHE.keySet().toArray(new Project[]{});
    }
}
