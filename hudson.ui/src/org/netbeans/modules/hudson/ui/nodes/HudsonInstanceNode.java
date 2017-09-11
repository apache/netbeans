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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.hudson.ui.nodes;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonChangeListener;
import org.netbeans.modules.hudson.api.HudsonFolder;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJob.Color;
import org.netbeans.modules.hudson.api.HudsonManager;
import org.netbeans.modules.hudson.api.HudsonVersion;
import org.netbeans.modules.hudson.api.HudsonView;
import org.netbeans.modules.hudson.api.Utilities;
import static org.netbeans.modules.hudson.ui.nodes.Bundle.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Union2;
import org.openide.util.lookup.Lookups;

/**
 * Describes HudsonInstance in the Runtime Tab
 *
 * @author Michal Mocnak
 */
public class HudsonInstanceNode extends AbstractNode {
    
    private static final String ICON_BASE = "org/netbeans/modules/hudson/ui/resources/instance.png"; // NOI18N
    
    private final HudsonInstance instance;
    private Sheet.Set set;
    
    private boolean warn = false;
    private boolean run = false;
    private boolean alive = false;
    private boolean forbidden;
    private boolean version = false;
    
    public HudsonInstanceNode(final HudsonInstance instance) {
        super(new InstanceNodeChildren(instance), Lookups.singleton(instance));
        
        setName(instance.getUrl());
        setDisplayName(instance.getName());
        setShortDescription(instance.getUrl());
        setIconBaseWithExtension(ICON_BASE);
        setValue("customDelete", true); // NOI18N
        
        this.instance = instance;
        
        instance.addHudsonChangeListener(new HudsonChangeListener() {
            @Override public void stateChanged() {
                refreshState();
            }
            @Override public void contentChanged() {
                refreshContent();
            }
        });
        instance.prefs().addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override public void preferenceChange(PreferenceChangeEvent evt) {
                refreshContent();
            }
        });
        
        // Refresh
        refreshState();
        refreshContent();
    }
    
    
    @Messages({
        "# {0} - supported Hudson version number", "MSG_WrongVersion=[Older version than {0}]",
        "MSG_Disconnected=[Disconnected]",
        "MSG_forbidden=[Unauthorized]"
    })
    @Override public String getHtmlDisplayName() {
        String selectedView = instance.prefs().get(SELECTED_VIEW, null);
        return (run ? "<b>" : "") + (warn ? "<font color=\"#A40000\">" : "") + // NOI18N
                instance.getName() + (warn ? "</font>" : "") + (run ? "</b>" : "") + // NOI18N
                (selectedView != null ? " <font color='!controlShadow'>[" + selectedView + "]</font>" : "") + // NOI18N
                (alive ? (version ? "" : " <font color=\"#A40000\">" + // NOI18N
                    MSG_WrongVersion(HudsonVersion.SUPPORTED_VERSION) + "</font>") :
                    " <font color=\"#A40000\">" + // NOI18N
                (forbidden ? MSG_forbidden() : MSG_Disconnected()) + "</font>") +
                getProjectInfoString();
    }
    
    @Messages({
        "HudsonInstanceNode.from_open_project=(from open project)"
    })
    private String getProjectInfoString() {
        boolean pers = instance.isPersisted();
        String info = instance.getPersistence().getInfo(
                HudsonInstanceNode_from_open_project());
        return (!pers ? " <font color='!controlShadow'>" + // NOI18N
                info + "</font>" : "");                                 //NOI18N
    }

    public @Override Action[] getActions(boolean context) {
        List<? extends Action> actions = org.openide.util.Utilities.actionsForPath(HudsonInstance.ACTION_PATH);
        return actions.toArray(new Action[actions.size()]);
    }

    public @Override boolean canDestroy() {
        return instance.isPersisted();
    }

    public @Override void destroy() throws IOException {
        HudsonManager.removeInstance(instance);
    }

    public @Override PropertySet[] getPropertySets() {
        return new PropertySet[] {getSheetSet()};
    }
    
    @Messages({
        "TXT_Instance_Prop_Name=Name",
        "DESC_Instance_Prop_Name=Hudson's instance name",
        "TXT_Instance_Prop_Url=URL",
        "DESC_Instance_Prop_Url=Hudson's instance URL",
        "TXT_Instance_Prop_Sync=Autosynchronization time",
        "DESC_Instance_Prop_Sync=Autosynchronization time in minutes (if it's 0 the autosynchronization is off)"
    })
    private Sheet.Set getSheetSet() {
        if (null == set) {
            set = Sheet.createPropertiesSet();

            // Set display name
            set.setDisplayName(instance.getName());

            // Put properties in
            set.put(new Node.Property<?>[]{
                new PropertySupport<String>("name", String.class, //NOI18N
                TXT_Instance_Prop_Name(),
                DESC_Instance_Prop_Name(),
                true, false) {

                    @Override
                    public String getValue() {
                        return instance.getName();
                    }

                    @Override
                    public void setValue(String val) {
                    }
                },
                new PropertySupport<String>("url", String.class, //NOI18N
                TXT_Instance_Prop_Url(),
                DESC_Instance_Prop_Url(),
                true, false) {

                    @Override
                    public String getValue() throws IllegalAccessException, InvocationTargetException {
                        return instance.getUrl();
                    }

                    @Override
                    public void setValue(String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                    }
                },
                new PropertySupport<Integer>("sync", Integer.class, //NOI18N
                TXT_Instance_Prop_Sync(),
                DESC_Instance_Prop_Sync(),
                true, true) {
                    @Override
                    public Integer getValue() {
                        return instance.getSyncInterval();
                    }

                    @Override
                    public void setValue(Integer val) {
                        if (val == null || val < 0) {
                            throw new IllegalArgumentException();
                        }
                        instance.setSyncInterval(val);
                    }

                    public @Override
                    boolean canWrite() {
                        return instance.isPersisted();
                    }
                }
            });
        }

        return set;
    }

    private synchronized void refreshState() {
        alive = instance.isConnected();
        forbidden = instance.isForbidden();
        version = Utilities.isSupportedVersion(instance.getVersion());
        
        // Fire changes if any
        fireDisplayNameChange(null, getHtmlDisplayName());
    }
    
    private synchronized void refreshContent() {
        // Clear flags
        warn = false;
        run = false;
        
        // Refresh state flags
        for (HudsonJob job : instance.getJobs()) {
            if (job.getColor().equals(Color.red) || job.getColor().equals(Color.red_anime)) {
                warn = true;
            }
            if (job.getColor().isRunning()) {
                run = true;
            }
            if (warn && run) {
                break; // it's not necessary to continue
            }
        }
        // Fire changes if any
        fireDisplayNameChange(null, getHtmlDisplayName());
    }

    /**
     * Preferences key for currently display view.
     */
    public static final String SELECTED_VIEW = "view"; // NOI18N
    
    private static class InstanceNodeChildren extends Children.Keys<Union2<HudsonJob,HudsonFolder>> implements HudsonChangeListener {
        
        private final HudsonInstance instance;
        
        InstanceNodeChildren(HudsonInstance instance) {
            this.instance = instance;
            instance.addHudsonChangeListener(this);
            instance.prefs().addPreferenceChangeListener(new PreferenceChangeListener() {
                @Override public void preferenceChange(PreferenceChangeEvent evt) {
                    refreshKeys();
                }
            });
        }
        
        @Override protected Node[] createNodes(Union2<HudsonJob,HudsonFolder> item) {
            return new Node[] {item.hasFirst() ? new HudsonJobNode(item.first()) : new HudsonFolderNode(item.second())};
        }
        
        @Override
        protected void addNotify() {
            super.addNotify();
            if (!instance.isConnected()/* && seems undesirable: !instance.isForbidden()*/) {
                setKeys(Collections.<Union2<HudsonJob,HudsonFolder>>emptySet());
                instance.synchronize(true);
            } else {
                refreshKeys();
            }
        }
        
        @Override
        protected void removeNotify() {
            setKeys(Collections.<Union2<HudsonJob,HudsonFolder>>emptySet());
            super.removeNotify();
        }
        
        private void refreshKeys() {
            List<HudsonJob> jobs = new ArrayList<HudsonJob>();
            HudsonView view = instance.getPrimaryView();
            String selectedView = instance.prefs().get(SELECTED_VIEW, null);
            if (selectedView != null) {
                for (HudsonView v : instance.getViews()) {
                    if (v.getName().equals(selectedView)) {
                        view = v;
                        break;
                    }
                }
            }
            for (HudsonJob job : instance.getJobs()) {
                if (!job.getViews().contains(view)) {
                    continue;
                }
                jobs.add(job);
            }
            Collections.sort(jobs);
            List<Union2<HudsonJob,HudsonFolder>> items = new LinkedList<Union2<HudsonJob,HudsonFolder>>();
            for (HudsonFolder folder : instance.getFolders()) {
                // XXX ideally should restrict by selected view, like jobs
                items.add(Union2.<HudsonJob,HudsonFolder>createSecond(folder));
            }
            for (HudsonJob job : jobs) {
                items.add(Union2.<HudsonJob,HudsonFolder>createFirst(job));
            }
            setKeys(items);
        }
        
        @Override public void stateChanged() {
            refreshKeys();
        }
        
        @Override public void contentChanged() {
            refreshKeys();
        }
    }

}
