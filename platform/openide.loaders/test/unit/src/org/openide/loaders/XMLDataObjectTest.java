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

import org.openide.filesystems.*;
import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.logging.Level;
import org.netbeans.junit.Log;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.openide.util.NbMutexEventProvider;
import org.openide.cookies.*;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

/**
 *
 * @author  Jaroslav Tulach
 */
public class XMLDataObjectTest extends org.netbeans.junit.NbTestCase {
    private FileObject data;
    private CharSequence log;

    public XMLDataObjectTest (String name) {
        super (name);
    }

    @Override
    protected void setUp () throws Exception {
        clearWorkDir();
        
        log = Log.enable("org.openide.loaders", Level.WARNING);
        
        super.setUp ();
        System.setProperty ("org.openide.util.Lookup", "org.openide.loaders.XMLDataObjectTest$Lkp");
        String fsstruct [] = new String [] {
        };
        FileSystem fs = TestUtilHid.createLocalFileSystem (getWorkDir(), fsstruct);
        data = FileUtil.createData (
            fs.getRoot (),
            "kuk/test/my.xml"
        );
        FileLock lock = data.lock ();
        OutputStream os = data.getOutputStream (lock);
        PrintStream p = new PrintStream (os);
        
        p.println ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        p.println ("<root>");
        p.println ("</root>");
        
        p.close ();
        lock.releaseLock ();

        // initialize the document before we start to measure the access
        assertNotNull("Constructor ready", XMLDataObject.cnstr);

        assertParse(false, false);
        assertParse(false, true);
        assertParse(true, false);
        assertParse(true, true);
    }

    private static void assertParse(boolean validate, boolean namespace) throws Exception {
        try {
            XMLUtil.parse(new InputSource(new ByteArrayInputStream(new byte[0])), validate, namespace, null, null);
        } catch (SAXParseException ex) {
        }
    }
    
    @Override
    protected void tearDown () throws Exception {
        super.tearDown ();
        TestUtilHid.destroyLocalFileSystem (getName());
        if (log.length() > 0) {
            fail("There should be no warnings:\n" + log);
        }
    }
    
    public void testGetStatusBehaviour () throws Exception {
        DataObject obj = DataObject.find (data);
        
        assertEquals ("Is xml", XMLDataObject.class, obj.getClass ());
        
        XMLDataObject xml = (XMLDataObject)obj;
        
        assertEquals ("not parsed yet", XMLDataObject.STATUS_NOT, xml.getStatus ());
        
        org.w3c.dom.Document doc = xml.getDocument ();
        assertEquals ("still not parsed as we have lazy document", XMLDataObject.STATUS_NOT, xml.getStatus ());
        
        String id = doc.getDoctype ().getPublicId ();
        assertEquals ("still not parsed as we have special support for publilc id", XMLDataObject.STATUS_NOT, xml.getStatus ());
        
        org.w3c.dom.Element e = doc.getDocumentElement ();
        assertNotNull ("Document parsed", doc);
        
        assertEquals ("status is ok", XMLDataObject.STATUS_OK, xml.getStatus ());
        
        assertNotNull("Has open cookie", xml.getCookie(OpenCookie.class));
        assertNotNull("Has open cookie in lookup", xml.getLookup().lookup(OpenCookie.class));
        
        
        Reference<Object> ref = new WeakReference<Object>(xml);
        xml = null;
        obj = null;
        doc = null;
        e = null;
        assertGC("Data object has to be garbage collectable", ref);


    }

