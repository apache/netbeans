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
