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

package org.netbeans.modules.uihandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.netbeans.junit.NbTestCase;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jaroslav Tulach
 */
public class ServerTest extends NbTestCase {
    
    private static final String HTTP_HEADER = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n";
    
    public ServerTest(String s) {
        super(s);
    }
    
    
    public static int startServer(final Queue<String> replies, final Queue<String> queries) throws IOException {
        final ServerSocket ss = new ServerSocket(0);
        
        class Run implements Runnable {
            private void doRun(String reply) throws IOException {
                Socket s = ss.accept();
                s.setSoTimeout(500);
                InputStream is = s.getInputStream();
                StringBuffer sb = new StringBuffer();
                try {
                    for (;;) {
                        int ch = is.read();
                        if (ch == -1) {
                            break;
                        }
                        sb.append((char)ch);
                    }
                } catch (SocketTimeoutException ex) {
                    // ok
                }
                
                queries.add(sb.toString());
                
                OutputStream os = s.getOutputStream();
                os.write(reply.getBytes());
                os.close();
                
                is.close();
                s.close();
            }
            
            public void run() {
                for (;;) {
                    String reply = replies.poll();
                    if (reply == null) {
                        break;
                    }
                    try {
                        doRun(reply);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        Run r = new Run();
        new RequestProcessor(ServerTest.class.getName()).post(r);
        
        
        return ss.getLocalPort();
    }
    
    public void testRedirectsLogs() throws Exception {
        LinkedList<String> query = new LinkedList<String>();
        query.add(HTTP_HEADER + "<meta http-equiv=\"Refresh\" conteNT='URL=http://www.netbeans.org'>");
        LinkedList<String> reply = new LinkedList<String>();
        int port = startServer(query, reply);
        
        URL u = new URL("http://localhost:" + port);
        
        List<LogRecord> recs = new ArrayList<LogRecord>();
        recs.add(new LogRecord(Level.WARNING, "MSG_MISTAKE"));
        URL redir = Installer.uploadLogs(u, null, Collections.<String,String>emptyMap(), recs, false);

        assertTrue("one query has been sent: " + query, query.isEmpty());
        assertEquals("One reply received", 1, reply.size());
        assertEquals("Redirected to nb.org", new URL("http://www.netbeans.org"), redir);
    }


    public void testRedirectsLogsWithTime() throws Exception {
        LinkedList<String> query = new LinkedList<String>();
        query.add(HTTP_HEADER + "<meta http-equiv='Refresh' content='3; URL=http://logger.netbeans.org/welcome/use.html'>");
        LinkedList<String> reply = new LinkedList<String>();
        int port = startServer(query, reply);
        
        URL u = new URL("http://localhost:" + port);
        
        List<LogRecord> recs = new ArrayList<LogRecord>();
        recs.add(new LogRecord(Level.WARNING, "MSG_MISTAKE"));
        URL redir = Installer.uploadLogs(u, null, Collections.<String,String>emptyMap(), recs, false);

        assertTrue("one query has been sent: " + query, query.isEmpty());
        assertEquals("One reply received", 1, reply.size());
        assertEquals("Redirected to nb.org", new URL("http://logger.netbeans.org/welcome/use.html"), redir);
    }

    public void testUploadMessagesLog() throws IOException{
        byte[] message = "HalloWorld!".getBytes();
        System.setProperty("netbeans.user", getWorkDirPath());
        File messages = new File (new File(new File(getWorkDirPath(), "var"), "log"), "messages.log");
        messages.getParentFile().mkdirs();
        OutputStream os = new FileOutputStream(messages);
        os.write(message);
        os.close();

        LinkedList<String> query = new LinkedList<String>();
        query.add(HTTP_HEADER + "<meta http-equiv='Refresh' content='3; URL=http://logger.netbeans.org/welcome/use.html'>");
        LinkedList<String> reply = new LinkedList<String>();
        int port = startServer(query, reply);
        List<LogRecord> recs = new ArrayList<LogRecord>();
        recs.add(new LogRecord(Level.WARNING, "MSG_MISTAKE"));

        URL u = new URL("http://localhost:" + port);

        URL redir = Installer.uploadLogs(u, null, Collections.<String,String>emptyMap(), recs, true);
        assertEquals("One reply received", 1, reply.size());
        String replyString = reply.iterator().next();
        assertTrue(replyString.contains("Content-Type: x-application/log"));
        assertTrue(replyString.contains("Content-Type: x-application/gzip"));
        
    }
}
