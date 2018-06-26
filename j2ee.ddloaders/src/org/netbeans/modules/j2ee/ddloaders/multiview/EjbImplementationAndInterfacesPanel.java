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

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.EjbImplementationAndInterfacesForm;
import org.netbeans.modules.xml.multiview.ui.LinkButton;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;

/**
 * @author pfiala
 */
public class EjbImplementationAndInterfacesPanel extends EjbImplementationAndInterfacesForm {

    private static final Logger LOGGER = Logger.getLogger(EjbImplementationAndInterfacesForm.class.getName());
    
    private EntityAndSessionHelper helper;
    private NonEditableDocument beanClassDocument = new NonEditableDocument() {
        protected String retrieveText() {
            return helper == null ? null : helper.getEjbClass();
        }
    };
    private NonEditableDocument localComponentDocument = new NonEditableDocument() {
        protected String retrieveText() {
            return helper == null ? null : helper.getLocal();
        }
    };
    private NonEditableDocument localHomeDocument = new NonEditableDocument() {
        protected String retrieveText() {
            return helper == null ? null : helper.getLocalHome();
        }
    };
    private NonEditableDocument remoteComponentDocument = new NonEditableDocument() {
        protected String retrieveText() {
            return helper == null ? null : helper.getRemote();
        }
    };
    private NonEditableDocument remoteHomeDocument = new NonEditableDocument() {
        protected String retrieveText() {
            return helper == null ? null : helper.getHome();
        }
    };

    private static final String LINK_BEAN = "linkBean";
    private static final String LINK_LOCAL = "linkLocal";
    private static final String LINK_LOCAL_HOME = "linkLocalHome";
    private static final String LINK_REMOTE = "linkRemote";
    private static final String LINK_REMOTE_HOME = "linkRemoteHome";

    /**
     * Creates new form BeanForm
     */
    public EjbImplementationAndInterfacesPanel(final SectionNodeView sectionNodeView,
            final EntityAndSessionHelper helper) {
        super(sectionNodeView);
        this.helper = helper;
        getBeanClassTextField().setDocument(beanClassDocument);
        getLocalComponentTextField().setDocument(localComponentDocument);
        getLocalHomeTextField().setDocument(localHomeDocument);
        getRemoteComponentTextField().setDocument(remoteComponentDocument);
        getRemoteHomeTextField().setDocument(remoteHomeDocument);

        scheduleRefreshView();

        initLinkButton(getBeanClassLinkButton(), LINK_BEAN);
        initLinkButton(getLocalComponentLinkButton(), LINK_LOCAL);
        initLinkButton(getLocalHomeLinkButton(), LINK_LOCAL_HOME);
        initLinkButton(getRemoteComponentLinkButton(), LINK_REMOTE);
        initLinkButton(getRemoteHomeLinkButton(), LINK_REMOTE_HOME);
    }

    private void initLinkButton(AbstractButton button, String key) {
        LinkButton.initLinkButton(button, this, null, key);
    }

    @Override
    public void refreshView() {
        beanClassDocument.init();
        localComponentDocument.init();
        localHomeDocument.init();
        remoteComponentDocument.init();
        remoteHomeDocument.init();
    }

    @Override
    public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        if (source instanceof EntityAndSession) {
            scheduleRefreshView();
        }
    }

    @Override
    public void linkButtonPressed(Object ddBean, String ddProperty) {
        String javaClass = null;
        if(LINK_BEAN.equals(ddProperty)) {
            javaClass = helper.getEjbClass();
        } else if(LINK_LOCAL.equals(ddProperty)) {
            javaClass = helper.getLocal();
        } else if(LINK_LOCAL_HOME.equals(ddProperty)) {
            javaClass = helper.getLocalHome();
        } else if(LINK_REMOTE.equals(ddProperty)) {
            javaClass = helper.getRemote();
        } else if (LINK_REMOTE_HOME.equals(ddProperty)) {
            javaClass = helper.getHome();
        } 
        
        if (javaClass == null || "".equals(javaClass.trim())) {
            LOGGER.log(Level.INFO, "Could not resolve class for ddProperty:" + ddProperty ); //NO18N
            return;
        }
        Utils.openEditorFor(helper.ejbJarFile, javaClass);
    }
}
