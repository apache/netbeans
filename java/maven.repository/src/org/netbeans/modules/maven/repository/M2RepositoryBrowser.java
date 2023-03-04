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
package org.netbeans.modules.maven.repository;

import java.awt.event.ActionEvent;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.maven.cli.configuration.SettingsXmlConfigurationProcessor;
import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.netbeans.modules.maven.indexer.api.QueryField;
import org.netbeans.modules.maven.indexer.api.RepositoryIndexer;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import static org.netbeans.modules.maven.repository.Bundle.*;
import org.netbeans.modules.maven.repository.register.RepositoryRegisterUI;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Union2;

@ServicesTabNodeRegistration(name=M2RepositoryBrowser.NAME, displayName="#CTL_M2RepositoryBrowserTopComponent", shortDescription="#HINT_M2RepositoryBrowserTopComponent", iconResource=M2RepositoryBrowser.ICON_PATH, position=431)
@Messages({
    "# {0} - either empty string or CTL_M2RepositoriesDisabled",
    "CTL_M2RepositoryBrowserTopComponent2=Maven Repositories{0}",
    "CTL_M2RepositoryBrowserTopComponent=Maven Repositories",
    "CTL_M2RepositoriesDisabled= (Indexing disabled)",
    "LBL_Add_Repo=Add Repository",
    "ACT_Add_Repo=Add Repository...",
    "HINT_M2RepositoryBrowserTopComponent=Displays contents of local and remote Apache Maven repositories and permits them to be searched and indexed."
})
public final class M2RepositoryBrowser extends AbstractNode {
    private static final RequestProcessor RP = new RequestProcessor(M2RepositoryBrowser.class);

    static final String NAME = "M2RepositoryBrowser";
    static final /* XXX javac bug @StaticResource */ String ICON_PATH = "org/netbeans/modules/maven/repository/MavenRepoBrowser.png";

