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

package org.netbeans.modules.xml.axi.sync;

import org.netbeans.modules.xml.axi.*;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;

        
/**
 * Abstract sync test case. The sync test cases update the
 * underlying schema model and then syncs up the AXI model
 * based on events obtained from schema model.
 *
 * Note: the AXI model must be initialized. See setUp().
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class AbstractSyncTestCase extends AbstractTestCase {
            
    /**
     * AbstractSyncTestCase
     */
    public AbstractSyncTestCase(String testName,
            String schemaFile, String globalElement) {
        super(testName, schemaFile, globalElement);
    }
        
    GlobalElement findGlobalElement(String name) {
        for(GlobalElement element : getSchemaModel().getSchema().getElements()) {
            if(element.getName().equals(name))
                return element;
        }
        return null;
    }
    
    GlobalComplexType findGlobalComplexType(String name) {
        for(GlobalComplexType type : getSchemaModel().getSchema().getComplexTypes()) {
            if(type.getName().equals(name))
                return type;
        }
        return null;
    }
    
    GlobalGroup findGlobalGroup(String name) {
        for(GlobalGroup group : getSchemaModel().getSchema().getGroups()) {
            if(group.getName().equals(name))
                return group;
        }
        return null;
    }

    GlobalAttribute findGlobalAttribute(String name) {
        for(GlobalAttribute attr : getSchemaModel().getSchema().getAttributes()) {
            if(attr.getName().equals(name))
                return attr;
        }
        return null;
    }
    
    GlobalAttributeGroup findGlobalAttributeGroup(String name) {
        for(GlobalAttributeGroup group : getSchemaModel().getSchema().getAttributeGroups()) {
            if(group.getName().equals(name))
                return group;
        }
        return null;
    }    
}
