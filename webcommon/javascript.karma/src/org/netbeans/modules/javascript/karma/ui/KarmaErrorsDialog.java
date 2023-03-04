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

package org.netbeans.modules.javascript.karma.ui;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public final class KarmaErrorsDialog {

    private static final RequestProcessor RP = new RequestProcessor(KarmaErrorsDialog.class);
    private static final KarmaErrorsDialog INSTANCE = new KarmaErrorsDialog();

    private volatile boolean dialogShown = false;


    private KarmaErrorsDialog() {
    }

    public static KarmaErrorsDialog getInstance() {
        return INSTANCE;
    }

    @NbBundle.Messages("KarmaErrorsDialog.message=Review Output window for Karma warnings/errors")
    public void show() {
        show(Bundle.KarmaErrorsDialog_message());
    }

    public void show(final String message) {
        assert message != null;
        if (dialogShown) {
            return;
        }
        dialogShown = true;
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message));
                } finally {
                    dialogShown = false;
                }
            }
        });
    }

}
