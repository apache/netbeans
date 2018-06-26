/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2013 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.glassfish.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.tooling.admin.CommandListWebServices;
import org.netbeans.modules.glassfish.tooling.admin.ResultList;
import org.netbeans.modules.glassfish.tooling.admin.ServerAdmin;
import org.netbeans.modules.glassfish.common.GlassFishLogger;
import org.netbeans.modules.glassfish.common.GlassfishInstance;

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
            = GlassFishLogger.get(WSDesc.class);

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
     * Fetch list of web service descriptions from given GlassFish instance.
     * <p/>
     * @param instance GlassFish instance from which to retrieve
     *                 web service descriptions.
     * @return List of web service descriptions retrieved from GlassFish server.
     */
    public static List<WSDesc> getWebServices(GlassfishInstance instance) {
        List<WSDesc> wsList;
        List<String> values = null;
        Future<ResultList<String>> future =
                ServerAdmin.<ResultList<String>>exec(instance,
                new CommandListWebServices());
        try {
            ResultList<String> result = future.get();
            values = result.getValue();
        } catch (ExecutionException ee) {
            LOGGER.log(Level.INFO, ee.getMessage(), ee);
        } catch (InterruptedException ie) {
            LOGGER.log(Level.INFO, ie.getMessage(), ie);
        } catch (CancellationException ce) {
            LOGGER.log(Level.INFO, ce.getMessage(), ce);
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
