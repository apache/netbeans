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
package org.netbeans.modules.web.common.ui.api;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.netbeans.modules.web.common.api.ValueCompletion;
import org.netbeans.modules.web.common.spi.ProjectWebRootQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;

/**
 *
 * @author marekfukala
 * @param <T>
 */
public abstract class FileReferenceCompletion<T> implements ValueCompletion<T> {

    private static final RequestProcessor RP = new RequestProcessor();

    private static final ImageIcon PACKAGE_ICON
            = ImageUtilities.loadImageIcon("org/openide/loaders/defaultFolder.gif", false); // NOI18N

    /**
     * @since 1.58
     *
     * @param file file which is supposed to be represented by the completion
     * item
     * @param anchor anchor of the completion item
     * @return
     */
    public abstract T createFileItem(FileObject file, int anchor);

    public abstract T createGoUpItem(int anchor, Color color, ImageIcon icon);

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
            FileObject documentBase = ProjectWebRootQuery.getWebRoot(orig);
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
                    // Should match color in o.n.m.html.editor.api.completion.HtmlCompletionItem.createFileCompletionItem()
                    result.add(createGoUpItem(anchor, new Color(224, 160, 65), PACKAGE_ICON));
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
                } else {
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

    /**
     * This method returns an image, which is displayed for the FileObject in
     * the explorer.
     * 
     * @since 1.58
     * @param fo an instance of {@link FileObject}
     * @return an instance of {@link Image}
     */
    public static ImageIcon getIcon(FileObject fo) {
        if (fo.isFolder()) {
            return PACKAGE_ICON;
        } else {
            try {
                return ImageUtilities.icon2ImageIcon(ImageUtilities.image2Icon(
                    DataObject.find(fo).getNodeDelegate().getIcon(java.beans.BeanInfo.ICON_COLOR_16x16)));
            } catch (DataObjectNotFoundException e) {
                Logger.getLogger(FileReferenceCompletion.class.getName()).log(Level.INFO, "Cannot find icon for " + fo.getNameExt(), e);
            }
            return null;
        }
    }
    
}
