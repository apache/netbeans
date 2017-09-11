/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.project.ui.groups;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Env;
import org.openide.util.NbBundle.Messages;
import static org.netbeans.modules.project.ui.groups.Bundle.*;
import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

public class GroupOptionProcessor implements ArgsProcessor {
    @Arg(longName="open-group")
    @Description(
        displayName="#GroupOptionProcessor.open.name",
        shortDescription="#GroupOptionProcessor.open.desc"
    )
    @Messages({
        "GroupOptionProcessor.open.name=--open-group NAME",
        "GroupOptionProcessor.open.desc=open a project group by name"
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
            for (Group g : Group.allGroups()) {
                if (g.id.equals(openOption) || g.getName().equals(openOption)) {
                    supressWinsysLazyLoading();
                    Group.setActiveGroup(g, false);
                    return;
                }
            }
            throw new CommandException(2, GroupOptionProcessor_no_such_group(openOption));
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
