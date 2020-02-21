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

package org.netbeans.modules.cnd.completion.cplusplus.ext;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import org.netbeans.modules.cnd.modelimpl.test.ProjectBasedTestCase;
import org.netbeans.modules.editor.completion.CompletionItemComparator;
import org.netbeans.spi.editor.completion.CompletionItem;


/**
 *
 *
 */
public abstract class CompletionBaseTestCase extends ProjectBasedTestCase {
    
    /**
     * if test performs any modifications in data files or create new files
     * => pass performInWorkDir as true to create local copy of project in work dir
     */
    public CompletionBaseTestCase(String testName, boolean performInWorkDir) {
        super(testName, performInWorkDir);
        System.setProperty("cnd.mode.completion.unittest", "true");
        // System.setProperty("cnd.repository.hardrefs", "true");
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        //System.setProperty("cnd.completion.trace", "true");
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        //System.setProperty("cnd.completion.trace", "false");
    }
    
    protected void performTest(String source, int lineIndex, int colIndex) throws Exception {
        performTest(source, lineIndex, colIndex, "");// NOI18N
    }
    
    protected void performTest(String source, int lineIndex, int colIndex, String textToInsert) throws Exception {
        performTest(source, lineIndex, colIndex, textToInsert, 0);// NOI18N
    }
    
    protected void performTest(String source, int lineIndex, int colIndex, String textToInsert, int offsetAfterInsertion) throws Exception {
        performTest(source, lineIndex, colIndex, textToInsert, offsetAfterInsertion, getName()+".ref");// NOI18N
    }
    
    protected void performTest(String source, int lineIndex, int colIndex, String textToInsert, int offsetAfterInsertion, String goldenFileName) throws Exception {
        performTest(source, lineIndex, colIndex, textToInsert, offsetAfterInsertion, goldenFileName, null, null);
    }

    protected void performTest(String source, int lineIndex, int colIndex, String textToInsert, int offsetAfterInsertion, String goldenFileName, String toPerformItemRE, String goldenFileName2) throws Exception {
        performTest(source, lineIndex, colIndex, textToInsert, offsetAfterInsertion, goldenFileName, toPerformItemRE, goldenFileName2, false);
    }
    
    protected void performTest(String source, int lineIndex, int colIndex, String textToInsert, int offsetAfterInsertion, String goldenFileName, String toPerformItemRE, String goldenFileName2, boolean tooltip) throws Exception {
        File workDir = getWorkDir();
        File testFile = getDataFile(source);
        
        File output = new File(workDir, goldenFileName);
        PrintStream streamOut = new PrintStream(output);
        
        CompletionTestResultItem[] array = createTestPerformer().test(logWriter, textToInsert, offsetAfterInsertion, false, testFile, lineIndex, colIndex, tooltip); // NOI18N        

	assertNotNull("Result should not be null", array);
        Arrays.sort(array, new CompletionComparatorWrapper(CompletionItemComparator.BY_PRIORITY));
        for (int i = 0; i < array.length; i++) {
            streamOut.print(array[i].getCompletionItem().toString());
            streamOut.print("\t$\t");
            streamOut.println(array[i].getSubstituted());
        }
        streamOut.close();
        
        checkDifference(workDir, getGoldenFile(goldenFileName), output);
    } 

    protected CompletionTestPerformer createTestPerformer() {
        return new CompletionTestPerformer();
    }
    
    
    private static class CompletionComparatorWrapper implements Comparator<CompletionTestResultItem> {
        
        private final Comparator<CompletionItem> comparator;

        public CompletionComparatorWrapper(Comparator<CompletionItem> comparator) {
            this.comparator = comparator;
        }

        @Override
        public int compare(CompletionTestResultItem o1, CompletionTestResultItem o2) {
            return comparator.compare(o1.getCompletionItem(), o2.getCompletionItem());
        }
        
    }
}
