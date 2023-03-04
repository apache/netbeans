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

package org.netbeans.modules.profiler.actions;

import java.awt.event.ActionEvent;
import javax.swing.*;
import org.netbeans.modules.profiler.v2.ProfilerSession;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 * Action to start profiler attach
 *
 * @author Ian Formanek
 */
@NbBundle.Messages({
    "LBL_AttachAction=Attach to &External Process",
    "HINT_AttachAction=Attach to External Process"
})
public final class AttachAction extends AbstractAction {
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    private static final class Singleton {
        private static final AttachAction INSTANCE = new AttachAction();
    }
    
    private AttachAction() {
        putValue(Action.NAME, Bundle.LBL_AttachAction());
        putValue(Action.SHORT_DESCRIPTION, Bundle.HINT_AttachAction());
    }
    
    public static AttachAction getInstance() {
        return Singleton.INSTANCE;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(final ActionEvent e) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                ProfilerSession session = ProfilerSession.forContext(Lookup.EMPTY);
                if (session != null) {
                    session.setAttach(true);
                    session.open();
                }
            }
        });
    }
}
