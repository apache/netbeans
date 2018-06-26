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

package org.netbeans.modules.web.core.api;

import java.util.Hashtable;
import javax.swing.text.Document;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.web.core.jsploader.JspContextInfoImpl;

import org.openide.filesystems.FileObject;

import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
//import org.netbeans.api.registry.Context;
import org.openide.util.NbBundle;

/** The JspContextInfo can be implemented with other modules, 
 * which want to reuse the JSPKit for coloring their documents. 
 * One example can be Facelets. Facelets are mix of xhtml, 
 * jsf and facelets tags. The editor for Facelets can looks 
 * as the editor for jsp, but the data for code completion 
 * and the syntax highlighting (user tags) are not obtained 
 * from jspparser, but are counted externally. You can supply 
 * your data through your implementation of this class. 
 * Your implementation has to be registered on default 
 * file system (in layer file) in the folder 
 * /J2EE/JSPSyntaxColoring/${file_type_mimetype}.
 * 
 * For every mimetype is obtained only one instance. 
 */
public abstract class JspContextInfo {
    
//    /** Name of the settings context where an instance of this class should be registered */
//    public static final String CONTEXT_NAME = "/J2EE/JSPSyntaxColoring/"; //NOI18N
//
//    private static Hashtable <String, JspContextInfo> instances = new Hashtable();

    private static JspContextInfo SHARED;
    /**
     *
     * @param fo non-null fileobject
     * @return JspContextInfo instance, null if fo == null or not valid (already deleted)
     */
    public static synchronized JspContextInfo getContextInfo( FileObject fo ) {

//        if (fo == null || !fo.isValid()){
//            return null;
//        }

        if(SHARED == null) {
            SHARED = new JspContextInfoImpl();
        }

        return SHARED;

//        JspContextInfo instance = instances.get(fo.getMIMEType());
//
//        if (instance == null) {
//            FileObject f = FileUtil.getConfigFile(CONTEXT_NAME + fo.getMIMEType()); // NOI18N
//            if (f != null) {
//                try {
//                    DataFolder folder = (DataFolder)DataObject.find(f).getCookie(DataFolder.class);
//                    DataObject[] dobjs = folder.getChildren();
//
//                    for (int i = 0; i < dobjs.length; i ++){
//                        InstanceCookie ic = (InstanceCookie)dobjs[i].getCookie(InstanceCookie.class);
//                        Object o = ic.instanceCreate();
//                        if (o instanceof JspContextInfo){
//                            instance = (JspContextInfo)o;
//                            instances.put(fo.getMIMEType(), instance);
//                            continue;
//                        }
//                    }
//                } catch (DataObjectNotFoundException ex) {
//                    Logger.getLogger("global").log(Level.WARNING, null, ex);
//                } catch (java.io.IOException ex) {
//                    Logger.getLogger("global").log(Level.WARNING, null, ex);
//                } catch (java.lang.ClassNotFoundException ex){
//                    Logger.getLogger("global").log(Level.WARNING, null, ex);
//                }
//            }
//        }
//        return instance;
    }
    
    public abstract JspColoringData getJSPColoringData(FileObject fo);
    
    public abstract JspParserAPI.ParseResult getCachedParseResult(FileObject fo, boolean successfulOnly, boolean preferCurrent, boolean forceReload);
    
    public abstract JspParserAPI.ParseResult getCachedParseResult(FileObject fo, boolean successfulOnly, boolean preferCurrent);
    
    public abstract JspParserAPI.JspOpenInfo getCachedOpenInfo(FileObject fo, boolean preferCurrent);
    
    public abstract URLClassLoader getModuleClassLoader(FileObject fo);
    
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
    public abstract FileObject guessWebModuleRoot(FileObject fo);
    
    /** Returns the taglib map as returned by the parser, taking data from the editor as parameters.
     * Returns null in case of a failure (exception, no web module, no parser etc.)
     */
    public abstract Map getTaglibMap(FileObject fo);
    
    /** This method returns an image, which is displayed for the FileObject in the explorer.
     * It is used to display objects in editor (e.g. in code completion).
     * @param doc This is the documet, in which the icon will be used (for exmaple for completion).
     * @param fo file object for which the icon is looking for
     * @return an Image which is dislayed in the explorer for the file.
     */
    public abstract java.awt.Image getIcon(FileObject fo);
    
}
