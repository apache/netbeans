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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
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
import javax.swing.text.Document;
import org.netbeans.junit.*;
import org.openide.cookies.OpenCookie;


/** Simulates the deadlock from issue 60917
 * @author Jaroslav Tulach
 */
public class RefusesInvalidationTest extends NbTestCase {
    
    public RefusesInvalidationTest(String name) {
        super(name);
    }
    
	
	protected void setUp() throws Exception {
		System.setProperty("org.openide.util.Lookup", RefusesInvalidationTest.class.getName() + "$Lkp");
		
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

		class AddAndRemoveAFile implements FileSystem.AtomicAction {
			DataObject[] all;
			
			public void run () throws IOException {
				FileObject[] two = our.files().toArray(new FileObject[0]);
				assertEquals("Two", 2, two.length);
				
				// or secondary
				two[1].delete();
				

				l.filesInside = new HashSet();
				all = f.getChildren();
				if (!l.filesInside.contains(two[0])) {
					fail("We should query our secondary file: " + l.filesInside);
				}
			}
		}
		
		AddAndRemoveAFile addRemove = new AddAndRemoveAFile();
		f.getPrimaryFile().getFileSystem().runAtomicAction(addRemove);

		FileObject[] children = f.getPrimaryFile().getChildren();
		assertEquals("One child", 1, children.length);
		DataObject obj = DataObject.find(children[0]);
		assertEquals("Object found", children[0], obj.getPrimaryFile());
		assertEquals("Object is the same as it tries to prevent to invalidate itself", our, obj);
		
		assertNotNull("Children computed", addRemove.all);
		assertEquals("Three of them: " + Arrays.asList(addRemove.all), 1, addRemove.all.length);
		assertEquals("Object is the same as it tries to prevent to invalidate itself", our, addRemove.all[0]);		
		
		DataObject[] all = f.getChildren();
		assertNotNull("Children computed", all);
		assertEquals("Three of them: " + Arrays.asList(all), 1, all.length);
		assertEquals("Object is the same as it tries to prevent to invalidate itself", our, all[0]);		
    }
    
    public static final class ForgetableLoader extends MultiFileLoader 
	implements VetoableChangeListener {
		PropertyChangeEvent lastEvent;
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
			m.addVetoableChangeListener(this);
			return m;
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry (obj, primaryFile);
        }
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            return new FileEntry(obj, secondaryFile);
        }

        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
			this.lastEvent = evt;
			throw new PropertyVetoException("Cannot change isValid", evt);
        }
    }
	
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        public Lkp() {
            this(new org.openide.util.lookup.InstanceContent());
        }
        
        private Lkp(org.openide.util.lookup.InstanceContent ic) {
            super(ic);
            ic.add(new Pool ());
        }
    }
    
    private static final class Pool extends DataLoaderPool {
        protected java.util.Enumeration loaders () {
			DataLoader extra = DataLoader.getLoader(ForgetableLoader.class);
			return org.openide.util.Enumerations.singleton (extra);
        }
    }
}
