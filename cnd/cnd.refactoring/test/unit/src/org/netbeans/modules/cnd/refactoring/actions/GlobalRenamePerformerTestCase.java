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
package org.netbeans.modules.cnd.refactoring.actions;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.refactoring.test.RefactoringBaseTestCase;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.text.PositionRef;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public class GlobalRenamePerformerTestCase extends RefactoringBaseTestCase {
    
    
    public GlobalRenamePerformerTestCase(String testName) {
        super(testName);
    }

    @Override 
    protected File getTestCaseDataDir() {
        return getQuoteDataDir();
    }    
    
    public void testRenameOfModuleClass() throws Exception {
        performRenameRefactoring("module.h", 51, 10, "NewModule"); // NOI18N
    }    
    
    
    private void performRenameRefactoring(String source, int line, int column, String newName) throws Exception {
        performRenameRefactoring(source, line, column, newName, getName() + ".ref"); // NOI18N
    }
    
    private void performRenameRefactoring(String source, int line, int column, String newName, String goldenFileName) throws Exception {
        File workDir = getWorkDir();
        
        CsmReference reference = super.getReference(source, line, column);
        
        Lookup lookup = Lookups.singleton(reference);
        
        RefactoringSession session = RefactoringSession.create("Global refactoring test"); // NOI18N
        
        RenameRefactoring refactoring = new RenameRefactoring(lookup);
        refactoring.setNewName(newName);
        refactoring.setSearchInComments(false);        
        refactoring.prepare(session);
        
        File output = new File(workDir, goldenFileName);
        PrintStream streamOut = new PrintStream(output);
        
        List<RefactoringElement> elements = new ArrayList<>(session.getRefactoringElements());
        Collections.sort(elements, new RefactoringElementsComparator());
        
        for (RefactoringElement element : elements) {
            streamOut.println(formatRefactoringElement(element, newName));
        }
        
        streamOut.close();
        
        checkDifference(workDir, getGoldenFile(goldenFileName), output);
    }
    
    private String formatRefactoringElement(RefactoringElement element, String newText) throws Exception {
        String fileName = element.getParentFile().getName() + "." + element.getParentFile().getExt();  // NOI18N
        PositionRef begin = element.getPosition().getBegin();
        PositionRef end = element.getPosition().getEnd();        
        return fileName + ": <" + formatPosition(begin) + ", " + formatPosition(end) + ">: " + element.getPosition().getText() + " -> " + newText; // NOI18N
    }
    
    private String formatPosition(PositionRef position) throws Exception {
        return  String.valueOf(position.getOffset()) + " (" + position.getLine() + " : " + position.getColumn() + ")";  // NOI18N
    }
    
    
    // Compares elements by containing file and if in the same file, by offsets
    private static class RefactoringElementsComparator implements Comparator<RefactoringElement> {

        @Override
        public int compare(RefactoringElement o1, RefactoringElement o2) {
            String fileName1 = o1.getParentFile().getName() + "." + o1.getParentFile().getExt(); // NOI18N
            String fileName2 = o2.getParentFile().getName() + "." + o2.getParentFile().getExt(); // NOI18N           
            int result = fileName1.compareTo(fileName2);
            return result != 0 ? result : Integer.valueOf(o1.getPosition().getBegin().getOffset()).compareTo(o2.getPosition().getBegin().getOffset());
        }
        
    }
    
}
