/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.payara.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.payara.tooling.admin.CommandListWebServices;
import org.netbeans.modules.payara.tooling.admin.ResultList;
import org.netbeans.modules.payara.tooling.admin.ServerAdmin;
import org.netbeans.modules.payara.common.PayaraLogger;
import org.netbeans.modules.payara.common.PayaraInstance;

/**
 * Web service description.
 * <p/>
 * @author Peter Williams, Tomas Kraus
 */
public class WSDesc {
    
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER
            = PayaraLogger.get(WSDesc.class);

    /** URL extension for WSDL file URL. */
    private static String WSDL_URL_EXTENSION = "?wsdl";

    /** URL extension for URL for testing. */
    private static String TEST_URL_EXTENSION = "?Tester";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Build WSDL file URL from web service name.
     * <p/>
     * @return WSDL file URL.
     */
    private static String buildWsdlUrl(String name) {
        if (name == null)
            throw new IllegalArgumentException(
                    "Parameter name shall not be null.");
        StringBuilder sb = new StringBuilder(
                name.length() + WSDL_URL_EXTENSION.length());
        sb.append(name);
        sb.append(WSDL_URL_EXTENSION);
        return sb.toString();
    }

    /**
     * Build URL for testing from web service name.
     * <p/>
     * @return URL for testing.
     */
    private static String buildTestUrl(String name) {
        if (name == null)
            throw new IllegalArgumentException(
                    "Parameter name shall not be null.");
        StringBuilder sb = new StringBuilder(
                name.length() + TEST_URL_EXTENSION.length());
        sb.append(name);
        sb.append(TEST_URL_EXTENSION);
        return sb.toString();
    }

    /**
     * Fetch list of web service descriptions from given Payara instance.
     * <p/>
     * @param instance Payara instance from which to retrieve
     *                 web service descriptions.
     * @return List of web service descriptions retrieved from Payara server.
     */
    public static List<WSDesc> getWebServices(PayaraInstance instance) {
        List<WSDesc> wsList;
        List<String> values = null;
        Future<ResultList<String>> future =
                ServerAdmin.<ResultList<String>>exec(instance,
                new CommandListWebServices());
        try {
            ResultList<String> result = future.get();
            values = result.getValue();
        } catch (ExecutionException | InterruptedException | CancellationException ee) {
            LOGGER.log(Level.INFO, ee.getMessage(), ee);
        }
        if (values != null && values.size() > 0) {
            wsList = new ArrayList<WSDesc>(values.size());
            for (String value : values) {
                wsList.add(new WSDesc(value));
            }
        } else {
            wsList = Collections.emptyList();
        }
        return wsList;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Web Services Description Language file URL. */
    private final String wsdlUrl;

    /** URL for testing. */
    private final String testUrl;

    /** Web service name. */
    private final String name;
    
    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of web service description.
     * <p/>
     * @param name    Web service name.
     * @param wsdlUrl WSDL file URL.
     * @param testUrl URL for testing.
     */
    public WSDesc(final String name) {
        this.name = name;
        this.wsdlUrl = buildWsdlUrl(name);
        this.testUrl = buildTestUrl(name);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get web service name from web service description.
     * <p/>
     * @return Web service name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get URL for testing from web service description.
     * <p/>
     * @return URL for testing.
     */
    public String getTestURL() {
        return testUrl;
    }
    
    /**
     * Get WSDL file URL from web service description.
     * <p/>
     * @return WSDL file URL.
     */
    public String getWsdlUrl() {
        return wsdlUrl;
    }

}
