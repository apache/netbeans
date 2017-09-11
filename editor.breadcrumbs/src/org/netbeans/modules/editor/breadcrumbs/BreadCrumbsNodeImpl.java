/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011-2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.breadcrumbs;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsElement;
import org.openide.actions.OpenAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Dusan Balek
 */
public class BreadCrumbsNodeImpl extends AbstractNode {

    private final BreadcrumbsElement element;
    
    public BreadCrumbsNodeImpl(BreadcrumbsElement element) {
        super(Children.create(new ChildrenFactoryImpl(element), false), new ProxyLookup(Lookups.singleton(element), element.getLookup()));
        this.element = element;
    }

    @Override
    public String getHtmlDisplayName() {
        return element.getHtmlDisplayName();
    }

    @Override
    public Action getPreferredAction() {
        return OpenAction.get(OpenAction.class);
    }

    @Override
    public boolean canCopy() {
        return false;
    }

    @Override
    public boolean canCut() {
        return false;
    }

    @Override
    public boolean canDestroy() {
        return false;
    }

    @Override
    public boolean canRename() {
        return false;
    }

    @Override
    public PasteType getDropType(Transferable t, int action, int index) {
        return null;
    }

    @Override
    public Transferable drag() throws IOException {
        return null;
    }

    @Override
    protected void createPasteTypes(Transferable t, List<PasteType> s) {
        // Do nothing
    }

    @Override
    public Image getIcon(int type) {
        return element.getIcon(type);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return element.getOpenedIcon(type);
    }

    private static final class ChildrenFactoryImpl extends ChildFactory<BreadcrumbsElement> {

        private final BreadcrumbsElement element;

        public ChildrenFactoryImpl(BreadcrumbsElement element) {
            this.element = element;
        }

        @Override
        protected boolean createKeys(final List<BreadcrumbsElement> toPopulate) {
            toPopulate.addAll(element.getChildren());
            return true;
        }

        @Override
        protected Node createNodeForKey(BreadcrumbsElement key) {
            return new BreadCrumbsNodeImpl(key);
        }
        
    }
    
}
