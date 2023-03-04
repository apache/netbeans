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

package org.netbeans.modules.selenium2.webclient.ui;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import static javax.swing.Action.NAME;
import static javax.swing.Action.SHORT_DESCRIPTION;
import org.netbeans.modules.selenium2.api.Utils;
import org.netbeans.modules.selenium2.webclient.spi.JumpToCallStackCallback;
import org.openide.util.NbBundle;
import org.openide.util.Pair;



public class JumpToCallStackAction extends AbstractAction {

    private static final long serialVersionUID = -14558324203007090L;

    private final String[] stacktraces;
    private final JumpToCallStackCallback callback;


    @NbBundle.Messages("JumpToCallStackAction.name=&Go to Source")
    public JumpToCallStackAction(String[] stacktraces, JumpToCallStackCallback callback) {
        assert stacktraces != null;
        this.stacktraces = stacktraces;
        this.callback = callback;

        String name = Bundle.JumpToCallStackAction_name();
        putValue(NAME, name);
        putValue(SHORT_DESCRIPTION, name);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (callback != null) {
            // iterate through stacktraces in order to find the correct test case location
            // if stacktraces.length == 1, user probably clicked on a stacktrace node
            // if stacktraces.length > 1, user probably clicked on a failed test method node
            boolean underTestRoot = stacktraces.length > 1;
            for (String callstackFrameInfo : stacktraces) {
                Pair<File, int[]> pair = callback.parseLocation(callstackFrameInfo, underTestRoot);
                if (pair != null) {
                    Utils.openFile(pair.first(), pair.second()[0], pair.second()[1]);
                    return;
                }
            }
        }
    }


}
