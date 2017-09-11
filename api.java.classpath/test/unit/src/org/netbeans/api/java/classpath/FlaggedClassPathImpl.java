/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.api.java.classpath;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.java.classpath.FlaggedClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
class FlaggedClassPathImpl implements FlaggedClassPathImplementation {

    private final PropertyChangeSupport support;
    //@GuardedBy("this")
    private Set<ClassPath.Flag> flags;
    //@GuardedBy("this")
    private List<PathResourceImplementation> resources;

    FlaggedClassPathImpl() {
        this.support = new PropertyChangeSupport(this);
        this.flags = EnumSet.noneOf(ClassPath.Flag.class);
        this.resources = Collections.emptyList();
    }

    @Override
    public synchronized Set<ClassPath.Flag> getFlags() {
        return flags;
    }

    @Override
    public synchronized List<? extends PathResourceImplementation> getResources() {
        return resources;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        this.support.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        this.support.removePropertyChangeListener(listener);
    }

    void setResources(@NonNull final List<PathResourceImplementation> resources) {
        Parameters.notNull("resources", resources); //NOI18N
        synchronized (this) {
            this.resources = resources;
        }
        support.firePropertyChange(PROP_RESOURCES, null, null);
    }

    void setFlags(@NonNull final Set<ClassPath.Flag> flags) {
        Parameters.notNull("flags", flags); //NOI18N
        synchronized (this) {
            this.flags = flags;
        }
        support.firePropertyChange(PROP_FLAGS, null, null);
    }
}
