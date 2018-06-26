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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.core;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.netbeans.api.project.Project;

/**
 * This is base implementation of ProjectWebServiceViewImpl.
 * Implements the change support.
 * @author Ajit Bhate
 */
public abstract class AbstractProjectWebServiceViewImpl implements ProjectWebServiceViewImpl {

    private ChangeSupport serviceListeners,  clientListeners;
    private Reference<Project> project;
    protected AbstractProjectWebServiceViewImpl(Project project) {
        this.project = new WeakReference<Project>(project);
        serviceListeners = new ChangeSupport(this);
        clientListeners = new ChangeSupport(this);
    }

    public void addChangeListener(ChangeListener l, ProjectWebServiceView.ViewType viewType) {
        switch (viewType) {
            case SERVICE:
                serviceListeners.addChangeListener(l);
                break;
            case CLIENT:
                clientListeners.addChangeListener(l);
                break;
        }
    }

    public void removeChangeListener(ChangeListener l, ProjectWebServiceView.ViewType viewType) {
        switch (viewType) {
            case SERVICE:
                if (serviceListeners != null) {
                    serviceListeners.removeChangeListener(l);
                }
                break;
            case CLIENT:
                if (clientListeners != null) {
                    clientListeners.removeChangeListener(l);
                }
                break;
        }
    }

    protected void fireChange(ProjectWebServiceView.ViewType viewType) {
        switch (viewType) {
            case SERVICE:
                serviceListeners.fireChange();
                return;
            case CLIENT:
                clientListeners.fireChange();
                return;
        }
    }
    
    @Override
    public boolean equals(Object object) {
        if ( object == null ){
            return false;
        }
        if(getClass() == object.getClass()) {
            AbstractProjectWebServiceViewImpl other = (AbstractProjectWebServiceViewImpl) object;
            return other.getProject() == this.getProject();
        }
        return false;
    }

    @Override
    public int hashCode() {
        super.hashCode();
        int hash = 3;
        hash = 23 * hash + (this.getProject() != null ? this.getProject().hashCode() : 0);
        return hash;
    }

    protected Project getProject() {
        return project.get();
    }

}
