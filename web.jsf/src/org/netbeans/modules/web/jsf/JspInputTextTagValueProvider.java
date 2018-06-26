/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.jsf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.web.core.api.JspContextInfo;
import org.netbeans.modules.web.jsfapi.spi.InputTextTagValueProvider;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.Node;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author marekfukala
 */
@ServiceProvider(service=InputTextTagValueProvider.class)
public class JspInputTextTagValueProvider implements InputTextTagValueProvider {

    //XXX fix the hardcoded html lib prefix - it was just copied from the original implementation as it was
    private static final String INPUT_TEXT_TAG_NAME = "h:inputText"; //NOI18N
    private static final String VALUE_ATTR_NAME = "value"; //NOI18N

    @Override
    public Map<String, String> getInputTextValuesMap(FileObject fobj) {
//        if(!JspUtils.isJSPOrTagFile(fobj)) {
//            return null;
//        }
        Map<String, String> properties = new HashMap<String, String>();
        //try if in a JSP...
        JspContextInfo contextInfo = JspContextInfo.getContextInfo(fobj);
        if (contextInfo != null) {
            JspParserAPI.ParseResult result = contextInfo.getCachedParseResult(fobj, false, true);
            if (result != null) {
                Node.Nodes nodes = result.getNodes();
                List<Node> foundNodes = new ArrayList<Node>();
                foundNodes = findValue(nodes, INPUT_TEXT_TAG_NAME, foundNodes);
                for (Node node : foundNodes) {
                    String ref_val = node.getAttributeValue(VALUE_ATTR_NAME);
                    String key = generateKey(ref_val, properties);
                    properties.put(key, ref_val);
                }
            }
        }
        return properties;
    }

     private List<Node> findValue(Node.Nodes nodes, String tagName, List<Node> foundNodes) {
        if (nodes == null)
            return foundNodes;
        for (int i=0;i<nodes.size();i++) {
            Node node = nodes.getNode(i);
            if (tagName.equals(node.getQName())){
                foundNodes.add(node);
            } else {
                foundNodes = findValue(node.getBody(), tagName, foundNodes);
            }
        }
        return foundNodes;
    }

    private String generateKey(String value, Map<String, String> properties) {
        if (value.startsWith("#{")) {    //NOI18N
            value = value.substring(2, value.length()-1);
        }
        String result = value.substring(value.lastIndexOf(".")+1,value.length()).toLowerCase();
        int i=0;
        String tmp = result;
        while (properties.get(tmp) != null) {
            i++;
            tmp=result+i;
        }
        return result;
    }


}
