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
package org.openide.windows;

import java.io.Reader;
import static org.junit.Assert.*;
import org.junit.Test;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author jhavlin
 */
public class IOFoldingTest {

    @Test
    public void testStartFold() {
        InputOutputWithFolding io = new InputOutputWithFolding();
        FoldHandle fold1 = IOFolding.startFold(io, true);
        assertEquals(1, io.currentLevel);
        FoldHandle fold2 = fold1.startFold(true);
        assertEquals(2, io.currentLevel);
        fold2.finish();
        assertEquals(1, io.currentLevel);
        fold1.finish();
        assertEquals(0, io.currentLevel);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testStartFoldedUnsupported() {
        FoldHandle f = IOFolding.startFold(new DummyInputOutput(), true);
    }

    @Test(expected = IllegalStateException.class)
    public void testStartFoldIllegal() {
        InputOutputWithFolding io = new InputOutputWithFolding();
        FoldHandle f1 = IOFolding.startFold(io, true);
        FoldHandle f2 = IOFolding.startFold(io, true);
    }

    @Test(expected = IllegalStateException.class)
    public void testFinishIllegal() {
        InputOutputWithFolding io = new InputOutputWithFolding();
        FoldHandle f1 = IOFolding.startFold(io, true);
        FoldHandle f2 = f1.startFold(true);
        f1.finish();
    }

    @Test
    public void testIsSupported() {
        assertTrue(IOFolding.isSupported(new InputOutputWithFolding()));
    }

    @Test
    public void testIsSupportedUnsupported() {
        assertFalse(IOFolding.isSupported(new DummyInputOutput()));
    }

    @Test
    public void testIsFinished() {
        InputOutputWithFolding io = new InputOutputWithFolding();
        FoldHandle fold1 = IOFolding.startFold(io, true);
        assertFalse(fold1.isFinished());
        FoldHandle fold2 = fold1.startFold(true);
        assertFalse(fold2.isFinished());
        fold2.finish();
        assertTrue(fold2.isFinished());
        fold1.finish();
        assertTrue(fold1.isFinished());
    }

    @Test
    public void testSilentFinish() {
        InputOutputWithFolding io = new InputOutputWithFolding();
        FoldHandle fold1 = IOFolding.startFold(io, true);
        FoldHandle fold2 = fold1.startFold(true);
        fold1.silentFinish();
        assertTrue(fold2.isFinished());
        fold1.silentFinish(); // no exception when calling again
        fold1.silentFinish(); // still no exception
    }

    @Test
    public void testSilentFoldStart() {
        InputOutputWithFolding io = new InputOutputWithFolding();
        FoldHandle fold1 = IOFolding.startFold(io, true);
        FoldHandle fold2 = fold1.startFold(true);
        FoldHandle fold3 = fold1.silentStartFold(true);
        assertTrue(fold2.isFinished());
        assertNotNull(fold3);
        fold1.silentFinish();
        assertTrue(fold1.isFinished());
        FoldHandle fold4 = fold1.silentStartFold(true);
        assertNull(fold4);
    }

    /**
     * Dummy InputOutput that supports folding.
     */
    private static class InputOutputWithFolding extends DummyInputOutput
            implements Lookup.Provider {

        private final Lookup lookup = Lookups.fixed(new DummyIOFolding());

        private DummyIOFolding.DummyFoldHandleDef currentFold = null;
        private int currentLevel = 0;

        @Override
        public Lookup getLookup() {
            return lookup;
        }

        private class DummyIOFolding extends IOFolding {

            @Override
            protected FoldHandleDefinition startFold(boolean expanded) {
                if (currentFold != null) {
                    throw new IllegalStateException("Last fold not finished");
                } else {
                    currentFold = new DummyFoldHandleDef(null);
                    currentLevel++;
                    return currentFold;
                }
            }

            private class DummyFoldHandleDef extends FoldHandleDefinition {

                private DummyFoldHandleDef nested = null;
                private final DummyFoldHandleDef parent;
                private boolean finished = false;

                public DummyFoldHandleDef(DummyFoldHandleDef parent) {
                    this.parent = parent;
                }

                @Override
                public void finish() {
                    if (nested != null) {
                        throw new IllegalStateException("A nested fold exists.");
                    } else if (parent == null) {
                        currentFold = null;
                    } else {
                        parent.nested = null;
                    }
                    currentLevel--;
                    finished = true;
                }

                @Override
                public FoldHandleDefinition startFold(boolean expanded) {
                    if (finished) {
                        throw new IllegalStateException("Already finished.");
                    } else if (nested != null) {
                        throw new IllegalStateException("Last fold not finished.");
                    } else {
                        nested = new DummyFoldHandleDef(this);
                        currentLevel++;
                        return nested;
                    }
                }

                @Override
                public void setExpanded(boolean expanded) {
                }
            }
        }
    }

    /**
     * Dummy implementation of InputOutput.
     */
    private static class DummyInputOutput implements InputOutput {

        @Override
        public OutputWriter getOut() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Reader getIn() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public OutputWriter getErr() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void closeInputOutput() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isClosed() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setOutputVisible(boolean value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setErrVisible(boolean value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setInputVisible(boolean value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void select() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isErrSeparated() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setErrSeparated(boolean value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isFocusTaken() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setFocusTaken(boolean value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        @Deprecated
        public Reader flushReader() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
