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

package org.netbeans.modules.j2ee.persistence.unit;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Dialog for adding entities into persistence unit.
 *
 * @author Erno Mononen
 */
public class AddEntityDialog {
    
    /**
     * Opens dialog for adding entities.
     * @return fully qualified names of the selected entities' classes.
     */
    public static List<String> open(EntityClassScope entityClassScope, Set<String> ignoreClassNames){
        AddEntityPanel panel = AddEntityPanel.create(entityClassScope, ignoreClassNames);
        
        final DialogDescriptor nd = new DialogDescriptor(
                panel,
                NbBundle.getMessage(AddEntityDialog.class, "LBL_AddEntity"),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(AddEntityPanel.class),
                null
                );
        
        Object button = DialogDisplayer.getDefault().notify(nd);
        if (button != NotifyDescriptor.OK_OPTION) {
            return Collections.emptyList();
        }
        
        return Collections.unmodifiableList(panel.getSelectedEntityClasses());
    }
    
}
