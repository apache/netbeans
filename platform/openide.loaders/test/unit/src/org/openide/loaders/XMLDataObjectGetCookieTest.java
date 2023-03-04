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
package org.openide.loaders;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import org.netbeans.junit.Log;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.xml.sax.SAXException;

/** Ensuring that getCookie really works.
 *
 * @author Jaroslav Tulach
 */
public class XMLDataObjectGetCookieTest extends LoggingTestCaseHid 
implements Node.Cookie {

    private ErrorManager err;
    
    public XMLDataObjectGetCookieTest(String s) {
        super(s);
    }
    protected void setUp() throws Exception {
        clearWorkDir();
        
        err = ErrorManager.getDefault().getInstance("TEST-" + getName());
        
    }
    
    public void testGetTheLookupWhileWaitingBeforeAssigningIt() throws IOException {
        registerIntoLookup(ENV);
        doTest(
            "THREAD:t2 MSG:parsedId set to NULL" + 
            "THREAD:t1 MSG:Has already been parsed.*" +
            "THREAD:t1 MSG:Query for class org.openide.loaders.XMLDataObjectGetCookieTest"
        );
    }

    public void testGetTheLookupWhileWaitingAfterParsing() throws IOException {
        registerIntoLookup(ENV);
        doTest(
            "THREAD:t1 MSG:New id.*" +
            "THREAD:t2 MSG:Going to read parseId.*" 
        );
    }
    
    private void doTest(String switches) throws IOException {
        FileObject res = FileUtil.createData(
            FileUtil.getConfigRoot(),
            getName() + "/R.xml"
        );
        
        err.log("file created: " + res);
        org.openide.filesystems.FileLock l = res.lock();
        OutputStream os = res.getOutputStream(l);
        err.log("stream opened");
        PrintStream ps = new PrintStream(os);
        
        ps.println("<?xml version='1.0' encoding='UTF-8'?>");
        ps.println("<!DOCTYPE MIME-resolver PUBLIC '-//NetBeans//DTD MIME Resolver 1.0//EN' 'http://www.netbeans.org/dtds/mime-resolver-1_0.dtd'>");
        ps.println("<MIME-resolver>");
        ps.println("    <file>");
        ps.println("        <ext name='lenka'/>");
        ps.println("        <resolver mime='hodna/lenka'/>");
        ps.println("    </file>");
        ps.println("</MIME-resolver>");

        err.log("Content written");
        os.close();
        err.log("Stream closed");
        l.releaseLock();
        err.log("releaseLock");
    
        
        final DataObject obj = DataObject.find(res);
        
        class Run implements Runnable {
            public EP cookie;
            
            public void run () {
                cookie = (EP) obj.getCookie(EP.class);
            }
        }
        
        Run r1 = new Run();
        Run r2 = new Run();
        
        
        registerSwitches(switches, 200);
        
        RequestProcessor.Task t1 = new RequestProcessor("t1").post(r1);
        RequestProcessor.Task t2 = new RequestProcessor("t2").post(r2);
        
        t1.waitFinished();
        t2.waitFinished();
        
        if (r1.cookie == null && r2.cookie == null) {
            fail("Both cookies are null");
        }
        
        assertEquals("First result is ok", ENV, r1.cookie);
        assertEquals("Second result is ok", ENV, r2.cookie);
    }
    
    public void testParseUnparsableXML() throws IOException, PropertyVetoException {
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());
        FileObject res = FileUtil.createData(
            lfs.getRoot(), 
            getName() + "/R.xml"
        );
        
        err.log("file created: " + res);
        org.openide.filesystems.FileLock l = res.lock();
        OutputStream os = res.getOutputStream(l);
        err.log("stream opened");
        PrintStream ps = new PrintStream(os);
        
        ps.println("<?xml version='1.0' encoding='UTF-8'?>");
        ps.println("<PointSourceSubmissionGroup xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xmlns=\"http://www.epa.gov/exchangenetwork\" xsi" +
            ":schemaLocation=\"http://www.epa.gov/exchangenetwork EN_NEI_Point_v3_0.xsd\" " +
            "schemaVersion=\"3.0\">");
        for (int i = 0; i < 500; i++) {
            ps.println("<SystemRecordCountValues schemaVersion='3.0'>");
            ps.println("  <SystemRecordCountTransmittalValue>46</SystemRecordCountTransmittalValue>");
            ps.println("  <SystemRecordCountSiteValue>740</SystemRecordCountSiteValue>");
            ps.println("  <SystemRecordCountEmissionUnitValue>4256</SystemRecordCountEmissionUnitValue>");
            ps.println("</SystemRecordCountValues>");
            ps.println("<TransmittalSubmissionGroup schemaVersion='3.0'>");
            ps.println("  <IndividualFullName>LYNN BARNES</IndividualFullName>");
            ps.println("</TransmittalSubmissionGroup>");
        }

        err.log("Content written");
        os.close();
        err.log("Stream closed");
        l.releaseLock();
        err.log("releaseLock");
    
        
        CharSequence log = Log.enable("org.openide.loaders.XMLDataObject", Level.FINEST);
        final DataObject obj = DataObject.find(res);
        
        
        Object cookie = obj.getCookie(OpenCookie.class);
        assertNotNull( "Can be opened", cookie);
        cookie = obj.getCookie(EditorCookie.class);
        assertNotNull( "Can be editored", cookie);

        String s = log.toString();
        if (s.indexOf("stop") > 5000) {
            fail("Too much logged data:\n" + s.substring(0, 500));
        }
    }
    
    
    private static Object ENV = new EP();
        
    private static final class EP implements Environment.Provider, Node.Cookie {
        public Lookup getEnvironment(DataObject obj) {
            assertEquals("Right object: ", XMLDataObject.class, obj.getClass());
            XMLDataObject xml = (XMLDataObject)obj;
            String id = null;
            try {
                id = xml.getDocument().getDoctype().getPublicId();
            } catch (IOException ex) {
                ex.printStackTrace();
                fail("No exception");
            } catch (SAXException ex) {
                ex.printStackTrace();
                fail("No exception");
            }
            assertEquals("-//NetBeans//DTD MIME Resolver 1.0//EN", id);
            return Lookups.singleton(this);
        }
    };
}
