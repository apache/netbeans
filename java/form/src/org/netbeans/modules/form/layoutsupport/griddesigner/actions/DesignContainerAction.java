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

package org.netbeans.modules.form.layoutsupport.griddesigner.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.form.RADVisualContainer;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridDesigner;
import org.openide.util.NbBundle;

/**
 * Action that changes the designed container.
 *
 * @author Jan Stola
 */
public class DesignContainerAction extends AbstractAction {
    /** Designer whose designed component should be changed. */
    private GridDesigner designer;
    /** New designed container. */
    private RADVisualContainer container;

    /**
     * Creates a new {@code DesignContainerAction}.
     * 
     * @param designer designer whose designed component should be changed.
     * @param container new designed container.
     * @param parent determines whether the new designed container
     * is a parent container or a subcomponent.
     */
    public DesignContainerAction(GridDesigner designer, RADVisualContainer container, boolean parent) {
        this.designer = designer;
        this.container = container;
        String name = NbBundle.getMessage(DesignContainerAction.class,
                parent ? "DesignParentContainer_Name" : "DesignThisContainer_Name"); // NOI18N
        putValue(Action.NAME, name);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        designer.setDesignedContainer(container);
        designer.revalidate();
        designer.repaint();
    }

}
