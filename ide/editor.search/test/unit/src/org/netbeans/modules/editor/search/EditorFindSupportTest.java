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

package org.netbeans.modules.editor.search;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JTextArea;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @author David Strupl
 */
public class EditorFindSupportTest {

    public EditorFindSupportTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of replaceAll method, of class EditorFindSupport for regressions in #165497.
     */
    @Test
    public void testReplaceAllFinishes_165497_a() throws Exception {
        final Map<String, Object> props = new HashMap<>();
        props.put(EditorFindSupport.FIND_WHAT, "ahoj");
        props.put(EditorFindSupport.FIND_REPLACE_WITH, "ahoj");
        props.put(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_INC_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WRAP_SEARCH, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_MATCH_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_SMART_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_REG_EXP, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_HISTORY, new Integer(30));

        final EditorFindSupport instance = EditorFindSupport.getInstance();
        final boolean [] finished = new boolean[1];
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                instance.replaceAllImpl(props,new JTextArea("0123456789 ahoj svete"));
                finished[0] = true;
            }
        });
        t.start();
        Thread.sleep(2000);
        if (!finished[0]) {
            t.interrupt();
        }
        assertTrue(finished[0]);
    }
    /**
     * Test of replaceAll method, of class EditorFindSupport for regressions in #165497.
     */
    @Test
    public void testReplaceAllFinishes_165497_b() throws Exception {
        final Map<String, Object> props = new HashMap<>();
        props.put(EditorFindSupport.FIND_WHAT, "ahoj");
        props.put(EditorFindSupport.FIND_REPLACE_WITH, "ahoj");
        props.put(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_INC_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_WRAP_SEARCH, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_MATCH_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_SMART_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_REG_EXP, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_HISTORY, new Integer(30));

        final EditorFindSupport instance = EditorFindSupport.getInstance();
        final boolean [] finished = new boolean[1];
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                instance.replaceAllImpl(props,new JTextArea("0123456789 ahoj svete"));
                finished[0] = true;
            }
        });
        t.start();
        Thread.sleep(2000);
        if (!finished[0]) {
            t.interrupt();
        }
        assertTrue(finished[0]);
    }

    /**
     * Test of replaceAll method, of class EditorFindSupport for regressions in #165497.
     */
    @Test
    public void testReplaceAllFinishes_165497_c() throws Exception {
        final Map<String, Object> props = new HashMap<>();
        props.put(EditorFindSupport.FIND_WHAT, "ahoj");
        props.put(EditorFindSupport.FIND_REPLACE_WITH, "ahoj");
        props.put(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_INC_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WRAP_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_MATCH_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_SMART_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_REG_EXP, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_HISTORY, new Integer(30));

        final EditorFindSupport instance = EditorFindSupport.getInstance();
        final boolean [] finished = new boolean[1];
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                instance.replaceAllImpl(props,new JTextArea("0123456789 ahoj svete"));
                finished[0] = true;
            }
        });
        t.start();
        Thread.sleep(2000);
        if (!finished[0]) {
            t.interrupt();
        }
        assertTrue(finished[0]);
    }
    /**
     * Test of replaceAll method, of class EditorFindSupport for regressions in #165497.
     */
    @Test
    public void testReplaceAllFinishes_165497_d() throws Exception {
        final Map<String, Object> props = new HashMap<>();
        props.put(EditorFindSupport.FIND_WHAT, "ahoj");
        props.put(EditorFindSupport.FIND_REPLACE_WITH, "ahoj");
        props.put(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_INC_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_WRAP_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_MATCH_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_SMART_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_REG_EXP, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_HISTORY, new Integer(30));

        final EditorFindSupport instance = EditorFindSupport.getInstance();
        final boolean [] finished = new boolean[1];
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                instance.replaceAllImpl(props,new JTextArea("0123456789 ahoj svete"));
                finished[0] = true;
            }
        });
        t.start();
        Thread.sleep(2000);
        if (!finished[0]) {
            t.interrupt();
        }
        assertTrue(finished[0]);
    }
    /**
     * Test of replaceAll method, of class EditorFindSupport for regressions in #165497.
     */
    @Test
    public void testReplaceAllFinishes_165497_e() throws Exception {
        final Map<String, Object> props = new HashMap<>();
        props.put(EditorFindSupport.FIND_WHAT, "a");
        props.put(EditorFindSupport.FIND_REPLACE_WITH, "a");
        props.put(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_INC_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_WRAP_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_MATCH_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_SMART_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_REG_EXP, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_HISTORY, new Integer(30));

        final EditorFindSupport instance = EditorFindSupport.getInstance();
        final boolean [] finished = new boolean[1];
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                JTextArea ta = new JTextArea("aaaa");
                ta.setCaretPosition(2);
                instance.replaceAllImpl(props, ta);
                finished[0] = true;
            }
        });
        t.start();
        Thread.sleep(2000);
        if (!finished[0]) {
            t.interrupt();
        }
        assertTrue(finished[0]);
    }

    /**
     * Test of replaceAll method, of class EditorFindSupport for regressions in #165497.
     */
    @Test
    public void testReplaceAllFinishes_165497_f() throws Exception {
        final Map<String, Object> props = new HashMap<>();
        props.put(EditorFindSupport.FIND_WHAT, "a");
        props.put(EditorFindSupport.FIND_REPLACE_WITH, "A");
        props.put(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_INC_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_WRAP_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_MATCH_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_SMART_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_REG_EXP, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_HISTORY, new Integer(30));

        final EditorFindSupport instance = EditorFindSupport.getInstance();
        final boolean [] finished = new boolean[1];
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                JTextArea ta = new JTextArea("aaaa");
                instance.replaceAllImpl(props, ta);
                finished[0] = true;
            }
        });
        t.start();
        Thread.sleep(2000);
        if (!finished[0]) {
            t.interrupt();
        }
        assertTrue(finished[0]);
    }
    /**
     * Test of replaceAll method, of class EditorFindSupport.
     */
    @Test
    public void testReplaceAll1() throws Exception {
        final Map<String, Object> props = new HashMap<>();
        props.put(EditorFindSupport.FIND_WHAT, "ahoj");
        props.put(EditorFindSupport.FIND_REPLACE_WITH, "xxx");
        props.put(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_INC_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WRAP_SEARCH, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_MATCH_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_SMART_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_REG_EXP, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_HISTORY, new Integer(30));

        final EditorFindSupport instance = EditorFindSupport.getInstance();
        JTextArea ta = new JTextArea("0123456789 ahoj ahoj svete");
        ta.setCaretPosition(0);
        instance.replaceAllImpl(props, ta);
        assertEquals("0123456789 xxx xxx svete", ta.getText());
    }
    /**
     * Test of replaceAll method, of class EditorFindSupport.
     */
    @Test
    public void testReplaceAll2() throws Exception {
        final Map<String, Object> props = new HashMap<>();
        props.put(EditorFindSupport.FIND_WHAT, "ahoj");
        props.put(EditorFindSupport.FIND_REPLACE_WITH, "xxx");
        props.put(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_INC_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WRAP_SEARCH, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_MATCH_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_SMART_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_REG_EXP, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_HISTORY, new Integer(30));

        final EditorFindSupport instance = EditorFindSupport.getInstance();
        JTextArea ta = new JTextArea("0123456789 ahoj ahoj svete");
        ta.setCaretPosition(15);
        instance.replaceAllImpl(props, ta);
        assertEquals("0123456789 ahoj xxx svete", ta.getText());
    }
