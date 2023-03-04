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
package org.netbeans.modules.web.beans.navigation.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.JTextComponent;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.beans.CdiUtil;
import org.netbeans.modules.web.beans.MetaModelSupport;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.navigation.actions.ModelActionStrategy.InspectActionId;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class InspectCDIAtCaretAction extends AbstractWebBeansAction {
    
    private static final long serialVersionUID = -4505119467924502377L;
    
    private static final String INSPECT_CDI_AT_CARET =
        "inspect-cdi-at-caret";                     // NOI18N
    
    private static final String INSPECT_CDI_AT_CARET_POPUP =
        "inspect-cdi-at-caret-popup";               // NOI18N

    public InspectCDIAtCaretAction( ) {
        super(NbBundle.getMessage(InspectCDIAtCaretAction.class, 
                INSPECT_CDI_AT_CARET));
        myStrategies = new ArrayList<ModelActionStrategy>( 4 );
        /*
         *  The order is important !
         *  EventsActionStartegy should be after InjectablesActionStrategy 
         *  because it cares about several action ids.
         */
        myStrategies.add( new ObserversActionStrategy());
        myStrategies.add( new InjectablesActionStrategy());
        myStrategies.add( new DecoratoresActionStrategy());
        myStrategies.add( new InterceptorsActionStrategy());
        myStrategies.add( new EventsActionStartegy());
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.actions.AbstractWebBeansAction#modelAcessAction(org.netbeans.modules.web.beans.api.model.WebBeansModel, org.netbeans.modules.j2ee.metadata.model.api.MetadataModel, java.lang.Object[], javax.swing.text.JTextComponent, org.openide.filesystems.FileObject)
     */
    @Override
    protected void modelAcessAction( WebBeansModel model,
            MetadataModel<WebBeansModel> metaModel, Object[] subject,
            JTextComponent component, FileObject fileObject )
    {
        InspectActionId id = (InspectActionId) subject[2];
        for( ModelActionStrategy strategy : myStrategies ){
            if ( strategy.isApplicable( id ) && strategy.isApplicable( model ,
                    subject ))
            {
                strategy.invokeModelAction(model, metaModel, subject, 
                        component, fileObject);
                return;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.actions.AbstractWebBeansAction#getActionCommand()
     */
    @Override
    protected String getActionCommand() {
        return INSPECT_CDI_AT_CARET;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.actions.AbstractWebBeansAction#getPopupMenuKey()
     */
    @Override
    protected String getPopupMenuKey() {
        return INSPECT_CDI_AT_CARET_POPUP;
    }

    /* (non-Javadoc)
     * @see org.netbeans.editor.BaseAction#actionPerformed(java.awt.event.ActionEvent, javax.swing.text.JTextComponent)
     */
    @Override
    public void actionPerformed( ActionEvent event, final JTextComponent component ) {
        if ( component == null ){
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        final FileObject fileObject = NbEditorUtilities.getFileObject( 
                component.getDocument());
        if ( fileObject == null ){
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        Project project = FileOwnerQuery.getOwner( fileObject );
        if ( project == null ){
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        
        String msg = null;
        if ( event == null ){
            msg ="USG_CDI_INSPECT_CDI_GLYPH";    // NOI18N
        }
        else {
            msg = "USG_CDI_INSPECT_CDI";         // NOI18N
        }
        CdiUtil logger = project.getLookup().lookup(CdiUtil.class);
        if (logger != null) {
            logger.log(msg, 
                    InspectCDIAtCaretAction.class, new Object[] { project
                            .getClass().getName() });
        }

        MetaModelSupport support = new MetaModelSupport(project);
        final MetadataModel<WebBeansModel> metaModel = support.getMetaModel();
        if ( metaModel == null ){
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        
        /*
         *  this list will contain variable element name and TypeElement 
         *  qualified name which contains variable element. 
         */
        final Object[] subject = new Object[3];
        if ( !findContext(component, subject)){
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(
                            WebBeansActionHelper.class, "LBL_NoCdiContext"));
            return;
        }
        try {
            metaModel.runReadActionWhenReady( new MetadataModelAction<WebBeansModel, Void>() {

                @Override
                public Void run( WebBeansModel model ) throws Exception {
                    modelAcessAction(model, metaModel, subject, component, 
                            fileObject);
                    return null;
                }
            });
        }
        catch (MetadataModelException e) {
            Logger.getLogger( AbstractInjectableAction.class.getName()).
                log( Level.INFO, e.getMessage(), e);
        }
        catch (IOException e) {
            Logger.getLogger( AbstractInjectableAction.class.getName()).
                log( Level.WARNING, e.getMessage(), e);
        }
    }

    protected boolean findContext( final JTextComponent component,
            final Object[] subject )
    {
        return WebBeansActionHelper.getVariableElementAtDot( component, 
                subject , false ) || WebBeansActionHelper.
                getContextEventInjectionAtDot( component, subject  ) ||
                    WebBeansActionHelper.getMethodAtDot(component, subject) || 
                        WebBeansActionHelper.getClassAtDot(component, subject);
    }
    
    private List<ModelActionStrategy> myStrategies;

}
