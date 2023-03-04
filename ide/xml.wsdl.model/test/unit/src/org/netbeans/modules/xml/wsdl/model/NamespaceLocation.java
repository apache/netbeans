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
package org.netbeans.modules.xml.wsdl.model;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author nn136682
 */
public enum NamespaceLocation {
    HOTEL("http://www.sun.com/javaone/05/HotelReservationService", "resources/HotelReservationService.wsdl"),
    AIRLINE("http://www.sun.com/javaone/05/AirlineReservationService", "resources/AirlineReservationService.wsdl"),
    EMPTY_TRAVEL("http://www.sun.com/javaone/05/TravelReservationService", "resources/emptyTravel.wsdl"),
    TRAVEL("http://www.sun.com/javaone/05/TravelReservationService", "resources/TravelReservationService.wsdl"),
    VEHICLE("http://www.sun.com/javaone/05/VehicleReservationService", "resources/VehicleReservationService.wsdl"),
    OTA("http://www.opentravel.org/OTA/2003/05", "resources/OTA_TravelItinerary.xsd"),
    TESTOP("test/operations", "resources/TestOperations.wsdl"),
    TESTIMPORT("http://com.stc.database/pointbase/purchaseOrder", "resources/testImports.wsdl"),
    PO_1("http://com.stc.database/pointbase/purchaseOrder", "resources/purchaseOrder_1.xsd"),
    PO("http://www.w3.org/2001/XMLSchema", "resources/PurchaseOrder.xsd"),
    SCHEMA_NS_IN_WSDL("http://new.webservice.namespace", "resources/schemaUsingNamespaceFromWsdlRoot.wsdl"),
    ECHO("http://localhost/echo/echo", "resources/echo.wsdl"),
    ECHOCONCAT("http://stc.com/echoConcat", "resources/echoConcat.wsdl"),
    PARKING("urn:ParkingLotManager/wsdl", "resources/ParkingLotManager.wsdl");
    
    private String namespace;
    private String resourcePath;
    private String location;
    
    /** Creates a new instance of NamespaceLocation */
    NamespaceLocation(String namespace, String resourcePath) {
        this.namespace = namespace;
        this.resourcePath = resourcePath;
        this.location = resourcePath.substring(resourcePath.lastIndexOf("resources/")+10);
    }
    public String getNamespace() { return namespace; }
    public String getResourcePath() { return resourcePath; }
    public URI getLocationURI() throws URISyntaxException { 
        return new URI(getLocation());
    }
    public String getLocation() { return location; }
    public URI getNamespaceURI() throws URISyntaxException { return new URI(getNamespace()); }
    public static File wsdlTestDir = null;
    public static File getSchemaTestTempDir() throws Exception {
        if (wsdlTestDir == null) {
            wsdlTestDir = Util.getTempDir("wsdltest");
        }
        return wsdlTestDir;
    }
    public File getResourceFile() throws Exception {
        return new File(getSchemaTestTempDir(), Util.getFileName(getResourcePath()));
    }
    public void refreshResourceFile() throws Exception {
        if (getResourceFile().exists()) {
            ModelSource source = TestCatalogModel.getDefault().getModelSource(getLocationURI());
            DataObject dobj = (DataObject) source.getLookup().lookup(DataObject.class);
            SaveCookie save = (SaveCookie) dobj.getCookie(SaveCookie.class);
            if (save != null) save.save();
            FileObject fo = (FileObject) source.getLookup().lookup(FileObject.class);
            fo.delete();
        }
        Util.copyResource(getResourcePath(), FileUtil.toFileObject(getSchemaTestTempDir().getCanonicalFile()));
    }
    public URI getResourceURI() throws Exception { 
        return getResourceFile().toURI(); 
    }
    public static NamespaceLocation valueFromResourcePath(String resourcePath) {
        for (NamespaceLocation nl : values()) {
            if (nl.getResourcePath().equals(resourcePath)) {
                return nl;
            }
        }
        return null;
    }
}
