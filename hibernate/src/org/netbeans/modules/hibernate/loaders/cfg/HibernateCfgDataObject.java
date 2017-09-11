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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.hibernate.loaders.cfg;

import java.awt.Image;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.modules.hibernate.cfg.model.HibernateConfiguration;
import org.netbeans.modules.hibernate.cfg.model.Security;
import org.netbeans.modules.hibernate.cfg.model.SessionFactory;
import org.netbeans.modules.hibernate.loaders.HbXmlMultiViewEditorSupport;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.Schema2BeansException;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport;
import org.netbeans.modules.xml.multiview.XmlMultiViewElement;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObjectExistsException;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Represents the Hibernate Configuration file
 * 
 * @author Dongmei Cao
 */
@MIMEResolver.NamespaceRegistration(
    mimeType=HibernateCfgDataLoader.REQUIRED_MIME,
    displayName="org.netbeans.modules.hibernate.resources.Bundle#HibernateCfgResolver",
    doctypePublicId="-//Hibernate/Hibernate Configuration DTD 3.0//EN",
    position=1500
)
public class HibernateCfgDataObject extends XmlMultiViewDataObject {

    public static final int UPDATE_DELAY = 200;
    public static final String SOURCE_VIEW_ID = "hibernate_configuration_multiview_source"; // NOI18N
    public static final String DESIGN_VIEW_ID = "hibernate_configuration_multiview_design"; // NOI18N
    private HibernateConfiguration configuration;
    private ModelSynchronizer modelSynchronizer;
    public static final String ICON = "org/netbeans/modules/hibernate/resources/hibernate-configuration.png"; //NOI18N
    private static final Logger LOG = Logger.getLogger(HibernateCfgDataObject.class.getName());
    /**
     * The property name for the event fired when a security tag is added or removed
     */
    private static final String SECURITY_ADDED_OR_REMOVED = "security_added_or_removed";

    public HibernateCfgDataObject(FileObject pf, HibernateCfgDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        
        // Make sure to reset the MIME type here. See bug 127051
        getEditorSupport().setMIMEType(HibernateCfgDataLoader.REQUIRED_MIME);

        // Synchronize between the vew and XML file
        modelSynchronizer = new ModelSynchronizer(this);

        CookieSet cookies = getCookieSet();
        //cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
        org.xml.sax.InputSource in = DataObjectAdapters.inputSource(this);
        CheckXMLCookie checkCookie = new CheckXMLSupport(in);
        cookies.add(checkCookie);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        cookies.add(validateCookie);
        parseDocument();
    }

