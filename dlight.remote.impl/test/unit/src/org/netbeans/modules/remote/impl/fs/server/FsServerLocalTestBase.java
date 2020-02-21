/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.impl.fs.server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.openide.util.NotImplementedException;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class FsServerLocalTestBase extends NativeExecutionBaseTestCase {

    private final RequestProcessor RP = new RequestProcessor(null, 100);

    public FsServerLocalTestBase(String name) {
        super(name, ExecutionEnvironmentFactory.getLocal());
    }

    @Override
    protected void setUp() throws Exception {
        System.out.printf("========== %s.setUp() ==========\n", getName());
        super.setUp();
        ensureBinaries();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        System.out.printf("========== %s.tearDown() ==========\n", getName());
    }
    
    protected final File getServerFile() throws IOException, ConnectionManager.CancellationException {
        return FSSDispatcher.testGetOriginalFSServerFile(getTestExecutionEnvironment());
    }

    protected void ensureBinaries() throws Exception {
        File serverFile = getServerFile();
        assertTrue("Can not find " + serverFile.getAbsolutePath(), serverFile.exists());
        if (!serverFile.canExecute()) {
            serverFile.setExecutable(true, true);
        }
        assertTrue("Can not execute " + serverFile.getAbsolutePath(), serverFile.canExecute());
    }
    
    protected File getHome() {
        return new File(System.getProperty("user.home"));
    }

    protected void doSimpleTest(File dir, int lapCount, String... params) throws Exception {
        FSServer server = new FSServer(params);
        sleep(200);
        RP.post(new LoggingStreamReader(server.getProcess().getInputStream(), System.out, null));
        List<File> dirs = findDirectories(dir);
        String title = String.format("Requesting ls for all %d subdirectories of %s (recursively) %d times\n", 
                dirs.size(), dir.getAbsolutePath(), lapCount);
        System.out.printf(title);
        sleep(200);
        long time = System.currentTimeMillis();
        for (int i = 0; i < lapCount; i++) {
            for (File d : dirs) {
                server.requestLs(d.getAbsolutePath());
            }
        }        
        time = System.currentTimeMillis() - time;
        System.out.printf("%s took %d seconds\n", title, time/1000);
        
        PrintWriter writer = new PrintWriter(new File(getWorkDir(), getName() + ".requests"));
        try {
            for (File d : dirs) {
                FSSDispatcher.sendRequest(writer, new FSSRequest(FSSRequestKind.FS_REQ_LS, d.getAbsolutePath()));
            }
        } finally {
            writer.close();
        }
        
        server.requestQuit();
        timedWait(server.getProcess(), 2000);
    }
        
    protected void clearPersistence() throws Exception {
        File home = getHome();
        assertTrue(home.exists());
        File netbeans = new File(home, ".netbeans");
        assertTrue(netbeans.exists());
        File persistenceRoot = new File(netbeans, "remotefs");
        assertTrue(persistenceRoot.exists());
        removeDirectoryContent(persistenceRoot);
    }
    
    protected int timedWait(final NativeProcess process, int timeout) throws InterruptedException {
        Timer timer = new Timer(timeout, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isFinished(process)) {
                    process.destroy();
                }
            }
        });
        timer.start();
        int rc = process.waitFor();
        return rc;
    }
    
    protected int getPID(NativeProcess process) {
        try {
            return process.getPID();
        } catch (IOException ex) {
            return -1;
        }
    }
    
    protected boolean isFinished(NativeProcess process) {
        NativeProcess.State state = process.getState();
        switch(state) {
            case CANCELLED:
            case ERROR:
            case FINISHED:
                return true;
            case FINISHING:
            case INITIAL:
            case RUNNING:
            case STARTING:
                return false;
            default :   
                throw new IllegalStateException("Unexpected process state: " + state);
        }
    }
    
    protected List<File> findDirectories(File base) throws Exception {
        assertTrue("Should be a directory: " + base, base.isDirectory());
        System.out.printf("Gathering %s subdirectories\n", base.getAbsolutePath());
        long time = System.currentTimeMillis();
        List<File> l = new ArrayList<>();
        findDirectories(base, l, new HashSet<File>());
        System.out.printf("Gathering %s subdirectories took %d seconds\n", 
                base.getAbsolutePath(),
                (System.currentTimeMillis() - time)/1000);
        return l;
    }

    private void findDirectories(File f, List<File> list, Set<File> antiLoop) {
        if (f.isDirectory()) {
            if (!antiLoop.contains(f)) {
                antiLoop.add(f);
                for (File child : f.listFiles()) {
                    findDirectories(child, list, antiLoop);
                }
                list.add(f);
            }
        }        
    }
    
    protected enum Mode {
        DEFAULT,
        TIME,
        COLLECT
    }
    
    protected final class FSServer implements ChangeListener {

        private final File serverFile;
        private final PrintWriter writer;
        private final BufferedReader reader;
        private final NativeProcess process;
        private final CyclicStringBuffer errBuffer;

        public FSServer(String... params) throws IOException, ConnectionManager.CancellationException {
            this(Mode.DEFAULT, params);
        }

        public FSServer(Mode mode, String... params) throws IOException, ConnectionManager.CancellationException {
            this.errBuffer = new CyclicStringBuffer(100);
            this.serverFile = getServerFile();
            NativeProcessBuilder processBuilder = NativeProcessBuilder.newProcessBuilder(getTestExecutionEnvironment());
            processBuilder.addNativeProcessListener(this);
            
            switch (mode) {
                case DEFAULT:
                    processBuilder.setExecutable(serverFile.getAbsolutePath());            
                    processBuilder.setArguments(params);                    
                    break;
                case TIME:
                    processBuilder.setExecutable("/usr/bin/time");
                    List<String> args = new ArrayList<>(params.length + 2);
                    if (HostInfoUtils.getHostInfo(getTestExecutionEnvironment()).getOSFamily() == OSFamily.LINUX) {
                        args.add("--portability");
                    }
                    args.add(serverFile.getAbsolutePath());
                    args.addAll(Arrays.asList(params));
                    processBuilder.setArguments(args.toArray(new String[args.size()]));
                    break;
                case COLLECT:
                    throw new NotImplementedException();
                    //break;
                default:
                    throw new IllegalArgumentException("Unexpected mode: " + mode);
            }            
            process = processBuilder.call();
            writer = new PrintWriter(process.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            RP.post(new LoggingStreamReader(process.getErrorStream(), System.err, errBuffer));
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            NativeProcess process = (NativeProcess) e.getSource();
            System.err.printf("process [pid=%d] [%s] %s\n", getPID(process), process.toString(), process.getState());
        }
        
        public PrintWriter getWriter() {
            return writer;
        }

        public BufferedReader getReader() {
            return reader;
        }

        public NativeProcess getProcess() {
            return process;
        }
        
        public void kill() {
            process.destroy();
        }
        
        public void requestQuit() {
            getWriter().print("q\n");
            getWriter().flush();
        }
        
        public void requestLs(String path) {
            FSSDispatcher.sendRequest(getWriter(), new FSSRequest(FSSRequestKind.FS_REQ_LS, path));
        }
        
        public List<String> getStdErr() {
            return errBuffer.getLines();
        }
    }

    private static class CyclicStringBuffer {
        
        private final List<String> lines;
        private final int capacity;
        private final Object lock = new Object();
        private int last;
        private int total;

        public CyclicStringBuffer(int capacity) {
            this.lines = new ArrayList<>(capacity);
            this.capacity = capacity;
            this.last = -1;
        }
        
        public void add(String line) {
            synchronized (lock) {
                total++;
                last++;
                if (last >= capacity) {
                    last = 0;
                }
                if (last < lines.size()) {
                    lines.set(last, line);
                } else {
                    lines.add(line);
                    assert lines.size() == last +1 ;
                }
            }
        }
        
        public List<String> getLines() {           
            synchronized (lock) {
                List<String> result = new ArrayList<>(Math.max(total, lines.size()));
                if (total > lines.size()) { // wrapped
                    for (int i = last + 1; i < lines.size(); i++) {
                        result.add(lines.get(i));
                    }
                }
                for (int i = 0; i <= last; i++) {
                    result.add(lines.get(i));                    
                }
                return result;
            }
        }        
    }
    
    private class LoggingStreamReader implements Runnable {
        
        private final InputStream inputStream;
        private final PrintStream outputStream;
        private final CyclicStringBuffer buffer;

        public LoggingStreamReader(InputStream inputStream, PrintStream outputStream, CyclicStringBuffer buffer) {
            this.inputStream = inputStream;
            this.outputStream = outputStream;
            this.buffer = buffer;
        }
        
        @Override
        public void run() {
            String oldThreadName = Thread.currentThread().getName();
            try {
                Thread.currentThread().setName("fs_server error reader");
                BufferedReader reader = ProcessUtils.getReader(inputStream, true);
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        outputStream.printf("%s\n", line); //NOI18N
                        if (buffer != null) {
                            buffer.add(line);
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                } finally {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                    }
                }
            } finally {
                Thread.currentThread().setName(oldThreadName);
            }
        }
        
    }
    
}
