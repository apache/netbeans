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
package org.netbeans.modules.groovy.editor.completion.provider;

import groovy.lang.MetaMethod;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.swing.text.Document;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.reflection.CachedClass;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.completion.CaretLocation;
import org.netbeans.modules.groovy.editor.api.completion.CompletionHandler;
import static org.netbeans.modules.groovy.editor.api.completion.CompletionHandler.getMethodSignature;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext;
import org.netbeans.modules.groovy.editor.api.completion.util.ContextHelper;
import org.netbeans.modules.groovy.editor.api.completion.util.DotCompletionContext;
import org.netbeans.modules.groovy.editor.api.elements.ast.ASTMethod;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
import org.netbeans.modules.groovy.editor.completion.ProposalsCollector;
import org.netbeans.modules.groovy.editor.java.JavaElementHandle;
import org.netbeans.modules.groovy.support.api.GroovySettings;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

/**
 * Code migrated from {@link CompletionHandler} to a nonpublic package. Allows to extend
 * internal module API without exposing details to dependent modules.
 * 
 * @author sdedic
 */
@ServiceProvider(service = GroovyCompletionImpl.class)
public class GroovyCompletionImpl {
    private static final Logger LOG = Logger.getLogger(GroovyCompletionImpl.class.getName());
    private final PropertyChangeListener docListener;
    
    private String jdkJavaDocBase = null;
    private String groovyJavaDocBase = null;
    private String groovyApiDocBase = null;
    

