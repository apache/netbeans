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

package org.netbeans.modules.csl.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.JSeparator;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.lexer.Language;
import org.netbeans.lib.editor.codetemplates.CodeTemplateCompletionProvider;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.modules.csl.editor.codetemplates.GsfCodeTemplateFilter;
import org.netbeans.modules.csl.editor.codetemplates.GsfCodeTemplateProcessor;
import org.netbeans.modules.csl.editor.completion.GsfCompletionProvider;
import org.netbeans.modules.csl.editor.fold.GsfFoldManagerFactory;
import org.netbeans.modules.csl.editor.hyperlink.GsfHyperlinkProvider;
import org.netbeans.modules.csl.editor.semantic.HighlightsLayerFactoryImpl;
import org.netbeans.modules.csl.editor.semantic.OccurrencesMarkProviderCreator;
import org.netbeans.modules.csl.hints.GsfUpToDateStateProviderFactory;
import org.netbeans.modules.csl.navigation.ClassMemberPanel;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProviderCreator;
import org.netbeans.modules.editor.indent.spi.IndentTask;
import org.netbeans.modules.editor.indent.spi.ReformatTask;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProviderFactory;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author vita
 */
@ServiceProvider(service=Processor.class)
@SupportedAnnotationTypes("org.netbeans.modules.csl.spi.LanguageRegistration") //NOI18N
public class LanguageRegistrationProcessor extends LayerGeneratingProcessor {

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        for(Element e : roundEnv.getElementsAnnotatedWith(LanguageRegistration.class)) {
            TypeElement cls = (TypeElement) e;
            LanguageRegistration languageRegistration = cls.getAnnotation(LanguageRegistration.class);
            String [] mimeTypes = languageRegistration.mimeType();
            if (mimeTypes == null || mimeTypes.length == 0) {
            }
            for(String mimeType : mimeTypes) {
                if (!MimePath.validate(mimeType)) {
                    throw new LayerGenerationException("Invalid mime type: '" + mimeType + "'", cls); //NOI18N
                }
            }

            TypeElement dlc = processingEnv.getElementUtils().getTypeElement("org.netbeans.modules.csl.spi.DefaultLanguageConfig"); //NOI18N
            if (!processingEnv.getTypeUtils().isSubtype(cls.asType(), dlc.asType())) {
                throw new LayerGenerationException("Class " + cls + " is not subclass of " + dlc, e); //NOI18N
            }

            boolean isAnnotatedByPathRecognizerRegistration = false;
            TypeElement prr = processingEnv.getElementUtils().getTypeElement("org.netbeans.modules.parsing.spi.indexing.PathRecognizerRegistration"); //NOI18N
            for(AnnotationMirror am : cls.getAnnotationMirrors()) {
                if (am.getAnnotationType().asElement().equals(prr)) {
                    isAnnotatedByPathRecognizerRegistration = true;
                    break;
                }
            }

            List<ExecutableElement> methodsList = ElementFilter.methodsIn(cls.getEnclosedElements());
            Map<String, ExecutableElement> methods = new HashMap<String, ExecutableElement>(methodsList.size());
            for(ExecutableElement m : methodsList) {
                methods.put(m.getSimpleName().toString(), m);
            }

            final LayerBuilder lb = layer(cls);

            for(String mimeType : mimeTypes) {
                registerCslPlugin(lb, mimeType, cls, languageRegistration.useMultiview());
                registerTLIndexer(lb, mimeType);

                // for some reason the structure scanner registration in CslJar was done always, no matter
                // of useCustomEditorKit value
                if (methods.containsKey("getStructureScanner")) { //NOI18N
                    registerStructureScanner(lb, mimeType);
                }

                if (!languageRegistration.useCustomEditorKit()) {
                    registerEditorKit(lb, mimeType);
                    registerLoader(lb, mimeType);
                    if (methods.containsKey("getLexerLanguage")) { //NOI18N
                        registerLexer(lb, mimeType);
                    }
                    if (methods.containsKey("getParser")) { //NOI18N
                        registerParser(lb, mimeType);
                    }
                        if (methods.containsKey("getIndexerFactory")) { //NOI18N
                        registerIndexer(lb, mimeType);
                        if (!isAnnotatedByPathRecognizerRegistration) {
                            registerPathRecognizer(lb, mimeType);
                        }
                    }
                    registerCodeCompletion(lb, mimeType);
                    registerCodeFolding(lb, mimeType);
                    registerCodeTemplates(lb, mimeType);
                    if (methods.containsKey("getDeclarationFinder")) { //NOI18N
                        registerHyperlinks(lb, mimeType);
                    }
                    registerSemanticHighlighting(lb, mimeType);
                    registerUpToDateStatus(lb, mimeType);
                    registerContextMenu(lb, mimeType, methods);
                    registerCommentUncommentToolbarButtons(lb, mimeType);
                    if (methods.containsKey("getFormatter")) { //NOI18N
                        registerFormatterIndenter(lb, mimeType);
                    }
                }
            }
        }
        
