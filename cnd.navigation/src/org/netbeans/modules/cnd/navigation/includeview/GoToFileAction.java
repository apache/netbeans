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

package org.netbeans.modules.cnd.navigation.includeview;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.util.NbBundle;

/**
 */
public class GoToFileAction extends AbstractAction {
    
    private final CsmObject csmObject;
    private final Action delegate;
    
    public GoToFileAction(CsmObject csmObject, Action delegate) {
        this.csmObject = csmObject;
        this.delegate = delegate;
        putValue(Action.NAME, NbBundle.getMessage(GoToFileAction.class, "LBL_GoToFile")); //NOI18N
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (delegate != null){
            delegate.actionPerformed(e);
        }
        final String taskName = "Open file"; //NOI18N
        Runnable run = new Runnable() {

            @Override
            public void run() {
                CsmUtilities.openSource(csmObject);
            }
        };
        CsmModelAccessor.getModel().enqueue(run, taskName);
    }
}
