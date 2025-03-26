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
/*
 * Contributor(s): Soot Phengsy
 */

package org.netbeans.swing.dirchooser;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import org.openide.modules.ModuleInstall;
import org.openide.windows.WindowManager;
import org.openide.windows.WindowSystemEvent;
import org.openide.windows.WindowSystemListener;

/**
 * Registers the directory chooser in NetBeans.
 *
 * @author Soot Phengsy
 */
public class Module extends ModuleInstall {
    
    private static final String KEY = "FileChooserUI"; // NOI18N
    private static Class<?> originalImpl;
    private static PropertyChangeListener pcl;
    
    private static final String QUICK_CHOOSER_NAME = 
            "org.netbeans.modules.quickfilechooser.ChooserComponentUI";
    
    private static final String FORCE_STANDARD_CHOOSER = "standard-file-chooser"; // NOI18N

    @Override public void restored() {
        WindowManager.getDefault().addWindowSystemListener(new WindowSystemListener() {

            @Override
            public void beforeLoad (WindowSystemEvent event) {
            }
            @Override
            public void afterLoad (WindowSystemEvent event) {
                WindowManager.getDefault().removeWindowSystemListener(this);
                EventQueue.invokeLater(Module::install);
            }
            @Override
            public void beforeSave (WindowSystemEvent event) {
            }
            @Override
            public void afterSave (WindowSystemEvent event) {
            }
        });
    }

    @Override public void uninstalled() {
        EventQueue.invokeLater(Module::uninstall);
    }
        
    private static void install() {
        // don't install directory chooser if standard chooser is desired
        if (isStandardChooserForced()) {
            return;
        }
        final UIDefaults uid = UIManager.getDefaults();
        originalImpl = (Class<?>) uid.getUIClass(KEY);
        Class<?> impl = DelegatingChooserUI.class;
        final String val = impl.getName();
        // don't install dirchooser if quickfilechooser is present
        if (!isQuickFileChooser(uid.get(KEY))) {
            uid.put(KEY, val);
            // To make it work in NetBeans too:
            uid.put(val, impl);
        }
        // #61147: prevent NB from switching to a different UI later (under GTK):
        uid.addPropertyChangeListener(pcl = (evt) -> {
            String name = evt.getPropertyName();
            Object className = uid.get(KEY);
            if ((name.equals(KEY) || name.equals("UIDefaults")) && !val.equals(className) && !isQuickFileChooser(className)) {
                originalImpl = (Class<?>) uid.getUIClass(KEY);
                uid.put(KEY, val);
            }
        });
    }
    
    private static void uninstall() {
        if (isInstalled()) {
            assert pcl != null;
            UIDefaults uid = UIManager.getDefaults();
            uid.removePropertyChangeListener(pcl);
            pcl = null;
            String val = originalImpl.getName();
            uid.put(KEY, val);
            uid.put(val, originalImpl);
            originalImpl = null;
        }
    }
    
    private static boolean isInstalled() {
        return originalImpl != null;
    }
    
    static Class<?> getOrigChooser () {
        return originalImpl;
    }
    
    private static boolean isQuickFileChooser (Object className) {
        return QUICK_CHOOSER_NAME.equals(className);
    }
    
    private static boolean isStandardChooserForced () {
        return Boolean.getBoolean(FORCE_STANDARD_CHOOSER);
    }
    
}
