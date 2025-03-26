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
package org.netbeans.test.java.editor.occurrences;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.editor.base.options.MarkOccurencesSettingsNames;
import org.netbeans.modules.java.editor.base.semantic.MarkOccurrencesHighlighterBase;
import org.netbeans.modules.java.editor.options.MarkOccurencesSettings;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;


/**
 *
 * @author Jiri Prox
 */
public class MarkOccurrencesTest extends NbTestCase {
    
    private FileObject fileObject;
       
    private JEditorPane editorPane;
    
    private DataObject dataObject;
    
    private JavaSource js;
    
    private static final SimpleMark[] EMPTY = new MarkOccurrencesTest.SimpleMark[]{};
    
    private final SimpleMark[] TEST_TYPE = new SimpleMark[] {
        new SimpleMark(270,274,null),
        new SimpleMark(195,199,null),
        new SimpleMark(160,164,null),
        new SimpleMark(64,68,null)
    };
    
    private final SimpleMark[] TEST_METHOD = new SimpleMark[] {
        new SimpleMark(259,265,null),
        new SimpleMark(336,342,null),
        new SimpleMark(381,387,null),
        new SimpleMark(183,189,null),
        new SimpleMark(150,156,null)
    };
    
    private final SimpleMark[] TEST_FIELD = new SimpleMark[] {
        new SimpleMark(141,144,null),
        new SimpleMark(102,105,null),
        new SimpleMark(186,189,null),
        new SimpleMark(59,62,null),
        new SimpleMark(224,227,null)
    };
    
    private final SimpleMark[] TEST_CONST = new SimpleMark[] {
        new SimpleMark(141,146,null),
        new SimpleMark(243,248,null),
        new SimpleMark(182,187,null),
        new SimpleMark(77,82,null),
        new SimpleMark(273,278,null)
    };
    
    
    private final SimpleMark[] TEST_LOCAL1 = new SimpleMark[] {
        new SimpleMark(180,186,null),
        new SimpleMark(105,111,null),
        new SimpleMark(211,217,null)
    };
    
    private final SimpleMark[] TEST_LOCAL2 = new SimpleMark[] {
        new SimpleMark(140,147,null),
        new SimpleMark(243,250,null),
        new SimpleMark(287,294,null),
        new SimpleMark(233,240,null),
        new SimpleMark(203,210,null)
    };
    
    private final SimpleMark[] TEST_THROWING1 = new SimpleMark[] {
        new SimpleMark(352,373,null)
    };
    
    private final SimpleMark[] TEST_THROWING2 = new SimpleMark[] {
        new SimpleMark(615,625,null)
    };
    
    private final SimpleMark[] TEST_EXIT = new SimpleMark[] {
        new SimpleMark(635,644,null),
        new SimpleMark(352,373,null),
        new SimpleMark(615,625,null),
        new SimpleMark(460,472,null)
    };
    
    private final SimpleMark[] TEST_IMPLEMENT1 = new SimpleMark[] {
        new SimpleMark(123,126,null)
    };
    
    private final SimpleMark[] TEST_IMPLEMENT2 = new SimpleMark[] {
        new SimpleMark(224,233,null)
    };
    
    private final SimpleMark[] TEST_KEEP = new SimpleMark[] {
        new SimpleMark(40,45,null)
                
    };
    
    private final SimpleMark[] TEST_LABELS1 = new SimpleMark[] {
        new SimpleMark(414,415,null),
        new SimpleMark(161,166,null),
        new SimpleMark(282,287,null),
        new SimpleMark(93,98,null)
    };
    
    private final SimpleMark[] TEST_LABELS2 = new SimpleMark[] {
        new SimpleMark(184,189,null),
        new SimpleMark(331,336,null),
        new SimpleMark(383,384,null)
    };
    
    private final SimpleMark[] TEST_OVERRIDE = new SimpleMark[] {
        new SimpleMark(271,284,null),
        new SimpleMark(178,191,null)
    };
    
    public Document getDocument() {
        try {
            DataObject d = DataObject.find(fileObject);
            EditorCookie ec = d.getCookie(EditorCookie.class);
            
            if (ec == null)
                return null;
            
            return ec.getDocument();
        } catch (DataObjectNotFoundException donfe) {
            fail();
        }
        return null;
    }
    