    /**
     * Parses the document.
     * 
     * @return true if document could be parsed (it was valid), false otwherwise.
     */
    public boolean parseDocument() {
        if (configuration == null) {
            try {
                configuration = getHibernateConfiguration();
            } catch (RuntimeException ex) { // must catch RTE (thrown by schema2beans when document is not valid)
                return false;
            } 
        } else {
            try {
                java.io.InputStream is = getEditorSupport().getInputStream();
                HibernateConfiguration newConfiguration;
                try {
                    newConfiguration = HibernateConfiguration.createGraph(is);
                } catch (RuntimeException ex) { // must catch RTE (thrown by schema2beans when document is not valid)
                    return false;
                }
                if (newConfiguration != null) {
                    try {
                        configuration.merge(new HibernateConfiguration(), BaseBean.MERGE_UPDATE);//need to refresh with inner nodes, see #187592 and PUDataObject
                        configuration.merge(newConfiguration, BaseBean.MERGE_UPDATE);
                    } catch (IllegalArgumentException iae) {
                        return false;
                    }
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
                return false;
            } catch (IllegalStateException e) {
                //issue 198676, sometimes faled to parser document if it's changed during update, just skip, should be parsed with next event
                LOG.log(Level.INFO, null, e);
                return false;
            }
        }
        return true;
    }

    @Override
    protected String getEditorMimeType() {
        return HibernateCfgDataLoader.REQUIRED_MIME;
    }
    
    @Override
    protected int getXMLMultiViewIndex(){
        return 1;
    }
    
    @MultiViewElement.Registration(
        mimeType=HibernateCfgDataLoader.REQUIRED_MIME,
        iconBase=ICON,
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID=SOURCE_VIEW_ID,
        displayName="#CTL_SourceTabCaption",
        position=2550
    )
    @NbBundle.Messages("CTL_SourceTabCaption=Source")
    public static XmlMultiViewElement createXmlMultiViewElement(Lookup lookup) {
        return new XmlMultiViewElement(lookup.lookup(XmlMultiViewDataObject.class));
    }    
    
    /**
     * Checks whether the preferred view can be displayed and switches to the
     * xml view and displays an appropriate warning if not. In case that
     * the preferred view is the design view, it
     * can be displayed if <ol><li>document is valid (parseable) and</li>
     *<li>the target server is attached></li></ol>.
     *@return true if the preferred view can be displayed, false otherwise.
     */
    public boolean viewCanBeDisplayed() {
        
        boolean switchView = false;
        NotifyDescriptor nd = null;
        
        if (!parseDocument() && getSelectedPerspective().preferredID().startsWith(DESIGN_VIEW_ID)) {
            nd = new org.openide.NotifyDescriptor.Message(
                    NbBundle.getMessage(HibernateCfgDataObject.class, "TXT_DocumentUnparsable",
                    getPrimaryFile().getNameExt()), NotifyDescriptor.WARNING_MESSAGE);
            switchView = true;
            
        } 
        
        if (switchView){
            DialogDisplayer.getDefault().notify(nd);
            // postpone the "Switch to XML View" action to the end of event dispatching thread
            // this enables to finish the current action first (e.g. painting particular view)
            // see the issue 67580
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    goToXmlView();
                }
            });
        }
        return !switchView;

    }

    /**
     * Gets the object graph representing the contents of the 
     * Hibernate configuration file with which this data object 
     * is associated.
     *
     * @return the persistence graph.
     */
    public HibernateConfiguration getHibernateConfiguration() {
        if (configuration == null) {
            try {
                configuration = HibernateCfgMetadata.getDefault().getRoot(getPrimaryFile());
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            }
        }
        assert configuration != null;
        return configuration;
    }
    
    /**
     *  Adds the session Factory model object to the HibernateConfiguration. 
     *  @param sFactory
     */

    public void addSessionFactory(SessionFactory sFactory) {
        getHibernateConfiguration().setSessionFactory(sFactory);
        modelUpdatedFromUI();
    }

    /**
     * Saves the document.
     * @see EditorCookie#saveDocument
     */
    public void save() {
        EditorCookie edit = (EditorCookie) getLookup().lookup(EditorCookie.class);
        if (edit != null) {
            try {
                edit.saveDocument();
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            }
        }
    }
    /**
     * Override this method to workaround issue 
     * http://www.netbeans.org/issues/show_bug.cgi?id=128211
     */
    @Override
    protected synchronized XmlMultiViewEditorSupport getEditorSupport() {
        if(editorSupport == null) {
            editorSupport = new HbXmlMultiViewEditorSupport(this);
            editorSupport.getMultiViewDescriptions();
        }
        return editorSupport;
    }
    
    @Override
    protected Node createNodeDelegate() {
        return new HibernateCfgDataNode(this);
    }

    @Override
    protected String getPrefixMark() {
        return null;
    }

    public void modelUpdatedFromUI() {
        setModified(true);
        modelSynchronizer.requestUpdateData();
    }

    @Override
    protected Image getXmlViewIcon() {
        return ImageUtilities.loadImage("org/netbeans/modules/hibernate/resources/hibernate-configuration.png");
    }

    /** 
     * Enable to focus specific object in Multiview Editor
     * The default implementation opens the XML View
     */
    @Override
    public void showElement(Object element) {
        Object target = null;
        if (element instanceof SessionFactory ||
                element instanceof Security) {
            openView(0);
            target = element;
        }

        if (target != null) {
            final Object key = target;
            org.netbeans.modules.xml.multiview.Utils.runInAwtDispatchThread(new Runnable() {

                @Override
                public void run() {
                    getActiveMultiViewElement0().getSectionView().openPanel(key);
                }
            });
        }
    }

    /** 
     * Enable to get active MultiViewElement object
     */
    public ToolBarMultiViewElement getActiveMultiViewElement0() {
        return (ToolBarMultiViewElement) super.getActiveMultiViewElement();
    }

    private class ModelSynchronizer extends XmlMultiViewDataSynchronizer {

        public ModelSynchronizer(XmlMultiViewDataObject dataObject) {
            super(dataObject, UPDATE_DELAY);
        }

        @Override
        protected boolean mayUpdateData(boolean allowDialog) {
            return true;
        }

        @Override
        protected void updateDataFromModel(Object model, FileLock lock, boolean modify) {
            if (model == null) {
                return;
            }
            try {
                Writer out = new StringWriter();
                ((HibernateConfiguration) model).write(out);
                out.close();
                getDataCache().setData(lock, out.toString(), modify);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
            } catch (Schema2BeansException e) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
            } finally {
                if (lock != null) {
                    lock.releaseLock();
                }
            }
        }

        @Override
        protected Object getModel() {
            return getHibernateConfiguration();
        }

        @Override
        protected void reloadModelFromData() {
            parseDocument();
        }
    }
}
