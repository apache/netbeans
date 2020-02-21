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

import java.util.Set;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;

/**
 * Repository implementation.
 *
 * Some basic facts about repository
 *
 * the main responsibility is to instantiate Persistent objects It knows keys,
 * it can get factories that will read-out and instantiate objects from provided
 * repostreams...
 *
 * - no encoding is done here. Encoding is 'done' in repostreams....
 *
 */
public interface RepositoryImplementation {

    // Main responsibility
    Persistent get(Key key);

    void put(Key key, Persistent obj);

    void remove(Key key);

    void shutdown();

    // -------------------------------------------------------
    // Performance hints -------------------------------------------------------
    // ?? performance should not be managed outside
    // closeUnit is a notification that repository may 'forget' about some data
    // associated with the unit
    void closeUnit(int unitId, boolean cleanRepository, Set<Integer> requiredUnits);

    // Do not garbage-collect related data in 10 seconds... 
    void openUnit(int unitId);

    // Remove not only this Key, but all subsequent info
    void removeUnit(int unitId);

    // Cache....
    // Stick object in memory (cache) 
    void hang(Key key, Persistent obj);

    public int getFileIdByName(int unitId, CharSequence fileName);

    public CharSequence getFileNameById(int unitId, int fileId);

    public CharSequence getFileNameByIdSafe(int unitId, int fileId);

    public CharSequence getUnitName(int unitId);

    public int getUnitID(UnitDescriptor unitDescriptor, int storageID);

    public int getUnitID(UnitDescriptor unitDescriptor);
    
    public LayeringSupport getLayeringSupport(int clientUnitID);

    // Debugging and logging...
    // --------------------------------------------------------------------
    void debugDistribution();

    void debugDump(Key key);

    public int getRepositoryID(int unitId);
}
