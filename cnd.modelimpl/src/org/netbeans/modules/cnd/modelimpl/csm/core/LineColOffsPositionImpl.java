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
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * (line, col, offset) based CsmOffsetable.Position implementation
 */
public class LineColOffsPositionImpl implements CsmOffsetable.Position {
    private final int line;
    private final int col;
    private final int offset;

    public LineColOffsPositionImpl() {
        this(0,0,0);
    }

    public LineColOffsPositionImpl(CsmOffsetable.Position pos) {
        if (pos != null) {
            this.line = pos.getLine();
            this.col = pos.getColumn();
            this.offset = pos.getOffset();            
        } else {
            this.line = 0;
            this.col = 0;
            this.offset = 0;
        }
    }
    
    public LineColOffsPositionImpl(int line, int col, int offset) {
        this.line = line;
        this.col = col;
        this.offset = offset;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getColumn() {
        return col;
    }
    
    @Override
    public String toString() {
        return "" + getLine() + ':' + getColumn() + '/' + getOffset();
    }

    /* package */ void toStream(RepositoryDataOutput output) throws IOException {
        output.writeInt(line);
        output.writeInt(col);
        output.writeInt(offset);
    }

    /* package */ LineColOffsPositionImpl(RepositoryDataInput input) throws IOException {
        line = input.readInt();
        col = input.readInt();
        offset = input.readInt();
    }
}       