        return true;
    }

    private static File instanceFile(LayerBuilder b, String folder, String name, Class implClass, String factoryMethod, Class... instanceOf) {
        return instanceFile(b, folder, name, implClass, factoryMethod, null, instanceOf);
    }
    
    private static File instanceFile(LayerBuilder b, String folder, String name, Class implClass, String factoryMethod, Integer position, Class... instanceOf) {
        return instanceFile(b, folder, name, implClass == null ? null : implClass.getName(), factoryMethod, position, instanceOf);
    }
    
    private static File instanceFile(LayerBuilder b, String folder, String name, String implClass, String factoryMethod, Class... instanceOf) {
        return instanceFile(b, folder, name, implClass, factoryMethod, null, instanceOf);
    }
    
    private static File instanceFile(LayerBuilder b, String folder, String name, String implClass, String factoryMethod, Integer position, Class... instanceOf) {
        String basename;
        if (name == null) {
            basename = implClass.replace('.', '-'); //NOI18N
            if (factoryMethod != null) {
                basename += "-" + factoryMethod; //NOI18N
            }
        } else {
            basename = name;
        }
        
        File f = b.file(folder + "/" + basename + ".instance"); //NOI18N
        if (implClass != null) {
            if (factoryMethod != null) {
                f.methodvalue("instanceCreate", implClass, factoryMethod); //NOI18N
            } else {
                f.stringvalue("instanceClass", implClass); //NOI18N
            }
        }
        
        for(Class c : instanceOf) {
            f.stringvalue("instanceOf", c.getName()); //NOI18N
        }
        
        if (position != null) {
            f.intvalue("position", position); //NOI18N
        }

        return f;
    }

    private static void registerCslPlugin(LayerBuilder b, String mimeType, TypeElement language, boolean useMultiview) throws LayerGenerationException {
        File f = b.folder("CslPlugins/" + mimeType); //NOI18N
        f.intvalue("genver", 2); //NOI18N
        f.write();
        
        f.boolvalue("useMultiview", useMultiview); //NOI18N
        f.write();
        
        f = instanceFile(b, "CslPlugins/" + mimeType, "language", (String) null, null); //NOI18N
        f.stringvalue("instanceClass", language.getQualifiedName().toString()); //NOI18N
        f.write();
    }

    private static void registerLoader(LayerBuilder b, String mimeType) throws LayerGenerationException {
//        File f = b.file("Loaders/" + mimeType + "/Factories/org-netbeans-modules-csl-GsfDataLoader.instance"); //NOI18N
        File f = instanceFile(b, "Loaders/" + mimeType + "/Factories", null, GsfDataLoader.class, null); //NOI18N
        f.position(89998);
        f.write();
    }

    private static void registerPathRecognizer(LayerBuilder b, String mimeType) throws LayerGenerationException {
        File f = instanceFile(b,
                "Services/Hidden/PathRecognizers", //NOI18N
                "org-netbeans-modules-csl-core-PathRecognizerImpl-" + makeFilesystemName(mimeType), //NOI18N
                PathRecognizerImpl.class,
                "createInstance", //NOI18N
                PathRecognizer.class);
//        f.methodvalue("instanceCreate", "org.netbeans.modules.csl.core.PathRecognizerImpl", "createInstance"); //NOI18N
        f.stringvalue("mimeType", mimeType); //NOI18N
        f.write();
    }

    private static void registerParser(LayerBuilder b, String mimeType) {
        instanceFile(b, "Editors/" + mimeType, null, GsfParserFactory.class, "create", ParserFactory.class).write(); //NOI18N

//        // Parser factory
//        item = createFile(doc, mimeFolder, "org-netbeans-modules-csl-core-GsfParserFactory.instance"); // NOI18N
//        setFileAttribute(doc, item, "instanceCreate", METHODVALUE, "org.netbeans.modules.csl.core.GsfParserFactory.create"); //NOI18N
//        setFileAttribute(doc, item, "instanceOf", STRINGVALUE, "org.netbeans.modules.parsing.spi.ParserFactory"); //NOI18N
    }

    private static void registerIndexer(LayerBuilder b, String mimeType) {
        instanceFile(b, "Editors/" + mimeType, null, EmbeddingIndexerFactoryImpl.class, "create", EmbeddingIndexerFactory.class).write(); //NOI18N
//
//        // Indexer factory
//        item = createFile(doc, mimeFolder, "org-netbeans-modules-csl-core-EmbeddingIndexerFactoryImpl.instance"); // NOI18N
//        setFileAttribute(doc, item, "instanceCreate", METHODVALUE, "org.netbeans.modules.csl.core.EmbeddingIndexerFactoryImpl.create"); //NOI18N
//        setFileAttribute(doc, item, "instanceOf", STRINGVALUE, "org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory"); //NOI18N
    }

    private static void registerTLIndexer(LayerBuilder b, String mimeType) {
        instanceFile(b, "Editors/" + mimeType, null, TLIndexerFactory.class, null, EmbeddingIndexerFactory.class).write(); //NOI18N
//                // TL Indexer factory
//                Element mimeFolder = mkdirs(doc, "Editors/" + mimeType); // NOI18N
//                Element item = createFile(doc, mimeFolder, "org-netbeans-modules-csl-core-TLIndexerFactory.instance"); // NOI18N
//                setFileAttribute(doc, item, "instanceOf", "stringvalue", "org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory"); //NOI18N
    }

    private static void registerCodeCompletion(LayerBuilder b, String mimeType) {
        instanceFile(b, "Editors/" + mimeType + "/CompletionProviders", null, CodeTemplateCompletionProvider.class, null).write(); //NOI18N
        instanceFile(b, "Editors/" + mimeType + "/CompletionProviders", null, GsfCompletionProvider.class, null).write(); //NOI18N
        instanceFile(b, "Editors/" + mimeType + "/CompletionProviders", null, "org.netbeans.modules.parsing.ui.WaitScanFinishedCompletionProvider", null).write(); //NOI18N
//        // Code Completion
//        Element completionFolder = mkdirs(doc, "Editors/" + mimeType + "/CompletionProviders"); // NOI18N
//        createFile(doc, completionFolder, "org-netbeans-lib-editor-codetemplates-CodeTemplateCompletionProvider.instance"); // NOI18N
//        createFile(doc, completionFolder, "org-netbeans-modules-csl-editor-completion-GsfCompletionProvider.instance"); // NOI18N
    }

    private static void registerCodeFolding(LayerBuilder b, String mimeType) {
        instanceFile(b, "Editors/" + mimeType + "/FoldManager", null, GsfFoldManagerFactory.class, null).
                intvalue("position", 900).write(); //NOI18N
//
//        // Code Folding
//        if (hasStructureScanner) {
//            Element sideBarFolder = mkdirs(doc, "Editors/" + mimeType + "/SideBar"); // NOI18N
//            Element sidebarFile = createFile(doc, sideBarFolder, "org-netbeans-modules-csl-editor-GsfCodeFoldingSideBarFactory.instance"); // NOI18N
//            setFileAttribute(doc, sidebarFile, "position", INTVALUE, "1200"); // NOI18N
//
//            Element foldingFolder = mkdirs(doc, "Editors/" + mimeType + "/FoldManager"); // NOI18N
//            createFile(doc, foldingFolder, "org-netbeans-modules-csl-editor-fold-GsfFoldManagerFactory.instance"); // NOI18N
//        }
    }

    private static void registerCodeTemplates(LayerBuilder b, String mimeType) {
        instanceFile(b, "Editors/" + mimeType + "/CodeTemplateProcessorFactories", null, GsfCodeTemplateProcessor.Factory.class, null).write(); //NOI18N
        instanceFile(b, "Editors/" + mimeType + "/CodeTemplateFilterFactories", null, GsfCodeTemplateFilter.Factory.class, null).write(); //NOI18N
//
//        // Code Templates
//        Element codeProcessorFolder = mkdirs(mimeFolder, "CodeTemplateProcessorFactories"); // NOI18N
//        item = createFile(doc, codeProcessorFolder, "org-netbeans-modules-csl-editor-codetemplates-GsfCodeTemplateProcessor$Factory.instance"); // NOI18N
//
//        // Code Template Filters
//        Element codeFilter = mkdirs(mimeFolder, "CodeTemplateFilterFactories"); // NOI18N
//        item = createFile(doc, codeFilter, "org-netbeans-modules-csl-editor-codetemplates-GsfCodeTemplateFilter$Factory.instance"); // NOI18N
    }

    private static void registerHyperlinks(LayerBuilder b, String mimeType) {
        instanceFile(b, "Editors/" + mimeType + "/HyperlinkProviders", null, GsfHyperlinkProvider.class, null, 1000, HyperlinkProviderExt.class).write(); //NOI18N
//
//        // Hyperlinks
//        if (hasDeclarationFinder) {
//            Element hyperlinkFolder = mkdirs(doc, "Editors/" + mimeType + "/HyperlinkProviders"); // NOI18N
//            Element file = createFile(doc, hyperlinkFolder, "GsfHyperlinkProvider.instance"); // NOI18N
//            setFileAttribute(doc, file, "instanceClass", STRINGVALUE, "org.netbeans.modules.csl.editor.hyperlink.GsfHyperlinkProvider"); // NOI18N
//            setFileAttribute(doc, file, "instanceOf", STRINGVALUE, "org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt"); // NOI18N
//        }
    }

    private static void registerSemanticHighlighting(LayerBuilder b, String mimeType) {
        instanceFile(b, "Editors/" + mimeType, null, HighlightsLayerFactoryImpl.class, null, HighlightsLayerFactory.class).write(); //NOI18N
//
//        // Highlighting Factories
//        item = createFile(doc, mimeFolder, "org-netbeans-modules-csl-editor-semantic-HighlightsLayerFactoryImpl.instance"); // NOI18N
    }

    private void registerStructureScanner(LayerBuilder b, String mimeType) {
        instanceFile(b, "Navigator/Panels/" + mimeType, null, ClassMemberPanel.class, null).intvalue("position", 1000).write(); //NOI18N
        File sideBar = instanceFile(b, "Editors/" + mimeType + "/SideBar", null, "org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsController", "createSideBarFactory");
        sideBar.stringvalue("location", "South")
               .intvalue("position", 5238)
               .boolvalue("scrollable", false)
               .write();
//
//        Element navigatorFolder = mkdirs(doc, "Navigator/Panels/" + mimeType); // NOI18N
//        createFile(doc, navigatorFolder, "org-netbeans-modules-csl-navigation-ClassMemberPanel.instance"); // NOI18N
    }

    private static void registerUpToDateStatus(LayerBuilder b, String mimeType) {
        instanceFile(b, "Editors/" + mimeType + "/UpToDateStatusProvider", null, GsfUpToDateStateProviderFactory.class, null, UpToDateStatusProviderFactory.class).write(); //NOI18N
        instanceFile(b, "Editors/" + mimeType + "/UpToDateStatusProvider", null, OccurrencesMarkProviderCreator.class, null, MarkProviderCreator.class).write(); //NOI18N
//
//        // UpToDateStatusProviders
//        Element upToDateFolder = mkdirs(doc, "Editors/" + mimeType + "/UpToDateStatusProvider"); // NOI18N
//        item = createFile(doc, upToDateFolder, "org-netbeans-modules-csl-hints-GsfUpToDateStateProviderFactory.instance"); // NOI18N
//        item = createFile(doc, upToDateFolder, "org-netbeans-modules-csl-editor-semantic-OccurrencesMarkProviderCreator.instance"); // NOI18N
    }

    private static void registerContextMenu(LayerBuilder b, String mimeType, Map<String, ExecutableElement> methods) {
        File f;

// XXX: removed due to #180501, CSL pluguns now ought to reguster this action manually; this is
// to give plugins control over what action (in-place-refactoring vs. full rename refactorig) is registered
//        if (methods.containsKey("getInstantRenamer")) { //NOI18N
//            f = b.file("Editors/" + mimeType + "/Popup/in-place-refactoring"); //NOI18N
//            f.position(680);
//            f.write();
//        }

//
//        Element mimeFolder = mkdirs(doc, "Editors/" + mimeType); // NOI18N
//
//        // Context menu
//        Element popupFolder = mkdirs(doc, "Editors/" + mimeType + "/Popup"); // NOI18N
//
//        Element renameFile = createFile(doc, popupFolder, "in-place-refactoring"); // NOI18N
//        setFileAttribute(doc, renameFile, "position", INTVALUE, "680"); // NOI18N

        f = b.folder("Editors/" + mimeType + "/Popup/goto"); //NOI18N
        f.position(500);
        f.bundlevalue("displayName", "org.netbeans.modules.csl.core.Bundle", "generate-goto-popup"); //NOI18N
        f.write();
//
//        boolean alreadyLocalized = false;
//        boolean alreadyPositioned = false;
//        List<Element> gotoAttributes = getAttributeElements(doc, "Editors/" + mimeType + "/Popup/goto"); // NOI18N
//        for (Element gotoAttribute : gotoAttributes) {
//            if (gotoAttribute.getAttribute(FILENAME).equals("SystemFileSystem.localizingBundle") || //NOI18N
//                gotoAttribute.getAttribute(FILENAME).equals("displayName") //NOI18N
//            ) {
//                alreadyLocalized = true;
//            }
//            if (gotoAttribute.getAttribute(FILENAME).equals("position")) { // NOI18N
//                alreadyPositioned = true;
//            }
//        }
//
//        Element gotoFolder = findPath(mimeFolder, "Popup/goto");
//        if (gotoFolder == null) {
//            gotoFolder = mkdirs(mimeFolder, "Popup/goto"); // NOI18N
//        }
//        if (!alreadyPositioned) {
//            setFileAttribute(doc, gotoFolder, "position", INTVALUE, "500"); // NOI18N
//        }
//
//        if (!alreadyLocalized) {
//            setFileAttribute(doc, gotoFolder, "displayName", BUNDLEVALUE, "org.netbeans.modules.csl.core.Bundle#generate-goto-popup");
//        }

        if (methods.containsKey("getDeclarationFinder")) { //NOI18N
            f = b.file("Editors/" + mimeType + "/Popup/goto/goto-declaration"); //NOI18N
            f.position(500);
            f.write();
        }
//
//        Element item;
//        if (hasDeclarationFinder) {
//            item = createFile(doc, gotoFolder, "goto-declaration"); // NOI18N
//            setFileAttribute(doc, item, "position", INTVALUE, "500"); // NOI18N
//        }

        f = b.file("Editors/" + mimeType + "/Popup/goto/goto"); //NOI18N
        f.position(600);
        f.write();
//
//        // Goto by linenumber
//        item = createFile(doc, gotoFolder, "goto");  // NOI18N
//        setFileAttribute(doc, item, "position", INTVALUE, "600"); // NOI18N
//
//        // What about goto-source etc?
//        // TODO: Goto Type (integrate with Java's GotoType)
//

        f = instanceFile(b, "Editors/" + mimeType + "/Popup", "SeparatorBeforeCut", JSeparator.class, null); //NOI18N
        f.position(1200);
        f.write();
//        item = createFile(doc, popupFolder, "SeparatorBeforeCut.instance"); // NOI18N
//        setFileAttribute(doc, item, "position", INTVALUE, "1200"); // NOI18N
//        setFileAttribute(doc, item, "instanceClass", STRINGVALUE, "javax.swing.JSeparator"); // NOI18N
//

        f = b.file("Editors/" + mimeType + "/Popup/format"); //NOI18N
        f.position(750);
        f.write();
//        item = createFile(doc, popupFolder, "format"); // NOI18N
//        setFileAttribute(doc, item, "position", INTVALUE, "750"); // NOI18N
//

        f = instanceFile(b, "Editors/" + mimeType + "/Popup", "SeparatorAfterFormat", JSeparator.class, null); //NOI18N
        f.position(780);
        f.write();
//        item = createFile(doc, popupFolder, "SeparatorAfterFormat.instance"); // NOI18N
//        // Should be between org-openide-actions-PasteAction.instance and format
//        setFileAttribute(doc, item, "position", INTVALUE, "780"); // NOI18N
//        setFileAttribute(doc, item, "instanceClass", STRINGVALUE, "javax.swing.JSeparator"); // NOI18N
    }

    private static void registerCommentUncommentToolbarButtons(LayerBuilder b, String mimeType) {
        File f = instanceFile(b, "Editors/" + mimeType + "/Toolbars/Default", "Separator-before-comment", JSeparator.class, null); //NOI18N
        f.position(30000);
        f.write();

        f = b.file("Editors/" + mimeType + "/Toolbars/Default/comment"); //NOI18N
        f.position(30100);
        f.write();

        f = b.file("Editors/" + mimeType + "/Toolbars/Default/uncomment"); //NOI18N
        f.position(30200);
        f.write();
//
//        // Toolbar
//        if (linePrefix != null && linePrefix.length() > 0) {
//            // Yes, found line comment prefix - register comment/uncomment toolbar buttons!
//            Element toolbarFolder = mkdirs(mimeFolder, "Toolbars/Default"); // NOI18N
//
//            item = createFile(doc, toolbarFolder, "Separator-before-comment.instance"); // NOI18N
//            setFileAttribute(doc, item, "instanceClass", STRINGVALUE, "javax.swing.JSeparator"); // NOI18N
//            setFileAttribute(doc, item, "position", INTVALUE, "30000"); // NOI18N
//
//            item = createFile(doc, toolbarFolder, "comment"); // NOI18N
//            setFileAttribute(doc, item, "position", INTVALUE, "30100"); // NOI18N
//
//            item = createFile(doc, toolbarFolder, "uncomment"); // NOI18N
//            setFileAttribute(doc, item, "position", INTVALUE, "30200"); // NOI18N
//        }
    }

    private static void registerEditorKit(LayerBuilder b, String mimeType) {
        instanceFile(b, "Editors/" + mimeType, null, CslEditorKit.class, "createEditorKitInstance", EditorKit.class).write(); //NOI18N
    }

    private static void registerLexer(LayerBuilder b, String mimeType) {
        instanceFile(b, "Editors/" + mimeType, null, CslEditorKit.class, "createLexerLanguageInstance", Language.class).write(); //NOI18N
    }

    private static void registerFormatterIndenter(LayerBuilder b, String mimeType) {
        instanceFile(b, "Editors/" + mimeType, null, GsfReformatTaskFactory.class, null, ReformatTask.Factory.class).write(); //NOI18N
        instanceFile(b, "Editors/" + mimeType, null, GsfIndentTaskFactory.class, null, IndentTask.Factory.class).write(); //NOI18N
    }

    private static String makeFilesystemName(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for(int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            } else {
                sb.append("-"); //NOI18N
            }
        }
        return sb.toString();
    }
}
