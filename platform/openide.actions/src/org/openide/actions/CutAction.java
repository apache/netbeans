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


/** Cut an object to the clipboard.
*
* @author   Petr Hamernik, Ian Formanek
*/
public class CutAction extends CallbackSystemAction {
    protected void initialize() {
        super.initialize();
    }

    public Object getActionMapKey() {
        return javax.swing.text.DefaultEditorKit.cutAction;
    }

    public String getName() {
        return NbBundle.getMessage(CutAction.class, "Cut");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(CutAction.class);
    }

    protected String iconResource() {
        return "org/openide/resources/actions/cut.gif"; // NOI18N
    }

    protected boolean asynchronous() {
        return false;
    }
}