    public void testCookieIsUpdatedWhenContentChanges () throws Exception {
        
        
        FileLock lck;
        DataObject obj;
        lck = data.lock();
        
        PCL pcl = new PCL ();
        
        // this next line causes the test to fail on 2004/03/03
        obj = DataObject.find (data);
        obj.addPropertyChangeListener (pcl);
        
        assertNull ("No instance cookie", obj.getCookie (org.openide.cookies.InstanceCookie.class));
        assertEquals (0, pcl.cnt);
        
        try {
            OutputStream ostm = data.getOutputStream(lck);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(ostm, "UTF8")); //NOI18N
            pw.println("<?xml version='1.0'?>"); //NOI18N
            pw.println("<!DOCTYPE driver PUBLIC '-//NetBeans//DTD JDBC Driver 1.0//EN' 'http://www.netbeans.org/dtds/jdbc-driver-1_0.dtd'>"); //NOI18N
            pw.println("<driver>"); //NOI18N
            pw.println("  <name value='somename'/>"); //NOI18N
            pw.println("  <class value='java.lang.String'/>"); //NOI18N
            pw.println("  <urls>"); //NOI18N
            pw.println("  </urls>"); //NOI18N
            pw.println("</driver>"); //NOI18N
            pw.flush();
            pw.close();
            ostm.close();
        } finally {
            lck.releaseLock();
        }
        assertEquals ("One change fired when the file was written", 1, pcl.cnt);
        assertNotNull ("There is an cookie", obj.getCookie (org.openide.cookies.InstanceCookie.class));

    }

    @RandomlyFails // NB-Core-Build #1691
    public void testToolbarsAreBrokenAsTheLookupIsClearedTooOftenIssue41360 () throws Exception {
        FileLock lck;
        DataObject obj;
        lck = data.lock();
        String id = "-//NetBeans//DTD Fake Toolbar 1.0//EN";
        
        XMLDataObject.Info info = new XMLDataObject.Info ();
        info.addProcessorClass (ToolbarProcessor.class);
        try {
            XMLDataObject.registerInfo (id, info);
            
            
            OutputStream ostm = data.getOutputStream(lck);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(ostm, "UTF8")); //NOI18N
            pw.println("<?xml version='1.0'?>"); //NOI18N
            pw.println("<!DOCTYPE toolbar PUBLIC '" + id + "' 'http://www.netbeans.org/dtds/jdbc-driver-1_0.dtd'>"); //NOI18N
            pw.println("<toolbar>"); //NOI18N
            pw.println("  <name value='somename'/>"); //NOI18N
            pw.println("  <class value='java.lang.String'/>"); //NOI18N
            pw.println("  <urls>"); //NOI18N
            pw.println("  </urls>"); //NOI18N
            pw.println("</toolbar>"); //NOI18N
            pw.flush();
            pw.close();
            ostm.close();
            
            obj = DataObject.find (data);
            PCL pcl = new PCL ();
            obj.addPropertyChangeListener (pcl);
            
            InstanceCookie cookie = (InstanceCookie)obj.getCookie (InstanceCookie.class);
            assertNotNull (cookie);
            assertEquals ("No changes yet", 0, pcl.cnt);
            
            assertNotNull("Data object in lkp", obj.getLookup().lookup(DataObject.class));
            assertNotNull("Data object in lkp", obj.getLookup().lookup(FileObject.class));
            assertNotNull("Data object in cookie", obj.getCookie(DataObject.class));
            checkLookup(obj, 4);

            pcl.cnt = 0;

            ostm = data.getOutputStream(lck);
            pw = new java.io.PrintWriter(new OutputStreamWriter(ostm, "UTF8")); //NOI18N
            pw.println("<?xml version='1.0'?>"); //NOI18N
            pw.println("<!DOCTYPE toolbar PUBLIC '" + id + "' 'http://www.netbeans.org/dtds/jdbc-driver-1_0.dtd'>"); //NOI18N
            pw.println("<toolbar>"); //NOI18N
            pw.println("  <name value='somename'/>"); //NOI18N
            pw.println("  <class value='java.lang.String'/>"); //NOI18N
            pw.println("  <urls>"); //NOI18N
            pw.println("  </urls>"); //NOI18N
            pw.println("</toolbar>"); //NOI18N
            pw.flush();
            pw.close();
            ostm.close();
            
            InstanceCookie newCookie = (InstanceCookie)obj.getCookie (InstanceCookie.class);
            assertNotNull (newCookie);
            assertEquals ("One change in document", 1, pcl.docChange);
            assertEquals ("The cookie is still the same", cookie, newCookie);
            assertEquals ("No cookie change", 0, pcl.cnt);
            
        } finally {
            XMLDataObject.registerInfo (id, null);
            lck.releaseLock ();
        }
    }

    public void testWrongUTFCharacer() throws Exception {
        FileLock lck;
        DataObject obj;

        FileObject d = data.getParent().createData("wrongutfchar.xml");

        lck = d.lock();


        OutputStream os = d.getOutputStream(lck);
        os.write(0xc5); // multibyte
        os.write(0x00); // wrong char after multibyte
        os.close();

        obj = DataObject.find(d);

        XMLDataObject xml = (XMLDataObject)obj;
        String id = xml.getDocument().getDoctype().getPublicId();
        assertEquals("No ID", null, id);
        
        assertEquals("No warnings\n" + log, 0, log.length());
    }
    
    public void testCheckLookupContent() throws DataObjectNotFoundException {
        DataObject obj = DataObject.find(data);
        checkLookup(obj, 12);
    }
    
    private static void checkLookup(DataObject obj, int expected) throws DataObjectNotFoundException {
        Collection<? extends Object> all = obj.getLookup().lookupAll(Object.class);

        int cnt = 0;
        for (Object object : all) {
            assertEquals("Is in lkp", object, obj.getLookup().lookup(object.getClass()));
            Class c = object.getClass();
            if (object instanceof EditorCookie) {
                c = EditorCookie.class;
            }
            if (object instanceof Node.Cookie) {
                assertEquals("Is in cookie: " + c.getSuperclass(), object, obj.getCookie(c));
            }
            cnt++;
        }
        assertEquals("There are some items:\n" + all, expected, cnt);
    }
    
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup 
    implements org.openide.loaders.Environment.Provider {
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            ic.add (this); // Environment.Provider
            ic.add (new NbMutexEventProvider());
        }
        
        public org.openide.util.Lookup getEnvironment (org.openide.loaders.DataObject obj) {
            if (obj instanceof XMLDataObject) {
                try {
                    XMLDataObject xml = (XMLDataObject)obj;
                    final String id = xml.getDocument ().getDoctype ().getPublicId ();
                    if (id != null) {
                        return org.openide.util.lookup.Lookups.singleton (new org.openide.cookies.InstanceCookie () {
                            public Object instanceCreate () {
                                return id;
                            }
                            public Class instanceClass () {
                                return String.class;
                            }
                            public String instanceName () {
                                return instanceClass ().getName ();
                            }
                        });
                    }
                } catch (Exception ex) {
                    fail (ex.getMessage ());
                }
            }
            return null;
        }
        
    } // end of Lkp

    
    /** Processor.
     */
    public static class ToolbarProcessor 
    implements XMLDataObject.Processor, InstanceCookie.Of {
        
        public void attachTo (org.openide.loaders.XMLDataObject xmlDO) {
        }
        
        public Class instanceClass () throws java.io.IOException, ClassNotFoundException {
            return getClass ();
        }
        
        public Object instanceCreate () throws java.io.IOException, ClassNotFoundException {
            return this;
        }
        
        public String instanceName () {
            return getClass ().getName ();
        }
        
        public boolean instanceOf (Class type) {
            return type.isAssignableFrom (getClass());
        }
        
    } // end of ToolbarProcessor

    public void testGetCookieCannotBeReentrantFromMoreThreads () throws Exception {
        FileLock lck;
        DataObject obj;
        lck = data.lock();
        String id = "-//NetBeans//DTD X Prcs 1.0//EN";
        
        XMLDataObject.Info info = new XMLDataObject.Info ();
        info.addProcessorClass (XProcessor.class);
        try {
            XMLDataObject.registerInfo (id, info);
            
            
            OutputStream ostm = data.getOutputStream(lck);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(ostm, "UTF8")); //NOI18N
            pw.println("<?xml version='1.0'?>"); //NOI18N
            pw.println("<!DOCTYPE toolbar PUBLIC '" + id + "' 'http://www.netbeans.org/dtds/jdbc-driver-1_0.dtd'>"); //NOI18N
            pw.println("<toolbar>"); //NOI18N
            pw.println("  <name value='somename'/>"); //NOI18N
            pw.println("  <class value='java.lang.String'/>"); //NOI18N
            pw.println("  <urls>"); //NOI18N
            pw.println("  </urls>"); //NOI18N
            pw.println("</toolbar>"); //NOI18N
            pw.flush();
            pw.close();
            ostm.close();
            
            obj = DataObject.find (data);
            
            Object ic = obj.getCookie(InstanceCookie.class);
            assertNotNull("There is a cookie", ic);
            assertEquals("The right class", XProcessor.class, ic.getClass());
            
            XProcessor xp = (XProcessor)ic;
            
            // now it can finish
            xp.task.waitFinished();
            
            assertNotNull("Cookie created", xp.cookie);
            assertEquals("It is the same as me", xp.cookie, xp);
        } finally {
            XMLDataObject.registerInfo (id, null);
            lck.releaseLock ();
        }
    }

    /** Processor.
     */
    public static class XProcessor 
    implements XMLDataObject.Processor, InstanceCookie.Of, Runnable {
        private XMLDataObject obj;
        private Node.Cookie cookie;
        private RequestProcessor.Task task;
        
        public void attachTo (org.openide.loaders.XMLDataObject xmlDO) {
            obj = xmlDO;
            task = RequestProcessor.getDefault().post(this);
            try {
                assertFalse("This is going to time out", task.waitFinished(500));
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                fail("No exceptions please");
            }
            assertNull("Cookie is still null", cookie);
            
        }
        
        public void run () {
            cookie = obj.getCookie(InstanceCookie.class);
        }
        
        public Class instanceClass () throws java.io.IOException, ClassNotFoundException {
            return getClass ();
        }
        
        public Object instanceCreate () throws java.io.IOException, ClassNotFoundException {
            return this;
        }
        
        public String instanceName () {
            return getClass ().getName ();
        }
        
        public boolean instanceOf (Class type) {
            return type.isAssignableFrom (getClass());
        }
        
    } // end of XProcessor
    
    private static class PCL implements java.beans.PropertyChangeListener {
        int cnt;
        int docChange;

        public void propertyChange (java.beans.PropertyChangeEvent ev) {
            if (DataObject.PROP_COOKIE.equals (ev.getPropertyName ())) {
                cnt++;
            }
            if (XMLDataObject.PROP_DOCUMENT.equals (ev.getPropertyName ())) {
                docChange++;
            }
        }
    } // end of PCL

    
}
