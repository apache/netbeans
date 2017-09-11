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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.api.autoupdate;

import java.net.URL;
import java.util.List;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.autoupdate.services.UpdateUnitFactoryTest;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogProvider;

/**
 *
 * @author Jiri Rechtacek
 */
public class UpdateProviderFactoryTest extends NbTestCase {
    
    public UpdateProviderFactoryTest (String testName) {
        super (testName);
    }

    @Override
    protected void setUp () throws Exception {
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDirPath());
        MockServices.setServices (MyProvider.class, MyProvider2.class);
    }
    
    @Override
    protected void tearDown () throws  Exception {
    }

    public void testGetUpdatesProviders () {
        List<UpdateUnitProvider> result = UpdateUnitProviderFactory.getDefault ().getUpdateUnitProviders (false);
        
        assertFalse ("Providers found in lookup.", result.isEmpty ());
        assertEquals ("Two providers found.", 2, result.size ());
    }

    public void testSetEnable () {
        List<UpdateUnitProvider> result = UpdateUnitProviderFactory.getDefault ().getUpdateUnitProviders (false);

        UpdateUnitProvider provider = result.get (1);
        boolean state = false;
        provider.setEnable (state);
        
        assertEquals ("New state stored.", state, provider.isEnabled ());

        List<UpdateUnitProvider> resultOnlyEnabled = UpdateUnitProviderFactory.getDefault ().getUpdateUnitProviders (true);
        
        assertFalse ("Providers still found in lookup.", resultOnlyEnabled.isEmpty ());
        assertEquals ("Only one enable provider found.", 1, resultOnlyEnabled.size ());
        assertTrue ("Provider in only enabled must be enabled.", resultOnlyEnabled.get (0).isEnabled ());
    }

    public static class MyProvider extends AutoupdateCatalogProvider {
        public MyProvider () {
            super ("test-updates-provider", "test-updates-provider", UpdateUnitFactoryTest.class.getResource ("data/catalog.xml"));
        }
    }
    
    public static class MyProvider2 extends AutoupdateCatalogProvider {
        public MyProvider2 () {
            super ("test-updates-provider-2", "test-updates-provider-2", UpdateUnitFactoryTest.class.getResource ("data/catalog.xml"));
        }
    }
}
