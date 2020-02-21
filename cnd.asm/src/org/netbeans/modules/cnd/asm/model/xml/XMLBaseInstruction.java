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

    
package org.netbeans.modules.cnd.asm.model.xml;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.netbeans.modules.cnd.asm.model.lang.instruction.Instruction;
import org.netbeans.modules.cnd.asm.model.lang.instruction.InstructionArgs;

public abstract class XMLBaseInstruction implements Instruction {
    
    private final String name;
    private final String groupName;
    private final String description;
    private final Collection<InstructionArgs> args;
    
    private Map<String, String> propMap;
    
    public XMLBaseInstruction(String name, String groupName, String description, 
                              Collection<InstructionArgs> args) {
        this.name = name;
        this.groupName = groupName;
        this.args = args;
        this.description = description;
    }    
    
    public String getName() {
        return name;
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Collection<InstructionArgs> getArguments() {
        return args;
    }
    
    public String getProperty(String name) {
        if (propMap != null) {
            return propMap.get(name);
        }
        
        return null;
    }

    @Override
    public String toString() {
        return name + " " + description; // NOI18N
    }
    
    protected void setProperty(String name, String value) {
        try {            
            String t = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1); // NOI18N
            Method m = this.getClass().getMethod(t, String.class);                        
            m.invoke(this, value);            
        } catch (Exception ex) {
            setMap(name, value);
        }
    }        
    
    private void setMap(String name, String value) {
        // Note: it's lazy method used only in initialiation => 
        // not thread safe !!! 
        if (propMap == null) {
            propMap = new HashMap<String, String>();
        }
        
        propMap.put(name, value);
    }

}
