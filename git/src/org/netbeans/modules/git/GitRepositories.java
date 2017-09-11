/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.versioning.util.Utils;

/**
 *
 * @author ondra
 */
public class GitRepositories {
    private static GitRepositories instance;
    private final Set<File> repositories = new HashSet<>();
    private final Set<File> closed = new HashSet<>(5);
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    public static final String PROP_REPOSITORIES = "GitRepositories.repositories"; //NOI18N

    public static synchronized GitRepositories getInstance () {
        if (instance == null) {
            instance = new GitRepositories();
        }
        return instance;
    }

    public void add (File repository, boolean byUser) {
        boolean added;
        if (!byUser && closed.contains(repository)) {
            // closed by user, so he must open it manually
            return;
        }
        if (Utils.isAncestorOrEqual(new File(System.getProperty("java.io.tmpdir")), repository)) { //NOI18N
            // skip repositories in temp folder
            return;
        }
        Set<File> oldValues = null;
        Set<File> newValues = null;
        synchronized (repositories) {
            added = repositories.add(repository);
            if (added) {
                newValues = new HashSet<File>(repositories);
            }
        }
        if (added) {
            closed.remove(repository);
            oldValues = new HashSet<File>(newValues);
            oldValues.remove(repository);
            support.firePropertyChange(PROP_REPOSITORIES, oldValues, newValues);
        }
    }

    public void remove (File repository, boolean byUser) {
        boolean removed;
        Set<File> oldValues = null;
        Set<File> newValues = null;
        synchronized (repositories) {
            removed = repositories.remove(repository);
            if (removed) {
                newValues = new HashSet<File>(repositories);
                if (byUser) {
                    closed.add(repository);
                }
            }
        }
        if (removed) {
            oldValues = new HashSet<File>(newValues);
            oldValues.add(repository);
            support.firePropertyChange(PROP_REPOSITORIES, oldValues, newValues);
        }
    }

    public Set<File> getKnownRepositories () {
        synchronized (repositories) {
            return new HashSet<File>(repositories);
        }
    }

    public void addPropertyChangeListener (PropertyChangeListener list) {
        support.addPropertyChangeListener(list);
    }

    public void removePropertyChangeListener (PropertyChangeListener list) {
        support.removePropertyChangeListener(list);
    }

}
