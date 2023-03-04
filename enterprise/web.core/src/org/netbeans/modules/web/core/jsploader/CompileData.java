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

package org.netbeans.modules.web.core.jsploader;

import java.io.File;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.JSPServletFinder;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.core.api.JspContextInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/** Data related to compilation attached to one JSP page.
 *  Basically a copy of the data retrieved from the compilation plugin.
 *  This data will change during the compilation process.
 *  This class is also responsible for translating File-view of compiled data to
 *  FileObject-view, including the creation of the necessary filesystems.
 *
 * @author  Petr Jiricka
 * @version 
 */
public class CompileData {

    private JspDataObject jspPage;
    private FileObject docRoot;
    private String servletEncoding;
    private File servletJavaRoot;
    private String servletResourceName;
     

    /** Creates new CompileData */
    public CompileData(JspDataObject jspPage) {
        this.jspPage = jspPage;
        WebModule wm = WebModule.getWebModule (jspPage.getPrimaryFile());
        if (wm != null) {
            this.docRoot = wm.getDocumentBase();
            if (docRoot == null) {
                // #235824 - NPE happens during refactoring, deletion etc.
                Project project = FileOwnerQuery.getOwner(jspPage.getPrimaryFile());
                docRoot = project.getProjectDirectory();
            }
            String jspResourcePath = JspCompileUtil.findRelativeContextPath(docRoot, jspPage.getPrimaryFile());
            JSPServletFinder finder = JSPServletFinder.findJSPServletFinder (docRoot);
            servletJavaRoot = finder.getServletTempDirectory();
            servletResourceName = finder.getServletResourcePath(jspResourcePath);
            servletEncoding = finder.getServletEncoding(jspResourcePath);
        }
    }
    
    public FileObject getServletJavaRoot() {
        if ((servletJavaRoot != null) && servletJavaRoot.exists()) {
            return FileUtil.toFileObject(servletJavaRoot);
        }
        else {
            return null;
        }
    }
    
    public String getServletResourceName() {
        return servletResourceName;
    }
    
    private File getServletFile() {
        if (servletJavaRoot == null) {
            return null;
        }
        URI rootURI = servletJavaRoot.toURI();
        URI servletURI = rootURI.resolve(servletResourceName);
        return new File(servletURI);
    }
    
    public FileObject getServletFileObject() {
        FileObject root = getServletJavaRoot();
        if (root == null) {
            return null;
        }
        File servlet = getServletFile();
        if ((servlet == null) || !servlet.exists()) {
            return null;
        }
        
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(servlet));
        if (fo != null) {
            return fo;
        }
        try {
            root.getFileSystem().refresh(false);
            return root.getFileObject(getServletResourceName());
        }
        catch (FileStateInvalidException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }
        
        return null;
    }
    
    
    /** Returns encoding for the servlet generated from the JSP. */
    public String getServletEncoding() {
        return servletEncoding;
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("--COMPILE DATA--"); // NOI18N
        sb.append("\n"); // NOI18N
        sb.append("JSP page        : " + FileUtil.getFileDisplayName(jspPage.getPrimaryFile())); // NOI18N
        sb.append("\n"); // NOI18N
        sb.append("servletJavaRoot : " + servletJavaRoot + ", exists= " +  // NOI18N
            ((servletJavaRoot == null) ? "false" : "" + servletJavaRoot.exists())); // NOI18N
        sb.append("\n"); // NOI18N
        sb.append("servletResource : " + servletResourceName + ", fileobject exists= " +  // NOI18N
            (getServletFileObject() != null)); // NOI18N
        sb.append("\n"); // NOI18N
        File sf = getServletFile();
        if (sf != null) {
            sb.append("servletFile : " + sf.getAbsolutePath() + ", exists= " +  // NOI18N
                getServletFile().exists()); // NOI18N
        }
        else {
            sb.append("servletFile : null"); // NOI18N
        }
        sb.append("\n"); // NOI18N
        sb.append("--end COMPILE DATA--"); // NOI18N
        return sb.toString();
    }
    
}
