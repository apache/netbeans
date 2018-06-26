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

package org.netbeans.modules.j2ee.deployment.execution;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.deployment.impl.*;
import org.openide.filesystems.*;
import java.io.*;

/**
 *
 * @author nn136682
 */
public class ServerExecutorTest extends NbTestCase {

    public ServerExecutorTest(java.lang.String testName) {
        super(testName);
    }

    private static ServerExecutorTest instance;
    private static ServerExecutorTest instance() {
        if (instance == null) 
            instance = new ServerExecutorTest("testNothing");
        return instance;
    }
    
    public void testNothing() {
    }
    
    static private LocalFileSystem wfs;
    static FileSystem getWorkFileSystem() {
        if (wfs != null)
            return wfs;
        try {
            File workdir = instance().getWorkDir();
            wfs = new LocalFileSystem();
            wfs.setRootDirectory(workdir);
            return wfs;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
            
    public static boolean writeTargetModule(String destFile, TargetModule.List tml) {
        FileLock lock = null;
        Writer writer = null;
        try {
            if (tml == null)
                return true;
            
            FileObject fo = FileUtil.createData(getWorkFileSystem().getRoot(), destFile+".xml");
            lock = fo.lock();
            writer = new OutputStreamWriter(fo.getOutputStream(lock));
            TargetModuleConverter.create().write(writer, tml);
            return true;
            
        } catch(Exception ioe) {
            throw new RuntimeException(ioe);
        }
        finally {
            try {
            if (lock != null) lock.releaseLock();
            if (writer != null) writer.close();
            } catch (Exception e) {}
        }
    }

    public static TargetModule.List readTargetModule(String fromFile) {
        Reader reader = null;
        try {
            FileObject dir = getWorkFileSystem().getRoot();
            FileObject fo = dir.getFileObject (fromFile, "xml");
            if (fo == null) {
                System.out.println(Thread.currentThread()+ " readTargetModule: Can't get FO for "+fromFile+".xml from "+dir.getPath());
                return null;
            }
            reader = new InputStreamReader(fo.getInputStream());
            return (TargetModule.List) TargetModuleConverter.create().read(reader);
        } catch(Exception ioe) {
            throw new RuntimeException(ioe);
        } finally {
            try {
            if (reader != null) reader.close();
            } catch (Exception e) {}
        }
    }
}
