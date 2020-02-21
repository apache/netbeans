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
import java.nio.ByteBuffer;
import java.util.List;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.openide.filesystems.FileSystem;

/**
 *
 */
public interface WriteLayerCapability extends LayerCapability {

    public void write(LayerKey key, ByteBuffer byteBuffer);

    public void remove(LayerKey key, boolean keepRemovedTable);

    public void removeUnit(int unitIDInLayer);
    
    public void closeUnit(int unitIDInLayer, boolean cleanRespository);

    public int registerNewUnit(UnitDescriptor unitDescriptor);

    public int registerClientFileSystem(FileSystem clientFileSystem);
    
/** 
     * Determines the necessity of maintenance.
     * When a maintenancy is to be done, repository
     * sorts all units in accordance with the returned value.
     * So greater is the value, more need in maintenance unit has.
     */
    public int getMaintenanceWeight() throws IOException;
    
/**
     * Performes necessary maintenance (such as defragmentation) during the given timeout
     * @return true if maintenance was finished by timeout and needs more time to be completed
     */
    public boolean maintenance(long timeout)  throws IOException;    
}
