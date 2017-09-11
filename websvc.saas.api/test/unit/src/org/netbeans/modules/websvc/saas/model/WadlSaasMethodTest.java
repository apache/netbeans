/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.saas.model;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.saas.model.wadl.Resource;
import org.netbeans.modules.websvc.saas.util.SetupUtil;

/**
 *
 * @author nam
 */
public class WadlSaasMethodTest extends NbTestCase {
    
    public WadlSaasMethodTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetResourcePath() throws Exception {
        SetupUtil.commonSetUp(super.getWorkDir());
        SaasServicesModel instance = SaasServicesModel.getInstance();
        SaasGroup group = instance.getRootGroup().getChildGroup("Delicious");
        WadlSaas saas = (WadlSaas) group.getServices().get(0);
        assertEquals("Bookmarking Service", saas.getDisplayName());
        WadlSaasMethod m = saas.getResources().get(0).getChildResources().get(0).getMethods().get(0);
        assertEquals("getPosts", m.getName());
        Resource[] path = m.getResourcePath();
        assertEquals("posts", path[0].getPath());
        assertEquals("get", path[1].getPath());

        SetupUtil.commonTearDown();
    }

    public void testGetResourcePathOfFilteredMethod() throws Exception {
        SetupUtil.commonSetUp(super.getWorkDir());
        
        SaasServicesModel instance = SaasServicesModel.getInstance();
        SaasGroup group = instance.getRootGroup().getChildGroup("Zillow");
        WadlSaas saas = (WadlSaas) group.getServices().get(0);
        assertEquals("Real Estate Service", saas.getDisplayName());
        /* These wadl methods are no longer filter.
        WadlSaasMethod m = (WadlSaasMethod) saas.getMethods().get(2);
        assertEquals("ListPopular", m.getName());
        Resource[] path = m.getResourcePath();
        assertEquals(1, path.length);
        assertEquals("api2_rest", path[0].getPath());
        */
        SetupUtil.commonTearDown();
    }
}
