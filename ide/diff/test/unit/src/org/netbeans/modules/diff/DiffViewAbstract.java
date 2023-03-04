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
import java.io.*;

import org.netbeans.junit.*;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.diff.builtin.provider.BuiltInDiffProvider;

/**
 *
 * @author Martin Entlicher
 */
public abstract class DiffViewAbstract extends NbTestCase {
    /** the DiffView to work on */
    private DiffView dv;

    public DiffViewAbstract(String name) {
        super(name);
    }

    protected abstract DiffView createDiffView(StreamSource ss1, StreamSource ss2) throws IOException;
    
    protected void setUp() throws Exception {
        MockServices.setServices(BuiltInDiffProvider.class);
        dv = createDiffView(new Impl("name1", "title1", "text/plain", "content1\nsame\ndifferent1"), new Impl("name2", "title2", "text/plain", "content2\nsame\ndifferent2"));
        final boolean[] finished = new boolean[1];
        dv.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                dv.removePropertyChangeListener(this);
                finished[0] = true;
            }
        });
        for (int i = 0; i < 10 && !finished[0]; ++i) {
            Thread.sleep(1000);
        }
    }
    
    public void testCanDiffConsistent() throws Exception {
        if (dv.canSetCurrentDifference()) {
            dv.setCurrentDifference(0);
        } else {
            try {
                dv.setCurrentDifference(0);
                fail("Should throw UnsupportedOperationException");
            } catch (UnsupportedOperationException uoex) {
                // OK
            }
        }
    }
    
    public void testCurrentDifference() throws Exception {
        if (dv.canSetCurrentDifference()) {
            int dc = dv.getDifferenceCount();
            assertEquals("Just one difference", 2, dc);
            dv.setCurrentDifference(1);
            assertEquals("Test current difference", 1, dv.getCurrentDifference());
//            try {
//                dv.setCurrentDifference(10);
//                fail("Should report an exception.");
//            } catch (IllegalArgumentException ioex) {
//                // OK
//            }
        }
    }
    
    /**
     * Private implementation to be returned by the static methods.
     */
    private static class Impl extends StreamSource {

        private String name;
        private String title;
        private String MIMEType;
        private Reader r;
        private String buffer;
        private Writer w;

        Impl(String name, String title, String MIMEType, String str) {
            this.name = name;
            this.title = title;
            this.MIMEType = MIMEType;
            this.w = null;
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
