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
package org.netbeans.modules.web.jsf.editor;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Rule.SelectionRule;
import org.netbeans.modules.csl.api.*;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.HtmlParsingResult;
import org.netbeans.modules.html.editor.lib.api.elements.CloseTag;
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
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.spi.LibraryUtils;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author marekfukala
 */
public class InjectCompositeComponent {

    private static final int HINT_PRIORITY = 60; //magic number
    private static final String TEMPLATES_FOLDER = "JSF";   //NOI18N
    private static final String TEMPLATE_NAME = "out.xhtml";  //NOI18N

    public static void inject(Document document, int from, int to) {
        try {
            FileObject fileObject = NbEditorUtilities.getFileObject(document);
            Project project = FileOwnerQuery.getOwner(fileObject);

            instantiateTemplate(project, fileObject, document, from, to);

        } catch (ParseException | BadLocationException | IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static Hint getHint(final RuleContext context, final int from, final int to) {
        return new Hint(injectCCRule,
                NbBundle.getMessage(InjectCompositeComponent.class, "MSG_InjectCompositeComponentSelectionHintDescription"), //NOI18N
                context.parserResult.getSnapshot().getSource().getFileObject(),
                new OffsetRange(from, to),
                Collections.<HintFix>singletonList(new HintFix() {
            @Override
            public String getDescription() {
                return NbBundle.getMessage(InjectCompositeComponent.class, "MSG_InjectCompositeComponentSelectionHintDescription"); //NOI18N
            }

            @Override
            public void implement() throws Exception {
                inject(context.doc, from, to);
            }

            @Override
            public boolean isSafe() {
                return true;
            }

            @Override
            public boolean isInteractive() {
                return true;
            }
        }),
                HINT_PRIORITY);
    }

    private static void instantiateTemplate(Project project, FileObject file, final Document document, final int startOffset, final int endOffset) throws BadLocationException, DataObjectNotFoundException, IOException, ParseException {
        String selectedText = startOffset == endOffset ? null : document.getText(startOffset, endOffset - startOffset);

        TemplateWizard templateWizard = new TemplateWizard();
        templateWizard.putProperty("project", project); //NOI18N
        templateWizard.putProperty("selectedText", selectedText); //NOI18N
        templateWizard.setTitle(NbBundle.getMessage(InjectCompositeComponent.class, "MSG_InsertCompositeComponent")); //NOI18N
        templateWizard.putProperty("fromEditor", true); //NOI18N
        DataFolder templatesFolder = templateWizard.getTemplatesFolder();
        FileObject template = templatesFolder.getPrimaryFile().getFileObject(TEMPLATES_FOLDER + "/" + TEMPLATE_NAME);   //NOI18N
        DataObject templateDO;
        FileObject projectDir = project.getProjectDirectory();
        DataFolder targetFolder = DataFolder.findFolder(projectDir);

        final Logger logger = Logger.getLogger(InjectCompositeComponent.class.getSimpleName());
        final JsfSupportImpl jsfs = JsfSupportImpl.findFor(file);
        if (jsfs == null) {
            logger.log(Level.WARNING, "Cannot find JsfSupport instance for file {0}", file.getPath()); //NOI18N
            return;
        }

        final SnippetContext context = getSnippetContext(document, startOffset, endOffset, jsfs);
        if (!context.isValid()) {
            templateWizard.putProperty("incorrectActionContext", true); //NOI18N
        }

        //get list of used declarations, which needs to be passed to the wizard
        Source source = Source.create(document);
        final AtomicReference<Map<String, String>> declaredPrefixes = new AtomicReference<>();
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                ResultIterator ri = WebUtils.getResultIterator(resultIterator, HtmlKit.HTML_MIME_TYPE);
                if (ri != null) {
                    HtmlParsingResult result = (HtmlParsingResult) ri.getParserResult();
                    if (result != null) {
                        declaredPrefixes.set(result.getNamespaces());
                    }
                }
            }
        });
        templateWizard.putProperty("declaredPrefixes", declaredPrefixes.get()); //NOI18N

