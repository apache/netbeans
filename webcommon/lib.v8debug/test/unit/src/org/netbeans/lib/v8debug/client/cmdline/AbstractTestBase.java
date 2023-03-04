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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONObject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.netbeans.lib.v8debug.JSONWriter;
import org.netbeans.lib.v8debug.V8Body;
import org.netbeans.lib.v8debug.V8Breakpoint;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Event;
import org.netbeans.lib.v8debug.V8Frame;
import org.netbeans.lib.v8debug.V8Response;
import org.netbeans.lib.v8debug.commands.Evaluate;
import org.netbeans.lib.v8debug.commands.Frame;
import org.netbeans.lib.v8debug.commands.Lookup;
import org.netbeans.lib.v8debug.commands.SetBreakpoint;
import org.netbeans.lib.v8debug.vars.ReferencedValue;
import org.netbeans.lib.v8debug.vars.V8Boolean;
import org.netbeans.lib.v8debug.vars.V8Function;
import org.netbeans.lib.v8debug.vars.V8Number;
import org.netbeans.lib.v8debug.vars.V8Object;
import org.netbeans.lib.v8debug.vars.V8String;
import org.netbeans.lib.v8debug.vars.V8Value;

/**
 *
 * @author Martin Entlicher
 */
abstract class AbstractTestBase {
    
    private static final String NODE_EXE = "node";          // NOI18N
    private static final String NODE_EXE_PROP = "nodeBinary";   // NOI18N
    
    protected static final Object VALUE_UNDEFINED = new String("<undefined>");
    
    protected String testFilePath;
    protected V8Debug v8dbg;
    protected ResponseHandler responseHandler;
    protected Process nodeProcess;
    
    protected final void startUp(InputStream testSource, String testFileName, String debugArgs) throws IOException {
        startUp(testSource, testFileName, new String[] { debugArgs });
    }
    
    protected final void startUp(InputStream testSource, String testFileName, String[] debugArgs) throws IOException {
        int port = startNodeDebug(testSource, testFileName, debugArgs);
        assertTrue("Invalid port: "+port, port > 0);
        responseHandler = new ResponseHandler();
        v8dbg = V8Debug.TestAccess.createV8Debug("localhost", port, responseHandler);
    }
    
