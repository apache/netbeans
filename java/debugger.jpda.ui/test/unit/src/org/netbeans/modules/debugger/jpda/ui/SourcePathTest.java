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
package org.netbeans.modules.debugger.jpda.ui;

import com.sun.jdi.AbsentInformationException;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.api.debugger.jpda.MonitorInfo;
import org.netbeans.api.debugger.jpda.This;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;

public final class SourcePathTest extends NbTestCase {
    private MockSourcePathProvider sourcePathProvider;
    private MockSourcePath sourcePath;

    public SourcePathTest(String name) {
        super(name);
    }

    private static final class MockSourcePathProvider extends SourcePathProvider {
        private final Map<String, Object> values;

        private MockSourcePathProvider(Map<String, Object> values) {
            this.values = values;
        }

        @Override
        public String getRelativePath(String url, char directorySeparator, boolean includeExtension) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getURL(String relativePath, boolean global) {
            assertTrue("Only called with global flag on", global);
            return (String) values.get("getURL:"+relativePath);
        }

        @Override
        public String[] getSourceRoots() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSourceRoots(String[] sourceRoots) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String[] getOriginalSourceRoots() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            throw new UnsupportedOperationException();
        }

    }

    private static final class MockSourcePath extends SourcePath {

        private int lineNumber;
        private String url;


        public MockSourcePath(ContextProvider contextProvider) {
            super(contextProvider);
        }

        @Override
        protected void handleShowSource(String url, int lineNumber, Runnable onFailure) {
            assertNotNull("Url cannot be null", url);
            assertNull("This is the first handleShowSource call", this.url);
            this.url = url;
            this.lineNumber = lineNumber;
        }


    }

    private static final class MockCallStackFrame implements CallStackFrame {
        private final Map<String, Object> values;

        MockCallStackFrame(Map<String,Object> values) {
            this.values = values;
        }

        @Override
        public int getLineNumber(String struts) {
            return (Integer)values.get("getLineNumber:" + struts);
        }

        @Override
        public int getFrameDepth() {
            throw new UnsupportedOperationException();
        }

        @Override
        public EditorContext.Operation getCurrentOperation(String struts) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getMethodName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getClassName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getDefaultStratum() {
            return (String)values.get("getDefaultStratum");
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> getAvailableStrata() {
            return (List<String>)values.get("getAvailableStrata");
        }

        @Override
        public String getSourceName(String struts) throws AbsentInformationException {
            return (String) values.get("getSourceName:" + struts);
        }

        @Override
        public String getSourcePath(String stratum) throws AbsentInformationException {
            return (String) values.get("getSourcePath:" + stratum);
        }

        @Override
        public LocalVariable[] getLocalVariables() throws AbsentInformationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public This getThisVariable() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void makeCurrent() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isObsolete() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void popFrame() {
            throw new UnsupportedOperationException();
        }

        @Override
        public JPDAThread getThread() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<MonitorInfo> getOwnedMonitors() {
            throw new UnsupportedOperationException();
        }

    }

    private MockSourcePath sourcePath() {
        if (sourcePath == null) {
            ContextProvider cp = new ContextProvider() {
                @Override
                @SuppressWarnings("unchecked")
                public <T> List<? extends T> lookup(String folder, Class<T> service) {
                    if (service == SourcePathProvider.class) {
                        assertNotNull("sourcePathProvider must be set", sourcePathProvider);
                        return (List<T>) List.of(sourcePathProvider);
                    }
                    return List.of();
                }

                @Override
                public <T> T lookupFirst(String folder, Class<T> service) {
                    return lookup(folder, service).stream().findFirst().orElse(null);
                }
            };
            sourcePath = new MockSourcePath(cp);
        }
        return sourcePath;
    }

    public void testShowSourceResolution1() {
        sourcePathProvider = new MockSourcePathProvider(Map.of(
                "getURL:/mydir/test", "file:///mydir/test"
        ));
        var csf = new MockCallStackFrame(Map.of(
                "getDefaultStratum", "Java",
                "getAvailableStrata", List.of("Java"),
                "getSourceName:null", "Dummy.java",
                "getSourcePath:null", "/mydir/test",
                "getLineNumber:null", 33
        ));
        sourcePath().showSource(csf, null);
        assertEquals(sourcePath.url, "file:///mydir/test");
        assertEquals(sourcePath.lineNumber, 33);
    }

}
