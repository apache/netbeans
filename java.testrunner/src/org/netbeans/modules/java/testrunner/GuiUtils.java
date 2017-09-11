/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.java.testrunner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.accessibility.AccessibleContext;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.gsf.testrunner.api.NamedObject;
import org.netbeans.modules.gsf.testrunner.api.SizeRestrictedPanel;
import org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Various utility method for creating and manipulating with GUI elements.
 *
 * @author  Marian Petras
 */
public final class GuiUtils {
    
    /** */
    public static final String TEMPLATES_DIR = "Templates/JUnit";       //NOI18N
    
    
    public static final String JUNIT_TEST_FRAMEWORK = TestCreatorProvider.FRAMEWORK_JUNIT;
    public static final String TESTNG_TEST_FRAMEWORK = TestCreatorProvider.FRAMEWORK_TESTNG;
    
    public static final String CHK_INTEGRATION_TESTS = "IntegrationTests";                   //NOI18N
    
    /** */
    public static final String CHK_PUBLIC = "Public";                   //NOI18N
    /** */
    public static final String CHK_PROTECTED = "Protected";             //NOI18N
    /** */
    public static final String CHK_PACKAGE = "Package";                 //NOI18N
    /** */
    public static final String CHK_PACKAGE_PRIVATE_CLASSES
                               = "PackagePrivateClasses";               //NOI18N
    /** */
    public static final String CHK_ABSTRACT_CLASSES
                               = "AbstractImpl";                        //NOI18N
    /** */
    public static final String CHK_EXCEPTION_CLASSES
                               = "Exceptions";                          //NOI18N
    /** */
    public static final String CHK_SUITES = "GenerateSuites";           //NOI18N
    /** */
    public static final String CHK_SETUP = "SetUp";                     //NOI18N
    /** */
    public static final String CHK_TEARDOWN = "TearDown";               //NOI18N
    /** */
    public static final String CHK_BEFORE_CLASS = "BeforeClass";                     //NOI18N
    /** */
    public static final String CHK_AFTER_CLASS = "AfterClass";               //NOI18N
    /** */
    public static final String CHK_METHOD_BODIES = "Content";           //NOI18N
    /** */
    public static final String CHK_JAVADOC = "JavaDoc";                 //NOI18N
    /** */
    public static final String CHK_HINTS = "Comments";                  //NOI18N
    
    /**
     * Creates a combo-box for choosing a template.
     * The combo-box will contain <code>FileObject</code>s representing
     * available JUnit templates, wrapped using class {@link NamedObject},
     * so that the file's names are displayed in the combo-box.
     * <p>
     * To get the currently selected template from the combo-box, use:
     * <blockquote><pre>
     * NamedObject namedObject = (NamedObject) comboBox.getSelectedItem();
     * FileObject template = (FileObject) namedObject.object;
     * </pre></blockquote>
     * 
     *
     * @param  defaultTemplate  path to the default template
     * @return  non-editable combo-box displaying names of templates
     */
    public static JComboBox createTemplateChooser(String defaultTemplate) {
        FileObject templatesDir = FileUtil.getConfigFile(TEMPLATES_DIR);
        if (templatesDir == null) {
            throw new RuntimeException("Not found: " + TEMPLATES_DIR);  //NOI18N
        }
        FileObject templates[] = templatesDir.getChildren();
        
        /*
         * collect a list of templates and identify the default template
         * among them:
         */
        List<NamedObject> itemList = new ArrayList<NamedObject>(templates.length);
        int defaultItemIndex = -1;
        int itemIndex = 0;
        
        for (int i = 0; i < templates.length; i++) {
            FileObject template = templates[i];
            
            if (!template.getExt().equals("java")) {                    //NOI18N
                continue;
            }
            
            itemList.add(new NamedObject(template, template.getName()));
            
            if ((defaultItemIndex == -1)
                    && (defaultTemplate != null)
                    && template.getPath().equals(defaultTemplate)) {
                defaultItemIndex = itemIndex;
            }
            
            itemIndex++;
        }
        
        /* create the combo-box and select the default template: */
        JComboBox comboBox;
        if (itemList.isEmpty()) {
            comboBox = new JComboBox();
        } else {
            comboBox = new JComboBox(itemList.toArray());
            if (defaultItemIndex != -1) {
                comboBox.setSelectedIndex(defaultItemIndex);
            }
        }
        comboBox.setEditable(false);
        return comboBox;
    }
    
