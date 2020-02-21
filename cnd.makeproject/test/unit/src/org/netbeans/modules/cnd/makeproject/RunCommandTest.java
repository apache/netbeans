/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject;

import java.io.File;
import org.junit.Test;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.cnd.test.CndBaseTestCase;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 */
public class RunCommandTest extends CndBaseTestCase{
    public RunCommandTest(String testName) {
        super(testName);
    }
    
    @Override
    protected boolean addEditorSupport() {
        return false;
    }
    
    private static File getBaseFolder(){
        String dataPath = System.getProperty("java.io.tmpdir");
        if (!dataPath.endsWith(File.separator)) {
            dataPath += File.separator;
        }
        dataPath += "RunCommandTestCaseProject";
        File fileDataPath = new File(dataPath);
        fileDataPath = FileUtil.normalizeFile(fileDataPath);
        
        return fileDataPath;
    }
    
    private static PathMap getPathMapFromConfig(Configuration conf) {
        MakeConfiguration mc = (MakeConfiguration) conf;
        ExecutionEnvironment ee = mc.getDevelopmentHost().getExecutionEnvironment();                
        RemoteSyncFactory syncFactory = mc.getRemoteSyncFactory();                
        return (syncFactory == null) ? null : syncFactory.getPathMap(ee);
    }
    
    @Test
    public static void testExpandingMacroses() {
        File folderBase = getBaseFolder();
        final FileObject folderBaseFO = CndFileUtils.toFileObject(folderBase);
        MakeConfiguration conf = MakeConfiguration.createConfiguration(FSPath.toFSPath(folderBaseFO), "Default", MakeConfiguration.TYPE_APPLICATION, null, HostInfoUtils.LOCALHOST);  // NOI18N
        String result = ProjectActionEvent.getRunCommandAsString(
                "\"${OUTPUT_PATH}\" \"arg 1\" \"${OUTPUT_PATH}\" \"arg 2\"", 
                conf, 
                getPathMapFromConfig(conf));
        String expected = "\"" + conf.getAbsoluteOutputValue() + 
                "\" \"arg 1\" \"" + conf.getAbsoluteOutputValue() + 
                "\" \"arg 2\"";
        

        assertEquals(expected, result);
    }
}
