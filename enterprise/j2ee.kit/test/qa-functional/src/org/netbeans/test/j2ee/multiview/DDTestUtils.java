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
package org.netbeans.test.j2ee.multiview;

import java.awt.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.AssertionFailedErrorException;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.DDBeanTableModel;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.FilterParamsPanel;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.InitParamsPanel;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.xml.multiview.ui.SectionPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author jp159440
 */
public class DDTestUtils /*extends JellyTestCase*/ {

    private DDDataObject ddObj;
    private WebApp webapp;
    private JellyTestCase testCase;

    /** Creates a new instance of DDTestUtils */
    public DDTestUtils(DDDataObject ddObj, JellyTestCase testCase) {
        this.ddObj = ddObj;
        webapp = ddObj.getWebApp();
        this.testCase = testCase;
    }

    public void testTable(DDBeanTableModel model, String[][] values) {
        JellyTestCase.assertEquals("Wrong count of table rows", values.length, model.getRowCount());
        for (int i = 0; i < model.getRowCount(); i++) {
            this.testTableRow(model, i, values[i]);
        }
    }

    public void testTableRow(DDBeanTableModel model, int row, String[] values) {
        JellyTestCase.assertTrue("No such row in table model", model.getRowCount() >= row);
        int i;

        for (i = 0; i < values.length; i++) {
            JellyTestCase.assertEquals("Value at " + row + "," + i + " does not match.", values[i], model.getValueAt(row, i));
        }
    }

    public void setTableRow(DDBeanTableModel model, int row, Object[] values) throws Exception {
        for (int i = 0; i < values.length; i++) {
            model.setValueAt(values[i], row, i);
        }
    }

    public CommonDDBean getBeanByProp(CommonDDBean[] beans, String prop, Object value) {
        for (int i = 0; i < beans.length; i++) {
            if (beans[i].getValue(prop).equals(value)) {
                return beans[i];
            }
        }
        return null;
    }

