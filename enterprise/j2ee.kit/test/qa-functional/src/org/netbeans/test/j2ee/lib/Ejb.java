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
/*
 * Ejb.java
 *
 * Created on May 24, 2005, 6:01 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.test.j2ee.lib;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author jungi
 */
public final class Ejb extends AbstractJ2eeFile {

    static final String HOME = "Home";
    static final String LOCAL = "Local";
    static final String REMOTE = "Remote";
    private boolean isLocal;
    private boolean isRemote;
    private String beanImpl;
    private File remoteJavaProjectDir;
    
    /** Creates a new instance of Ejb */
    public Ejb(String fqName, Project p, File remoteJavaProjectDir, boolean local, boolean remote) {
        super(fqName, p);
        this.remoteJavaProjectDir = remoteJavaProjectDir;
        isLocal = local;
        isRemote = remote;
        beanImpl = name;
    }
    
    public Ejb(String fqName, Project p, File remoteJavaProjectDir, boolean local, boolean remote, String srcRoot) {
        super(fqName, p, srcRoot);
        this.remoteJavaProjectDir = remoteJavaProjectDir;
        isLocal = local;
        isRemote = remote;
        beanImpl = name;
    }
    
    @Override
    public String[] checkExistingFiles() {
        List<String> l = new ArrayList<String>();
        if (!implClassExists()) {
            l.add(MESSAGE.replaceAll("\\$0", "Bean impl class"));
        }
        if (isLocal) {
            if (!localIntfExists()) {
                l.add(MESSAGE.replaceAll("\\$0", "Local interface class"));
            }
            if (!localHomeIntfExists()) {
                l.add(MESSAGE.replaceAll("\\$0", "Local home interface class"));
            }
        }
        if (isRemote) {
            if (!remoteIntfExists()) {
                l.add(MESSAGE.replaceAll("\\$0", "Remote interface class"));
            }
            if (!remoteHomeIntfExists()) {
                l.add(MESSAGE.replaceAll("\\$0", "Remote home interface class"));
            }
        }
        return l.toArray(new String[0]);
    }
    
    private boolean implClassExists() {
        String res = pkgName.replace('.', File.separatorChar) + beanImpl + ".java";
        //System.err.println("name: " + name);
        //System.err.println("impl: " + res);
        return srcFileExist(res);
    }
    
    private boolean localIntfExists() {
        String res = pkgName.replace('.', File.separatorChar) + name + LOCAL + ".java";
        //System.err.println("intf: " + res);
        return srcFileExist(res);
    }
    
    private boolean localHomeIntfExists() {
        String res = pkgName.replace('.', File.separatorChar) + name + LOCAL + HOME + ".java";
        //System.err.println("intf: " + res);
        return srcFileExist(res);
    }
    
    private boolean remoteIntfExists() {
        String res = pkgName.replace('.', File.separatorChar) + name + REMOTE + ".java";
        //System.err.println("intf: " + res);
        return remoteSrcFileExist(res);
    }
    
    private boolean remoteHomeIntfExists() {
        String res = pkgName.replace('.', File.separatorChar) + name + REMOTE + HOME + ".java";
        //System.err.println("intf: " + res);
        return remoteSrcFileExist(res);
    }
    
    /** Checks whether remote sources created in JavaProject next to main project or in main project. */
    protected boolean remoteSrcFileExist(String name) {
        boolean retVal = false;
        try {
            File f = new File(remoteJavaProjectDir, srcRoot.replace("java", "").replace("beans", ""));
            final File fileInJava = new File(f, name);
            f = new File(FileUtil.toFile(prjRoot), srcRoot);
            final File fileInMain = new File(f, name);
            Waiter waiter = new Waiter(new Waitable() {

                @Override
                public Object actionProduced(Object anObject) {
                    return fileInJava.exists() || fileInMain.exists() ? Boolean.TRUE : null;
                }

                @Override
                public String getDescription() {
                    return "file " + fileInJava + " or " + fileInMain + " exists";
                }
            });
            waiter.waitAction(null);
            retVal = fileInJava.exists() || fileInMain.exists();
        } catch (Exception e) {
        }
        return retVal;
    }
}
