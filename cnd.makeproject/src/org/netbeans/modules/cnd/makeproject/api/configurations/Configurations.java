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
package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.spi.toolchain.CompilerSetManagerEvents;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.util.RequestProcessor;

public final class Configurations {

    private final PropertyChangeSupport pcs;
    private final List<Configuration> configurations = new ArrayList<>();
    private final ReadWriteLock configurationsLock = new ReentrantReadWriteLock();
    private final List<NamedRunnable> tasks = new ArrayList<>();
    private static final RequestProcessor RP = new RequestProcessor("Configurations events", 1); //NOI18N


    public Configurations() {
        pcs = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    public Configurations init(Configuration[] confs, int defaultConf) {
        List<NamedRunnable> toRun = new ArrayList<>();
        Configuration def = null;
        configurationsLock.writeLock().lock();
        try {
            configurations.clear();
            if (confs != null) {
                int current = 0;
                for (int i = 0; i < confs.length; i++) {
                    if (confs[i] != null) {
                        configurations.add(confs[i]);
                        if (current == defaultConf) {
                            confs[i].setDefault(true);
                            def = confs[i];
                        } else {
                            confs[i].setDefault(false);
                        }
                        current++;
                    } else {
                        new Exception("Configuration[" + i + "]==null").printStackTrace(); // NOI18N
                    }
                }
                if (def != null) {
                    toRun.addAll(tasks);
                    tasks.clear();
                }
            }
        } finally {
            configurationsLock.writeLock().unlock();
        }
//        if (def != null) {
//            pcs.firePropertyChange(PROP_ACTIVE_CONFIGURATION, null, def);
//            pcs.firePropertyChange(PROP_DEFAULT, null, null);
//        }
        toRun.forEach((task) -> {
            runOnProjectReadiness(task, false);
        });
        return this;
    }

    public void runOnProjectReadiness(NamedRunnable task) {
        runOnProjectReadiness(task, true);
    }

    private void runOnProjectReadiness(NamedRunnable task, boolean postpone) {
        MakeConfiguration active = null;
        configurationsLock.writeLock().lock();
        try {
            active = (MakeConfiguration) getActive();
            if (active == null) {
                if (postpone) {
                    tasks.add(task);
                }
            }
        } finally {
            configurationsLock.writeLock().unlock();
        }
        if (active != null) {
            DevelopmentHostConfiguration host = active.getDevelopmentHost();
            CompilerSetManagerEvents.get(host.getExecutionEnvironment()).runProjectReadiness(task);
        }
    }

    public int size() {
        configurationsLock.readLock().lock();
        try {
            return configurations.size();
        } finally {
            configurationsLock.readLock().unlock();
        }
    }

    /*
     * Get all configurations
     */
    public Configuration[] toArray() {
        configurationsLock.readLock().lock();
        try {
            return configurations.toArray(new Configuration[size()]);
        } finally {
            configurationsLock.readLock().unlock();
        }
    }

    public Collection<Configuration> getConfigurations() {
        Collection<Configuration> collection = new LinkedHashSet<>();
        configurationsLock.readLock().lock();
        try {
            collection.addAll(configurations);
            return collection;
        } finally {
            configurationsLock.readLock().unlock();
        }
    }

    public Configuration[] getClonedConfs() {
        configurationsLock.readLock().lock();
        try {
            Configuration[] cs = new Configuration[size()];
            for (int i = 0; i < size(); i++) {
                Configuration c = configurations.get(i);
                cs[i] = c.cloneConf();
            }
            return cs;
        } finally {
            configurationsLock.readLock().unlock();
        }
    }

    public String[] getConfsAsDisplayNames() {
        configurationsLock.readLock().lock();
        try {
            String[] names = new String[size()];
            for (int i = 0; i < size(); i++) {
                Configuration configuration = configurations.get(i);
                names[i] = configuration.toString();
            }
            return names;
        } finally {
            configurationsLock.readLock().unlock();
        }
    }

    public String[] getConfsAsNames() {
        configurationsLock.readLock().lock();
        try {
            String[] names = new String[size()];
            for (int i = 0; i < size(); i++) {
                Configuration configuration = configurations.get(i);
                names[i] = configuration.getName();
            }
            return names;
        } finally {
            configurationsLock.readLock().unlock();
        }
    }

    /*
     * Get a specific configuration
     */
    public Configuration getConf(int index) {
        configurationsLock.readLock().lock();
        try {
            if (checkValidIndex(index)) {
                return configurations.get(index);
            }
        } finally {
            configurationsLock.readLock().unlock();
        }
        return null;
    }

    public Configuration getConfByDisplayName(String displayName) {
        Configuration ret = null;
        configurationsLock.readLock().lock();
        try {
            for (Configuration c : configurations) {
                if (c.getDisplayName().equals(displayName)) {
                    ret = c;
                    break;
                }
            }
        } finally {
            configurationsLock.readLock().unlock();
        }
        return ret;
    }

    public Configuration getConf(String name) {
        Configuration ret = null;
        configurationsLock.readLock().lock();
        try {
            for (Configuration c : configurations) {
                if (c.getName().equals(name)) {
                    ret = c;
                    break;
                }
            }
        } finally {
            configurationsLock.readLock().unlock();
        }
        return ret;
    }

    /*
     * Set default configuration
     */
    public void setActive(Configuration def) {
        if (def == null) {
            return;
        }
        Configuration old;
        boolean fire = false;
        configurationsLock.readLock().lock();
        try {
            old = getActive();
            if (def == old) {
                return; // Nothing has changed
            }

            for (Configuration c : configurations) {
                c.setDefault(false);
                if (c == def) {
                    def.setDefault(true);
                    fire = true;
                }
            }
        } finally {
            configurationsLock.readLock().unlock();
        }
        if (fire) {
            fireChangedActiveConfiguration(old, def);
        }
    }

    /*
     * Set default configuration
     */
    public void setActive(String name) {
        setActive(getConf(name));
    }

    public void setActive(int index) {
        if (index < 0) {
            return;
        }
        Configuration old;
        Configuration def;
        configurationsLock.readLock().lock();
        try {
            old = getActive();
            if (!checkValidIndex(index)) {
                return;
            }
            def = configurations.get(index);
            if (def != null) {
                configurations.forEach((c) -> {
                    c.setDefault(false);
                });
                def.setDefault(true);
            }
        } finally {
            configurationsLock.readLock().unlock();
        }

        fireChangedActiveConfiguration(old, def);
    }

    public void fireChangedActiveConfiguration(final Configuration oldActive, final Configuration newActive) {
        RP.post(() -> {
            pcs.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE, oldActive, newActive);
        });
    }

