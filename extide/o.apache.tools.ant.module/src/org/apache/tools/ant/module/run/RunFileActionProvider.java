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

package org.apache.tools.ant.module.run;

import java.io.IOException;
import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.support.TargetLister;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Permits Run File to work on Ant script and target nodes.
 */
@ServiceProvider(service=ActionProvider.class)
public class RunFileActionProvider implements ActionProvider {

    @Override public String[] getSupportedActions() {
        return new String[] {ActionProvider.COMMAND_RUN_SINGLE};
    }

    @Override public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        return context.lookup(AntProjectCookie.class) != null || context.lookup(TargetLister.Target.class) != null;
    }

    @Override public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        AntProjectCookie apc;
        String[] targets;
        TargetLister.Target target = context.lookup(TargetLister.Target.class);
        if (target != null) {
            apc = target.getOriginatingScript();
            targets = new String[] {target.getName()};
        } else {
            apc = context.lookup(AntProjectCookie.class);
            targets = null;
        }
        try {
            new TargetExecutor(apc, targets).execute();
        } catch (IOException ioe) {
            AntModule.err.notify(ioe);
        }
    }

}
