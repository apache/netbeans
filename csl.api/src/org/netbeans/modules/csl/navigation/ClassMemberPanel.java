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

package org.netbeans.modules.csl.navigation;

import javax.swing.JComponent;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 * <p>
 *
 * @author Tomas Zezula
 */
public class ClassMemberPanel implements NavigatorPanel {

    private ClassMemberPanelUI component;

    private static ClassMemberPanel INSTANCE;  //Apparently not accessed in event dispatch thread in CaretListeningTask

    private static final RequestProcessor RP = new RequestProcessor(ClassMemberPanel.class.getName(),1);
    
    //Bugfix BZ#191289 - switching between files doesn't change navigator content
    private Lookup.Result selection;
    private final LookupListener selectionListener = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            if(selection == null)
                return;
            ClassMemberNavigatorSourceFactory f = ClassMemberNavigatorSourceFactory.getInstance();
            if (f != null)
                f.firePropertyChangeEvent();
        }
    };


    public ClassMemberPanel() {
    }

    @Override
    public void panelActivated(final Lookup context) {
        assert context != null;
        INSTANCE = this;
        getClassMemberPanelUI().showWaitNode();

        //workaround #1: initialize schedulers, resp. the CSLNavigatorScheduler
        for(Scheduler s : Lookup.getDefault().lookupAll(Scheduler.class)) {
        }
        
        //Bugfix BZ#191289 - switching between files doesn't change navigator content
        selection = context.lookup(new Lookup.Template(DataObject.class));
        selection.addLookupListener(selectionListener);

        RP.post( new Runnable () {
            @Override
            public void run () {
                FileObject fileObject = context.lookup(FileObject.class);
                Language language = null;
                if (fileObject != null) {
                    language = LanguageRegistry.getInstance().getLanguageByMimeType(fileObject.getMIMEType());
                }
                ClassMemberNavigatorSourceFactory f = ClassMemberNavigatorSourceFactory.getInstance();
                if (f != null) {
                    f.setLookup(context, getClassMemberPanelUI(language));
                }
            }
        });
    }

    public void panelDeactivated() {
        getClassMemberPanelUI().showWaitNode(); // To clear the ui
        INSTANCE = null;

        //Bugfix BZ#191289 - switching between files doesn't change navigator content
        if(selection != null) {
            selection.removeLookupListener(selectionListener);
            selection = null;
        }

        //Even the setLookup(EMPTY) is fast, has to be called in RP to keep ordering
        RP.post( new Runnable () {
            @Override
            public void run () {
                ClassMemberNavigatorSourceFactory f = ClassMemberNavigatorSourceFactory.getInstance();
                if (f != null) {
                    f.setLookup(Lookup.EMPTY, null);
                }
            }
        });
    }

    public Lookup getLookup() {
        return this.getClassMemberPanelUI().getLookup();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ClassMemberPanel.class,"LBL_members");
    }

    public String getDisplayHint() {
        return NbBundle.getMessage(ClassMemberPanel.class,"HINT_members");
    }

    public JComponent getComponent() {
        return getClassMemberPanelUI();
    }

    public void selectElement(ParserResult info, int offset) {
        getClassMemberPanelUI().selectElementNode(info, offset);
    }
    
    private synchronized ClassMemberPanelUI getClassMemberPanelUI(Language language) {
        if (this.component == null) {
            this.component = new ClassMemberPanelUI(language);
        }
        return this.component;
    }

    private ClassMemberPanelUI getClassMemberPanelUI() {
        return getClassMemberPanelUI(null);
    }
    
    public static ClassMemberPanel getInstance() {
        return INSTANCE;
    }    
}
