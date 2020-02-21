/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
