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
package org.netbeans.libs.jstestdriver.ext;

import com.google.jstestdriver.JsTestDriver;
import com.google.jstestdriver.browser.DocType;
import com.google.jstestdriver.config.DefaultConfiguration;
import com.google.jstestdriver.embedded.JsTestDriverBuilder;
import com.google.jstestdriver.model.BasePaths;
import com.google.jstestdriver.runner.RunnerMode;
import java.io.File;
import java.util.concurrent.Semaphore;

/**
 *
 */
public class StartServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        assert args.length == 2;
        int port = Integer.parseInt(args[0]);
        boolean strict = Boolean.parseBoolean(args[1]);
        new StartServer().startServer(port, strict);
    }
    
    private Semaphore s;
    
    public void startServer(int port, boolean strict) {
        System.out.println("Starting js-test-driver server on port "+port);
        JsTestDriver serverRunner = new JsTestDriverBuilder()
                .setDefaultConfiguration(new ServerConfiguration(strict))
                .raiseExceptionOnTestFailure(false)
                .addServerListener(new MyListener())
                .setRunnerMode(RunnerMode.QUIET)
                .setPort(port).build();
        serverRunner.startServer();

        // wait until server is stopped:
        s = new Semaphore(0);
        try {
            s.acquire();
        } catch (InterruptedException ex) {
            // ok; server was stopped
        }
    }
    
    static class ServerConfiguration extends DefaultConfiguration {

        private boolean strictMode;

        public ServerConfiguration(boolean strictMode) {
            super(new BasePaths(new File(".")));
            this.strictMode = strictMode;
        }

        @Override
        public DocType getDocType() {
            return strictMode ? DocType.STRICT : DocType.QUIRKS;
        }
    }
    
    class MyListener implements com.google.jstestdriver.hooks.ServerListener {

        public MyListener() {
        }

        @Override
        public void serverStarted() {
            System.out.println("msg.server.started");
        }

        @Override
        public void serverStopped() {
            System.out.println("Starting stopped");
            s.release();
        }

        @Override
        public void browserCaptured(com.google.jstestdriver.BrowserInfo bi) {
            System.out.println("New browser captured:\n"+bi);
        }

        @Override
        public void browserPanicked(com.google.jstestdriver.BrowserInfo bi) {
            System.out.println("Browser panicked:\n"+bi);
        }
    }

}
