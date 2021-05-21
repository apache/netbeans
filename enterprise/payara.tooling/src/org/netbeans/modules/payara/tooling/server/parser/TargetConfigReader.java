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

import org.netbeans.modules.payara.tooling.server.parser.TreeParser.NodeListener;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Marks that the xml parser is currently inside config element with
 * give name.
 * This information is used by descendants of this class.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
class TargetConfigReader extends NodeListener {

    public static final String CONFIG_PATH =
            "/domain/configs/config";

    public static final String DEFAULT_TARGET = "server";

    protected static boolean readData = false;

    private String targetConfigName = null;

    public TargetConfigReader(String targetConfigName) {
        this.targetConfigName = targetConfigName;
        // TODO all parsing has to be rewritten at some point
        this.readData = false;
    }

    class TargetConfigMarker extends NodeListener {


        @Override
        public void readAttributes(String qname, Attributes attributes) throws
                SAXException {
            if ((targetConfigName != null) && attributes.getValue("name").equalsIgnoreCase(targetConfigName)) {
                readData = true;
            }
        }

        @Override
        public void endNode(String qname) throws SAXException {
            if ("config".equals(qname)) {
                readData = false;
            }
        }

    }

}
