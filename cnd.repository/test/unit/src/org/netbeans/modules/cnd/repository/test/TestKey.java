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

package org.netbeans.modules.cnd.repository.test;

import java.io.IOException;
import org.netbeans.modules.cnd.repository.impl.spi.UnitsConverter;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;


/**
 * Test interface Implementation 
 * for tests
 */
public class TestKey implements Key, SelfPersistent {
    
    private final String key;
    private final String unit;
    private final int unitId;
    private final Behavior behavior;
    
    @Override
    public Behavior getBehavior() {
	return Behavior.Default;
    }
    
    public TestKey(String key, int unitId, String unit, Behavior behavior) {
	this.key = key;
        this.unit = unit;
        this.unitId = unitId;
        this.behavior = behavior;
    }
    

    public TestKey(RepositoryDataInput stream) throws IOException {
        this(stream.readUTF(), stream.readInt(), stream.readUTF(),
                stream.readBoolean() ? Behavior.LargeAndMutable : Behavior.Default);
    }
    
    
    @Override
    public String getAt(int level) {
	return key;
    }
    
    @Override
    public int getDepth() {
	return 1;
    }
    
    @Override
    public PersistentFactory getPersistentFactory() {
	return TestFactory.instance();
    }
    
    @Override
    public int getSecondaryAt(int level) {
	return 0;
    }
    
    @Override
    public int getSecondaryDepth() {
	return 0;
    }

    @Override
    public final boolean equals(int thisUnitID, Key object, int objectUnitID) {
        if (object == null || (this.getClass() != object.getClass())) {
            return false;
        }        
        final TestKey other = (TestKey) object;
        if (this.key != other.key && (this.key == null || !this.key.equals(other.key))) {
            return false;
        }
        if (this.unit != other.unit && (this.unit == null || !this.unit.equals(other.unit))) {
            return false;
        }
        return this.behavior == other.behavior;
    }

    @Override
    public final boolean equals(Object object) {
        if (object == null || (this.getClass() != object.getClass())) {
            return false;
        } 
        
        final TestKey other = (TestKey) object;
        if (unitId != other.unitId) {
            return false;
        }
        if (this.key != other.key && (this.key == null || !this.key.equals(other.key))) {
            return false;
        }
        if (this.unit != other.unit && (this.unit == null || !this.unit.equals(other.unit))) {
            return false;
        }
        return this.behavior == other.behavior;        
    }

    @Override
    public final int hashCode(int unitID) {
        int hash = this.key != null ? this.key.hashCode() : 0;
        hash = 59 * hash + (this.unit != null ? this.unit.hashCode() : 0);
        hash = 59 * hash + (this.behavior != null ? this.behavior.hashCode() : 0);
        return hash + unitID;
    }

    @Override
    public final int hashCode() {
         return hashCode(unitId);
    }
    
    @Override
    public String toString() {
	return unitId + ' ' + unit + ':' + key + ' ' + behavior;
    }

    @Override
    public String getUnit() {
	return unit;
    }

    @Override
    public int getUnitId() {
        return unitId;
    }

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        output.writeUTF(key);
        output.writeInt(unitId);
        output.writeUTF(unit);
        output.writeBoolean(behavior == Behavior.LargeAndMutable);
    }

    @Override
    public boolean hasCache() {
        return false;
    }
}
