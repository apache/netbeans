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
/*
 * IntEditor.java
 *
 * Created on February 28, 2003, 2:15 PM
 */

package org.netbeans.beaninfo.editors;
import java.util.Arrays;
import org.netbeans.beaninfo.editors.ExPropertyEditorSupport.EnvException;
import org.netbeans.core.UIExceptions;
import org.openide.explorer.propertysheet.*;
import org.openide.nodes.PropertyEditorRegistration;
import org.openide.util.NbBundle;
/** An editor for primitive integer types which allows hinting of tag
 *  values and handles whitespace in setAsText better than the default
 *  JDK one.  The following hints are supported:
 *  <UL><LI><B>stringKeys</B> - an array of Strings that should be supplied
 *  from getTags()</LI><LI><B>intValues - an array of ints corresponding to
 *  the values represented by stringKeys. <I>This hint must be present if
 *  the stringKeys hint is used, and the arrays must be of the same length.</LI>
 *  <LI><B>codeValues</B> - an array of strings that should be returned from
 *  getJavaInitializationString.  This hint is optional when using the
 *  aforementioned hints.  <P>These hints will also work for the Integer
 *  editor, which wraps an instance of this editor.
 *
 * @author  Tim Boudreau
 * @version 1.0
 */
@PropertyEditorRegistration(targetType=Integer.class)
public class IntEditor extends ExPropertyEditorSupport {
    
    public static final String KEYS = "stringKeys"; //NOI18N
    public static final String VALS = "intValues"; //NOI18N
    public static final String CODE_VALS = "codeValues"; //NOI18N
    String[] keys=null;
    String[] code=null;
    int[] values=null;
    /** Creates a new instance of IntEditor */
    public IntEditor() {
    }
    
    protected void attachEnvImpl(PropertyEnv env) {
        keys = (String[]) env.getFeatureDescriptor().getValue(KEYS);
        values = (int[]) env.getFeatureDescriptor().getValue(VALS);
        code = (String[]) env.getFeatureDescriptor().getValue(CODE_VALS);
    }
    
    /** Validate that the values supplied by the PropertyEnv are proper,
     *  so there's not an obscure ArrayIndexOutOfBoundsException some time
     *  later because bad information was supplied.  */
    protected void validateEnv(PropertyEnv env) {
        //fail fast validation of illegal values
        boolean valid = keys == null && values == null && code == null;
        if (!valid) {
            valid = keys != null && values != null;
            if (!valid) {
                throw new EnvException(
                "You must specify both an array of keys and an " + //NOI18N
                "array of values if you specify one. Keys=" +   //NOI18N
                arrToStr(keys) + " Values=" + arrToStr(values));  //NOI18N
            } else {
                valid = keys.length == values.length;
                if (valid) {
                    valid = keys.length > 0 && values.length > 0;
                }
                
                if (!valid) {
                    throw new EnvException(
                    "The arrays of keys and values must have the same " + //NOI18N
                    "length and the length must be > 0. keys.length =" +  //NOI18N
                    keys.length + " values.length=" + values.length + " Keys=" + //NOI18N
                    arrToStr(keys) + " Values=" + arrToStr(values)); //NOI18N
                } else {
                    if (code != null) {
                        valid = code.length == keys.length;
                        if (valid) {
                            valid = code.length > 0;
                        }
                        if (!valid) {
                            throw new EnvException(
                            "The arrays of keys and values and codes must all" + //NOI18N
                            " have the same length, > 0. keys.length =" +   //NOI18N
                            keys.length + " values.length=" + values.length + //NOI18N
                            " Code.length=" + code.length + " Keys=" + //NOI18N
                            arrToStr(keys) + " Values=" + arrToStr(values) +
                            " Code=" + arrToStr(code));  //NOI18N
                        }
                    }
                }
            }
        }
    }
    
    private static final String arrToStr(int[] s) {
        if (s == null) return "null"; //NOI18N
        StringBuilder out = new StringBuilder(s.length * 3);
        for (int i=0; i < s.length; i++) {
            out.append(s[i]);
            if (i != s.length-1) {
                out.append(','); //NOI18N
            }
        }
        return out.toString();
    }
    
    public String getAsText() {
        Integer i = (Integer) getValue();
        String result;
        if (i != null) {
            if (keys != null) {
                int intVal = i.intValue();
                int idx = -1;
                for (int j=0; j < values.length; j++) {
                    if (values[j] == intVal) {
                        idx = j;
                        break;
                    }
                }
                if (idx != -1) {
                    result = keys [idx];
                } else {
                    throw new IllegalArgumentException(
                    "This property editor uses a set of keyed values, " +  //NOI18N
                    "and the current value, " //NOI18N
                    + i + ", is not specified."); //NOI18N
                }
            } else {
                result = getValue().toString();
            }
        } else {
            result = NbBundle.getMessage (IntEditor.class, "NULL"); //NOI18N
        }
        return result;
    }
    
    private void doSetAsText(String s) {
        //fixes issue 23077, confusing error message from NFE.
        //IllegalArgumentException is a more correct exception to throw
        //anyway
        try {
            setValue(Integer.valueOf(s));
        } catch (NumberFormatException nfe) {
            String msg = NbBundle.getMessage(
                IntEditor.class, "EXC_ILLEGAL_VALUE_TEXT") + s; //NOI18N
            RuntimeException iae = new IllegalArgumentException(msg); //NOI18N
            UIExceptions.annotateUser(iae, msg, msg, nfe, new java.util.Date());
            throw iae;
        }
    }
    
    public void setAsText(String s) {
        s = s.trim();
        if (keys == null) {
            doSetAsText(s);
        } else {
            //use the keys, translate to an int value
            int idx = Arrays.asList(keys).indexOf(s);
            if ((idx == -1) || (idx > values.length-1)) {
                StringBuilder msg = new StringBuilder();
                msg.append(NbBundle.getMessage(IntEditor.class,
                    "EXC_ILLEGAL_STRING_TEXT_FIRST")); //NOI18N
                msg.append(s);
                msg.append(NbBundle.getMessage(IntEditor.class,
                    "EXC_ILLEGAL_STRING_TEXT_SECOND")); //NOI18N
                msg.append(arrToStr(keys));
                String message = msg.toString();
                RuntimeException iae = new IllegalArgumentException(message);
                UIExceptions.annotateUser(iae, message, message, iae,
                                         new java.util.Date());
                throw iae;
            } else {
                setValue(Integer.valueOf(values[idx]));
            }
        }
    }
    
    //issue 34037 - make setValue calls with illegal values fail-fast
    public void setValue (Object value) {
        if ((value instanceof Integer) || (value == null)) {
            super.setValue (value);
        } else {
            throw new IllegalArgumentException (
                "Argument to IntEditor.setValue() must be Integer, but was " + //NOI18N
                value.getClass().getName() + "(=" +  //NOI18N
                value.toString() + ")"); //NOI18N
        }
    }
    
    public String[] getTags() {
        return keys;
    }
    
    public String getJavaInitializationString() {
        String result;
        if (code == null) {
            result = getValue().toString();
        } else {
            result = code[((Integer) getValue()).intValue()];
        }
        return result;
    }
}
