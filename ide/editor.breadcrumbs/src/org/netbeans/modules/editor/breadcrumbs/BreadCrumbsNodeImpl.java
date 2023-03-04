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
