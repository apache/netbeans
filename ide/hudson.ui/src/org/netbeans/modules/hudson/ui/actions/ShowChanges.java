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

package org.netbeans.modules.hudson.ui.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.spi.HudsonJobChangeItem;
import org.netbeans.modules.hudson.spi.HudsonJobChangeItem.HudsonJobChangeFile;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 * Displays changelog for a build.
 */
public class ShowChanges extends AbstractAction implements Runnable {

    private static final Logger LOG = Logger.getLogger(ShowChanges.class.getName());

    private final HudsonJobBuild build;

    public ShowChanges(HudsonJobBuild build) {
        super(NbBundle.getMessage(ShowChanges.class, "ShowChanges.label"));
        this.build = build;
    }

    public void actionPerformed(ActionEvent e) {
        new RequestProcessor(build + "changes").post(this); // NOI18N
    }

    public void run() {
        String name = NbBundle.getMessage(ShowChanges.class, "ShowChanges.title", build.getDisplayName());
        InputOutput io = IOProvider.getDefault().getIO(name, new Action[0]);
        io.select();
        OutputWriter out = io.getOut();
        OutputWriter err = io.getErr();
        Collection<? extends HudsonJobChangeItem> changes = build.getChanges();
        boolean first = true;
        for (HudsonJobChangeItem item : changes) {
            if (first) {
                first = false;
            } else {
                out.println();
            }
            out.println(item.getUser() + ": " + item.getMessage());
            for (HudsonJobChangeFile file : item.getFiles()) {
                // XXX hyperlink to diff viewer
                switch (file.getEditType()) {
                case edit:
                    out.print('±');
                    break;
                case add:
                    out.print('+');
                    break;
                case delete:
                    out.print('-');
                }
                out.print(' ');
                OutputListener hyperlink = file.hyperlink();
                if (hyperlink != null) {
                    try {
                        out.println(file.getName(), hyperlink);
                    } catch (IOException x) {
                        LOG.log(Level.INFO, null, x);
                    }
                } else {
                    out.println(file.getName());
                }
            }
        }
        if (first) {
            out.println(NbBundle.getMessage(ShowChanges.class, "ShowChanges.no_changes"));
        }
        out.close();
        err.close();
    }

}
