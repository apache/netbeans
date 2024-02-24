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

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.VariableInfo;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.core.api.JspColoringData;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI.ParseResult;
import org.netbeans.modules.web.jsps.parserapi.Node.IncludeDirective;
import org.netbeans.modules.web.jsps.parserapi.PageInfo;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.web.jsps.parserapi.Node.Visitor;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public abstract class JSPProcessor {
    protected static final Logger logger = Logger.getLogger(JSPProcessor.class.getName());
    //static cache of FileObject---ParserData
    private static final WeakHashMap<FileObject, ParserData> PARSER_DATA_CACHE = new WeakHashMap<>();

    protected Document doc;
    protected FileObject fobj;
    protected boolean processingSuccessful = true;

    private boolean processCalled = false;
    private boolean jakartaVariant = false;
    private ParserData parserData;

    private static class ParserData {
        private final JspParserAPI.ParseResult parserResult;
        private final JspColoringData coloringData;

        public ParserData(JspParserAPI.ParseResult parserResult, JspColoringData coloringData) {
            assert parserResult != null;

            this.parserResult = parserResult;
            this.coloringData = coloringData;
        }

        public JspColoringData getColoringData() {
            return coloringData;
        }

        public ParseResult getParserResult() {
            return parserResult;
        }
    }

    private ParserData getParserData() {
        assert parserData != null; //never allow to return null

        return parserData;
    }

    private static ParserData getCachedParserData(FileObject file) {
        return PARSER_DATA_CACHE.get(file);
    }

    protected String createBeanVarDeclarations(List<String> localBeans) {
        //TODO: the parser data contains no information about offsets and
        //therefore it is not possible to create proper java embeddings
        //inside bean declarations. We need a similar solution to what was
        //done for imports, see issue #161246
        StringBuilder beanDeclarationsBuff = new StringBuilder();

        PageInfo pageInfo = getPageInfo();

        if (pageInfo != null) {
            PageInfo.BeanData[] beanData = getBeanData();

            if (beanData != null) {
                for (PageInfo.BeanData bean : beanData) {
                    if (!localBeans.contains(bean.getId())) {
                        beanDeclarationsBuff.append(bean.getClassName());
                        beanDeclarationsBuff.append(" "); //NOI18N
                        beanDeclarationsBuff.append(bean.getId());
                        beanDeclarationsBuff.append(";\n"); //NOI18N
                    }
                }
            }

            if (pageInfo.isTagFile()) {
                for (TagAttributeInfo info : pageInfo.getTagInfo().getAttributes()) {
                    if (info.getTypeName() != null) { // will be null e.g. for fragment attrs
                        if (!localBeans.contains(info.getName())) {
                            beanDeclarationsBuff.append(info.getTypeName());
                            beanDeclarationsBuff.append(" "); //NOI18N
                            beanDeclarationsBuff.append(info.getName());
                            beanDeclarationsBuff.append(";\n"); //NOI18N
                        }
                    }
                }
            }
        }

        JspSyntaxSupport syntaxSupport = JspSyntaxSupport.get(doc);
        JspColoringData coloringData = getParserData().getColoringData();

        if (coloringData != null && coloringData.getPrefixMapper() != null) {
            Collection<String> prefixes = coloringData.getPrefixMapper().keySet();
            TagData fooArg = new TagData((Object[][]) null);

            for (String prefix : prefixes) {
                List<TagInfo> tags = syntaxSupport.getAllTags(prefix, false); //do not require fresh data - #146762

                for (TagInfo tag : tags) {
                    // #146754 - prevent NPE:
                    if (tag == null) {
                        continue;
                    }
                    VariableInfo vars[] = tag.getVariableInfo(fooArg);

                    if (vars != null) {
                        for (VariableInfo var : vars) {
                            // Create Variable Definitions
                            if (var != null && var.getVarName() != null && var.getClassName() != null && var.getDeclare()) {
                                String varDeclaration = var.getClassName() + " " + var.getVarName() + ";\n";
                                beanDeclarationsBuff.append(varDeclaration);
                            }
                        }
                    }
                }
            }
        }

        return beanDeclarationsBuff.toString();
    }

    protected PageInfo getPageInfo() {
        JspParserAPI.ParseResult result = getParserData().getParserResult();
        return result != null ? result.getPageInfo() : null;

    }

    private PageInfo.BeanData[] getBeanData() {

        PageInfo pageInfo = getPageInfo();
        //pageInfo can be null in some cases when the parser cannot parse
        //the webmodule or the page itself
        if (pageInfo != null) {
            return pageInfo.getBeans();
        }

        //TagLibParseSupport support = (dobj == null) ?
        //null : (TagLibParseSupport)dobj.getCookie(TagLibParseSupport.class);
        //return support.getTagLibEditorData().getBeanData();
        return null;
    }

    protected void assureProcessCalled() {
        if (!processCalled) {
            throw new IllegalStateException("process() method must be called first!"); //NOI18N
        }
    }

    protected void processIncludes(boolean onlyPrelude, final String path) {
        PageInfo pageInfo = getPageInfo();
        if (pageInfo == null) {
            //if we do not get pageinfo it is unlikely we will get something reasonable from
            //jspSyntax.getParseResult()...
            return;
        }

        final Collection<String> processedFiles = new TreeSet<>(processedIncludes());
        processedFiles.add(fobj.getPath());

        if (pageInfo.getIncludePrelude() != null) {
            for (String preludePath : (List<String>) pageInfo.getIncludePrelude()) {
                processIncludedFile(preludePath, processedFiles);
            }
        }

        if (!onlyPrelude) {
            Visitor visitor = new Visitor() {

                @Override
                public void visit(IncludeDirective includeDirective) throws JspException {
                    String fileName = includeDirective.getAttributeValue("file");
                    if (path == null || path.equals(fileName)) {
                        processIncludedFile(fileName, processedFiles);
                    }
                }
            };

            try {
                JspParserAPI.ParseResult parseResult = getParserData().getParserResult();

                if (parseResult != null && parseResult.getNodes() != null) {
                    parseResult.getNodes().visit(visitor);
                }
            } catch (JspException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private void processIncludedFile(String filePath, Collection<String> processedFiles) {
        FileObject includedFile = JspUtils.getFileObject(doc, filePath);

        if (includedFile != null && includedFile.canRead() // prevent endless loop in case of a circular reference
                && !processedFiles.contains(includedFile.getPath())) {

            processedFiles.add(includedFile.getPath());

            try {
                DataObject includedFileDO = DataObject.find(includedFile);
                String mimeType = includedFile.getMIMEType();

                if ("text/x-jsp".equals(mimeType) || "text/x-tag".equals(mimeType)) { //NOI18N
                    EditorCookie editor = includedFileDO.getLookup().lookup(EditorCookie.class);

                    if (editor != null) {
                        IncludedJSPFileProcessor includedJSPFileProcessor = new IncludedJSPFileProcessor((BaseDocument) editor.openDocument(), processedFiles);
                        includedJSPFileProcessor.process();
                        processIncludedFile(includedJSPFileProcessor);
                    }
                }
            } catch (IOException | BadLocationException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public static boolean ignoreLockFromUnitTest = false;

    public synchronized void process() throws BadLocationException {
        processCalled = true;

        //get fileobject
        fobj = NbEditorUtilities.getFileObject(doc);
        if(fobj == null) {
            //do not handle non fileobject documents like coloring properties preview document
            processingSuccessful = false;
            return;
        }

        ClassPath cp = ClassPath.getClassPath(fobj, ClassPath.COMPILE);
        jakartaVariant = cp != null && cp.findResource("jakarta/servlet/http/HttpServlet.class") != null;

        //workaround for issue #120195 - Deadlock in jspparser while reformatting JSP
        //
        //we MAY be called here this way:
        //ActionFactory$FormatAction.actionPerformed()
        //calls  doc.runAtomicAsUser (new Runnable () {
             //trigger the formatters under writelock
        // }
        //so we cannot access the jsp parser since it may try acquire
        //document readlock in another thread (TaglibParseSupport)
        //
        //possible solutions:
        //1. use files by the jsp parser (do not try to access the editor document)
        //2. would it help if we force the parsing in the current thred? In another words
        //   can by readlock called from within a writelock? - doesn't make much sense, probably not possible
        //3. cache the embeddings - the bottleneck is that we may return a virtual source very distinct
        //   from what is currently in the editor
        //4. cache the jsp parser result - should give us much more accurate results, at least
        //   the java sections in the virtual source should reflect the current editor document state.
        //
        //#4 choosen for now as it seems to have the best risk/gain ratio
        //
        if (DocumentUtilities.isWriteLocked(doc) && !ignoreLockFromUnitTest) {
            //try to get the parser result data from the cache
            parserData = getCachedParserData(fobj);
            if(parserData == null) {
                //nothing in the cache, just fail
                processingSuccessful = false;
                return;
            }
        } else {

            //call the jsp parser and cache the results
            JspParserAPI.ParseResult parseResult = JspUtils.getCachedParseResult(fobj, true, false);
            if (parseResult == null || !parseResult.isParsingSuccess()) {
                processingSuccessful = false;
                return;
            }

            //get & cache coloring data
            JspColoringData coloringData = JspUtils.getJSPColoringData(fobj);
            //having the coloring data doesn't seem to be necessary (according to the current code)

            parserData = new ParserData(parseResult, coloringData);

            //and cache...
            PARSER_DATA_CACHE.put(fobj, parserData);

        }

        final AtomicReference<BadLocationException> ble = new AtomicReference<>();
        doc.render(() -> {
            try {
                renderProcess();
            } catch (BadLocationException ex) {
                ble.set(ex);
            }
        });
        if (ble.get() != null) {
            throw ble.get(); //just rethrow to this level
        }
    }

    protected abstract void processIncludedFile(IncludedJSPFileProcessor includedJSPFileProcessor);

    protected abstract void renderProcess() throws BadLocationException;

    /**
     * Add extra imports according to information obtained from the JSP parser
     *
     * @param localImports imports already included in the Simplified Servlet
     * by the processImportDirectives method
     */
    protected String createImplicitImportStatements(List<String> localImports) {
        StringBuilder importsBuff = new StringBuilder();
        String[] imports = getImportsFromJspParser();

        if (imports == null || imports.length == 0) {
            processingSuccessful = false;
        } else {
            // TODO: better support for situation when imports is null
            // (JSP doesn't belong to a project)
            for (String pckg : imports) {
                if (!localImports.contains(pckg)) {
                    importsBuff.append("import "); //NOI18N
                    importsBuff.append(pckg);
                    importsBuff.append(";\n"); //NOI18N
                }
            }
        }

        return importsBuff.toString();
    }

    private String[] getImportsFromJspParser() {
        PageInfo pi = getPageInfo();
        if (pi == null) {
            //we need at least some basic imports
            if(jakartaVariant) {
                return new String[]{"jakarta.servlet.*", "jakarta.servlet.http.*", "jakarta.servlet.jsp.*"};
            } else {
                return new String[]{"javax.servlet.*", "javax.servlet.http.*", "javax.servlet.jsp.*"};
            }
        }
        List<String> imports = pi.getImports();
        if(imports == null) {
            return null;
        }
        return imports
                .stream()
                .map(i -> jakartaVariant ? i.replaceFirst("^javax\\.", "jakarta.") : i)
                .toArray(i -> new String[i]);
    }

    protected abstract Collection<String> processedIncludes();
}
