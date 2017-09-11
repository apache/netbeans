/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.ui.node;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.docker.api.DockerAction;
import org.netbeans.modules.docker.api.DockerInstance;
import org.netbeans.modules.docker.api.DockerSupport;
import org.openide.util.ChangeSupport;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public class StatefulDockerInstance implements Refreshable {

    private static final RequestProcessor RP = new RequestProcessor(StatefulDockerInstance.class);

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    // FIXME default value
    private final AtomicBoolean available = new AtomicBoolean(true);

    private final DockerInstance.ConnectionListener listener = new DockerInstance.ConnectionListener() {
        @Override
        public void onConnect() {
            update(true);
        }

        @Override
        public void onDisconnect() {
            update(false);
        }
    };

    private final DockerInstance instance;

    public StatefulDockerInstance(DockerInstance instance) {
        this.instance = instance;
        instance.addConnectionListener(listener);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public DockerInstance getInstance() {
        return instance;
    }

    public boolean isAvailable() {
        return available.get();
    }

    @Override
    public void refresh() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                update(new DockerAction(instance).ping());
            }
        });
    }

    public void remove() {
        instance.removeConnectionListener(listener);
        DockerSupport.getDefault().removeInstance(instance);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.instance);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StatefulDockerInstance other = (StatefulDockerInstance) obj;
        if (!Objects.equals(this.instance, other.instance)) {
            return false;
        }
        return true;
    }

    private void update(boolean newValue) {
        boolean oldValue = available.getAndSet(newValue);
        if (oldValue != newValue) {
            changeSupport.fireChange();
        }
    }
}
