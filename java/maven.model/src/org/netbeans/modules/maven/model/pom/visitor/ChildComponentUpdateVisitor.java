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
package org.netbeans.modules.maven.model.pom.visitor;

import org.netbeans.modules.maven.model.pom.*;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;

/**
 * Visitor to add or remove a child of a domain component.
 * 
 * @author mkleint
 */
public class ChildComponentUpdateVisitor<T extends POMComponent> //implements SCAComponentVisitor, 
        implements ComponentUpdater<T> {
    
    private POMComponent parent;
    private int index;
    private boolean canAdd = false;
    
    /**
     * Creates a new instance of ChildComponentUpdateVisitor
     */
    public ChildComponentUpdateVisitor() {
    }
    
    public boolean canAdd(POMComponent target, Component child) {
        if (!(child instanceof POMComponent)) return false;
        update(target, (POMComponent) child, null);
        return canAdd;
    }
    
    @Override
    public void update(POMComponent target, POMComponent child, Operation operation) {
        update(target, child, -1, operation);
    }
    
    @Override
    public void update(POMComponent target, POMComponent child, int index, Operation operation) {
        assert target != null;
        assert child != null;

        this.parent = target;
        this.index = index;
        //#165465
        if (operation != null) {
            if (operation == Operation.REMOVE) {
                //TODO what property shall be fired? is it important?
                removeChild("XXX", child);
            } else {
                //TODO what property shall be fired? is it important?
                addChild("XXX", child);
            }
        }
//        child.accept(this);
    }
    
    private void addChild(String eventName, DocumentComponent child) {
        ((AbstractComponent) parent).insertAtIndex(eventName, child, index);
    }
    
    private void removeChild(String eventName, DocumentComponent child) {
        ((AbstractComponent) parent).removeChild(eventName, child);
    }

}
