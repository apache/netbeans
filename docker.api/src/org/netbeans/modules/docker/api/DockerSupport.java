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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.docker.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.BaseUtilities;
import org.openide.util.ChangeSupport;
import org.openide.util.NbPreferences;
import org.openide.util.Parameters;

/**
 *
 * @author Petr Hejl
 */
public final class DockerSupport {

    private static final Logger LOGGER = Logger.getLogger(DockerSupport.class.getName());

    private static DockerSupport support;

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    // GuardedBy("this")
    private final Map<String, DockerInstance> instances = new HashMap<>();

    // GuardedBy("this")
    private boolean initialized;

    private DockerSupport() {
        super();
    }

    public static DockerSupport getDefault() {
        DockerSupport ret;
        synchronized (DockerSupport.class) {
            if (support == null) {
                support = new DockerSupport();
                Preferences p = NbPreferences.forModule(DockerInstance.class).node(DockerInstance.INSTANCES_KEY);
                p.addNodeChangeListener(new NodeChangeListener() {
                    @Override
                    public void childAdded(NodeChangeEvent evt) {
                        support.refresh();
                    }

                    @Override
                    public void childRemoved(NodeChangeEvent evt) {
                        support.refresh();
                    }
                });
            }
            ret = support;
        }
        synchronized (ret) {
            if (!ret.isInitialized()) {
                ret.refresh();
            }
        }
        return ret;
    }

    public DockerInstance addInstance(@NonNull DockerInstance instance) {
        Parameters.notNull("instance", instance);

        String url = instance.getUrl();
        synchronized (this) {
            if (instances.containsKey(url)) {
                throw new IllegalStateException("Docker instance already exist: " + url);
            }
            instance.save();
            instances.put(url, instance);
        }
        changeSupport.fireChange();
        return instance;
    }

    public void removeInstance(@NonNull DockerInstance instance) {
        Parameters.notNull("instance", instance);

        synchronized (this) {
            instances.remove(instance.getUrl());
            instance.delete();

            // we shouldn't need it and use it
            //instance.getEventBus().close();
        }
        changeSupport.fireChange();
    }

    public Collection<? extends DockerInstance> getInstances() {
        synchronized (this) {
            return new HashSet<>(instances.values());
        }
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public boolean isSocketSupported() {
        return false;
//        if (BaseUtilities.getOperatingSystem() != BaseUtilities.OS_LINUX) {
//            return false;
//        }
//        String arch = System.getProperty("os.arch"); // NOI18N
//        return arch != null && (arch.contains("x86") || arch.contains("amd64")); // NOI18N
    }

    private boolean isInitialized() {
        synchronized (this) {
            return initialized;
        }
    }

    private void refresh() {
        boolean fire = false;
        synchronized (this) {
            initialized = true;
            Set<String> toRemove = new HashSet<>(instances.keySet());
            for (DockerInstance i : DockerInstance.loadAll()) {
                if (instances.get(i.getUrl()) == null) {
                    fire = true;
                    instances.put(i.getUrl(), i);
                }
                toRemove.remove(i.getUrl());
            }
            if (instances.keySet().removeAll(toRemove)) {
                fire = true;
            }
        }
        if (fire) {
            changeSupport.fireChange();
        }
    }
}
