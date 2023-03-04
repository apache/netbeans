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
