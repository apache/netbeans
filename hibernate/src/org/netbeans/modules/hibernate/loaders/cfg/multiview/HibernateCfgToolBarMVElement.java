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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hibernate.loaders.cfg.multiview;

import org.netbeans.modules.hibernate.loaders.cfg.*;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.modules.hibernate.cfg.model.HibernateConfiguration;
import org.netbeans.modules.hibernate.cfg.model.SessionFactory;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.modules.xml.multiview.ui.SectionContainer;
import org.netbeans.modules.xml.multiview.ui.SectionContainerNode;
import org.netbeans.modules.xml.multiview.ui.SectionPanel;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * ToolBarMultiView for Hibernate Configuration file
 * 
 * @author Dongmei Cao
 */
@MultiViewElement.Registration(
    mimeType=HibernateCfgDataLoader.REQUIRED_MIME,
    iconBase=HibernateCfgDataObject.ICON,
    persistenceType=TopComponent.PERSISTENCE_NEVER,
    preferredID=HibernateCfgDataObject.DESIGN_VIEW_ID,
    displayName="#LBL_Design",
    position=2540
)
public class HibernateCfgToolBarMVElement extends ToolBarMultiViewElement {

    public static final String PROPERTIES = "Properties";
    public static final String JDBC_PROPS = "JDBC Properties";
    public static final String DATASOURCE_PROPS = "Datasource Properties";
    public static final String OPTIONAL_PROPS = "Optional Properties";
    public static final String CONFIGURATION_PROPS = "Configuration Properties";
    public static final String JDBC_CONNECTION_PROPS = "JDBC and Connection Properties";
    public static final String CACHE_PROPS = "Cache Properties";
    public static final String TRANSACTION_PROPS = "Transaction Properties";
    public static final String MISCELLANEOUS_PROPS = "Miscellaneous Properties";
    public static final String MAPPINGS = "Mappings";
    public static final String CLASS_CACHE = "Class Cache";
    public static final String COLLECTION_CACHE = "Collection Cache";
    public static final String CACHE = "Cache";
    public static final String SECURITY = "Security";
    private ConfigurationView view;
    private ToolBarDesignEditor comp;
    private HibernateCfgDataObject configDataObject;
    private HibernateCfgPanelFactory factory;
    private Project project;

    public HibernateCfgToolBarMVElement(Lookup lookup){
        this(lookup.lookup(HibernateCfgDataObject.class));
    }
    
    public HibernateCfgToolBarMVElement(HibernateCfgDataObject dObj) {
        super(dObj);
        this.configDataObject = dObj;
        this.project = FileOwnerQuery.getOwner(dObj.getPrimaryFile());

        comp = new ToolBarDesignEditor();
        factory = new HibernateCfgPanelFactory(comp, dObj);
        setVisualEditor(comp);
    }

    public SectionView getSectionView() {
        return view;
    }

    @Override
    public void componentOpened() {
        super.componentOpened();

    }

    @Override
    public void componentClosed() {
        super.componentClosed();

    }

    @Override
    public void componentShowing() {
        super.componentShowing();
        view = new ConfigurationView(configDataObject);
        
        if (!configDataObject.viewCanBeDisplayed()) {
            view.setRoot(Node.EMPTY);
            comp.setContentView(view);
            return;
        }

        view.initialize();
        comp.setContentView(view);

        Object lastActive = comp.getLastActive();
        if (lastActive != null) {
            view.openPanel(lastActive);
        } else {
            // Expand the first node in session factory if there is one
            Node childrenNodes[] = view.getSessionFactoryContainerNode().getChildren().getNodes();
            if (childrenNodes.length > 0) {
                view.selectNode(childrenNodes[0]);
            }
        }

        view.checkValidity();

    }

    private class ConfigurationView extends SectionView {

        private HibernateCfgDataObject configDataObject;
        private Node securityNode;
        private Node sessionFactoryContainerNode;

        ConfigurationView(HibernateCfgDataObject dObj) {
            super(factory);
            configDataObject = dObj;
        }

        public Node getSessionFactoryContainerNode() {
            return this.sessionFactoryContainerNode;

        }

        public Node getSecurityNode() {
            return this.securityNode;
        }

