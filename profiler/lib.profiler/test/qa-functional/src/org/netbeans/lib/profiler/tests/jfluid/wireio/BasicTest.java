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

package org.netbeans.lib.profiler.tests.jfluid.wireio;

import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.wireprotocol.*;
import java.net.Socket;


/**
 *
 * @author ehucka
 */
public class BasicTest extends CommonWireIOTestCase {

    private Socket clientSocket = null;
    private LoggingThread t = null;
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of BasicTest */
    public BasicTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.createConfiguration(BasicTest.class).addTest(
            "testComplexCommands",
            "testComplexResponse",
            "testSimpleCommands",
            "testSimpleResponse",
            "testUnknownSimpleCommand").enableModules(".*").clusters(".*").gui(false));
    }

    protected void tearDown() throws Exception {
        //To prevent chain failures due to occupied socket or unclosed logging thread
        try {
            t.setRunning(false);
            clientSocket.close();
        } catch (Exception e) {}
        super.tearDown();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void testComplexCommands() {
        t = new LoggingThread();
        t.start();
        try {
            while (!t.isPrepared()) {
                Thread.sleep(1000);
            }

            clientSocket = new Socket("localhost", PORT);
            WireIO wireIO = createWireIOClient(clientSocket);

            Command cmd;
            cmd = new AsyncMessageCommand(true, "async message text");
            log("sending command " + cmd);
            wireIO.sendComplexCommand(cmd);

            cmd = new ClassLoadedCommand("java.lang.String", new int[3], new byte[0], true);
            log("sending command " + cmd);
            wireIO.sendComplexCommand(cmd);

            cmd = new RootClassLoadedCommand(new String[] { "java.lang.String" }, new int[1], new byte[1][0], new int[1], new int[1][], 1, new int[1]);
            log("sending command " + cmd);
            wireIO.sendComplexCommand(cmd);

            cmd = new SetUnchangeableInstrParamsCommand(false, true, false, 0, 1024);
            log("sending command " + cmd);
            wireIO.sendComplexCommand(cmd);

            cmd = new MethodInvokedFirstTimeCommand('\0');
            log("sending command " + cmd);
            wireIO.sendComplexCommand(cmd);

            cmd = new GetMethodNamesForJMethodIdsCommand(new int[] { 1 });
            log("sending command " + cmd);
            wireIO.sendComplexCommand(cmd);

            cmd = new MethodLoadedCommand("java.lang.String", 7, "toString()", "");
            log("sending command " + cmd);
            wireIO.sendComplexCommand(cmd);

            cmd = new EventBufferDumpedCommand(1024, null, -1);
            log("sending command " + cmd);
            wireIO.sendComplexCommand(cmd);

            cmd = new SetChangeableInstrParamsCommand(false, 32, 12, 10, 5, 10, true, true, true, true, 10);
            log("sending command " + cmd);
            wireIO.sendComplexCommand(cmd);

            cmd = new InitiateProfilingCommand(CommonConstants.CPU_INSTR_FULL, "java.lang.String");
            log("sending command " + cmd);
            wireIO.sendComplexCommand(cmd);

            cmd = new InstrumentMethodGroupCommand(CommonConstants.CPU_INSTR_FULL, new String[] { "java.lang.String" },
                                                   new int[1], new byte[1][0], new boolean[1], 0);
            log("sending command " + cmd);
            wireIO.sendComplexCommand(cmd);

            cmd = new GetDefiningClassLoaderCommand("java.lang.String", 1);
            log("sending command " + cmd);
            wireIO.sendComplexCommand(cmd);

            clientSocket.close();
            log("wait for thread");

            long tm = 0;

            while ((tm < 20000) && t.isRunning()) {
                Thread.sleep(1000);
                tm += 1000;
            }

            log("finished.");
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(ex.getMessage(), false);
        }
    }

    public void testComplexResponse() {
        t = new LoggingThread();
        t.start();
        try {
            while (!t.isPrepared()) {
                Thread.sleep(1000);
            }

            clientSocket = new Socket("localhost", PORT);
            WireIO wireIO = createWireIOClient(clientSocket);

            Response resp;
            resp = new CodeRegionCPUResultsResponse(new long[] { 10L, 20L, 30L });
            log("send response " + resp);
            wireIO.sendComplexResponse(resp);

            resp = new ThreadLivenessStatusResponse("status".getBytes());
            log("send response " + resp);
            wireIO.sendComplexResponse(resp);

            // Testing VMProperties against a golden file may fail because of different environemtn; ommiting this response
            //            resp = new VMPropertiesResponse(
            //          System.getProperty("java.version"), // NOI18N
            //          System.getProperty("java.class.path"), // NOI18N
            //          System.getProperty("java.ext.dirs"), // NOI18N
            //          System.getProperty("sun.boot.class.path"), // NOI18N
            //          System.getProperty("user.dir"), // NOI18N
            //          "-cp",
            //          "Run",
            //          System.getProperty("os.name"), // NOI18N
            //          Runtime.getRuntime().maxMemory(),
            //          System.currentTimeMillis(),
            //          Timers.getCurrentTimeInCounts(),
            //          10
            //          ); // NOI18N
            //            
            // //            resp=new VMPropertiesResponse("1.5.0_04", "rt.jar", "/tmp" , "agentpath", ".", "-cp", "Run", "linux", 128, 10);
            //            log("send response "+resp);
            //            wireIO.sendComplexResponse(resp);
            resp = new DumpResultsResponse(true, 200508181215L);
            log("send response " + resp);
            wireIO.sendComplexResponse(resp);

            resp = new InstrumentMethodGroupResponse(new String[] { "java.lang.String" }, new int[1], new byte[1][0],
                                                     new boolean[1], 0);
            log("send response " + resp);
            wireIO.sendComplexResponse(resp);

            resp = new MethodNamesResponse(new byte[0], new int[0]);
            log("send response " + resp);
            wireIO.sendComplexResponse(resp);

            resp = new DefiningLoaderResponse(1);
            log("send response " + resp);
            wireIO.sendComplexResponse(resp);

            resp = new CalibrationDataResponse(new double[] { 12.4 }, new double[] { 10.4 }, new double[] { 13.54 },
                                               new long[] { 3, 10 });
            log("send response " + resp);
            wireIO.sendComplexResponse(resp);

            resp = new InternalStatsResponse();
            log("send response " + resp);
            wireIO.sendComplexResponse(resp);

            resp = new ObjectAllocationResultsResponse(new int[] { 20 }, 1);
            log("send response " + resp);
            wireIO.sendComplexResponse(resp);

            MonitoredNumbersResponse r = new MonitoredNumbersResponse(new long[] { 20L }, 0, 0);
            log("send response " + r);
            // to prevent NPE due to null fields gcStarts, gcFinishes
            r.setGCstartFinishData(new long[] { 0L }, new long[] { 20L });
            wireIO.sendComplexResponse(r);
            
            clientSocket.close();
            log("wait for thread");

            long tm = 0;

            while ((tm < 20000) && t.isRunning()) {
                Thread.sleep(1000);
                tm += 1000;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(ex.getMessage(), false);
        }
    }

    public void testSimpleCommands() {
        t = new LoggingThread();
        t.start();
        try {
            while (!t.isPrepared()) {
                Thread.sleep(1000);
            }

            clientSocket = new Socket("localhost", PORT);
            WireIO wireIO = createWireIOClient(clientSocket);

            for (int cmd = 1; cmd < 40; cmd++) {
                wireIO.sendSimpleCommand(cmd);
                log("send command " + cmd);
            }

            clientSocket.close();
            log("wait for thread");

            long tm = 0;

            while ((tm < 20000) && t.isRunning()) {
                Thread.sleep(1000);
                tm += 1000;
            }

            log("finished.");
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(ex.getMessage(), false);
        }
    }

    public void testSimpleResponse() {
        t = new LoggingThread();
        t.start();
        try {
            while (!t.isPrepared()) {
                Thread.sleep(1000);
            }

            clientSocket = new Socket("localhost", PORT);
            WireIO wireIO = createWireIOClient(clientSocket);

            wireIO.sendSimpleResponse(true, "Error message.");
            log("response send");
            clientSocket.close();
            log("wait for thread");

            long tm = 0;

            while ((tm < 20000) && t.isRunning()) {
                Thread.sleep(1000);
                tm += 1000;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(ex.getMessage(), false);
        }
    }

    public void testUnknownSimpleCommand() {
        t = new LoggingThread();
        t.start();
        try {
            while (!t.isPrepared()) {
                Thread.sleep(1000);
            }

            clientSocket = new Socket("localhost", PORT);
            WireIO wireIO = createWireIOClient(clientSocket);

            int cmd = 0;
            wireIO.sendSimpleCommand(cmd);
            log("send command " + cmd);
            clientSocket.close();
            log("wait for thread");

            long tm = 0;

            while ((tm < 20000) && t.isRunning()) {
                Thread.sleep(1000);
                tm += 1000;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(ex.getMessage(), false);
        }
    }
}
