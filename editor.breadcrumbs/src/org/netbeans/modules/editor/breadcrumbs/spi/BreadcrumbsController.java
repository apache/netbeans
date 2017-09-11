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
package org.netbeans.modules.editor.breadcrumbs.spi;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.SideBarFactory;
import org.netbeans.modules.editor.breadcrumbs.BreadCrumbsNodeImpl;
import org.netbeans.modules.editor.breadcrumbs.HolderImpl;
import org.netbeans.modules.editor.breadcrumbs.SideBarFactoryImpl;
import org.netbeans.modules.editor.breadcrumbs.support.BreadCrumbsScheduler;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex.Action;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 *
 * @author lahvac
 */
public class BreadcrumbsController {

    private BreadcrumbsController() {
    }

    private static final RequestProcessor WORKER = new RequestProcessor(BreadcrumbsController.class.getName(), 1, false, false);
    
    /**
     * 
     * @param doc
     * @param selected 
     * @since 1.8
     */
    public static void setBreadcrumbs(@NonNull final Document doc, @NonNull final BreadcrumbsElement selected) {
        WORKER.post(new Runnable() {
            @Override public void run() {
                List<BreadcrumbsElement> path = new ArrayList<>();
                
                BreadcrumbsElement el = selected;
                
                while (el != null) {
                    path.add(el);
                    el = el.getParent();
                }
                
                Node root = new BreadCrumbsNodeImpl(path.remove(path.size() - 1));
                Node last = root;
                
                Collections.reverse(path);
               
                for (BreadcrumbsElement current : path) {
                    for (Node n : last.getChildren().getNodes(true)) {
                        if (n.getLookup().lookup(BreadcrumbsElement.class) == current) {
                            last = n;
                            break;
                        }
                    }
                }
                
                setBreadcrumbs(doc, root, last);
            }
        });
    }
    
    @Deprecated
    public static void setBreadcrumbs(@NonNull Document doc, @NonNull final Node root, @NonNull final Node selected) {
        Parameters.notNull("doc", doc);
        Parameters.notNull("root", root);
        Parameters.notNull("selected", selected);
        
        final ExplorerManager manager = HolderImpl.get(doc).getManager();

        Children.MUTEX.writeAccess(new Action<Void>() {
            @Override public Void run() {
                manager.setRootContext(root);
                manager.setExploredContext(selected);
                return null;
            }
        });
    }
    
    public static boolean areBreadCrumsEnabled(@NonNull Document doc) {
        return MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class).getBoolean(SideBarFactoryImpl.KEY_BREADCRUMBS, SideBarFactoryImpl.DEF_BREADCRUMBS);
    }
    
    public static void addBreadCrumbsEnabledListener(@NonNull final ChangeListener l) {
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        
        prefs.addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override public void preferenceChange(PreferenceChangeEvent evt) {
                if (evt == null || SideBarFactoryImpl.KEY_BREADCRUMBS.equals(evt.getKey())) {
                    l.stateChanged(new ChangeEvent(evt));
                }
            }
        });
    }
    
    public static SideBarFactory createSideBarFactory() {
        return new SideBarFactoryImpl();
    }
    
    public static final Class<? extends Scheduler> BREADCRUMBS_SCHEDULER = BreadCrumbsScheduler.class;
    
    /**
     * @since 1.3
     */
    public static final Image NO_ICON = ImageUtilities.loadImage("org/netbeans/modules/editor/breadcrumbs/resources/no-icon.png");
}
