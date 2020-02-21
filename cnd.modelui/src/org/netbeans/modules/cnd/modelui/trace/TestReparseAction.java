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

package org.netbeans.modules.cnd.modelui.trace;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.xref.CsmIncludeHierarchyResolver;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModel;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;
        
/**
 * A test action that reparses the given project 
 * and redirects error output to the output pane
 * 
 */
public class TestReparseAction extends TestProjectActionBase {

    @Override
    public String getName() {
        return NbBundle.getMessage(getClass(), "CTL_TestProjectReparse"); //NOI18N
    }

    
    @Override
    protected void performAction(Collection<CsmProject> csmProjects) {
        if (csmProjects != null && !csmProjects.isEmpty()) {
            for (CsmProject p : csmProjects) {
                testReparse(p);
            }
        }
    }    
    
    private static class ErrorInfo {
        public final int line;
        public final int column;
        public final String text;
        public ErrorInfo(int line, int column, String text) {
            this.line = line;
            this.column = column;
            this.text = text;
        }
    }
    
    private void testReparse(CsmProject project) {
        
        String task = "Parser Errors " + project.getName(); // NOI18N
        
        final ProgressHandle handle = ProgressHandleFactory.createHandle(task);
        handle.start();
        handle.switchToDeterminate(project.getAllFiles().size());
        int handled = 0;
        
        InputOutput io = IOProvider.getDefault().getIO(task, false);
        io.select();
        OutputWriter out = io.getOut();
        
        for( CsmFile file : project.getSourceFiles() ) {
            handle.progress("Parsing " + file.getName(), handled++); // NOI18N
            testReparse(file, out);
        }
        for( CsmFile file : project.getHeaderFiles() ) {
            if (!isPartial(file, new HashSet<CsmFile>())) {
                handle.progress("Parsing " + file.getName(), handled++); // NOI18N
                testReparse(file, out);
            } else {
                handle.progress("SKIP INCLUDED AS BODY " + file.getName(), handled++); // NOI18N
            }
        }
        
        handle.finish();
        out.flush();
        out.close();
    }
    
    /**
     * Determines whether this file contains part of some declaration,
     * i.e. whether it was included in the middle of some other declaration
     */
    private static boolean isPartial(CsmFile isIncluded, Set<CsmFile> antiLoop) {
        if (antiLoop.contains(isIncluded)) {
            return false;
        }
        antiLoop.add(isIncluded);
        //Collection<CsmFile> files = CsmIncludeHierarchyResolver.getDefault().getFiles(isIncluded);
        Collection<CsmReference> directives = CsmIncludeHierarchyResolver.getDefault().getIncludes(isIncluded);
        for (CsmReference directive : directives) {
            if (directive != null  ) {
                int offset = directive.getStartOffset();
                CsmFile containingFile = directive.getContainingFile();
                if (containingFile != null) {
                    if (CsmSelect.hasDeclarations(containingFile)) {
                        CsmSelect.CsmFilter filter = CsmSelect.getFilterBuilder().createOffsetFilter(offset);
                        Iterator<CsmOffsetableDeclaration> declarations = CsmSelect.getDeclarations(containingFile, filter);
                        if (declarations.hasNext()) {
                            return true;
                        }
                    } else {
                        if (isPartial(containingFile, antiLoop)) {
                            return true;
                        }
                    }
                }
            }
        }
	return false;
    }
    
    private void testReparse(final CsmFile fileImpl, final OutputWriter out) {
        for (CsmInclude include : fileImpl.getIncludes()) {
            if (include.getIncludeFile() == null) {
                int line = include.getStartPosition().getLine();
                int column = include.getStartPosition().getColumn();
                char lBracket = include.isSystem() ? '<' : '"'; //NOI18N
                char rBracket = include.isSystem() ? '>' : '"'; //NOI18N
                printError(out, fileImpl, line, column, "Unresolved include: " + lBracket + include.getIncludeName() + rBracket); //NOI18N
            }
        }
        
        TraceModel.getFileErrors(fileImpl, new TraceModel.ErrorListener() {
            @Override
            public void error(String text, int line, int column) {
                printError(out, fileImpl, line, column, text);
            }
        });
    }

    private void printError(OutputWriter out, CsmFile fileImpl, int line, int column, String text) {
        ErrorInfo info = new ErrorInfo(line, column, text);
        text = fileImpl.getAbsolutePath().toString() + ':' + info.line + ':' + info.column + ": " + info.text; //NOI18N
        try {
            out.println(text, new MyOutputListener(fileImpl, info));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private static class MyOutputListener implements OutputListener {
        
        private final CsmFile file;
        private final ErrorInfo info;

        public MyOutputListener(CsmFile file, ErrorInfo info) {
            this.file = file;
            this.info = info;
        }
        
        @Override
        public void outputLineAction(OutputEvent ev) {
            CsmUtilities.openSource(file, info.line, info.column);
        }
        
        @Override
        public void outputLineSelected(OutputEvent ev) {}
        @Override
        public void outputLineCleared(OutputEvent ev) {}
    }
    
    
}
