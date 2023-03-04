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
package org.netbeans.modules.javascript.nodejs.file;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Assert;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class PackageJsonTest extends NbTestCase {

    private static final ExecutorService EXECUTORS = Executors.newCachedThreadPool();

    private FileObject directory;
    private PackageJson packageJson;


    public PackageJsonTest(String name) {
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
        packageJson = new PackageJson(directory);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        packageJson.cleanup();
    }

    public void testNoPackageJson() {
        assertFalse(packageJson.exists());
        assertEquals(getFile().getAbsolutePath(), packageJson.getPath());
    }

    public void testPackageJson() throws Exception {
        writeFile(getData(true, false));
        assertTrue(packageJson.exists());
        assertEquals(getFile().getAbsolutePath(), packageJson.getPath());
    }

    public void testDependencies() throws Exception {
        PackageJson invalidPackageJson = new PackageJson(FileUtil.toFileObject(getDataDir()), "invalid-package.json");
        assertTrue(invalidPackageJson.getFile().getAbsolutePath(), invalidPackageJson.exists());
        PackageJson.NpmDependencies dependencies = invalidPackageJson.getDependencies();
        assertEquals(28, dependencies.getCount());
        assertEquals(7, dependencies.dependencies.size());
        assertEquals(7, dependencies.devDependencies.size());
        assertEquals(7, dependencies.peerDependencies.size());
        assertEquals(7, dependencies.optionalDependencies.size());
        Map<String, String> expectedDependencies = new HashMap<>();
        expectedDependencies.put("a/b", "1.13.1");
        expectedDependencies.put("c/d", "1");
        expectedDependencies.put("e/f", "1.5");
        expectedDependencies.put("g/h", "null");
        expectedDependencies.put("i/j", "true");
        expectedDependencies.put("k/l", "{myver=123}");
        expectedDependencies.put("m/n", "[1, 2]");
        assertEquals(expectedDependencies, dependencies.dependencies);
        Map<String, String> expectedDevDependencies = new HashMap<>();
        expectedDevDependencies.put("aa/bb", "42");
        expectedDevDependencies.put("cc/dd", "1");
        expectedDevDependencies.put("ee/ff", "1.5");
        expectedDevDependencies.put("gg/hh", "null");
        expectedDevDependencies.put("ii/jj", "true");
        expectedDevDependencies.put("kk/ll", "{myver=123}");
        expectedDevDependencies.put("mm/nn", "[1, 2]");
        assertEquals(expectedDevDependencies, dependencies.devDependencies);
        Map<String, String> expectedPeerDependencies = new HashMap<>();
        expectedPeerDependencies.put("aaa/bbb", "42");
        expectedPeerDependencies.put("ccc/ddd", "1");
        expectedPeerDependencies.put("eee/fff", "1.5");
        expectedPeerDependencies.put("ggg/hhh", "null");
        expectedPeerDependencies.put("iii/jjj", "true");
        expectedPeerDependencies.put("kkk/lll", "{myver=123}");
        expectedPeerDependencies.put("mmm/nnn", "[1, 2]");
        assertEquals(expectedPeerDependencies, dependencies.peerDependencies);
        Map<String, String> expectedOptionalDependencies = new HashMap<>();
        expectedOptionalDependencies.put("aaaa/bbbb", "42");
        expectedOptionalDependencies.put("cccc/dddd", "1");
        expectedOptionalDependencies.put("eeee/ffff", "1.5");
        expectedOptionalDependencies.put("gggg/hhhh", "null");
        expectedOptionalDependencies.put("iiii/jjjj", "true");
        expectedOptionalDependencies.put("kkkk/llll", "{myver=123}");
        expectedOptionalDependencies.put("mmmm/nnnn", "[1, 2]");
        assertEquals(expectedOptionalDependencies, dependencies.optionalDependencies);
    }

    public void testNameChange() throws Exception {
        CountDownLatch countDownLatch1 = new CountDownLatch(1);
        PropertyChangeListenerImpl listener = new PropertyChangeListenerImpl();
        listener.setCountDownLatch(countDownLatch1);
        packageJson.addPropertyChangeListener(listener);
        asyncWriteFile(getData(true, false));
        // wait
        countDownLatch1.await(1, TimeUnit.MINUTES);
        // needed for FS to notice the change
        Thread.sleep(1000);
        // change name
        CountDownLatch countDownLatch2 = new CountDownLatch(1);
        listener.setCountDownLatch(countDownLatch2);
        Map<String, Object> newData = getData(true, false);
        newData.put(PackageJson.FIELD_NAME, "YourProject");
        asyncWriteFile(newData);
        // wait
        countDownLatch2.await(1, TimeUnit.MINUTES);
        // check events
        Map<String, List<PropertyChangeEvent>> allEvents = listener.getAllEvents();
        assertEquals(1, allEvents.size());
        List<PropertyChangeEvent> events = allEvents.get(PackageJson.PROP_NAME);
        assertNotNull(events);
        assertEquals(2, events.size());
        PropertyChangeEvent event = events.get(0);
        assertEquals(PackageJson.PROP_NAME, event.getPropertyName());
        assertNull(event.getOldValue());
        assertEquals("MyProject", event.getNewValue());
        event = events.get(1);
        assertEquals(PackageJson.PROP_NAME, event.getPropertyName());
        assertEquals("MyProject", event.getOldValue());
        assertEquals("YourProject", event.getNewValue());
    }

    public void testStartFileChange() throws Exception {
        CountDownLatch countDownLatch1 = new CountDownLatch(1);
        PropertyChangeListenerImpl listener = new PropertyChangeListenerImpl();
        listener.setCountDownLatch(countDownLatch1);
        packageJson.addPropertyChangeListener(listener);
        asyncWriteFile(getData(false, true));
        // wait
        countDownLatch1.await(1, TimeUnit.MINUTES);
        // needed for FS to notice the change
        Thread.sleep(1000);
        // change start file
        CountDownLatch countDownLatch2 = new CountDownLatch(1);
        listener.setCountDownLatch(countDownLatch2);
        Map<String, Object> newData = getData(false, true);
        ((Map<String, Object>) newData.get(PackageJson.FIELD_SCRIPTS)).put(PackageJson.FIELD_START, "node app.js --port 2080");
        asyncWriteFile(newData);
        // wait
        countDownLatch2.await(1, TimeUnit.MINUTES);
        // check events
        Map<String, List<PropertyChangeEvent>> allEvents = listener.getAllEvents();
        assertEquals(1, allEvents.size());
        List<PropertyChangeEvent> events = allEvents.get(PackageJson.PROP_SCRIPTS_START);
        assertNotNull(events);
        assertEquals(2, events.size());
        PropertyChangeEvent event = events.get(0);
        assertEquals(PackageJson.PROP_SCRIPTS_START, event.getPropertyName());
        assertNull(event.getOldValue());
        assertEquals("node server.js", event.getNewValue());
        event = events.get(1);
        assertEquals(PackageJson.PROP_SCRIPTS_START, event.getPropertyName());
        assertEquals("node server.js", event.getOldValue());
        assertEquals("node app.js --port 2080", event.getNewValue());
    }

    public void testFileChange() throws Exception {
        CountDownLatch countDownLatch1 = new CountDownLatch(1);
        PropertyChangeListenerImpl listener = new PropertyChangeListenerImpl();
        listener.setCountDownLatch(countDownLatch1);
        packageJson.addPropertyChangeListener(listener);
        asyncWriteFile(getData(true, true));
        // wait
        countDownLatch1.await(1, TimeUnit.MINUTES);
        // needed for FS to notice the change
        Thread.sleep(1000);
        // change name & start file
        CountDownLatch countDownLatch2 = new CountDownLatch(1);
        listener.setCountDownLatch(countDownLatch2);
        Map<String, Object> newData = getData(true, true);
        newData.put(PackageJson.FIELD_NAME, "YourProject");
        ((Map<String, Object>) newData.get(PackageJson.FIELD_SCRIPTS)).put(PackageJson.FIELD_START, "node app.js --port 2080");
        asyncWriteFile(newData);
        // wait
        countDownLatch2.await(1, TimeUnit.MINUTES);
        // needed for FS to notice the change
        Thread.sleep(1000);
        // change start file only
        CountDownLatch countDownLatch3 = new CountDownLatch(1);
        listener.setCountDownLatch(countDownLatch3);
        Map<String, Object> newerData = new HashMap<>(newData);
        ((Map<String, Object>) newerData.get(PackageJson.FIELD_SCRIPTS)).put(PackageJson.FIELD_START, "node app.js");
        asyncWriteFile(newerData);
        // wait
        countDownLatch3.await(1, TimeUnit.MINUTES);
        // check events
        Map<String, List<PropertyChangeEvent>> allEvents = listener.getAllEvents();
        assertEquals(2, allEvents.size());
        // name
        List<PropertyChangeEvent> events = allEvents.get(PackageJson.PROP_NAME);
        assertNotNull(events);
        assertEquals(2, events.size());
        PropertyChangeEvent event = events.get(0);
        assertEquals(PackageJson.PROP_NAME, event.getPropertyName());
        assertNull(event.getOldValue());
        assertEquals("MyProject", event.getNewValue());
        event = events.get(1);
        assertEquals(PackageJson.PROP_NAME, event.getPropertyName());
        assertEquals("MyProject", event.getOldValue());
        assertEquals("YourProject", event.getNewValue());
        // start file
        events = allEvents.get(PackageJson.PROP_SCRIPTS_START);
        assertNotNull(events);
        assertEquals(3, events.size());
        event = events.get(0);
        assertEquals(PackageJson.PROP_SCRIPTS_START, event.getPropertyName());
        assertNull(event.getOldValue());
        assertEquals("node server.js", event.getNewValue());
        event = events.get(1);
        assertEquals(PackageJson.PROP_SCRIPTS_START, event.getPropertyName());
        assertEquals("node server.js", event.getOldValue());
        assertEquals("node app.js --port 2080", event.getNewValue());
        event = events.get(2);
        assertEquals(PackageJson.PROP_SCRIPTS_START, event.getPropertyName());
        assertEquals("node app.js --port 2080", event.getOldValue());
        assertEquals("node app.js", event.getNewValue());
    }

    public void testWriteContent() throws Exception {
        writeFile(getData(true, true));
        Map<String, Object> content = packageJson.getContent();
        assertNotNull(content);
        final String oldName = (String) content.get(PackageJson.FIELD_NAME);
        assertNotNull(oldName);
        // needed for FS to notice the change
        Thread.sleep(1000);
        // listener
        CountDownLatch countDownLatch1 = new CountDownLatch(1);
        PropertyChangeListenerImpl listener = new PropertyChangeListenerImpl();
        listener.setCountDownLatch(countDownLatch1);
        packageJson.addPropertyChangeListener(listener);
        // change name
        String newName = "some-new-cool-name";
        packageJson.setContent(Collections.singletonList(PackageJson.FIELD_NAME), newName);
        // needed for FS to notice the change
        Thread.sleep(1000);
        // manual refresh
        refreshForFile(packageJson.getFile());
        // wait
        countDownLatch1.await(1, TimeUnit.MINUTES);
        // check events
        Map<String, List<PropertyChangeEvent>> allEvents = listener.getAllEvents();
        assertEquals(1, allEvents.size());
        // name
        List<PropertyChangeEvent> events = allEvents.get(PackageJson.PROP_NAME);
        assertNotNull(events);
        assertEquals(1, events.size());
        PropertyChangeEvent event = events.get(0);
        assertEquals(PackageJson.PROP_NAME, event.getPropertyName());
        assertEquals(oldName, event.getOldValue());
        assertEquals(newName, event.getNewValue());
    }

    public void testSetContentString() throws Exception {
        Map<String, Object> data = getData(true, true);
        writeFile(data);
        Map<String, Object> content = packageJson.getContent();
        assertNotNull(content);
        assertEquals("MyProject", content.get(PackageJson.FIELD_NAME));
        final String newName = "MyLibrary";
        packageJson.setContent(Collections.singletonList(PackageJson.FIELD_NAME), newName);
        content = packageJson.getContent();
        assertNotNull(content);
        assertEquals(JSONValue.toJSONString(content), newName, content.get(PackageJson.FIELD_NAME));
    }

    public void testSetContentNumber() throws Exception {
        Map<String, Object> data = getData(true, true);
        writeFile(data);
        Map<String, Object> content = packageJson.getContent();
        assertNotNull(content);
        assertEquals("MyProject", content.get(PackageJson.FIELD_NAME));
        final int newName1 = 150;
        packageJson.setContent(Collections.singletonList(PackageJson.FIELD_NAME), newName1);
        content = packageJson.getContent();
        assertNotNull(content);
        assertEquals(JSONValue.toJSONString(content), (long) newName1, content.get(PackageJson.FIELD_NAME));
        final String newName2 = "MyNewJsLib";
        packageJson.setContent(Collections.singletonList(PackageJson.FIELD_NAME), newName2);
        content = packageJson.getContent();
        assertNotNull(content);
        assertEquals(JSONValue.toJSONString(content), newName2, content.get(PackageJson.FIELD_NAME));
    }

    public void testSetContentObject() throws Exception {
        Map<String, Object> data = getData(true, true);
        writeFile(data);
        Map<String, Object> content = packageJson.getContent();
        assertNotNull(content);
        assertEquals("MyProject", content.get(PackageJson.FIELD_NAME));
        final Map<String, Object> newName = new LinkedHashMap<>();
        newName.put("simple", "Simple NewName");
        newName.put("complex", "Complex NewName");
        packageJson.setContent(Collections.singletonList(PackageJson.FIELD_NAME), newName);
        content = packageJson.getContent();
        assertNotNull(content);
        assertEquals(JSONValue.toJSONString(content), newName, content.get(PackageJson.FIELD_NAME));
    }

    public void testSetContentEscaped() throws Exception {
        Map<String, Object> data = getData(true, true);
        writeFile(data);
        Map<String, Object> content = packageJson.getContent();
        assertNotNull(content);
        assertEquals("MyProject", content.get(PackageJson.FIELD_NAME));
        final String newName = "My \" Library";
        packageJson.setContent(Collections.singletonList(PackageJson.FIELD_NAME), newName);
        content = packageJson.getContent();
        assertNotNull(content);
        assertEquals(JSONValue.toJSONString(content), newName, content.get(PackageJson.FIELD_NAME));
    }

    public void testSetContentSubField() throws Exception {
        Map<String, Object> data = getData(true, true);
        writeFile(data);
        Map<String, Object> content = packageJson.getContent();
        assertNotNull(content);
        assertEquals("node server.js", getValue(String.class, content, PackageJson.FIELD_SCRIPTS, PackageJson.FIELD_START));
        final String newStartScript = "node src/main.js 8080";
        packageJson.setContent(Arrays.asList(PackageJson.FIELD_SCRIPTS, PackageJson.FIELD_START), newStartScript);
        content = packageJson.getContent();
        assertNotNull(content);
        assertEquals(JSONValue.toJSONString(content), newStartScript, getValue(String.class, content, PackageJson.FIELD_SCRIPTS, PackageJson.FIELD_START));
    }

    public void testSetContentSameFieldNames() throws Exception {
        Map<String, Object> data = new LinkedHashMap<>();
        final String topLevelStart = "some dummy value";
        data.put(PackageJson.FIELD_START, topLevelStart);
        data.putAll(getData(true, true));
        writeFile(data);
        Map<String, Object> content = packageJson.getContent();
        assertNotNull(content);
        assertEquals(topLevelStart, data.get(PackageJson.FIELD_START));
        assertEquals("node server.js", getValue(String.class, content, PackageJson.FIELD_SCRIPTS, PackageJson.FIELD_START));
        final String newStartScript = "node src/main.js 8080";
        packageJson.setContent(Arrays.asList(PackageJson.FIELD_SCRIPTS, PackageJson.FIELD_START), newStartScript);
        content = packageJson.getContent();
        assertNotNull(content);
        assertEquals(JSONValue.toJSONString(content), topLevelStart, data.get(PackageJson.FIELD_START));
        assertEquals(JSONValue.toJSONString(content), newStartScript, getValue(String.class, content, PackageJson.FIELD_SCRIPTS, PackageJson.FIELD_START));
    }

    public void testSetContentSameFieldNames2() throws Exception {
        Map<String, Object> data = new LinkedHashMap<>();
        Map<String, Object> test = new LinkedHashMap<>();
        final String testName = "testname";
        test.put(PackageJson.FIELD_NAME, testName);
        data.put("test", test);
        data.put(PackageJson.FIELD_NAME, "oldname");
        writeFile(data);
        Map<String, Object> content = packageJson.getContent();
        assertNotNull(content);
        assertEquals("oldname", content.get(PackageJson.FIELD_NAME));
        assertEquals(testName, getValue(String.class, content, "test", PackageJson.FIELD_NAME));
        final String newName = "newname";
        packageJson.setContent(Collections.singletonList(PackageJson.FIELD_NAME), newName);
        content = packageJson.getContent();
        assertNotNull(content);
        assertEquals(newName, content.get(PackageJson.FIELD_NAME));
        assertEquals(testName, getValue(String.class, content, "test", PackageJson.FIELD_NAME));
    }

    public void testSetContentNewField() throws Exception {
        Map<String, Object> data = getData(true, false);
        writeFile(data);
        Map<String, Object> content = packageJson.getContent();
        assertNotNull(content);
        assertEquals("MyProject", content.get(PackageJson.FIELD_NAME));
        final String key = "env";
        final String value = "devel";
        assertNull(content.get(key));
        packageJson.setContent(Collections.singletonList(key), value);
        content = packageJson.getContent();
        assertNotNull(content);
        assertEquals(JSONValue.toJSONString(content), value, content.get(key));
    }

    public void testSetContentNewSubField() throws Exception {
        Map<String, Object> data = getData(true, true);
        writeFile(data);
        Map<String, Object> content = packageJson.getContent();
        assertNotNull(content);
        assertEquals("MyProject", content.get(PackageJson.FIELD_NAME));
        final String key = "executable";
        final String value = "yes";
        packageJson.setContent(Arrays.asList(PackageJson.FIELD_SCRIPTS, key), value);
        content = packageJson.getContent();
        assertNotNull(content);
        assertEquals(JSONValue.toJSONString(content), value, getValue(String.class, content, PackageJson.FIELD_SCRIPTS, key));
    }

    private File getFile() {
        return new File(FileUtil.toFile(directory), PackageJson.FILE_NAME);
    }

    private Map<String, Object> getData(boolean name, boolean startFile) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (name) {
            data.put(PackageJson.FIELD_NAME, "MyProject");
        }
        if (startFile) {
            Map<String, Object> scripts = new LinkedHashMap<>();
            scripts.put(PackageJson.FIELD_START, "node server.js");
            data.put(PackageJson.FIELD_SCRIPTS, scripts);
        }
        return data;
    }

    @CheckForNull
    private <T> T getValue(Class<T> valueType, Map<String, Object> data, String... fieldHierarchy) {
        Map<String, Object> subdata = data;
        for (int i = 0; i < fieldHierarchy.length; ++i) {
            String field = fieldHierarchy[i];
            if (i == fieldHierarchy.length - 1) {
                return valueType.cast(subdata.get(field));
            }
            subdata = (Map<String, Object>) subdata.get(field);
        }
        return null;
    }

    private void writeFile(Map<String, Object> data) throws IOException {
        File file = getFile();
        try (Writer out = new FileWriter(file)) {
            JSONObject.writeJSONString(data, out);
        }
        assertTrue(file.isFile());
        refreshForFile(file);
    }

    private void asyncWriteFile(Map<String, Object> data) {
        final Map<String, Object> synchronizedData = Collections.synchronizedMap(data);
        EXECUTORS.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    writeFile(synchronizedData);
                } catch (IOException ex) {
                    fail(ex.getMessage());
                }
            }
        });
    }

    private void refreshForFile(File file) {
        FileUtil.refreshFor(file.getParentFile());
    }

    //~ Inner classes

    private static final class PropertyChangeListenerImpl implements PropertyChangeListener {

        private final Map<String, List<PropertyChangeEvent>> allEvents = new HashMap<>();
        private volatile CountDownLatch countDownLatch;


        public void setCountDownLatch(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public synchronized void propertyChange(PropertyChangeEvent evt) {
            Assert.assertNotNull(evt);
            String propertyName = evt.getPropertyName();
            Assert.assertNotNull(propertyName);
            List<PropertyChangeEvent> events = allEvents.get(propertyName);
            if (events == null) {
                events = new ArrayList<>();
                allEvents.put(propertyName, events);
            }
            events.add(evt);
            countDownLatch.countDown();
        }

        public Map<String, List<PropertyChangeEvent>> getAllEvents() {
            return new HashMap<>(allEvents);
        }

    }

}
