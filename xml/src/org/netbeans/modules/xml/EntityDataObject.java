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
package org.netbeans.modules.xml;

import java.io.IOException;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.loaders.*;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.actions.EditAction;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;

import org.netbeans.modules.xml.text.TextEditorSupport;
import org.netbeans.modules.xml.sync.*;
import org.netbeans.modules.xml.cookies.*;

import org.netbeans.modules.xml.util.Util;
import org.netbeans.spi.xml.cookies.*;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.xml.sax.InputSource;

/** 
 * Object that provides main functionality for XML Entity data object.
 *
 * @author Libor Kramolis
 * @version 0.1
 */
@MIMEResolver.ExtensionRegistration(
    displayName="org.netbeans.modules.xml.resources.Bundle#ENTResolver",
    extension="ent",
    mimeType="text/xml-external-parsed-entity",
    position=60003
)
public final class EntityDataObject extends MultiDataObject implements XMLDataObjectLook {
    /** Serial Version UID */
    private static final long serialVersionUID = 2909112365229995364L;
    
    /** Default XML Entity MIME type. */
    public static final String MIME_TYPE = "text/xml-external-parsed-entity"; // NOI18N

    /** Delegate sync support */
    private transient Synchronizator synchronizator;

    /** Cookie Manager */
    private transient final DataObjectCookieManager cookieManager;

    
    public EntityDataObject (final FileObject obj, final UniFileLoader loader) throws DataObjectExistsException {
        super (obj, loader);

        CookieSet set = getCookieSet();
        set.add (cookieManager = new DataObjectCookieManager (this, set));
        
        final TextEditorSupport.TextEditorSupportFactory editorFactory =
            new TextEditorSupport.TextEditorSupportFactory (this, MIME_TYPE);
        editorFactory.registerCookies (set);

//         CookieSet.Factory treeEditorFactory = new TreeEditorCookieImpl.CookieFactoryImpl (this);
//         set.add (TreeEditorCookie.class, treeEditorFactory);

        // add check cookie
        InputSource in = DataObjectAdapters.inputSource(this);
        set.add(new CheckXMLSupport(in, CheckXMLSupport.CHECK_ENTITY_MODE));
        
//         new CookieManager (this, set, EntityCookieFactoryCreator.class);
        //enable "Save As"
        set.assign( SaveAsCapable.class, new SaveAsCapable() {
            public void saveAs(FileObject folder, String fileName) throws IOException {
                editorFactory.createEditor().saveAs( folder, fileName );
            }
        });
    }

    @MultiViewElement.Registration(
        displayName="org.netbeans.modules.xml.Bundle#CTL_SourceTabCaption",
        iconBase="org/netbeans/modules/xml/resources/entObject.gif",
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="entity.text",
        mimeType=MIME_TYPE,
        position=1
    )
    public static MultiViewEditorElement createMultiViewDTDElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }
    

    @Override
    public final Lookup getLookup() {
        return getCookieSet().getLookup();
    }

    /**
     */
    @Override
    protected Node createNodeDelegate () {
        return new EntityDataNode (this);
    }


    /** @return provider of sync interface.  */
    public synchronized Synchronizator getSyncInterface() {
        if (synchronizator == null) {
            synchronizator = new EntitySyncSupport (this);
        
        }
        return synchronizator;
    }

    public DataObjectCookieManager getCookieManager() {
        return cookieManager;
    }

    
    /**
     */
    @Override
    public HelpCtx getHelpCtx() {
        //return new HelpCtx (EntityDataObject.class);
        return HelpCtx.DEFAULT_HELP;
    }
        

    /**
     *
     */
    private static class EntityDataNode extends DataNode {

        /** Create new EntityDataNode. */
        public EntityDataNode (EntityDataObject obj) {
            super (obj, Children.LEAF);

            setDefaultAction (SystemAction.get (EditAction.class));
            setIconBase ("org/netbeans/modules/xml/resources/entObject"); // NOI18N
            setShortDescription(Util.THIS.getString(
                    EntityDataObject.class, "PROP_EntityDataNode_desc"));
        }

        /**
         */
        @Override
        public HelpCtx getHelpCtx() {
            //return new HelpCtx (EntityDataObject.class);
            return HelpCtx.DEFAULT_HELP;
        }
        
    } // end of class EntityDataNode

}
