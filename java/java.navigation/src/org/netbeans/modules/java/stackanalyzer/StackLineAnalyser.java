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

package org.netbeans.modules.java.stackanalyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.StyledDocument;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Jancura
 */
class StackLineAnalyser {

    private static final RequestProcessor RP = new RequestProcessor(StackLineAnalyser.class);

    private static final String IDENTIFIER =
        "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";    // NOI18N
    private static final Pattern LINE_PATTERN = Pattern.compile(
        "at\\s+" +                                       //  initial at // NOI18N
        "("+IDENTIFIER+"(?:\\."+IDENTIFIER+")*/)?" + // optional module name // NOI18N
        "(("+IDENTIFIER+"(\\."+IDENTIFIER+")*)\\.)?("+IDENTIFIER+")" + // class name // NOI18N
        "\\.("+IDENTIFIER+"|\\<init\\>|\\<clinit\\>)\\s*"+ // method name
        "\\((?:"+IDENTIFIER+"(?:\\."+IDENTIFIER+")*/)?"+IDENTIFIER+"(\\.(?:\\p{javaJavaIdentifierPart}+))?"+ // '(File.java' // NOI18N
        "\\:([0-9]*)\\)");                              // ':123)' // NOI18N

    static boolean matches(String line) {
        Matcher matcher = LINE_PATTERN.matcher(line);
        return matcher.find();
    }

    static Link analyse(String line) {
        Matcher matcher = LINE_PATTERN.matcher(line);
        if (matcher.find()) {
            int lineNumber;
            try {
                lineNumber = Integer.parseInt(matcher.group(8));
            } catch (NumberFormatException nfe) {
                return null;
            }
            int moduleStart = -1;
            if (matcher.group(1) != null) {
                moduleStart = matcher.start(1);
            }
            String ext = matcher.group(7);       
            if (matcher.group(2)==null ) {
                return new Link(matcher.group(5),
                            lineNumber,
                            moduleStart != (-1) ? moduleStart : matcher.start(5),
                            matcher.end(8)+1, ext
                            );

            }
            return new Link(matcher.group(2) + matcher.group(5),
                            lineNumber,
                            moduleStart != (-1) ? moduleStart : matcher.start(2),
                            matcher.end(8)+1, ext
                            );
        }
        return null;
    }

    static final class Link {
        private final String className;
        private final int lineNumber;
        private final int startOffset;
        private final int endOffset;
        private final String extension;

        private Link(
            String className,
            int lineNumber,
            int startOffset,
            int endOffset,
            String extension
        ) {
            this.className = className;
            this.lineNumber = lineNumber;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.extension = extension;
        }

        int getStartOffset () {
            return startOffset;
        }

        int getEndOffset () {
            return endOffset;
        }

        String getExtension() {
            return extension;
        }

        void show () {
            String name = className.replace('.', '/');
            final List<String> resources = new ArrayList<>();
            resources.add(name + getExtension()); //NOI18N
            resources.add(name + ".java"); //NOI18N
            int idx = name.lastIndexOf('$');
            while (idx >= 0) {
                name = name.substring(0, idx);
                resources.add(name + getExtension()); //NOI18N
                resources.add(name + ".java"); //NOI18N
                idx = name.lastIndexOf('$');
            }
            final ProgressHandle handle = ProgressHandle.createHandle(
                NbBundle.getMessage(StackLineAnalyser.class, "TXT_OpeningSource", resources.get(0)));
            handle.start();
            RP.execute(() -> {
                DataObject dobj = null;
                try {
                    final ClassPath classPath = ClassPathSupport.createClassPath(
                            GlobalPathRegistry.getDefault().getSourceRoots().toArray(new FileObject[0]));
                    for (String resource : resources) {
                        dobj = findDataObject(classPath.findResource(resource));
                        if (dobj != null)
                            break;
                    }
                } finally {
                    final DataObject dataObject = dobj;
                    Mutex.EVENT.readAccess(() -> {
                        try {
                            if (dataObject == null) {
                                StatusDisplayer.getDefault().setStatusText(
                                        NbBundle.getMessage(StackLineAnalyser.class,
                                                "AnalyzeStackTopComponent.sourceNotFound",
                                                new Object[]{resources.get(0)}));
                                return;
                            }
                            try {
                                EditorCookie editorCookie = dataObject.getLookup().lookup(EditorCookie.class);
                                LineCookie lineCookie = dataObject.getLookup().lookup(LineCookie.class);
                                if (editorCookie != null && lineCookie != null && lineNumber != -1) {
                                    StyledDocument doc = editorCookie.openDocument();
                                    if (doc != null) {
                                        if (lineNumber != -1) {
                                            try {
                                                Line l = lineCookie.getLineSet().getCurrent(lineNumber - 1);
                                                if (l != null) {
                                                    l.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FRONT);
                                                    return;
                                                }
                                            } catch (IndexOutOfBoundsException oob) {
                                                //line number is no more valid, do not report as an error
                                                StatusDisplayer.getDefault().setStatusText(
                                                        NbBundle.getMessage(StackLineAnalyser.class,
                                                                "AnalyzeStackTopComponent.lineNotFound",
                                                                new Object[]{lineNumber}));
                                            }
                                        }
                                    }
                                }
                                OpenCookie openCookie = dataObject.getLookup().lookup(OpenCookie.class);
                                if (openCookie != null) {
                                    openCookie.open();
                                    return;
                                }
                            } catch (IOException e) {
                                Exceptions.printStackTrace(e);
                            }
                        } finally {
                            handle.finish();
                        }
                    });
                }
            });
        }
    }

    @CheckForNull
    private static DataObject findDataObject (@NullAllowed final FileObject file) {
        try {
            return file == null ? null : DataObject.find (file);
        } catch (DataObjectNotFoundException donf) {
            return null;
        }
    }
}
