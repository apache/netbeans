/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.utils.ui.impl;

import org.netbeans.modules.cnd.spi.utils.CndNotifier;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = CndNotifier.class, position = 100)
public final class CndUIErrorNotifier extends CndNotifier {

    @Override
    public void notifyError(String errmsg) {
        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(errmsg, NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(msg);
    }

    @Override
    public void notifyInfo(String msg) {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
    }

    @Override
    public boolean notifyAndIgnore(String title, String msg) {
        NotifyDescriptor nd = new NotifyDescriptor(msg,
                title, NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE,
                null, NotifyDescriptor.YES_OPTION);
        Object ret = DialogDisplayer.getDefault().notify(nd);
        return ret == NotifyDescriptor.YES_OPTION;
    }

    @Override
    public void notifyErrorLater(String msg) {
         DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
    }

    @Override
    public void notifyStatus(String text) {
        StatusDisplayer.getDefault().setStatusText(text);
    }

}
