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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.uihandler.LogRecords;
//import org.netbeans.lib.uihandler.MultiPartHandler;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Jaroslav Tulach
 */
public class UploadLogsTest extends NbTestCase {
    private Logger LOG;
    

    public UploadLogsTest(String s) {
        super(s);
    }
    
    protected void setUp() throws Exception {
        LOG = Logger.getLogger("test." + getName());
        
        clearWorkDir();
    }
    
    protected Level logLevel() {
        return Level.INFO;
    }

    public void testSendNull() throws Exception {
        List<LogRecord> recs = new ArrayList<LogRecord>();
        recs.add(null);
        MemoryURL.registerURL("memory://upload", "Ok");
        URL redir = Installer.uploadLogs(new URL("memory://upload"), "myId", Collections.<String,String>emptyMap(), recs, false);
    }
    
    public void ignoreTestSendsCorrectlyEncoded() throws Exception {
        
        for (int times = 0; times < 10; times++) {
            LOG.log(Level.INFO, "Running for {0} times", times);
            List<LogRecord> recs = new ArrayList<LogRecord>();
            recs.add(new LogRecord(Level.WARNING, "MSG_MISTAKE"));
            MemoryURL.registerURL("memory://upload", "Ok");
            URL redir = Installer.uploadLogs(new URL("memory://upload"), "myId", Collections.<String,String>emptyMap(), recs, false);

            final byte[] content = MemoryURL.getOutputForURL("memory://upload");

            String lineDelim = System.getProperty("line.separator");
            assertNotNull("Some delim is there", lineDelim);
            int head = new String(content, "utf-8").indexOf(lineDelim + lineDelim);
            if (head == -1) {
                fail("There should be an empty line:\n" + new String(content, "utf-8"));
            }

            /*
            class RFImpl implements MultiPartHandler.RequestFacade, MultiPartHandler.InputFacade {
                private ByteArrayInputStream is = new ByteArrayInputStream(content);

                public int getContentLength() {
                    return content.length;
                }

                public String getContentType() {
                    return MemoryURL.getRequestParameter("memory://upload", "Content-Type");
                }

                public MultiPartHandler.InputFacade getInput() throws IOException {
                    return this;
                }

                public int readLine(byte[] arr, int off, int len) throws IOException {
                    int cnt = 0;
                    for (; cnt < len; ) {
                        int ch = is.read();
                        if (ch == -1) {
                            return ch;
                        }
                        arr[off + cnt] = (byte)ch;
                        cnt++;
                        if (ch == '\n') {
                            break;
                        }
                    }
                    return cnt;
                }

                public InputStream getInputStream() {
                    return is;
                }
            }
            RFImpl request = new RFImpl();
            */

            File dir = new File(getWorkDir(), "ui");
            dir.mkdirs();

            //MultiPartHandler handler = new MultiPartHandler(request, dir.getPath());
            //handler.parseMultipartUpload();

            File[] files = dir.listFiles();
            assertEquals(times + "th file created", times + 1, files.length);
            if (!files[times].getName().startsWith("myId")) {
                fail("Expected was 'myId':" + files[times].getName());
            }
            LOG.info("Got these files: " + Arrays.asList(files));
            Arrays.sort(files);
            LOG.info("Sorted to files: " + Arrays.asList(files));


            //assertEquals("Handler keeps name of the right file", files[times], handler.getFile("logs"));

            DataInputStream is = new DataInputStream(new FileInputStream(files[0]));
            class H extends Handler {
                public LogRecord nr;
                
                public void publish(LogRecord arg0) {
                    assertNull("First call", nr);
                    nr = arg0;
                }

                public void flush() {
                }

                public void close() throws SecurityException {
                }
            }
            
            H h2 = new H();
            LogRecords.scan(is, h2);
            is.close();
            LogRecord rec = h2.nr;

            assertEquals("Same msg", recs.get(0).getMessage(), rec.getMessage());


            DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document dom = null;
            try {
                dom = b.parse(new GZIPInputStream(new FileInputStream(files[0])));
            } catch (SAXException ex) {
                fail("cannot parse: " + files[0]);
            }

            assertNotNull("Parsed", dom);
        }
    }

}


