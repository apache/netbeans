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
package org.netbeans.modules.diff;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.Difference;

import javax.swing.*;
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import java.io.StringReader;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.diff.builtin.provider.BuiltInDiffProvider;

/**
 * @author Maros Sandor
 */
public class DiffControllerTest extends NbTestCase {

    private DiffController controller;
    private DiffController enhancedController;

    public DiffControllerTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        MockServices.setServices(BuiltInDiffProvider.class);
        controller = DiffController.create(new Impl("name1", "title1", "text/plain", "content1\nsame\ndifferent1"), new Impl("name2", "title2", "text/plain", "content2\nsame\ndifferent2"));
        final boolean[] finished = new boolean[2];
        controller.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                controller.removePropertyChangeListener(this);
                finished[0] = true;
            }
        });

        enhancedController = DiffController.createEnhanced(new Impl("name1", "title1", "text/plain", "content1\nsame\ndifferent1"), new Impl("name2", "title2", "text/plain", "content2\nsame\ndifferent2"));
        enhancedController.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                enhancedController.removePropertyChangeListener(this);
                finished[1] = true;
            }
        });
        for (int i = 0; i < 10 && !(finished[0] && finished[1]); ++i) {
            Thread.sleep(1000);
        }
    }

    public void testCurrentDifference() throws Exception {
        int dc = controller.getDifferenceCount();
        assertEquals("Wrong number of differences", 2, dc);
        dc = enhancedController.getDifferenceCount();
        assertEquals("Wrong number of differences", 2, dc);
    }

    public void testDifferenceIndex() throws Exception {
        int dc = controller.getDifferenceCount();
        int di = controller.getDifferenceIndex();
        assertTrue("Wrong difference index", di == -1 || di >= 0 && di < dc);
        dc = enhancedController.getDifferenceCount();
        di = enhancedController.getDifferenceIndex();
        assertTrue("Wrong difference index", di == -1 || di >= 0 && di < dc);
    }

    public void testComponent() throws Exception {
        JComponent c = controller.getJComponent();
        assertNotNull("Not a JComponent", c);
        c = enhancedController.getJComponent();
        assertNotNull("Not a JComponent", c);
    }

    /**
     * Private implementation to be returned by the static methods.
     */
    private static class Impl extends StreamSource {

        private String name;
        private String title;
        private String MIMEType;
        private String buffer;

        Impl(String name, String title, String MIMEType, String str) {
            this.name = name;
            this.title = title;
            this.MIMEType = MIMEType;
            buffer = str;
        }

        public String getName() {
            return name;
        }

        public String getTitle() {
            return title;
        }

        public String getMIMEType() {
            return MIMEType;
        }

        public Reader createReader() throws IOException {
            return new StringReader(buffer);
        }

        public Writer createWriter(Difference[] conflicts) throws IOException {
            return null;
        }
    }


}
