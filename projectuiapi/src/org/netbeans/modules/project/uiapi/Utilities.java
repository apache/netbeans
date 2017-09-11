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

package org.netbeans.modules.project.uiapi;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.support.BuildExecutionSupport.Item;
import org.netbeans.spi.project.ui.support.FileActionPerformer;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * Way of getting implementations of UI components defined in projects/projectui.
 * @author Petr Hrebejk, Jesse Glick
 */
public class Utilities {

    private static final Map<ProjectCustomizer.Category,CategoryChangeSupport> CATEGORIES = new HashMap<ProjectCustomizer.Category,CategoryChangeSupport>();

    private Utilities() {}

    /** Gets action factory from the global Lookup.
     */
    public static ActionsFactory getActionsFactory() {
        ActionsFactory instance = Lookup.getDefault().lookup(ActionsFactory.class);
        return instance != null ? instance : new ActionsFactory() {
            class Dummy extends AbstractAction implements ContextAwareAction {
                Dummy(String label) {
                    super(label);
                }
                @Override public boolean isEnabled() {
                    return false;
                }
                @Override public void actionPerformed(ActionEvent e) {
                    assert false : getValue(NAME) + " is just a placeholder";
                }
                @Override public Action createContextAwareInstance(Lookup actionContext) {
                    return this;
                }
            }
            @Override public Action setAsMainProjectAction() {
                return new Dummy("setAsMainProject");
            }
            @Override public Action customizeProjectAction() {
                return new Dummy("customizeProject");
            }
            @Override public Action openSubprojectsAction() {
                return new Dummy("openSubprojects");
            }
            @Override public Action closeProjectAction() {
                return new Dummy("closeProject");
            }
            @Override public Action newFileAction() {
                return new Dummy("newFile");
            }
            @Override public Action deleteProjectAction() {
                return new Dummy("deleteProject");
            }
            @Override public Action copyProjectAction() {
                return new Dummy("copyProject");
            }
            @Override public Action moveProjectAction() {
                return new Dummy("moveProject");
            }
            @Override public Action newProjectAction() {
                return new Dummy("newProject");
            }
            @Override public Action renameProjectAction() {
                return new Dummy("renameProject");
            }
            @Override public Action setProjectConfigurationAction() {
                return new Dummy("setProjectConfiguration");
            }
            // XXX may perhaps be useful to provide basic impls of the following, so that e.g.
            // o.n.m.ant.freeform.ActionsTest.testLogicalViewActions can pass w/o test dep on projectui
            @Override public ContextAwareAction projectCommandAction(String command, String namePattern, Icon icon) {
                return new Dummy("projectCommand:" + command);
            }
            @Override public Action projectSensitiveAction(ProjectActionPerformer performer, String name, Icon icon) {
                return new Dummy("projectSensitive");
            }
            @Override public Action mainProjectCommandAction(String command, String name, Icon icon) {
                return new Dummy("mainProjectCommand:" + command);
            }
            @Override public Action mainProjectSensitiveAction(ProjectActionPerformer performer, String name, Icon icon) {
                return new Dummy("mainProjectSensitive");
            }
            @Override public Action fileCommandAction(String command, String name, Icon icon) {
                return new Dummy("fileCommand:" + command);
            }
            @Override public Action fileSensitiveAction(FileActionPerformer performer, String name, Icon icon) {
                return new Dummy("fileCommand");
            }
        };
    }

    /** Gets BuildSupportImpl from the global Lookup.
     */
    public static BuildExecutionSupportImplementation getBuildExecutionSupportImplementation() {
        BuildExecutionSupportImplementation instance = Lookup.getDefault().lookup(BuildExecutionSupportImplementation.class);
        return instance != null ? instance : new BuildExecutionSupportImplementation() {
            public void registerFinishedItem(Item item) {}
            public void registerRunningItem(Item item) {}
            public void addChangeListener(ChangeListener listener) {}
            public void removeChangeListener(ChangeListener listener) {}
            public Item getLastItem() {return null;}
            public List<Item> getRunningItems() {return Collections.<Item>emptyList();}
        };
    }
    
    /** Gets the projectChooser factory from the global Lookup
     */
    public static ProjectChooserFactory getProjectChooserFactory() {
        ProjectChooserFactory instance = Lookup.getDefault().lookup(ProjectChooserFactory.class);
        return instance != null ? instance : new ProjectChooserFactory() {
            File projectsFolder;
            @Override public File getProjectsFolder() {
                return projectsFolder != null ? projectsFolder : FileUtil.normalizeFile(new File(System.getProperty("java.io.tmpdir", "")));
            }
            @Override public void setProjectsFolder(File file) {
                projectsFolder = file;
            }
            @Override public JFileChooser createProjectChooser() {
                JFileChooser jfc = new JFileChooser();
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                return jfc;
            }
            @Override public WizardDescriptor.Panel<WizardDescriptor> createSimpleTargetChooser(Project project, SourceGroup[] folders, WizardDescriptor.Panel<WizardDescriptor> bottomPanel, boolean freeFileExtension) {
                return new WizardDescriptor.Panel<WizardDescriptor>() {
                    @Override public Component getComponent() {
                        return new JPanel();
                    }
                    @Override public HelpCtx getHelp() {
                        return null;
                    }
                    @Override public void readSettings(WizardDescriptor settings) {}
                    @Override public void storeSettings(WizardDescriptor settings) {}
                    @Override public boolean isValid() {
                        return false;
                    }
                    @Override public void addChangeListener(ChangeListener l) {}
                    @Override public void removeChangeListener(ChangeListener l) {}
                };
            }
        };
    }
    
    public static CategoryChangeSupport getCategoryChangeSupport(ProjectCustomizer.Category category) {
        CategoryChangeSupport cw = Utilities.CATEGORIES.get(category);
        return cw == null ? CategoryChangeSupport.NULL_INSTANCE : cw;
    }
    
    public static void putCategoryChangeSupport(
            ProjectCustomizer.Category category, CategoryChangeSupport wrapper) {
        Utilities.CATEGORIES.put(category, wrapper);
    }
    
    public static void removeCategoryChangeSupport(ProjectCustomizer.Category category) {
        Utilities.CATEGORIES.remove(category);
    }
    
}
