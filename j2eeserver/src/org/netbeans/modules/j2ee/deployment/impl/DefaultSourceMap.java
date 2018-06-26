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

package org.netbeans.modules.j2ee.deployment.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author nn136682
 */
public class DefaultSourceMap extends SourceFileMap {

    /**
     * Straight file mapping service.
     * Map a distribution path to a file using distribution path as relative path to a mapping root.
     */
    private J2eeModuleProvider provider;
    private HashSet rootFiles = new HashSet();
    
    /** Creates a new instance of DefaultFileMapping */
    public DefaultSourceMap(J2eeModuleProvider provider) {
        this.provider = provider;
        FileObject[] roots = provider.getSourceRoots();
        for (int i=0; i<roots.length; i++) {
            if (roots[i] != null) {
                rootFiles.add(FileUtil.toFile(roots[i]));
            }
        }
    }
    
    public String getContextName() {
        return provider.getDeploymentName();
    }

    public FileObject[] getSourceRoots() {
        return provider.getSourceRoots();
    }
    
    public File getEnterpriseResourceDir() {
        return provider.getJ2eeModule().getResourceDirectory();
    }
    
    public File[] getEnterpriseResourceDirs() {
        ArrayList result = new ArrayList();
        result.add(provider.getJ2eeModule().getResourceDirectory());
        if (provider instanceof J2eeApplicationProvider) {
            J2eeApplicationProvider jap = (J2eeApplicationProvider) provider;
            J2eeModuleProvider[] children = jap.getChildModuleProviders();
            for (int i=0; i<children.length; i++) {
                result.add(children[i].getJ2eeModule().getResourceDirectory());
            }
        }
        return (File[]) result.toArray(new File[result.size()]);
    }
   
    public boolean add(String distributionPath, FileObject sourceFile) {
        return false;
    }
    
    public FileObject remove(String distributionPath) {
        return null;
    }
    
    public FileObject[] findSourceFile(String distributionPath) {
        ArrayList ret = new ArrayList();
        FileObject[] roots = getSourceRoots();
        String path = distributionPath.startsWith("/") ? distributionPath.substring(1) : distributionPath; //NOI18N
        for (int i=0; i<roots.length; i++) {
            FileObject fo = roots[i].getFileObject(path);
            if (fo != null)
                ret.add(fo);
        }
        return (FileObject[]) ret.toArray(new FileObject[ret.size()]);
    }
    
    public File getDistributionPath(FileObject sourceFile) {
        for (Iterator i=rootFiles.iterator(); i.hasNext();) {
            File rootFile = (File) i.next();
            FileObject root = FileUtil.toFileObject(rootFile);
            String relative = FileUtil.getRelativePath(root, sourceFile);
            if (relative != null && ! relative.trim().equals("")) { //NOI18N
                return new File(relative);
            }
        }
        return null;
    }
}


