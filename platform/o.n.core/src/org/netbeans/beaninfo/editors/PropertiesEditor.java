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
    @Override
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
    @Override
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

    @Override
    public String getJavaInitializationString () {
        return null; // does not generate any code
    }

    @Override
    public boolean supportsCustomEditor () {
        return true;
    }

    @Override
    public java.awt.Component getCustomEditor () {
        return new PropertiesCustomEditor (this);
    }

}
