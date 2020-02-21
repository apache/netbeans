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

package org.netbeans.modules.cnd.classview;

import java.awt.BorderLayout;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.api.model.CsmModelState;
import org.netbeans.modules.cnd.api.model.CsmModelStateListener;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProjectRegistry;
import org.netbeans.modules.cnd.classview.resources.I18n;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 *
 */
public class ClassViewTopComponent extends TopComponent implements CsmModelListener, CsmModelStateListener {

    static final long serialVersionUID = 420172427347975689L;

    private static final String PREFERRED_ID = "classview"; //NOI18N

    private static transient ClassViewTopComponent DEFAULT;
    
    public static final String OPENED_PREFERENCE = "ClassViewWasOpened"; // NOI18N

    private transient ClassView view;

    private transient boolean modelOn = true;
    
    public ClassViewTopComponent() {
        //if( Diagnostic.DEBUG ) Diagnostic.traceStack("ClassViewTopComponent .ctor #" + (++cnt));
    }

    /** Return preferred ID */
    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    // VK: code is copied from org.netbeans.modules.favorites.Tab class
    /** Finds default instance. Use in client code instead of {@link #getDefault()}. */
    public static synchronized ClassViewTopComponent findDefault() {
        if (DEFAULT == null) {
            TopComponent tc = WindowManager.getDefault().findTopComponent(PREFERRED_ID); // NOI18N
            //if( ! (tc instanceof ClassViewTopComponent) ) {
            if (DEFAULT == null) {
                ErrorManager.getDefault().log(ErrorManager.WARNING, 
                        "Cannot find ClassView component. It will not be located properly in the window system."); // NOI18N
//                DEFAULT = new ClassViewTopComponent();
//                // XXX Look into getDefault method.
//                DEFAULT.scheduleValidation();
                getDefault();
            }
        }

        return DEFAULT;
    }

    /** Gets default instance. Don't use directly, it reserved for deserialization routines only,
     * e.g. '.settings' file in xml layer, otherwise you can get non-deserialized instance. */
    public static synchronized ClassViewTopComponent getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new ClassViewTopComponent();
            // put a request for later validation
            // we must do this here, because of ExplorerManager's deserialization.
            // Root context of ExplorerManager is validated AFTER all other
            // deserialization, so we must wait for it
            //$ DEFAULT.scheduleValidation();
        }

        return DEFAULT;
    }

    public Object readResolve() throws java.io.ObjectStreamException {
        //return getDefault();
        if (DEFAULT == null) {
            DEFAULT = this;
            //$ DEFAULT.scheduleValidation();
        }
        return this;
    }

    /** Overriden to explicitely set persistence type of ProjectsTab
     * to PERSISTENCE_ALWAYS */
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    public static final String ICON_PATH = "org/netbeans/modules/cnd/classview/resources/class_view.png"; // NOI18N

    @Override
    protected void componentOpened() {
        if( Diagnostic.DEBUG ) {Diagnostic.trace("ClassesTC: componentOpened()");} // NOI18N
        if (view == null) {
            view = new ClassView();
            setLayout(new BorderLayout());
            setToolTipText(I18n.getMessage("ClassViewTitle")); // NOI18N
            setName(I18n.getMessage("ClassViewTooltip")); // NOI18N
            setIcon(ImageUtilities.loadImage(ICON_PATH));
        }
        view.startup();
        addRemoveModelListeners(true);
        if( NativeProjectRegistry.getDefault().getOpenProjects().isEmpty() ) {
            removeAll();
            add(createEmptyContent(), BorderLayout.CENTER);
        } else {
            add(view, BorderLayout.CENTER);
        }
    }

    private boolean isAutoMode = false;
    public void closeImplicit(){
        isAutoMode = true;
        close();
    }
    
    public void selectInClasses(CsmOffsetableDeclaration decl){
        if (view != null) {
            view.selectInClasses(decl);
        }
    }
    
    @Override
    protected void componentClosed() {
        if( Diagnostic.DEBUG ) {Diagnostic.trace("ClassesTC: componentClosed()");} // NOI18N
        if (!isAutoMode) {
            Preferences ps = NbPreferences.forModule(ClassViewTopComponent.class);
            ps.putBoolean(ClassViewTopComponent.OPENED_PREFERENCE, true); // NOI18N
        }
        isAutoMode = false;
        addRemoveModelListeners(false);
        if (view != null) {
            // paranoia
            view.shutdown();
            // clearing of view doesn't work. Opening component do not see mouse actions.
            //view = null;
        }
    }

    @Override
    protected void componentActivated() {
        if( Diagnostic.DEBUG ) {Diagnostic.trace("ClassesTC: componentActivated()");} // NOI18N
        super.componentActivated();
        view.requestFocus();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("UsingClassView"); // NOI18N
    }

    private void addRemoveModelListeners(boolean add) {
        if (add) {
            CsmListeners.getDefault().addModelListener(this);
            CsmListeners.getDefault().addModelStateListener(this);
        } else {
            CsmListeners.getDefault().removeModelListener(this);
            CsmListeners.getDefault().removeModelStateListener(this);
        }
    }

    @Override
    public void modelStateChanged(CsmModelState newState, CsmModelState oldState) {
        switch(newState) {
            case ON:
                modelOn = true;
                break;
            case CLOSING:
                modelOn = false;
                if( Diagnostic.DEBUG ) {Diagnostic.trace("ClassesTC: model switched off");} // NOI18N
                break;
        }
    }

    @Override
    public void projectOpened(CsmProject project) {
        if (!modelOn){
            return;
        }
        if (view != null) {
            view.projectOpened(project);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    removeAll();
                    add(view, BorderLayout.CENTER);
                    validate();
                }
            });
        }
    }

    @Override
    public void projectClosed(CsmProject project) {
        if (!modelOn){
            return;
        }
        if (view != null) {
            view.projectClosed(project);
        }
        if (CsmModelAccessor.getModel().projects().isEmpty()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    removeAll();
                    add(createEmptyContent(), BorderLayout.CENTER);
                    validate();
                }
            });
        }
    }

    private JComponent createEmptyContent() {
        JButton res = new JButton(I18n.getMessage("NoProjectOpen")); // NOI18N
        res.setEnabled(false);
        res.setBorder(BorderFactory.createEmptyBorder());
        res.setBackground(new JTextArea().getBackground());
        return res;
    }

    @Override
    public void modelChanged(CsmChangeEvent e) {
        if (!modelOn){
            return;
        }
        if (view != null) {
            view.modelChanged(e);
        }
    }
}
