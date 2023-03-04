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
package org.netbeans.test.java.hints;

import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Jiri Prox
 */
public class ImplAllAbstractTest extends HintsTestCase{

    public ImplAllAbstractTest(String name) {
        super(name);
    }
    
    public void testImplementAbstract() {
        String file = "AllAbs";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(42,1);
        new EventTool().waitNoEvent(750);
        String pattern = ".*public void run\\(\\) \\{.*throw new UnsupportedOperationException\\(\"Not supported yet.\"\\);.*\\}.*";
        useHint("Implement",new String[]{"Implement all abstract methods"},pattern);
    }
    
    public void testImplementAbstract2() {
        String file = "AllAbs2";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(44,1);
        new EventTool().waitNoEvent(750);
        String pattern = ".*public int getRowCount\\(\\) \\{.*" +
                "throw new UnsupportedOperationException\\(\"Not supported yet.\"\\);.*" +
                "\\}.*" +
                "public int getColumnCount\\(\\) \\{.*" +
                "throw new UnsupportedOperationException\\(\"Not supported yet.\"\\);.*" +
                "\\}.*"+
                "public Object getValueAt\\(int rowIndex, int columnIndex\\) \\{.*"+
                "throw new UnsupportedOperationException\\(\"Not supported yet.\"\\);.*" +
                "\\}.*";
        useHint("Implement",new String[]{"Implement all abstract methods"},pattern);
    }
    
    public void testImplementAbstract3() {
        String file = "AllAbs2";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(49,1);
        new EventTool().waitNoEvent(750);
        String pattern = ".*\\{.*public int compareTo\\(T o\\) \\{.*throw new UnsupportedOperationException\\(\"Not supported yet.\"\\);.*\\}.*\\}.*";
        useHint("Implement",new String[]{"Implement all abstract methods"},pattern);
    }
    
    public static void main(String[] args) {
        TestRunner.run(ImplAllAbstractTest.class);
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(ImplAllAbstractTest.class).enableModules(".*").clusters(".*"));
    }
    
}
