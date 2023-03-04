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
package org.netbeans.modules.java.hints;

import com.sun.source.util.TreePath;
import java.util.List;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.TreeRuleTestBase;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Jan Lahoda
 */
public class EmptyCancelForCancellableTaskTest extends TreeRuleTestBase {
    
    private static final String ERROR_MESSAGE = "Empty cancel()";
    
    public EmptyCancelForCancellableTaskTest(String testName) {
        super(testName);
    }

    public void testSimple1() throws Exception {
        performAnalysisTest("test/Test.java", "package test; import org.netbeans.api.java.source.*; public class Test implements CancellableTask<CompilationInfo> { public void cancel() {}}", 180 - 48, "0:129-0:135:verifier:" + ERROR_MESSAGE);
    }
    
    public void testSimple2() throws Exception {
        performAnalysisTest("test/Test.java", "package test; import org.netbeans.api.java.source.*; public class Test implements CancellableTask<CompilationInfo> { public void cancel(boolean b) {}}", 180 - 48);
    }
    
    public void testSimple3() throws Exception {
        performAnalysisTest("test/Test.java", "package test; import org.netbeans.api.java.source.*; public class Test { public void cancel() {}}", 180 - 48);
    }
    
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        return new EmptyCancelForCancellableTask().run(info, path);
    }
    
    @Override
    protected FileObject[] extraClassPath() {
        FileObject api = URLMapper.findFileObject(CancellableTask.class.getProtectionDomain().getCodeSource().getLocation());
        
        assertNotNull(api);
        
        return new FileObject[] {FileUtil.getArchiveRoot(api)};
    }
    
}
