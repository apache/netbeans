/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.diff.builtin;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.MessageFormat;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.diff.builtin.provider.BuiltInDiffProvider;
import org.netbeans.modules.diff.builtin.visualizer.editable.EditableDiffView;
import org.netbeans.spi.diff.DiffControllerImpl;


/**
 * Tests for new UI in the diff view
 * @author ondra
 */
public class DefaultDiffControllerProviderTest extends NbTestCase {

    private DiffControllerImpl simple;
    private DiffControllerImpl enhanced;

    public DefaultDiffControllerProviderTest (String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        MockServices.setServices(BuiltInDiffProvider.class);
        super.setUp();
        final boolean[] finished = new boolean[2];
        simple = new DefaultDiffControllerProvider().createDiffController(new Impl(new File(getDataDir(), "enhancedview/file1")), new Impl(new File(getDataDir(), "enhancedview/file2")));
        assertTrue(simple instanceof EditableDiffView);
        simple.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                simple.removePropertyChangeListener(this);
                finished[0] = true;
            }
        });

        enhanced = new DefaultDiffControllerProvider().createEnhancedDiffController(new Impl(new File(getDataDir(), "enhancedview/file1")), new Impl(new File(getDataDir(), "enhancedview/file2")));
        assert (enhanced instanceof EditableDiffView);
        enhanced.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                enhanced.removePropertyChangeListener(this);
                finished[1] = true;
            }
        });
        for (int i = 0; i < 10 && !(finished[0] && finished[1]); ++i) {
            Thread.sleep(1000);
        }
    }

    public void testComponents () {
        JComponent c = simple.getJComponent();
        assertFalse(c == null);
        // component should be an old-style jsplitpane
        assertTrue(findSplitPane(c) instanceof JSplitPane);

        c = enhanced.getJComponent();
        assertFalse(c == null);
        c = findTabbedPane(c);
        // component should be new jtabbedpane
        assertTrue(c instanceof JTabbedPane);
        // and it should contain two tabs - a graphical and a textual view
        assertEquals(2, c.getComponentCount());
    }

    public void testDifferenceCount () throws Exception {
        int dc = simple.getDifferenceCount();
        assertEquals(3, dc);

        // as default, the graphical view is displayed and the diff count is the same as in the old-style diff view
        dc = enhanced.getDifferenceCount();
        assertEquals(3, dc);
        JTabbedPane c = findTabbedPane(enhanced.getJComponent());

        // switching to the textual view
        final boolean[] finished = new boolean[1];
        enhanced.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (DiffController.PROP_DIFFERENCES.equals(evt.getPropertyName())) {
                    finished[0] = true;
                }
            }
        });
        c.setSelectedIndex(1);
        dc = enhanced.getDifferenceCount();
        // zero differences for the textual diff view
        // it shows no differences, displays the diff as a whole
        assertEquals(0, dc);
        // and it should fire an event about differences being changed so clients can refresh navigation button etc.
        for (int i = 0; i < 5 && !finished[0]; ++i) {
            Thread.sleep(1000);
        }
        assertTrue(finished[0]);
    }

    public void testTextualDiffContent () throws Exception {
        File diffFile = new File(getDataDir(), "enhancedview/diff");
        String goldenText = getFileContents(diffFile);
        goldenText = MessageFormat.format(goldenText, new Object[] {"a/", "b/"});

        final JTabbedPane tabbedPane = findTabbedPane(enhanced.getJComponent());
        JPanel p = (JPanel) tabbedPane.getComponentAt(1);
        tabbedPane.setSelectedIndex(1);
        JEditorPane pane = findEditorPane(p);
        assertFalse(pane == null);
        String text = pane.getText();
        for (int i = 0; i < 100; ++i) {
            if (!text.isEmpty()) {
                break;
            }
            Thread.sleep(100);
            text = pane.getText();
        }
        assertEquals(goldenText, text);
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run () {
                tabbedPane.setSelectedIndex(0);
            }
        });
    }

    private String getFileContents (File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }
        return sb.toString();
    }

    private static JEditorPane findEditorPane (Container c) {
        JEditorPane pane = null;
        for (Component comp : c.getComponents()) {
            if (comp instanceof JEditorPane) {
                pane = (JEditorPane) comp;
                break;
            } else if (comp instanceof Container) {
                pane = findEditorPane((Container) comp);
                if (pane != null) {
                    break;
                }
            }
        }
        return pane;
    }

    private static JTabbedPane findTabbedPane (JComponent component) {
        JTabbedPane pane = null;
        if (component instanceof JTabbedPane) {
            pane = (JTabbedPane) component;
        } else {
            for (Component c : component.getComponents()) {
                if (c instanceof JComponent) {
                    pane = findTabbedPane((JComponent) c);
                    if (pane != null) {
                        break;
                    }
                }
            }
        }
        return pane;
    }

    private static JSplitPane findSplitPane (JComponent component) {
        JSplitPane pane = null;
        if (component instanceof JSplitPane) {
            pane = (JSplitPane) component;
        } else {
            for (Component c : component.getComponents()) {
                if (c instanceof JComponent) {
                    pane = findSplitPane((JComponent) c);
                    if (pane != null) {
                        break;
                    }
                }
            }
        }
        return pane;
    }

    /**
     * Private implementation to be returned by the static methods.
     */
    private static class Impl extends StreamSource {

        private final File file;

        Impl(File file) {
            this.file = file;
        }

        @Override
        public String getName() {
            return file.getName();
        }

        @Override
        public String getTitle() {
            return file.getAbsolutePath();
        }

        @Override
        public String getMIMEType() {
            return "text/plain";
        }

        @Override
        public Reader createReader() throws IOException {
            return new BufferedReader(new FileReader(file));
        }

        @Override
        public Writer createWriter(Difference[] conflicts) throws IOException {
            return null;
        }
    }
}