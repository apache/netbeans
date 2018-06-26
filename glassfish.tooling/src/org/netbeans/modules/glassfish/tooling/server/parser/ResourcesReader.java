/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.glassfish.tooling.server.parser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.glassfish.tooling.server.parser.TreeParser.NodeListener;
import org.netbeans.modules.glassfish.tooling.server.parser.TreeParser.Path;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Reads resources from domain.xml.
 * User has to specify an {@link ResourceType} which specifies
 * path and name of attribute value of which will be the key
 * in returned map.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class ResourcesReader extends NodeListener implements
        XMLReader {

    /**
     * Paths and key names for various resource types.
     */
    public enum ResourceType {

        JDBC_RESOURCE("/resources/jdbc-resource", "jndi-name"),
        JDBC_CONNECTION_POOL("/resources/jdbc-connection-pool", "name"),
        JAVA_MAIL("/resources/mail-resource", "jndi-name"),
        CONNECTOR_RESOURCE("/resources/connector-resource", "jndi-name"),
        CONNECTOR_POOL("/resources/connector-connection-pool", "name"),
        ADMIN_OBJECT_RESOURCE("/resources/admin-object-resource", "jndi-name");

        private String defaultKeyName;

        private String defaultPath;

        private ResourceType(String defaultPath, String defaultKeyName) {
            this.defaultPath = defaultPath;
            this.defaultKeyName = defaultKeyName;
        }

        public String getDefaultPath() {
            return defaultPath;
        }

        public String getDefaultKeyName() {
            return defaultKeyName;
        }
    }

    private String path;

    private String keyName;

    private Map<String, String> properties = null;

    private Map<String, Map<String, String>> resourceData =
            new HashMap<String, Map<String, String>>();

    public ResourcesReader(ResourceType type) {
        this(type.getDefaultPath(), type.getDefaultKeyName());
    }

    public ResourcesReader(String path, String keyName) {
        this.path = path;
        this.keyName = keyName;
    }

    @Override
    public void readAttributes(String qname, Attributes attributes) throws
            SAXException {
        properties = new HashMap<String, String>();

        String resourceName = attributes.getValue(keyName);
        properties.put(keyName, resourceName);

        int attrLen = attributes.getLength();
        for (int i = 0 ; i < attrLen ; i++) {
            String name = attributes.getQName(i);
            String value = attributes.getValue(i);
            if (name != null && name.length() > 0 && value != null && value.
                    length() > 0) {
                properties.put(name, value);
            }
        }
    }

    @Override
    public void readChildren(String qname, Attributes attributes) throws
            SAXException {
        String propName = qname + "." + attributes.getValue("name"); //$NON-NLS-1$ //$NON-NLS-2$
        properties.put(propName, attributes.getValue("value"));  //$NON-NLS-1$
    }

    @Override
    public void endNode(String qname) throws SAXException {
        String poolName = properties.get(keyName);
        resourceData.put(poolName, properties);
    }

    @Override
    public List<TreeParser.Path> getPathsToListen() {
        LinkedList<TreeParser.Path> paths = new LinkedList<TreeParser.Path>();
        paths.add(new Path(path, this));
        return paths;
    }

    public Map<String, Map<String, String>> getResourceData() {
        return resourceData;
    }
}
