/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.xml.axi.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.xml.axi.AXIComponent;

/**
 * The preview class encapsulates a collection of global elements,
 * used by other global elements.
 *
 * @author Ayub Khan
 */
public class Preview {
    
    Map<AXIComponent, java.util.List<AXIComponent>> pmap =
            new HashMap<AXIComponent, java.util.List<AXIComponent>>();
    Map<AXIComponent, java.util.List<AXIComponent>> reversemap =
            new HashMap<AXIComponent, java.util.List<AXIComponent>>();
    
    /** Creates a new instance of Preview */
    public Preview() {
    }
    
    /**
     * Returns a collection of schema components, all of which,
     * reference the same global schema component.
     */
    public Map<AXIComponent, java.util.List<AXIComponent>> getUsages() {
        return pmap;
    }
    
    /**
     * Returns a collection of schema components, all of which,
     * reference the same global schema component.
     */
    public Map<AXIComponent, java.util.List<AXIComponent>> getReverseUsages() {
        return reversemap;
    }
    
    public void addToUsage(AXIComponent c, AXIComponent usedBy) {
        java.util.List<AXIComponent> l = pmap.get(c);
        if(l == null) {
            l = new ArrayList<AXIComponent>();
            pmap.put(c, l);
        }
        l.add(usedBy);
        
        addToReverseUsage(c, usedBy);
    }
    
    public void addToReverseUsage(AXIComponent c, AXIComponent usedBy) {
        java.util.List<AXIComponent> r = reversemap.get(usedBy);
        if(r == null) {
            r = new ArrayList<AXIComponent>();
            reversemap.put(usedBy, r);
        }
        r.add(c);
    }
}
