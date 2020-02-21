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

package org.netbeans.modules.cnd.repository.impl;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import static junit.framework.Assert.assertTrue;
import org.netbeans.junit.Manager;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;
import org.netbeans.modules.cnd.repository.util.RepositoryTestSupport;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.openide.util.Exceptions;

/**
 *
 */
public class RepositoryValidationBase extends TraceModelTestBase {

    public RepositoryValidationBase(String testName) {
        super(testName);
    }

    protected static final File localFilesStorage = new File(System.getProperty("user.home"), "cnd-test-files-storage");
    protected static final String nimi = "ModelBuiltFromRepository"; //NOI18N
    protected static final String modelimplName = "cnd.modelimpl";
    protected static final String moduleName = "cnd.repository";
    private static String goldenDirectory;

    @Override
    protected File getTestCaseDataDir() {
        String dataPath = convertToModelImplDataDir("repository");
        String filePath = "common";
        return Manager.normalizeFile(new File(dataPath, filePath));
    }

    @Override
    protected void doTest(String[] args, PrintStream streamOut, PrintStream streamErr, Object... params) throws Exception {
        super.doTest(args, streamOut, streamErr, params);
    }

    protected boolean returnOnShutdown() {
        return false;
    }

    protected boolean dumpModel() {
        return true;
    }
    
    @Override
    protected void postTest(String[] args, Object... params) throws Exception {
        if (!getTraceModel().getProject().isStable(null)) {
            if (returnOnShutdown()) {
                return;
            }
            CndUtils.threadsDump();
            while (!getTraceModel().getProject().isStable(null)) {
                if (returnOnShutdown()) {
                    return;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        if (dumpModel()) {
            RepositoryTestSupport.dumpCsmProject(getTraceModel().getProject(), System.out, returnOnShutdown());
            super.postTest(args, params);
        }
    }

    protected static String getGoldenDirectory() {
        return goldenDirectory;
    }

    protected static void setGoldenDirectory(String goldenDirectory) {
        RepositoryValidationBase.goldenDirectory = goldenDirectory;
    }
    
    protected final List<String> find() throws IOException {
        return download();
//        List<String> list = new ArrayList<String>();
//        //String dataPath = convertToModelImplDataDir("repository");
//        //list.add(dataPath + "/common/quote_nosyshdr"); //NOI18N
//        //list.add(dataPath + "/org"); //NOI18N
//        String dataPath = getDataDir().getAbsolutePath();
//        int i = dataPath.indexOf("repository");
//        dataPath = dataPath.substring(0,i+11)+"test";
//        list.add(dataPath + "/CLucene"); //NOI18N
//        list.add(dataPath + "/pkg-config"); //NOI18N
//        list = expandAndSort(list);
//        list.add("-I"+dataPath+"/CLucene");
//        list.add("-I"+dataPath+"/CLucene/CLucene");
//        list.add("-DHAVE_CONFIG_H");
//        list.add("-I"+dataPath + "/pkg-config");
//        return list;
//
//
    }
    
    // "http://pkgconfig.freedesktop.org/releases/pkgconfig-0.18.tar.gz"
    // "http://www.mirrorservice.org/sites/download.sourceforge.net/pub/sourceforge/l/li/litesql/litesql-0.3.3.tar.gz"
    // wget http://pkgconfig.freedesktop.org/releases/pkgconfig-0.18.tar.gz
    // gzip -d pkgconfig-0.18.tar.gz
    // tar xf pkgconfig-0.18.tar
   private List<String> download() throws IOException{
        List<String> list = new ArrayList<String>();
        File fileDataPath = CndCoreTestUtils.getDownloadBase();
        String dataPath = fileDataPath.getAbsolutePath();
        final AtomicBoolean finish = new AtomicBoolean(false);
        ExecutionListener listener = new ExecutionListener() {
            @Override
            public void executionStarted(int pid) {
            }
            @Override
            public void executionFinished(int rc) {
                finish.set(true);
            }
        };
        String pkg = "pkg-config-0.25";
        File file = new File(dataPath, pkg);
        if (!file.exists()){
            file.mkdirs();
        }
        if (file.list().length == 0){
            File fileFromStorage = new File(localFilesStorage, pkg+".tar.gz");
            if (fileFromStorage.exists()) {
                execute("cp", dataPath, fileFromStorage.getAbsolutePath(), dataPath);
            } else {
                execute("wget", dataPath, "-qN", "http://pkgconfig.freedesktop.org/releases/"+pkg+".tar.gz");
            }
            execute("gzip", dataPath, "-d", pkg+".tar.gz");
            execute("tar", dataPath, "xf", pkg+".tar");
        }

        String sqlite = "sqlite-autoconf-3071700";
        file = new File(dataPath, sqlite);
        if (!file.exists()){
            file.mkdirs();
        }
        if (file.list().length == 0){
            File fileFromStorage = new File(localFilesStorage, sqlite+".tar.gz");
            if (fileFromStorage.exists()) {
                execute("cp", dataPath, fileFromStorage.getAbsolutePath(), dataPath);
            } else {
                execute("wget", dataPath, "-qN", "http://www.sqlite.org/2013/"+sqlite+".tar.gz");
            }
            execute("gzip", dataPath, "-d", sqlite+".tar.gz");
            execute("tar", dataPath, "xf", sqlite+".tar");
        }
        list.add(dataPath + "/"+pkg); //NOI18N
        list.add(dataPath + "/"+sqlite); //NOI18N
        for(String f : list){
            file = new File(f);
            assertTrue("Not found folder "+f, file.exists());
        }
        list = expandAndSort(list);
        list.add("-DHAVE_CONFIG_H");
        list.add("-I"+dataPath + "/"+pkg);
        list.add("-I"+dataPath + "/"+sqlite);
        return list;
    }

    private void execute(String command, String folder, String ... arguments){
        StringBuilder buf = new StringBuilder();
        for(String arg : arguments) {
            buf.append(' ');
            buf.append(arg);
        }
        System.err.println(folder+"#"+command+buf.toString());
        ExitStatus status = ProcessUtils.executeInDir(folder, ExecutionEnvironmentFactory.getLocal(), command, arguments);
        if (!status.isOK()) {
            System.out.println(status);
        }
    }

    protected final List<String> expandAndSort(List<String> files) {
        List<String> result = new ArrayList<String>();
        for( String file : files ) {
            addFile(file, result);
        }
        Collections.sort(result);
        return result;
    }
    
    private void addFile(String fileName, List<String> files) {
        File file = new File(fileName);
        if( file.isDirectory() ) {
            String[] list = file.list();
            for( int i = 0; i < list.length; i++ ) {
                addFile(new File(file, list[i]).getAbsolutePath(), files);
            }
        } else {
            if (fileName.endsWith(".c")||fileName.endsWith(".cpp")){
                if (fileName.indexOf("32.")>0){
                    return;
                }
                files.add(fileName);
            }
        }
    }
}