    private final int startNodeDebug(InputStream testSource, String testFileName, String[] debugArgs) throws IOException {
        File testFile = File.createTempFile(testFileName.substring(0, testFileName.indexOf('.')), ".js");
        testFile.deleteOnExit();
        Files.copy(testSource, testFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        ProcessBuilder pb = new ProcessBuilder();
        String nodeBinary = System.getProperty(NODE_EXE_PROP);
        if (nodeBinary == null) {
            nodeBinary = pb.environment().get(NODE_EXE_PROP);
        }
        if (nodeBinary == null) {
            nodeBinary = NODE_EXE;
        }
        this.testFilePath = testFile.getAbsolutePath();
        List<String> args = new ArrayList<>();
        args.add(nodeBinary);
        args.addAll(Arrays.asList(debugArgs));
        args.add(testFile.getAbsolutePath());
        pb.command(args);
        pb.redirectErrorStream(true);
        Process node = pb.start();
        nodeProcess = node;
        InputStream stdOut = node.getInputStream();
        BufferedReader bso = new BufferedReader(new InputStreamReader(stdOut));
        String line;
        while ((line = bso.readLine()) != null) {
            int space = line.lastIndexOf(' ');
            if (space > 0) {
                int col = line.lastIndexOf(':');
                if (col > space) {
                    space = col + 1;
                }
                try {
                    int port = Integer.parseInt(line.substring(space).trim());
                    reportPrgOutput(bso);
                    return port;
                } catch (NumberFormatException nfex) {}
            }
            System.err.println(line);
        }
        return -1;
    }
    
    private void reportPrgOutput(final BufferedReader bso) {
        new Thread("Node.js output thread") {
            @Override
            public void run() {
                String line;
                try {
                    while ((line = bso.readLine()) != null) {
                        System.err.println("node.js: '"+line+"'");
                    }
                } catch (IOException ioex) {
                    ioex.printStackTrace();
                }
            }
        }.start();
    }
    
    protected void checkBRResponse(SetBreakpoint.ResponseBody sbResponseBody, long bpNumber, String scriptName, long line, Long column, long actualColumn) {
        checkBRResponse(sbResponseBody, bpNumber, scriptName, line, column, new long[] { actualColumn });
    }
    
    protected void checkBRResponse(SetBreakpoint.ResponseBody sbResponseBody, long bpNumber, String scriptName, long line, Long column, long[] actualColumns) {
        assertEquals("Breakpoint number", bpNumber, sbResponseBody.getBreakpoint());
        assertEquals("Breakpoint type", V8Breakpoint.Type.scriptName, sbResponseBody.getType());
        assertEquals(scriptName, sbResponseBody.getScriptName());
        assertEquals(line, sbResponseBody.getLine().getValue());
        if (column == null) {
            assertFalse(sbResponseBody.getColumn().hasValue());
        } else {
            assertEquals(column.longValue(), sbResponseBody.getColumn().getValue());
        }
        V8Breakpoint.ActualLocation[] actualLocations = sbResponseBody.getActualLocations();
        assertEquals("Breakpoint locations", 1, actualLocations.length);
        assertEquals(line, actualLocations[0].getLine());
        if (actualColumns.length == 1) {
            assertEquals(actualColumns[0], actualLocations[0].getColumn());
        } else {
            long alc = actualLocations[0].getColumn();
            boolean match = false;
            for (long ac : actualColumns) {
                if (ac == alc) {
                    match = true;
                }
            }
            assertTrue("Actual column "+alc+" does not match expected colums: "+Arrays.toString(actualColumns), match);
        }
        long scriptId = actualLocations[0].getScriptId().getValue();
        assertEquals(scriptName, V8Debug.TestAccess.getScript(v8dbg, scriptId).getName());
    }

    protected void checkLocalVar(String varName, Object value, boolean isArgument) throws IOException, InterruptedException {
        V8Debug.TestAccess.doCommand(v8dbg, "frame");
        V8Response lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Frame, lastResponse.getCommand());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        V8Body body = lastResponse.getBody();
        Frame.ResponseBody fbody = (Frame.ResponseBody) body;
        V8Frame frame = fbody.getFrame();
        Map<String, ReferencedValue> refs;
        if (isArgument) {
            refs = frame.getArgumentRefs();
        } else {
            refs = frame.getLocalRefs();
        }
        ReferencedValue referenceAndVal = refs.get(varName);
        assertNotNull("Variable "+varName+" is not present.", referenceAndVal);
        
        V8Value vv = referenceAndVal.getValue();
        if (!referenceAndVal.hasValue()) {
            V8Debug.TestAccess.send(v8dbg, Lookup.createRequest(333, new long[]{ referenceAndVal.getReference() }, false));
            lastResponse = responseHandler.getLastResponse();
            assertEquals(V8Command.Lookup, lastResponse.getCommand());
            Lookup.ResponseBody lrb = (Lookup.ResponseBody) lastResponse.getBody();
            vv = lrb.getValuesByHandle().get(referenceAndVal.getReference());
        }
        checkValue(varName, vv, value);
    }
    
    protected void checkValue(String varName, V8Value vv, Object value) throws IOException, InterruptedException {
        switch (vv.getType()) {
            case Boolean:
                assertEquals(varName, value, Boolean.valueOf(((V8Boolean) vv).getValue()));
                return ;
            case Function:
                V8Function fv = (V8Function) vv;
                ((AbstractTestBase.FunctionCheck) value).check(fv);
                return ;
            case Null:
                assertNull(varName, value);
                return ;
            case Number:
                V8Number nv = (V8Number) vv;
                if (value instanceof Long || value instanceof Integer) {
                    assertEquals(varName, V8Number.Kind.Long, nv.getKind());
                    assertEquals(varName, value, nv.getLongValue());
                } else {
                    assertEquals(varName, V8Number.Kind.Double, nv.getKind());
                    assertEquals(varName, (double) value, nv.getDoubleValue(), 1e-14);
                }
                return ;
            case Object:
                V8Object ov = (V8Object) vv;
                ((AbstractTestBase.ObjectCheck) value).check(ov);
                return ;
            case String:
                assertEquals(varName, value, ((V8String) vv).getValue());
                return ;
            case Undefined:
                assertEquals(varName, value, VALUE_UNDEFINED);
                return ;
            case Set:
            case Map:
            case Symbol:
            case Promise:
                ((AbstractTestBase.ES6TypesCheck) value).check(vv);
                return ;
            default:
                fail("Unhandled variable type: "+vv.getType()+" of "+varName);
        }
    }

