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
