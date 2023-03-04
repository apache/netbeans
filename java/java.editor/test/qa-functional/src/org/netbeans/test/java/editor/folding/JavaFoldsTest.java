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

package org.netbeans.test.java.editor.folding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import javax.swing.text.JTextComponent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import junit.textui.TestRunner;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test behavior of java code folds.
 * Sample file contains java folds. Test creates a fold hierarchy dump
 * and compares it with golden file.
 * Then test simply moves a caret
 * position and tries to invoke expand or collapse action.
 * At the end collapse all and expand all action is tested.
 * Note: static imports block are not a subject of testing yet [PENDING]
 * @author Martin Roskanin
 */
  public class JavaFoldsTest extends JavaCodeFoldingTestCase {

    // private PrintStream wrapper for System.out
    PrintStream systemOutPSWrapper = new PrintStream(System.out);
    
    JTextComponent target;
    EditorOperator editor;
    int index = 0;
      
      
    /** Creates a new instance of Main */
    public JavaFoldsTest(String testMethodName) {
        super(testMethodName);
    }

    
    
    private String getIndexAsString(){
        String ret = String.valueOf(index);
        if (ret.length() == 1) ret = "0" + ret;
        return ret;
    }
    
    private String getRefFileName(){
        return this.getName()+getIndexAsString()+".ref"; //NOI18N
    }
    
    private String getGoldenFileName(){
        return this.getName()+getIndexAsString()+".pass"; //NOI18N
    }
    
    private String getDiffFileName(){
        return this.getName()+getIndexAsString()+".diff"; //NOI18N
    }
    
    // hashtable holding all already used logs and correspondig printstreams
    private Hashtable logStreamTable = null;
    
    private PrintStream getFileLog(String logName) throws IOException {
        OutputStream outputStream;
        FileOutputStream fileOutputStream;
        
        if ((logStreamTable == null)|(hasTestMethodChanged())) {
            // we haven't used logging capability - create hashtables
            logStreamTable = new Hashtable();
            //System.out.println("Created new hashtable");
        } else {
            if (logStreamTable.containsKey(logName)) {
                //System.out.println("Getting stream from cache:"+logName);
                return (PrintStream)logStreamTable.get(logName);
            }
        }
        // we didn't used this log, so let's create it
        FileOutputStream fileLog = new FileOutputStream(new File(getWorkDir(),logName));
        PrintStream printStreamLog = new PrintStream(fileLog,true);
        logStreamTable.put(logName,printStreamLog);
        //System.out.println("Created new stream:"+logName);
        return printStreamLog;
    }
    
    private String lastTestMethod=null;
    
    private boolean hasTestMethodChanged() {
        if (!this.getName().equals(lastTestMethod)) {
            lastTestMethod=this.getName();
            return true;
        } else {
            return false;
        }
    }
    
    public PrintStream getRef() {
        String refFilename = getRefFileName();
        try {
            return getFileLog(refFilename);
        } catch (IOException ioe) {
            // canot get ref file - return system.out
            //System.err.println("Test method "+this.getName()+" - cannot open ref file:"+refFilename
            //                                +" - defaulting to System.out and failing test");
            fail("Could not open reference file: "+refFilename);
            return  systemOutPSWrapper;
        }
    }
    
    private void compareFoldHierarchyDump(){
        String viewDumpHierarchy = foldHierarchyToString(target);
        ref(viewDumpHierarchy);
        compareReferenceFiles(getRefFileName(), getGoldenFileName(), getDiffFileName());
        index++;
    }
    
    
    
    
    public void testJavaFolds(){
        openDefaultProject();
        openFile("Source Packages|java_code_folding.JavaFoldsTest", "testJavaFolds");
        
        editor = getDefaultSampleEditorOperator();
        JTextComponentOperator text = new JTextComponentOperator(editor);
        target = (JTextComponent)text.getSource();
        
        // wait max. 6 second for code folding initialization
        waitForFolding(target, 3000);

        //00 compare fold hierarchy dump against golden file
        compareFoldHierarchyDump();
    
        //01 collapse initial comment fold. Caret is inside fold
        //* Initial Comme|nt Fold
        collapseFoldAtCaretPosition(editor, 2, 17);
        compareFoldHierarchyDump();
    
        //02 collapse import section fold. Caret is at the fold start offset
        //|import javax.swing.JApplet;
        collapseFoldAtCaretPosition(editor, 9, 1);
        compareFoldHierarchyDump();
    
        //03 collapse Outer Class Javadoc Fold. Caret is at the end fold guarded position
        //*/|
        collapseFoldAtCaretPosition(editor, 17, 4);
        compareFoldHierarchyDump();
    
        //04 collapse One Line Field Javadoc Fold. Caret - before the fold
        //|    /** One Line Field Javadoc Fold*/
        collapseFoldAtCaretPosition(editor, 20, 1);
        compareFoldHierarchyDump();
    
        //05 collapse Multi-line Field Favadoc Fold. Caret - before the fold
        //  |/**
        collapseFoldAtCaretPosition(editor, 23, 5);
        compareFoldHierarchyDump();

        //06 collapse One-line Constructor Javadoc Fold
        //    /** One-line Constructor Javadoc Fold |*/
        collapseFoldAtCaretPosition(editor, 28, 43);
        compareFoldHierarchyDump();
    
        //07 collapse One-line Constructor Fold
        //    public testJavaFolds() { } //One-line Const|ructor Fold
        collapseFoldAtCaretPosition(editor, 29, 47);
        compareFoldHierarchyDump();
    
        //08
        //|     *  Multi-line Constructor Javadoc Fold
        collapseFoldAtCaretPosition(editor, 32, 1);
        compareFoldHierarchyDump();
    
        //09
        //    public testJavaFolds(String s) |{ //Multi-line Constructor Fold
        collapseFoldAtCaretPosition(editor, 34, 36);
        compareFoldHierarchyDump();
    
        //10
        //    /**| One-line Method Javadoc Fold */
        collapseFoldAtCaretPosition(editor, 40, 8);
        compareFoldHierarchyDump();
    
        //11
        //    public void methodOne(){|} // One-line Method Fold
        collapseFoldAtCaretPosition(editor, 41, 29);
        compareFoldHierarchyDump();
    
        //12
        //     *  Multi-line Meth|od Javadoc Fold
        collapseFoldAtCaretPosition(editor, 44, 24);
        compareFoldHierarchyDump();
    
        //13 Multi-line Method Fold
        //    } |
        collapseFoldAtCaretPosition(editor, 48, 7);
        compareFoldHierarchyDump();
    
        //14 multi folds on one line. Collapsing middle method
        //    public void firstMethod(){ }  public void secondM|ethod(){ } public void thirdMethod(){ }
        collapseFoldAtCaretPosition(editor, 50, 53);
        compareFoldHierarchyDump();
    
        //15 multi folds on one line. Expanding middle method
        //    public void firstMethod(){ }  public void secondM|ethod()[...] public void thirdMethod(){ }
        expandFoldAtCaretPosition(editor, 50, 53);
        compareFoldHierarchyDump();
    
        //16 multi folds on one line. Collapsing middle method
        //    public void firstMethod(){ } | public void secondMethod(){ } public void thirdMethod(){ }
        collapseFoldAtCaretPosition(editor, 50, 33);
        compareFoldHierarchyDump();
    
        //17 multi folds on one line. Expandinging middle method
        //    public void firstMethod(){ } | public void secondMethod()[...] public void thirdMethod(){ }
        expandFoldAtCaretPosition(editor, 50, 33);
        compareFoldHierarchyDump();
    
        //18 multi folds on one line. Collapsing middle method
        //    public void firstMethod(){ }  public void secondMethod(){ } |public void thirdMethod(){ }
        collapseFoldAtCaretPosition(editor, 50, 65);
        compareFoldHierarchyDump();
    
        //19 multi folds on one line. Expandinging middle method
        //    public void firstMethod(){ }  public void secondMethod()[...] |public void thirdMethod(){ }
        expandFoldAtCaretPosition(editor, 50, 65);
        compareFoldHierarchyDump();
    
        //20 multi folds on one line. Collapsing first method
        //    public void firstMethod(){|}  public void secondMethod(){ } public void thirdMethod(){ }
        collapseFoldAtCaretPosition(editor, 50, 31);
        compareFoldHierarchyDump();
    
        //21 multi folds on one line. Expanding first method
        //    public void firstMethod()[...]|  public void secondMethod(){ } public void thirdMethod(){ }
        expandFoldAtCaretPosition(editor, 50, 33);
        compareFoldHierarchyDump();

        //22 multi folds on one line. Collapsing first method
        //    public void firstMethod(){ }|  public void secondMethod(){ } public void thirdMethod(){ }
        collapseFoldAtCaretPosition(editor, 50, 33);
        compareFoldHierarchyDump();

        //23 multi folds on one line. Expanding first method
        //|   public void firstMethod()[...]  public void secondMethod(){ } public void thirdMethod(){ }
        expandFoldAtCaretPosition(editor, 50, 1);
        compareFoldHierarchyDump();

        //24 multi folds on one line. Collapsing third method
        //    public void firstMethod(){ }  public void secondMethod(){ } |public void thirdMethod(){ }
        collapseFoldAtCaretPosition(editor, 50, 64);
        compareFoldHierarchyDump();

        //25 multi folds on one line. Expanding third method
        //   public void firstMethod(){ }  public void secondMethod(){ } |public void thirdMethod()[...]
        expandFoldAtCaretPosition(editor, 50, 64);
        compareFoldHierarchyDump();

        //26 multi folds on one line. Collapsing third method
        //    public void firstMethod(){ }  public void secondMethod(){ } public void thirdMethod(){ }|
        collapseFoldAtCaretPosition(editor, 50, 92);
        compareFoldHierarchyDump();

        //27 multi folds on one line. Expanding third method
        //   public void firstMethod(){ }  public void secondMethod(){ } public void thirdMethod()[...]|
        expandFoldAtCaretPosition(editor, 50, 92);
        compareFoldHierarchyDump();

        //28 collapse One-line InnerClass Javadoc Fold
        // One-line Inn|erClass Javadoc Fold
        collapseFoldAtCaretPosition(editor, 52, 21);
        compareFoldHierarchyDump();
    
        //29 collapse One-line InnerClass Fold
        //|  public static class InnerClassOne{ }
        collapseFoldAtCaretPosition(editor, 53, 1);
        compareFoldHierarchyDump();
    
        //30 collapse Multi-line InnerClass Javadoc Fold 
        //|    /** 
        collapseFoldAtCaretPosition(editor, 55, 1);
        compareFoldHierarchyDump();

        //31 collapse Multi-line InnerClass Fold
        //public static class InnerClassTwo{ //Multi-line InnerClass Fold|
        collapseFoldAtCaretPosition(editor, 58, 68);
        compareFoldHierarchyDump();
    
        //------------------- Inner Class tests -------------------------
        //---------------------------------------------------------------
    
        //32 collapse Multi-line InnerClass Fold
        //|    public static class InnerClassThree{
        collapseFoldAtCaretPosition(editor, 61, 1);
        compareFoldHierarchyDump();
    
        //33 expand Multi-line InnerClass Fold
        //|    public static class InnerClassThree[...]
        expandFoldAtCaretPosition(editor, 61, 1);
        compareFoldHierarchyDump();
    
        //34 collapse One Line InnerClass Field Javadoc Fold
        //        /** One Line InnerClass Field Javadoc Fold*/|    
        collapseFoldAtCaretPosition(editor, 62, 53);
        compareFoldHierarchyDump();

        //35 collapse Multi-line InnerClass Field Javadoc Fold
        //        /**|
        collapseFoldAtCaretPosition(editor, 65, 12);
        compareFoldHierarchyDump();
    
        //36 collapse One-line InnerClass Constructor Javadoc Fold
        //        |/** One-line InnerClass Constructor Javadoc Fold */
        collapseFoldAtCaretPosition(editor, 70, 9);
        compareFoldHierarchyDump();
    
        //37 collapse One-line InnerClass Constructor Fold
        //            public InnerClassThree() { }| //One-line InnerClass Constructor Fold
        collapseFoldAtCaretPosition(editor, 71, 36);
        compareFoldHierarchyDump();

        //38 collapse Multi-line InnerClass Constructor Javadoc Fold
        //         |*/
        collapseFoldAtCaretPosition(editor, 75, 10);
        compareFoldHierarchyDump();
    
        //39 collapse Multi-line InnerClass Constructor Fold
        //            applet = new |JApplet();
        collapseFoldAtCaretPosition(editor, 78, 26);
        compareFoldHierarchyDump();

        //40 collapse One-line InnerClass Method Javadoc Fold
        //        /**| One-line InnerClass Method Javadoc Fold */
        collapseFoldAtCaretPosition(editor, 82, 12);
        compareFoldHierarchyDump();

        //41 collapse One-line InnerClass Method Fold
        //        public void methodOne(){ }| // One-line InnerClass Method Fold
        collapseFoldAtCaretPosition(editor, 83, 34);
        compareFoldHierarchyDump();

        //42 collapse Multi-line InnerClass Method Javadoc Fold 
        //         *|/
        collapseFoldAtCaretPosition(editor, 87, 11);
        compareFoldHierarchyDump();
    
        //43 collapse Multi-line InnerClass Method Fold
        //        }|
        collapseFoldAtCaretPosition(editor, 90, 10);
        compareFoldHierarchyDump();
    
        //44 collapse Whole InnerClass, the caret is just one characted behind method fold endOffset 
        //        } |
        collapseFoldAtCaretPosition(editor, 90, 11);
        compareFoldHierarchyDump();
    
        //45 expand Whole InnerClass
        //    public static class InnerClassThree[...]|
        expandFoldAtCaretPosition(editor, 93, 6);
        compareFoldHierarchyDump();

        //46 collapse first method
        //|            public void firstMethod(){ }  public void secondMethod(){ } public void thirdMethod(){ }
        collapseFoldAtCaretPosition(editor, 92, 1);
        compareFoldHierarchyDump();

        //47 expand first method
        //|            public void firstMethod()[...]  public void secondMethod(){ } public void thirdMethod(){ }
        expandFoldAtCaretPosition(editor, 92, 1);
        compareFoldHierarchyDump();

        //48 collapse second method
        //            public void firstMethod(){ } | public void secondMethod(){ } public void thirdMethod(){ }
        collapseFoldAtCaretPosition(editor, 92, 38);
        compareFoldHierarchyDump();

        //49 expand second method
        //            public void firstMethod(){ } | public void secondMethod()[...] public void thirdMethod(){ }
        expandFoldAtCaretPosition(editor, 92, 38);
        compareFoldHierarchyDump();
    
        //51 collapse third method
        //            public void firstMethod(){ }  public void secondMethod(){ } public void thirdMe|thod(){ }
        collapseFoldAtCaretPosition(editor, 92, 86);
        compareFoldHierarchyDump();

        //51 expand second method
        //            public void firstMethod(){ }  public void secondMethod(){ } public void thirdMe|thod()[...]
        expandFoldAtCaretPosition(editor, 92, 86);
        compareFoldHierarchyDump();
    
        //52 collapse all three methods
        //            public void firstMethod(){|}  public void second|Method(){ } public void thirdMethod(){ }|
        collapseFoldAtCaretPosition(editor, 92, 35);
        collapseFoldAtCaretPosition(editor, 92, 56);
        collapseFoldAtCaretPosition(editor, 92, 94);
        compareFoldHierarchyDump();

        
        
        //---------------------------------------------------
        // Inner class in Inner Class testing
        
        //53 collapse a method javadoc
        //             */|
        collapseFoldAtCaretPosition(editor, 131, 16);
        compareFoldHierarchyDump();
        
        //54 collapse a method
        //            public void method|Two(){ // Multi-line InnerClassInInnerClass Method Fold
        collapseFoldAtCaretPosition(editor, 132, 31);
        compareFoldHierarchyDump();
        
        //55 multi line methods
        //            public vo|id firstMethod(){ }  public void secondMethod(){|} public void thirdMethod(){ }|
        collapseFoldAtCaretPosition(editor, 136, 22);
        collapseFoldAtCaretPosition(editor, 136, 69);
        collapseFoldAtCaretPosition(editor, 136, 98);
        compareFoldHierarchyDump();

        
        //--------------------------------------------------------------
        // 56 Collapse All Folds
        collapseAllFolds(editor);
        compareFoldHierarchyDump();
        
        //--------------------------------------------------------------
        // 57 Collapse All Folds
        expandAllFolds(editor);
        compareFoldHierarchyDump();
        
        closeFileWithDiscard();

    }
    
      public static void main(String[] args) {
          TestRunner.run(JavaFoldsTest.class);
      }
      
      public static Test suite() {
          return NbModuleSuite.create(
                  NbModuleSuite.createConfiguration(JavaFoldsTest.class).addTest("testJavaFolds").enableModules(".*").clusters(".*"));
      }

    
}
