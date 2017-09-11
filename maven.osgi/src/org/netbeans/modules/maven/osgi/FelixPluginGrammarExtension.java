/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.osgi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.jdom.Element;
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
