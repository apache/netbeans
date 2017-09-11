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


package org.netbeans.modules.i18n.java;

import java.util.Map;
import org.netbeans.modules.i18n.I18nString;
import org.netbeans.modules.i18n.I18nSupport;

/**
 * This is <code>I18nString</code> for java sources.
 *
 * @author  Peter Zavadsky
 * @author  Petr Kuzel
 */
public class JavaI18nString extends I18nString {

    /**
     * Arguments used by creation replacing code enclapsulating
     * in java.util.MessageFormat.format method call.
     */
    protected String[] arguments;

    /** Creates 'empty' <code>JavaI18nString</code>.*/
    public JavaI18nString(I18nSupport i18nSupport) {
        super(i18nSupport);
    }

    /**
     * Copy contructor.
     */
    protected JavaI18nString(JavaI18nString copy) {
        super(copy);
        if (copy.arguments == null) {
            return;
        }
        this.arguments = copy.arguments.clone();
    }
    
    @Override
    public void become(I18nString i18nString) {
        super.become(i18nString);        
        if(i18nString instanceof JavaI18nString) {
            JavaI18nString peer = (JavaI18nString) i18nString;
            this.setArguments(peer.arguments);    
        }        
    }   
    
    @Deprecated
    public void become(JavaI18nString i18nString) {
        super.become(i18nString);        
        
        JavaI18nString peer = i18nString;
            this.setArguments(peer.arguments);    
        }        
    
    @Override
    public Object clone() {
        return new JavaI18nString(this);
    }
    
    /** Getter for property arguments.
     * @return Value of property arguments.
     */
    public String[] getArguments() {
        if (arguments == null) {
            arguments = new String[0];
        }
        return arguments;
    }
    
    /** Setter for property arguments.
     * @param arguments New value of property arguments.
     */
    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }
    
    /** 
     * Add java specific replacing values. 
     */
    @Override
    protected void fillFormatMap(Map<String,String> map) {
        map.put("identifier", ((JavaI18nSupport) getSupport()).getIdentifier()); // NOI18N

        // Arguments.
        String[] arguments = getArguments();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("new Object[] {"); // NOI18N
        
        for(int i=0; i<arguments.length; i++) {
            stringBuffer.append(arguments[i]);
            
            if(i<arguments.length - 1)
                stringBuffer.append(", "); // NOI18N
        }
        
        stringBuffer.append("}"); // NOI18N
        
        map.put("arguments", stringBuffer.toString());
    }
    
}
