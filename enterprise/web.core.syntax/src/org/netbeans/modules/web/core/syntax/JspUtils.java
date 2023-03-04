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
package org.netbeans.modules.web.core.syntax;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.spi.jsp.lexer.JspParseData;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.core.api.JspColoringData;
import org.netbeans.modules.web.core.api.JspContextInfo;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

public class JspUtils {
    
    public static final String TAG_MIME_TYPE = "text/x-tag"; // NOI18N

    //XXX probably already done somewhere...
    public static boolean isJSPOrTagFile(FileObject fo) {
        String mimeType = fo.getMIMEType();
        return JspKit.JSP_MIME_TYPE.equals(mimeType) || JspKit.TAG_MIME_TYPE.equals(mimeType);
    }
    

    /** Create a TokenHierarchy instance for file based Snapshot.
     * Use this method of getting the token hierarchy instead of
     * directly using TokenHierarchy.create(...) for the Snapshot content.
     * This method will also initialize JSP coloring data instance and
     * it to the TokenHierarchy's input attributes.
     * This is necessary if you want the JSP lexing to be driven by
     * the JSP parser result.
     */
    public static TokenHierarchy<CharSequence> createJspTokenHierarchy(Snapshot jspSnapshot) {
        InputAttributes inputAttributes = new InputAttributes();

        FileObject fo = jspSnapshot.getSource().getFileObject();
        if (fo != null) {
            //try to obtain jsp coloring info for file based snapshots
            JspColoringData data = getJSPColoringData(fo);

            if (data == null) {
                if (fo.isValid()) {
                    //error if valid file, just log since it seems can happen quite often
                    Logger.global.info("Cannot obtain JSPColoringData instance for file " + fo.getPath()); //NOI18N
                }
            } else {
                JspParseData jspParseData = new JspParseData((Map<String, String>) data.getPrefixMapper(), data.isELIgnored(), data.isXMLSyntax(), data.isInitialized());
                inputAttributes.setValue(JspTokenId.language(), JspParseData.class, jspParseData, false);
            }

        }

        TokenHierarchy<CharSequence> th = TokenHierarchy.create(
                jspSnapshot.getText(),
                true,
                JspTokenId.language(),
                Collections.EMPTY_SET,
                inputAttributes);

        return th;
    }

    /**
     * @param fo A FileObject representing a JSP like file.
     */
    public static JspColoringData getJSPColoringData(FileObject fo) {
        //TODO: assert that the fo really represents a JSP like file
        JspColoringData result = null;
        if (fo != null && fo.isValid()) {
            JspContextInfo context = JspContextInfo.getContextInfo(fo);
            if (context != null) {
                result = context.getJSPColoringData(fo);
            }
        }
        return result;
    }

    /** 
     * @param fo A FileObject representing a JSP like file.
     */
    public static JspParserAPI.ParseResult getCachedParseResult(FileObject fo, boolean successfulOnly, boolean preferCurrent, boolean forceParse) {
        //TODO: assert that the fo really represents a JSP like file
        JspContextInfo contextInfo = JspContextInfo.getContextInfo(fo);
        if (contextInfo == null) {
            return null;
        } else {
            return contextInfo.getCachedParseResult(fo, successfulOnly, preferCurrent, forceParse);
        }
    }

    /** 
     * @param fo A FileObject representing a JSP like file.
     */
    public static JspParserAPI.ParseResult getCachedParseResult(FileObject fo, boolean successfulOnly, boolean preferCurrent) {
        return getCachedParseResult(fo, successfulOnly, preferCurrent, false);
    }

    /** 
     * @param fo A FileObject representing a JSP like file.
     */
    public static URLClassLoader getModuleClassLoader(FileObject fo) {
        //TODO: assert that the fo really represents a JSP like file
        return JspContextInfo.getContextInfo(fo).getModuleClassLoader(fo);
    }

    /** Returns the root of the web module containing the given file object.
     * If the resource belongs to the subtree of the project's web module,
     * returns this module's document base directory.
     * Otherwise (or if the project parameter is null), it checks for the WEB-INF directory,
     * and determines the root accordingly. If WEB-INF is not found, returns null.
     *
     * @param fo the resource for which to find the web module root
     * @param doc document in which is fileobject editted.
     * @return the root of the web module, or null if a directory containing WEB-INF 
     *   is not on the path from resource to the root
     */
    public static FileObject guessWebModuleRoot(FileObject fo) {
        //TODO: assert that the fo really represents a JSP like file
        return JspContextInfo.getContextInfo(fo).guessWebModuleRoot(fo);
    }

    public static FileObject getFileObject(Document doc, String path) {
        //Find out the file object from the document
        DataObject dobj = NbEditorUtilities.getDataObject(doc);
        FileObject fobj = (dobj != null) ? NbEditorUtilities.getDataObject(doc).getPrimaryFile() : null;

        if (fobj != null) {
            return getFileObject(fobj, path);
        }
        return null;
    }

