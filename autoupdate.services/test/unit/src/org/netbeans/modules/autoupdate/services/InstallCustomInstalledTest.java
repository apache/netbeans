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

package org.netbeans.modules.autoupdate.services;

import java.util.List;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.TestUtils;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.autoupdate.updateprovider.NativeComponentItem;
import org.netbeans.modules.autoupdate.updateprovider.UpdateItemImpl;
import org.netbeans.spi.autoupdate.CustomInstaller;
import org.netbeans.spi.autoupdate.UpdateItem;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstallCustomInstalledTest extends OperationsTestImpl {
    public InstallCustomInstalledTest(String testName) {
        super(testName);
    }
    
    protected String moduleCodeNameBaseForTest () {
        return "hello-installer";
    }
    
    @Override
    public void setUp () throws Exception {
        super.setUp ();
        TestUtils.setCustomInstaller (installer);
    }
    
    public void testSelf () throws Exception {
        List<UpdateUnit> units = UpdateManager.getDefault ().getUpdateUnits (UpdateManager.TYPE.CUSTOM_HANDLED_COMPONENT);
        assertNotNull (units);
        assertFalse (units.isEmpty ());
        UpdateUnit toInstall = UpdateManagerImpl.getInstance ().getUpdateUnit (moduleCodeNameBaseForTest ());
        assertFalse (toInstall + " has available elements.", toInstall.getAvailableUpdates ().isEmpty ());
        UpdateElement toInstallElement = toInstall.getAvailableUpdates ().get (0);
        installNativeComponent (toInstall, toInstallElement);
        assertTrue ("Custom installer was called.", installerCalled);
    }
    
    private boolean installerCalled = false;
    
    private CustomInstaller installer = new CustomInstaller () {
        public boolean install (String codeName, String specificationVersion, ProgressHandle handle) throws OperationException {
            UpdateItem exp = TestUtils.getUpdateItemWithCustomInstaller ();
            UpdateItemImpl impl = Trampoline.SPI.impl (exp);
            assertTrue ("Get instanceOf NativeComponentItem", impl instanceof NativeComponentItem);
            NativeComponentItem nativeImpl = (NativeComponentItem) impl;
            assertNotNull ("Code name is not null.", codeName);
            assertNotNull ("SpecificationVersion is not null.", specificationVersion);
            assertEquals ("Was called with as same codeName as excepted.", impl.getCodeName (), codeName);
            assertEquals ("Was called with as same specificationVersion as excepted.", nativeImpl.getSpecificationVersion (), specificationVersion);
            installerCalled = true;
            return true;
        }
    };
    
}
