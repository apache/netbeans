/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.modelimpl.repository;

import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Key.Behavior;
import org.netbeans.modules.cnd.repository.spi.KeyDataPresentation;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.openide.util.CharSequences;


/*package*/
// have to be public or UID factory does not work
public abstract class AbstractKey implements Key, SelfPersistent, KeyDataPresentation {

    /**
     * must be implemented in child
     */
    @Override
    public abstract String toString();

    /**
     * must be implemented in child
     */
    @Override
    public abstract int hashCode();

    /**
     * must be implemented in child
     */
    @Override
    public abstract boolean equals(Object obj);

    @Override
    public Key.Behavior getBehavior() {
        return Behavior.Default;
    }

    @Override
    public boolean hasCache() {
        return false;
    }

    @Override
    public abstract int getSecondaryAt(int level);

    @Override
    public abstract CharSequence getAt(int level);

    @Override
    public abstract CharSequence getUnit();

    @Override
    public abstract int getUnitId();

    @Override
    public abstract int getSecondaryDepth();

    @Override
    public abstract int getDepth();

    @Override
    public int getFilePresentation() {
        return -1;
    }
    
    @Override
    public CharSequence getNamePresentation() {
        return CharSequences.empty();
    }

    @Override
    public int getStartPresentation() {
        return -1;
    }

    @Override
    public int getEndPresentation() {
        return -1;
    }
}
