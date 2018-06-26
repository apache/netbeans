/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.bower.file;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
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
import org.junit.Assert;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class BowerJsonTest extends NbTestCase {

    private static final ExecutorService EXECUTORS = Executors.newCachedThreadPool();

    private FileObject directory;
    private BowerJson bowerJson;


    public BowerJsonTest(String name) {
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
        bowerJson = new BowerJson(directory);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        bowerJson.cleanup();
    }

    public void testNoBowerJson() {
        assertFalse(bowerJson.exists());
        assertEquals(getFile().getAbsolutePath(), bowerJson.getPath());
    }

    public void testBowerJson() throws Exception {
        writeFile(getData(true, false));
        assertTrue(bowerJson.exists());
        assertEquals(getFile().getAbsolutePath(), bowerJson.getPath());
    }

    public void testDependencies() throws Exception {
        BowerJson invalidBowerJson = new BowerJson(FileUtil.toFileObject(getDataDir()), "invalid-bower.json");
        assertTrue(invalidBowerJson.getFile().getAbsolutePath(), invalidBowerJson.exists());
        BowerJson.BowerDependencies dependencies = invalidBowerJson.getDependencies();
        assertEquals(14, dependencies.getCount());
        assertEquals(7, dependencies.dependencies.size());
        assertEquals(7, dependencies.devDependencies.size());
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
    }

    public void testDependenciesChange() throws Exception {
        CountDownLatch countDownLatch1 = new CountDownLatch(1);
        PropertyChangeListenerImpl listener = new PropertyChangeListenerImpl();
        listener.setCountDownLatch(countDownLatch1);
        bowerJson.addPropertyChangeListener(listener);
        Map<String, Object> data = getData(true, false);
        asyncWriteFile(data);
        // wait
        countDownLatch1.await(1, TimeUnit.MINUTES);
        // needed for FS to notice the change
        Thread.sleep(1000);
        // change content
        CountDownLatch countDownLatch2 = new CountDownLatch(1);
        listener.setCountDownLatch(countDownLatch2);
        Map<String, Object> newData = getData(true, true);
        newData.put(BowerJson.FIELD_DEPENDENCIES, Collections.singletonMap("http://example.com/script.js", "1.0"));
        asyncWriteFile(newData);
        // wait
        countDownLatch2.await(1, TimeUnit.MINUTES);
        // needed for FS to notice the change
        Thread.sleep(1000);
        // check events
        Map<String, List<PropertyChangeEvent>> allEvents = listener.getAllEvents();
        assertEquals(2, allEvents.size());
        // dependencies
        List<PropertyChangeEvent> depEvents = allEvents.get(BowerJson.PROP_DEPENDENCIES);
        assertNotNull(depEvents);
        assertEquals(2, depEvents.size());
        PropertyChangeEvent event = depEvents.get(0);
        assertEquals(BowerJson.PROP_DEPENDENCIES, event.getPropertyName());
        assertNull(event.getOldValue());
        assertEquals(data.get(BowerJson.FIELD_DEPENDENCIES), event.getNewValue());
        event = depEvents.get(1);
        assertEquals(BowerJson.PROP_DEPENDENCIES, event.getPropertyName());
        assertEquals(data.get(BowerJson.FIELD_DEPENDENCIES), event.getOldValue());
        assertEquals(newData.get(BowerJson.FIELD_DEPENDENCIES), event.getNewValue());
        // devDependencies
        List<PropertyChangeEvent> devDepEvents = allEvents.get(BowerJson.PROP_DEV_DEPENDENCIES);
        assertNotNull(devDepEvents);
        assertEquals(1, devDepEvents.size());
        event = devDepEvents.get(0);
        assertEquals(BowerJson.PROP_DEV_DEPENDENCIES, event.getPropertyName());
        assertNull(event.getOldValue());
        assertEquals(newData.get(BowerJson.FIELD_DEV_DEPENDENCIES), event.getNewValue());
    }

    private File getFile() {
        return new File(FileUtil.toFile(directory), BowerJson.FILE_NAME);
    }

    private Map<String, Object> getData(boolean dependencies, boolean devDependencies) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (dependencies) {
            Map<String, Object> deps = new LinkedHashMap<>();
            deps.put("jquery", "*");
            data.put(BowerJson.FIELD_DEPENDENCIES, deps);
        }
        if (devDependencies) {
            Map<String, Object> devDeps = new LinkedHashMap<>();
            devDeps.put("jasmine", "*");
            data.put(BowerJson.FIELD_DEV_DEPENDENCIES, devDeps);
        }
        return data;
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
