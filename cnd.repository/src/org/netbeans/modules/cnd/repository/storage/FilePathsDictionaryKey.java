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

package org.netbeans.modules.cnd.repository.storage;

import java.io.IOException;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 *
 */
final  class FilePathsDictionaryKey implements Key, SelfPersistent {
    private static final String UNITS_INDEX_FILE_NAME = "project-index";//NOI18N

    private final int unitId;

    public FilePathsDictionaryKey(int unitId) {
        this.unitId = unitId;
    }

    @Override
    public PersistentFactory getPersistentFactory() {
        return FilePathsDictionaryPersistentFactory.instance();
    }

    @Override
    public CharSequence getUnit() {
        return Repository.getUnitName(this.unitId);
    }

    @Override
    public int getUnitId() {
        return unitId;
    }

    @Override
    public Behavior getBehavior() {
        return Behavior.LargeAndMutable;
    }

    @Override
    public boolean hasCache() {
        return false;
    }

    @Override
    public int getDepth() {
        return 1;
    }

    @Override
    public CharSequence getAt(int level) {
        return UNITS_INDEX_FILE_NAME;
    }

    @Override
    public int getSecondaryDepth() {
        return 0;
    }

    @Override
    public int getSecondaryAt(int level) {
        return 0;
    }

    @Override
    public int hashCode(int unitID) {
        return 37 + unitID;
    }

    @Override
    public int hashCode() {
        return hashCode(unitId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || (this.getClass() != obj.getClass())) {
             return false;
         }
        FilePathsDictionaryKey other = (FilePathsDictionaryKey) obj;
        return equals(unitId, other, other.unitId);
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
    public void write(RepositoryDataOutput output) throws IOException {
        output.writeUnitId(unitId);
    }

}
