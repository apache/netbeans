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

package org.netbeans.modules.db.explorer.node;

import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.api.db.explorer.node.ChildNodeFactory;
import org.netbeans.lib.ddl.impl.SpecificationFactory;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * This is the root node for the database explorer.  This is a singleton
 * instance since the database explorer only uses 1 root node.
 * 
 * @author Rob Englander
 */ 
public class RootNode extends BaseNode {
    private static final String NAME = "Databases"; //NOI18N
    private static final String ICONBASE = "org/netbeans/modules/db/resources/database.gif"; //NOI18N
    private static final String FOLDER = "Root"; //NOI18N

    /** the singleton instance */
    private static RootNode instance = null;
    
    private SpecificationFactory factory;

    /**
     * Gets the singleton instance.
     *            
     * @return the singleton instance
     */
    @ServicesTabNodeRegistration(
        name="Databases",
        displayName="org.netbeans.modules.db.explorer.node.Bundle#RootNode_DISPLAYNAME",
        iconResource="org/netbeans/modules/db/resources/database.gif",
        position=101
    )
    public static RootNode instance() {
        if (instance == null) { 
            NodeDataLookup lookup = new NodeDataLookup();
            lookup.add(ConnectionList.getDefault());
            instance = new RootNode(lookup);
            instance.setup();
        }
        
        return instance;
    }

    public static boolean isCreated() {
        return instance != null;
    }

    /**
     * Constructor.  This is private to prevent multiple instances from
     * being created.
     * 
     * @param lookup the associated lookup
     */
    private RootNode(NodeDataLookup lookup) {
        super(new ChildNodeFactory(lookup), lookup, FOLDER, null);
    }
    
    protected void initialize() {
        try {
            factory = new SpecificationFactory();
            if (factory == null) {
                throw new Exception(
                        NbBundle.getMessage (RootNode.class, "EXC_NoSpecificationFactory"));
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    public SpecificationFactory getSpecificationFactory() {
        return factory;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage (RootNode.class, "RootNode_DISPLAYNAME"); // NOI18N
    }

    @Override
    public String getIconBase() {
        return ICONBASE;
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getMessage (RootNode.class, "ND_Root"); //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(RootNode.class);
    }
}
