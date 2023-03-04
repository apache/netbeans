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

package org.netbeans.modules.maven.hints.pom;

import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.spi.editor.errorstripe.UpToDateStatus;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProvider;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProviderFactory;
import org.openide.filesystems.*;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkleint
 */
@MimeRegistration(mimeType=Constants.POM_MIME_TYPE, service=UpToDateStatusProviderFactory.class)
public final class StatusProvider implements UpToDateStatusProviderFactory {
    private static final RequestProcessor RP = new RequestProcessor("StatusProvider"); //NOI18N
    private static final Logger LOG = Logger.getLogger(StatusProvider.class.getName());

    @Override
    public UpToDateStatusProvider createUpToDateStatusProvider(Document document) {
        return new StatusProviderImpl(document);
    }

    static class StatusProviderImpl extends UpToDateStatusProvider {
        private final Document document;
        private @NullAllowed POMModel model;
        private Project project;
        
        StatusProviderImpl(Document doc) {
            this.document = doc;
            RP.post(new Runnable() {
                @Override
                public void run() {
                    initializeModel(); //#204067 moved to RP 
                }
            });
        }

        private void initializeModel() {
            FileObject fo = NbEditorUtilities.getFileObject(document);
            if (fo != null) {
                //#236116 passing document protects from looking it up later and causing a deadlock.
                ModelSource ms = Utilities.createModelSource(fo, null, document instanceof BaseDocument ? (BaseDocument)document : null);
                model = POMModelFactory.getDefault().createFreshModel(ms);
                project = FileOwnerQuery.getOwner(fo);
            }
        }

        @Override
        public UpToDateStatus getUpToDate() {
            return UpToDateStatus.UP_TO_DATE_OK; // XXX should use UP_TO_DATE_PROCESSING if checkHints task is currently running
        }
    }
}
