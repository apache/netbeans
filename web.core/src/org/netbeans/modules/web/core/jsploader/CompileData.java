/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