    /**
     * Creates a specified set of checkboxes.
     * The checkboxes are specified by unique identifiers.
     * The identifiers are given by this class's constants <code>CHK_xxx</code>.
     * <p>
     * The array of strings passed as the argument may also contain
     * <code>null</code> items. In such a case, the resulting array
     * of check-boxes will contain <code>null</code>s on the corresponding
     * possitions.
     *
     * @param  ids  identifiers of the checkboxes to be created
     * @return  array of checkboxes corresponding to the array of identifiers
     *          passed as the argument
     */
    public static JCheckBox[] createCheckBoxes(String[] ids) {
        JCheckBox[] chkBoxes = new JCheckBox[ids.length];
        
        if (chkBoxes.length == 0) {
            return chkBoxes;
        }
        
        ResourceBundle bundle = NbBundle.getBundle(GuiUtils.class);
        for (int i = 0; i < ids.length; i++) {
            String id = ids[i];
            
            if (id == null) {
                chkBoxes[i] = null;
                continue;
            }
            
            JCheckBox chkBox = new JCheckBox();
            String baseName = "CommonTestsCfgOfCreate.chk" + id;              //NOI18N
            AccessibleContext accessCtx = chkBox.getAccessibleContext();
            Mnemonics.setLocalizedText(
                    chkBox,
                    bundle.getString(baseName + ".text"));              //NOI18N
            chkBox.setToolTipText(
                    bundle.getString(baseName + ".toolTip"));           //NOI18N
            accessCtx.setAccessibleName(
                    bundle.getString(baseName + ".AN"));                //NOI18N
            accessCtx.setAccessibleDescription(
                    bundle.getString(baseName + ".AD"));                //NOI18N
            
            chkBoxes[i] = chkBox;
        }
        return chkBoxes;
    }
    
    /**
     * Creates a labelled group of checkboxes.
     *
     * @param  title  title for the group of checkboxes
     * @param  elements  checkboxes - members of the group
     * @return  visual component representing the group
     */
    public static JComponent createChkBoxGroup(String title,
                                               JCheckBox[] elements) {
        
        /* create a component representing the group without title: */
        JComponent content = new JPanel(new GridLayout(0, 1, 0, 5));
        for (int i = 0; i < elements.length; i++) {
            content.add(elements[i]);
        }
        
        /* add the title and insets to the group: */
        JPanel result = new SizeRestrictedPanel(new BorderLayout(), true, true);
        result.add(new JLabel(title), BorderLayout.NORTH);
        addBorder(content, BorderFactory.createEmptyBorder(6, 12, 0, 0));
        result.add(content, BorderLayout.CENTER);
        
        return result;
    }
    
    /**
     * Creates a text component to be used as a multi-line, automatically
     * wrapping label.
     * <p>
     * <strong>Restriction:</strong><br>
     * The component may have its preferred size very wide.
     *
     * @param  text  text of the label
     * @return  created multi-line text component
     */
    public static JTextComponent createMultilineLabel(String text) {
        return createMultilineLabel(text, null);
    }

    /**
     * Creates a text component to be used as a multi-line, automatically
     * wrapping label.
     * <p>
     * <strong>Restriction:</strong><br>
     * The component may have its preferred size very wide.
     *
     * @param  text  text of the label
     * @param  color  desired color of the label,
     *                or {@code null} if the default color should be used
     * @return  created multi-line text component
     */
    public static JTextComponent createMultilineLabel(String text, Color color) {
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEnabled(false);
        textArea.setOpaque(false);
        textArea.setColumns(25);
        textArea.setDisabledTextColor((color != null)
                                      ? color
                                      : new JLabel().getForeground());
        
        return textArea;
    }
    
    /**
     * Adds a given border to a given component.
     * If the component already has some border, the given border is put
     * around the existing border.
     *
     * @param  component  component the border should be added to
     * @param  border  the border to be added
     */
    private static void addBorder(JComponent component,
                                  Border newBorder) {
        Border currentBorder = component.getBorder();
        if (currentBorder == null) {
            component.setBorder(newBorder);
        } else {
            component.setBorder(BorderFactory.createCompoundBorder(
                    newBorder,          //outside
                    currentBorder));    //inside
        }
    }
    
}
