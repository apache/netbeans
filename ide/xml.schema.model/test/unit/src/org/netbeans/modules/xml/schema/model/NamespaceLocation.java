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

package org.netbeans.modules.xml.schema.model;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author nn136682
 */
public enum NamespaceLocation {
    OTA("http://www.opentravel.org/OTA/2003/05", "resources/J1_TravelItinerary.xsd"),
    ORGCHART("http://www.xmlspy.com/schemas/orgchart", "resources/OrgChart.xsd"),
    IPO("http://www.altova.com/IPO", "resources/ipo.xsd"),
    CUTPASTE("resources/CutPasteTest_before.xsd"),
    EXPREPORT("resources/ExpReport.xsd"),
    KEYREF("namespace1", "resources/KeyRef.xsd"),
    TEST_INCLUDE("http://www.example.com/testInclude", "resources/testInclude.xsd"),
    SOMEFILE("http://www.example.com/testInclude", "resources/somefile.xsd"),
    TEST_LENGTH("resources/testLength.xsd"),
    TEST_LIST("resources/testList.xsd"),
    TEST_BAD("resources/testBad.xsd"),
    LOANAPP("resources/loanApplication.xsd"),
    ADDRESS("resources/address.xsd"),
    REORDER_TEST("resources/ReorderTest.xsd"),
    SYNCTEST_PO("resources/PurchaseOrderSyncTest.xsd"),
    SYNCTEST_GLOBAL("resources/SyncTestGlobal_before.xsd"),
    SYNCTEST_NONGLOBAL("resources/SyncTestNonGlobal_before.xsd"),
    PO("http://www.example.com/PO1", "resources/PurchaseOrder.xsd");

    private String namespace;
    private String resourcePath;
    private String location;
    
    /** Creates a new instance of NamespaceLocation */
    NamespaceLocation(String location) {
        this("http://www.example.com/" +nameFromLocation(location), location);
    }
    
    NamespaceLocation(String namespace, String resourcePath) {
        this.namespace = namespace;
        this.resourcePath = resourcePath;
        this.location = resourcePath.substring(resourcePath.lastIndexOf("resources/")+10);
    }
    
    private static String nameFromLocation(String loc) {
         File f = new File(loc);
         String name = f.getName();
         return name.substring(0, name.length()-4);
    }
    
    public String getNamespace() { return namespace; }
    public String getResourcePath() { return resourcePath; }
    public String getLocationString() { return location; }
    public URI getNamespaceURI() throws URISyntaxException { return new URI(getNamespace()); }
    public static File schemaTestDir = null;
    public static File getSchemaTestTempDir() throws Exception {
        if (schemaTestDir == null) {
            schemaTestDir = Util.getTempDir("schematest");
        }
        return schemaTestDir;
    }
    public File getResourceFile() throws Exception {
        return new File(getSchemaTestTempDir(), Util.getFileName(getResourcePath()));
    }
    public void refreshResourceFile() throws Exception {
        Util.copyResource(getResourcePath(), FileUtil.toFileObject(getSchemaTestTempDir().getCanonicalFile()));
    }
    public URI getResourceURI() throws Exception { 
        return getResourceFile().toURI(); 
    }
    public URI getLocationURI() throws Exception { return new URI(location); }
    
    public static NamespaceLocation valueFromResourcePath(String resourcePath) {
        for (NamespaceLocation nl : values()) {
            if (nl.getResourcePath().equals(resourcePath)) {
                return nl;
            }
        }
        return null;
    }
}
