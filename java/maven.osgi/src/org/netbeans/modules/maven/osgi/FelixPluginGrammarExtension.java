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

package org.netbeans.modules.maven.osgi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.jdom2.Element;
import org.netbeans.modules.maven.grammar.spi.AbstractSchemaBasedGrammar;
import org.netbeans.modules.maven.grammar.spi.GrammarExtensionProvider;
import org.netbeans.modules.xml.api.model.GrammarResult;
import org.netbeans.modules.xml.api.model.HintContext;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Extension of grammar for pom.xml completion for felix bundle plugin instructions.
 *
 * @author Dafe Simonek
 */
@ServiceProvider(service=GrammarExtensionProvider.class)
public class FelixPluginGrammarExtension implements GrammarExtensionProvider {

    private static final String[] txtInstructions = new String[] {
                OSGiConstants.EXPORT_PACKAGE, OSGiConstants.PRIVATE_PACKAGE,
                OSGiConstants.BUNDLE_ACTIVATOR, OSGiConstants.BUNDLE_SYMBOLIC_NAME,
                OSGiConstants.IMPORT_PACKAGE, OSGiConstants.INCLUDE_RESOURCE,
                OSGiConstants.EMBED_DEPENDENCY, OSGiConstants.EMBED_DIRECTORY,
                OSGiConstants.EMBED_STRIP_GROUP, OSGiConstants.EMBED_STRIP_VERSION,
                OSGiConstants.EMBED_TRANSITIVE
            };

    @Override
    @NonNull 
    public List<GrammarResult> getDynamicCompletion(String path, HintContext hintCtx, Element parent) {
        //TODO also plugin/executions/execution/configuration should apply
        if (path.endsWith("plugins/plugin/configuration") && isFelixPlugin(hintCtx.getParentNode())) { //NOI18N
            List<GrammarResult> result = new ArrayList<GrammarResult>();
            result.add(new AbstractSchemaBasedGrammar.MyTextElement(OSGiConstants.PARAM_INSTRUCTIONS, hintCtx.getCurrentPrefix()));
            return result;
        }

        if (path.endsWith("plugins/plugin/configuration/" + OSGiConstants.PARAM_INSTRUCTIONS) &&
                isFelixPlugin(hintCtx.getParentNode().getParentNode())) { //NOI18N
            List<GrammarResult> result = new ArrayList<GrammarResult>();
            for (String curInst : txtInstructions) {
                result.add(new AbstractSchemaBasedGrammar.MyTextElement(curInst, hintCtx.getCurrentPrefix()));
            }
            return result;
        }

        return Collections.<GrammarResult>emptyList();
    }

    @Override
    public Enumeration<GrammarResult> getDynamicValueCompletion(String path, HintContext virtualTextCtx, Element el) {
        return null;
    }

    private static boolean isFelixPlugin (Node configNode) {
        Node pluginNode = configNode.getParentNode();
        if (pluginNode == null) {
            return false;
        }
        NodeList pluginChildren = pluginNode.getChildNodes();
        boolean felixGroupId = false;
        boolean felixArtifactId = false;
        for (int i = 0; i < pluginChildren.getLength(); i++) {
            Node curNode = pluginChildren.item(i);
            if ("groupId".equals(curNode.getNodeName())) {
                NodeList children = curNode.getChildNodes();
                if (children.getLength() > 0 && 
                        OSGiConstants.GROUPID_FELIX.equals(children.item(0).getNodeValue())) {
                    felixGroupId = true;
                } else {
                    return false;
                }
            }
            if ("artifactId".equals(curNode.getNodeName())) {
                NodeList children = curNode.getChildNodes();
                if (children.getLength() > 0 && 
                        OSGiConstants.ARTIFACTID_BUNDLE_PLUGIN.equals(children.item(0).getNodeValue())) {
                    felixArtifactId = true;
                } else {
                    return false;
                }
            }
        }

        return felixGroupId && felixArtifactId;
    }


}
