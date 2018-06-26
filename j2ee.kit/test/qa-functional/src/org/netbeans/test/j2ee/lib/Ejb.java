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
        return l.toArray(new String[l.size()]);
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
