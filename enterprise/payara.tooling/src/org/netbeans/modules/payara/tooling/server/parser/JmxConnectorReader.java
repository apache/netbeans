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
package org.netbeans.modules.payara.tooling.server.parser;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.payara.tooling.logging.Logger;
import org.netbeans.modules.payara.tooling.server.parser.TreeParser.Path;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Reader for jmx connector port number as string from domain.xml.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class JmxConnectorReader extends TargetConfigReader implements XMLReader {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(JmxConnectorReader.class);

    public static final String DEFAULT_PATH =
            "/domain/configs/config/admin-service/jmx-connector";

    private String path;

    private String result;

    public JmxConnectorReader(String path, String targetConfigName) {
        super(targetConfigName);
        this.path = path;
    }
    
    public JmxConnectorReader(String targetConfigName) {
        this(DEFAULT_PATH, targetConfigName);
    }

    @Override
    public void readAttributes(String qname, Attributes attributes)
            throws SAXException {
        final String METHOD = "getServerConfig";
        /*
         <admin-service type="das-and-server" system-jmx-connector-name="system">
         <jmx-connector ..... port="8686" />
         */
        if (readData) {
            String jmxAttr = attributes.getValue("port");
            try {
                int port = Integer.parseInt(jmxAttr);
                result = "" + port;	//$NON-NLS-1$
                LOGGER.log(Level.INFO, METHOD, "port", result);
            } catch (NumberFormatException ex) {
                LOGGER.log(Level.SEVERE, METHOD, "error", ex);
            }
        }

    }

    @Override
    public List<TreeParser.Path> getPathsToListen() {
        LinkedList<TreeParser.Path> paths = new LinkedList<TreeParser.Path>();
        paths.add(new Path(path, this));
        paths.add(new Path(CONFIG_PATH, new TargetConfigMarker()));
        return paths;
    }

    public String getResult() {
        return result;
    }
}
