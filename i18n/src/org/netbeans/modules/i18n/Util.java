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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.i18n;

import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.netbeans.api.project.Project;
import org.openide.nodes.Node;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.netbeans.api.project.FileOwnerQuery;

/**
 * Bundle access, ...
 *
 * @author  Petr Kuzel
 */
public class Util {

    public static String getString(String key) {
        return NbBundle.getMessage(Util.class, key);
    }
    
    public static char getChar(String key) {
        return getString(key).charAt(0);
    }
    
    /**
     * Write the exception into log.
     */
    public static void debug(Throwable t) {
        ErrorManager err = ErrorManager.getDefault();
        err.notify(err.INFORMATIONAL, t);
    }

    /**
     * Write annotated exception into log.
     */
    public static void debug(String annotation, Throwable t) {
        ErrorManager err = ErrorManager.getDefault();
        err.annotate(t, err.INFORMATIONAL, annotation, null, null, null);
        err.notify(err.INFORMATIONAL, t);
    }
    
    public static Project getProjectFor(DataObject dobj) {
      Project prj = null;
      FileObject fo = dobj.getPrimaryFile();
      return FileOwnerQuery.getOwner(fo);
    }

  public static Project getProjectFor(Node [] activatedNodes) {
    Project project = null;

    if (activatedNodes.length > 0) {
      DataObject dataObject = activatedNodes[0].getCookie(DataObject.class);
      if(dataObject != null && dataObject.getPrimaryFile() != null) 
	project = FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
    } 
    return project;
  }

    /**
     * Gets classpath that contains the given resource bundle. 
     * In addition to the bundle file, a source must be given that
     * will access the resource at run-time.
     */
    public static ClassPath getExecClassPath(FileObject srcFile, FileObject resFile) {
        // try EXECUTE class-path first
        ClassPath ecp = ClassPath.getClassPath( srcFile, ClassPath.EXECUTE );
        if ((ecp != null) && (ecp.getResourceName( resFile, '.',false) != null))
            return ecp;


        // if not directly on EXECUTE, might be on SOURCE
        ClassPath scp = ClassPath.getClassPath( srcFile, ClassPath.SOURCE);
        // try to find  the resource on source class path
        if ((scp != null) && (scp.getResourceName( resFile, '.',false) != null)) 
            return scp; 

        // now try resource owner
        ClassPath rcp = ClassPath.getClassPath( resFile, ClassPath.SOURCE);
        // try to find  the resource on source class path
        if ((rcp!=null) && (rcp.getResourceName( resFile, '.',false) != null))  
                return rcp; 
        

        return null;
    }
        
    /**
     * Tries to find the bundle either in sources or in execution
     * classpath.
     */
    public static FileObject getResource(FileObject srcFile, String bundleName) {
        // try to find it in sources of the same project
        ClassPath scp = ClassPath.getClassPath( srcFile, ClassPath.SOURCE);
        if (scp != null) {
            FileObject ret = scp.findResource(bundleName);
            if (ret != null) return ret;
        }

        // try to find in sources of execution classpath
        ClassPath ecp = ClassPath.getClassPath( srcFile, ClassPath.EXECUTE);
        if (ecp != null) {
            for (ClassPath.Entry e : ecp.entries()) {
                SourceForBinaryQuery.Result r = SourceForBinaryQuery.findSourceRoots(e.getURL());
                for (FileObject srcRoot : r.getRoots()) {
                    // try to find the bundle under this source root
                    ClassPath cp = ClassPath.getClassPath(srcRoot, ClassPath.SOURCE);
                    if (cp != null) {
                        FileObject ret = cp.findResource(bundleName);
                        if (ret != null)
                            return ret;
                    }
                }
            }
        }

        return null;
    }


    /**
     * Inverse to the previous method - finds name for the give
     * resource bundle. It is equivalent but more effective to use
     * this method instead of getExecClassPath(...).getResourceName(...) .
     */
    public static String getResourceName(FileObject srcFile, FileObject resFile, char separator, boolean bpar) {
        // try SOURCE class-path first
        ClassPath scp = ClassPath.getClassPath( srcFile, ClassPath.SOURCE );
        if (scp!= null) {
            String ret = scp.getResourceName( resFile, separator, bpar);
            if (ret!=null) return ret;
        }

        ClassPath ecp = ClassPath.getClassPath( srcFile, ClassPath.EXECUTE );
        if (ecp!=null) {
            String ret = ecp.getResourceName( resFile, separator, bpar);
            if (ret != null) return ret;
        }

        ClassPath rcp = ClassPath.getClassPath( resFile, ClassPath.SOURCE );
        if (rcp != null) {
            String ret = rcp.getResourceName( resFile, separator, bpar);
            if (ret!=null) return ret;
        }
        
        return null;
        
    }
}