    public MarkOccurrencesTest(String name) {
        super(name);
    }
    
    private class SimpleMark implements Comparable {
        int start;
        int end;
        Color color;
        
        public SimpleMark(int start, int end, Color color) {
            this.start = start;
            this.end = end;
            this.color = color;
        }
        
        public int compareTo(Object o) {
            return new Integer(start).compareTo(((SimpleMark)o).start);
        }
        
    }
    
    private void closeFile() {
        EditorCookie ec = null;
        if(dataObject!=null) ec = dataObject.getCookie(EditorCookie.class);
        if(ec != null) ec.close();
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.out.println("##### "+this.getName()+" #####");
    }
       
    @Override
    protected void tearDown() throws Exception {
        closeFile();
        super.tearDown();
    }
        
    private List<int[]> foundMarks;
    
    class MyTask implements Task<CompilationController> {
    
        public List<int[]> process(CompilationController parameter, Preferences node, Document doc, int caretPosition,MarkOccurrencesHighlighterBase moh) {
            try {                
                Method[] methods = moh.getClass().getDeclaredMethods();
                Method procesImpl = null;
                for (Method method : methods) {                    
                    if(method.getName().equals("processImpl")) procesImpl = method;
                }
                if(procesImpl==null) return Collections.EMPTY_LIST;
                procesImpl.setAccessible(true);
                Object result = procesImpl.invoke(moh, parameter, node, doc, caretPosition);                
                return  (List<int[]>) result;
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            } catch (SecurityException ex) {
                Exceptions.printStackTrace(ex);
            }
            return Collections.EMPTY_LIST;
        }
        
        public void run(CompilationController parameter) throws Exception {
            parameter.toPhase(Phase.RESOLVED);
            foundMarks = null;
            Document doc = getDocument();
            Constructor c = MarkOccurrencesHighlighterBase.class.getDeclaredConstructor(FileObject.class);
            c.setAccessible(true);
            MarkOccurrencesHighlighterBase moh = (MarkOccurrencesHighlighterBase) c.newInstance(fileObject);
            int caretPosition = CaretAwareJavaSourceTaskFactory.getLastPosition(fileObject);
            Preferences node = MarkOccurencesSettings.getCurrentNode();
            //List<int[]> highlights = moh.processImpl(parameter, node, doc, caretPosition);
            List<int[]> highlights = process(parameter, node, doc, caretPosition, moh);
            foundMarks  = highlights;
        }
    }
    
    private void browse(String s) {
        int c = 0;
        for(int i=0;i<s.length();i++) {
            char r = s.charAt(i);
            if(r=='\n') r='&';
            if(c==0) System.out.print(i+" "+r);
            else System.out.print(r);
            if(c==9) System.out.println();
            c = (c+1) % 10;
        }
    }
    
