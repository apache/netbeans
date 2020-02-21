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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
