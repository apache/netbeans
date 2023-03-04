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
package org.netbeans.modules.maven.model.pom;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;

/**
 *
 * @author mkleint
 */
public abstract class POMModel extends AbstractDocumentModel<POMComponent> { 
    
    protected POMModel(ModelSource source) {
        super(source);
    }
    
    public abstract POMComponentFactory getFactory();

    public abstract POMQNames getPOMQNames();

    /**
     * Gets domain-specific root component.
     */
    public abstract Project getProject();

    @Override
    public void refresh() {
        super.refresh();
    }
    
    public <T/* extends POMComponent is this safe to add?*/> T findComponent(int position, Class<T> clazz, boolean recursive) {
        Component dc = findComponent(position);
        while (dc != null) {
            if (clazz.isAssignableFrom(dc.getClass())) {
                return (T) dc;
            }
            if (recursive) {
                dc = dc.getParent(); 
            } else {
                dc = null;
            }
        }
        return null;
    }

}