    public GroovyCompletionImpl() {
        JavaPlatformManager platformMan = JavaPlatformManager.getDefault();
        JavaPlatform platform = platformMan.getDefaultPlatform();
        List<URL> docfolder = platform.getJavadocFolders();

        for (URL url : docfolder) {
            LOG.log(Level.FINEST, "JDK Doc path: {0}", url.toString()); // NOI18N
            jdkJavaDocBase = url.toString();
        }

        GroovySettings groovySettings = GroovySettings.getInstance();
        docListener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                synchronized (GroovyCompletionImpl.this) {
                    groovyJavaDocBase = null;
                    groovyApiDocBase = null;
                }
            }
        };
        groovySettings.addPropertyChangeListener(WeakListeners.propertyChange(docListener, this));
    }
    
    
    public static final class CompletionImplResult {
        List<CompletionProposal> proposals;
        CompletionContext groovyContext;

        public CompletionImplResult(List<CompletionProposal> proposals, CompletionContext groovyContext) {
            this.proposals = proposals;
            this.groovyContext = groovyContext;
        }

        public List<CompletionProposal> getProposals() {
            return proposals;
        }

        public CompletionContext getGroovyContext() {
            return groovyContext;
        }
        
        public boolean isEmpty() {
            return proposals == null || proposals.isEmpty();
        }
    }

    public CompletionImplResult makeProposals(CodeCompletionContext completionContext) {
        ParserResult parserResult = completionContext.getParserResult();
        String prefix = completionContext.getPrefix();
        
        // Documentation says that @NonNull is return from getPrefix() but it's not true
        // Invoking "this.^" makes the return value null
        if (prefix == null) {
            prefix = "";
        }
        
        int lexOffset = completionContext.getCaretOffset();
        int astOffset = ASTUtils.getAstOffset(parserResult, lexOffset);
        int anchor = lexOffset - prefix.length();

        LOG.log(Level.FINEST, "complete(...), prefix      : {0}", prefix); // NOI18N
        LOG.log(Level.FINEST, "complete(...), lexOffset   : {0}", lexOffset); // NOI18N
        LOG.log(Level.FINEST, "complete(...), astOffset   : {0}", astOffset); // NOI18N

        final Document document = parserResult.getSnapshot().getSource().getDocument(false);
        if (document == null) {
            return null;
        }
        final BaseDocument doc = (BaseDocument) document;

        doc.readLock(); // Read-lock due to Token hierarchy use

        try {
            CompletionContext context = new CompletionContext(parserResult, prefix, anchor, lexOffset, astOffset, doc);
            context.init();

            // if we are above a package statement or inside a comment there's no completion at all.
            if (context.location == CaretLocation.ABOVE_PACKAGE || context.location == CaretLocation.INSIDE_COMMENT) {
                return new CompletionImplResult(Collections.emptyList(), context);
            }
            
            ProposalsCollector proposalsCollector = new ProposalsCollector(context);

            if (ContextHelper.isVariableNameDefinition(context) || ContextHelper.isFieldNameDefinition(context)) {
                proposalsCollector.completeNewVars(context);
            } else {
                ClassNode realClass = null;
                if (context.rawDseclaringClass != null && context.rawDseclaringClass.getName().equals("java.lang.Class")) { // NOI18N
                    if (context.rawDseclaringClass.getGenericsTypes().length == 1) {
                        realClass = context.rawDseclaringClass.getGenericsTypes()[0].getType();
                        context.setAddSortOverride(510);
                    }
                }
                makeClassProposals(proposalsCollector, context);
                // implicit conversion of GString > String:
                if (context.declaringClass != null && context.declaringClass.getName().equals("groovy.lang.GString")) { // NOI18N
                    // add String methods in addition to GString ones.
                    ClassNode sn = ((GroovyParserResult)parserResult).resolveClassName("java.lang.String"); // NOI18N
                    if (sn != null) {
                        context.setDeclaringClass(sn, false);
                        makeClassProposals(proposalsCollector, context);
                    }
                }
                if (realClass != null) { // NOI18N
                    context.setAddSortOverride(0);
                    context.setDeclaringClass(realClass, true);
                    context.init();
                    makeClassProposals(proposalsCollector, context);
                }
            }
            proposalsCollector.completeCamelCase(context);

            return new CompletionImplResult(proposalsCollector.getCollectedProposals(), context);
        } finally {
            doc.readUnlock();
        }
    }
    
    private void makeClassProposals(ProposalsCollector proposalsCollector, CompletionContext context) {
        
        if (context.location == CaretLocation.INSIDE_PACKAGE) {
            proposalsCollector.completePackages(context);
            return;
        }
        
        if (!(context.location == CaretLocation.OUTSIDE_CLASSES || context.location == CaretLocation.INSIDE_STRING)) {
            proposalsCollector.completePackages(context);
            proposalsCollector.completeTypes(context);
        }

        if (!context.isBehindImportStatement()) {
            if (context.location != CaretLocation.INSIDE_STRING) {
                proposalsCollector.completeKeywords(context);
                proposalsCollector.completeMethods(context);
            }

            proposalsCollector.completeFields(context);
            proposalsCollector.completeLocalVars(context);
        }

        if (context.location == CaretLocation.INSIDE_CONSTRUCTOR_CALL) {
            if (ContextHelper.isAfterComma(context) || ContextHelper.isAfterLeftParenthesis(context)) {
                proposalsCollector.completeNamedParams(context);
            }
        }
    }

    private String getGroovyJavadocBase() {
        synchronized (this) {
            if (groovyJavaDocBase == null) {
                String docroot = GroovySettings.getInstance().getGroovyDoc() + "/"; // NOI18N
                groovyJavaDocBase = directoryNameToUrl(docroot + "groovy-jdk/"); // NOI18N
            }
            return groovyJavaDocBase;
        }
    }

    private String getGroovyApiDocBase() {
        synchronized (this) {
            if (groovyApiDocBase == null) {
                String docroot = GroovySettings.getInstance().getGroovyDoc() + "/"; // NOI18N
                groovyApiDocBase = directoryNameToUrl(docroot + "gapi/"); // NOI18N
            }
            return groovyApiDocBase;
        }
    }

    private static String directoryNameToUrl(String dirname) {
        if (dirname == null) {
            return "";
        }

        // FIXME use FileObject (?)
        File dirFile = new File(dirname);

        if (dirFile != null && dirFile.exists() && dirFile.isDirectory()) {
            String fileURL = "";
            if (Utilities.isWindows()) {
                dirname = dirname.replace("\\", "/");
                fileURL = "file:/"; // NOI18N
            } else {
                fileURL = "file://"; // NOI18N
            }
            return fileURL + dirname;
        } else {
            return "";
        }
    }

    private static void printMethod(MetaMethod mm) {

        LOG.log(Level.FINEST, "--------------------------------------------------");
        LOG.log(Level.FINEST, "getName()           : {0}", mm.getName());
        LOG.log(Level.FINEST, "toString()          : {0}", mm.toString());
        LOG.log(Level.FINEST, "getDescriptor()     : {0}", mm.getDescriptor());
        LOG.log(Level.FINEST, "getSignature()      : {0}", mm.getSignature());
        // LOG.log(Level.FINEST, "getParamTypes()     : " + mm.getParameterTypes());
        LOG.log(Level.FINEST, "getDeclaringClass() : {0}", mm.getDeclaringClass());
    }

    public String document(ParserResult info, ElementHandle element) {
        LOG.log(Level.FINEST, "document(), ElementHandle : {0}", element);

        String error = NbBundle.getMessage(CompletionHandler.class, "GroovyCompletion_NoJavaDocFound");
        String doctext = null;
        
        if (element instanceof ASTMethod) {
            ASTMethod ame = (ASTMethod) element;

            String base = "";

            String javadoc = getGroovyJavadocBase();
            if (jdkJavaDocBase != null && ame.isGDK() == false) {
                base = jdkJavaDocBase;
            } else if (javadoc != null && ame.isGDK() == true) {
                base = javadoc;
            } else {
                LOG.log(Level.FINEST, "Neither JDK nor GDK or error locating: {0}", ame.isGDK());
                return error;
            }

            MetaMethod mm = ame.getMethod();

            // enable this to troubleshoot subtle differences in JDK/GDK signatures
            printMethod(mm);

            // figure out who originally defined this method

            String className;

            if (ame.isGDK()) {
                className = mm.getDeclaringClass()/*.getCachedClass()*/.getName();
            } else {

                String declName = null;

                if (mm != null) {
                    CachedClass cc = mm.getDeclaringClass();
                    if (cc != null) {
                        declName = cc.getName();
                    }
                }

                if (declName != null) {
                    className = declName;
                } else {
                    className = ame.getClz().getName();
                }
            }

            // create path from fq java package name:
            // java.lang.String -> java/lang/String.html
            String classNamePath = className.replace(".", "/");
            classNamePath = classNamePath + ".html"; // NOI18N

            // if the file can be located in the GAPI folder prefer it
            // over the JDK
            if (!ame.isGDK()) {

                URL url;
                File testFile;

                String apiDoc = getGroovyApiDocBase();
                try {
                    url = new URL(apiDoc + classNamePath);
                    testFile = new File(url.toURI());
                } catch (MalformedURLException ex) {
                    LOG.log(Level.FINEST, "MalformedURLException: {0}", ex);
                    return error;
                } catch (URISyntaxException uriEx) {
                    LOG.log(Level.FINEST, "URISyntaxException: {0}", uriEx);
                    return error;
                }

                if (testFile != null && testFile.exists()) {
                    base = apiDoc;
                }
            }

            // create the signature-string of the method
            String sig = getMethodSignature(ame.getMethod(), true, ame.isGDK());
            String printSig = getMethodSignature(ame.getMethod(), false, ame.isGDK());

            String urlName = base + classNamePath + "#" + sig;

            try {
                LOG.log(Level.FINEST, "Trying to load URL = {0}", urlName); // NOI18N
                doctext = HTMLJavadocParser.getJavadocText(
                    new URL(urlName),
                    false,
                    ame.isGDK());
            } catch (MalformedURLException ex) {
                LOG.log(Level.FINEST, "document(), URL trouble: {0}", ex); // NOI18N
                return error;
            }

            // If we could not find a suitable JavaDoc for the method - say so.
            if (doctext == null) {
                return error;
            }

            doctext = "<h3>" + className + "." + printSig + "</h3><BR>" + doctext;
        }
        return doctext;
    }

    public Documentation documentElement(ParserResult info, ElementHandle handle, Callable<Boolean> cancel) {
        if (handle instanceof JavaElementHandle) {
            // let Java support do the hard work.
            ElementJavadoc jdoc;
            try {
                jdoc = ((JavaElementHandle)handle).extract(info, new JavaElementHandle.ElementFunction<ElementJavadoc>() {
                    @Override
                    public ElementJavadoc apply(CompilationInfo info, Element el) {
                        return ElementJavadoc.create(info, el);
                    }
                });
            } catch (IOException ex) {
                // TBR
                return null;
            }
            
            if (jdoc != null) {
                Boolean b;
                Future<String> content = jdoc.getTextAsync();
                try {
                    while (((b = cancel.call()) == null) || !b.booleanValue()) {
                        try {
                            return Documentation.create(content.get(250, TimeUnit.MILLISECONDS),
                                    jdoc.getURL());
                        } catch (TimeoutException te) {}
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    return null;
                }
                return null;
            }
        }
        
         String s = document(info, handle);
         return s == null ? null : Documentation.create(s);
    }
}