//    /**
//     * Test of replaceAll method, of class EditorFindSupport.
//     * Commented out bacause it uses FIND_WRAP_SEARCH, Boolean.FALSE
//     */
//    @Test
//    public void testReplaceAll3() throws Exception {
//        final Map<String, Object> props = new HashMap<String, Object>();
//        props.put(EditorFindSupport.FIND_WHAT, "ahoj");
//        props.put(EditorFindSupport.FIND_REPLACE_WITH, "xxx");
//        props.put(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
//        props.put(EditorFindSupport.FIND_INC_SEARCH, Boolean.TRUE);
//        props.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.TRUE);
//        props.put(EditorFindSupport.FIND_WRAP_SEARCH, Boolean.FALSE);
//        props.put(EditorFindSupport.FIND_MATCH_CASE, Boolean.FALSE);
//        props.put(EditorFindSupport.FIND_SMART_CASE, Boolean.FALSE);
//        props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.FALSE);
//        props.put(EditorFindSupport.FIND_REG_EXP, Boolean.FALSE);
//        props.put(EditorFindSupport.FIND_HISTORY, new Integer(30));
//
//        final EditorFindSupport instance = EditorFindSupport.getInstance();
//        JTextArea ta = new JTextArea("0123456789 ahoj ahoj svete");
//        ta.setCaretPosition(15);
//        instance.replaceAllImpl(props, ta);
//        assertEquals("0123456789 xxx ahoj svete", ta.getText());
//    }
    /**
     * Test of replaceAll method, of class EditorFindSupport.
     */
    @Test
    public void testReplaceAll4() throws Exception {
        final Map<String, Object> props = new HashMap<>();
        props.put(EditorFindSupport.FIND_WHAT, "ahoj");
        props.put(EditorFindSupport.FIND_REPLACE_WITH, "xxx");
        props.put(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_INC_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WRAP_SEARCH, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_MATCH_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_SMART_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_REG_EXP, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_HISTORY, new Integer(30));

        final EditorFindSupport instance = EditorFindSupport.getInstance();
        JTextArea ta = new JTextArea("0123456789 ahoj ahoj svete");
        ta.setCaretPosition(18);
        instance.replaceAllImpl(props, ta);
        assertEquals("0123456789 ahoj ahoj svete", ta.getText());
    }
