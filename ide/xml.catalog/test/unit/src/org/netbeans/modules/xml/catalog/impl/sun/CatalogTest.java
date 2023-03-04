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

package org.netbeans.modules.xml.catalog.impl.sun;

import junit.framework.TestCase;

import java.net.URL;

import org.xml.sax.InputSource;

/**
 * Tests OASIS XML catalog implementatin.
 *
 * @author Petr Kuzel
 */
public final class CatalogTest extends TestCase {

    public void testSpaceInPath() throws Exception {
        Catalog catalog = new Catalog();
        URL locationURL = getClass().getResource("data/catalog.xml");
        String location = locationURL.toExternalForm();
        catalog.setLocation(location);
        catalog.refresh();
        String s0 = catalog.resolvePublic("-//NetBeans//IZ53710//EN");
        new URL(s0).openStream();
        String s1 = catalog.resolvePublic("-//NetBeans//IZ53710 1//EN");
        new URL(s1).openStream();
        String s2 = catalog.resolvePublic("-//NetBeans//IZ53710 2//EN");
        // new URL(s2).openStream();
        String s3 = catalog.resolvePublic("-//NetBeans//IZ53710 3//EN");
        new URL(s3).openStream();

        InputSource in1 = catalog.resolveEntity("-//NetBeans//IZ53710 1//EN", null);
        InputSource in2 = catalog.resolveEntity("-//NetBeans//IZ53710 2//EN", null);
        InputSource in3 = catalog.resolveEntity("-//NetBeans//IZ53710 3//EN", null);

        System.err.println("Done");
    }
}
