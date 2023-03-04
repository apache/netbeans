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
package org.openide.io;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.io.Hyperlink;
import org.netbeans.api.io.OutputColor;
import org.netbeans.api.io.ShowOperation;
import org.netbeans.spi.io.InputOutputProvider;
import org.openide.util.Lookup;
import org.openide.windows.IOColorPrint;
import org.openide.windows.IOColors;
import org.openide.windows.IOContainer;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

public class BridgingIOProviderTest {

    public BridgingIOProviderTest() {
    }

    @Test
    public void bridgeToPrint() {
        MockIOP mock = new MockIOP();
        IOProvider provider = BridgingIOProvider.create(mock);
        InputOutput io = provider.getIO("test", true);
        StringBuilder sb = mock.assertBuilder();
        final OutputWriter stdOut = io.getOut();
        stdOut.println("Hello there!");
        assertEquals("Hello there!\n", sb.toString());
    }

    @Test
    public void bridgeToColorLessProvider() throws IOException {
        MockIOP mock = new MockIOP();
        IOProvider provider = BridgingIOProvider.create(mock);
        InputOutput io = provider.getIO("test", true);
        assertFalse("No colors", IOColors.isSupported(io));
        assertTrue("Color print supported!?", IOColorPrint.isSupported(io));
        IOColorPrint.print(io, "Red!", Color.RED);
        StringBuilder sb = mock.assertBuilder();
        assertEquals("Red!", sb.toString());
    }

    @Test
    public void bridgeToNullColor() throws IOException {
        MockIOP mock = new MockIOP();
        IOProvider provider = BridgingIOProvider.create(mock);
        InputOutput io = provider.getIO("test", true);
        assertFalse("No colors", IOColors.isSupported(io));
        assertTrue("Color print supported!?", IOColorPrint.isSupported(io));
        IOColorPrint.print(io, "Null!", null);
        StringBuilder sb = mock.assertBuilder();
        assertEquals("Null!", sb.toString());
    }

    @Test
    public void nullIOContainer() throws IOException {
        MockIOP mock = new MockIOP();
        IOProvider provider = BridgingIOProvider.create(mock);
        Action testAction = new AbstractAction("test") {
            @Override public void actionPerformed(ActionEvent e) {}
        };
        InputOutput io = provider.getIO("test", false, new Action[] {testAction}, null);
        assertSame(testAction, mock.last.lookup.lookup(Action.class));
    }

    @Test
    public void hasIOContainer() throws IOException {
        MockIOP mock = new MockIOP();
        IOProvider provider = BridgingIOProvider.create(mock);
        Action testAction = new AbstractAction("test") {
            @Override public void actionPerformed(ActionEvent e) {}
        };
        IOContainer ioContainer = IOContainer.create(new MockIOProvider());
        InputOutput io = provider.getIO("test", false, new Action[] {testAction}, ioContainer);
        assertSame(testAction, mock.last.lookup.lookup(Action.class));
        assertSame(ioContainer, mock.last.lookup.lookup(IOContainer.class));
    }

    private static class MockBuilder {
        final StringBuilder io = new StringBuilder();
        final PrintWriter out = new PrintWriter(new AppendWriter(io));
        final Lookup lookup;
        public MockBuilder(Lookup lookup) {
            this.lookup = lookup;
        }
    }

    private static final class MockIOP implements InputOutputProvider<MockBuilder, PrintWriter, Void, Void> {
        private MockBuilder last;

        public StringBuilder assertBuilder() {
            try {
                assertNotNull(last);
                return last.io;
            } finally {
                last = null;
            }
        }

        @Override
        public String getId() {
            return "mock";
        }

        @Override
        public MockBuilder getIO(String name, boolean newIO, Lookup lookup) {
            assertNull(last);
            last = new MockBuilder(lookup);
            return last;
        }

        @Override
        public Reader getIn(MockBuilder io) {
            return new StringReader(io.toString());
        }

        @Override
        public PrintWriter getOut(MockBuilder io) {
            return io.out;
        }

        @Override
        public PrintWriter getErr(MockBuilder io) {
            return io.out;
        }

        @Override
        public void print(MockBuilder io, PrintWriter writer, String text, Hyperlink link, OutputColor color, boolean printLineEnd) {
            if (printLineEnd) {
                writer.println(text);
            } else {
                writer.print(text);
            }
        }

        @Override
        public Lookup getIOLookup(MockBuilder io) {
            return Lookup.EMPTY;
        }

        @Override
        public void resetIO(MockBuilder io) {
        }

        @Override
        public void showIO(MockBuilder io, Set<ShowOperation> operations) {
        }

        @Override
        public void closeIO(MockBuilder io) {
        }

        @Override
        public boolean isIOClosed(MockBuilder io) {
            return false;
        }

        @Override
        public Void getCurrentPosition(MockBuilder io, PrintWriter writer) {
            return null;
        }

        @Override
        public void scrollTo(MockBuilder io, PrintWriter writer, Void position) {
        }

        @Override
        public Void startFold(MockBuilder io, PrintWriter writer, boolean expanded) {
            return null;
        }

        @Override
        public void endFold(MockBuilder io, PrintWriter writer, Void fold) {
        }

        @Override
        public void setFoldExpanded(MockBuilder io, PrintWriter writer, Void fold, boolean expanded) {
        }

        @Override
        public String getIODescription(MockBuilder io) {
            return "mock";
        }

        @Override
        public void setIODescription(MockBuilder io, String description) {
        }
    }

    private static final class AppendWriter extends Writer {
        private final StringBuilder io;

        private AppendWriter(StringBuilder io) {
            this.io = io;
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            io.append(cbuf, off, len);
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
        }

    }

    private static class MockIOProvider implements IOContainer.Provider {

        @Override
        public void open() {}

        @Override
        public void requestActive() {}

        @Override
        public void requestVisible() {}

        @Override
        public boolean isActivated() {
            return false;
        }

        @Override
        public void add(JComponent comp, IOContainer.CallBacks cb) {}

        @Override
        public void remove(JComponent comp) {}

        @Override
        public void select(JComponent comp) {}

        @Override
        public JComponent getSelected() {
            return null;
        }

        @Override
        public void setTitle(JComponent comp, String name) {}

        @Override
        public void setToolTipText(JComponent comp, String text) {}

        @Override
        public void setIcon(JComponent comp, Icon icon) {}

        @Override
        public void setToolbarActions(JComponent comp, Action[] toolbarActions) {}

        @Override
        public boolean isCloseable(JComponent comp) {
            return false;
        }
    }
}
