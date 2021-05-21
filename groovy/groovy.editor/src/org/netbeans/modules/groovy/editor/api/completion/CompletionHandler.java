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
package org.netbeans.modules.groovy.editor.api.completion;

import groovy.lang.MetaMethod;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.reflection.CachedClass;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.*;
import org.netbeans.modules.csl.api.CodeCompletionHandler.QueryType;
import org.netbeans.modules.csl.spi.DefaultCompletionResult;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.completion.ProposalsCollector;
import org.netbeans.modules.groovy.editor.api.completion.util.ContextHelper;
import org.netbeans.modules.groovy.editor.api.elements.ast.ASTMethod;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.modules.groovy.editor.utils.GroovyUtils;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext;
import org.netbeans.modules.groovy.support.api.GroovySettings;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

public class CompletionHandler implements CodeCompletionHandler {

    private static final Logger LOG = Logger.getLogger(CompletionHandler.class.getName());
    private final PropertyChangeListener docListener;
    private String jdkJavaDocBase = null;
    private String groovyJavaDocBase = null;
    private String groovyApiDocBase = null;
    

    public CompletionHandler() {
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
                synchronized (CompletionHandler.this) {
                    groovyJavaDocBase = null;
                    groovyApiDocBase = null;
                }
            }
        };
        groovySettings.addPropertyChangeListener(WeakListeners.propertyChange(docListener, this));
    }

    @Override
    public CodeCompletionResult complete(CodeCompletionContext completionContext) {
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
            return CodeCompletionResult.NONE;
        }
        final BaseDocument doc = (BaseDocument) document;

        doc.readLock(); // Read-lock due to Token hierarchy use

        try {
            CompletionContext context = new CompletionContext(parserResult, prefix, anchor, lexOffset, astOffset, doc);
            context.init();

            // if we are above a package statement or inside a comment there's no completion at all.
            if (context.location == CaretLocation.ABOVE_PACKAGE || context.location == CaretLocation.INSIDE_COMMENT) {
                return new DefaultCompletionResult(Collections.<CompletionProposal>emptyList(), false);
            }
            
            ProposalsCollector proposalsCollector = new ProposalsCollector(context);

            if (ContextHelper.isVariableNameDefinition(context) || ContextHelper.isFieldNameDefinition(context)) {
                proposalsCollector.completeNewVars(context);
            } else {
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
            proposalsCollector.completeCamelCase(context);

            return new GroovyCompletionResult(proposalsCollector.getCollectedProposals(), context);
        } finally {
            doc.readUnlock();
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

    private static void printASTNodeInformation(String description, ASTNode node) {

        LOG.log(Level.FINEST, "--------------------------------------------------------");
        LOG.log(Level.FINEST, "{0}", description);

        if (node == null) {
            LOG.log(Level.FINEST, "node == null");
        } else {
            LOG.log(Level.FINEST, "Node.getText()  : {0}", node.getText());
            LOG.log(Level.FINEST, "Node.toString() : {0}", node.toString());
            LOG.log(Level.FINEST, "Node.getClass() : {0}", node.getClass());
            LOG.log(Level.FINEST, "Node.hashCode() : {0}", node.hashCode());


            if (node instanceof ModuleNode) {
                LOG.log(Level.FINEST, "ModuleNode.getClasses() : {0}", ((ModuleNode) node).getClasses());
                LOG.log(Level.FINEST, "SourceUnit.getName() : {0}", ((ModuleNode) node).getContext().getName());
            }
        }

        LOG.log(Level.FINEST, "--------------------------------------------------------");
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

    boolean checkForPackageStatement(final CompletionContext request) {
        TokenSequence<GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(request.doc, 1);

        if (ts != null) {
            ts.move(1);

            while (ts.isValid() && ts.moveNext() && ts.offset() < request.doc.getLength()) {
                Token<GroovyTokenId> t = ts.token();

                if (t.id() == GroovyTokenId.LITERAL_package) {
                    return true;
                }
            }
        }

        return false;
    }

    private ArgumentListExpression getSurroundingArgumentList(AstPath path) {
        if (path == null) {
            LOG.log(Level.FINEST, "path == null"); // NOI18N
            return null;
        }

        LOG.log(Level.FINEST, "AEL, Path : {0}", path);

        for (Iterator<ASTNode> it = path.iterator(); it.hasNext();) {
            ASTNode current = it.next();
            if (current instanceof ArgumentListExpression) {

                return (ArgumentListExpression) current;
            }
        }
        return null;
    }

    private AstPath getPathFromInfo(final int caretOffset, final ParserResult info) {
        assert info != null;

        ASTNode root = ASTUtils.getRoot(info);

        // If we don't get a valid root-node from a valid CompilationInfo,
        // there's not much we can do. cf. # 150929

        if (root == null) {
            return null;
        }

        // FIXME parsing API
        BaseDocument doc = (BaseDocument) info.getSnapshot().getSource().getDocument(true);

        return new AstPath(root, caretOffset, doc);

    }

    boolean checkBehindDot(final CompletionContext request) {
        boolean behindDot = false;

        if (request == null || request.context == null || request.context.before1 == null) {
            behindDot = false;
        } else {
            if (CharSequenceUtilities.textEquals(request.context.before1.text(), ".") // NOI18N
                    || (request.context.before1.text().toString().equals(request.getPrefix())
                        && request.context.before2 != null
                        && CharSequenceUtilities.textEquals(request.context.before2.text(), "."))) { // NOI18N
                behindDot = true;
            }
        }

        return behindDot;
    }

    /**
     * create the signature-string of this method usable as a
     * Javadoc URL suffix (behind the # )
     *
     * This was needed, since from groovy 1.5.4 to
     * 1.5.5 the MetaMethod.getSignature() changed from
     * human-readable to Class.getName() output.
     *
     * To make matters worse, we have some subtle
     * differences between JDK and GDK MetaMethods
     *
     * method.getSignature for the JDK gives the return-
     * value right behind the method and encodes like Class.getName():
     *
     * codePointCount(II)I
     *
     * GDK-methods look like this:
     * java.lang.String center(java.lang.Number, java.lang.String)
     *
     * TODO: if groovy folks ever change this (again), we're falling
     * flat on our face.
     *
     */
    public static String getMethodSignature(MetaMethod method, boolean forURL, boolean isGDK) {
        String methodSignature = method.getSignature();
        methodSignature = methodSignature.trim();

        if (isGDK) {
            // remove return value
            int firstSpace = methodSignature.indexOf(" ");

            if (firstSpace != -1) {
                methodSignature = methodSignature.substring(firstSpace + 1);
            }

            if (forURL) {
                methodSignature = methodSignature.replaceAll(", ", ",%20");
            }

            return methodSignature;

        } else {
            String parts[] = methodSignature.split("[()]");

            if (parts.length < 2) {
                return "";
            }

            String paramsBody = decodeTypes(parts[1], forURL);

            return parts[0] + "(" + paramsBody + ")";
        }
    }

    /**
     * This is more a less the reverse function for Class.getName()
     */
    static String decodeTypes(final String encodedType, boolean forURL) {
        String DELIMITER = ",";

        if (forURL) {
            DELIMITER = DELIMITER + "%20";
        } else {
            DELIMITER = DELIMITER + " ";
        }

        StringBuilder sb = new StringBuilder("");
        boolean nextIsAnArray = false;

        for (int i = 0; i < encodedType.length(); i++) {
            char c = encodedType.charAt(i);

            if (c == '[') {
                nextIsAnArray = true;
                continue;
            } else if (c == 'Z') {
                sb.append("boolean");
            } else if (c == 'B') {
                sb.append("byte");
            } else if (c == 'C') {
                sb.append("char");
            } else if (c == 'D') {
                sb.append("double");
            } else if (c == 'F') {
                sb.append("float");
            } else if (c == 'I') {
                sb.append("int");
            } else if (c == 'J') {
                sb.append("long");
            } else if (c == 'S') {
                sb.append("short");
            } else if (c == 'L') { // special case reference
                i++;
                int semicolon = encodedType.indexOf(";", i);
                String typeName = encodedType.substring(i, semicolon);
                typeName = typeName.replace('/', '.');

                if (forURL) {
                    sb.append(typeName);
                } else {
                    sb.append(GroovyUtils.stripPackage(typeName));
                }

                i = semicolon;
            }

            if (nextIsAnArray) {
                sb.append("[]");
                nextIsAnArray = false;
            }

            if (i < encodedType.length() - 1) {
                sb.append(DELIMITER);
            }
        }
        return sb.toString();
    }

    @Override
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
                    /*CachedClass cc = mm.getDeclaringClass();
                    if (cc != null) {
                        Class clz = cc.getCachedClass();
                        if (clz != null) {
                            declName = clz.getName();
                        }
                    }*/
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

    @Override
    public ElementHandle resolveLink(String link, ElementHandle originalHandle) {
        // pass the original handle back. That's better than to throw an unsupported-exception.
        return originalHandle;
    }

    @Override
    public String getPrefix(ParserResult info, int caretOffset, boolean upToOffset) {
        return null;
    }

    @Override
    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        char c = typedText.charAt(0);

        if (c == '.') {
            return QueryType.COMPLETION;
        }

        return QueryType.NONE;
    }

    @Override
    public String resolveTemplateVariable(String variable, ParserResult info, int caretOffset, String name, Map parameters) {
        return null;
    }

    @Override
    public Set<String> getApplicableTemplates(Document d, int selectionBegin, int selectionEnd) {
        return Collections.emptySet();
    }

    @Override
    public ParameterInfo parameters(ParserResult info, int caretOffset, CompletionProposal proposal) {
        LOG.log(Level.FINEST, "parameters(), caretOffset = {0}", caretOffset); // NOI18N

        // here we need to calculate the list of parameters for the methods under the caret.
        // proposal seems to be null all the time.

        List<String> paramList = new ArrayList<String>();

        AstPath path = getPathFromInfo(caretOffset, info);

        // FIXME parsing API
        BaseDocument doc = (BaseDocument) info.getSnapshot().getSource().getDocument(true);

        if (path != null) {

            ArgumentListExpression ael = getSurroundingArgumentList(path);

            if (ael != null) {

                List<ASTNode> children = ASTUtils.children(ael);

                // populate list with *all* parameters, but let index and offset
                // point to a specific parameter.

                int idx = 1;
                int index = -1;
                int offset = -1;

                for (ASTNode node : children) {
                    OffsetRange range = ASTUtils.getRange(node, doc);
                    paramList.add(node.getText());

                    if (range.containsInclusive(caretOffset)) {
                        offset = range.getStart();
                        index = idx;
                    }

                    idx++;
                }

                // calculate the parameter we are dealing with

                if (paramList != null && !paramList.isEmpty()) {
                    return new ParameterInfo(paramList, index, offset);
                }
            } else {
                LOG.log(Level.FINEST, "ArgumentListExpression ==  null"); // NOI18N
                return ParameterInfo.NONE;
            }

        } else {
            LOG.log(Level.FINEST, "path ==  null"); // NOI18N
            return ParameterInfo.NONE;
        }
        return ParameterInfo.NONE;
    }
}
