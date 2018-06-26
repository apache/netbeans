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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.common.nodes;

import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.ResourceDecorator;
import org.netbeans.modules.glassfish.spi.ResourceDesc;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author Peter Williams
 */
public class Hk2ResourceNode extends Hk2ItemNode {

    public Hk2ResourceNode(final Lookup lookup, final ResourceDesc resource, 
            final ResourceDecorator decorator, final Class customizer) {
        super(Children.LEAF, lookup, resource.getName(), decorator);
        setDisplayName(resource.getName());
        setShortDescription("<html>name: " + resource.getName() + "</html>");

        if(decorator.canUnregister()) {
            getCookieSet().add(new Hk2Cookie.Unregister(lookup,
                    resource.getName(), resource.getCommandType(),
                    decorator.getCmdPropertyName(),
                    decorator.isCascadeDelete()));
        }

        if (decorator.canEditDetails()) {
            GlassfishModule m = lookup.lookup(GlassfishModule.class);
            if (null != m) {
                String rootDir = m.getInstanceProperties().get(GlassfishModule.GLASSFISH_FOLDER_ATTR);
                if (ServerUtilities.isTP2(rootDir)) {
                    // don't add the edit details cookie
                } else {
                    // add the editor cookie
                    getCookieSet().add(new Hk2Cookie.EditDetails(
                            lookup, getDisplayName(),
                            resource.getCommandType(), customizer));
                }

            }

        }
    }
}
