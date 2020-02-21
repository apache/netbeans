/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.platform;

import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.csm.core.AbstractFileBuffer;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 */
public class FileBufferSnapshot2 extends AbstractFileBuffer {

    private final Snapshot snapshot;
    private final long timestamp;

    public FileBufferSnapshot2(Snapshot snapshot, long timestamp) {
        super(snapshot.getSource().getFileObject());
        this.snapshot = snapshot;
        this.timestamp = timestamp;
    }

    @Override
    public char[] getCharBuffer() throws IOException {
        CharSequence text = snapshot.getText();
        char[] res = new char[text.length()];
        for (int i = 0; i < res.length; i++) {
            res[i] = text.charAt(i);
        }
        return res;
    }

    @Override
    public boolean isFileBased() {
        return false; // TODO: ???
    }

    @Override
    public String getText(int start, int end) throws IOException {
        return snapshot.getText().subSequence(start, end).toString();
    }

    @Override
    public CharSequence getText() throws IOException {
         return snapshot.getText();
    }

    @Override
    public long lastModified() {
        //return snapshot.getSource().getFileObject().lastModified().getTime();
        return timestamp;
    }
}
