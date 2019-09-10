/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.editor.impl;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import javax.swing.text.EditorKit;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.EditorTestLookup;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.lib.KitsTracker;
import org.openide.filesystems.FileObject;

/**
 *
 * @author vita
 */
public class KitsTrackerTest extends NbTestCase {
    
    /** Creates a new instance of KitsTrackerTest */
    public KitsTrackerTest(String name) {
        super(name);
    }
    
    protected @Override void setUp() throws Exception {
        super.setUp();

        clearWorkDir();

        EditorTestLookup.setLookup(
            new URL[] {
                KitsTrackerTest.class.getResource("/org/netbeans/modules/editor/impl/KitsTracker-test-layer.xml"),
            },
            new Object[] {},
            getClass().getClassLoader()
        );
    }
    
    public void testFindKit() {
        String mimeType = KitsTrackerImpl.getInstance().findMimeType(TestAKit.class);
        assertEquals("Wrong mime type", "text/x-type-A", mimeType);
    }
    
    public void testFindKitFromSuper() {
        String mimeType = KitsTrackerImpl.getInstance().findMimeType(TestAChildKit.class);
        assertEquals("Wrong mime type", "text/x-type-A", mimeType);
    }

    public void testFindSharedKit() {
        String mimeType = KitsTrackerImpl.getInstance().findMimeType(SharedKit.class);
        assertNull("Should not get any mimetype", mimeType);
        
        List<String> mimeTypes = KitsTrackerImpl.getInstance().getMimeTypesForKitClass(SharedKit.class);
        assertEquals("Wrong number of mime types", 2, mimeTypes.size());
        assertTrue("Should be registered for text/x-type-B-1", mimeTypes.contains("text/x-type-B-1"));
        assertTrue("Should be registered for text/x-type-B-2", mimeTypes.contains("text/x-type-B-2"));
    }

    public void testFindKitForKnownSupers() {
        Class [] kitClasses = new Class [] {
            BaseKit.class, ExtKit.class, NbEditorKit.class
        };
        
        for(Class c : kitClasses) {
            String mimeType = KitsTrackerImpl.getInstance().findMimeType(c);
            assertNull("Shouldn't have mimetype for " + c, mimeType);
            
            List<String> mimeTypes = KitsTrackerImpl.getInstance().getMimeTypesForKitClass(c);
            assertEquals("Wrong number of mime types for " + c, 0, mimeTypes.size());
        }
    }
    
    public void testFindKitForNull() {
        String mimeType = KitsTrackerImpl.getInstance().findMimeType(null);
        assertEquals("Wrong mimetype for null", "", mimeType);

        List<String> mimeTypes = KitsTrackerImpl.getInstance().getMimeTypesForKitClass(null);
        assertEquals("Wrong number of mime types for null", 1, mimeTypes.size());
        assertEquals("Wrong mime type for null", "", mimeTypes.get(0));
    }
    
    // o.n.editor.BaseKit uses similar code
    public void testKitsTrackerCallable() throws Exception {
        Class<?> clazz = getClass().getClassLoader().loadClass("org.netbeans.modules.editor.lib.KitsTracker"); //NOI18N
        Method getInstanceMethod = clazz.getDeclaredMethod("getInstance"); //NOI18N
        Method findMimeTypeMethod = clazz.getDeclaredMethod("findMimeType", Class.class); //NOI18N
        Object kitsTracker = getInstanceMethod.invoke(null);
        String mimeType = (String) findMimeTypeMethod.invoke(kitsTracker, EditorKit.class);
        assertNull("EditorKit.class should not have a mime type", mimeType);
    }

    public void testKitsTrackerImplInstalled() throws Exception {
        KitsTracker tracker = KitsTracker.getInstance();
        assertEquals("Wrong KitsTracker implementation installed", KitsTrackerImpl.class, tracker.getClass());
    }
    
    public static class TestAKit extends NbEditorKit {

        @Override
        public String getContentType() {
            return "text/x-type-A";
        }
        
    } // End of TestAKit class

    public static class TestAChildKit extends TestAKit {

    } // End of TestAChildKit class
    
    public static SharedKit sharedKit(FileObject f) {
        String mimeType = f.getParent().getPath().substring(8); //'Editors/'
        return new SharedKit(mimeType);
    }
    
    public static class SharedKit extends NbEditorKit {
        private final String mimeType;
        
        public SharedKit(String mimeType) {
            this.mimeType = mimeType;
        }
        
        @Override
        public String getContentType() {
            return mimeType;
        }
        
    } // End of SharedKit class

}
