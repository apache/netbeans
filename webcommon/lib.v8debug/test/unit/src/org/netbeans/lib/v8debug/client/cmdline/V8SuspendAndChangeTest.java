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

package org.netbeans.lib.v8debug.client.cmdline;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.lib.v8debug.V8Body;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Event;
import org.netbeans.lib.v8debug.V8Frame;
import org.netbeans.lib.v8debug.V8Request;
import org.netbeans.lib.v8debug.V8Response;
import org.netbeans.lib.v8debug.V8Script;
import org.netbeans.lib.v8debug.commands.ChangeLive;
import org.netbeans.lib.v8debug.commands.Frame;
import org.netbeans.lib.v8debug.commands.Suspend;

/**
 *
 * @author Martin Entlicher
 */
public class V8SuspendAndChangeTest extends AbstractTestBase {
    
    private static final String TEST_FILE = "TestSuspendAndChange.js"; // NOI18N
    private static final String TEST_FILE_CHANGING = "TestTheChangingScript.js";// NOI18N
    private static final String TEST_FILE_CHANGING2 = "TestTheChangingScript2.js";// NOI18N
    private static final String NODE_ARG_DBG = "--debug";   // NOI18N
    
    private String changingFilePath;
    
    public V8SuspendAndChangeTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        // To block standard in:
        System.setIn(new InputStream() {
            @Override
            public int read() throws IOException {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException ex) {
                    throw new IOException(ex.getLocalizedMessage());
                }
                return -1;
            }
        });
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws IOException {
        copyChangingScript();
        startUp(V8DebugTest.class.getResourceAsStream(TEST_FILE), TEST_FILE, NODE_ARG_DBG);
    }
    
    private void copyChangingScript() throws IOException {
        File changingFile = new File(System.getProperty("java.io.tmpdir"), TEST_FILE_CHANGING);
        changingFilePath = changingFile.getAbsolutePath();
        InputStream changingFileSource = V8DebugTest.class.getResourceAsStream(TEST_FILE_CHANGING);
        changingFile.deleteOnExit();
        Files.copy(changingFileSource, changingFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
    
    @After
    public void tearDown() throws InterruptedException {
        Thread.sleep(2000); // To recover
        if (nodeProcess != null) {
            nodeProcess.destroy();
        }
    }
    
    @Test
    public void testSuspendAndChange() throws IOException, InterruptedException {
        // Wait till the program starts up and process something...
        Thread.sleep(1000);
        // Suspend first:
        V8Debug.TestAccess.send(v8dbg, Suspend.createRequest(123));
        V8Response lastResponse;
        do {
            lastResponse = responseHandler.getLastResponse();
        } while (lastResponse.getRequestSequence() != 123l);
        assertEquals(V8Command.Suspend, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        
        // Check where am I:
        V8Debug.TestAccess.doCommand(v8dbg, "frame");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Frame, lastResponse.getCommand());
        /*if ("No frames".equals(lastResponse.getErrorMessage())) {
            V8Debug.TestAccess.doCommand(v8dbg, "step");
            V8Event lastEvent;
            do {
                lastEvent = responseHandler.getLastEvent();
            } while (V8Event.Kind.AfterCompile == lastEvent.getKind());
            assertEquals(V8Event.Kind.Break, lastEvent.getKind());
            
            V8Debug.TestAccess.doCommand(v8dbg, "frame");
            lastResponse = responseHandler.getLastResponse();
            assertEquals(V8Command.Frame, lastResponse.getCommand());
        }*/
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        V8Body body = lastResponse.getBody();
        Frame.ResponseBody fbody = (Frame.ResponseBody) body;
        V8Frame frame = fbody.getFrame();
        frame.getLine();
        
        V8Script changingScript = V8Debug.TestAccess.getScriptByName(v8dbg, changingFilePath);
        assertNotNull("Script "+changingFilePath+" not found.", changingScript);
        String newSource = readChangedContent();
        // Try dry-run first...
        V8Debug.TestAccess.send(v8dbg, ChangeLive.createRequest(222, changingScript.getId(), newSource, Boolean.TRUE));
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Changelive, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        ChangeLive.ResponseBody chlrb = (ChangeLive.ResponseBody) lastResponse.getBody();
        ChangeLive.Result result = chlrb.getResult();
        assertNotNull(result);
        assertFalse(result.isUpdated());
        ChangeLive.Result.TextualDiff diff = result.getDiff();
        assertEquals(2392, diff.getOldLength());
        assertEquals(2408, diff.getNewLength());
        long[] chunks = diff.getChunks();
        assertEquals(3, chunks.length);
        assertEquals(0, chunks[0]);
        assertEquals(diff.getOldLength(), chunks[1]);
        assertEquals(diff.getNewLength(), chunks[2]);
        ChangeLive.Result.ChangeTree changeTree = result.getChangeTree();
        assertEquals(ChangeLive.Result.ChangeTree.FunctionStatus.Changed, changeTree.getStatus());
        assertEquals(0, changeTree.getPositions().getStartPosition());
        assertEquals(0, changeTree.getNewPositions().getStartPosition());
        assertEquals(diff.getOldLength(), changeTree.getPositions().getEndPosition());
        assertEquals(diff.getNewLength(), changeTree.getNewPositions().getEndPosition());
        ChangeLive.Result.ChangeTree[] children = changeTree.getChildren();
        {
            assertEquals(1, children.length);
            assertEquals(10, children[0].getPositions().getStartPosition());
            assertEquals(2390, children[0].getPositions().getEndPosition());
            assertEquals(ChangeLive.Result.ChangeTree.FunctionStatus.Damaged, children[0].getStatus());
            assertEquals("Text diff overlaps with function boundary", children[0].getStatusExplanation());
        }
        ChangeLive.Result.ChangeTree[] newChildren = changeTree.getNewChildren();
        {
            assertEquals(2, newChildren.length);
            assertEquals("changingFunction", newChildren[0].getName());
            assertEquals(2101, newChildren[0].getPositions().getStartPosition());
            assertEquals(2269, newChildren[0].getPositions().getEndPosition());
            assertEquals("summa", newChildren[1].getName());
            assertEquals(2295, newChildren[1].getPositions().getStartPosition());
            assertEquals(2330, newChildren[1].getPositions().getEndPosition());
        }
        // Dry-run was O.K.
        
        // Do the script change now:
        V8Debug.TestAccess.send(v8dbg, ChangeLive.createRequest(222, changingScript.getId(), newSource));
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Changelive, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        chlrb = (ChangeLive.ResponseBody) lastResponse.getBody();
        result = chlrb.getResult();
        assertNotNull(result);
        assertTrue(result.isUpdated());
        diff = result.getDiff();
        assertTrue("Unexpected old script name: "+result.getCreatedScriptName(), result.getCreatedScriptName().startsWith(changingFilePath));
        ChangeLive.ChangeLog changeLog = chlrb.getChangeLog();
        assertEquals(0, changeLog.getBreakpointsUpdate().length);
        assertEquals(3, changeLog.getNamesLinkedToOldScript().length);
        Set<String> linkedToOldScript = new HashSet<>(Arrays.asList(new String[]{ "changingFunction", "summa" }));
        for (String name : changeLog.getNamesLinkedToOldScript()) {
            linkedToOldScript.remove(name);
        }
        assertTrue(linkedToOldScript.toString(), linkedToOldScript.isEmpty());
    }

    private String readChangedContent() throws IOException {
        InputStream changedFileSource = V8DebugTest.class.getResourceAsStream(TEST_FILE_CHANGING2);
        BufferedReader br = new BufferedReader(new InputStreamReader(changedFileSource));
        StringWriter sw = new StringWriter();
        String line;
        while ((line = br.readLine()) != null) {
            sw.write(line);
        }
        sw.close();
        return sw.toString();
    }
    
}
