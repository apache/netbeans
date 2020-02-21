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
package org.netbeans.modules.cnd.source;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.modules.cnd.source.spi.CndPropertiesProvider;
import org.netbeans.modules.cnd.source.spi.RenameHandler;
import org.openide.actions.OpenAction;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

/**
 *  A base class for C/C++/Fortran (C-C-F) nodes.
 */
public class SourceDataNode extends DataNode {
    
    /** Constructor for this class */
    public SourceDataNode(DataObject obj, Lookup lookup, String icon) {
        super(obj, Children.LEAF, lookup);
        setIconBaseWithExtension(icon);
    }

    /**
     * Create the properties sheet for the node
     */
    @Override
    protected Sheet createSheet() {
        // Just add properties to default property tab (they used to be in a special 'Building Tab')
        Sheet defaultSheet = super.createSheet();
        CndPropertiesProvider.getDefault().addExtraProperties(this, defaultSheet);
        return defaultSheet;
    }

    /**
     *  Overrides default action from DataNode.
     *  Instantiate a template, if isTemplate() returns true.
     *  Opens otherwise.
     */
    @Override
    public Action getPreferredAction() {
        Action result = super.getPreferredAction();
        return result == null ? SystemAction.get(OpenAction.class) : result;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("Welcome_cpp_home"); // NOI18N
    }
    
    @Override
    public void setName(String name) {
        RenameHandler handler = getRenameHandler();
        if (handler == null) {
            super.setName(name);
        } else {
            try {
                handler.handleRename(SourceDataNode.this, name);
            } catch (IllegalArgumentException ioe) {
                super.setName(name);
            }
        }
    }

    private static synchronized RenameHandler getRenameHandler() {
        Collection<? extends RenameHandler> handlers = (Lookup.getDefault().lookupAll(RenameHandler.class));
        if (handlers.isEmpty()) {
            return null;
        }
        if (handlers.size() > 1) {
            LOG.log(Level.WARNING, "Multiple instances of RenameHandler found in Lookup; only using first one: {0}", handlers); //NOI18N
        }
        return handlers.iterator().next();
    }
    private static final Logger LOG = Logger.getLogger(SourceDataNode.class.getName());
}