    public void fireChangedConfigurations(final Configuration[] oldConf, final Configuration[] newConf) {
        RP.post(() -> {
            pcs.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATIONS, oldConf, newConf);
        });
    }

    /*
     * Get default configuration
     */
    public String getActiveDisplayName() {
        String defDisplayName = null;
        Configuration def = getActive();
        if (def != null) {
            defDisplayName = def.getDisplayName();
        }
        return defDisplayName;
    }

    /**
     * @deprecated Use getActive()
     */
    @Deprecated
    public Configuration getDefault() {
        return getActive();
    }

    public Configuration getActive() {
        configurationsLock.readLock().lock();
        try {
            for (Configuration c : configurations) {
                if (c.isDefault()) {
                    return c;
                }
            }
        } finally {
            configurationsLock.readLock().unlock();
        }
        return null;
    }

    public int getActiveAsIndex() {
        configurationsLock.readLock().lock();
        try {
            int index = -1;
            for (Configuration c : configurations) {
                index++;
                if (c.isDefault()) {
                    return index;
                }
            }
        } finally {
            configurationsLock.readLock().unlock();
        }
        return -1;
    }

    /*
     * Check valid index
     */
    private boolean checkValidIndex(int index) {
        if (index < 0 || index >= size()) {
            new ArrayIndexOutOfBoundsException(index).printStackTrace(); // NOI18N
            // Error ???
            // FIXUP ???
            return false;
        }
        return true;
    }

    public Configurations cloneConfs() {
        Configurations clone = new Configurations();
        configurationsLock.readLock().lock();
        try {
            clone.init(getClonedConfs(), getActiveAsIndex());
        } finally {
            configurationsLock.readLock().unlock();
        }
        return clone;
    }
}
