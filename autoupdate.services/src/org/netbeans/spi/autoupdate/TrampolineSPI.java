/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.spi.autoupdate;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.autoupdate.updateprovider.UpdateItemImpl;
import org.netbeans.api.autoupdate.*;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.modules.autoupdate.services.*;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.modules.autoupdate.services.OperationContainerImpl.OperationInfoImpl;
import org.netbeans.modules.autoupdate.services.UpdateUnitImpl;

/** Trampoline to access internals of API and SPI.
 *
 * @author Jiri Rechtacek
 */
final class TrampolineSPI extends Trampoline {
    
    @Override
    protected UpdateUnit createUpdateUnit (UpdateUnitImpl impl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    protected UpdateUnitImpl impl (UpdateUnit unit) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    protected UpdateElement createUpdateElement (UpdateElementImpl impl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected UpdateElementImpl impl (UpdateElement element) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public UpdateItemImpl impl(UpdateItem item) {
        return item.impl;
    }

    @Override
    protected UpdateItem createUpdateItem (UpdateItemImpl impl) {
        return new UpdateItem (impl);
    }

    @Override
    protected OperationContainerImpl impl(OperationContainer container) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected UpdateUnitProvider createUpdateUnitProvider(UpdateProvider provider) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected UpdateUnitProvider createUpdateUnitProvider(UpdateUnitProviderImpl impl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public UpdateUnitProviderImpl impl(UpdateUnitProvider provider) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected OperationInfoImpl impl (OperationInfo info) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected OperationInfo createOperationInfo(OperationContainerImpl.OperationInfoImpl impl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected File findCluster (String clusterName, AutoupdateClusterCreator creator) {
        return creator.findCluster (clusterName);
    }

    @Override
    protected File[] registerCluster (String clusterName, File cluster, AutoupdateClusterCreator creator) throws IOException {
        return creator.registerCluster (clusterName, cluster);
    }

    @Override
    public InstallSupportImpl impl(InstallSupport support) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
