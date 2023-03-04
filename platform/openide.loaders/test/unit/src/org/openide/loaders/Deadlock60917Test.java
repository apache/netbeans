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
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.*;
import org.openide.nodes.Node;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import javax.swing.text.Document;
import org.netbeans.junit.*;
import org.netbeans.modules.editor.mimelookup.SharedMimeLookupCache;
import org.netbeans.modules.openide.util.NbMutexEventProvider;
import org.openide.cookies.OpenCookie;

/** Simulates the deadlock from issue 60917
 * @author Jaroslav Tulach
 */
@RandomlyFails
public class Deadlock60917Test extends NbTestCase {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    
    public Deadlock60917Test(String name) {
        super(name);
    }

    @Override protected Level logLevel() {
        return Level.INFO;
    }
    
    protected void setUp() throws Exception {
        System.setProperty("org.openide.util.Lookup", Deadlock60917Test.class.getName() + "$Lkp");
        
        super.setUp();
        
        Lookup l = Lookup.getDefault();
        if (!(l instanceof Lkp)) {
            fail("Wrong lookup: " + l);
        }
        
        clearWorkDir();
    }
    
    public void testWhatHappensWhenALoaderBecomesInvalidAndFileIsOpened() throws Exception {
        final ForgetableLoader l = DataLoader.getLoader(ForgetableLoader.class);
		FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), new String[] {
			"folder/f.keep",
			"folder/f.forget",
		});

		FileObject fo = lfs.findResource("folder");
		final DataFolder f = DataFolder.findFolder(fo);

		FileObject primary = lfs.findResource("folder/f.keep");

		final DataObject our = DataObject.find(primary);
		assertEquals("The right loader", l, our.getLoader());

		OpenCookie oc = our.getCookie(OpenCookie.class);
		oc.open();
		waitEQ();
		
		EditorCookie ec = our.getCookie(EditorCookie.class);
		assertNotNull("We have ec", ec);
		
		Document d = ec.openDocument();
		assertNotNull("There is a document", d);
		
		d.insertString(0, "Ahoj", null);
		
		assertTrue("Nows the doc is modified", ec.isModified());
		assertTrue("DO is modified", our.isModified());
		
		class BlockAWT implements Runnable {
			DataObject[] arr;
			
			public void run () {
				synchronized (this) {
					try {

						wait(1000);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
				arr = f.getChildren();
			}
		}
		final BlockAWT awt = new BlockAWT();
		
		class AddAndRemoveAFile implements FileSystem.AtomicAction {
			DataObject[] all;
			
			public void run () throws IOException {
				javax.swing.SwingUtilities.invokeLater(awt);
			
				FileObject[] two = our.files().toArray(new FileObject[0]);
				assertEquals("Two", 2, two.length);
				
				// delete just primary file
				//two[0].delete(((MultiDataObject)our).getPrimaryEntry().takeLock());
				// or secondary
				two[1].delete();
				

				l.filesInside = new HashSet();
				all = f.getChildren();
				if (!l.filesInside.contains(two[0])) {
					fail("We should query our secondary file: " + l.filesInside);
				}
				
				waitEQ();
				assertNotNull("Children computed", awt.arr);
			}
		}
		
		AddAndRemoveAFile addRemove = new AddAndRemoveAFile();
		f.getPrimaryFile().getFileSystem().runAtomicAction(addRemove);
		
		DataObject[] all = f.getChildren();
		
		assertNotNull("Children computed", awt.arr);
		assertEquals("Three of them: " + Arrays.asList(awt.arr), 1, awt.arr.length);
		assertEquals("Still remains the same old object: ", our, awt.arr[0]);
    }

    public void testWhatHappenUnderLock () throws Exception {
        org.openide.nodes.Children.MUTEX.readAccess (new org.openide.util.Mutex.ExceptionAction () {
            public Object run () throws Exception {
                testWhatHappensWhenALoaderBecomesInvalidAndFileIsOpened();
                return null;
            }
        });
    }

    public static final class ForgetableLoader extends MultiFileLoader {

		HashSet filesInside;
		
        public ForgetableLoader () {
            super(MultiDataObject.class);
        }
        protected String displayName() {
            return "ForgetableLoader";
        }
        /** Recognizes just two files - .forget and .keep at once.
         */
        protected FileObject findPrimaryFile(FileObject fo) {
			if (filesInside != null) {
//				System.err.println("file: " + fo);
//				Thread.dumpStack();
				filesInside.add(fo);
			} else {
				assertFalse("We cannot be queried from recognizer thread: ", FolderList.isFolderRecognizerThread());
			}
			
			FileObject forget = FileUtil.findBrother(fo, "forget");
			FileObject keep = FileUtil.findBrother(fo, "keep");
			if (keep == null || forget == null) {
				return null;
			}
			return fo == keep || fo == forget ? keep : null;
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            MultiDataObject m = new MultiDataObject (primaryFile, this);
			m.getCookieSet().add((Node.Cookie)DataEditorSupport.create(m, m.getPrimaryEntry(), m.getCookieSet()));
			return m;
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry (obj, primaryFile);
        }
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            return new FileEntry(obj, secondaryFile);
        }
    }
	
	private static void waitEQ() {
		if (javax.swing.SwingUtilities.isEventDispatchThread()) {
			return;
		}
		
		try {

			javax.swing.SwingUtilities.invokeAndWait(new Runnable() { 
				public void run() { } 
			});
			return;
		} catch (InvocationTargetException ex) {
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		fail("An exception happened");
	}
	
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        public Lkp() {
            this(new org.openide.util.lookup.InstanceContent());
        }
        
        private Lkp(org.openide.util.lookup.InstanceContent ic) {
            super(ic);
            ic.add(new Pool ());
            ic.add(new DD());
            ic.add(new NbMutexEventProvider());
            ic.add(new SharedMimeLookupCache()); // Fixed failing MimePath.getLookup()
        }
    }
    
    private static final class Pool extends DataLoaderPool {
        
        protected java.util.Enumeration loaders () {
			DataLoader extra = DataLoader.getLoader(ForgetableLoader.class);
			return org.openide.util.Enumerations.singleton (extra);
        }
    }
    /** Our own dialog displayer.
     */
    private static final class DD extends org.openide.DialogDisplayer {
        
        public java.awt.Dialog createDialog(org.openide.DialogDescriptor descriptor) {
            throw new IllegalStateException ("Not implemented");
        }
        
        public Object notify(org.openide.NotifyDescriptor descriptor) {
			try {
				// we need to get into EQ - as the regular DialogDescriptor does
				waitEQ();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return NotifyDescriptor.CLOSED_OPTION;
        }
        
    } // end of DD
	
}
