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
