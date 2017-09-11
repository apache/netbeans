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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.beaninfo.editors;

import java.beans.PropertyEditorSupport;

// bugfix# 9219 for attachEnv() method
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import java.beans.FeatureDescriptor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * A property editor for String class.
* @author   Ian Formanek
*/
public class StringEditor extends PropertyEditorSupport implements ExPropertyEditor
{
    private static boolean useRaw = Boolean.getBoolean("netbeans.stringEditor.useRawCharacters");
    
   // bugfix# 9219 added editable field and isEditable() "getter" to be used in StringCustomEditor    
    private boolean editable=true;   
    /** gets information if the text in editor should be editable or not */
    public boolean isEditable(){
        return (editable);
    }
                
    @Override
    public String getAsText() {
        Object value = getValue();
        if (value != null) {
            return value.toString();
        } else {
            return nullValue != null ? nullValue : NbBundle.getMessage(StringEditor.class, "CTL_NullValue");
        }
    }

    /** sets new value */
    @Override
    public void setAsText(String s) {
        if ( "null".equals( s ) && getValue() == null ) // NOI18N
            return;
        if (nullValue != null && nullValue.equals (s)) {
            setValue (null);
            return;
        }

        setValue(s);
    }

    @Override
    public String getJavaInitializationString () {
        String s = (String) getValue ();
        return "\"" + toAscii(s) + "\""; // NOI18N
    }

    @Override
    public boolean supportsCustomEditor () {
        return customEd;
    }

    @Override
    public java.awt.Component getCustomEditor () {
        Object val = getValue();
        String s = ""; // NOI18N
        if (val != null) {
            s = val instanceof String ? (String) val : val.toString();
        }
        return new StringCustomEditor (s, isEditable(), oneline, instructions, this, env);
    }

    private static String toAscii(String str) {
        StringBuilder buf = new StringBuilder(str.length() * 6); // x -> \u1234
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
            case '\b': buf.append("\\b"); break; // NOI18N
            case '\t': buf.append("\\t"); break; // NOI18N
            case '\n': buf.append("\\n"); break; // NOI18N
            case '\f': buf.append("\\f"); break; // NOI18N
            case '\r': buf.append("\\r"); break; // NOI18N
            case '\"': buf.append("\\\""); break; // NOI18N
                //        case '\'': buf.append("\\'"); break; // NOI18N
            case '\\': buf.append("\\\\"); break; // NOI18N
            default:
                if (c >= 0x0020 && (useRaw || c <= 0x007f))
                    buf.append(c);
                else {
                    buf.append("\\u"); // NOI18N
                    String hex = Integer.toHexString(c);
                    for (int j = 0; j < 4 - hex.length(); j++)
                        buf.append('0');
                    buf.append(hex);
                }
            }
        }
        return buf.toString();
    }
    
    private String instructions=null;
    private boolean oneline=false;
    private boolean customEd=false; // until PropertyEnv is attached
    private PropertyEnv env;
    /** null or name to use for null value */
    private String nullValue;

    // bugfix# 9219 added attachEnv() method checking if the user canWrite in text box 
    @Override
    public void attachEnv(PropertyEnv env) {
        this.env = env;

        readEnv(env.getFeatureDescriptor());
    }

    /*@VisibleForTesting*/ void readEnv (FeatureDescriptor desc) {
        if (desc instanceof Node.Property){
            Node.Property prop = (Node.Property)desc;
            editable = prop.canWrite();
            //enh 29294 - support one-line editor & suppression of custom
            //editor
            instructions = (String) prop.getValue ("instructions"); //NOI18N
            oneline = Boolean.TRUE.equals (prop.getValue ("oneline")); //NOI18N
            customEd = !Boolean.TRUE.equals (prop.getValue
                ("suppressCustomEditor")); //NOI18N
        }
        Object obj = desc.getValue(ObjectEditor.PROP_NULL);
        if (Boolean.TRUE.equals(obj)) {
            nullValue = NbBundle.getMessage(StringEditor.class, "CTL_NullValue");
        } else {
            if (obj instanceof String) {
                nullValue = (String)obj;
            } else {
                nullValue = null;
            }
        }
    }
}
