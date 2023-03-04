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

package org.netbeans.modules.refactoring.java.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.junit.NbPerformanceTest;
import org.netbeans.junit.NbPerformanceTest.PerformanceData;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import static org.netbeans.modules.refactoring.java.test.Utilities.*;

/**
 * 
 * @author Jan Becicka
 * @author Pavel Flaska
 */
public class RefPerfTestCase extends NbTestCase implements NbPerformanceTest {

    FileObject projectDir;

    private MyHandler handler;
    final List<PerformanceData> data;

    protected RefPerfTestCase(String name) {
        super(name);
        handler = new MyHandler();
        handler.setLevel(Level.FINE);
        data = new ArrayList<PerformanceData>();
    }
    
    /**
     * Set-up the services and project
     */
    @Override
    protected void setUp() throws IOException, InterruptedException {
        clearWorkDir();
        String work = getWorkDirPath();
        System.setProperty("netbeans.user", work);
        String zipPath = Utilities.jEditProjectOpen();
        File zipFile = FileUtil.normalizeFile(new File(zipPath));
        unzip(zipFile, work);
        projectDir = openProject("jEdit41", getWorkDir());
    }

    /**
     * Clear work-dir
     */
    @Override
    protected void tearDown() throws IOException {
    }

    public FileObject getProjectDir() {
        return projectDir;
    }

    public boolean perform(AbstractRefactoring absRefactoring, ParameterSetter parameterSetter) {
        Problem problem = absRefactoring.preCheck();
        boolean fatal = false;
        while (problem != null) {
            ref(problem.getMessage());
            fatal = fatal || problem.isFatal();
            problem = problem.getNext();
        }
        if (fatal) {
            return false;
        }
        parameterSetter.setParameters();
        problem = absRefactoring.fastCheckParameters();
        while (problem != null) {
            ref(problem.getMessage());
            fatal = fatal || problem.isFatal();
            problem = problem.getNext();
        }
        if (fatal) {
            return false;
        }
        problem = absRefactoring.checkParameters();
        while (problem != null) {
            ref(problem.getMessage());
            fatal = fatal || problem.isFatal();
            problem = problem.getNext();
        }
        if (fatal) {
            return false;
        }
        RefactoringSession rs = RefactoringSession.create("Session");
        try {
            absRefactoring.prepare(rs);
            Collection<RefactoringElement> elems = rs.getRefactoringElements();
            rs.doRefactoring(true);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return true;
    }
    
    @Override
    public PerformanceData[] getPerformanceData() {
        return data.toArray(new PerformanceData[0]);
    }

    public MyHandler getHandler() {
        return handler;
    }

}
