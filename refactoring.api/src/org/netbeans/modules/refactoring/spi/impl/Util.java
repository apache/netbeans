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

package org.netbeans.modules.refactoring.spi.impl;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author  Jan Becicka
 */

public class Util {

    //copy - paste programming
    //http://ant.netbeans.org/source/browse/ant/src-bridge/org/apache/tools/ant/module/bridge/impl/BridgeImpl.java.diff?r1=1.15&r2=1.16
    //http:/java.netbeans.org/source/browse/java/javacore/src/org/netbeans/modules/javacore/Util.java    
    //http://core.netbeans.org/source/browse/core/ui/src/org/netbeans/core/ui/MenuWarmUpTask.java
    //http://core.netbeans.org/source/browse/core/src/org/netbeans/core/actions/RefreshAllFilesystemsAction.java
    //http://java.netbeans.org/source/browse/java/api/src/org/netbeans/api/java/classpath/ClassPath.java
        
    private static FileSystem[] fileSystems;

    private static FileSystem[] getFileSystems() {
        if (fileSystems != null) {
            return fileSystems;
        }
        File[] roots = File.listRoots();
        Set allRoots = new LinkedHashSet();
        assert roots != null && roots.length > 0 : "Could not list file roots"; // NOI18N
        
        for (int i = 0; i < roots.length; i++) {
            File root = roots[i];
            FileObject random = FileUtil.toFileObject(root);
            if (random == null) continue;
            
            FileSystem fs;
            try {
                fs = random.getFileSystem();
                allRoots.add(fs);
            } catch (FileStateInvalidException e) {
                throw new AssertionError(e);
            }
        }
        FileSystem[] retVal = new FileSystem [allRoots.size()];
        allRoots.toArray(retVal);
        assert retVal.length > 0 : "Could not get any filesystem"; // NOI18N
        
        return fileSystems = retVal;
    }
    
    /**
     * Adds given listener to all FileSystems
     * @param l listener to add
     */    
    public static void addFileSystemsListener(FileChangeListener l) {
        FileSystem[] fileSystems = getFileSystems();
        for (int i=0; i<fileSystems.length; i++) {
            fileSystems[i].addFileChangeListener(l);
        }
    }
    
    /**
     * Removes given listener to all FileSystems
     * @param l listener to remove
     */    
    public static void removeFileSystemsListener(FileChangeListener l) {
        FileSystem[] fileSystems = getFileSystems();
        for (int i=0; i<fileSystems.length; i++) {
            fileSystems[i].removeFileChangeListener(l);
        }
    }
}
