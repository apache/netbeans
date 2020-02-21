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
package org.netbeans.modules.cnd.toolchain.ui.impl;

import java.util.EnumSet;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.openide.util.NbBundle;
import org.openide.windows.IOPosition;
import org.openide.windows.IOSelect;
import org.openide.windows.InputOutput;

/**
 *
 */
public class ShowInOutputFix implements EnhancedFix {

    private static final int MAX_LENGTH = 50;

    private final String description;
    private final InputOutput io;
    private final IOPosition.Position position;

    public ShowInOutputFix(String description, InputOutput io, IOPosition.Position position) {
        this.description = description;
        this.io = io;
        this.position = position;
    }

    @Override
    public String getText() {
        String info = (description.length() > MAX_LENGTH)
                ? description.substring(0, MAX_LENGTH).concat("...") // NOI18N
                : description;
        return NbBundle.getMessage(ShowInOutputFix.class, "HINT_ShowInLog") //NOI18N
                .concat(" - ") //NOI18N
                .concat(info);
    }

    @Override
    public ChangeInfo implement() throws Exception {
        IOSelect.select(
                io,
                EnumSet.of(
                        IOSelect.AdditionalOperation.OPEN,
                        IOSelect.AdditionalOperation.REQUEST_VISIBLE
                )
        );
        position.scrollTo();

        return null;
    }

    @Override
    public CharSequence getSortText() {
        return "\uAAAA"; //NOI18N
    }
}
