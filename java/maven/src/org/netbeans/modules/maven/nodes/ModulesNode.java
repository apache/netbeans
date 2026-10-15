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

package org.netbeans.modules.maven.nodes;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.actions.OpenPOMAction;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.POMModel;
import static org.netbeans.modules.maven.nodes.Bundle.*;
import org.netbeans.modules.maven.spi.nodes.NodeUtils;
import org.netbeans.modules.project.ui.api.ProjectActionUtils;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * display the modules for pom packaged project
 * @author Milos Kleint 
 */
public class ModulesNode extends AbstractNode {

    private static final @StaticResource String MODULES_BADGE = "org/netbeans/modules/maven/modules-badge.png";
    private final NbMavenProjectImpl proj;

    @Messages("LBL_Modules=Modules")
    public ModulesNode(NbMavenProjectImpl proj) {
        super(Children.create(new ModulesChildFactory(proj), true));
        this.proj = proj;
        setName("Modules"); //NOI18N
        setDisplayName(LBL_Modules());
    }

    @Override
    public Action[] getActions(boolean bool) {
        return new Action[] {
            new AddModuleAction(),
            new CreateModuleAction()
        };
    }

    private Image getIcon(boolean opened) {
        Image badge = ImageUtilities.loadImage(MODULES_BADGE, true); //NOI18N
        return ImageUtilities.mergeImages(NodeUtils.getTreeFolderIcon(opened), badge, 8, 8);
    }

    @Override
    public Image getIcon(int type) {
        return getIcon(false);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(true);
    }

    private static class Wrapper {
        boolean isAggregator;
        LogicalViewProvider provider;
        NbMavenProjectImpl proj;
    }
    
   
    
    private static class ModulesChildFactory extends ChildFactory<Wrapper>{
        private final NbMavenProjectImpl project;
        private final PropertyChangeListener listener;
        
         ModulesChildFactory(NbMavenProjectImpl proj) {
            project = proj;
            NbMavenProject watcher = project.getProjectWatcher();
            listener = new PropertyChangeListener() {
                                       @Override
                                       public void propertyChange(PropertyChangeEvent evt) {
                                           if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                                               refresh(false);
                                           }
                                       }
                                   };
             
            watcher.addPropertyChangeListener(WeakListeners.propertyChange(listener, watcher));                       
            
 
        }
         
        @Override
        protected boolean createKeys(final List<Wrapper> modules) {                                    
            for (String module : project.getOriginalMavenProject().getModules()) {
                File base = project.getOriginalMavenProject().getBasedir();
                File projDir = FileUtil.normalizeFile(new File(base, module));
                FileObject fo = FileUtil.toFileObject(projDir);
                if (fo != null) {
                    try {
                        Project prj = ProjectManager.getDefault().findProject(fo);
                        if(prj == null) {
                            // issue #242542
                            // the projects pom might be already cached by ProjectManager as NO_SUCH_PROJECT, 
                            // we have to get rid of that cached value.
                            // Would prefer a better place to call .clearNonProjectCache after a project was created,
                            // unfortunatelly .createKeys is invoked by a chain of events triggered by the poms save document
                            // - not sure how to hook before that, so that we can ensure that it isn't cached anymore.
                            // - on the other hand lets not clear the ProjectManager cache on each Node refresh.
                            ProjectManager.getDefault().clearNonProjectCache();
                            prj = ProjectManager.getDefault().findProject(fo);
                        }
                        if (prj != null && prj.getLookup().lookup(NbMavenProjectImpl.class) != null) {
                            Wrapper wr = new Wrapper();
                            wr.proj = (NbMavenProjectImpl) prj;
                            MavenProject mp = wr.proj.getOriginalMavenProject();
                            wr.isAggregator = NbMavenProject.TYPE_POM.equals(mp.getPackaging()) && !mp.getModules().isEmpty();
                            wr.provider = prj.getLookup().lookup(LogicalViewProvider.class);
                            assert wr.provider != null;
                            modules.add(wr);
                        }
                    } catch (IllegalArgumentException ex) {
                        ex.printStackTrace();//TODO log ?
                    } catch (IOException ex) {
                        ex.printStackTrace();//TODO log ?
                    }
                } else {
                    //TODO broken module reference.. show as such..
                }
            }
            return true;
        }