    private M2RepositoryBrowser() {
        super(Children.create(new RootNodes(), true));
        setName(NAME);
        setDisplayName(CTL_M2RepositoryBrowserTopComponent2(RepositoryPreferences.isIndexRepositories() ? "" : CTL_M2RepositoriesDisabled()));
        setShortDescription(HINT_M2RepositoryBrowserTopComponent());
        setIconBaseWithExtension(ICON_PATH);
        NbPreferences.root().node("org/netbeans/modules/maven/nexus/indexing").addPreferenceChangeListener(new PreferenceChangeListener() {

            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                if (RepositoryPreferences.PROP_INDEX.equals(evt.getKey())) {
                    setDisplayName(CTL_M2RepositoryBrowserTopComponent2(RepositoryPreferences.isIndexRepositories() ? "" : CTL_M2RepositoriesDisabled()));
                }
            }
        });
    }

    @Override public Action[] getActions(boolean context) {
        return new Action[] {
            new SearchAction(),
            new AddAction()
        };
    }
    
    

    private static class AddAction extends AbstractAction {
        AddAction() {
            super(ACT_Add_Repo());
        }
        @Override public void actionPerformed(ActionEvent e) {
            final RepositoryRegisterUI rrui = new RepositoryRegisterUI();
            rrui.getAccessibleContext().setAccessibleDescription(LBL_Add_Repo());
            DialogDescriptor dd = new DialogDescriptor(rrui, LBL_Add_Repo());
            dd.setClosingOptions(new Object[]{
                        rrui.getButton(),
                        DialogDescriptor.CANCEL_OPTION
                    });
            dd.setOptions(new Object[]{
                        rrui.getButton(),
                        DialogDescriptor.CANCEL_OPTION
                    });
            Object ret = DialogDisplayer.getDefault().notify(dd);
            if (rrui.getButton() == ret) {
                final  RepositoryInfo info;
                try {
                    info = rrui.getRepositoryInfo();
                } catch (URISyntaxException x) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(x.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE));
                    return;
                }
                RepositoryPreferences.getInstance().addOrModifyRepositoryInfo(info);
                RP.post(new Runnable() {
                        @Override public void run() {
                            RepositoryIndexer.indexRepo(info);
                        }
                    });
            }
        }
    }

    private class SearchAction extends AbstractAction {
        @Messages("LBL_REPO_Find=Find...")
        SearchAction() {
            super(LBL_REPO_Find());
        }
        @Messages("TIT_Find_In_Repositories=Find in Repositories")
        @Override public void actionPerformed(ActionEvent e) {
            final FindInRepoPanel pnl = new FindInRepoPanel();
            pnl.getAccessibleContext().setAccessibleDescription(TIT_Find_In_Repositories());
            final DialogDescriptor dd = new DialogDescriptor(pnl, TIT_Find_In_Repositories());
            pnl.attachDesc(dd);
            Object ret = DialogDisplayer.getDefault().notify(dd);
            if (ret == DialogDescriptor.OK_OPTION) {
                QueryRequest request = new QueryRequest(pnl.getQuery(), RepositoryPreferences.getInstance().getRepositoryInfos());
                synchronized (searches) {
                    searches.add(request);
                }
                cs.fireChange();
                //TODO we need to find, select and expand the node of the query here.
            }
        }
    }

    private static final List<QueryRequest> searches = new ArrayList<QueryRequest>();
    static void remove(QueryRequest search) {
        synchronized (searches) {
            searches.remove(search);
        }
        cs.fireChange();
    }
    private static final ChangeSupport cs = new ChangeSupport(M2RepositoryBrowser.class);
    public static void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }
    public static void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }

    private static class RootNodes extends ChildFactory.Detachable<Union2<RepositoryInfo,QueryRequest>> implements ChangeListener, FileChangeListener {
        boolean addNotifyCalled = false;
        @Override protected boolean createKeys(List<Union2<RepositoryInfo,QueryRequest>> toPopulate) {
            for (RepositoryInfo info : RepositoryPreferences.getInstance().getRepositoryInfos()) {
                toPopulate.add(Union2.<RepositoryInfo,QueryRequest>createFirst(info));
            }
            synchronized (searches) {
                for (QueryRequest search : searches) {
                    toPopulate.add(Union2.<RepositoryInfo,QueryRequest>createSecond(search));
                }
            }
            return true;
        }
        @Override protected Node createNodeForKey(Union2<RepositoryInfo,QueryRequest> key) {
            if (key.hasFirst()) {
                return new RepositoryNode(key.first());
            } else {
                return new FindResultsNode(key.second());
            }
        }
        @Override protected void addNotify() {
            RepositoryPreferences.getInstance().addChangeListener(this);
            FileUtil.addFileChangeListener(this, SettingsXmlConfigurationProcessor.DEFAULT_USER_SETTINGS_FILE);
            addChangeListener(this);
            addNotifyCalled = true;
        }
        @Override protected void removeNotify() {
            RepositoryPreferences.getInstance().removeChangeListener(this);
            removeChangeListener(this);
            if (addNotifyCalled) { //#213038
                try {
                    FileUtil.removeFileChangeListener(this, SettingsXmlConfigurationProcessor.DEFAULT_USER_SETTINGS_FILE);
                } catch (IllegalArgumentException exc) {
                    //we just ignore, who cares
                }
                addNotifyCalled = false;
            }
        }
        @Override public void stateChanged(ChangeEvent e) {
            refresh(false);
        }

        @Override public void fileFolderCreated(FileEvent fe) {
        }

        @Override public void fileDataCreated(FileEvent fe) {
            refresh(false);
        }

        @Override public void fileChanged(FileEvent fe) {
            refresh(false);
        }

        @Override public void fileDeleted(FileEvent fe) {
            refresh(false);
        }

        @Override public void fileRenamed(FileRenameEvent fe) {
            refresh(false);
        }

        @Override public void fileAttributeChanged(FileAttributeEvent fe) {
        }

    }
    
    static class QueryRequest {
        final List<QueryField> fields;
        final List<RepositoryInfo> infos;
        QueryRequest(List<QueryField> fields, List<RepositoryInfo> infos) {
            this.fields = fields;
            this.infos = infos;
        }
        
    }

}
