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

package org.netbeans.modules.editor.guards;

import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.editor.guards.InteriorSection;
import org.netbeans.api.editor.guards.SimpleSection;

/**
 *
 * @author Jan Pokorsky
 */
public abstract class GuardsAccessor {
    
    public static GuardsAccessor DEFAULT;
    
    static {
        Class<?> clazz = GuardedSectionManager.class;
        try {
            Class.forName(clazz.getName(), true, clazz.getClassLoader());
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
    }
    
    public abstract GuardedSectionManager createGuardedSections(GuardedSectionsImpl impl);
    
    public abstract SimpleSection createSimpleSection(SimpleSectionImpl impl);
    
    public abstract InteriorSection createInteriorSection(InteriorSectionImpl impl);
    
    public abstract GuardedSectionImpl getImpl(GuardedSection gs);
    
    public abstract GuardedSection clone(GuardedSection gs, int offset);
    
}