    public int getRowIndexByProp(DDBeanTableModel model, int col, Object value) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, col).equals(value)) {
                return i;
            }
        }
        return -1;
    }

    public DDBeanTableModel getModelByBean(Object bean) {
        JPanel panel = getInnerSectionPanel(bean);
        Component[] comp = panel.getComponents();
        DDBeanTableModel model = (DDBeanTableModel) ((DefaultTablePanel) comp[1]).getModel();
        return model;
    }

    public void checkPropertyGroup(JspPropertyGroup prop, String name, String desc, String enc, String[] headers, String[] footers, String[] urls, boolean[] switches) {
        JellyTestCase.assertEquals("Display name doesn't match", name, prop.getDefaultDisplayName());
        JellyTestCase.assertEquals("Description doesn't match", desc, prop.getDefaultDescription());
        JellyTestCase.assertEquals("Encoding doesn't match", enc, prop.getPageEncoding());
        testStringArrayEquals(prop.getIncludeCoda(), footers);
        testStringArrayEquals(prop.getIncludePrelude(), headers);
        testStringArrayEquals(prop.getUrlPattern(), urls);
        JellyTestCase.assertEquals("ELIgnored: ", switches[0], prop.isElIgnored());
        JellyTestCase.assertEquals("XML syntax: ", switches[1], prop.isIsXml());
        JellyTestCase.assertEquals("Scripting invalid: ", switches[2], prop.isScriptingInvalid());
        JPanel panel = getInnerSectionPanel(prop);
        Component[] comp = panel.getComponents();
        JellyTestCase.assertEquals("Display name doesn't match.", name, ((JTextField) comp[1]).getText());
        JellyTestCase.assertEquals("Description doesn't match.", desc, ((JTextArea) comp[3]).getText());
        String tmp = "";
        for (int i = 0; i < urls.length; i++) {
            if (i > 0) {
                tmp = tmp + ", ";
            }
            tmp = tmp + urls[i];
        }
        JellyTestCase.assertEquals("Url patterns doesn't match.", tmp, ((JTextField) comp[5]).getText());
        JellyTestCase.assertEquals("Encoding doesn't match.", enc, ((JTextField) comp[9]).getText());
        JellyTestCase.assertEquals("EL ignore doesn't match.", switches[0], ((JCheckBox) comp[10]).isSelected());
        JellyTestCase.assertEquals("XML syntax doesn't match.", switches[1], ((JCheckBox) comp[11]).isSelected());
        JellyTestCase.assertEquals("Script invalid doesn't match.", switches[2], ((JCheckBox) comp[12]).isSelected());
        tmp = "";
        for (int i = 0; i < headers.length; i++) {
            if (i > 0) {
                tmp = tmp + ", ";
            }
            tmp = tmp + headers[i];
        }
        JellyTestCase.assertEquals("Preludes doesn't match.", tmp, ((JTextField) comp[14]).getText());
        tmp = "";
        for (int i = 0; i < footers.length; i++) {
            if (i > 0) {
                tmp = tmp + ", ";
            }
            tmp = tmp + footers[i];
        }
        JellyTestCase.assertEquals("Codas doesn't match.", tmp, ((JTextField) comp[17]).getText());
    }

    public DDBeanTableModel getServletInitParamsTableModel() {
        Servlet[] servlets = webapp.getServlet();
        JellyTestCase.assertEquals("Wrong count of servlets", 1, servlets.length);
        JPanel panel = getInnerSectionPanel(servlets[0]);
        Component[] comp = panel.getComponents();
        InitParamsPanel tablePanel = ((InitParamsPanel) comp[17]);
        return (DDBeanTableModel) tablePanel.getTable().getModel();
    }

    public DDBeanTableModel getFilterInitParamsTableModel() {
        Filter[] filters = webapp.getFilter();
        JellyTestCase.assertEquals("Wrong count of filters", 1, filters.length);
        JPanel panel = getInnerSectionPanel(filters[0]);
        Component[] comp = panel.getComponents();
        FilterParamsPanel tablePanel = ((FilterParamsPanel) comp[9]);
        return (DDBeanTableModel) tablePanel.getTable().getModel();
    }

    public void testProperties(CommonDDBean bean, String[] properties, Object[] values) throws Exception {
        for (int i = 0; i < properties.length; i++) {
            JellyTestCase.assertEquals("Property " + properties[i] + " has wrong value.", values[i], bean.getValue(properties[i]));
        }
    }

    public void save() throws Exception {
        new StepIterator() {

            SaveCookie saveCookie;

            @Override
            public boolean step() throws Exception {
                saveCookie = (SaveCookie) ddObj.getCookie(SaveCookie.class);
                return saveCookie != null;
            }
        };
        SaveCookie saveCookie = (SaveCookie) ddObj.getCookie(SaveCookie.class);
        JellyTestCase.assertNotNull("Document was not modified", saveCookie);
        saveCookie.save();
    }

    public void traverse(Node n, String pref) {
        System.out.println(pref + n.getName() + " " + n.getClass().getName());
        Children ch = n.getChildren();
        Node[] nodes = ch.getNodes();
        for (int i = 0; i < nodes.length; i++) {
            traverse(nodes[i], pref + "  ");
        }
    }

    public static void waitForDispatchThread() {
        if (SwingUtilities.isEventDispatchThread()) {
            return;
        }
        final boolean[] finished = new boolean[]{false};
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                finished[0] = true;
            }
        });
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return finished[0];
            }
        };
    }

    public JPanel getInnerSectionPanel(Object sectionPanel) {
        ToolBarMultiViewElement multi = ddObj.getActiveMVElement();
        SectionView section = multi.getSectionView();
        section.openPanel(sectionPanel);
        SectionPanel sp = section.findSectionPanel(sectionPanel);
        JellyTestCase.assertNotNull("Section panel " + sectionPanel + " not found", sp);
        JPanel p = sp.getInnerPanel();
        JellyTestCase.assertNotNull("Section panel has no inner panel", p);
        return p;
    }

    public void checkInDDXML(String findText) {
        checkInDDXML(findText, true);
    }

    public void checkInDDXML(String findText, boolean present) {
        boolean matches = contains(findText);
        if (present) {
            JellyTestCase.assertTrue("Cannot find correct element in XML view (editor document)", matches);
        } else {
            JellyTestCase.assertFalse("Unexpected element found in XML view (editor document)", matches);
        }
    }

    public String document() {
        XmlMultiViewEditorSupport editor = (XmlMultiViewEditorSupport) ddObj.getCookie(EditorCookie.class);
        javax.swing.text.Document document = editor.getDocument();
        try {
            String text = document.getText(0, document.getLength());
            return text;
        } catch (javax.swing.text.BadLocationException ex) {
            throw new AssertionFailedErrorException("Failed to read the document: ", ex);
        }
    }

    public boolean contains(String findText) {
        waitForDispatchThread();
        XmlMultiViewEditorSupport editor = (XmlMultiViewEditorSupport) ddObj.getCookie(EditorCookie.class);
        javax.swing.text.Document document = editor.getDocument();
        try {
            String text = document.getText(0, document.getLength());
            Pattern p = Pattern.compile(findText, Pattern.DOTALL);
            Matcher m = p.matcher(text.subSequence(0, text.length() - 1));
            return m.matches();
        } catch (javax.swing.text.BadLocationException ex) {
            throw new AssertionFailedErrorException("Failed to read the document: ", ex);
        }
    }

    public void checkNotInDDXML(String findText) {
        checkInDDXML(findText, false);
    }

    public void testStringArrayEquals(String[] expected, String[] actual) {
        JellyTestCase.assertEquals("Wrong array size. ", expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            JellyTestCase.assertEquals("The " + i + " element has wrong value ", expected[i], actual[i]);
        }
    }

    public void setText(JTextComponent component, String text) {
        component.requestFocus();
        waitForDispatchThread();
        Document doc = component.getDocument();
        try {
            doc.remove(0, doc.getLength());
            doc.insertString(0, text, null);
        } catch (BadLocationException ex) {
            testCase.fail(ex);
        }
        ddObj.modelUpdatedFromUI();
        waitForDispatchThread();
    }
}
