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
import org.netbeans.modules.payara.tooling.server.parser.TreeParser.Path;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Reads the name of the configuration for given target (server
 * or cluster).
 * TODO now it reads only servers and not clusters...
 * @author Peter Benedikovic, Tomas Kraus
 */
public class TargetConfigNameReader extends TreeParser.NodeListener implements
        XMLReader {

    public static final String SERVER_PATH =
            "/domain/servers/server";

    public static final String DEFAULT_TARGET = "server";

    private String targetConfigName = null;

    private String targetName;

    public TargetConfigNameReader() {
        this(DEFAULT_TARGET);
    }

    public TargetConfigNameReader(String targetName) {
        this.targetName = targetName;
    }

    public String getTargetConfigName() {
        return targetConfigName;
    }

    @Override
    public void readAttributes(String qname, Attributes attributes) throws
            SAXException {
        if (attributes.getValue("name").equalsIgnoreCase(targetName)) {
            targetConfigName = attributes.getValue("config-ref");
        }
    }

    @Override
    public List<Path> getPathsToListen() {
        LinkedList<TreeParser.Path> paths = new LinkedList<TreeParser.Path>();
        paths.add(new Path(SERVER_PATH, this));
        return paths;
    }
}
