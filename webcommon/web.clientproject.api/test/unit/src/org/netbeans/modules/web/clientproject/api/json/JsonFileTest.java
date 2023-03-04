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
package org.netbeans.modules.web.clientproject.api.json;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;

public class JsonFileTest extends NbTestCase {

    private static final String PROP_NAME = "PROP_NAME";
    private static final String PROP_CITY = "PROP_CITY";

    private static final String KEY_NAME = "name";
    private static final String KEY_AGE = "age";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_STREET = "street";
    private static final String KEY_CITY = "city";

    private FileObject directory;


    public JsonFileTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        File dummy = new File(getWorkDir(), "dummy");
        assertTrue(dummy.mkdir());
        directory = FileUtil.toFileObject(dummy);
        assertNotNull(directory);
    }

    public void testWatchedFieldsAll() {
        JsonFile.WatchedFields fields = JsonFile.WatchedFields.all();
        try {
            fields.add(PROP_NAME, KEY_NAME);
            fail("Should not get here");
        } catch (IllegalStateException ex) {
            // noop
        }
        assertNull(fields.getData());
    }

    public void testWatchedFields() {
        JsonFile.WatchedFields fields = JsonFile.WatchedFields.create();
        List<Pair<String, String[]>> data = fields.getData();
        assertNotNull(data);
        assert data != null;
        assertEquals(0, data.size());
        fields.add(PROP_NAME, KEY_NAME);
        data = fields.getData();
        assertNotNull(data);
        assert data != null;
        assertEquals(1, data.size());
        JsonFile jsonFile = new JsonFile("dummy.json", directory, fields);
        try {
            fields.add(PROP_NAME, KEY_NAME);
            fail("Should not get here");
        } catch (IllegalStateException ex) {
            // noop
        }
    }

    public void testNameChange() throws Exception {
        JsonFile jsonFile = new JsonFile("name.json", directory, JsonFile.WatchedFields.create()
                .add(PROP_NAME, KEY_NAME));
        CountDownLatch countDownLatch = new CountDownLatch(2);
        PropertyChangeListenerImpl listener = new PropertyChangeListenerImpl(countDownLatch);
        jsonFile.addPropertyChangeListener(listener);
        putAndChangeData(jsonFile);
        countDownLatch.await(10, TimeUnit.SECONDS);
        // check events
        Map<String, List<PropertyChangeEvent>> allEvents = listener.getAllEvents();
        assertEquals(1, allEvents.size());
        List<PropertyChangeEvent> events = allEvents.get(PROP_NAME);
        assertNotNull(events);
        assertEquals(2, events.size());
        PropertyChangeEvent event = events.get(0);
        assertEquals(PROP_NAME, event.getPropertyName());
        assertNull(event.getOldValue());
        assertEquals("John Doe", event.getNewValue());
        event = events.get(1);
        assertEquals(PROP_NAME, event.getPropertyName());
        assertEquals("John Doe", event.getOldValue());
        assertEquals("Jack Black", event.getNewValue());
    }

    public void testAddressCityChange() throws Exception {
        JsonFile jsonFile = new JsonFile("address-city.json", directory, JsonFile.WatchedFields.create()
                .add(PROP_CITY, KEY_ADDRESS, KEY_CITY));
        CountDownLatch countDownLatch = new CountDownLatch(2);
        PropertyChangeListenerImpl listener = new PropertyChangeListenerImpl(countDownLatch);
        jsonFile.addPropertyChangeListener(listener);
        putAndChangeData(jsonFile);
        countDownLatch.await(10, TimeUnit.SECONDS);
        // check events
        Map<String, List<PropertyChangeEvent>> allEvents = listener.getAllEvents();
        assertEquals(1, allEvents.size());
        List<PropertyChangeEvent> events = allEvents.get(PROP_CITY);
        assertNotNull(events);
        assertEquals(2, events.size());
        PropertyChangeEvent event = events.get(0);
        assertEquals(PROP_CITY, event.getPropertyName());
        assertNull(event.getOldValue());
        assertEquals("Prague", event.getNewValue());
        event = events.get(1);
        assertEquals(PROP_CITY, event.getPropertyName());
        assertEquals("Prague", event.getOldValue());
        assertEquals("Praha", event.getNewValue());
    }

    public void testAllWatchedFields() throws Exception {
        JsonFile jsonFile = new JsonFile("all.json", directory, JsonFile.WatchedFields.all());
        // first, init file content
        putData(jsonFile);
        CountDownLatch countDownLatch = new CountDownLatch(2);
        PropertyChangeListenerImpl listener = new PropertyChangeListenerImpl(countDownLatch);
        jsonFile.addPropertyChangeListener(listener);
        // now, change data
        putAndChangeData(jsonFile);
        countDownLatch.await(10, TimeUnit.SECONDS);
        // check events
        Map<String, List<PropertyChangeEvent>> allEvents = listener.getAllEvents();
        assertEquals(1, allEvents.size());
        // 'null' events
        List<PropertyChangeEvent> events = allEvents.get(null);
        assertNotNull(events);
        assertEquals(2, events.size());
    }

    public void testNoWatchedFields() throws Exception {
        JsonFile jsonFile = new JsonFile("none.json", directory, JsonFile.WatchedFields.create());
        CountDownLatch countDownLatch = new CountDownLatch(1);
        PropertyChangeListenerImpl listener = new PropertyChangeListenerImpl(countDownLatch);
        jsonFile.addPropertyChangeListener(listener);
        putAndChangeData(jsonFile);
        countDownLatch.await(5, TimeUnit.SECONDS);
        // check events
        Map<String, List<PropertyChangeEvent>> allEvents = listener.getAllEvents();
        assertEquals(0, allEvents.size());
    }

    public void testNoContentChange() throws Exception {
        JsonFile jsonFile = new JsonFile("same.json", directory, JsonFile.WatchedFields.all());
        // first, init file content
        putData(jsonFile);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        PropertyChangeListenerImpl listener = new PropertyChangeListenerImpl(countDownLatch);
        jsonFile.addPropertyChangeListener(listener);
        // now, "change" it
        putData(jsonFile);
        countDownLatch.await(5, TimeUnit.SECONDS);
        // check events
        Map<String, List<PropertyChangeEvent>> allEvents = listener.getAllEvents();
        assertEquals(0, allEvents.size());
    }

    private void putData(JsonFile jsonFile) throws Exception {
        putAndPossiblyChangeData(jsonFile, false);
    }

    private void putAndChangeData(JsonFile jsonFile) throws Exception {
        putAndPossiblyChangeData(jsonFile, true);
    }

    private void putAndPossiblyChangeData(JsonFile jsonFile, boolean changeData) throws Exception {
        // write default data
        final Map<String, Object> data = getDefaultData();
        writeFile(jsonFile.getFile(), data);
        // needed for FS to notice the change
        Thread.sleep(1000);
        // change name
        if (changeData) {
            data.put(KEY_NAME, "Jack Black");
        }
        writeFile(jsonFile.getFile(), data);
        // needed for FS to notice the change
        Thread.sleep(1000);
        // change address
        if (changeData) {
            ((Map<String, Object>) data.get(KEY_ADDRESS))
                    .put(KEY_CITY, "Praha");
        }
        writeFile(jsonFile.getFile(), data);
        // needed for FS to notice the change
        Thread.sleep(1000);
    }

    private Map<String, Object> getDefaultData() {
        Map<String, Object> data = new HashMap<>();
        data.put(KEY_NAME, "John Doe");
        data.put(KEY_AGE, 42);
        Map<String, Object> address = new HashMap<>();
        address.put(KEY_STREET, "V Parku 8");
        address.put(KEY_CITY, "Prague");
        data.put(KEY_ADDRESS, address);
        return data;
    }

    private void writeFile(File file, Map<String, Object> data) throws IOException {
        try (Writer out = new FileWriter(file)) {
            JSONObject.writeJSONString(data, out);
        }
        assertTrue(file.isFile());
        refreshForFile(file);
    }

    private void refreshForFile(File file) {
        FileUtil.refreshFor(file.getParentFile());
    }

    //~ Inner classes

    private static final class PropertyChangeListenerImpl implements PropertyChangeListener {

        // @GuardedBy("this")
        private final Map<String, List<PropertyChangeEvent>> allEvents = new HashMap<>();
        private final CountDownLatch countDownLatch;


        public PropertyChangeListenerImpl(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public synchronized void propertyChange(PropertyChangeEvent evt) {
            Assert.assertNotNull(evt);
            String propertyName = evt.getPropertyName();
            List<PropertyChangeEvent> events = allEvents.get(propertyName);
            if (events == null) {
                events = new ArrayList<>();
                allEvents.put(propertyName, events);
            }
            events.add(evt);
            countDownLatch.countDown();
        }

        public synchronized Map<String, List<PropertyChangeEvent>> getAllEvents() {
            return new HashMap<>(allEvents);
        }

    }

}
