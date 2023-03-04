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

package org.netbeans.modules.groovy.debug;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.groovy.ast.ASTNode;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.api.parser.GroovyLanguage;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.debugger.jpda.BreakpointStratifier;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Modifies breakpoints to adapt to the Groovy stratum.
 *
 * @author Martin Janicek
 */
@ServiceProvider(service = BreakpointStratifier.class)
public final class GroovyBreakpointStratifier implements BreakpointStratifier {

    public final static String GROOVY_STRATUM = "Groovy"; //NOI18N
    private static final Logger LOGGER = Logger.getLogger(GroovyBreakpointStratifier.class.getName());

    public GroovyBreakpointStratifier() {
    }

    /**
     * Creates a new breakpoint for the given parameters.
     *
     * @param url a url
     * @param lineNumber a line number
     * @return a new breakpoint for given parameters
     */
    @Override
    @Messages("CTL_Default_Print_Text=Breakpoint reached at line {lineNumber} in {groovyName} by thread {threadName}.")
    public void stratify(JPDABreakpoint breakpoint) {
        if (breakpoint instanceof LineBreakpoint) {
            LineBreakpoint lb = (LineBreakpoint) breakpoint;
            String url = lb.getURL();
            FileObject fo = getFileObjectFromUrl(url);
            if (fo != null && GroovyLanguage.GROOVY_MIME_TYPE.equals(fo.getMIMEType())) {
                int lineNumber = lb.getLineNumber();
                String groovyName = fo.getNameExt();
                String pt = Bundle.CTL_Default_Print_Text();
                String printText = pt.replace("{groovyName}", (groovyName != null) ? groovyName : "?"); // NOI18N
                String groovyPath = getGroovyPath(url, fo);

                lb.setStratum(GROOVY_STRATUM);
                lb.setSourceName(groovyName);
                lb.setSourcePath(groovyPath);
                lb.setPreferredClassName(findGroovyClassName(groovyPath, fo, lineNumber));
                lb.setPrintText(printText);
            }
        }
    }

    private static FileObject getFileObjectFromUrl(String url) {

        FileObject fo = null;

        try {
            fo = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException e) {
            //noop
        }
        return fo;
    }

    private static String findGroovyClassName(String relativePath, FileObject fo, int lineNumber) {
        Source source = Source.create(fo);
        String[] className = new String[] { null };
        try {
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                public @Override void run(ResultIterator resultIterator) throws Exception {
                    GroovyParserResult result = ASTUtils.getParseResult(resultIterator.getParserResult());
                    ASTNode root = ASTUtils.getRoot(result);
                    AstPath path = new AstPath(root, lineNumber, 0);
                    className[0] = ASTUtils.getFqnName(path);
                }
            });
        } catch (ParseException ex) {
        }
        if (className[0] != null) {
            return className[0];
        } else {
            return getClassFilter(relativePath);
        }
    }

    private static String getClassFilter(String relativePath) {
        if (relativePath == null) {
            return "";
        }

        int dot = relativePath.lastIndexOf('.');
        if (dot > 0) {
            relativePath = relativePath.substring(0, dot);
        }
        return relativePath.replace('/', '.') + "*";
    }

    private static String getGroovyPath(String url, FileObject fo) {
        if (fo != null) {
            String relativePath;
            ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
            if (cp != null) {
                FileObject root = cp.findOwnerRoot(fo);
                if (root != null) {
                    relativePath = FileUtil.getRelativePath(root, fo);
                } else {
                    // Suppose the current folder
                    relativePath = fo.getNameExt();
                }
            } else {
                LOGGER.log(Level.FINE, "No classpath for {0}", url);
                // Suppose the current folder
                relativePath = fo.getNameExt();
                return relativePath;
            }
            return relativePath;
        }
        return url;
    }
}
