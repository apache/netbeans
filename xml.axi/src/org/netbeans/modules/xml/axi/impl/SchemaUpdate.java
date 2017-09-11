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

package org.netbeans.modules.xml.axi.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.impl.SchemaUpdate.UpdateUnit.Type;

/**
 *
 * @author Ayub Khan
 */
public class SchemaUpdate {
    
    /** Creates a new instance of SchemaUpdate */
    public SchemaUpdate() {
    }
    
    public Collection<UpdateUnit> getUpdateUnits() {
        return Collections.unmodifiableList(units);
    }
    
    public void addUpdateUnit(UpdateUnit uu) {
        units.add(uu);
    }
    
    public UpdateUnit createUpdateUnit(Type type,
            AXIComponent source, Object oldValue, Object newValue, String propertyName) {
        AXIComponent key = null;
        if(type == UpdateUnit.Type.CHILD_MODIFIED)
            key = source;
        else if(type == UpdateUnit.Type.CHILD_ADDED)
            key = (AXIComponent) newValue;
        else if(type == UpdateUnit.Type.CHILD_DELETED)
            key = (AXIComponent) oldValue;
        
        if(key instanceof AXIComponentProxy) {
            key = key.getOriginal();
        }
        if(key != null) {
            List<AXIComponent> items = uniqueMap.get(key);
            if(items == null) {
                items = new ArrayList();
                uniqueMap.put(key, items);
            }
            items.add(key);
            return new UpdateUnit(String.valueOf(count++), type, source, oldValue, newValue,
                    propertyName);
        }
        return null;
    }
    
    public static class UpdateUnit {
        
        public static enum Type {CHILD_ADDED, CHILD_DELETED, CHILD_MODIFIED};
        
        private String id;
        
        private Type type;
        
        private AXIComponent source;
        
        private Object oldValue;
        
        private Object newValue;
        
        private String propertyName;
        
        public UpdateUnit(String id, Type type,
                AXIComponent source, Object oldValue, Object newValue,
                String propertyName) {
            this.id = id;
            this.type = type;
            this.source = source;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.propertyName = propertyName;
        }
        
        public String getId() {
            return id;
        }
        
        public AXIComponent getSource() {
            return source;
        }
        
        public Type getType() {
            return type;
        }
        
        public Object getOldValue() {
            return oldValue;
        }
        
        public Object getNewValue() {
            return newValue;
        }
        
        public String getPropertyName() {
            return propertyName;
        }
    }
    
    private List<UpdateUnit> units = new ArrayList<UpdateUnit>();
    
    private HashMap<AXIComponent, List<AXIComponent>> uniqueMap =
            new HashMap<AXIComponent, List<AXIComponent>>();
    
    private int count = 0;
}
