/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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