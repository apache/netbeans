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
package org.netbeans.modules.acp.ui;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import junit.framework.TestCase;
import org.netbeans.api.diff.DiffController.DiffPane;
import org.netbeans.api.diff.DiffController.LocationType;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.acp.api.AcpToolCall;
import org.netbeans.spi.diff.DiffControllerImpl;
import org.netbeans.spi.diff.DiffControllerProvider;
import org.openide.util.test.MockLookup;
import org.openide.windows.TopComponent;

public class AcpDiffViewerTest extends TestCase {

    public void testOpensReadOnlySourcesAndFocusesFirstDifference() throws Exception {
        TrackingProvider provider = new TrackingProvider();
        MockLookup.setInstances(provider);
        try {
            AcpDiffViewer.showDiff(new AcpToolCall.DiffContent("C:\\project\\Example.java", null, "class Example {}"));
            SwingUtilities.invokeAndWait(() -> {
                try {
                    assertNotNull(provider.base);
                    assertEquals("text/x-java", provider.base.getMIMEType());
                    assertEquals("C:\\project\\Example.java", provider.modified.getName());
                    assertEquals("", read(provider.base));
                    assertEquals("class Example {}", read(provider.modified));
                    assertNull(provider.modified.createWriter(null));
                    assertEquals(DiffPane.Modified, provider.pane);
                    assertEquals(LocationType.DifferenceIndex, provider.locationType);
                    assertEquals(0, provider.location);
                    TopComponent tab = (TopComponent) provider.panel.getParent();
                    assertEquals(Bundle.TTL_DiffTab("Example.java"), tab.getDisplayName());
                    tab.close();
                } catch (IOException ex) {
                    throw new AssertionError(ex);
                }
            });
        } finally {
            MockLookup.setInstances();
        }
    }

    private static String read(StreamSource source) throws IOException {
        try (Reader reader = source.createReader()) {
            StringWriter content = new StringWriter();
            reader.transferTo(content);
            return content.toString();
        }
    }

    private static final class TrackingProvider extends DiffControllerProvider {

        private StreamSource base;
        private StreamSource modified;
        private JPanel panel;
        private DiffPane pane;
        private LocationType locationType;
        private int location = -1;

        @Override
        public DiffControllerImpl createDiffController(StreamSource from, StreamSource to) {
            assertTrue(SwingUtilities.isEventDispatchThread());
            base = from;
            modified = to;
            panel = new JPanel();
            return new DiffControllerImpl() {
                @Override
                public JComponent getJComponent() {
                    return panel;
                }

                @Override
                public int getDifferenceCount() {
                    return 1;
                }

                @Override
                public void setLocation(DiffPane diffPane, LocationType type, int index) {
                    pane = diffPane;
                    locationType = type;
                    location = index;
                }
            };
        }
    }
}
