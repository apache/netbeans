/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.demo.osgi.showbundles;

import java.awt.Image;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

/**
 * Remembers connection to BundleContext.
 */
public class Installer implements BundleActivator {
    private static BundleContext context;

    @Override
    public void start(BundleContext c) throws Exception {
        context = c;
    }

    @Override
    public void stop(BundleContext c) throws Exception {
        context = null;
    }

    public static Children createBundleChildren() {
        assert context != null;
        return new BundleChildren();
    }

    private static final class BundleChildren extends Children.Keys<Bundle>
    implements BundleListener {
        @Override
        protected void addNotify() {
            context.addBundleListener(this);
            bundleChanged(null);
        }

        @Override
        protected void removeNotify() {
            context.removeBundleListener(this);
        }

        @Override
        protected Node[] createNodes(Bundle t) {
            try {
                Node bn = new BeanNode<Bundle>(t) {
                    @Override
                    public Image getIcon(int type) {
                        if (getBean().getLocation().startsWith("netigso:/")) { // NOI18N
                            return ImageUtilities.loadImage("org/netbeans/demo/osgi/showbundles/module.png");
                        } else {
                            return ImageUtilities.loadImage("org/netbeans/demo/osgi/showbundles/bundle.png");
                        }
                    }

                    @Override
                    public Image getOpenedIcon(int type) {
                        return getIcon(type);
                    }
                };
                bn.setName(t.getSymbolicName());
                return new Node[]{bn};
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }

        @Override
        public void bundleChanged(BundleEvent be) {
            setKeys(context.getBundles());
        }
    }
}
