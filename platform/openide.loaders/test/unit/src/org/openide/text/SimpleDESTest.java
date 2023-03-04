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

package org.openide.text;

import java.awt.EventQueue;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.*;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;

/** DefaultDataObject is supposed to have open operation that shows the text
 * editor or invokes a dialog with questions.
 *
 * @author  Jaroslav Tulach
 */
public class SimpleDESTest extends NbTestCase {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    private static SimpleDESTest RUNNING;
    
    private FileSystem lfs;
    private DataObject obj;
    private Logger LOG;
    
    /** Creates a new instance of DefaultSettingsContextTest */
    public SimpleDESTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    protected Node.Cookie createEditorCookie(SO so) {
        return (Node.Cookie)DataEditorSupport.create(so, so.getPrimaryEntry(), so.getCSet ());
    }
    
    @Override
    protected void setUp() throws java.lang.Exception {
        clearWorkDir ();
        LOG = Logger.getLogger("test." + getName());
        
        RUNNING = this;
        
        MockServices.setServices(DLP.class);
        super.setUp();
        
        LocalFileSystem l = new LocalFileSystem ();
        l.setRootDirectory (getWorkDir ());
        lfs = l;
        
        FileObject fo = FileUtil.createData (lfs.getRoot (), "AA/" + getName () + ".test");
        assertNotNull("file not found", fo);
        obj = DataObject.find(fo);
        
        assertEquals ("The right class", obj.getClass (), SO.class);
    }

    @RandomlyFails // NB-Core-Build #1210
    public void testHasEditorCookieForResonableContentOfFiles () throws Exception {
        doCookieCheck (true);
    }
    
    private void doCookieCheck (boolean hasEditCookie) throws Exception {
        EditorCookie c = tryToOpen (
            "Ahoj Jardo," +
            "how are you" +
            "\t\n\rBye"
        );
        assertNotNull (c);
        
        assertEquals (
            "Next questions results in the same cookie", 
            c, 
            obj.getCookie(EditorCookie.class)
        );
        assertEquals (
            "Print cookie is provided",
            c,
            obj.getCookie(org.openide.cookies.PrintCookie.class)
        );
        assertEquals (
            "CloseCookie as well",
            c,
            obj.getCookie(org.openide.cookies.CloseCookie.class)
        );
        
        if (hasEditCookie) {
            assertEquals (
                "EditCookie as well",
                c,
                obj.getCookie(org.openide.cookies.EditCookie.class)
            );
        } else {
            assertNull (
                "No EditCookie",
                obj.getCookie(org.openide.cookies.EditCookie.class)
            );
            
        }
        
        OpenCookie open = obj.getCookie(OpenCookie.class);
        open.open ();
        javax.swing.text.Document d = null;
        for (int i = 0; i < 10; i++) {
            d = c.getDocument();
            LOG.log(Level.INFO, "Round {0} document {1}", new Object[]{i, d});
            if (d != null) {
                break;
            }
            Thread.sleep(100);
            waitAWT();
        }
        assertNotNull ("Document is now openned", d);
        
        d.insertString(0, "Kuk", null);
        
        assertNotNull (
            "Now there is a save cookie", 
            obj.getCookie (org.openide.cookies.SaveCookie.class)
        );
    }
    
    private void waitAWT() throws Exception {
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
            }
        });
    }
    
    public void testItIsPossibleToMaskEditCookie () throws Exception {
        doCookieCheck (false);
    }
    
    private EditorCookie tryToOpen (String content) throws Exception {
        FileObject fo = obj.getPrimaryFile();
        FileLock lock = fo.lock();
        PrintStream os = new PrintStream (fo.getOutputStream(lock));
        os.print (content);
        os.close ();
        lock.releaseLock();
        
        return obj.getCookie(EditorCookie.class);
    }
    
    private static final class SL extends org.openide.loaders.UniFileLoader {
        public SL () {
            super (SO.class.getName ());
            getExtensions().addExtension("test");
        }
        protected org.openide.loaders.MultiDataObject createMultiObject(FileObject primaryFile) throws org.openide.loaders.DataObjectExistsException, java.io.IOException {
            return new SO (primaryFile);
        }
    } // end of SL
    
    static final class SO extends org.openide.loaders.MultiDataObject implements org.openide.nodes.CookieSet.Factory {
        private org.openide.nodes.Node.Cookie cookie;
        
        
        public SO (FileObject fo) throws org.openide.loaders.DataObjectExistsException {
            super (fo, SL.getLoader(SL.class));
            
            cookie = RUNNING.createEditorCookie(this);
            
            if (fo.getNameExt().indexOf ("MaskEdit") == -1) {
                getCookieSet ().add (cookie);
            } else {
                getCookieSet ().add (new Class[] { 
                    OpenCookie.class, 
                    org.openide.cookies.CloseCookie.class, EditorCookie.class, 
                    org.openide.cookies.PrintCookie.class
                }, this); 
            }
        }
        
        
        public org.openide.nodes.Node.Cookie createCookie (Class c) {
            return cookie;
        }

        final CookieSet getCSet() {
            return getCookieSet();
        }
    } // end of SO

    public static final class DLP extends org.openide.loaders.DataLoaderPool {
        protected java.util.Enumeration loaders() {
            return java.util.Collections.enumeration(
                java.util.Collections.singleton(
                    SL.getLoader (SL.class)
                )
            );
        }
    } // end of DataLoaderPool
}