//    /**
//     * Test of replaceAll method, of class EditorFindSupport.
//     * Commented out bacause it uses FIND_WRAP_SEARCH, Boolean.FALSE
//     */
//    @Test
//    public void testReplaceAll5() throws Exception {
//        final Map<String, Object> props = new HashMap<String, Object>();
//        props.put(EditorFindSupport.FIND_WHAT, "ahoj");
//        props.put(EditorFindSupport.FIND_REPLACE_WITH, "xxx");
//        props.put(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
//        props.put(EditorFindSupport.FIND_INC_SEARCH, Boolean.TRUE);
//        props.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.TRUE);
//        props.put(EditorFindSupport.FIND_WRAP_SEARCH, Boolean.FALSE);
//        props.put(EditorFindSupport.FIND_MATCH_CASE, Boolean.FALSE);
//        props.put(EditorFindSupport.FIND_SMART_CASE, Boolean.FALSE);
//        props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.FALSE);
//        props.put(EditorFindSupport.FIND_REG_EXP, Boolean.FALSE);
//        props.put(EditorFindSupport.FIND_HISTORY, new Integer(30));
//
//        final EditorFindSupport instance = EditorFindSupport.getInstance();
//        JTextArea ta = new JTextArea("0123456789 ahoj ahoj svete");
//        ta.setCaretPosition(1);
//        instance.replaceAllImpl(props, ta);
//        assertEquals("0123456789 ahoj ahoj svete", ta.getText());
////        ta.setCaretPosition(0);
////        instance.replaceAllImpl(props, ta);
////        assertEquals("0123456789 ahoj ahoj svete", ta.getText());
//    }
    /**
     * Test of replaceAll method, of class EditorFindSupport.
     */
    @Test
    public void testReplaceAll6() throws Exception {
        final Map<String, Object> props = new HashMap<>();
        props.put(EditorFindSupport.FIND_WHAT, "ahoj");
        props.put(EditorFindSupport.FIND_REPLACE_WITH, "xxx");
        props.put(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_INC_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_WRAP_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_MATCH_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_SMART_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_REG_EXP, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_HISTORY, new Integer(30));

        final EditorFindSupport instance = EditorFindSupport.getInstance();
        JTextArea ta = new JTextArea("0123456789 ahoj ahoj svete");
        ta.setCaretPosition(18);
        instance.replaceAllImpl(props, ta);
        assertEquals("0123456789 xxx xxx svete", ta.getText());
    }
    /**
     * Test of replaceAll method, of class EditorFindSupport.
     */
    @Test
    public void testReplaceAll7() throws Exception {
        final Map<String, Object> props = new HashMap<>();
        props.put(EditorFindSupport.FIND_WHAT, "ahoj");
        props.put(EditorFindSupport.FIND_REPLACE_WITH, "xxx");
        props.put(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_INC_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_WRAP_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_MATCH_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_SMART_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_REG_EXP, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_HISTORY, new Integer(30));

        final EditorFindSupport instance = EditorFindSupport.getInstance();
        JTextArea ta = new JTextArea("0123456789 ahojahojahoj svete");
        ta.setCaretPosition(ta.getText().length()-1);
        instance.replaceAllImpl(props, ta);
        assertEquals("0123456789 xxxxxxxxx svete", ta.getText());
    }
    /**
     * Test of replaceAll method, of class EditorFindSupport.
     */
    @Test
    public void testReplaceAll8() throws Exception {
        final Map<String, Object> props = new HashMap<>();
        props.put(EditorFindSupport.FIND_WHAT, "a");
        props.put(EditorFindSupport.FIND_REPLACE_WITH, "b");
        props.put(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_INC_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WRAP_SEARCH, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_MATCH_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_SMART_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_REG_EXP, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_HISTORY, new Integer(30));

        final EditorFindSupport instance = EditorFindSupport.getInstance();
        JTextArea ta = new JTextArea("aa");
        instance.replaceAllImpl(props, ta);
        assertEquals("bb", ta.getText());
    }
    /**
     * Test of replaceAll method, of class EditorFindSupport.
     */
    @Test
    public void testReplaceAll9() throws Exception {
        final Map<String, Object> props = new HashMap<>();
        props.put(EditorFindSupport.FIND_WHAT, "a");
        props.put(EditorFindSupport.FIND_REPLACE_WITH, "b");
        props.put(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_INC_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WRAP_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_MATCH_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_SMART_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_REG_EXP, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_HISTORY, new Integer(30));

        final EditorFindSupport instance = EditorFindSupport.getInstance();
        JTextArea ta = new JTextArea("aa");
        ta.setCaretPosition(1);
        instance.replaceAllImpl(props, ta);
        assertEquals("bb", ta.getText());
    }
    /**
     * Test of replaceAll method, of class EditorFindSupport.
     */
    @Test
    public void testReplaceAll10() throws Exception {
        final Map<String, Object> props = new HashMap<>();
        props.put(EditorFindSupport.FIND_WHAT, "a");
        props.put(EditorFindSupport.FIND_REPLACE_WITH, "b");
        props.put(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_INC_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WRAP_SEARCH, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_MATCH_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_SMART_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_REG_EXP, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_HISTORY, new Integer(30));

        final EditorFindSupport instance = EditorFindSupport.getInstance();
        JTextArea ta = new JTextArea("aa");
        ta.setCaretPosition(1);
        instance.replaceAllImpl(props, ta);
        assertEquals("ab", ta.getText());
    }
