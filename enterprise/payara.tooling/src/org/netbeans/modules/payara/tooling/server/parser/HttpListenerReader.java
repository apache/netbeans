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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.payara.tooling.logging.Logger;
import org.netbeans.modules.payara.tooling.server.parser.TreeParser.Path;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Reads configuration of http listeners from domain.xml.
 * For each http listener returns one {@link HttpData} object that
 * contains name of listener, port number and information whether
 * this listener is secured.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class HttpListenerReader extends TargetConfigReader implements XMLReader {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(HttpListenerReader.class);

    public static final String DEFAULT_PATH =
            "/domain/configs/config/http-service/http-listener";

    private String path;

    private Map<String, HttpData> result;

    public HttpListenerReader(String targetConfigName) {
        this(DEFAULT_PATH, targetConfigName);
    }
 
    public HttpListenerReader(String path, String targetConfigName) {
        super(targetConfigName);
        this.path = path;
        this.result = new HashMap<String, HttpData>();
    }

    @Override
    public void readAttributes(String qname, Attributes attributes) throws
            SAXException {
        final String METHOD = "readAttributes";
        // <http-listener 
        //   id="http-listener-1" port="8080" xpowered-by="true" 
        //   enabled="true" address="0.0.0.0" security-enabled="false" 
        //   family="inet" default-virtual-server="server" 
        //   server-name="" blocking-enabled="false" acceptor-threads="1">
        if (readData) {
            try {
                String id = attributes.getValue("id");
                if (id != null && id.length() > 0) {
                    int port = Integer.parseInt(attributes.getValue("port"));
                    boolean secure = Boolean.TRUE.toString().equals(attributes.
                            getValue("security-enabled"));
                    boolean enabled = !Boolean.FALSE.toString().
                            equals(attributes.
                            getValue("enabled"));
                    LOGGER.log(Level.INFO, METHOD, "port", new Object[] {
                        Integer.toString(port), Boolean.toString(enabled),
                        Boolean.toString(secure)});
                    if (enabled) {
                        HttpData data = new HttpData(id, port, secure);
                        LOGGER.log(Level.INFO, METHOD, "add", data);
                        result.put(id, data);
                    }
                } else {
                    LOGGER.log(Level.INFO, METHOD, "noName");
                }
            } catch (NumberFormatException ex) {
                LOGGER.log(Level.SEVERE, METHOD, "numberFormat", ex);
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

    public Map<String, HttpData> getResult() {
        return result;
    }
}