        @Override
        protected Node createNodeForKey(Wrapper wr) {
             return new ProjectFilterNode(project, wr.proj, wr.provider.createLogicalView(), wr.isAggregator);
        }
        
        
    }
  
    private static class ProjectFilterNode extends FilterNode {

        private final NbMavenProjectImpl project;
        private final NbMavenProjectImpl parent;

        ProjectFilterNode(NbMavenProjectImpl parent, NbMavenProjectImpl proj, Node original, boolean isAggregator) {
            super(original, isAggregator ? Children.create(new ModulesChildFactory(proj), true) : Children.LEAF);
//            disableDelegation(DELEGATE_GET_ACTIONS);
            project = proj;
            this.parent = parent;
        }

        @Override
        public Action[] getActions(boolean b) {
            ArrayList<Action> lst = new ArrayList<Action>();
            lst.add(OpenProjectAction.SINGLETON);
            lst.add(OpenPOMAction.instance());
            lst.add(new RemoveModuleAction(parent, project));
//            lst.addAll(Arrays.asList(super.getActions(b)));
            return lst.toArray(new Action[0]);
        }

        @Override
        public Action getPreferredAction() {
            return OpenProjectAction.SINGLETON;
        }
    }

    private static class RemoveModuleAction extends AbstractAction {

        private final NbMavenProjectImpl project;
        private final NbMavenProjectImpl parent;

        @Messages("BTN_Remove_Module=Remove Module")
        RemoveModuleAction(NbMavenProjectImpl parent, NbMavenProjectImpl proj) {
            putValue(Action.NAME, BTN_Remove_Module());
            project = proj;
            this.parent = parent;
        }

        @Messages("MSG_Remove_Module=Do you want to remove the module from the parent POM?")
        @Override public void actionPerformed(ActionEvent e) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(MSG_Remove_Module(), NotifyDescriptor.YES_NO_OPTION);
            Object ret = DialogDisplayer.getDefault().notify(nd);
            if (ret == NotifyDescriptor.YES_OPTION) {
                FileObject fo = FileUtil.toFileObject(parent.getPOMFile());
                ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
                    @Override
                    public void performOperation(POMModel model) {
                        List<String> modules = model.getProject().getModules();
                        if (modules != null) {
                            for (String path : modules) {
                                File rel = new File(parent.getPOMFile().getParent(), path);
                                File norm = FileUtil.normalizeFile(rel);
                                FileObject folder = FileUtil.toFileObject(norm);
                                if (folder != null && folder.equals(project.getProjectDirectory())) {
                                    model.getProject().removeModule(path);
                                    break;
                                }
                            }
                        }
                    }
                };
                org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(fo, Collections.singletonList(operation));
                //TODO is the manual reload necessary if pom.xml file is being saved?
                NbMavenProject.fireMavenProjectReload(project);
            }
        }
    }

    private static class OpenProjectAction extends AbstractAction implements ContextAwareAction {

        static final OpenProjectAction SINGLETON = new OpenProjectAction();

        private OpenProjectAction() {}

        public @Override void actionPerformed(ActionEvent e) {
            assert false;
        }

        @Messages("BTN_Open_Project=Open Project")
        public @Override Action createContextAwareInstance(final Lookup context) {
            return new AbstractAction(BTN_Open_Project()) {
                public @Override void actionPerformed(ActionEvent e) {
                    Collection<? extends NbMavenProjectImpl> projects = context.lookupAll(NbMavenProjectImpl.class);
                    final NbMavenProjectImpl[] projectsArray = projects.toArray(new NbMavenProjectImpl[0]);
                    if(projectsArray.length > 0) {
                        RequestProcessor.getDefault().post(new Runnable() {
                            public @Override void run() {
                                OpenProjects.getDefault().open(projectsArray, false, true);
                                RequestProcessor.getDefault().post(new Runnable() {
                                    public @Override void run() {
                                        ProjectActionUtils.selectAndExpandProject(projectsArray[0]);
                                    }
                                }, 500);    
                            }
                        });
                    }
                }
            };
        }
    }

    private class AddModuleAction extends AbstractAction {

        @Messages("BTN_add_module=Add Existing Module...")
        AddModuleAction() {
            super(BTN_add_module());
        }

        @Override public void actionPerformed(ActionEvent e) {
            JFileChooser c = ProjectChooser.projectChooser();
            File basedir = FileUtil.toFile(proj.getProjectDirectory());
            c.setCurrentDirectory(basedir);
            if (c.showOpenDialog(Utilities.findDialogParent()) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            final List<String> mods = new ArrayList<String>();
            for (File d : c.getSelectedFiles()) {
                String mod = FileUtilities.relativizeFile(basedir, d);
                if (mod != null && !mod.equals(".")) {
                    mods.add(mod);
                }
            }
            if (mods.isEmpty()) {
                return;
            }
            org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(proj.getProjectDirectory().getFileObject("pom.xml"), Collections.singletonList(new ModelOperation<POMModel>() {
                @Override public void performOperation(POMModel model) {
                    for (String mod : mods) {
                        model.getProject().addModule(mod);
                    }
                }
            }));
        }

    }
    
    private class CreateModuleAction extends AbstractAction {

        @Messages("BTN_create_module=Create New Module...")
        CreateModuleAction() {
            super(BTN_create_module());
        }

        @Override public void actionPerformed(ActionEvent e) {
            Action act = CommonProjectActions.newProjectAction();
            act.putValue("PRESELECT_CATEGORY" /*ProjectTemplates.PRESELECT_CATEGORY */, "Maven2");
            act.putValue(CommonProjectActions.PROJECT_PARENT_FOLDER, proj.getPOMFile().getParentFile());
            act.putValue("initialValueProperties", new String[] {"groupId", "version"});
            act.putValue("groupId", proj.getOriginalMavenProject().getGroupId());
            act.putValue("version", proj.getOriginalMavenProject().getVersion());
            act.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "actionPerformed"));
        }

    }

}
