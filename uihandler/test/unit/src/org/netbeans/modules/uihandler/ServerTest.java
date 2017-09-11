/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
