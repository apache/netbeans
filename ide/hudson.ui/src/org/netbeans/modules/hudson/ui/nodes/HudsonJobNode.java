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

package org.netbeans.modules.hudson.ui.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJob.Color;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.ui.actions.LogInAction;
import org.netbeans.modules.hudson.ui.actions.OpenUrlAction;
import org.netbeans.modules.hudson.ui.actions.StartJobAction;
import org.netbeans.modules.hudson.ui.actions.ViewConfigAction;
import org.netbeans.modules.hudson.api.ui.OpenableInBrowser;
import org.netbeans.modules.hudson.constants.HudsonInstanceConstants;
import org.netbeans.modules.hudson.constants.HudsonJobConstants;
import org.netbeans.modules.hudson.ui.actions.ProjectAssociationAction;
import static org.netbeans.modules.hudson.ui.nodes.Bundle.*;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle.Messages;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;

/**
 * Describes HudsonJob in the RuntimeTab
 *
 * @author Michal Mocnak
 */
public class HudsonJobNode extends AbstractNode {
    
    private String htmlDisplayName;
    private HudsonJob job;
    private PropertyChangeListener watchedListener;
    private Sheet.Set set;
    
    public HudsonJobNode(HudsonJob job) {
        super(makeChildren(job), Lookups.singleton(job));
        setName(job.getName());
        setHudsonJob(job);
        setWatchedListener();
    }

    private static Children makeChildren(final HudsonJob job) {
        if (job.getColor() == Color.secured) {
            return Children.LEAF;
        }
        return Children.create(new ChildFactory<Object>() {
            final Object WORKSPACE = new Object();
            protected @Override boolean createKeys(List<Object> toPopulate) {
                if (job.getRemoteWorkspace() != null) {
                    toPopulate.add(WORKSPACE);
                }
                for (HudsonJobBuild b : job.getBuilds()) {
                    // Processing one build at a time, make sure its result is known (blocking call).
                    b.getResult();
                    toPopulate.add(b);
                }
                return true;
            }
            protected @Override Node createNodeForKey(Object key) {
                if (key == WORKSPACE) {
                    return new HudsonWorkspaceNode(job);
                } else {
                    return new HudsonJobBuildNode((HudsonJobBuild) key);
                }
            }
        }, true);
    }

    @Override
    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        if (job.getColor() == Color.secured) {
            return new Action[] {new LogInAction(job.getInstance())};
        }
        List<Action> actions = new ArrayList<Action>();
        actions.add(SystemAction.get(StartJobAction.class));
        actions.add(new ProjectAssociationAction(job));
        actions.add(null);
        if (job instanceof OpenableInBrowser) {
            actions.add(OpenUrlAction.forOpenable((OpenableInBrowser) job));
        }
        actions.add(new ViewConfigAction(job));
        actions.add(SystemAction.get(PropertiesAction.class));
        return actions.toArray(new Action[0]);
    }
    
    @Override
    protected Sheet createSheet() {
        // Create a property sheet
        Sheet s = super.createSheet();
        
        // Put properties in
        s.put(getSheetSet()); // XXX is cast necessary?
        
        return s;
    }
    
    @Messages({
        "TXT_Job_Prop_Name=Name",
        "DESC_Job_Prop_Name=Jenkins's job name",
        "TXT_Job_Prop_Url=URL",
        "DESC_Job_Prop_Url=Jenkins's job URL",
        "HudsonJobImpl.watched=Watched",
        "HudsonJobImpl.watched_desc=Whether you wish to be notified of failures in this job."
    })
    private Sheet.Set getSheetSet() {
        if (null == set) {
            set = Sheet.createPropertiesSet();

            // Set display name
            set.setDisplayName(getDisplayName());

            // Put properties in
            set.put(new Node.Property<?>[]{
                new PropertySupport<String>(HudsonJobConstants.JOB_NAME, String.class,
                TXT_Job_Prop_Name(),
                DESC_Job_Prop_Name(), true, false) {

                    @Override
                    public String getValue() {
                        return job.getName();
                    }

                    @Override
                    public void setValue(String val) {
                    }
                },
                new PropertySupport<String>(HudsonJobConstants.JOB_URL, String.class,
                TXT_Job_Prop_Url(),
                DESC_Job_Prop_Url(), true, false) {

                    @Override
                    public String getValue() throws IllegalAccessException, InvocationTargetException {
                        return job.getUrl();
                    }

                    @Override
                    public void setValue(String val) {
                    }

                },
                new PropertySupport.ReadWrite<Boolean>("salient", Boolean.TYPE, // NOI18N
                HudsonJobImpl_watched(),
                HudsonJobImpl_watched_desc()) {
                    @Override
                    public Boolean getValue() {
                        return job.isSalient();
                    }

                    @Override
                    public void setValue(Boolean val) {
                        if (!getValue().equals(val)) {
                            job.setSalient(val);
                        }
                    }
                }
            });
        }
        return set;
    }

    private void setWatchedListener() {

        watchedListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String pn = evt.getPropertyName();
                if (pn.equals(HudsonInstanceConstants.INSTANCE_SUPPRESSED_JOBS)
                        || pn.equals(HudsonInstanceConstants.INSTANCE_PREF_JOBS)) {
                    setHudsonJob(HudsonJobNode.this.job);
                }
            }
        };
        job.getInstance().addPropertyChangeListener(
                WeakListeners.propertyChange(watchedListener,
                job.getInstance()));
    }

    @Messages({
        "HudsonJobNode.running=(running)",
        "HudsonJobNode.in_queue=(in queue)",
        "HudsonJobNode.secured=(secured)",
        "HudsonJobNode.not_watched=(not watched)"
    })
    private void setHudsonJob(HudsonJob job) {
        this.job = job;
        Color color = job.getColor();
        setShortDescription(job.getUrl());

        // XXX #159836: tooltips
        setIconBaseWithExtension(color.iconBase());

        String oldHtmlDisplayName = getHtmlDisplayName();
        try {
            String escapedName = XMLUtil.toElementContent(job.getDisplayName());
            htmlDisplayName = job.isSalient()
                    ? color.colorizeDisplayName(escapedName)
                    : toGray(escapedName);
        } catch (CharConversionException ex) {
            assert false : ex;
            return;
        }
        if (!job.isSalient()) {
            htmlDisplayName += toGrayWithSpace(HudsonJobNode_not_watched());
        }
        if (color.isRunning()) {
            htmlDisplayName += toGrayWithSpace(HudsonJobNode_running());
        }
        if (color == Color.secured) {
            htmlDisplayName += toGrayWithSpace(HudsonJobNode_secured());
        }
        if (job.isInQueue()) {
            htmlDisplayName += toGrayWithSpace(HudsonJobNode_in_queue());
        }
        fireDisplayNameChange(oldHtmlDisplayName, htmlDisplayName);
    }

    private String toGray(String text) {
        return "<font color='!controlShadow'>" + text + "</font>";      //NOI18N
    }

    private String toGrayWithSpace(String text) {
        return " " + toGray(text);                                      //NOI18N
    }
}
