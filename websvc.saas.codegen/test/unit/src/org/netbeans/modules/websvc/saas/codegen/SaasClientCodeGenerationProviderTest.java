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
package org.netbeans.modules.websvc.saas.codegen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.text.Document;
//import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.modules.websvc.saas.model.*;
import org.netbeans.modules.websvc.saas.model.wadl.Method;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author ayubkhan
 */
public class SaasClientCodeGenerationProviderTest extends NbTestCase {

    private static final List<String> nonhyperlinkedOut = new ArrayList<String>();
    private static final List<String> nonhyperlinkedErr = new ArrayList<String>();
    private static final List<String> hyperlinkedOut = new ArrayList<String>();
    private static final List<String> hyperlinkedErr = new ArrayList<String>();
    private FileObject targetFolder;
    private Properties props;
    private FileObject targetFO;
    private SaasMethod method;

    public SaasClientCodeGenerationProviderTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        targetFolder = FileUtil.createFolder(new File("/tmp/testuserdir"));
        nonhyperlinkedOut.clear();
        nonhyperlinkedErr.clear();
        hyperlinkedOut.clear();
        hyperlinkedErr.clear();
        try {
            SetupUtil.commonSetUp(super.getWorkDir());
            InputStream is = this.getClass().getResourceAsStream("JavaApplication.zip");
            TestUtils.unZipFile(is, targetFolder);
        } catch (Exception ex) {
            assertFalse("Failed with exception: "+ex.getMessage(), ex != null);
        }
        targetFO = targetFolder.getFileObject("JavaApplication/src/javaapplication/Main.java");
        assertTrue("Target File is null", targetFO != null);

        SaasServicesModel instance = SaasServicesModel.getInstance();
        SaasGroup group = instance.getRootGroup().getChildGroup("Delicious");
        WadlSaas saas = (WadlSaas) group.getServices().get(0);
        assertEquals("Bookmarking Service", saas.getDisplayName());
        method = saas.getResources().get(0).getChildResources().get(0).getMethods().get(0);
        Method m = ((WadlSaasMethod)method).getWadlMethod();
        if (m == null) {
            Exceptions.printStackTrace(new IOException("Wadl method not found"));
        }
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public void testCanAccept() {
        try {
            Document doc = Util.getDocument(targetFO);
            SaasClientCodeGenerator codegen = 
                    (SaasClientCodeGenerator) SaasClientCodeGenerationManager.lookup(method, doc);
            codegen.canAccept(method, doc);
        } catch (Exception ex) {
            assertFalse("Failed with exception: "+ex.getMessage(), ex != null);
        }
    }
    
    public void testInit() {
        try {
            Document doc = Util.getDocument(targetFO);
            SaasClientCodeGenerator codegen = 
                    (SaasClientCodeGenerator) SaasClientCodeGenerationManager.lookup(method, doc);
            codegen.init(method, doc);
        } catch (Exception ex) {
            assertFalse("Failed with exception: "+ex.getMessage(), ex != null);
        }
    }
    
    public void testGenerate() {
        try {
            Document doc = Util.getDocument(targetFO);
            SaasClientCodeGenerator codegen = 
                    (SaasClientCodeGenerator) SaasClientCodeGenerationManager.lookup(method, doc);
            codegen.generate();
        } catch (Exception ex) {
            assertFalse("Failed with exception: "+ex.getMessage(), ex != null);
        }
    }
}
