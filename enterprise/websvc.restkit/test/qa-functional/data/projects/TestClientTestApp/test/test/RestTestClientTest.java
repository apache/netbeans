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
package test;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tellurium.test.java.TelluriumJavaTestCase;
import org.xml.sax.SAXParseException;

/**
 * Test for REST test client
 *
 * Duration of this test suite: aprox. 3min (using Firefox 3)
 *
 * @author lukas
 */
public class RestTestClientTest extends TelluriumJavaTestCase {

    public RestTestClientTest() {
    }

    private static TestClient tc;

    @BeforeClass
    public static void initUi() {
        tc = new TestClient();
        tc.defineUi();
    }

    @Before
    public void setUpBeforeTest(){
        connectUrl("http://localhost:8080/CustomerDB/rest-test/test-resbeans.html"); //NOI18N
    }

    /**
     * Test for GET request
     */
    @Test
    public void testGetResponseFormatOnContainer() {
        // show test UI for 'customers' resource
        tc.clickOn("customers"); //NOI18N
        // GET(application/json) should be selected by default - let's check it
        assertEquals("GET(application/json)", tc.getSelectedRMethod()); //NOI18N
        //should have four options:
        // GET(application/xml), GET(application/json),
        // POST(application/xml), POST(application/json)
        assertEquals(4, tc.getAvailableRMethods().length);
        tc.doTest();

        String s = tc.getContentFromView("raw"); //NOI18N
        try {
            JSONObject json = new JSONObject(s);
        } catch (JSONException ex) {
            ex.printStackTrace(System.err);
            fail("invalid JSON string: [" + s + "]"); //NOI18N
        }
        // check app/xml response format
        tc.setSelectedRMethod("GET(application/xml)"); //NOI18N
        assertEquals("GET(application/xml)", tc.getSelectedRMethod()); //NOI18N
        tc.doTest();
        s = tc.getContentFromView("raw"); //NOI18N
        try {
            Utils.readXml(s);
        } catch (SAXParseException se) {
            se.printStackTrace(System.err);
            fail("invalid xml response [" + s + "]"); //NOI18N
        }
    }

    /**
     * Test for GET request
     */
    @Test
    public void testGetResponseFormat() {
        // show test UI for 'customers/{customerId}' resource
        tc.expand("customers"); //NOI18N
        tc.clickOn("customerId"); //NOI18N
        // GET and application/xml should be selected by default - let's check it
        // XXX - should the default mime be app/json? IZ #156896
        assertEquals("GET", tc.getSelectedRMethod()); //NOI18N
        assertEquals("application/xml", tc.getSelectedMIMEType()); //NOI18N
        //should have three options:
        // GET, PUT, DELETE
        assertEquals(3, tc.getAvailableRMethods().length);
        // set an ID of a customer
        tc.setTestArg("resourceId", "1"); //NOI18N
        tc.doTest();
        String s = tc.getContentFromView("raw"); //NOI18N
        try {
            Utils.readXml(s);
        } catch (SAXParseException se) {
            se.printStackTrace(System.err);
            fail("invalid xml response [" + s + "]"); //NOI18N
        }

        // check app/json response format
        tc.setSelectedMIMEType("application/json"); //NOI18N
        assertEquals("application/json", tc.getSelectedMIMEType()); //NOI18N
        tc.doTest();
        s = tc.getContentFromView("raw"); //NOI18N
        try {
            JSONObject json = new JSONObject(s);
        } catch (JSONException ex) {
            ex.printStackTrace(System.err);
            fail("invalid JSON string: [" + s + "]"); //NOI18N
        }
    }

    /**
     * Test for POST request
     */
    @Test
    public void testPostRequest() {
        // show test UI for 'customers' resource
        tc.clickOn("customers"); //NOI18N
        // choose post - app/xml
        tc.setSelectedRMethod("POST(application/xml)"); //NOI18N
        assertEquals("POST(application/xml)", tc.getSelectedRMethod()); //NOI18N
        tc.setTestArg("content", Utils.readFile("resources/newCustomer.xml")); //NOI18N
        tc.doTest();
        String s = tc.getContentFromView("raw"); //NOI18N
        assertEquals(1000000, Utils.getCreditLimit(1001));

        // choose post - app/json
        tc.setSelectedRMethod("POST(application/json)"); //NOI18N
        assertEquals("POST(application/json)", tc.getSelectedRMethod()); //NOI18N
        tc.setTestArg("content", Utils.readFile("resources/newCustomer.json")); //NOI18N
        tc.doTest();
        s = tc.getContentFromView("raw"); //NOI18N
        assertEquals(1000000, Utils.getCreditLimit(1010));
    }

    /**
     * Test for PUT request
     */
    @Test
    public void testPutRequest() {
        // show test UI for 'customers/{customerId}' resource
        tc.expand("customers"); //NOI18N
        tc.clickOn("customerId"); //NOI18N
        // choose put
        tc.setSelectedRMethod("PUT"); //NOI18N
        assertEquals("PUT", tc.getSelectedRMethod()); //NOI18N
        // choose app/json response format
        tc.setSelectedMIMEType("application/json"); //NOI18N
        assertEquals("application/json", tc.getSelectedMIMEType()); //NOI18N
        // set resource to be modified ID
        tc.setTestArg("resourceId", "1010"); //NOI18N
        tc.setTestArg("content", Utils.readFile("resources/putCustomer.json")); //NOI18N
        tc.doTest();
        String s = tc.getContentFromView("raw"); //NOI18N
        assertEquals(0, Utils.getCreditLimit(1010));

        // choose app/xml
        tc.setSelectedMIMEType("application/xml"); //NOI18N
        assertEquals("application/xml", tc.getSelectedMIMEType()); //NOI18N
        assertEquals("PUT", tc.getSelectedRMethod()); //NOI18N
        // set resource to be modified ID
        tc.setTestArg("resourceId", "1001"); //NOI18N
        tc.setTestArg("content", Utils.readFile("resources/putCustomer.xml")); //NOI18N
        tc.doTest();
        s = tc.getContentFromView("raw"); //NOI18N
        assertEquals(0, Utils.getCreditLimit(1001));
    }

    /**
     * Test for DELETE request
     */
    @Test
    public void testDeleteRequest() {
        // show test UI for 'customers/{customerId}' resource
        tc.expand("customers"); //NOI18N
        tc.clickOn("customerId"); //NOI18N
        // choose delete
        tc.setSelectedRMethod("DELETE"); //NOI18N
        assertEquals("DELETE", tc.getSelectedRMethod()); //NOI18N
        // choose app/xml
        tc.setSelectedMIMEType("application/xml"); //NOI18N
        assertEquals("application/xml", tc.getSelectedMIMEType()); //NOI18N
        // set resource to be deleted ID
        tc.setTestArg("resourceId", "1001"); //NOI18N
        tc.doTest();
        String s = tc.getContentFromView("raw"); //NOI18N
        assertEquals(-1, Utils.getCreditLimit(1001));

        // choose app/json response format
        tc.setSelectedMIMEType("application/json"); //NOI18N
        assertEquals("application/json", tc.getSelectedMIMEType()); //NOI18N
        assertEquals("DELETE", tc.getSelectedRMethod()); //NOI18N
        // set resource to be deleted ID
        tc.setTestArg("resourceId", "1010"); //NOI18N
        tc.doTest();
        s = tc.getContentFromView("raw"); //NOI18N
        assertEquals(-1, Utils.getCreditLimit(1010));
    }
}
