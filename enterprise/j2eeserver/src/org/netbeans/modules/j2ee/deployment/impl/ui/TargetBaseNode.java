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

package org.netbeans.modules.j2ee.deployment.impl.ui;

import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.netbeans.modules.j2ee.deployment.impl.ServerTarget;
import javax.swing.Action;


/**
 * Target base node is a base for any target node. The behaviour of this target
 * base node can be customized/extended by the target node provided by the plugin.
 *
 * @author Nam Nguyen
 */
public class TargetBaseNode extends AbstractNode {

    public TargetBaseNode(Children children, ServerTarget target) {
		super(children);
        setDisplayName(target.getName());
        setIconBase(target.getInstance().getServer().getIconBase());
        getCookieSet().add(target);
    }
    
    public Action[] getActions(boolean b) {
        return new Action[] {};
    }
    
    protected ServerTarget getServerTarget() {
        return (ServerTarget) getCookie(ServerTarget.class);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public Sheet createSheet() {
        return Sheet.createDefault();
    }
}
