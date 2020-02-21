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
package org.netbeans.modules.cnd.repository.impl.spi;

import java.io.IOException;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.KeyFactory;

/**
 *
 */
public final class LayerKey implements Key {
    
    private final Key delegate;
    private final int unitId;

    private LayerKey(Key delegate, int unitIdInLayer) {
        this.delegate = delegate;
        this.unitId = unitIdInLayer;
    }

    @Override
    public PersistentFactory getPersistentFactory() {
        return delegate.getPersistentFactory();
    }

    @Override
    public CharSequence getUnit() {
        return delegate.getUnit();
    }

    @Override
    public int getUnitId() {
        return unitId;
    }

    @Override
    public Behavior getBehavior() {
        return delegate.getBehavior();
    }

    @Override
    public boolean hasCache() {
        return delegate.hasCache();
    }

    @Override
    public int getDepth() {
        return delegate.getDepth();
    }

    @Override
    public CharSequence getAt(int level) {
        return delegate.getAt(level);
    }

    @Override
    public int getSecondaryDepth() {
        return delegate.getSecondaryDepth();
    }

    @Override
    public int getSecondaryAt(int level) {
        return delegate.getSecondaryAt(level);
    }



    @Override
    public String toString() {
        return "LayerKey unitId=" + unitId + " delegate=" + delegate.getClass().getName(); // NOI18N
    }

    @Override
    public int hashCode() {
        final int res = delegate.hashCode();
        assert delegate.hashCode(0) + delegate.getUnitId()== res;
        return res - delegate.getUnitId() + unitId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LayerKey)) {
            return false;
        }
        final LayerKey other = (LayerKey) obj;
        if (other.delegate == delegate) {
            return true;
        }
        if (other.getUnitId() != unitId) {
            return false;
        }
        return delegate.equals(unitId, other.delegate, other.getUnitId());
    }

    public static LayerKey create(Key key, int unitIdInLayer) {
        return new LayerKey(key, unitIdInLayer);
    }

    public static void write(LayerKey key, RepositoryDataOutput output) throws IOException {
        output.writeInt(key.getUnitId());
        KeyFactory.getDefaultFactory().writeKey(key.delegate, output);
    }

    public static LayerKey read(RepositoryDataInput input) throws IOException {
        int unitIdInLayer = input.readInt();
        Key key = KeyFactory.getDefaultFactory().readKey(input);
        return new LayerKey(key, unitIdInLayer);
    }
    

    @Override
    public int hashCode(int unitID) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(int thisUnitID, Key object, int objectUnitID) {
        throw new UnsupportedOperationException();
    }
    

    
}
