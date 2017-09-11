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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugzilla.query;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.netbeans.modules.bugzilla.*;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugzilla.query.QueryParameter.AllWordsTextFieldParameter;
import org.netbeans.modules.bugzilla.query.QueryParameter.CheckBoxParameter;
import org.netbeans.modules.bugzilla.query.QueryParameter.ComboParameter;
import org.netbeans.modules.bugzilla.query.QueryParameter.EmptyValuesListParameter;
import org.netbeans.modules.bugzilla.query.QueryParameter.ListParameter;
import org.netbeans.modules.bugzilla.query.QueryParameter.ParameterValue;
import org.netbeans.modules.bugzilla.query.QueryParameter.TextFieldParameter;

/**
 *
 * @author tomas
 */
public class QueryParameterTest extends NbTestCase implements TestConstants {

    private final static String PARAMETER = "parameter";
    private final static ParameterValue PV1 = new ParameterValue("pv1");
    private final static ParameterValue PV2 = new ParameterValue("pv2");
    private final static ParameterValue PV3 = new ParameterValue("pv3");
    private final static ParameterValue PV4 = new ParameterValue("pv4");
    private final static ParameterValue[] VALUES = new ParameterValue[] {PV1, PV2, PV3, PV4};

    public QueryParameterTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
    }

    public void testComboParameters() {
        JComboBox combo = new JComboBox();
        ComboParameter cp = new QueryParameter.ComboParameter(combo, PARAMETER, "UTF-8");
        assertEquals(PARAMETER, cp.getParameter());
        assertNull(combo.getSelectedItem());
        assertEquals(cp.get(false).toString(), "&" + PARAMETER + "=");
        assertFalse(cp.isChanged());
        cp.setParameterValues(VALUES);
        cp.setValues(new ParameterValue[] {PV2});

        Object item = combo.getSelectedItem();
        assertNotNull(item);
        assertEquals(PV2, item);

        ParameterValue[] v = cp.getValues();
        assertEquals(1, v.length);
        assertEquals(PV2, v[0]);

        assertEquals(cp.get(false).toString(), "&" + PARAMETER + "=" + PV2.getValue());

        combo.setSelectedItem(PV3);
        assertEquals(cp.get(false).toString(), "&" + PARAMETER + "=" + PV3.getValue());
        
        assertTrue(cp.isChanged());
        cp.reset();
        assertFalse(cp.isChanged());
    }

    public void testListParameters() {
        JList list = new JList();
        ListParameter lp = new ListParameter(list, PARAMETER, "UTF-8");
        assertEquals(PARAMETER, lp.getParameter());
        assertEquals(-1, list.getSelectedIndex());
        assertEquals(lp.get(false).toString(), "&" + PARAMETER + "=");
        assertFalse(lp.isChanged());
        lp.setParameterValues(VALUES);
        lp.setValues(new ParameterValue[] {PV2, PV3});

        Object[] items = list.getSelectedValues();
        assertNotNull(items);
        assertEquals(2, items.length);
        Set<ParameterValue> s = new HashSet<ParameterValue>();
        for (Object i : items) s.add((ParameterValue)i);
        if(!s.contains(PV2)) fail("mising parameter [" + PV2 + "]");
        if(!s.contains(PV3)) fail("mising parameter [" + PV3 + "]");

        ParameterValue[] v = lp.getValues();
        assertEquals(2, v.length);
        s.clear();
        for (ParameterValue pv : v) s.add(pv);
        if(!s.contains(PV2)) fail("mising parameter [" + PV2 + "]");
        if(!s.contains(PV3)) fail("mising parameter [" + PV3 + "]");

        String get = lp.get(false).toString();
        String[] returned = get.split("&");
        Set<String> ss = new HashSet<String>();
        for (int i = 1; i < returned.length; i++) ss.add(returned[i]);
        assertEquals(2, ss.size());
        if(!ss.contains(PARAMETER + "=" + PV2.getValue())) fail("mising parameter [" + PV2 + "]");
        if(!ss.contains(PARAMETER + "=" + PV3.getValue())) fail("mising parameter [" + PV3 + "]");

        list.setSelectedValue(PV4, false);
        assertEquals(lp.get(false).toString(), "&" + PARAMETER + "=" + PV4.getValue());
        assertTrue(lp.isChanged());
        lp.reset();
        assertFalse(lp.isChanged());
    }

    public void testEmptyValuesListParameters() {
        JList list = new JList();
        EmptyValuesListParameter lp = new EmptyValuesListParameter(list, PARAMETER, "UTF-8");
        assertEquals(PARAMETER, lp.getParameter());
        assertEquals(-1, list.getSelectedIndex());
        assertEquals("", lp.get(false).toString());
        assertEquals("[&" + PARAMETER + "=]", lp.toString());
        assertFalse(lp.isChanged());
        lp.setParameterValues(VALUES);
        lp.setValues(new ParameterValue[] {PV2, PV3});

        Object[] items = list.getSelectedValues();
        assertNotNull(items);
        assertEquals(2, items.length);
        Set<ParameterValue> s = new HashSet<ParameterValue>();
        for (Object i : items) s.add((ParameterValue)i);
        if(!s.contains(PV2)) fail("mising parameter [" + PV2 + "]");
        if(!s.contains(PV3)) fail("mising parameter [" + PV3 + "]");

        ParameterValue[] v = lp.getValues();
        assertEquals(2, v.length);
        s.clear();
        for (ParameterValue pv : v) s.add(pv);
        if(!s.contains(PV2)) fail("mising parameter [" + PV2 + "]");
        if(!s.contains(PV3)) fail("mising parameter [" + PV3 + "]");

        String get = lp.get(false).toString();
        String[] returned = get.split("&");
        Set<String> ss = new HashSet<String>();
        for (int i = 1; i < returned.length; i++) ss.add(returned[i]);
        assertEquals(2, ss.size());
        if(!ss.contains(PARAMETER + "=" + PV2.getValue())) fail("mising parameter [" + PV2 + "]");
        if(!ss.contains(PARAMETER + "=" + PV3.getValue())) fail("mising parameter [" + PV3 + "]");

        list.setSelectedValue(PV4, false);
        assertEquals(lp.get(false).toString(), "&" + PARAMETER + "=" + PV4.getValue());
        assertTrue(lp.isChanged());
        lp.reset();
        assertFalse(lp.isChanged());
    }

    public void testTextFieldParameter() throws UnsupportedEncodingException {
        JTextField text = new JTextField();
        TextFieldParameter tp = new TextFieldParameter(text, PARAMETER, "UTF-8");
        assertEquals(PARAMETER, tp.getParameter());
        assertEquals("", text.getText());
        assertEquals(tp.get(false).toString(), "&" + PARAMETER + "=");
        assertFalse(tp.isChanged());
        
        tp.setValues(new ParameterValue[] {PV2});
        assertEquals(PV2.getValue(), text.getText());
        assertEquals(1, tp.getValues().length);
        assertEquals(PV2, tp.getValues()[0]);
        assertEquals(tp.get(false).toString(), "&" + PARAMETER + "=" + PV2.getValue());

        String parameterValue = "New+Value";
        tp.setValues(new ParameterValue[] {new ParameterValue(parameterValue)});
        assertEquals("New Value", text.getText());
        assertEquals(1, tp.getValues().length);
        assertEquals(new ParameterValue(parameterValue), tp.getValues()[0]);
        assertEquals(tp.get(true).toString(), "&" + PARAMETER + "=" + URLEncoder.encode(parameterValue, "UTF-8"));

        parameterValue = "NewValue";
        text.setText(parameterValue);
        assertEquals(1, tp.getValues().length);
        assertEquals(new ParameterValue(parameterValue), tp.getValues()[0]);
        assertEquals(tp.get(true).toString(), "&" + PARAMETER + "=" + URLEncoder.encode(parameterValue, "UTF-8"));
        assertEquals("NewValue", text.getText());

        text.setText("New Value1");
        assertEquals(1, tp.getValues().length);
        parameterValue = "New+Value1";
        assertEquals(new ParameterValue(parameterValue), tp.getValues()[0]);
        assertEquals(tp.get(true).toString(), "&" + PARAMETER + "=" + URLEncoder.encode(parameterValue, "UTF-8"));

        assertTrue(tp.isChanged());
        tp.reset();
        assertFalse(tp.isChanged());
    }
    
    public void testAllWordsTextFieldParameter() throws UnsupportedEncodingException {
        JTextField text = new JTextField();
        TextFieldParameter tp = new AllWordsTextFieldParameter(text, PARAMETER, "UTF-8");
        assertEquals(PARAMETER, tp.getParameter());
        assertEquals("", text.getText());
        assertEquals(tp.get(false).toString(), "&" + PARAMETER + "=");
        assertFalse(tp.isChanged());
        
        tp.setValues(new ParameterValue[] {PV2});
        assertEquals(PV2.getValue(), text.getText());
        assertEquals(1, tp.getValues().length);
        assertEquals(PV2, tp.getValues()[0]);
        assertEquals(tp.get(false).toString(), "&" + PARAMETER + "=" + PV2.getValue());

        String parameterValue = "New+Value";
        tp.setValues(new ParameterValue[] {new ParameterValue(parameterValue)});
        assertEquals("New+Value", text.getText());
        assertEquals(1, tp.getValues().length);
        assertEquals(new ParameterValue(parameterValue), tp.getValues()[0]);
        assertEquals(tp.get(true).toString(), "&" + PARAMETER + "=" + URLEncoder.encode(parameterValue, "UTF-8"));

        parameterValue = "NewValue";
        text.setText(parameterValue);
        assertEquals(1, tp.getValues().length);
        assertEquals(new ParameterValue(parameterValue), tp.getValues()[0]);
        assertEquals(tp.get(true).toString(), "&" + PARAMETER + "=" + URLEncoder.encode(parameterValue, "UTF-8"));
        assertEquals("NewValue", text.getText());

        text.setText("New Value1");
        assertEquals(1, tp.getValues().length);
        parameterValue = "New Value1";
        assertEquals(new ParameterValue(parameterValue), tp.getValues()[0]);
        assertEquals(tp.get(true).toString(), "&" + PARAMETER + "=" + URLEncoder.encode(parameterValue, "UTF-8"));

        assertTrue(tp.isChanged());
        tp.reset();
        assertFalse(tp.isChanged());
    }

    public void testCheckBoxParameter() {
        JCheckBox checkbox = new JCheckBox();
        CheckBoxParameter cp = new CheckBoxParameter(checkbox, PARAMETER, "UTF-8");
        assertEquals(PARAMETER, cp.getParameter());
        assertFalse(checkbox.isSelected());
        assertEquals(cp.get(false).toString(), "&" + PARAMETER + "=");
        assertFalse(cp.isChanged());
        
        ParameterValue pv = new ParameterValue("1");
        cp.setValues(new ParameterValue[] {pv});
        assertTrue(checkbox.isSelected());
        assertEquals(1, cp.getValues().length);
        assertEquals(pv, cp.getValues()[0]);
        assertEquals(cp.get(false).toString(), "&" + PARAMETER + "=1");

        pv = new ParameterValue("0");
        cp.setValues(new ParameterValue[] {pv});
        assertFalse(checkbox.isSelected());
        assertEquals(1, cp.getValues().length);
        assertEquals(QueryParameter.EMPTY_PARAMETER_VALUE[0], cp.getValues()[0]);
        assertEquals(cp.get(false).toString(), "&" + PARAMETER + "=");
        
        checkbox.setSelected(!checkbox.isSelected());
        assertTrue(cp.isChanged());
        cp.reset();
        assertFalse(cp.isChanged());
    }

}