    /**
     * This method finds the file object according the path or null if the file object doesn't exist.
     * @param doc Document, where user perform the hyperlink action.
     * @param file
     * @param path
     * @return
     */
    public static FileObject getFileObject(FileObject file, String path) {
        if (path == null) // it the path is null -> don't find it
        {
            return file;
        }
        path = path.trim();
        FileObject find = file;
        if (!file.isFolder()) // if the file is not folder, get the parent
        {
            find = file.getParent();
        }

        if (path.length() > 0 && path.charAt(0) == '/') {  // is the absolute path in the web module?
            find = JspUtils.guessWebModuleRoot(file);  // find the folder, where the absolut path starts
            if (find == null) {
                return null;            // we are not able to find out the webmodule root
            }
            path = path.substring(1);   // if we have folder, where the webmodule starts, the path can me relative to this folder
        }
        // find relative path to the folder
        StringTokenizer st = new StringTokenizer(path, "/");
        String token;
        while (find != null && st.hasMoreTokens()) {
            token = st.nextToken();
            if ("..".equals(token)) // move to parent
            {
                find = find.getParent();
            } else if (!".".equals(token)) // if there is . - don't move
            {
                find = find.getFileObject(token);
            }
        }
        return find;

    }

    /** Returns the taglib map as returned by the parser, taking data from the editor as parameters.
     * Returns null in case of a failure (exception, no web module, no parser etc.)
     */
    public static Map getTaglibMap(FileObject fo) {
        //TODO: assert that the fo really represents a JSP like file
        JspContextInfo jspci = JspContextInfo.getContextInfo(fo);
        return jspci == null ? null : jspci.getTaglibMap(fo);
    }

    /** This method returns an image, which is displayed for the FileObject in the explorer.
     * @param doc This is the documet, in which the icon will be used (for exmaple for completion).
     * @param fo file object for which the icon is looking for
     * @return an Image which is dislayed in the explorer for the file.
     */
    public static java.awt.Image getIcon(FileObject fo) {
        try {
            return DataObject.find(fo).getNodeDelegate().getIcon(java.beans.BeanInfo.ICON_COLOR_16x16);
        } catch (DataObjectNotFoundException e) {
            Logger.getLogger(JspUtils.class.getName()).log(Level.INFO, "Cannot find icon for " + fo.getNameExt(), e);
        }
        return null;
    }

    /** Returns an absolute context URL (starting with '/') for a relative URL and base URL.
     *  @param relativeTo url to which the relative URL is related. Treated as directory iff
     *    ends with '/'
     *  @param url the relative URL by RFC 2396
     *  @exception IllegalArgumentException if url is not absolute and relativeTo
     * can not be related to, or if url is intended to be a directory
     */
    public static String resolveRelativeURL(String relativeTo, String url) {
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

    // helper methods for help implement toString() 
    public static String mapToString(Map m, String indent) {
        StringBuffer sb = new StringBuffer();
        Iterator it = m.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            sb.append(indent).append(key).append(" -> ").append(m.get(key)).append("\n");
        }
        return sb.toString();
    }

    /** Decides whether a given file is in the subtree defined by the given folder.
     * Similar to <code>org.openide.filesystems.FileUtil.isParentOf (FileObject folder, FileObject fo)</code>, 
     * but also accepts the case that <code>fo == folder</code>
     */
    public static boolean isInSubTree(FileObject folder, FileObject fo) {
        if (fo == folder) {
            return true;
        } else {
            return FileUtil.isParentOf(folder, fo);
        }
    }

    /** Finds a relative resource path between rootFolder and relativeObject. 
     * @return relative path between rootFolder and relativeObject. The returned path
     * never starts with a '/'. It never ends with a '/'.
     * @exception IllegalArgumentException if relativeObject is not in rootFolder's tree.
     */
    public static String findRelativePath(FileObject rootFolder, FileObject relativeObject) {
        String rfp = rootFolder.getPath();
        String rop = relativeObject.getPath();
        // check that they share the start of the path 
        if (!isInSubTree(rootFolder, relativeObject)) {
            throw new IllegalArgumentException("" + rootFolder + " / " + relativeObject); // NOI18N
        }
        // now really return the result
        String result = rop.substring(rfp.length());
        if (result.startsWith("/")) { // NOI18N
            result = result.substring(1);
        }
        return result;
    }

    /**********************************
     * Copied over from WebModuleUtils.
     **********************************
     */
    /** Finds a relative context path between rootFolder and relativeObject. 
     * Similar to <code>findRelativePath(FileObject, FileObject)</code>, only 
     * different slash '/' conventions.
     * @return relative context path between rootFolder and relativeObject. The returned path
     * always starts with a '/'. It ends with a '/' if the relative object is a directory.
     * @exception IllegalArgumentException if relativeObject is not in rootFolder's tree.
     */
    public static String findRelativeContextPath(FileObject rootFolder, FileObject relativeObject) {
        String result = "/" + findRelativePath(rootFolder, relativeObject); // NOI18N
        return relativeObject.isFolder() ? (result + "/") : result; // NOI18N
    }

    /** Finds a FileObject relative to a given root folder, with a given relative path. 
     * @param rootFolder the root folder
     * @relativePath the relative path (not starting with a '/', delimited by '/')
     * @return fileobject relative to the given root folder or null if not found.
     * @exception IllegalArgumentException if relativeObject is not in rootFolder's tree.
     */
    public static FileObject findRelativeFileObject(FileObject rootFolder, String relativePath) {
        if (relativePath.startsWith("/")) {  // NOI18N
            relativePath = relativePath.substring(1);
        }
        FileObject myObj = rootFolder;
        StringTokenizer st = new StringTokenizer(relativePath, "/"); // NOI18N
        while (myObj != null && st.hasMoreTokens()) {
            myObj = myObj.getFileObject(st.nextToken());
        }
        return myObj;
    }

    public static boolean isJspDocument(Document doc) {
        FileObject fo = DataLoadersBridge.getDefault().getFileObject(doc);
        return fo != null && (fo.getMIMEType().equals("text/x-jsp") || fo.getMIMEType().equals("text/x-tag")); //NOI18N
    }
}