    private JavaSource openFile(String name) throws DataObjectNotFoundException, IOException, InterruptedException, InvocationTargetException {
        String dataDir = getDataDir().getAbsoluteFile().getPath();
        File sample = new File(dataDir+"/projects/java_editor_test/src/markOccurrences",name);
        assertTrue("file "+sample.getAbsolutePath()+" does not exist",sample.exists());
        
        fileObject = FileUtil.toFileObject(sample);
        dataObject = DataObject.find(fileObject);
        JavaSource js = JavaSource.forFileObject(fileObject);                
        final EditorCookie ec = dataObject.getCookie(EditorCookie.class);
        ec.openDocument();
        ec.open();
                
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                JEditorPane[] panes = ec.getOpenedPanes();
                editorPane = panes[0];
                
            }
        });
        return js;
        
    }
    
    private void setAndCheck(int pos,SimpleMark[] marks) throws IOException {
        Caret c = editorPane.getCaret();
        c.setDot(pos);
        foundMarks=null;
        int cycles = 0;
        sleep(500);
        while(foundMarks==null && cycles<30) {
            sleep(200);
            cycles++;
            js.runUserActionTask(new MyTask() ,false);
        }        
        Arrays.sort(marks);        
        String etalon = "";
        for (int i = 0; i < marks.length; i++) {
            SimpleMark m = marks[i];
            etalon = etalon + "["+m.start+","+m.end+"] ";
        }
        String ref = "";
        //not locking, should be fine in tests:
        if(foundMarks==null) {
            ref = "";
        } else {            
            foundMarks.sort(new Comparator<int[]>(){

                public int compare(int[] o1, int[] o2) {
                    if(o1[0]<o2[0]) return -1;
                    if(o1[0]>o2[0]) return 1;
                    if(o1[1]<o2[1]) return -1;
                    if(o2[1]>o2[1]) return 1;                    
                    return 0;
                }
            });
            for(int[] mark:foundMarks) {                
                ref = ref + "[" + mark[0] + "," + mark[1] + "] ";
            }
        }        
        assertEquals(etalon, ref);
    }
    
    public void testType() throws Exception {
        SimpleMark[] marks = TEST_TYPE;
        js = openFile("Test.java");
        setAndCheck(66, marks);
        setAndCheck(272, marks);

    }

    public void testMethod() throws Exception {
        SimpleMark[] marks = TEST_METHOD;
        js = openFile("Test2.java");
        setAndCheck(153, marks);
        setAndCheck(185, marks);
        setAndCheck(260, marks);
        setAndCheck(340, marks);
        setAndCheck(385, marks);
    }

    public void testField() throws Exception {
        SimpleMark[] marks = TEST_FIELD;
        js = openFile("Test3.java");
        setAndCheck(61, marks);
        setAndCheck(104, marks);
        setAndCheck(142, marks);
        setAndCheck(188, marks);
        setAndCheck(225, marks);
    }

    public void testConstant() throws Exception {
        SimpleMark[] marks = TEST_CONST;
        js = openFile("Test4.java");
        setAndCheck(78, marks);
        setAndCheck(143, marks);
        setAndCheck(184, marks);
        setAndCheck(246, marks);
        setAndCheck(276, marks);
    }

    public void testLocal() throws Exception {
        SimpleMark[] marks = TEST_LOCAL1;
        js = openFile("Test5.java");

        setAndCheck(109, marks);
        setAndCheck(182, marks);
        setAndCheck(217, marks);
        marks = TEST_LOCAL2;
        setAndCheck(141, marks);
        setAndCheck(208, marks);
        setAndCheck(238, marks);
        setAndCheck(248, marks);
        setAndCheck(290, marks);
    }

    public void testThrowingPoints() throws Exception {
        SimpleMark[] marks = TEST_THROWING1;
        js = openFile("Test6.java");
        setAndCheck(295, marks);

        marks = TEST_THROWING2;
        setAndCheck(315, marks);
    }

    public void testExitPoints() throws Exception {
        SimpleMark[] marks = TEST_EXIT;
        js = openFile("Test6.java");
        setAndCheck(250, marks);
    }

    public void testImplementing() throws Exception {
        SimpleMark[] marks = TEST_IMPLEMENT1;
        js = openFile("Test8.java");
        setAndCheck(60, marks);

        marks = TEST_IMPLEMENT2;
        setAndCheck(70, marks);
    }

    public void testOverriding() throws Exception {
        SimpleMark[] marks = TEST_OVERRIDE;
        js = openFile("Test9.java");
        setAndCheck(130, marks);
    }

    public void testLabels() throws Exception {
        SimpleMark[] marks = TEST_LABELS1;
        js = openFile("Testa.java");
        setAndCheck(162, marks);
        setAndCheck(284, marks);

        marks = TEST_LABELS2;
        setAndCheck(333, marks);

    }

    private void setAndFlush(String key,boolean value) {
        try {
            Preferences pref = MarkOccurencesSettings.getCurrentNode();
            pref.putBoolean(key, value);
            pref.flush();            
        } catch (BackingStoreException ex) {
            fail("Error while storing settings");
        }
    }

    public void testOptions() throws Exception {        
//        setAndFlush(MarkOccurencesSettingsNames.ON_OFF, false);
//        js = openFile("Test.java");
//        setAndCheck(80, EMPTY);
//        setAndCheck(205, EMPTY);
//        setAndFlush(MarkOccurencesSettingsNames.ON_OFF, true);
//        setAndCheck(168, new SimpleMark[]{new SimpleMark(166,170,null)});
//        closeFile();
        
        setAndFlush(MarkOccurencesSettingsNames.BREAK_CONTINUE, false);
        js = openFile("Testa.java");
        setAndCheck(162, EMPTY);        
        setAndFlush(MarkOccurencesSettingsNames.BREAK_CONTINUE, true);        
        setAndCheck(162, TEST_LABELS1);
        closeFile();
        
        setAndFlush(MarkOccurencesSettingsNames.CONSTANTS, false);
        js = openFile("Test4.java");
        setAndCheck(78, EMPTY);       
        setAndFlush(MarkOccurencesSettingsNames.CONSTANTS, true);        
        setAndCheck(78, TEST_CONST);
        closeFile();
        
        setAndFlush(MarkOccurencesSettingsNames.EXCEPTIONS, false);
        js = openFile("Test6.java");
        setAndCheck(295, new SimpleMark[]{new SimpleMark(73,94,null),new SimpleMark(281,302,null)});        
        setAndFlush(MarkOccurencesSettingsNames.EXCEPTIONS, true);        
        setAndCheck(295, TEST_THROWING1);
        closeFile();
        
        setAndFlush(MarkOccurencesSettingsNames.EXIT, false);
        js = openFile("Test6.java");
        setAndCheck(250,  new SimpleMark[]{new SimpleMark(246,252,null),
                                           new SimpleMark(261,267,null),
                                           new SimpleMark(481,487,null)});
        setAndFlush(MarkOccurencesSettingsNames.EXIT, true);        
        setAndCheck(250, TEST_EXIT);
        closeFile();
        
        setAndFlush(MarkOccurencesSettingsNames.FIELDS, false);
        js = openFile("Test3.java");
        setAndCheck(61, EMPTY);
        setAndFlush(MarkOccurencesSettingsNames.FIELDS, true);
        setAndCheck(61, TEST_FIELD);
        closeFile();
        
        setAndFlush(MarkOccurencesSettingsNames.IMPLEMENTS, false);
        js = openFile("Test8.java");
        setAndCheck(60, new SimpleMark[]{new SimpleMark(57,65,null)});
        
        setAndFlush(MarkOccurencesSettingsNames.IMPLEMENTS, true);
        setAndCheck(60, TEST_IMPLEMENT1);
        closeFile();
        
        setAndFlush(MarkOccurencesSettingsNames.LOCAL_VARIABLES, false);
        js = openFile("Test5.java");
        setAndCheck(109, EMPTY);
        setAndFlush(MarkOccurencesSettingsNames.LOCAL_VARIABLES, true);
        setAndCheck(109, TEST_LOCAL1);
        closeFile();
        
        setAndFlush(MarkOccurencesSettingsNames.METHODS, false);
        js = openFile("Test2.java");
        setAndCheck(153, EMPTY);
        setAndFlush(MarkOccurencesSettingsNames.METHODS, true);
        setAndCheck(153, TEST_METHOD);
        closeFile();
        
        setAndFlush(MarkOccurencesSettingsNames.OVERRIDES, false);
        js = openFile("Test9.java");
        setAndCheck(130,new SimpleMark[]{new SimpleMark(77,94,null),
                                           new SimpleMark(124,141,null)
                                        });                 
        setAndFlush(MarkOccurencesSettingsNames.OVERRIDES, true);
        setAndCheck(130, TEST_OVERRIDE);
        closeFile();
        
        setAndFlush(MarkOccurencesSettingsNames.TYPES, false);
        js = openFile("Test.java");
        setAndCheck(66, EMPTY);
        setAndFlush(MarkOccurencesSettingsNames.TYPES, true);
        setAndCheck(66, TEST_TYPE);
        
    }
    
    
    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch(InterruptedException ie) {
            // ignored
        }
    }
    
    public static void main(String[] args) {
        TestRunner.run(MarkOccurrencesTest.class);
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(MarkOccurrencesTest.class).enableModules(".*").clusters(".*"));
    }

}