    protected void checkEval(String evalStr, Object value) throws IOException, InterruptedException {
        V8Debug.TestAccess.doCommand(v8dbg, "eval "+evalStr);
        V8Response lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Evaluate, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        Evaluate.ResponseBody erb = (Evaluate.ResponseBody) lastResponse.getBody();
        checkValue(evalStr, erb.getValue(), value);
    }

    protected final class FunctionCheck {
        
        private final String name;
        private final String inferredName;
        private final String source;
        private final String scriptName;
        private final long scriptId;
        private final long position;
        private final long line;
        private final long column;
        private final ObjectCheck objCheck;
        
        public FunctionCheck(String name, String inferredName,
                             String source, String scriptName, long scriptId,
                             long position, long line, long column,
                             String[] propNames, Object[] propValues) {
            this.name = name;
            this.inferredName = inferredName;
            this.source = source;
            this.scriptName = scriptName;
            this.scriptId = scriptId;
            this.position = position;
            this.line = line;
            this.column = column;
            this.objCheck = new ObjectCheck("Function", propNames, propValues, source);
        }
        
        public void check(V8Function f) throws IOException, InterruptedException {
            assertEquals(name, f.getName());
            assertEquals(inferredName, f.getInferredName());
            assertEquals(source, f.getSource());
            assertEquals(scriptName, V8Debug.TestAccess.getScript(v8dbg, f.getScriptId().getValue()).getName());
            //assertEquals(scriptId, f.getScriptId());
            assertEquals(position, f.getPosition().getValue());
            assertEquals(line, f.getLine().getValue());
            assertEquals(column, f.getColumn().getValue());
            objCheck.check(f);
        }
    }
    
    protected final class ObjectCheck {
        
        private final String className;
        //private final Map<String, Long> properties;
        private final String[] propNames;
        private final Object[] propValues;
        private final String text;
        
        public ObjectCheck(String className, String[] propNames, Object[] propValues,
                           String text) {
            this.className = className;
            //this.properties = properties;
            this.propNames = propNames;
            this.propValues = propValues;
            this.text = text;
        }
        
        public void check(V8Object o) throws IOException, InterruptedException {
            assertEquals(className, o.getClassName());
            assertEquals(text, o.getText());
            if (propNames == null) {
                if (propValues != null) {
                    // An array
                    V8Object.Array array = o.getArray();
                    assertNotNull(array);
                    assertEquals(propValues.length, array.getLength());
                    StringBuilder referencesToLookup = new StringBuilder();
                    for (int i = 0; i < array.getLength(); i++) {
                        referencesToLookup.append(" "+array.getReferenceAt(i));
                    }
                    String refsStr = referencesToLookup.toString().trim();
                    if (!refsStr.isEmpty()) {
                        V8Debug.TestAccess.doCommand(v8dbg, "lookup "+refsStr);
                        V8Response lastResponse = responseHandler.getLastResponse();
                        assertEquals(V8Command.Lookup, lastResponse.getCommand());
                        assertTrue(lastResponse.isSuccess());
                        assertFalse(lastResponse.isRunning());
                        Lookup.ResponseBody lrb = (Lookup.ResponseBody) lastResponse.getBody();
                        for (int i = 0; i < array.getLength(); i++) {
                            V8Value value = lrb.getValuesByHandle().get(array.getReferenceAt(i));
                            checkValue(o.getText()+"["+i+"]", value, propValues[i]);
                        }
                    }
                }
                return ;
            }
            Map<String, V8Object.Property> oprops = o.getProperties();
            StringBuilder referencesToLookup = new StringBuilder();
            for (int i = 0; i < propNames.length; i++) {
                V8Object.Property prop = oprops.get(propNames[i]);
                assertNotNull("Object "+o.getText()+" does not contain property "+propNames[i], prop);
                long ref = prop.getReference();
                //if (ref > 0) {
                    referencesToLookup.append(" ");
                    referencesToLookup.append(ref);
                /*} else {
                    fail("Object "+o.getText()+" does not contain reference in property "+propNames[i]);
                }*/
            }
            String refsStr = referencesToLookup.toString().trim();
            if (!refsStr.isEmpty()) {
                V8Debug.TestAccess.doCommand(v8dbg, "lookup "+refsStr);
                V8Response lastResponse = responseHandler.getLastResponse();
                assertEquals(V8Command.Lookup, lastResponse.getCommand());
                assertTrue(lastResponse.isSuccess());
                assertFalse(lastResponse.isRunning());
                Lookup.ResponseBody lrb = (Lookup.ResponseBody) lastResponse.getBody();

                for (int i = 0; i < propNames.length; i++) {
                    V8Object.Property prop = oprops.get(propNames[i]);
                    assertNotNull("Object "+o.getText()+" does not contain property "+propNames[i], prop);
                    long ref = prop.getReference();
                    V8Value value = lrb.getValuesByHandle().get(ref);
                    checkValue(o.getText()+"."+propNames[i], value, propValues[i]);
                }
            }
        }
    }
    
