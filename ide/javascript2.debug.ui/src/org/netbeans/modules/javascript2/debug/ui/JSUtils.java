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

package org.netbeans.modules.javascript2.debug.ui;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.javascript2.debug.EditorLineHandler;
import org.netbeans.modules.javascript2.debug.EditorLineHandlerFactory;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.modules.javascript2.debug.ui.editor.LineDelegate;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.Utilities;

/**
 *
 * @author Martin
 */
public class JSUtils {
    
    private static final Logger LOG = Logger.getLogger(JSUtils.class.getName());
    
    public static final String JS_MIME_TYPE = "text/javascript";    // NOI18N
    
    public static Line getCurrentLine() {
        FileObject fo = EditorContextDispatcher.getDefault().getCurrentFile();
        if (fo == null) {
            return null;
        }
        if (!JS_MIME_TYPE.equalsIgnoreCase(fo.getMIMEType())) {
            return null;
        }
        return EditorContextDispatcher.getDefault().getCurrentLine();
    }
    
    public static Line getLine(final String filePath, final int lineNumber) {
        if (filePath == null || lineNumber < 0) {
            return null;
        }
        
        FileObject fileObject = null;
        URI uri = URI.create(filePath);
        if (uri.isAbsolute()) {
            URL url;
            try {
                url = uri.toURL();
            } catch (MalformedURLException muex) {
                // Issue 230657
                LOG.log(Level.INFO, "Cannot resolve " + filePath, muex); // NOI18N
                return null;
            }
            fileObject = URLMapper.findFileObject(url);
        }
        if (fileObject == null) {
            File file;
            if (filePath.startsWith("file:/")) {
                file = Utilities.toFile(uri);
            } else {
                file = new File(filePath);
            }
            fileObject = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        }
        if (fileObject == null) {
            LOG.log(Level.INFO, "Cannot resolve \"{0}\"", filePath);
            return null;
        }

        LineCookie lineCookie = getLineCookie(fileObject);
        if (lineCookie == null) {
            LOG.log(Level.INFO, "No line cookie for \"{0}\"", fileObject);
            return null;
        }
        try {
            return lineCookie.getLineSet().getCurrent(lineNumber);
        } catch (IndexOutOfBoundsException ioob) {
            List<? extends Line> lines = lineCookie.getLineSet().getLines();
            if (lines.size() > 0) {
                return lines.get(lines.size() - 1);
            } else {
                return null;
            }
        }
    }
    
    public static Line getLine(final FileObject fileObject, final int lineNumber) {
        if (fileObject != null) {
            LineCookie lineCookie = JSUtils.getLineCookie(fileObject);
            if (lineCookie != null) {
                Line.Set ls = lineCookie.getLineSet();
                if (ls != null) {
                    try {
                        return ls.getCurrent(lineNumber - 1);
                    } catch (IndexOutOfBoundsException ioob) {
                        List<? extends Line> lines = ls.getLines();
                        if (lines.size() > 0) {
                            return lines.get(lines.size() - 1);
                        } else {
                            return null;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static LineCookie getLineCookie(final FileObject fo) {
        LineCookie result = null;
        try {
            DataObject dataObject = DataObject.find(fo);
            if (dataObject != null) {
                result = dataObject.getLookup().lookup(LineCookie.class);
            }
        } catch (DataObjectNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    public static String getFileName(JSLineBreakpoint b) {
        FileObject fo = b.getFileObject();
        if (fo != null) {
            return fo.getNameExt();
        } else {
            URL url = b.getURL();
            String fileName = url.getPath();
            int i = fileName.lastIndexOf('/');
            if (i < 0) {
                i = fileName.lastIndexOf(File.separatorChar);
            }
            if (i >= 0) {
                fileName = fileName.substring(i + 1);
            }
            return fileName;
        }
    }
    
    public static Line getLine(JSLineBreakpoint b) {
        return ((TextLineHandler) b.getLineHandler()).getLine();
    }
    
    public static JSLineBreakpoint createLineBreakpoint(Line line) {
        EditorLineHandler lineHandler = createLineHandler(line);
        return new JSLineBreakpoint(lineHandler);
    }

    public static EditorLineHandler createLineHandler(Line line) {
        return new LineDelegate(line);
    }
    
}
