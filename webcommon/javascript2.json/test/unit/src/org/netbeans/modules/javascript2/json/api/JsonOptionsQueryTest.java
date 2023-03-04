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
package org.netbeans.modules.javascript2.json.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.javascript2.json.spi.JsonOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;
import org.openide.util.test.MockPropertyChangeListener;

/**
 *
 * @author Tomas Zezula
 */
public final class JsonOptionsQueryTest extends NbTestCase {

    FileObject wd;

    public JsonOptionsQueryTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        assertNotNull(wd);
    }

    public void testNoImplementation() {
        MockLookup.setInstances();
        final JsonOptionsQuery.Result res = JsonOptionsQuery.getOptions(wd);
        assertNotNull(res);
        assertFalse(res.isCommentSupported());
    }

    public void testSingleImplementation() {
        MockLookup.setInstances(new JsonOptsImpl(Boolean.TRUE));
        final JsonOptionsQuery.Result res = JsonOptionsQuery.getOptions(wd);
        assertNotNull(res);
        assertTrue(res.isCommentSupported());
    }

    public void testMultipleImplementations1() {
        MockLookup.setInstances(
                new JsonOptsImpl(null),
                new JsonOptsImpl(Boolean.TRUE));
        final JsonOptionsQuery.Result res = JsonOptionsQuery.getOptions(wd);
        assertNotNull(res);
        assertTrue(res.isCommentSupported());
    }

    public void testMultipleImplementations2() {
        MockLookup.setInstances(
                new JsonOptsImpl(Boolean.FALSE),
                new JsonOptsImpl(Boolean.TRUE));
        final JsonOptionsQuery.Result res = JsonOptionsQuery.getOptions(wd);
        assertNotNull(res);
        assertFalse(res.isCommentSupported());
    }

    public void testChanges() {
        final JsonOptsImpl impl1 = new JsonOptsImpl(Boolean.FALSE);
        final JsonOptsImpl impl2 = new JsonOptsImpl(Boolean.TRUE);
        MockLookup.setInstances(
                impl1,
                impl2);
        final JsonOptionsQuery.Result res = JsonOptionsQuery.getOptions(wd);
        assertNotNull(res);
        assertFalse(res.isCommentSupported());
        final MockPropertyChangeListener l = new MockPropertyChangeListener(JsonOptionsQuery.Result.PROP_COMMENT_SUPPORTED);
        res.addPropertyChangeListener(l);
        impl1.setComments(null);
        l.assertEventCount(1);
        assertTrue(res.isCommentSupported());
    }

    private static final class JsonOptsImpl implements JsonOptionsQueryImplementation {
        private final PropertyChangeSupport listeners;
        private final ResImpl res;
        private Boolean comments;

        JsonOptsImpl(final Boolean comments) {
            this.comments = comments;
            this.listeners = new PropertyChangeSupport(this);
            this.res = new ResImpl();
        }

        void setComments(Boolean comments) {
            this.comments = comments;
            this.listeners.firePropertyChange(Result.PROP_COMMENT_SUPPORTED, null, null);
        }

        @Override
        public Result getOptions(FileObject file) {
            return res;
        }

        private class ResImpl implements Result {

            @Override
            public Boolean isCommentSupported() {
                return comments;
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {
                listeners.addPropertyChangeListener(listener);
            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {
                listeners.removePropertyChangeListener(listener);
            }
        }
    }

}
