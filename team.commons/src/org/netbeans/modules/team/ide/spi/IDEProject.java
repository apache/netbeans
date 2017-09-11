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
package org.netbeans.modules.team.ide.spi;

import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.Icon;

/**
 * Representation of a standalone (top-level) project the user can open in the
 * IDE directly. It is used for visualization of projects the user opened or can
 * open from the versioned sources obtained from a team server project. <p>
 * Specific subclass should override addDeleteListener and implement the
 * actual listening on project deletion.
 *
 * @author Tomas Pavek
 */
public abstract class IDEProject {
    private final String displayName;
    private final Icon icon;
    private final URL url;

    private final List<DeleteListener> deleteListeners = new CopyOnWriteArrayList();

    protected IDEProject(String displayName, Icon icon, URL url) {
        this.displayName = displayName;
        this.icon = icon;
        this.url = url;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Icon getIcon() {
        return icon;
    }

    public URL getURL() {
        return url;
    }

    /**
     * To be overridden in subclass to start observing the actual project deletion.
     * It should call notifyDeleted when the project is deleted.
     * @param l
     * @return true if listener was added (not existed already)
     */
    public synchronized boolean addDeleteListener(DeleteListener l) {
        if (deleteListeners.contains(l)) {
            return false;
        } else {
            deleteListeners.add(l);
            return true;
        }
    }

    public boolean removeDeleteListener(DeleteListener l) {
        return deleteListeners.remove(l);
    }

    protected final List<DeleteListener> getDeleteListeners() {
        return deleteListeners;
    }

    public final void notifyDeleted() {
        for (DeleteListener dl : deleteListeners) {
            dl.projectDeleted(this);
        }
    }

    public interface OpenListener {
        void projectsOpened(IDEProject[] prj);
    }

    public interface DeleteListener {
        void projectDeleted(IDEProject prj);
    }

    // -----

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IDEProject other = (IDEProject) obj;
        if (this.url != other.url && (this.url == null || !this.url.equals(other.url))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.url != null ? this.url.hashCode() : 0);
        return hash;
    }
}
