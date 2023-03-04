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

package org.netbeans.modules.project.ui.groups;

import java.io.PrintStream;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Env;
import org.openide.util.NbBundle.Messages;
import static org.netbeans.modules.project.ui.groups.Bundle.*;
import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.ArgsProcessor;

public class GroupOptionProcessor implements ArgsProcessor {
    @Arg(longName="open-group")
    @Description(
        displayName="#GroupOptionProcessor.open.name",
        shortDescription="#GroupOptionProcessor.open.desc"
    )
    @Messages({
        "GroupOptionProcessor.open.name=--open-group NAME",
        "GroupOptionProcessor.open.desc=open a project group by shortened or full name (or unique substring)"
    })
    public String openOption;
    @Arg(longName="close-group")
    @Description(
        shortDescription="#GroupOptionProcessor.close.desc"
    )
    @Messages("GroupOptionProcessor.close.desc=close any open project group")
    public boolean closeOption;

    @Arg(longName="list-groups")
    @Description(
        shortDescription="#GroupOptionProcessor.list.desc"
    )
    @Messages("GroupOptionProcessor.list.desc=list available project groups")
    public boolean listOption;

    @Messages({
        "# {0} - name of group", "GroupOptionProcessor.no_such_group=No such group: {0}",
        "GroupOptionProcessor.column_id=Shortened Name",
        "GroupOptionProcessor.column_name=Full Name"
    })
    @Override public void process(Env env) throws CommandException {
        if (openOption != null) {
            Group found = null;
            for (Group g : Group.allGroups()) {
                if (g.id.equals(openOption) || g.getName().equals(openOption)) {
                    found = g;
                    break;
                }
            }
            if (found == null) {
                for (Group g : Group.allGroups()) {
                    if (g.id.contains(openOption) || g.getName().contains(openOption)) {
                        found = g;
                        break;
                    }
                }
            }
            if (found == null) {
                throw new CommandException(2, GroupOptionProcessor_no_such_group(openOption));
            }
            supressWinsysLazyLoading();
            Group.setActiveGroup(found, false);
        } else if (closeOption) {
            supressWinsysLazyLoading();
            Group.setActiveGroup(null, false);
        } else if (listOption) {
            int max_size = GroupOptionProcessor_column_id().length();
            for (Group g : Group.allGroups()) {
                max_size = Math.max(max_size, g.id.length());
            }
            PrintStream ps = env.getOutputStream();
            ps.printf("%-" + max_size + "s  %s\n", GroupOptionProcessor_column_id(), GroupOptionProcessor_column_name());
            for (Group g : Group.allGroups()) {
                ps.printf("%-" + max_size + "s  %s\n", g.id, g.getName());
            }
        }
    }
    
    private void supressWinsysLazyLoading() {
        String oldValue = System.getProperty("nb.core.windows.no.lazy.loading");
        if (oldValue != null) {
            System.setProperty("group.supresses.lazy.loading", oldValue);
        }
        System.setProperty("nb.core.windows.no.lazy.loading", "true");
    }
}
