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

package org.netbeans.modules.subversion.client.commands;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.modules.subversion.client.AbstractCommandTestCase;
import org.netbeans.modules.subversion.client.SvnClient;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 *
 * @author tomas
 */
public class CancelTestHidden extends AbstractCommandTestCase {

    public CancelTestHidden(String testName) throws Exception {
        super(testName);
    }
            
    public void testCancel() throws Exception {                                                
        final SvnClient c = getNbClient();

        abstract class TimeFormatter {
            public String getCurrTime() {
                return Long.toString(System.currentTimeMillis());
            }
        }
        class CommandRunner extends TimeFormatter implements Runnable {
            public void run() {
                try {
                    System.out.println("CommandRunner.run() - started at    " + getCurrTime());
                    c.getInfo(getRepoUrl(), null, null);
                } catch (SVNClientException ex) {
                    System.out.println("CommandRunner.run() - exception thrown: " + ex.getClass() + ": " + ex.getMessage());
                } finally {
                    System.out.println("CommandRunner.run() - finished at   " + getCurrTime());
                }
            }
        } 
        class TaskInterrupter extends TimeFormatter implements Runnable {
            Boolean cancellationSuccess;
            TaskInterrupter() { }
            public void run() {
                try {
                    System.out.println("TaskInterrupter.run() - started at  " + getCurrTime());
                    c.cancel();
                    cancellationSuccess = Boolean.TRUE;
                } catch (Exception ex) {
                    System.out.println("TaskInterrupter.run() - exception thrown " + ex.getClass() + ": " + ex.getMessage());
                    Exceptions.printStackTrace(ex);
                    cancellationSuccess = Boolean.FALSE;
                } finally {
                    System.out.println("TaskInterrupter.run() - finished at " + getCurrTime());
                }
            }
        }

        Blocker blocker;

        final CommandRunner cmdRunner = new CommandRunner();
        final TaskInterrupter interrupter = new TaskInterrupter();

        Logger.getLogger("").addHandler(blocker = new Blocker("cli: process created"));

        RequestProcessor rp = new RequestProcessor("clitest", 2);
        rp.post(cmdRunner);
        boolean interrupted = rp.post(interrupter).waitFinished(60000);

        assertTrue(interrupted);
        assertTrue(interrupter.cancellationSuccess.booleanValue());
        assertTrue(blocker.destroyed);
    }            
    
    private class Blocker extends Handler {
        private final String msg;
        private boolean destroyed = false;
        public Blocker(String msg) {
            this.msg = msg;
        }
        @Override
        public void publish(LogRecord record) {
            if(record.getMessage().indexOf(msg) > -1) {
                while(true) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            } else if(record.getMessage().indexOf("cli: Process destroyed") > -1) {
                destroyed = true;
            }
        }
        @Override
        public void flush() { }
        @Override
        public void close() throws SecurityException { }
    }
    
}
