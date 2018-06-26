/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript2.requirejs.html;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;
import javax.swing.ImageIcon;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.web.common.ui.api.FileReferenceCompletion;
import static org.netbeans.modules.web.common.ui.api.FileReferenceCompletion.getIcon;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;

/**
 * 
 * @author Petr Pisl, Marek Fukala
 */
public abstract class RequireJsFileReferenceCompletion<T> extends FileReferenceCompletion<T> {
    // TODO  This is basically copy of the FileReferenceCompletion + filtering just js files. 
    // it would be much better extend the FileReferenceCompletion with a file filter. Thi is ugly
    private static final RequestProcessor RP = new RequestProcessor();

    private static final ImageIcon PACKAGE_ICON
            = ImageUtilities.loadImageIcon("org/openide/loaders/defaultFolder.gif", false); // NOI18N
    
    @Override
    public List<T> getItems(FileObject orig, int offset, String valuePart) {
        List<T> result = new ArrayList<>();

        String path = "";   // NOI18N
        String fileNamePart = valuePart;
        int lastSlash = valuePart.lastIndexOf('/');
        if (lastSlash == 0) {
            path = "/"; // NOI18N
            fileNamePart = valuePart.substring(1);
        } else if (lastSlash > 0) { // not a leading slash?
            path = valuePart.substring(0, lastSlash);
            fileNamePart = (lastSlash == valuePart.length()) ? "" : valuePart.substring(lastSlash + 1);    // NOI18N
        }

        int anchor = offset + lastSlash + 1;  // works even with -1

        try {
            FileObject documentBase = orig; // also requirejs needs to relative files from the orig file. 
            // need to normalize fileNamePart with respect to orig
            String aaaa = orig.isFolder() ? orig.getPath() + "/" : orig.getPath(); 
            String ctxPath = resolveRelativeURL("/"+aaaa, path);  // NOI18N
            //is this absolute path?
            if (path.startsWith("/")) {
                if (documentBase == null) {
                    //abosolute path but no web root, cannot complete
                    return Collections.emptyList();
                } else {
                    ctxPath = documentBase.getPath() + path;
                }
            } else {
                ctxPath = ctxPath.substring(1);
            }

            FileSystem fs = orig.getFileSystem();

            FileObject folder = fs.findResource(ctxPath);
            if (folder != null) {
                //add all accessible files from current context
                result.addAll(files(anchor, folder, fileNamePart));

                //add go up in the directories structure item
                if (!(documentBase != null && folder.equals(documentBase)) && !path.startsWith("/") // NOI18N
                        && (fileNamePart.isEmpty() // ../| case
                        || fileNamePart.equals(".") // ../.| case
                        || fileNamePart.equals("..")) //../..| case
                        ) { // NOI18N
                    result.add(createGoUpItem(anchor, Color.BLUE, PACKAGE_ICON)); // NOI18N
                }
            }
        } catch (FileStateInvalidException | IllegalArgumentException ex) {
            // unreachable FS - disable completion
        }

        return result;
    }

    private List<T> files(int offset, FileObject folder, String prefix) {
        List<T> res = new ArrayList<>();
        TreeMap<String, T> resFolders = new TreeMap<>();
        TreeMap<String, T> resFiles = new TreeMap<>();

        Enumeration<? extends FileObject> files = folder.getChildren(false);
        while (files.hasMoreElements()) {
            final FileObject file = files.nextElement();
            String fname = file.getNameExt();
            if (fname.startsWith(prefix) && !"cvs".equalsIgnoreCase(fname)) {

                if (file.isFolder()) {
                    resFolders.put(file.getNameExt(), createFileItem(file, offset));
                } else if (JsTokenId.JAVASCRIPT_MIME_TYPE.equals(file.getMIMEType())) {  // this is only the diference against the FileReferenceCompletion
                    T fileItem = createFileItem(file, offset);
                    if (fileItem instanceof PropertyChangeListener) { //"bit" hacky :-)
                        //lazy load icons
                        final PropertyChangeListener plistener = (PropertyChangeListener) fileItem;
                        RP.post(new Runnable() {
                            @Override
                            public void run() {
                                ImageIcon icon = getIcon(file);
                                plistener.propertyChange(
                                        new PropertyChangeEvent(this, "iconLoaded", null, icon)); //NOI18N
                            }

                        });
                    } else {
                        //direct icons load
                        ImageIcon icon = getIcon(file);
                        fileItem = createFileItem(file, offset);
                    }
                    resFiles.put(file.getNameExt(), fileItem);
                }
            }
        }

        res.addAll(resFolders.values());
        res.addAll(resFiles.values());

        return res;
    }

    /**
     * Returns an absolute context URL (starting with '/') for a relative URL
     * and base URL.
     *
     * @param relativeTo url to which the relative URL is related. Treated as
     * directory iff ends with '/'
     * @param url the relative URL by RFC 2396
     * @exception IllegalArgumentException if url is not absolute and relativeTo
     * can not be related to, or if url is intended to be a directory
     */
    private static String resolveRelativeURL(String relativeTo, String url) {
        //System.out.println("- resolving " + url + " relative to " + relativeTo);
        String result;
        if (url.startsWith("/")) { // NOI18N
            result = "/"; // NOI18N
            url = url.substring(1);
        } else {
            // canonize relativeTo
            if ((relativeTo == null) || (!relativeTo.startsWith("/"))) // NOI18N
            {
                throw new IllegalArgumentException();
            }
            relativeTo = resolveRelativeURL(null, relativeTo);
            int lastSlash = relativeTo.lastIndexOf('/');
            if (lastSlash == -1) {
                throw new IllegalArgumentException();
            }
            result = relativeTo.substring(0, lastSlash + 1);
        }

        // now url does not start with '/' and result starts with '/' and ends with '/'
        StringTokenizer st = new StringTokenizer(url, "/", true); // NOI18N
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            //System.out.println("token : \"" + tok + "\""); // NOI18N
            if (tok.equals("/")) { // NOI18N
                if (!result.endsWith("/")) // NOI18N
                {
                    result = result + "/"; // NOI18N
                }
            } else if (tok.equals("")) // NOI18N
            ; // do nohing
            else if (tok.equals(".")) // NOI18N
            ; // do nohing
            else if (tok.equals("..")) { // NOI18N
                String withoutSlash = result.substring(0, result.length() - 1);
                int ls = withoutSlash.lastIndexOf("/"); // NOI18N
                if (ls != -1) {
                    result = withoutSlash.substring(0, ls + 1);
                }
            } else {
                // some file
                result = result + tok;
            }
            //System.out.println("result : " + result); // NOI18N
        }
        //System.out.println("- resolved to " + result);
        return result;
    }
}
