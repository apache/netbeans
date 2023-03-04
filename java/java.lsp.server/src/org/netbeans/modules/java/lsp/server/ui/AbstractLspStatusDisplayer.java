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
package org.netbeans.modules.java.lsp.server.ui;

import javax.swing.event.ChangeListener;
import org.eclipse.lsp4j.MessageType;
import org.netbeans.modules.java.lsp.server.protocol.ShowStatusMessageParams;
import org.netbeans.modules.java.lsp.server.protocol.UIContext;
import org.openide.awt.StatusDisplayer;

public abstract class AbstractLspStatusDisplayer extends StatusDisplayer {
    private String text = "";

    protected AbstractLspStatusDisplayer() {
        if (!"org.netbeans.modules.nbcode.integration.LspStatusDisplayer".equals(getClass().getName())) {
            throw new IllegalStateException();
        }
    }

    @Override
    public final String getStatusText() {
        return text;
    }

    @Override
    public final void setStatusText(String text) {
        setStatusText(text, IMPORTANCE_ANNOTATION);
    }

    @Override
    public final Message setStatusText(String text, int importance) {
        this.text = text;
        UIContext ctx = UIContext.find();
        final MessageType type;
        if (importance >= IMPORTANCE_ANNOTATION) {
            type = MessageType.Info;
        } else if (importance >= IMPORTANCE_FIND_OR_REPLACE) {
            type = MessageType.Info;
        } else if (importance >= IMPORTANCE_ERROR_HIGHLIGHT) {
            type = MessageType.Error;
        } else {
            type = MessageType.Info;
        }
        Message m = ctx.showStatusMessage(new ShowStatusMessageParams(type, text, 0));
        return m != null ? m : (int timeInMillis) -> {
        };
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
    }
}