        templateDO = DataObject.find(template);
        final Set<DataObject> result = templateWizard.instantiate(templateDO, targetFolder);
        final String prefix = (String) templateWizard.getProperty("selectedPrefix"); //NOI18N
        if (result != null && result.size() > 0) {
            final String compName = result.iterator().next().getName();
            final BaseDocument doc = (BaseDocument) document;
            final Indent indent = Indent.get(doc);
            indent.lock();
            try {
                doc.runAtomic(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doc.remove(startOffset, endOffset - startOffset);
                            String text = "<" + prefix + ":" + compName + "/>"; //NOI18N
                            doc.insertString(startOffset, text, null);
                            indent.reindent(startOffset, startOffset + text.length());
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            } finally {
                indent.unlock();
            }

            // get component folder
            FileObject tF = Templates.getTargetFolder(templateWizard);
            String compFolder = FileUtil.getRelativePath(projectDir, tF);
            compFolder = compFolder.substring(compFolder.lastIndexOf("/") + 1);

            // issue #232189 - Composite component's NS declaration not inserted on first usage
            FileObject generatedFO = result.iterator().next().getPrimaryFile();
            if (generatedFO != null) {
                WebModule webModule = WebModule.getWebModule(generatedFO);
                if (webModule != null && webModule.getDocumentBase() != null) {
                    IndexingManager.getDefault().refreshIndexAndWait(
                            webModule.getDocumentBase().toURL(),
                            // issue #225974 - refresh the source FO to get hints
                            Arrays.asList(generatedFO.toURL(), source.getFileObject().toURL()));
                }
            }

            //now we need to import the library if not already done,
            //but since the library has just been created by adding an xhtml file
            //to the resources/xxx/ folder we need to wait until the files
            //get indexed and the library is created
            final String compositeLibURL = LibraryUtils.getCompositeLibraryURL(compFolder, jsfs.isJsf22Plus());
            Source documentSource = Source.create(document);
            ParserManager.parseWhenScanFinished(Collections.singletonList(documentSource), new UserTask() { //NOI18N
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Library lib = jsfs.getLibrary(compositeLibURL);
                    if (lib != null) {
                        if (!LibraryUtils.importLibrary(document, lib, prefix, jsfs.isJsf22Plus())) { //XXX: fix the damned static prefix !!!
                            logger.log(Level.WARNING, "Cannot import composite components library {0}", compositeLibURL); //NOI18N
                        }
                    } else {
                        //error
                        logger.log(Level.WARNING, "Composite components library for uri {0} seems not to be created.", compositeLibURL); //NOI18N
                    }
                }
            });

            //now we need to import all the namespaces refered in the snipet
            DataObject templateInstance = result.iterator().next();
            final EditorCookie ec = templateInstance.getLookup().lookup(EditorCookie.class);
            final Document templateInstanceDoc = ec.openDocument();
            ParserManager.parseWhenScanFinished(Collections.singletonList(documentSource), new UserTask() { //NOI18N
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    final Map<Library, String> importsMap = new LinkedHashMap<>();
                    for (Map.Entry<String, String> entry : context.getDeclarations().entrySet()) {
                        String uri = entry.getKey();
                        String prefix = entry.getValue();
                        Library lib = jsfs.getLibrary(uri);
                        if (lib != null) {
                            importsMap.put(lib, prefix);
                        }
                    }
                    //do the import under atomic lock in different thread,
                    RequestProcessor.getDefault().post(new Runnable() {
                        @Override
                        public void run() {
                            ((BaseDocument) templateInstanceDoc).runAtomic(new Runnable() {
                                @Override
                                public void run() {
                                    LibraryUtils.importLibrary(templateInstanceDoc, importsMap, jsfs.isJsf22Plus());
                                }
                            });
                            try {
                                ec.saveDocument(); //save the template instance after imports
                            } catch (IOException ioe) {
                                Exceptions.printStackTrace(ioe);
                            }
                        }
                    });
                }
            });
        }
    }

    private static SnippetContext getSnippetContext(Document doc, final int from, final int to, final JsfSupportImpl jsfs) {
        final SnippetContext context = new SnippetContext();
        context.setValid(true);

        final Source source = Source.create(doc);
        try {
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    final HtmlParserResult result = (HtmlParserResult) JsfUtils.getEmbeddedParserResult(resultIterator, "text/html"); //NOI18N
                    if (result == null) {
                        return;
                    }
                    final int astFrom = result.getSnapshot().getEmbeddedOffset(from);
                    final int astTo = result.getSnapshot().getEmbeddedOffset(to);

                    try {
                        for (final String libUri : result.getNamespaces().keySet()) {
                            //is the declared uri a faceler library?
                            if (jsfs.getLibrary(libUri) == null) {
                                continue; //no facelets stuff, skip it
                            }
                            Node root = result.root(libUri);
                            ElementUtils.visitChildren(
                                    root,
                                    new SnippetContextVisitor(astFrom, astTo, result, context, libUri),
                                    ElementType.OPEN_TAG);
                        }
                    } catch (AstTreeVisitingBreakException e) {
                        //no-op, we just need to stop the visition once we find first problem
                        context.setValid(false);
                    }
                }
            });

        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return context;
    }
    private static final Rule injectCCRule = new InjectCCSelectionRule();

    private static class AstTreeVisitingBreakException extends RuntimeException {
    };

    private static class SnippetContext {

        private boolean valid;
        private Map<String, String> relatedDeclarations = new HashMap<>();

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public void addDeclaration(String uri, String prefix) {
            relatedDeclarations.put(uri, prefix);
        }

        /**
         * uri2prefix map of related declarations
         */
        public Map<String, String> getDeclarations() {
            return relatedDeclarations;
        }

        public boolean isValid() {
            return valid;
        }
    }

    private static class InjectCCSelectionRule implements SelectionRule {

        @Override
        public boolean appliesTo(RuleContext context) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return null;
        }

        @Override
        public boolean showInTasklist() {
            return false;
        }

        @Override
        public HintSeverity getDefaultSeverity() {
            return HintSeverity.CURRENT_LINE_WARNING; //???
        }
    }

    public static class InjectCCCodeGen implements CodeGenerator {

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(InjectCompositeComponent.class, "MSG_InjectCompositeComponentHint"); //NOI18N
        }

        @Override
        public void invoke() {
            JTextComponent textComponent = EditorRegistry.lastFocusedComponent();
            Document doc = textComponent.getDocument();
            int from = textComponent.getSelectionStart();
            int to = textComponent.getSelectionEnd();

            inject(doc, from, to);

        }
    }

    private static class SnippetContextVisitor implements ElementVisitor {

        private final int astFrom;
        private final int astTo;
        private final HtmlParserResult result;
        private final SnippetContext context;
        private final String libUri;

        public SnippetContextVisitor(int astFrom, int astTo, HtmlParserResult result, SnippetContext context, String libUri) {
            this.astFrom = astFrom;
            this.astTo = astTo;
            this.result = result;
            this.context = context;
            this.libUri = libUri;
        }

        @Override
        public void visit(Element node) {
            OpenTag ot = (OpenTag) node;
            int node_logical_from = ot.from();
            int node_logical_to = ot.semanticEnd();

            if (node_logical_from >= astFrom && node_logical_from <= astTo
                    || node_logical_to >= astFrom && node_logical_to <= astTo) {
                //the node start or end is in the selection
                //such info is enough to add the node's namespace
                //to the list of future imports for the snippet
                context.addDeclaration(libUri, result.getNamespaces().get(libUri));
            }

            //todo: optimize me a bit please :-)
            //the node must either contain both offsets or none
            if ((astFrom > node_logical_from && astFrom < node_logical_to && !(astTo > node_logical_from && astTo < node_logical_to))
                    || (astTo > node_logical_from && astTo < node_logical_to && !(astFrom > node_logical_from && astFrom < node_logical_to))) {
                //crossing tags - quit
                fail();
            }

            //and the offset must not fall into the tag itself
            CloseTag closeTag = ot.matchingCloseTag();
            if (closeTag == null) {
                //broken source, error
                if (!ot.isEmpty()) {
                    fail();
                }
            }
            if (isInTagItself(node, astFrom) || isInTagItself(node, astTo)
                    || isInTagItself(closeTag, astFrom) || isInTagItself(closeTag, astTo)) {
                fail();
            }

        }

        private boolean isInTagItself(Element node, int offset) {
            return node != null && node.from() < offset && node.to() > offset;
        }

        private void fail() {
            context.setValid(false);
            throw new AstTreeVisitingBreakException();
        }
    }
}
