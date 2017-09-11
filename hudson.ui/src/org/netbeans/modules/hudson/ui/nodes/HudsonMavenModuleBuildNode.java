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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hudson.ui.nodes;

import java.io.CharConversionException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.netbeans.modules.hudson.ui.api.UI;
import org.netbeans.modules.hudson.ui.actions.OpenUrlAction;
import org.netbeans.modules.hudson.api.ui.OpenableInBrowser;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;

/**
 * Represents one Maven module in a build.
 */
class HudsonMavenModuleBuildNode extends AbstractNode {

    private final HudsonMavenModuleBuild module;
    private String htmlDisplayName;

    public HudsonMavenModuleBuildNode(HudsonMavenModuleBuild module) {
        super(makeChildren(module), Lookups.singleton(module));
        this.module = module;
        setName(module.getName());
        setDisplayName(module.getDisplayName());
        try {
            htmlDisplayName = module.getColor().colorizeDisplayName(XMLUtil.toElementContent(getDisplayName()));
        } catch (CharConversionException x) {
            htmlDisplayName = null;
        }
        setIconBaseWithExtension(module.getColor().iconBase());
    }

    public @Override String getHtmlDisplayName() {
        return htmlDisplayName;
    }

    public @Override Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();
        actions.add(UI.showConsoleAction(module));
        actions.add(UI.showFailuresAction());
        actions.add(null);
        if (module instanceof OpenableInBrowser) {
            actions.add(OpenUrlAction.forOpenable((OpenableInBrowser) module));
        }
        return actions.toArray(new Action[actions.size()]);
    }

    private static Children makeChildren(final HudsonMavenModuleBuild module) {
        return Children.create(new ChildFactory<Object>() {
            final Object ARTIFACTS = new Object();
            protected boolean createKeys(List<Object> toPopulate) {
                // XXX is it possible to cheaply check in advance if the build has any artifacts?
                if (module.getArtifacts() != null) {
                    toPopulate.add(ARTIFACTS);
                }
                return true;
            }
            protected @Override Node createNodeForKey(Object key) {
                assert key == ARTIFACTS : key;
                return new HudsonArtifactsNode(module);
            }
        }, false);
    }

}
