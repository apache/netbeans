/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
