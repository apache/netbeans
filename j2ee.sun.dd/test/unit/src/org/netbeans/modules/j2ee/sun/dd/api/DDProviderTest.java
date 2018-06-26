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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.sun.dd.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.xml.sax.InputSource;
import static org.junit.Assert.*;
import org.xml.sax.SAXException;

/**
 *
 * @author vkraemer
 */
public class DDProviderTest extends NbTestCase {

    public DDProviderTest() {
        super("DDProvider");
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetEjbDDRoot() throws FileNotFoundException, IOException, SAXException {
        File[] list = getDataDir().listFiles();
        if (null == list || list.length == 0)
            return;
        for (File fo : list) {
            if (fo.getName().startsWith("valid-sun-ejb-jar")) {
                InputStream is = null;
                try {
                    is = new FileInputStream(fo);

                    DDProvider.getDefault().getEjbDDRoot(new InputSource(is));
                } finally {
                    if (null != is) {
                        try {
                            is.close();
                        } catch (IOException ioe) { }
                    }
                }
            }
        }


    }

    @Test
    public void testGetWebDDRoot() throws FileNotFoundException, IOException, SAXException, DDException {
        File[] list = getDataDir().listFiles();
        if (null == list || list.length == 0)
            return;
        for (File fo : list) {
            if (fo.getName().startsWith("valid-sun-web")) {
                InputStream is = null;
                try {
                    is = new FileInputStream(fo);

                    DDProvider.getDefault().getWebDDRoot(new InputSource(is));
                } finally {
                    if (null != is) {
                        try {
                            is.close();
                        } catch (IOException ioe) { }
                    }
                }
            }
        }

    }

    @Test
    public void testGetAppDDRoot() throws FileNotFoundException, IOException, SAXException {
        File[] list = getDataDir().listFiles();
        if (null == list || list.length == 0)
            return;
        for (File fo : list) {
            if (fo.getName().startsWith("valid-sun-application")) {
                InputStream is = null;
                try {
                    is = new FileInputStream(fo);

                    DDProvider.getDefault().getAppDDRoot(new InputSource(is));
                } finally {
                    if (null != is) {
                        try {
                            is.close();
                        } catch (IOException ioe) { }
                    }
                }
            }
        }

    }

    @Test
    public void testGetAppClientDDRoot() throws FileNotFoundException, IOException, SAXException {
        File[] list = getDataDir().listFiles();
        if (null == list || list.length == 0)
            return;
        for (File fo : list) {
            if (fo.getName().startsWith("valid-sun-appclient")) {
                InputStream is = null;
                try {
                    is = new FileInputStream(fo);

                    DDProvider.getDefault().getAppClientDDRoot(new InputSource(is));
                } finally {
                    if (null != is) {
                        try {
                            is.close();
                        } catch (IOException ioe) { }
                    }
                }
            }
        }

    }
}