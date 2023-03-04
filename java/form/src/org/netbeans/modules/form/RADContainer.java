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


package org.netbeans.modules.form;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ian Formanek
 */
public class RADContainer extends RADComponent implements ComponentContainer {
    private List<RADComponent> subComponents;

    @Override
    public RADComponent[] getSubBeans() {
        RADComponent[] components = new RADComponent [subComponents.size()];
        subComponents.toArray(components);
        return components;
    }

    @Override
    public void initSubComponents(RADComponent[] initComponents) {
        subComponents = new ArrayList<RADComponent>(initComponents.length);
        for (int i = 0; i < initComponents.length; i++) {
            subComponents.add(initComponents[i]);
            initComponents[i].setParentComponent(this);
        }
    }

    @Override
    public void reorderSubComponents(int[] perm) {
        RADComponent[] components = new RADComponent[subComponents.size()];
        for (int i=0; i < perm.length; i++)
            components[perm[i]] = subComponents.get(i);

        subComponents.clear();
        subComponents.addAll(java.util.Arrays.asList(components));
    }

    @Override
    public void add(RADComponent comp) {
        subComponents.add(comp);
        comp.setParentComponent(this);
    }

    @Override
    public void remove(RADComponent comp) {
        if (subComponents.remove(comp))
            comp.setParentComponent(null);
    }

    @Override
    public int getIndexOf(RADComponent comp) {
        return subComponents.indexOf(comp);
    }

    /**
     * Called to obtain a Java code to be used to generate code to access the
     * container for adding subcomponents.  It is expected that the returned
     * code is either ""(in which case the form is the container) or is a name
     * of variable or method call ending with
     * "."(e.g. "container.getContentPane().").  This implementation returns
     * "", as there is no sense to add visual components to non-visual
     * containers
     * @return the prefix code for generating code to add subcomponents to this container
     */
    public String getContainerGenName() {
        return ""; // NOI18N
    }
}
