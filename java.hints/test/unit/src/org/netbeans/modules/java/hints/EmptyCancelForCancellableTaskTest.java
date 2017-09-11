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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
