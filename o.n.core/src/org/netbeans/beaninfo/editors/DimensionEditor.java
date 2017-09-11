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

import java.awt.Dimension;
import org.netbeans.core.UIExceptions;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.util.NbBundle;

/** A property editor for Dimension class.
* @author   Petr Hamernik
* @version  0.10, 21 Jul, 1998
*/
public class DimensionEditor extends ArrayOfIntSupport {
    public DimensionEditor() {
        super("java.awt.Dimension", 2); // NOI18N
    }

    /** Abstract method for translating the value from getValue() method to array of int. */
    int[] getValues() {
        Dimension d = (Dimension) getValue();
        return new int[] { d.width, d.height };
    }

    static String toArr (int[] ints) {
        StringBuilder sb = new StringBuilder();
        if ((ints != null) && (ints.length > 0)) {
            for (int i=0; i < ints.length; i++) {
                sb.append (ints[i]);
                if (i != ints.length-1) {
                    sb.append (','); //NOI18N
                }
            }
        } else {
            return NbBundle.getMessage (DimensionEditor.class,
                "MSG_NULL_OR_EMPTY"); //NOI18N
        }
        return sb.toString();
    }
    
    /** Abstract method for translating the array of int to value
    * which is set to method setValue(XXX)
    */
    void setValues(int[] val) {
        if ((val[0] < 0) || (val[1] < 0)) {
            String msg = NbBundle.getMessage(DimensionEditor.class, 
                "CTL_NegativeSize"); //NOI18N
            IllegalArgumentException iae = new IllegalArgumentException (
                "Negative value"); //NOI18N
            UIExceptions.annotateUser(iae, iae.getMessage(), msg, null,
                                     new java.util.Date());
            throw iae;
        }
        else
            setValue(new Dimension(val[0], val[1]));
    }

    public boolean supportsCustomEditor () {
        return true;
    }

    public java.awt.Component getCustomEditor () {
        return new PointCustomEditor (this, env);
    }


    /** @return the format of value set in property editor. */
    String getHintFormat() {
        return NbBundle.getMessage(DimensionEditor.class, "CTL_HintFormat"); //NOI18N
    }

    /** Provides name of XML tag to use for XML persistence of the property value */
    protected String getXMLValueTag () {
        return "Dimension"; // NOI18N
    }

}
