/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
