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
/*
 * AbstractJ2eeFile.java
 *
 * Created on May 24, 2005, 11:51 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.test.j2ee.lib;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author jungi
 */
abstract class AbstractJ2eeFile {
    
    protected static final String MESSAGE = "$0 does not exist.";
    protected String pkgName;
    protected FileObject prjRoot;
    protected String name;
    protected boolean isEjbMod;
    protected String srcRoot;
    
    /** Creates a new instance of AbstractJ2eeFile */
    public AbstractJ2eeFile(String fqName, Project p) {
        this(fqName, p, "src/java");
    }
    
    public AbstractJ2eeFile(String fqName, Project p, String srcRoot) {
        int i = fqName.lastIndexOf('.') + 1;
        name = fqName.substring(i);
        pkgName = fqName.substring(0, i);
        prjRoot = p.getProjectDirectory();
        isEjbMod = p instanceof EjbJarProject;
        this.srcRoot = srcRoot;
    }
    
    protected boolean confFileExist(String name) {
        boolean retVal = false;
        String confDir = (isEjbMod) ? "src/conf" : "web/WEB-INF";
        File f = new File(FileUtil.toFile(prjRoot), confDir);
        try {
            File ff = new File(f, name);
            //System.err.println(ff.getAbsolutePath());
            //System.err.println("confEx: " + ff.exists());
            retVal = ff.exists();
        } catch (Exception e) {
        }
        return retVal;
    }
    
    protected boolean srcFileExist(String name) {
        boolean retVal = false;
        File f = new File(FileUtil.toFile(prjRoot), srcRoot);
        try {
            final File ff = new File(f, name);
            //System.err.println(ff.getAbsolutePath());
            //System.err.println("srcEx: " + ff.exists());
            Waiter waiter = new Waiter(new Waitable() {

                @Override
                public Object actionProduced(Object anObject) {
                    return ff.exists() ? Boolean.TRUE : null;
                }

                @Override
                public String getDescription() {
                    return "file " + ff + " exists";
                }
            });
            waiter.waitAction(null);
            retVal = ff.exists();
        } catch (Exception e) {
        }
        return retVal;
    }

    /**
     * empty array iff there'are no errors, otherwise array of error messages
     *
     *@return empty array iff there'are no errors, otherwise array of error
     *     messages
     */
    public abstract String[] checkExistingFiles();
}
