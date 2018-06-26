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

import org.netbeans.modules.glassfish.tooling.server.parser.TreeParser.NodeListener;
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
