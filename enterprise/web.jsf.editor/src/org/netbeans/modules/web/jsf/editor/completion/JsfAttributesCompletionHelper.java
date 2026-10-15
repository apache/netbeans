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
package org.netbeans.modules.web.jsf.editor.completion;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementScanner6;
import javax.swing.ImageIcon;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension.CompletionContext;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.ElementUtils;
import org.netbeans.modules.html.editor.lib.api.elements.ElementVisitor;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.common.taginfo.AttrValueType;
import org.netbeans.modules.web.common.taginfo.LibraryMetadata;
import org.netbeans.modules.web.common.taginfo.TagAttrMetadata;
import org.netbeans.modules.web.common.taginfo.TagMetadata;
import org.netbeans.modules.web.common.ui.api.FileReferenceCompletion;
import org.netbeans.modules.web.jsf.editor.JsfSupportImpl;
import org.netbeans.modules.web.jsf.editor.JsfUtils;
import org.netbeans.modules.web.jsf.editor.facelets.CompositeComponentLibrary;
import org.netbeans.modules.web.jsf.editor.facelets.FaceletsLibraryMetadata;
import org.netbeans.modules.web.jsf.editor.facelets.JsfNamespaceComparator;
import org.netbeans.modules.web.jsf.editor.index.CompositeComponentModel;
import org.netbeans.modules.web.jsf.editor.index.JsfPageModelFactory;
import org.netbeans.modules.web.jsfapi.api.Attribute;
import org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo;
import org.netbeans.modules.web.jsfapi.api.JsfSupport;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.api.LibraryComponent;
import org.netbeans.modules.web.jsfapi.api.NamespaceUtils;
import org.netbeans.modules.web.jsfapi.api.Tag;
import org.netbeans.modules.web.jsfapi.spi.LibraryUtils;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Contains helper method for completion within Facelet.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsfAttributesCompletionHelper {

    private static final FilenameSupport FILENAME_SUPPORT = new FilenameSupport();

    private static final Map<String, Set<String>> HTML_TO_JSF_MAPPING = new HashMap<>();
    static {
        // according to JavaDoc of the TagDecorator
        HTML_TO_JSF_MAPPING.put("a", new HashSet<>(Arrays.asList("commandLink", "outputLink", "link")));        //NOI18N
        HTML_TO_JSF_MAPPING.put("body", new HashSet<>(Arrays.asList("body")));                                  //NOI18N
        HTML_TO_JSF_MAPPING.put("button", new HashSet<>(Arrays.asList("commandButton", "button")));             //NOI18N
        HTML_TO_JSF_MAPPING.put("form", new HashSet<>(Arrays.asList("form")));                                  //NOI18N
        HTML_TO_JSF_MAPPING.put("head", new HashSet<>(Arrays.asList("head")));                                  //NOI18N
        HTML_TO_JSF_MAPPING.put("img", new HashSet<>(Arrays.asList("graphicImage")));                           //NOI18N
        HTML_TO_JSF_MAPPING.put("input", new HashSet<>(Arrays.asList("commandButton", "selectBooleanCheckbox",  //NOI18N
                "inputText", "inputFile", "inputHidden", "inputSecret")));                                      //NOI18N
        HTML_TO_JSF_MAPPING.put("label", new HashSet<>(Arrays.asList("outputLabel")));                          //NOI18N
        HTML_TO_JSF_MAPPING.put("link", new HashSet<>(Arrays.asList("outputStylesheet")));                      //NOI18N
        HTML_TO_JSF_MAPPING.put("script", new HashSet<>(Arrays.asList("outputScript")));                        //NOI18N
        HTML_TO_JSF_MAPPING.put("select", new HashSet<>(Arrays.asList("selectManyListbox", "selectOneListbox")));//NOI18N
        HTML_TO_JSF_MAPPING.put("textarea", new HashSet<>(Arrays.asList("inputTextArea")));                     //NOI18N
    }

    private JsfAttributesCompletionHelper() {
    }

    public static List<CompletionItem> getJsfItemsForHtmlElement(CompletionContext context, Library htmlLibrary, String prefix) {
        List<CompletionItem> result = new ArrayList<>();
        OpenTag ot = (OpenTag) context.getCurrentNode();
        Set<String> mapped = HTML_TO_JSF_MAPPING.get(ot.name().toString());
        if (mapped == null) {
            return Collections.emptyList();
        }

        // complete all attributes of JSF elements
        String prefixNs = prefix.substring(0, prefix.indexOf(":"));      // NOI18N
        String prefixValue = prefix.substring(prefix.indexOf(":") + 1); // NOI18N
        for (String jsfComponent : mapped) {
            completeAttributes(context, result, prefixNs, htmlLibrary, jsfComponent, prefixValue);
        }
        return result;
    }

    public static void completeAttributes(CompletionContext context, List<CompletionItem> items, String attrPrefix, Library lib, String component, String prefix) {
        OpenTag ot = (OpenTag) context.getCurrentNode();
        LibraryComponent comp = lib.getComponent(component);
        if (comp != null) {
            Tag tag = comp.getTag();
            if (tag != null) {
                Collection<Attribute> attrs = tag.getAttributes();
                //TODO resolve help
                Collection<String> existingAttrNames = new ArrayList<>();
                for (org.netbeans.modules.html.editor.lib.api.elements.Attribute a : ot.attributes()) {
                    existingAttrNames.add(a.name().toString());
                }

                int replaceOffset = attrPrefix.isEmpty() ? context.getCCItemStartOffset() : context.getCCItemStartOffset() + attrPrefix.length() + 1;
                for (Attribute a : attrs) {
                    String attrName = a.getName();
                    if ((!existingAttrNames.contains(attrName)|| existingAttrNames.contains(context.getItemText()))
                            && !containsCCEntry(items, attrName)) {
                        items.add(JsfCompletionItem.createAttribute(attrName, replaceOffset, lib, tag, a)); //NOI18N
                    }
                }
            }

        }

        if (!prefix.isEmpty()) {
            //filter the items according to the prefix
            Iterator<CompletionItem> itr = items.iterator();
            while (itr.hasNext()) {
                CharSequence insertPrefix = itr.next().getInsertPrefix();
                if(insertPrefix != null) {
                    if (!CharSequenceUtilities.startsWith(insertPrefix, prefix)) {
                        itr.remove();
                    }
                }
            }
        }
    }

    private static boolean containsCCEntry(List<CompletionItem> items, String entry) {
        for (CompletionItem completionItem : items) {
            if (completionItem.getInsertPrefix().equals(entry)) {
                return true;
            }
        }
        return false;
    }

    public static void completeJavaClasses(final CompletionContext context, final List<CompletionItem> items, String ns, OpenTag openTag) {
        // <cc:attribute type="com.example.|
        String tagName = openTag.unqualifiedName().toString();
        String attrName = context.getAttributeName();
        if (NamespaceUtils.containsNsOf(Collections.singleton(ns), DefaultLibraryInfo.COMPOSITE)
                && "attribute".equalsIgnoreCase(tagName) && "type".equalsIgnoreCase(attrName)) { //NOI18N

            FileObject fileObject = context.getResult().getSnapshot().getSource().getFileObject();
            JavaSource js = JavaSource.create(ClasspathInfo.create(fileObject));
            if (js == null) {
                return;
            }

            try {
                js.runUserActionTask(new org.netbeans.api.java.source.Task<CompilationController>() {
                    @Override
                    public void run(CompilationController cc) throws Exception {
                        String prefix = context.getPrefix();
                        String packageName = context.getPrefix();

                        int dotIndex = prefix.lastIndexOf('.'); // NOI18N
                        if (dotIndex != -1) {
                            packageName = prefix.substring(0, dotIndex);
                        }

                        // adds packages to the CC
                        addPackages(context, cc, items, prefix);

                        // adds types to the CC
                        addTypesFromPackages(context, cc, items, prefix, packageName);
                    }
                }, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static void addPackages(CompletionContext context, CompilationController controler, List<CompletionItem> items, String prefix) {
        int dotOffset = prefix.lastIndexOf('.');
        for (String pkgName : controler.getClasspathInfo().getClassIndex().getPackageNames(prefix, true, EnumSet.of(ClassIndex.SearchScope.SOURCE))) {
            items.add(HtmlCompletionItem.createAttributeValue(
                    pkgName.substring(dotOffset == -1 ? 0 : dotOffset + 1),
                    context.getCCItemStartOffset() + (dotOffset == -1 ? 0 : dotOffset + 1)));
        }
    }

    private static void addTypesFromPackages(CompletionContext context, CompilationController cc, List<CompletionItem> items, String prefix, String packageName) {
        int dotOffset = prefix.lastIndexOf('.');
        PackageElement pkgElem = cc.getElements().getPackageElement(packageName);
        if (pkgElem == null) {
            return;
        }

        List<TypeElement> tes = new TypeScanner().scan(pkgElem);
        for (TypeElement te : tes) {
            if (te.getQualifiedName().toString().startsWith(prefix)) {
                items.add(HtmlCompletionItem.createAttributeValue(
                        te.getSimpleName().toString(),
                        context.getCCItemStartOffset() + (dotOffset == -1 ? 0 : dotOffset + 1)));
            }
        }
    }

    public static void completeSectionsOfTemplate(final CompletionContext context, final List<CompletionItem> items, String ns, OpenTag openTag) {
        // <ui:define name="|" ...
        String tagName = openTag.unqualifiedName().toString();
        String attrName = context.getAttributeName();
        if (NamespaceUtils.containsNsOf(Collections.singleton(ns), DefaultLibraryInfo.FACELETS)
                && "define".equalsIgnoreCase(tagName) && "name".equalsIgnoreCase(attrName)) { //NOI18N

            // get the template path
            Node root = JsfUtils.getRoot(context.getResult(), DefaultLibraryInfo.FACELETS);
            final String[] template = new String[1];
            ElementUtils.visitChildren(root, new ElementVisitor() {
                @Override
                public void visit(Element node) {
                    OpenTag openTag = (OpenTag) node;
                    if ("composition".equalsIgnoreCase(openTag.unqualifiedName().toString())) { //NOI18N
                        for (org.netbeans.modules.html.editor.lib.api.elements.Attribute attribute : openTag.attributes()) {
                            if ("template".equalsIgnoreCase(attribute.name().toString())) { //NOI18N
                                template[0] = attribute.unquotedValue().toString();
                            }
                        }
                    }
                }
            }, ElementType.OPEN_TAG);

            if (template[0] == null) {
                return;
            }

            // find the template inside the web root or resource library contract
            List<Source> candidates = getTemplateCandidates(context.getResult().getSnapshot().getSource().getFileObject(), template[0]);
            try {
                ParserManager.parse(candidates, new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        Parser.Result result = resultIterator.getParserResult(0);
                        if (result.getSnapshot().getMimeType().equals("text/html")) {
                            HtmlParserResult htmlResult = (HtmlParserResult) result;
                            Node root = JsfUtils.getRoot(htmlResult, DefaultLibraryInfo.FACELETS);
                            if (root != null) {
                                List<OpenTag> foundNodes = findValue(root.children(OpenTag.class), getPrefixForFaceletsNs(htmlResult) + ":insert", new ArrayList<OpenTag>()); //NOI18N
                                for (OpenTag node : foundNodes) {
                                    org.netbeans.modules.html.editor.lib.api.elements.Attribute attr = node.getAttribute("name"); //NOI18N
                                    if (attr != null) {
                                        String value = attr.unquotedValue().toString();
                                        if (value != null && !"".equals(value)) { //NOI18N
                                            items.add(HtmlCompletionItem.createAttributeValue(value, context.getCCItemStartOffset(), !context.isValueQuoted())); //NOI18N
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static String getPrefixForFaceletsNs(HtmlParserResult result) {
        return DefaultLibraryInfo.FACELETS.getValidNamespaces().stream()
                .map(result.getNamespaces()::get)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private static List<OpenTag> findValue(Collection<OpenTag> nodes, String tagName, List<OpenTag> foundNodes) {
        if (nodes == null) {
            return foundNodes;
        }
        for (OpenTag ot : nodes) {
            if (LexerUtils.equals(tagName, ot.name(), true, false)) {
                foundNodes.add(ot);
            } else {
                foundNodes = findValue(ot.children(OpenTag.class), tagName, foundNodes);
            }

        }
        return foundNodes;
    }

    private static List<Source> getTemplateCandidates(FileObject client, String path) {
        List<Source> result = new ArrayList<>();
        FileObject template = client.getParent().getFileObject(path);
        if (template != null) {
            result.add(Source.create(template));
        }

        Project project = FileOwnerQuery.getOwner(client);
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null && wm.getDocumentBase() != null) {
            handleContracts(wm.getDocumentBase(), path, result);
        } else {
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            for (SourceGroup sourceGroup : sourceGroups) {
                FileObject metaInf = sourceGroup.getRootFolder().getFileObject("META-INF"); //NOI18N
                if (metaInf != null) {
                    handleContracts(metaInf, path, result);
                }
            }
        }

        return result;
    }

    private static void handleContracts(FileObject parent, String path, List<Source> result) {
        FileObject contractsFolder = parent.getFileObject("contracts"); //NOI18N
        if (contractsFolder != null) {
            for (FileObject child : contractsFolder.getChildren()) {
                FileObject contract = child.getFileObject(path);
                if (contract != null) {
                    result.add(Source.create(contract));
                }
            }
        }
    }

    //1.
    //<cc:implementation>
    //<cc:render/insertFacet name="|" />
    //</cc:implementation>
    //offsers facet declarations only from within this document
    public static void completeFacetsInCCImpl(CompletionContext context, List<CompletionItem> items, String ns, OpenTag openTag, JsfSupportImpl jsfs) {
        if (ns == null) {
            return;
        }

        if (DefaultLibraryInfo.COMPOSITE.getValidNamespaces().contains(ns.toLowerCase())) {
            String tagName = openTag.unqualifiedName().toString();
            if ("renderFacet".equalsIgnoreCase(tagName) || "insertFacet".equalsIgnoreCase(tagName)) { //NOI18N
                if ("name".equalsIgnoreCase(context.getAttributeName())) { //NOI18N
                    CompositeComponentModel ccModel = (CompositeComponentModel) JsfPageModelFactory.getFactory(CompositeComponentModel.Factory.class).getModel(context.getResult());
                    if (ccModel != null) {
                        Collection<String> facets = ccModel.getDeclaredFacets();
                        for (String facet : facets) {
                            items.add(HtmlCompletionItem.createAttributeValue(facet, context.getCCItemStartOffset(), !context.isValueQuoted())); //NOI18N
                        }
                    }
                }
            }
        }
    }

    //2.<f:facet name="|">
    //offsers all facetes
    public static void completeFacets(CompletionContext context, List<CompletionItem> items, String ns, OpenTag openTag, JsfSupportImpl jsfs) {
        if (ns == null) {
            return;
        }

        if (DefaultLibraryInfo.COMPOSITE.getValidNamespaces().contains(ns.toLowerCase())) {
            String tagName = openTag.unqualifiedName().toString();
            if ("facet".equalsIgnoreCase(tagName)) { //NOI18N
                if ("name".equalsIgnoreCase(context.getAttributeName())) { //NOI18N
                    //try to get composite library model for all declared libraries and extract facets from there
                    for (String libraryNs : context.getResult().getNamespaces().keySet()) {
                        Library library = jsfs.getLibrary(libraryNs);
                        if (library != null) {
                            if (library instanceof CompositeComponentLibrary) {
                                Collection<? extends LibraryComponent> lcs = library.getComponents();
                                for (LibraryComponent lc : lcs) {
                                    CompositeComponentLibrary.CompositeComponent ccomp = (CompositeComponentLibrary.CompositeComponent) lc;
                                    CompositeComponentModel model = ccomp.getComponentModel();
                                    for (String facetName : model.getDeclaredFacets()) {
                                        items.add(HtmlCompletionItem.createAttributeValue(facetName, context.getCCItemStartOffset(), !context.isValueQuoted())); //NOI18N
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void completeValueAccordingToType(CompletionContext context, List<CompletionItem> items, String ns, OpenTag openTag, JsfSupportImpl jsfs) {
        Library lib = jsfs.getLibrary(ns);
        if (lib == null) {
            return;
        }

        String tagName = openTag.unqualifiedName().toString();

        LibraryComponent comp = lib.getComponent(tagName);
        if (comp == null) {
            return;
        }

        String attrName = context.getAttributeName();
        Attribute attr = comp.getTag().getAttribute(attrName);
        if (attr == null) {
            return;
        }

        //TODO: Add more types and generalize the code then
        String aType = attr.getType();
        if ("boolean".equals(aType) || "java.lang.Boolean".equals(aType)) { //NOI18N
            //boolean type
            items.add(HtmlCompletionItem.createAttributeValue("true", context.getCCItemStartOffset(), !context.isValueQuoted())); //NOI18N
            items.add(HtmlCompletionItem.createAttributeValue("false", context.getCCItemStartOffset(), !context.isValueQuoted())); //NOI18N
        }

    }

    public static void completeFaceletsFromProject(CompletionContext context, List<CompletionItem> items, String ns, OpenTag openTag) {
        // <ui:include src="|" ...
        String tagName = openTag.unqualifiedName().toString();
        String attrName = context.getAttributeName();
        if (NamespaceUtils.containsNsOf(Collections.singleton(ns), DefaultLibraryInfo.FACELETS)
                && "include".equalsIgnoreCase(tagName) && "src".equalsIgnoreCase(attrName)) { //NOI18N
            items.addAll(FILENAME_SUPPORT.getItems(
                    context.getResult().getSnapshot().getSource().getFileObject(),
                    context.getCCItemStartOffset(),
                    context.getPrefix()));
        }
    }

    public static void completeXMLNSAttribute(CompletionContext context, List<CompletionItem> items, JsfSupport jsfs) {
        String xmlns = context.getAttributeName().toLowerCase(Locale.ENGLISH);

        if (xmlns.startsWith("xmlns:")) { //NOI18N
            Set<String> preferredNamespaces = Collections.emptySet();

            int preferredNamespaceSortPriority = 10;

            String xmlnsPrefix = xmlns.substring(6);
            if (!"".equals(xmlnsPrefix)) {
                Optional<? extends Library> preferedLibrary = jsfs.getLibraries().values().stream().filter(lib -> lib.getDefaultPrefix().equals(xmlnsPrefix)).findFirst();
                if (preferedLibrary.isPresent()) {
                    preferredNamespaces = preferedLibrary.get().getValidNamespaces();
                    for (String preferredNamespace : preferredNamespaces) {
                        items.add(HtmlCompletionItem.createAttributeValue(preferredNamespace, context.getCCItemStartOffset(), !context.isValueQuoted(), preferredNamespaceSortPriority++));
                    }
                }
            }

            //xml namespace completion for facelets namespaces
            List<String> otherNamespaces = new ArrayList<>(jsfs.getLibraries().keySet());
            otherNamespaces.sort(JsfNamespaceComparator.getInstance());

            int otherNamespaceSortPriority = 20;
            for (String namespace : otherNamespaces) {
                if (preferredNamespaces.contains(namespace)) {
                    continue;
                }

                items.add(HtmlCompletionItem.createAttributeValue(namespace, context.getCCItemStartOffset(), !context.isValueQuoted(), otherNamespaceSortPriority++));
            }
        } else if (xmlns.startsWith("xmlns")) { //NOI18N
            items.add(HtmlCompletionItem.createAttributeValue(LibraryUtils.XHTML_NS, context.getCCItemStartOffset(), !context.isValueQuoted()));
        }
    }

    public static void completeTagLibraryMetadata(CompletionContext context, List<CompletionItem> items, String ns, OpenTag openTag) {
        String attrName = context.getAttributeName();
        String tagName = openTag.unqualifiedName().toString();
        LibraryMetadata lib = FaceletsLibraryMetadata.get(ns);

        if (lib != null) {
            TagMetadata tag = lib.getTag(tagName);

            if (tag != null) {
                TagAttrMetadata attr = tag.getAttribute(attrName);

                if (attr != null) {
                    Collection<AttrValueType> valueTypes = attr.getValueTypes();

                    if (valueTypes != null) {
                        for (AttrValueType valueType : valueTypes) {
                            String[] possibleVals = valueType.getPossibleValues();

                            if (possibleVals != null) {
                                for (String val : possibleVals) {
                                    if (val.startsWith(context.getPrefix())) {
                                        CompletionItem itm = HtmlCompletionItem.createAttributeValue(val,
                                                context.getCCItemStartOffset(),
                                                !context.isValueQuoted());

                                        items.add(itm);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static class FilenameSupport extends FileReferenceCompletion<HtmlCompletionItem> {

        @Override
        public HtmlCompletionItem createFileItem(FileObject file, int anchor) {
            return HtmlCompletionItem.createFileCompletionItem(file, anchor);
        }

        @Override
        public HtmlCompletionItem createGoUpItem(int anchor, Color color, ImageIcon icon) {
            return HtmlCompletionItem.createGoUpFileCompletionItem(anchor, color, icon); // NOI18N
        }
    }

    private static final class TypeScanner extends ElementScanner6<List<TypeElement>, Void> {

        public TypeScanner() {
            super(new ArrayList<TypeElement>());
        }

        private static boolean isAccessibleClass(TypeElement te) {
            NestingKind nestingKind = te.getNestingKind();
            return (nestingKind == NestingKind.TOP_LEVEL);
        }

        @Override
        public List<TypeElement> visitType(TypeElement typeElement, Void arg) {
            if (typeElement.getKind() == javax.lang.model.element.ElementKind.CLASS && isAccessibleClass(typeElement)) {
                DEFAULT_VALUE.add(typeElement);
            }
            return super.visitType(typeElement, arg);
        }
    }
}
