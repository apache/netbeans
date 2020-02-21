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
package org.netbeans.modules.cnd.modelimpl.repository;

import java.io.IOException;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.KeyDataPresentation;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.CharSequences;

/**
 * A common ancestor for nearly all keys 
 */

/*package*/ abstract class ProjectNameBasedKey extends AbstractKey {

    private final int unitIndex;
    
    /*package*/ static final CharSequence NO_PROJECT = CharSequences.create("<No Project Name>"); // NOI18N

    protected ProjectNameBasedKey(int unitIndex) {
        this.unitIndex = unitIndex;
        CndUtils.assertTrue(this.unitIndex > 10000, "Impossible unit index: ", unitIndex); //NOI18N
    }

    protected ProjectNameBasedKey(KeyDataPresentation presentation) {
        unitIndex = presentation.getUnitPresentation();
        CndUtils.assertTrue(this.unitIndex > 10000, "Impossible unit index: ", unitIndex); //NOI18N
    }

    @Override
    public String toString() {
        return getProjectName().toString();
    }

    @Override
    public int hashCode(int unitID) {
        return 37*getHandler() + unitID;
    }

    @Override
    public int hashCode() {
        return hashCode(unitIndex);
    }

    @Override
    public final int getUnitId() {
        return unitIndex;
    }

   @Override
    public boolean equals(int thisUnitID, Key object, int objectUnitID) {
        if (this == object) {
            return true;
        }
        if (object == null || (this.getClass() != object.getClass())) {
            return false;
        }
        return thisUnitID == objectUnitID;
    }


    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || (this.getClass() != obj.getClass())) {
             return false;
         }
        ProjectNameBasedKey other = (ProjectNameBasedKey) obj;
        return equals(unitIndex, other, other.unitIndex);

    }

    @Override
    public void write(RepositoryDataOutput aStream) throws IOException {
        aStream.writeUnitId(this.unitIndex);
    }

    protected ProjectNameBasedKey(RepositoryDataInput aStream) throws IOException {
        this.unitIndex = aStream.readUnitId();
    }

    @Override
    public int getDepth() {
        return 0;
    }

    @Override
    public CharSequence getAt(int level) {
        throw new UnsupportedOperationException();
    }

    protected CharSequence getProjectName() {
        return KeyUtilities.getUnitNameSafe(this.unitIndex);
    }

    @Override
    public CharSequence getUnit() {
        if (this.unitIndex < 0) {
            return NO_PROJECT;
        }
        // having this functionality here to be sure unit is the same thing as project
        return KeyUtilities.getUnitName(this.unitIndex);
    }

    @Override
    public final int getUnitPresentation() {
        return unitIndex;
    }
}
