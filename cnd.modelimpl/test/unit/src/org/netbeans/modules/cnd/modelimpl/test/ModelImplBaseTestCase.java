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

package org.netbeans.modules.cnd.modelimpl.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import org.netbeans.junit.Manager;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;

/**
 * IMPORTANT NOTE:
 * If This class is not compiled with the notification about not resolved
 * BaseTestCase class => cnd/core tests are not compiled
 * 
 * To solve this problem compile or run tests for cnd/core
 *
 * If problems with NB JUnit see comment to @see BaseTestCase
 */

/**
 * base class for modelimpl module tests
 */
public abstract class ModelImplBaseTestCase extends ModelBasedTestCase {
    
    public static final String PROPERTY_DATA_PATH = "cnd.modelimpl.unit.data";
    public static final String PROPERTY_GOLDEN_PATH = "cnd.modelimpl.unit.golden";
    public static final String PROPERTY_WORK_PATH = "cnd.modelimpl.unit.workdir";
    
    /**
     * Creates a new instance of ModelImplBaseTestCase
     */
    public ModelImplBaseTestCase(String testName) {
        super(testName);
    }
    
    @Override 
    public String getWorkDirPath() {
        String workDirPath = System.getProperty(PROPERTY_WORK_PATH); // NOI18N
        if (workDirPath == null || workDirPath.length() == 0) {
            return super.getWorkDirPath();
        } else {
            return workDirPath;
        }
    }
    
    @Override 
    public File getGoldenFile(String filename) {
        String goldenDirPath = System.getProperty(PROPERTY_GOLDEN_PATH); // NOI18N
        if (goldenDirPath == null || goldenDirPath.length() == 0) {
            return super.getGoldenFile(filename);
        } else {
            return Manager.normalizeFile(new File(goldenDirPath, filename));
        }        
    }   

    @Override
    protected File getDataFile(String filename) {
        String dataDirPath = System.getProperty(PROPERTY_DATA_PATH); // NOI18N
        if (dataDirPath == null || dataDirPath.length() == 0) {
            return super.getDataFile(filename);
        } else {
            return Manager.normalizeFile(new File(dataDirPath, filename));
        }
    }   
    
    protected void sleep(long timeout) {
        try {
            Thread.sleep(timeout);
        } catch( InterruptedException e ) {
        }
    }
    
    protected void overwriteFile(File file, String text) throws IOException, InterruptedException {
        // to be sure that timestamps are different between quick writes from tests into the same file
        // we introduce delay to be sure that on systems with 1sec granularity they will work as well
        Thread.sleep(1001);
        writeFile(file, text);
    }

    protected void writeFile(File file, String text) throws IOException {
        PrintWriter writer = new PrintWriter(new FileOutputStream(file));
        writer.append(text);
        writer.flush();
        writer.close();
    }

    protected CsmDeclaration findDeclaration(String name, CsmProject project) {
        for( CsmDeclaration decl : project.getGlobalNamespace().getDeclarations() ) {
            if( name.equals(decl.getName().toString())) {
                return decl;
            }
        }
        return null;
    }

    public static String convertToModelImplDataDir(File curDir, String moduleToReplace) {
        assert curDir != null;
        // changed "\\" to "\\\\" in replacement stings, otherwise String.replaceAll threw out of bounds exception
        // see Matcher.appendReplacement (Matcher.java:760) - it treats '\\' as escape character too!
        String dataPath = curDir.getAbsolutePath().replaceAll("/" + moduleToReplace + "/", "/modelimpl/").replaceAll("\\\\" + moduleToReplace + "\\\\", "\\\\modelimpl\\\\"); //NOI18N
        dataPath = dataPath.replaceAll("/cnd." + moduleToReplace + "/", "/cnd.modelimpl/").replaceAll("\\\\cnd." + moduleToReplace + "\\\\", "\\\\cnd.modelimpl\\\\"); //NOI18N
        return dataPath;
    }
    
    protected String convertToModelImplDataDir(String moduleToReplace) {
        return convertToModelImplDataDir(getDataDir(), moduleToReplace);
    }    
}
