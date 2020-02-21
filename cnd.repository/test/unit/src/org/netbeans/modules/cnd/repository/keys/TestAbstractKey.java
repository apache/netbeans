/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.repository.keys;

import java.io.IOException;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

public abstract class TestAbstractKey implements Key, SelfPersistent {

    private final String key;
    private final CharSequence unitName;
    private final int unitID;

    public TestAbstractKey(RepositoryDataInput stream) throws IOException {
        this.key = stream.readUTF();
        this.unitID = stream.readUnitId();
        this.unitName = Repository.getUnitName(unitID);
    }

    public TestAbstractKey(String key, int unitID) {
        this.key = key;
        this.unitID = unitID;
        this.unitName = Repository.getUnitName(unitID);
    }

    @Override
    public int getSecondaryAt(int level) {
        return 0;
    }

    @Override
    public String getAt(int level) {
        return key;
    }

    @Override
    public CharSequence getUnit() {
        return unitName;
    }

    @Override
    public int getUnitId() {
        return unitID;
    }

    @Override
    public int getSecondaryDepth() {
        return 0;
    }

    @Override
    public PersistentFactory getPersistentFactory() {
        return TestValuePersistentFactory.getInstance();
    }

    @Override
    public int getDepth() {
        return 1;
    }

    @Override
    public void write(RepositoryDataOutput output) throws IOException {        
        output.writeUTF(key);
        output.writeUnitId(getUnitId());
    }

    @Override
    public boolean equals(int thisUnitID, Key object, int objectUnitID) {
        if (thisUnitID != objectUnitID) {
            return false;
        }
        final TestAbstractKey other = (TestAbstractKey) object;
        if (this.key != other.key && (this.key == null || !this.key.equals(other.key))) {
            return false;
        }
        if (this.unitName != other.unitName && (this.unitName == null || !this.unitName.equals(other.unitName))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode(int unitID) {
        int hash = this.key != null ? this.key.hashCode() : 0;
        hash = 59 * hash + (this.unitName != null ? this.unitName.hashCode() : 0);
        return hash + unitID;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || (this.getClass() != object.getClass())) {
            return false;
        }
        final TestAbstractKey other = (TestAbstractKey) object;
        return equals(unitID, other, other.unitID);
    }

    @Override
    public int hashCode() {
        return hashCode(unitID);
    }       
    
    abstract protected short getHandler();
}