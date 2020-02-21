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

package org.netbeans.modules.cnd.remote.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.netbeans.api.javahelp.Help;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
public class RemoteHelpAction  extends AbstractAction {

    public RemoteHelpAction() {
        super(NbBundle.getMessage(RemoteHelpAction.class, "Menu/Help/RemoteDevelopmentItem"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Help help = Lookup.getDefault().lookup(Help.class);
        if (help != null) {
            help.showHelp(new HelpCtx("remote-dd-about"));
        }        
    }
}
