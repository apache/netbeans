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
package org.netbeans.modules.search.ui;

import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.modules.search.MatchingObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author jhavlin
 */
@NbBundle.Messages({
    "RefreshAction.name=Refresh"
})
public class RefreshAction extends AbstractAction {

    private final MatchingObject mo;

    public RefreshAction(MatchingObject mo) {
        super(Bundle.RefreshAction_name());
        this.mo = mo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (mo != null) {
            RequestProcessor.getDefault().post(() -> {
                FileObject fo = mo.getFileObject();
                if (!fo.isValid()) {
                    try {
                        // get fresh fo in case it has been recreated
                        fo = fo.getFileSystem().findResource(fo.getPath());
                    } catch (FileStateInvalidException ex) {
                        Logger.getLogger(
                                BasicReplaceResultsPanel.class.getName())
                                .log(Level.FINE, null, ex);
                    }
                }
                if (fo != null && fo.isValid()) {
                    MatchingObject.Def def = mo.getBasicComposition()
                            .getMatcher().check(
                                    fo, new SearchListener() {
                                    });
                    if (def != null) {
                        mo.refresh(def);
                    }
                }
            });
        }
    }
}
