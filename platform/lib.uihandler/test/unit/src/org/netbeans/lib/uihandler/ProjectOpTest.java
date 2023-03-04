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

package org.netbeans.lib.uihandler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.uihandler.ProjectOp;
import org.netbeans.lib.uihandler.TestHandler;

/**
 *
 * @author Jaroslav Tulach
 */
public class ProjectOpTest extends NbTestCase {
    private Logger LOG;

    public ProjectOpTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return null;//Level.FINEST;
    }

    @Override
    protected void setUp() throws Exception {
        LOG = Logger.getLogger("TEST-" + getName());
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public void testOpenAndCloseAProject() throws Exception {
        String what = "<record>" +
            "<date>2007-02-05T14:14:17</date>" +
            "<millis>1170681257194</millis>" +
            "<sequence>1148</sequence>" +
            "<logger>org.netbeans.ui.projects</logger>" +
            "<level>CONFIG</level>" +
            "<thread>11</thread>" +
            "<message>Closing 1 NbModuleProject Projects</message>" +
            "<key>UI_CLOSED_PROJECTS</key>" +
            "<catalog>&lt;null&gt;</catalog>" +
            "<param>org.netbeans.modules.apisupport.project.NbModuleProject</param>" +
            "<param>NbModuleProject</param>" +
            "<param>1</param>" +
            "</record>" +
            "<record>" +
              "<date>2007-02-06T09:08:03</date>" +
              "<millis>1170749283986</millis>" +
              "<sequence>1441</sequence>" +
              "<logger>org.netbeans.ui.actions.editor</logger>" +
              "<level>FINE</level>" +
              "<thread>11</thread>" +
              "<message>Invoking copy-to-clipboard implemented as org.netbeans.editor.BaseKit$CopyAction@e29e2c thru java.awt.event.ActionEvent[ACTION_PERFORMED,cmd=,when=1170749283985,modifiers=Ctrl] on org.openide.text.QuietEditorPane[,0,-931,1048x1485,layout=javax.swing.plaf.basic.BasicTextUI$UpdateHandler,alignmentX=0.0,alignmentY=0.0,border=javax.swing.plaf.basic.BasicBorders$MarginBorder@aec245,flags=296,maximumSize=,minimumSize=,preferredSize=,caretColor=java.awt.Color[r=255,g=255,b=255],disabledTextColor=javax.swing.plaf.ColorUIResource[r=184,g=207,b=229],editable=true,margin=java.awt.Insets[top=0,left=0,bottom=0,right=0],selectedTextColor=sun.swing.PrintColorUIResource[r=51,g=51,b=51],selectionColor=javax.swing.plaf.ColorUIResource[r=184,g=207,b=229],kit=org.netbeans.modules.editor.java.JavaKit@1e6cecc,typeHandlers=]</message>" +
              "<key>UI_ACTION_EDITOR</key>" +
              "<catalog>&lt;null&gt;</catalog>" +
              "<param>java.awt.event.ActionEvent[ACTION_PERFORMED,cmd=,when=1170749283985,modifiers=Ctrl] on org.openide.text.QuietEditorPane[,0,-931,1041x1515,layout=javax.swing.plaf.basic.BasicTextUI$UpdateHandler,alignmentX=0.0,alignmentY=0.0,border=javax.swing.plaf.basic.BasicBorders$MarginBorder@aec245,flags=296,maximumSize=,minimumSize=,preferredSize=,caretColor=java.awt.Color[r=255,g=255,b=255],disabledTextColor=javax.swing.plaf.ColorUIResource[r=184,g=207,b=229],editable=true,margin=java.awt.Insets[top=0,left=0,bottom=0,right=0],selectedTextColor=sun.swing.PrintColorUIResource[r=51,g=51,b=51],selectionColor=javax.swing.plaf.ColorUIResource[r=184,g=207,b=229],kit=org.netbeans.modules.editor.java.JavaKit@1e6cecc,typeHandlers=]</param>" +
              "<param>java.awt.event.ActionEvent[ACTION_PERFORMED,cmd=,when=1170749283985,modifiers=Ctrl] on org.openide.text.QuietEditorPane[,0,-931,1048x1485,layout=javax.swing.plaf.basic.BasicTextUI$UpdateHandler,alignmentX=0.0,alignmentY=0.0,border=javax.swing.plaf.basic.BasicBorders$MarginBorder@aec245,flags=296,maximumSize=,minimumSize=,preferredSize=,caretColor=java.awt.Color[r=255,g=255,b=255],disabledTextColor=javax.swing.plaf.ColorUIResource[r=184,g=207,b=229],editable=true,margin=java.awt.Insets[top=0,left=0,bottom=0,right=0],selectedTextColor=sun.swing.PrintColorUIResource[r=51,g=51,b=51],selectionColor=javax.swing.plaf.ColorUIResource[r=184,g=207,b=229],kit=org.netbeans.modules.editor.java.JavaKit@1e6cecc,typeHandlers=]</param>" +
              "<param>org.netbeans.editor.BaseKit$CopyAction@e29e2c</param>" +
              "<param>org.netbeans.editor.BaseKit$CopyAction@e29e2c</param>" +
              "<param>copy-to-clipboard</param>" +
            "</record>" +
            "<record>" +
            "  <date>2007-02-06T09:05:59</date>" +
            "  <millis>1170749159147</millis>" +
            "  <sequence>1399</sequence>" +
            "  <logger>org.netbeans.ui.projects</logger>" +
            "  <level>CONFIG</level>" +
            "  <thread>11</thread>" +
            "  <message>Opening 1 NbModuleProject Projects</message>" +
            "  <key>UI_OPEN_PROJECTS</key>" +
            "  <catalog>&lt;null&gt;</catalog>" +
            "  <param>org.netbeans.modules.apisupport.project.NbModuleProject</param>" +
            "  <param>NbModuleProject</param>" +
            "  <param>1</param>" +
            "</record>";

        InputStream is = new ByteArrayInputStream(what.getBytes());
        TestHandler records = new TestHandler(is);
        LogRecord rec = records.read();
        LogRecord rec2 = records.read();
        LogRecord rec3 = records.read();
        is.close();
        
        ProjectOp op = ProjectOp.valueOf(rec);
        
        assertNotNull("This record is project operation", op);
        assertEquals("org.netbeans.modules.apisupport.project.NbModuleProject", op.getProjectType());
        assertEquals(-1, op.getDelta());
        assertEquals("NbModule", op.getProjectDisplayName());
        
        
        op = ProjectOp.valueOf(rec2);
        assertNull("No project operation", op);
        
        op = ProjectOp.valueOf(rec3);
        assertNotNull("This record is project operation", op);
        assertEquals("org.netbeans.modules.apisupport.project.NbModuleProject", op.getProjectType());
        assertEquals("One project added", 1, op.getDelta());
        assertEquals("NbModule", op.getProjectDisplayName());

    }

    public void testAntProjectStyle() throws Exception {
        
        

        String log = "<record>" +
        "  <date>2007-09-11T14:41:41</date>" +
        "  <millis>1189514501494</millis>" +
        "  <sequence>90</sequence>" +
        "  <level>CONFIG</level>" +
        "  <thread>12</thread>" +
        "  <message>Opening 1 FreeformProject Projects</message>" +
        "  <key>UI_OPEN_PROJECTS</key>" +
        "  <catalog>org.netbeans.modules.project.ui.Bundle</catalog>" +
        "  <param>org.netbeans.modules.ant.freeform.FreeformProject</param>" +
        "  <param>FreeformProject</param>" +
        "  <param>1</param>" +
        "</record>";

        InputStream is = new ByteArrayInputStream(log.getBytes());
        TestHandler records = new TestHandler(is);
        LogRecord rec = records.read();
        is.close();
        
        ProjectOp op = ProjectOp.valueOf(rec);
        assertEquals("One change", 1, op.getDelta());
        assertEquals("org.netbeans.modules.ant.freeform.FreeformProject", op.getProjectType());
        assertEquals("Freeform", op.getProjectDisplayName());
    }

    public void testProjectOpError() throws Exception {
        LogRecord rec = new LogRecord(Level.WARNING, "UI_CLOSED_PROJECTS");
        rec.setParameters(new Object[] {
            "kuk", "buch", 
            "org.netbeans.modules.xml.schema.model/1"
        });
        
        ProjectOp op = ProjectOp.valueOf(rec);
        assertNull("Not recognized log", op);
    }

    public void testDoesItHandleInitAsWell() throws Exception {
        TestHandler handler = new TestHandler(ProjectOpTest.class.getResourceAsStream("two-debugs-on-j2se.log"));
        List<ProjectOp> arr = new ArrayList<ProjectOp>();
        for (;;) {
            LogRecord rec = handler.read();
            if (rec == null) {
                break;
            }
            ProjectOp op = ProjectOp.valueOf(rec);
            if (op != null) {
                arr.add(op);
            }
        }
        
        assertEquals("Three project changes: " + arr, 3, arr.size());

        {
            ProjectOp init = arr.get(0);
            assertEquals("One open", 1, init.getDelta());
            assertEquals("On statup", true, init.isStartup());
            assertEquals("Type", "Web", init.getProjectDisplayName());
        }

        {
            ProjectOp close = arr.get(1);
            assertEquals("One closed", -1, close.getDelta());
            assertEquals("No statup", false, close.isStartup());
            assertEquals("Type", "Web", close.getProjectDisplayName());
        }

        {
            ProjectOp open = arr.get(2);
            assertEquals("One opened", 1, open.getDelta());
            assertEquals("No statup", false, open.isStartup());
            assertEquals("Type", "J2SE", open.getProjectDisplayName());
        }
        
    }
}



