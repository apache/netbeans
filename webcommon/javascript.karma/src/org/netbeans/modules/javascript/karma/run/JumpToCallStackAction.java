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

package org.netbeans.modules.javascript.karma.run;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.javascript.karma.util.FileUtils;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

public class JumpToCallStackAction extends AbstractAction {

    private static final long serialVersionUID = -14558324203007090L;

    private final String callstackFrameInfo;
    private final Callback callback;


    @NbBundle.Messages("JumpToCallStackAction.name=&Go to Source")
    public JumpToCallStackAction(String callstackFrameInfo, Callback callback) {
        assert callstackFrameInfo != null;
        this.callstackFrameInfo = callstackFrameInfo;
        this.callback = callback;

        String name = Bundle.JumpToCallStackAction_name();
        putValue(NAME, name);
        putValue(SHORT_DESCRIPTION, name);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (callback != null) {
            Pair<File, Integer> pair = callback.parseLocation(callstackFrameInfo);
            if (pair != null) {
                FileUtils.openFile(pair.first(), pair.second());
            }
        }
    }

    //~ Inner classes

    public interface Callback {
        @CheckForNull
        Pair<File, Integer> parseLocation(String callStack);
    }

}
