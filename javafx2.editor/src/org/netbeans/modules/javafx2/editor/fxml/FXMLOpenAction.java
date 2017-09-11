/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.editor.fxml;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.javafx2.editor.spi.FXMLOpener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;
import org.openide.util.*;

/**
 * Custom open action. The opening itself is delgated to an instance of
 * {@linkplain FXMLOpener} if registered. If the provided opener fails
 * a fallback strategy of opening the XML file in editor is taken.
 * 
 * @author Jaroslav Bachorik
 */
@NbBundle.Messages("CTL_OpenAction=Open")
@ActionID(category="Edit", id="org.netbeans.modules.javafx2.editor.fxml.FXMLOpenAction")
@ActionReference(path="Loaders/text/x-fxml+xml/Actions", position=300)
@ActionRegistration(displayName="#CTL_OpenAction", lazy=false)
public class FXMLOpenAction extends AbstractAction implements ContextAwareAction, LookupListener {
    private final Lookup context;
    private Lookup.Result<DataObject> lkpInfo;
    
    private FXMLOpener opener;
    private FXMLOpener defaultOpener = new FXMLOpener() {
        @Override
        public boolean isEnabled(Lookup context) {
            return context.lookupAll(DataObject.class).size() == 1;
        }

        @Override
        public boolean open(Lookup context) {
            DataObject dobj = context.lookup(DataObject.class);
            OpenCookie oc = dobj.getCookie(OpenCookie.class);
            if (oc != null) {
                oc.open();
                return true;
            }
            return false;
        }
    };
    public FXMLOpenAction() {
        this(Utilities.actionsGlobalContext());
    }
    
    public FXMLOpenAction(Lookup context) {
        this.context = context;
        putValue(AbstractAction.NAME, Bundle.CTL_OpenAction());
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        
        setupOpener();
    }
    
    private void setupOpener() {
        opener = Lookup.getDefault().lookup(FXMLOpener.class);
    }
    
    void init() {
        assert SwingUtilities.isEventDispatchThread() 
               : "this shall be called just from AWT thread"; // NOI18N
 
        if (lkpInfo != null) {
            return;
        }
 
        //The thing we want to listen for the presence or absence of
        //on the global selection
        lkpInfo = context.lookupResult(DataObject.class);
        lkpInfo.addLookupListener(this);
        resultChanged(null);
    }
    
    @Override
    public boolean isEnabled() {
        init();
        return opener != null && opener.isEnabled(context) && super.isEnabled();
    }
 
    @Override
    public void resultChanged(LookupEvent ev) {
        setEnabled(!lkpInfo.allInstances().isEmpty());
    }
 
    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new FXMLOpenAction(context);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean isNewFile = Thread.currentThread().getStackTrace()[2].getFileName().equals("ProjectUtilities.java"); // NOI18N
        if (opener != null && !isNewFile) {
            opener.open(context);
        } else if (defaultOpener.isEnabled(context)) {
            defaultOpener.open(context);
        }
    }
}
