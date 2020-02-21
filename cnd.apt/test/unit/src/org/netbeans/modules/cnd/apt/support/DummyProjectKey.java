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

package org.netbeans.modules.cnd.apt.support;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.filesystems.FileSystem;

/**
 */
final class DummyProjectKey implements Key, PersistentFactory, Persistent {

    private static final Map<FileSystem, DummyProjectKey> instances = new HashMap<FileSystem, DummyProjectKey>();
    private static final Object lock = new Object();
    private static boolean first = true;
    
    public static DummyProjectKey getOrCreate(FileSystem fileSystem) {
        synchronized (lock) {
            DummyProjectKey key = instances.get(fileSystem);
            if (key == null) {
                key = new DummyProjectKey(fileSystem);
                instances.put(fileSystem, key);
            }
            return key;
        }
    }

    private final CharSequence unitName;
    private final int unitId;

    private DummyProjectKey(FileSystem fileSystem) {
        synchronized (lock) {
            if (first) {
                first = false;
                Repository.startup(RepositoryUtils.getPersistenceVersion());
            }
        }
        unitName = "dummy";
        unitId = Repository.getUnitId(new UnitDescriptor(unitName, fileSystem));
    }

    @Override
    public PersistentFactory getPersistentFactory() {
        return this;
    }

    @Override
    public CharSequence getUnit() {
        return unitName;
    }

    @Override
    public int getUnitId() {
        return unitId;
    }

    @Override
    public Behavior getBehavior() {
        return Behavior.Default;
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
        return unitName;
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
        return unitID;
    }

    @Override
    public boolean equals(int thisUnitID, Key object, int objectUnitID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void write(RepositoryDataOutput out, Persistent obj) throws IOException {
    }

    @Override
    public Persistent read(RepositoryDataInput in) throws IOException {
        return this;
    }
    
}
