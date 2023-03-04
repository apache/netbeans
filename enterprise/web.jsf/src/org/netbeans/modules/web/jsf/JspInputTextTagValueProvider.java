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