//    /**
//     * Test of replaceAll method, of class EditorFindSupport.
//     * Commented out bacause it uses FIND_WRAP_SEARCH, Boolean.FALSE
//     */
//    @Test
//    public void testReplaceAll11() throws Exception {
//        final Map<String, Object> props = new HashMap<String, Object>();
//        props.put(EditorFindSupport.FIND_WHAT, "a");
//        props.put(EditorFindSupport.FIND_REPLACE_WITH, "b");
//        props.put(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
//        props.put(EditorFindSupport.FIND_INC_SEARCH, Boolean.TRUE);
//        props.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.TRUE);
//        props.put(EditorFindSupport.FIND_WRAP_SEARCH, Boolean.FALSE);
//        props.put(EditorFindSupport.FIND_MATCH_CASE, Boolean.FALSE);
//        props.put(EditorFindSupport.FIND_SMART_CASE, Boolean.FALSE);
//        props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.FALSE);
//        props.put(EditorFindSupport.FIND_REG_EXP, Boolean.FALSE);
//        props.put(EditorFindSupport.FIND_HISTORY, new Integer(30));
//
//        final EditorFindSupport instance = EditorFindSupport.getInstance();
//        JTextArea ta = new JTextArea("aa aaaa");
//        ta.setCaretPosition(1);
//        instance.replaceAllImpl(props, ta);
//        assertEquals("ba aaaa", ta.getText());
//    }
    @Test
    public void testReplaceFind() throws Exception {
        final Map<String, Object> props = new HashMap<>();
        props.put(EditorFindSupport.FIND_WHAT, "a");
        props.put(EditorFindSupport.FIND_REPLACE_WITH, "b");
        props.put(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_INC_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WRAP_SEARCH, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_MATCH_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_SMART_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_REG_EXP, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_HISTORY, new Integer(30));
        
        final EditorFindSupport instance = EditorFindSupport.getInstance();
        JTextArea ta = new JTextArea("aaaa");
        ta.setCaretPosition(0);
        instance.replaceImpl(props, false, ta);
        instance.findReplaceImpl(null, props, false, ta);
        assertEquals("baaa", ta.getText());
        instance.replaceImpl(props, false, ta);
        instance.findReplaceImpl(null, props, false, ta);
        assertEquals("bbaa", ta.getText());
        instance.replaceImpl(props, false, ta);
        instance.findReplaceImpl(null, props, false, ta);
        assertEquals("bbba", ta.getText());
    }
    
    @Test
    public void testReplaceFindFocused() throws Exception {
        final Map<String, Object> props = new HashMap<>();
        props.put(EditorFindSupport.FIND_WHAT, "a");
        props.put(EditorFindSupport.FIND_REPLACE_WITH, "b");
        props.put(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_INC_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WRAP_SEARCH, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_MATCH_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_SMART_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_REG_EXP, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_HISTORY, new Integer(30));
        
        final EditorFindSupport instance = EditorFindSupport.getInstance();
        JTextArea ta = new JTextArea("aaaa");
        ta.setCaretPosition(0);
        instance.setFocusedTextComponent(ta);
        instance.replace(props, false);
        instance.find(props, false);
        assertEquals("baaa", ta.getText());
        instance.replace(props, false);
        instance.find(props, false);
        assertEquals("bbaa", ta.getText());
        instance.replace(props, false);
        instance.find(props, false);
        assertEquals("bbba", ta.getText());
    }
    
    @Test
    public void testReplaceFindNewLine() throws Exception {
        final Map<String, Object> props = new HashMap<>();
        props.put(EditorFindSupport.FIND_WHAT, "foo");
        props.put(EditorFindSupport.FIND_REPLACE_WITH, "bar");
        props.put(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_INC_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WRAP_SEARCH, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_MATCH_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_SMART_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_REG_EXP, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_HISTORY, new Integer(30));
        
        final EditorFindSupport instance = EditorFindSupport.getInstance();
        JTextArea ta = new JTextArea("foo\nfoo\nfoo\nfoo\n");
        ta.setCaretPosition(0);
//        instance.setFocusedTextComponent(ta);
        instance.replaceImpl(props, false, ta);
        instance.findReplaceImpl(null, props, false, ta);
        assertEquals("bar\nfoo\nfoo\nfoo\n", ta.getText());
        instance.replaceImpl(props, false, ta);
        instance.findReplaceImpl(null, props, false, ta);
        assertEquals("bar\nbar\nfoo\nfoo\n", ta.getText());
        instance.replaceImpl(props, false, ta);
        instance.findReplaceImpl(null, props, false, ta);
        assertEquals("bar\nbar\nbar\nfoo\n", ta.getText());
    }
}
