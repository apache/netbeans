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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.tasklist.filter;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;


/**
 * Basic condition class for string comparisons.
 * This class can also be used to compare objects of classes other than
 * strings. It uses getAsText() to convert properties to strings.
 *
 * @author Tor Norbye
 */
class StringFilterCondition extends OneOfFilterCondition {
    public static final int EQUALS = 0;
    public static final int NOTEQUALS = 1;
    public static final int CONTAINS = 2;
    public static final int DOESNOTCONTAIN = 3;
    public static final int BEGINSWITH = 4;
    public static final int ENDSWITH = 5;
    public static final int CEQUALS = 6;
    public static final int CCONTAINS = 7;
    public static final int CDOESNOTCONTAIN = 8;
    public static final int CBEGINSWITH = 9;
    public static final int CENDSWITH = 10;
    
    /**
     * Creates an array of filter conditions for the specified property
     *
     * @param index index of the property
     */
    public static StringFilterCondition[] createConditions() {
        return new StringFilterCondition[] {
            new StringFilterCondition(StringFilterCondition.CONTAINS),
            new StringFilterCondition(StringFilterCondition.DOESNOTCONTAIN),
            new StringFilterCondition(StringFilterCondition.BEGINSWITH),
            new StringFilterCondition(StringFilterCondition.ENDSWITH),
            new StringFilterCondition(StringFilterCondition.EQUALS),
            new StringFilterCondition(StringFilterCondition.NOTEQUALS),
            new StringFilterCondition(StringFilterCondition.CCONTAINS),
            new StringFilterCondition(StringFilterCondition.CDOESNOTCONTAIN),
            new StringFilterCondition(StringFilterCondition.CBEGINSWITH),
            new StringFilterCondition(StringFilterCondition.CENDSWITH),
            new StringFilterCondition(StringFilterCondition.CEQUALS)
        };
    }
    
    private static String[] NAME_KEYS = {
        "Equals", // NOI18N
        "NotEquals", // NOI18N
        "Contains", // NOI18N
        "DoesNotContain", // NOI18N
        "BeginsWith", // NOI18N
        "EndsWith", // NOI18N
        "CEquals", // NOI18N
        "CContains", // NOI18N
        "CDoesNotContain", // NOI18N
        "CBeginsWith", // NOI18N
        "CEndsWith" // NOI18N
    };
    
    /** saved constant for comparison */
    private String constant = ""; // NOI18N
    
    /**
     * Creates a condition with the given name.
     *
     * @param id one of the constants from this class
     */
    public StringFilterCondition(int id) {
        super(NAME_KEYS, id);
    }
    
    /**
     * Creates a condition with the given name.
     *
     * @param id one of the constants from this class
     * @param value the value to compare the property with
     */
    public StringFilterCondition(int id, String value) {
        this(id);
        this.constant = value;
    }
    
    
    public StringFilterCondition(final StringFilterCondition rhs) {
        super(rhs);
        this.constant = rhs.constant;
    }
    
    public Object clone() {
        return new StringFilterCondition(this);
    }
    
    StringFilterCondition() { super(NAME_KEYS); constant = null; }
    
    /** Return the value that Strings are compared with.
     * @return the value that Strings are compared with */
    public String getConstant() {
        return constant;
    }
    
    public JComponent createConstantComponent() {
        final JTextField tf = new JTextField();
        tf.setText(constant);
        tf.setToolTipText(Util.getString("string_desc")); //NOI18N
        tf.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                Boolean valid = Boolean.valueOf("".equals(tf.getText()) == false);
                tf.putClientProperty(FilterCondition.PROP_VALUE_VALID, valid);
            }
            
            public void insertUpdate(DocumentEvent e) {
                Boolean valid = Boolean.valueOf("".equals(tf.getText()) == false);
                tf.putClientProperty(FilterCondition.PROP_VALUE_VALID, valid);
            }
            
            public void removeUpdate(DocumentEvent e) {
                Boolean valid = Boolean.valueOf("".equals(tf.getText()) == false);
                tf.putClientProperty(FilterCondition.PROP_VALUE_VALID, valid);
            }
            
        });
        return tf;
    }
    
    public void getConstantFrom(JComponent cmp) {
        JTextField tf = (JTextField) cmp;
        constant = tf.getText();
    }
    
    public boolean isTrue(Object obj) {
        String s = obj == null ? "" : obj.toString(); //NOI18N
        switch (getId()) {
        case EQUALS:
            return s.equalsIgnoreCase(constant);
        case NOTEQUALS:
            return !s.equalsIgnoreCase(constant);
        case CONTAINS:
            return s.toLowerCase().indexOf(constant.toLowerCase()) >= 0;
        case DOESNOTCONTAIN:
            return s.toLowerCase().indexOf(constant.toLowerCase()) < 0;
        case BEGINSWITH:
            return s.toLowerCase().startsWith(constant.toLowerCase());
        case ENDSWITH:
            return s.toLowerCase().endsWith(constant.toLowerCase());
        case CEQUALS:
            return s.equals(constant);
        case CCONTAINS:
            return s.indexOf(constant) >= 0;
        case CDOESNOTCONTAIN:
            return s.indexOf(constant) < 0;
        case CBEGINSWITH:
            return s.startsWith(constant);
        case CENDSWITH:
            return s.endsWith(constant);
        default:
            throw new InternalError("wrong id"); //NOI18N
        }
    }
    
    void load( Preferences prefs, String prefix ) throws BackingStoreException {
        super.load( prefs, prefix );
        constant = prefs.get( prefix+"_constant", "" ); //NOI18N
    }
    
    void save( Preferences prefs, String prefix ) throws BackingStoreException {
        super.save( prefs, prefix );
        prefs.put( prefix+"_constant", constant ); //NOI18N
    }
}
