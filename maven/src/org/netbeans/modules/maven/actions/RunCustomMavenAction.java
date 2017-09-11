/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.actions;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import static org.netbeans.modules.maven.actions.Bundle.BTN_run_custom;
import org.netbeans.spi.project.ActionProvider;
import org.openide.awt.Actions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

//@ActionID(category=RunCustomMavenAction.CATEGORY, id=RunCustomMavenAction.ID)
//@ActionRegistration(displayName="#BTN_run_custom", iconBase = "org/netbeans/modules/maven/resources/Maven2Icon.gif")
@Messages({
    "# {0} - maven build name",
    "BTN_run_custom=Run custom Maven build:{0}"
})
public class RunCustomMavenAction extends AbstractAction implements ContextAwareAction {

    static final String CATEGORY = "Project";
    static final String ID = "org.netbeans.modules.maven.actions.runcustom";
    public static final String MAVEN_ATTR = "maven-goal";
//    public static ContextAwareAction instance() {
//        return (ContextAwareAction) Actions.forID(CATEGORY, ID);
//    }
    private NbMavenProjectImpl prj;
    private final String action;
    private FileObject[] fos;
    

    public RunCustomMavenAction(String action) {
        setEnabled(false);
        this.action = action;
    }
    
    public RunCustomMavenAction(String action, NbMavenProjectImpl prj, FileObject[] fos) {
        this.action = action;
        this.prj = prj;
        this.fos = fos;
        setEnabled(prj.getLookup().lookup(ActionProvider.class).isActionEnabled(action, Lookups.fixed((Object) fos)));
        
    }

    @Override public void actionPerformed(ActionEvent e) {
        if (prj != null) {
            prj.getLookup().lookup(ActionProvider.class).invokeAction(action, Lookups.fixed((Object) fos));
        } else {
            //in editor press shortcut and you end up here.. why?
            //guess prj based on global looukup?
            Action act = createContextAwareInstance(Utilities.actionsGlobalContext());
            if (act != this) {
                act.actionPerformed(e);
            }
        }
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        NbMavenProjectImpl owner = null;
        List<FileObject> fs = new ArrayList<FileObject>();
        for (FileObject fo : actionContext.lookupAll(FileObject.class)) {
            Project p = FileOwnerQuery.getOwner(fo);
            if (p != null) {
                NbMavenProjectImpl own = p.getLookup().lookup(NbMavenProjectImpl.class);
                if (own != null) {
                    if (owner != null) {
                        if (!owner.equals(own)) {
                            return this;
                        }
                    } else {
                        owner = own;
                    }
                    fs.add(fo);
                } else if (owner != null) {
                    return this;
                }
            } else if (owner != null) {
                return this;
            }
        }
        if (owner != null) {
            return new RunCustomMavenAction(action, owner, fs.toArray(new FileObject[0]));
        } else {
            return this;
        }
    }
    
    
    public static Action create(FileObject fo) {
        String mapp = (String) fo.getAttribute(MAVEN_ATTR);
        assert mapp != null;
        String imagePath = (String)fo.getAttribute("imagePath");
        assert imagePath != null;
        RunCustomMavenAction act = new RunCustomMavenAction(mapp);
        String name = mapp;
        if (name.startsWith("CUSTOM-")) {
            name = name.substring("CUSTOM-".length());
        }
        
        return Actions.context(FileObject.class, true, true, act, ID, BTN_run_custom(name), imagePath, true);
    }
    
    public static void createActionDeclaration(String action, String displayName, String iconPath) throws IOException {
        String fsAction = action.replace("/", ""); //#236336 slash is evil
        FileObject root = FileUtil.getConfigRoot();
        FileObject actions = root.getFileObject("Actions");
        if (actions == null) {
            actions = root.createFolder("Actions");
        }
        assert actions != null;
        FileObject mavenCategory = actions.getFileObject("Maven");
        if (mavenCategory == null) {
            mavenCategory = actions.createFolder("Maven");
        }
        assert mavenCategory != null;
        //is that enough?
        String name = fsAction + ".instance"; //action.replaceAll("[^a-z0-9_]+", "_");
        FileObject instance = mavenCategory.getFileObject(name);
        if (instance != null) {
            return; //what to do if we already have the action?
        }
        FileObject template = root.getFileObject("Maven/actionTemplate.instance");
        assert template != null;
        instance = template.copy(mavenCategory, fsAction, "instance");
        instance.setAttribute(MAVEN_ATTR, action);
        assert iconPath != null;
        instance.setAttribute("imagePath", iconPath);
        FileObject tb = root.getFileObject("Toolbars/Build");
        if (tb != null) {
            FileObject shadow = tb.createData("maven_" + fsAction + ".shadow");
            shadow.setAttribute("originalFile", "Actions/Maven/" + name);
            shadow.setAttribute("position", 3002);
        }
    }
    
    public static void deleteDeclaration(String action) throws IOException {
        String fsAction = action.replace("/", ""); //#236336 slash is evil
        FileObject root = FileUtil.getConfigRoot();
        FileObject fo = root.getFileObject("Toolbars/Build/maven_" + fsAction + ".shadow");
        if (fo != null) {
            fo.delete();
        }
        fo = root.getFileObject("Actions/Maven/" + fsAction + ".instance");
        if (fo != null) {
            fo.delete();
        }
    }
    
    public static boolean actionDeclarationExists(String action) {
        String fsAction = action.replace("/", ""); //#236336 slash is evil
        FileObject root = FileUtil.getConfigRoot();
        FileObject actions = root.getFileObject("Actions");
        if (actions != null) {
            FileObject mavenCategory = actions.getFileObject("Maven");
            if (mavenCategory != null) {
                String name = fsAction + ".instance"; //action.replaceAll("[^a-z0-9_]+", "_");
                FileObject instance = mavenCategory.getFileObject(name);
                if (instance != null && action.equals(instance.getAttribute(MAVEN_ATTR))) {
                    return true;
                }

            }
        }
        return false;
    }
    
    public static String actionDeclarationIconPath(String action) {
        String fsAction = action.replace("/", ""); //#236336 slash is evil
        FileObject root = FileUtil.getConfigRoot();
        FileObject actions = root.getFileObject("Actions");
        if (actions != null) {
            FileObject mavenCategory = actions.getFileObject("Maven");
            if (mavenCategory != null) {
                String name = fsAction + ".instance"; //action.replaceAll("[^a-z0-9_]+", "_");
                FileObject instance = mavenCategory.getFileObject(name);
                if (instance != null && action.equals(instance.getAttribute(MAVEN_ATTR))) {
                    String path = (String) instance.getAttribute("imagePath");
                    return path;
                }

            }
        }
        return null;
    }    
    
    
    public static Icon actionDeclarationIcon(String action) {
        String s = actionDeclarationIconPath(action);
        if (s != null) {
            return ImageUtilities.loadImageIcon(s, false);
        }
        return null;
    }    
    
    public static List<String> createAllActionIcons() {
        List<String> allImages = new ArrayList<String>(Arrays.asList(new String[] { 
            "org/netbeans/modules/maven/resources/Maven2IconRun1.png", 
            "org/netbeans/modules/maven/resources/Maven2IconRun2.png", 
            "org/netbeans/modules/maven/resources/Maven2IconRun3.png", 
            "org/netbeans/modules/maven/resources/Maven2IconRun4.png", 
            "org/netbeans/modules/maven/resources/Maven2IconRun5.png", 
            "org/netbeans/modules/maven/resources/Maven2IconRun6.png", 
        }));
        return allImages;
    }
}