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

package org.netbeans.spi.palette;

import javax.swing.AbstractAction;
import javax.swing.Action;


/**
 *
 * @author Stanislav Aubrecht
 */
public class DummyActions extends PaletteActions {

    private Action[] paletteActions = new Action[] { new DummyAction(1), new DummyAction(2), new DummyAction(3) };
    private Action[] categoryActions = new Action[] { new DummyAction(10), new DummyAction(20), new DummyAction(30) };
    private Action[] itemActions = new Action[] { new DummyAction(100), new DummyAction(200), new DummyAction(300) };

    private Action preferredAction;
    
    /** Creates a new instance of DummyActions */
    public DummyActions() {
    }

    public javax.swing.Action getPreferredAction(org.openide.util.Lookup item) {
        return preferredAction;
    }
    
    void setPreferredAction( Action a ) {
        this.preferredAction = a;
    }

    public javax.swing.Action[] getCustomItemActions(org.openide.util.Lookup item) {
        return itemActions;
    }

    public javax.swing.Action[] getCustomCategoryActions(org.openide.util.Lookup category) {
        return categoryActions;
    }

    public javax.swing.Action[] getImportActions() {
        return null;
    }

    public javax.swing.Action[] getCustomPaletteActions() {
        return paletteActions;
    }
    
    private static class DummyAction extends AbstractAction {
        public DummyAction( int id ) {
            super( "Action_" + id );
        }

        public void actionPerformed(java.awt.event.ActionEvent e) {
            System.out.println( "Action " + getValue( Action.NAME ) + " invoked." );
        }
    }
}
