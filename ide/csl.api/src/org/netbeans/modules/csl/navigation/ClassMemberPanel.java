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
