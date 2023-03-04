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
package org.netbeans.libs.jstestdriver;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.Configuration;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.libs.jstestdriver.api.BrowserInfo;
import org.netbeans.libs.jstestdriver.api.ServerListener;
import org.netbeans.libs.jstestdriver.api.TestListener;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.IOProvider;

// TODO: initially this implementation was in a different module;
// perhaps refactor back and drop the JsTestDriverImplementation
public class JsTestDriverImpl implements JsTestDriverImplementation {

    private boolean running = false;
    private Future task;
    private boolean externallyStarted;
    private static final Logger LOGGER = Logger.getLogger(JsTestDriverImpl.class.getName());
    private static RequestProcessor RP = new RequestProcessor("re-fire js-test-driver events");

    public JsTestDriverImpl() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (task != null) {
                    task.cancel(true);
                }
            }
        }));
    }
    
    private boolean wasStartedExternally(int port) {
        try {
            URL u = new URL("http://localhost:"+port);
            URLConnection conn = u.openConnection();
            conn.connect();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
    
    private static String getJavaBinary() {
        String javaHome = System.getProperty("java.home");
        String javaBinary = Utilities.isWindows() ? "java.exe" : "java"; // NOI18N
        return javaHome + File.separator + "bin" + File.separator + javaBinary;
        
    }
    
    @Override
    public void startServer(File jsTestDriverJar, int port, boolean strictMode, ServerListener listener) {
        if (wasStartedExternally(port)) {
            externallyStarted = true;
            IOProvider.getDefault().getIO("js-test-driver Server", false).getOut().
                    println("Port "+port+" is busy. Server was started outside of the IDE.");
            return;
        }
        ExecutionDescriptor descriptor = new ExecutionDescriptor().
                controllable(false).
//                outLineBased(true).
//                errLineBased(true).
                outProcessorFactory(new ServerInputProcessorFactory(listener)).
                frontWindowOnError(true);
        File extjar = InstalledFileLocator.getDefault().locate("modules/ext/libs.jstestdriver-ext.jar", "org.netbeans.libs.jstestdriver", false); // NOI18N
        ExternalProcessBuilder processBuilder = new ExternalProcessBuilder(getJavaBinary()).
            addArgument("-cp").
            addArgument(jsTestDriverJar.getAbsolutePath()+File.pathSeparatorChar+extjar.getAbsolutePath()).
            addArgument("org.netbeans.libs.jstestdriver.ext.StartServer").
            addArgument(""+port).
            addArgument(""+strictMode);
        ExecutionService service = ExecutionService.newService(processBuilder, descriptor, "js-test-driver Server");
        task = service.run();
        running = true;
    }

    @Override
    public void stopServer() {
        assert running : "server is not running";
        task.cancel(true);
        task = null;
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running || externallyStarted;
    }
    
    @Override
    public boolean wasStartedExternally() {
        return externallyStarted;
    }

    @Override
    public void runTests(File jsTestDriverJar, String serverURL, boolean strictMode, File baseFolder, File configFile, 
            String testsToRun, final TestListener listener, final LineConvertor lineConvertor) {
        ExecutionDescriptor descriptor = new ExecutionDescriptor().
                controllable(false).
//                outLineBased(true).
//                errLineBased(true).
                outProcessorFactory(new TestRunInputProcessorFactory(listener)).
                outConvertorFactory(new ExecutionDescriptor.LineConvertorFactory() {
                    @Override
                    public LineConvertor newLineConvertor() {
                        return lineConvertor;
                    }
                }).
                frontWindowOnError(true);
        File extjar = InstalledFileLocator.getDefault().locate("modules/ext/libs.jstestdriver-ext.jar", "org.netbeans.libs.jstestdriver", false); // NOI18N
        ExternalProcessBuilder processBuilder = new ExternalProcessBuilder(getJavaBinary()).
            addArgument("-cp").
            addArgument(jsTestDriverJar.getAbsolutePath()+File.pathSeparatorChar+extjar.getAbsolutePath()).
            addArgument("org.netbeans.libs.jstestdriver.ext.RunTests").
            addArgument(serverURL).
            addArgument(baseFolder.getAbsolutePath()).
            addArgument(configFile.getAbsolutePath()).
            addArgument(testsToRun).
            addArgument(""+strictMode);
        ExecutionService service = ExecutionService.newService(processBuilder, descriptor, "Running JS unit tests");
        try {
            service.run().get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
        RP.post(new Runnable() {
        @Override
            public void run() {
                listener.onTestingFinished();
            }
        });
    }

    private static class ServerInputProcessorFactory implements ExecutionDescriptor.InputProcessorFactory {

        private ServerListener listener;

        public ServerInputProcessorFactory(ServerListener listener) {
            this.listener = listener;
        }
        
        @Override
        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return new ServerInputProcessor(defaultProcessor, listener);
        }
        
    }
    
    private static class ServerInputProcessor implements InputProcessor {

        private InputProcessor delegate;
        private ServerListener listener;

        public ServerInputProcessor(InputProcessor delegate, ServerListener listener) {
            this.delegate = delegate;
            this.listener = listener;
        }
        
        @Override
        public void processInput(char[] chars) throws IOException {
            String s = new String(chars);
            if (s.indexOf("msg.server.started") != -1) {
                s = s.replace("msg.server.started", "Server has started");
                delegate.processInput(s.toCharArray());
                listener.serverStarted();
            } else {
                delegate.processInput(chars);
            }
        }

        @Override
        public void reset() throws IOException {
            delegate.reset();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
        
    }
    
    private static class TestRunInputProcessorFactory implements ExecutionDescriptor.InputProcessorFactory {

        private TestListener listener;

        public TestRunInputProcessorFactory(TestListener listener) {
            this.listener = listener;
        }
        
        @Override
        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return new TestRunInputProcessor(defaultProcessor, listener);
        }
        
    }
    
    private static class TestRunInputProcessor implements InputProcessor {

        private InputProcessor delegate;
        private TestListener listener;

        public TestRunInputProcessor(InputProcessor delegate, TestListener listener) {
            this.delegate = delegate;
            this.listener = listener;
        }
        
        @Override
        public void processInput(char[] chars) throws IOException {
            String s = new String(chars);
            processPossibleBlockOfLines(s);
        }
        
        private void processPossibleBlockOfLines(String s) throws IOException {
            try {
                StringTokenizer st = new StringTokenizer(s, "\n");
                while (st.hasMoreTokens()) {
                    String ss = processSingleLine(st.nextToken())+"\n";
                    delegate.processInput(ss.toCharArray());
                }
            } catch (Throwable t) {
                LOGGER.log(Level.SEVERE, "something went wrong: "+s, t);
            }
        }
        private String processSingleLine(String s) throws IOException {
            int start = s.indexOf("nb-easel-json:{");
            if (start != -1) {
                int end = s.lastIndexOf("}");
                String command = s.substring(start+14, end+1);
                Object o = JSONValue.parse(command);
                if (o == null) {
                    LOGGER.log(Level.SEVERE, "cannot parse following JSON: "+command + " original message: "+s, new IOException("cannot parse"));
                } else {
                    assert o instanceof JSONObject : "must be JSONObject: "+o+" "+o.getClass();
                    JSONObject json = (JSONObject)o;
                    final TestListener.TestResult tr = new TestListener.TestResult(
                            new BrowserInfo((String)json.get("browserName"), (String)json.get("browserVersion"), (String)json.get("browserOS")),
                            (String)json.get("result"), (String)json.get("message"), (String)json.get("log"), 
                            (String)json.get("testCase"), (String)json.get("testName"), ((Number)json.get("duration")).longValue(),
                            (String)json.get("stack"));
                    StringBuilder test = new StringBuilder();
                    test.append(tr.getTestCaseName() + " - " + tr.getTestName() + " " +
                            tr.getResult().toString().toUpperCase()+" in "+tr.getDuration()+"ms (" +tr.getBrowserInfo().getName()+ 
                            ","+tr.getBrowserInfo().getOs() + ","+tr.getBrowserInfo().getVersion()+")");
                    if (tr.getStack().length() > 0) {
                        test.append("\n"+tr.getStack());
                    }
                    if (tr.getLog().length() > 0) {
                        test.append("\n"+tr.getLog());
                    }
                    if (tr.getMessage().length() > 0) {
                        test.append("\n"+tr.getMessage());
                    }
                    s = s.substring(0, start)+test.toString()+s.substring(end+1);
                    RP.post(new Runnable() {
                    @Override
                        public void run() {
                            listener.onTestComplete(tr);
                        }
                    });
                    return s;
                }
            }
            return s;
        }

        @Override
        public void reset() throws IOException {
            delegate.reset();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
        
    }
}
