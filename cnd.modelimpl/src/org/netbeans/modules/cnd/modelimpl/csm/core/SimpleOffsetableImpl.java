/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.IOException;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * Trivial CsmOffsetable implementation
 */
public class SimpleOffsetableImpl implements CsmOffsetable {
    
        private LineColOffsPositionImpl stPos = null;
        private LineColOffsPositionImpl endPos = null;

        public SimpleOffsetableImpl(int line, int col, int offset) {
            stPos = new LineColOffsPositionImpl(line, col, offset);
        }

        public SimpleOffsetableImpl(CsmOffsetable offsetable) {
            stPos = new LineColOffsPositionImpl(offsetable.getStartPosition());
            endPos = new LineColOffsPositionImpl(offsetable.getEndPosition());
        }

        public void setEndPosition(Position startPosition) {
            endPos = new LineColOffsPositionImpl(startPosition);
        }
        
        public void setEndPosition(int line, int col, int offset) {
            endPos = new LineColOffsPositionImpl(line, col, offset);
        }
    
    @Override
        public CsmFile getContainingFile() {
            return null;
        }

    @Override
        public int getStartOffset() {
            return stPos.getOffset();
        }

    @Override
        public int getEndOffset() {
            return endPos.getOffset();
        }

    @Override
        public CsmOffsetable.Position getStartPosition() {
            return stPos;
        }

    @Override
        public CsmOffsetable.Position getEndPosition() {
            return endPos;
        }    

    @Override
        public CharSequence getText() {
            return null;
        }

        protected SimpleOffsetableImpl(RepositoryDataInput input) throws IOException {
            stPos = new LineColOffsPositionImpl(input.readInt(), input.readInt(), input.readInt());
            endPos = new LineColOffsPositionImpl(input.readInt(), input.readInt(), input.readInt());
        }

        protected void write(RepositoryDataOutput output) throws IOException {
            output.writeInt(stPos.getLine());
            output.writeInt(stPos.getColumn());
            output.writeInt(stPos.getOffset());
            output.writeInt(endPos.getLine());
            output.writeInt(endPos.getColumn());
            output.writeInt(endPos.getOffset());
        }
}
