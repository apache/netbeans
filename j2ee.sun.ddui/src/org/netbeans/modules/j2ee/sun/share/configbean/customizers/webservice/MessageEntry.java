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
 * MessageEntry.java
 *
 * Created on May 17, 2006, 6:28 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice;

import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.common.JavaMethod;
import org.netbeans.modules.j2ee.sun.dd.api.common.Message;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurity;
import org.netbeans.modules.j2ee.sun.dd.api.common.MethodParams;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;

import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableModel;
import org.openide.util.NbBundle;


/**
 *
 * @author  Peter Williams
 */
public class MessageEntry extends GenericTableModel.TableEntry {

    public static final String OPERATION_ENTRY = "OperationName"; // NOI18N
    public static final String METHOD_ENTRY = "MethodName"; // NOI18N
    
    /** Pattern to split a string representing a JavaMethod up into it's component parts.
     *  This simply breaks the string up on whitespace, commas, left/right parentheses
     *  and left/right square brackets.
     */
    public static final Pattern methodSplitter = Pattern.compile("[\\s,\\(\\)\\[\\]]+"); // NOI18N
    
    private boolean saveAsOperation;
    
    public MessageEntry(boolean useOperations) {
        super(null, MessageSecurity.MESSAGE, NbBundle.getBundle(
                "org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice.Bundle"),
                useOperations ? OPERATION_ENTRY : METHOD_ENTRY, true, true);
        
        saveAsOperation = useOperations;
    }

    public Object getEntry(CommonDDBean parent) {
        Object result = null;

        CommonDDBean [] message = (CommonDDBean []) parent.getValues(propertyName);
        if(message != null && message.length > 0 && message[0] != null) {
            if(saveAsOperation) {
                result = message[0].getValue(Message.OPERATION_NAME);
            } else {
                result = methodToString((JavaMethod) message[0].getValue(Message.JAVA_METHOD));
                if(!Utils.notEmpty((String) result)) {
                    result = message[0].getValue(Message.OPERATION_NAME);
                }
            }
        }

        return result;
    }
    
    public void setEntry(CommonDDBean parent, Object value) {
        // Set blank strings to null.  This object also handles message-security-binding
        // though, so we have to check it out.        
        if(value instanceof String && ((String) value).length() == 0) {
            value = null;
        }

        CommonDDBean [] m = (CommonDDBean []) parent.getValues(propertyName); 
        if(value != null) {
            if(m == null || m.length == 0 || m[0] == null) {
                MessageSecurity ms = (MessageSecurity) parent;
                m = new Message [] { ms.newMessage() };
                parent.setValue(propertyName, m);
            }

            if(saveAsOperation) {
                m[0].setValue(Message.OPERATION_NAME, value);
            } else {
                m[0].setValue(Message.JAVA_METHOD, stringToMethod((Message) m[0], value.toString()));
            }
        } else {
            if(m != null) {
                parent.setValue(propertyName, null);
            }
        }
    }

    public Object getEntry(CommonDDBean parent, int row) {
        throw new UnsupportedOperationException();
    }	

    public void setEntry(CommonDDBean parent, int row, Object value) {
        throw new UnsupportedOperationException();
    }
    
    private String methodToString(JavaMethod jm) {
        StringBuffer buf = new StringBuffer(128);

        if(jm != null) {
            buf.append(jm.getMethodName());
            
            MethodParams mps = jm.getMethodParams();
            if(mps != null && mps.sizeMethodParam() > 0) {
                buf.append("(");
                String [] params = mps.getMethodParam();
                for(int i = 0; i < params.length; i++) {
                    if(i > 0) {
                        buf.append(", ");
                    }
                    
                    if(Utils.notEmpty(params[i])) {
                        buf.append(params[i]);
                    } else {
                        buf.append(" ");
                    }
                }
                buf.append(")");
            }
        }
        
        return buf.toString();
    }
    
    private JavaMethod stringToMethod(Message m, String methodDesc) {
        JavaMethod method = null;
        
        if(Utils.notEmpty(methodDesc)) {
            String [] parts = methodSplitter.split(methodDesc);
            if(parts.length > 0) {
                method = m.newJavaMethod();
                method.setMethodName(parts[0]);
                
                if(parts.length > 1) {
                    MethodParams mps = method.newMethodParams();
                    for(int i = 1; i < parts.length; i++) {
                        mps.addMethodParam(parts[i]);
                    }
                    method.setMethodParams(mps);
                }
            }
        }
        
        return method;
    }
}
