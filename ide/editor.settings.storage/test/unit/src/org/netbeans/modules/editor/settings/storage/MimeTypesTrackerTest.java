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

package org.netbeans.modules.editor.settings.storage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Vita Stejskal
 */
public class MimeTypesTrackerTest extends NbTestCase {

    private static final String BASE = "Some/Folder/SomeWhere";
    
    /** Creates a new instance of MimeTypesTrackerTest */
    public MimeTypesTrackerTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    
        EditorTestLookup.setLookup(
            new URL[] { },
            getWorkDir(),
            new Object[] {},
            getClass().getClassLoader()
        );

        MimeTypesTracker.synchronous = true;
    }
    
    public void testBasic() throws Exception {
        TestUtilities.createFolder(BASE + "/text/plain");
        MimeTypesTracker mtt = new MimeTypesTracker(null, BASE);
        Collection<String> mimeTypes = mtt.getMimeTypes();
        
        assertNotNull("MimeTypes should not be null", mimeTypes);
        assertEquals("Wrong # of recognized mime types", 1, mimeTypes.size());
        assertEquals("Wrong mime type", "text/plain", mimeTypes.iterator().next());
    }

    public void testGC() throws Exception {
        TestUtilities.createFolder(BASE + "/text/plain");
        MimeTypesTracker mtt = new MimeTypesTracker(null, BASE);
        Collection<String> mimeTypes = mtt.getMimeTypes();
        
        assertNotNull("MimeTypes should not be null", mimeTypes);
        assertEquals("Wrong # of recognized mime types", 1, mimeTypes.size());
        assertEquals("Wrong mime type", "text/plain", mimeTypes.iterator().next());
        
        WeakReference<MimeTypesTracker> ref = new WeakReference<MimeTypesTracker>(mtt);
        mtt = null;
        assertGC("Can't GC the tracker", ref);
    }

    public void testWellKnownMimeTypes() throws Exception {
        // See http://www.iana.org/assignments/media-types/ for details
        String [] mimeTypes = new String [] {
            "application/pdf", //NOI18N
            "audio/mpeg", //NOI18N
            "image/jpeg", //NOI18N
            "message/news", //NOI18N
            "model/vrml", //NOI18N
            "multipart/mixed", //NOI18N
            "text/plain", //NOI18N
            "video/mpeg" //NOI18N
        };
        
        for(String s : mimeTypes) {
            TestUtilities.createFolder(BASE + "/" + s);
        }
        
        MimeTypesTracker mtt = new MimeTypesTracker(null, BASE);
        Collection<String> recognized = mtt.getMimeTypes();
        
        assertNotNull("MimeTypes should not be null", recognized);
        assertEquals("Wrong # of recognized mime types", mimeTypes.length, recognized.size());
        assertEquals("Wrong mime types", new TreeSet<String>(Arrays.asList(mimeTypes)), new TreeSet<String>(recognized));
    }
    
    public void testFoldersNotRecognized() throws IOException {
        String [] mimeTypes = new String [] {
            "NetBeans/Default", //NOI18N
            "Default", //NOI18N
            "KeyBindings/whatever", //NOI18N
            "Toolbars/Default", //NOI18N
            "SideBar/MySideBar", //NOI18N
        };
        
        for(String s : mimeTypes) {
            TestUtilities.createFolder(BASE + "/" + s);
        }
        
        MimeTypesTracker mtt = new MimeTypesTracker(null, BASE);
        Collection<String> recognized = mtt.getMimeTypes();
        
        assertNotNull("MimeTypes should not be null", recognized);
        assertEquals("Wrong # of recognized mime types", 0, recognized.size());
    }

    public void testFilesNotRecognized() throws IOException {
        TestUtilities.createFile(BASE + "/text/hello.xml");
        TestUtilities.createFile(BASE + "/text");
        TestUtilities.createFile(BASE + "/application/pdf");
        
        MimeTypesTracker mtt = new MimeTypesTracker(null, BASE);
        Collection<String> recognized = mtt.getMimeTypes();
        
        assertNotNull("MimeTypes should not be null", recognized);
        assertEquals("Wrong # of recognized mime types", 0, recognized.size());
    }
    
    public void testRecognition() throws IOException {
        final MimeTypesTracker mtt = new MimeTypesTracker(null, BASE);

        {
        Collection<String> recognized = mtt.getMimeTypes();
        assertNotNull("MimeTypes should not be null", recognized);
        assertEquals("Wrong # of recognized mime types", 0, recognized.size());
        }
        
        {
        TestUtilities.createFolder(BASE, 120);
        Collection<String> recognized = mtt.getMimeTypes();
        assertNotNull("MimeTypes should not be null", recognized);
        assertEquals("Wrong # of recognized mime types", 0, recognized.size());
        }
        
        {
        TestUtilities.createFolder(BASE + "/text", 120);
        Collection<String> recognized = mtt.getMimeTypes();
        assertNotNull("MimeTypes should not be null", recognized);
        assertEquals("Wrong # of recognized mime types", 0, recognized.size());
        }
        
        {
        TestUtilities.createFolder(BASE + "/text/x-java", 120);
        Collection<String> recognized = mtt.getMimeTypes();
        assertNotNull("MimeTypes should not be null", recognized);
        assertEquals("Wrong # of recognized mime types", 1, recognized.size());
        assertEquals("Wrong mime type recognized", "text/x-java", recognized.iterator().next());
        }

        {
        TestUtilities.delete(BASE + "/text/x-java", 120);
        Collection<String> recognized = mtt.getMimeTypes();
        assertNotNull("MimeTypes should not be null", recognized);
        assertEquals("Wrong # of recognized mime types", 0, recognized.size());
        }
        {
        TestUtilities.delete(BASE + "/text", 120);
        Collection<String> recognized = mtt.getMimeTypes();
        assertNotNull("MimeTypes should not be null", recognized);
        assertEquals("Wrong # of recognized mime types", 0, recognized.size());
        }
        {
        TestUtilities.delete(BASE, 120);
        Collection<String> recognized = mtt.getMimeTypes();
        assertNotNull("MimeTypes should not be null", recognized);
        assertEquals("Wrong # of recognized mime types", 0, recognized.size());
        }
    }
    
    public void testEvents() throws IOException {
        final L listener = new L();
        final MimeTypesTracker mtt = new MimeTypesTracker(null, BASE);
        mtt.addPropertyChangeListener(listener);
        
        {
        Collection<String> recognized = mtt.getMimeTypes();
        assertNotNull("MimeTypes should not be null", recognized);
        assertEquals("Wrong # of recognized mime types", 0, recognized.size());
        assertEquals("Wrong # of events fired", 0, listener.events);
        }
        
        {
        TestUtilities.createFolder(BASE + "/text/x-java", 120);
        Collection<String> recognized = mtt.getMimeTypes();
        assertNotNull("MimeTypes should not be null", recognized);
        assertEquals("Wrong # of recognized mime types", 1, recognized.size());
        assertEquals("Wrong mime type recognized", "text/x-java", recognized.iterator().next());
        assertEquals("Wrong # of events fired", 1, listener.events);
        assertEquals("Wrong change event name", MimeTypesTracker.PROP_MIME_TYPES, listener.lastEventName);
        assertTrue("Wrong change event old value", listener.lastEventOldValue instanceof Map);
        assertEquals("Wrong change event old value contents", 0, ((Map) listener.lastEventOldValue).size());
        assertTrue("Wrong change event new value", listener.lastEventNewValue instanceof Map);
        assertEquals("Wrong change event new value contents", 1, ((Map) listener.lastEventNewValue).size());
        assertEquals("Wrong change event new value mime type", "text/x-java", ((Map) listener.lastEventNewValue).keySet().iterator().next());
        }

        {
        listener.reset();
        TestUtilities.delete(BASE, 120);
        Collection<String> recognized = mtt.getMimeTypes();
        assertNotNull("MimeTypes should not be null", recognized);
        assertEquals("Wrong # of recognized mime types", 0, recognized.size());
        assertEquals("Wrong # of events fired", 1, listener.events);
        assertEquals("Wrong change event name", MimeTypesTracker.PROP_MIME_TYPES, listener.lastEventName);
        assertTrue("Wrong change event old value", listener.lastEventOldValue instanceof Map);
        assertEquals("Wrong change event old value contents", 1, ((Map) listener.lastEventOldValue).size());
        assertEquals("Wrong change event old value mime type", "text/x-java", ((Map) listener.lastEventOldValue).keySet().iterator().next());
        assertTrue("Wrong change event new value", listener.lastEventNewValue instanceof Map);
        assertEquals("Wrong change event new value contents", 0, ((Map) listener.lastEventNewValue).size());
        }
    }

    public void testEvents2() throws IOException {
        final L listener = new L();
        final MimeTypesTracker mtt = new MimeTypesTracker(null, BASE);
        mtt.addPropertyChangeListener(listener);
        
        {
        Collection<String> recognized = mtt.getMimeTypes();
        assertNotNull("MimeTypes should not be null", recognized);
        assertEquals("Wrong # of recognized mime types", 0, recognized.size());
        assertEquals("Wrong # of events fired", 0, listener.events);
        }
        
        {
        TestUtilities.createFolder(BASE + "/text/plain", 120);
        Collection<String> recognized = mtt.getMimeTypes();
        assertNotNull("MimeTypes should not be null", recognized);
        assertEquals("Wrong # of recognized mime types", 1, recognized.size());
        assertEquals("Wrong mime type recognized", "text/plain", recognized.iterator().next());
        assertEquals("Wrong # of events fired", 1, listener.events);
        }

        {
        listener.reset();
        TestUtilities.createFolder(BASE + "/NetBeans", 120);
        Collection<String> recognized = mtt.getMimeTypes();
        assertNotNull("MimeTypes should not be null", recognized);
        assertEquals("Wrong # of recognized mime types", 1, recognized.size());
        assertEquals("Wrong mime type recognized", "text/plain", recognized.iterator().next());
        assertEquals("Wrong # of events fired", 0, listener.events);
        }
        
        {
        listener.reset();
        TestUtilities.createFolder(BASE + "/text/plain/NetBeans", 120);
        Collection<String> recognized = mtt.getMimeTypes();
        assertNotNull("MimeTypes should not be null", recognized);
        assertEquals("Wrong # of recognized mime types", 1, recognized.size());
        assertEquals("Wrong mime type recognized", "text/plain", recognized.iterator().next());
        assertEquals("Wrong # of events fired", 0, listener.events);
        }
    }
    
    private static final class L implements PropertyChangeListener {
        public int events;
        public String lastEventName;
        public Object lastEventOldValue;
        public Object lastEventNewValue;
        
        public void propertyChange(PropertyChangeEvent evt) {
            events++;
            lastEventName = evt.getPropertyName();
            lastEventOldValue = evt.getOldValue();
            lastEventNewValue = evt.getNewValue();
        }
        
        public void reset() {
            events = 0;
            lastEventName = null;
            lastEventOldValue = null;
            lastEventNewValue = null;
        }
    }
}
