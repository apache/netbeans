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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.beans.PropertyEditorSupport;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import org.netbeans.core.UIExceptions;
import org.openide.util.NbBundle;


/** A property editor for Properties class.
* @author   Ian Formanek
*/
public class PropertiesEditor extends PropertyEditorSupport {

    /** Overrides superclass method. */
    public String getAsText() {
        Object value = getValue();
        
        if(value instanceof Properties) {
            Properties prop = (Properties)value;

            StringBuilder buff = new StringBuilder();
            
            for(Enumeration e = prop.keys(); e.hasMoreElements(); ) {
                if(buff.length() > 0) {
                    buff.append("; "); // NOI18N
                }
                
                Object key = e.nextElement();
                
                buff.append(key).append('=').append(prop.get(key)); // NOI18N
            }
            
            return buff.toString();
        }
        
        return String.valueOf(value); // NOI18N
    }

    /** Overrides superclass method.
     * @exception IllegalArgumentException if <code>null</code> value
     * is passes in or some io problem by converting occured */
    public void setAsText(String text) throws IllegalArgumentException {
        try {
            if(text == null) {
                throw new IllegalArgumentException("Inserted value can't be null."); // NOI18N
            }
            Properties prop = new Properties();
            InputStream is = new ByteArrayInputStream(
                text.replace(';', '\n').getBytes("ISO8859_1") // NOI18N
            );
            prop.load(is);
            setValue(prop);
        } catch(IOException ioe) {
            IllegalArgumentException iae = new IllegalArgumentException (ioe.getMessage());
            String msg = ioe.getLocalizedMessage();
            if (msg == null) {
                msg = MessageFormat.format(
                NbBundle.getMessage(
                    PropertiesEditor.class, "FMT_EXC_GENERIC_BAD_VALUE"), new Object[] {text}); //NOI18N
            }
            UIExceptions.annotateUser(iae, iae.getMessage(), msg, ioe, new Date());
            throw iae;
        }
    }

    public String getJavaInitializationString () {
        return null; // does not generate any code
    }

    public boolean supportsCustomEditor () {
        return true;
    }

    public java.awt.Component getCustomEditor () {
        return new PropertiesCustomEditor (this);
    }

}
