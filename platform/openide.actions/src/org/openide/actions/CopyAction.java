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
package org.openide.actions;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallbackSystemAction;


/** Copy the selected item to the clipboard. As callback action this
* class cooperate with other actions placed in JComponent ActionMap.
* As a key to the ActionMap the <code>javax.swing.text.DefaultEditorKit.copyAction</code>
* is used.
*
* @author   Petr Hamernik, Ian Formanek
*/
public class CopyAction extends CallbackSystemAction {
    protected void initialize() {
        super.initialize();
    }

    public Object getActionMapKey() {
        return javax.swing.text.DefaultEditorKit.copyAction;
    }

    public String getName() {
        return NbBundle.getMessage(CopyAction.class, "Copy");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(CopyAction.class);
    }

    protected String iconResource() {
        return "org/openide/resources/actions/copy.gif"; // NOI18N
    }

    protected boolean asynchronous() {
        return false;
    }
}