    protected final class ES6TypesCheck {
        
        private final String type;
        private final String text;

        public ES6TypesCheck(String type, String text) {
            this.type = type;
            this.text = text;
        }
        
        public void check(V8Value value) {
            assertEquals(type, value.getType().toString());
            assertEquals(text, value.getText());
        }
    }
    
    protected final class ResponseHandler implements V8Debug.Testeable {
        
        private V8Response lastResponse;
        private V8Event lastEvent;
        private String lastReceivedResponseMessage;
        private String lastReceivedEventMessage;
        private String lastNotifiedEventMessage;
        private boolean closed;

        @Override
        public synchronized void notifyResponse(V8Response response) {
            this.lastResponse = response;
            this.notifyAll();
        }

        @Override
        public synchronized void notifyEvent(V8Event event) {
            this.lastNotifiedEventMessage = lastReceivedEventMessage;
            this.lastEvent = event;
            this.notifyAll();
        }

        @Override
        public void notifyClosed() {
            this.closed = true;
        }
        
        @Override
        public void sent(String str) {
        }

        @Override
        public void received(String str) {
            if (str.indexOf("\"type\":\"event\"") > 0) {
                lastReceivedEventMessage = str;
            } else {
                lastReceivedResponseMessage = str;
            }
        }

        @Override
        public void closed() {
        }
        
        public synchronized V8Response getLastResponse() throws InterruptedException {
            while (lastResponse == null) {
                this.wait();
            }
            V8Response response = lastResponse;
            lastResponse = null;
            checkResponseStoreTo(response, lastReceivedResponseMessage);
            return response;
        }
        
        public synchronized void clearLastResponse() {
            lastResponse = null;
        }
        
        public synchronized V8Event getLastEvent() throws InterruptedException {
            while (lastEvent == null) {
                this.wait();
            }
            V8Event event = lastEvent;
            lastEvent = null;
            checkEventStoreTo(event, lastNotifiedEventMessage);
            return event;
        }
        
        public boolean isClosed() {
            return closed;
        }

    }
    
    private static void checkResponseStoreTo(V8Response response, String message) {
        JSONObject json = JSONWriter.store(response);
        String storedMessage = json.toJSONString();
        storedMessage = storedMessage.replace("\\/", "/"); // Replace escaped slash "\/" with shash "/". Unescape slashes.
        boolean equals = compareMessages(response.getCommand().name(), message, storedMessage);
        //assertEquals(message, storedMessage);
        assertTrue(equals);
    }
    
    private static void checkEventStoreTo(V8Event event, String message) {
        JSONObject json = JSONWriter.store(event);
        String storedMessage = json.toJSONString();
        storedMessage = storedMessage.replace("\\/", "/"); // Replace escaped slash "\/" with shash "/". Unescape slashes.
        boolean equals = compareMessages(event.getKind().name(), message, storedMessage);
        //assertEquals(message, storedMessage);
        assertTrue(equals);
    }
    