        /**
         * Initialize the view
         */
        void initialize() {

            HibernateConfiguration configuration = configDataObject.getHibernateConfiguration();
            if(configuration == null) {
                // Should never happen
                return;
            }

            // Node for JDBC properties
            Node jdbcPropsNode = new ElementLeafNode(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Jdbc_Properties"));
            // Node for data source properties
            Node datasourcePropsNode = new ElementLeafNode(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Datasource_Properties"));
            // Node for optional configuration properties
            Node configPropsNode = new ElementLeafNode(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Hibernate_Configuration_properties"));
            // Node for optional JDBC and Connection properties
            Node jdbcConnPropsNode = new ElementLeafNode(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Jdbc_Connection_Properties"));
            // Node for optional cache properties
            Node cachePropsNode = new ElementLeafNode(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Cache_Properties"));
            // Node for optional transaction properties
            Node transactionPropsNode = new ElementLeafNode(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Transaction_Properties"));
            // Node for optional miscellaneous properties
            Node miscPropsNode = new ElementLeafNode(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Miscellaneous_Properties"));

            // Container node for optional properties
            Children optionalPropsCh = new Children.Array();
            optionalPropsCh.add(new Node[]{configPropsNode, jdbcConnPropsNode, cachePropsNode, transactionPropsNode, miscPropsNode});
            SectionContainerNode optionalPropsContainerNode = new SectionContainerNode(optionalPropsCh);
            optionalPropsContainerNode.setDisplayName(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Optional_Properties"));

            SectionContainer optionalPropsContainer = new SectionContainer(this, optionalPropsContainerNode,
                    NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Optional_Properties"));
            optionalPropsContainer.addSection(new SectionPanel(this, configPropsNode, configPropsNode.getDisplayName(), HibernateCfgToolBarMVElement.CONFIGURATION_PROPS, false, false));
            optionalPropsContainer.addSection(new SectionPanel(this, jdbcConnPropsNode, jdbcConnPropsNode.getDisplayName(), HibernateCfgToolBarMVElement.JDBC_CONNECTION_PROPS, false, false));
            optionalPropsContainer.addSection(new SectionPanel(this, cachePropsNode, cachePropsNode.getDisplayName(), HibernateCfgToolBarMVElement.CACHE_PROPS, false, false));
            optionalPropsContainer.addSection(new SectionPanel(this, transactionPropsNode, transactionPropsNode.getDisplayName(), HibernateCfgToolBarMVElement.TRANSACTION_PROPS, false, false));
            optionalPropsContainer.addSection(new SectionPanel(this, miscPropsNode, miscPropsNode.getDisplayName(), HibernateCfgToolBarMVElement.MISCELLANEOUS_PROPS, false, false));

            // Node for the mappings inside the session-factory
            Node mappingsNode = new ElementLeafNode(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Mappings"));

            // Node for class-cache
            Node classCacheNode = new ElementLeafNode(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Class_Cache"));
            // Node for collection-cache
            Node collectionCacheNode = new ElementLeafNode(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Collection_Cache"));

            // Container Node for the cache inside the session-factory
            Children cacheCh = new Children.Array();
            cacheCh.add(new Node[]{classCacheNode, collectionCacheNode});
            SectionContainerNode cacheContainerNode = new SectionContainerNode(cacheCh);
            cacheContainerNode.setDisplayName(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Cache"));

            SectionContainer cacheCont = new SectionContainer(this, cacheContainerNode,
                    NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Cache"));
            cacheCont.addSection(new SectionPanel(this, classCacheNode, classCacheNode.getDisplayName(), HibernateCfgToolBarMVElement.CLASS_CACHE, false, false));
            cacheCont.addSection(new SectionPanel(this, collectionCacheNode, collectionCacheNode.getDisplayName(), HibernateCfgToolBarMVElement.COLLECTION_CACHE, false, false));

            // Nodes for events. One per event
            if(configuration.getSessionFactory() == null) {
                // Deal with a bad xml - missing <session-factory>. see issue 138154
                configuration.setSessionFactory( new SessionFactory());
            }
            // Container Node to contain the session factory child nodes
            Children sessionFactoryCh = new Children.Array();
            sessionFactoryCh.add(new Node[]{jdbcPropsNode, datasourcePropsNode, mappingsNode, cacheContainerNode, optionalPropsContainerNode});
            sessionFactoryContainerNode = new SectionContainerNode(sessionFactoryCh);
            sessionFactoryContainerNode.setDisplayName(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_SessionFactory"));

            SectionContainer sessionFactoryCont = new SectionContainer(this, sessionFactoryContainerNode,
                    NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_SessionFactory"));
            sessionFactoryCont.addSection(new SectionPanel(this, jdbcPropsNode, jdbcPropsNode.getDisplayName(), HibernateCfgToolBarMVElement.JDBC_PROPS, false, false));
            sessionFactoryCont.addSection(new SectionPanel(this, datasourcePropsNode, datasourcePropsNode.getDisplayName(), HibernateCfgToolBarMVElement.DATASOURCE_PROPS, false, false));
            sessionFactoryCont.addSection(new SectionPanel(this, mappingsNode, mappingsNode.getDisplayName(), HibernateCfgToolBarMVElement.MAPPINGS, false, false));
            sessionFactoryCont.addSection(cacheCont);
            sessionFactoryCont.addSection(optionalPropsContainer);

            // Node for security
            securityNode = new ElementLeafNode(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Security"));

            // Add the session-factory and security to the root node
            Children rootChildren = new Children.Array();
            rootChildren.add(new Node[]{sessionFactoryContainerNode, securityNode});
            Node root = new AbstractNode(rootChildren);

            // Add sections for the nodes
            addSection(sessionFactoryCont);
            addSection(new SectionPanel(this, securityNode, securityNode.getDisplayName(), HibernateCfgToolBarMVElement.SECURITY, false, false));

            setRoot(root);
        }

        @Override
        public Error validateView() {
            // There is nothing to validate
            return null;
        }
    }

    private class ElementLeafNode extends org.openide.nodes.AbstractNode {

        ElementLeafNode(String displayName) {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(displayName);
        }

        @Override
        public HelpCtx getHelpCtx() {
            //return new HelpCtx(HibernateCfgDataObject.HELP_ID_DESIGN_HIBERNATE_CONFIGURATION); //NOI18N
            return null;
        }
    }
}
