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

import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.actions.CookieAction;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Action for refresh of file systm
*
* @author Jaroslav Tulach
*/
public final class FileSystemRefreshAction extends CookieAction {

    protected Class<?>[] cookieClasses () {
        return new Class<?>[] {DataFolder.class};
    }

    protected void performAction (Node[] nodes) {
        for (Node n : nodes) {
            DataFolder df = n.getCookie(DataFolder.class);
            if (df != null) {
                FileObject fo = df.getPrimaryFile ();
                fo.refresh ();
            }
        }
    }
    
    protected boolean asynchronous() {
        return true;
    }

    protected int mode () {
        return MODE_ALL;
    }

    public String getName () {
        return NbBundle.getBundle(DataObject.class).getString("LAB_Refresh");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (FileSystemRefreshAction.class);
    }

}