    private static final String ignoreString = "frameIndex\":null,\"";
    private static final String ignoreNoNameProperties = ",{\"attributes\":";
    private static final String ignoreNoNameQuotation = "\"name\":";
    private static final String ignoreCondition1 = "\"condition\":{}";
    private static final String ignoreCondition2 = "\"condition\":null";
    
    private static boolean compareMessages(String name, String message, String storedMessage) {
        if (!message.equals(storedMessage)) {
            System.err.println("Different stored response of command/event: "+name);
            int i = 0;
            int j = 0;
            for (; i < message.length() && j < storedMessage.length(); i++, j++) {
                if (message.charAt(i) == storedMessage.charAt(j)) {
                    continue;
                }
                if (message.substring(i).startsWith(ignoreString)) {
                    i += ignoreString.length();
                    i--;
                    j--;
                    continue;
                } else if (storedMessage.substring(j).startsWith(ignoreString)) {
                    j += ignoreString.length();
                    i--;
                    j--;
                    continue;
                }
                if (i > 3 && j > 3) {
                    if (message.substring(i-3).startsWith(ignoreNoNameProperties) &&
                        storedMessage.substring(j-3).startsWith(",{\"name\":")) {
                        // property without a name
                        System.err.println("Ignoring message: '"+message.substring(i-3, message.indexOf('}', i)+1)+"'");
                        i = message.indexOf('}', i);
                        j -= 4;
                        continue;
                    } else if (message.substring(i-2).startsWith("{\"attributes\":") &&
                        storedMessage.substring(j-2).startsWith("{\"name\":")) {
                        // property without a name
                        System.err.println("Ignoring message: '"+message.substring(i-2, message.indexOf('}', i)+2)+"'");
                        i = message.indexOf('}', i) + 1;
                        j -= 3;
                        continue;
                    } else if (message.substring(i).startsWith(ignoreNoNameProperties) &&
                               storedMessage.substring(j).startsWith("]")) {
                        // property without a name
                        System.err.println("Ignoring message: '"+message.substring(i, message.indexOf('}', i)+1)+"'");
                        i = message.indexOf('}', i);
                        j--;
                        continue;
                    }
                }
                if (storedMessage.charAt(j) == '"' &&
                    (i > ignoreNoNameQuotation.length() &&
                     message.substring(0, i).endsWith(ignoreNoNameQuotation))) {
                    // name value not quoted, but in storedMessage it is quoted
                    j++;
                    while (message.charAt(i) == storedMessage.charAt(j)) {
                        i++; j++;
                    }
                    if (storedMessage.charAt(j) == '"') {
                        j++;
                    }
                    continue;
                }
                if (i > 12 && message.substring(i-12).startsWith(ignoreCondition1) &&
                    storedMessage.substring(j-12).startsWith(ignoreCondition2)) {
                    i++;
                    j += 3;
                    continue;
                }
                // There might be double values in a different format:
                String sd1 = nextDoubleString(message.substring(i));
                String sd2 = nextDoubleString(storedMessage.substring(j));
                if (!sd1.isEmpty() && !sd2.isEmpty()) {
                    try {
                        double d1 = Double.parseDouble(sd1);
                        double d2 = Double.parseDouble(sd2);
                        if (d1 == d2) {
                            i += sd1.length();
                            j += sd2.length();
                            continue;
                        }
                    } catch (NumberFormatException nfex) {
                        // ignore, there are not double values.
                    }
                }
                
                i = Math.max(0, i - 20);
                j = Math.max(0, j - 20);
                System.err.println("Expecting: ..."+message.substring(i));
                System.err.println("Received:  ..."+storedMessage.substring(j));
                System.err.println("Full message: "+message);
                return false;
            }
        }
        return true;
    }
    
    private static String nextDoubleString(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '-' || c == '.' || Character.isDigit(c) || Character.toLowerCase(c) == 'e') {
                // is a number character
            } else {
                return s.substring(0, i);
            }
        }
        return s;
    }
}
