/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.java.api.common.queries;

import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Tomas Zezula
 */
final class EvaluatorPropertyProvider implements PropertyProvider {
    private final PropertyEvaluator base;
    private final ChangeSupport listeners;

    EvaluatorPropertyProvider(@NonNull final PropertyEvaluator base) {
        this.base = base;
        this.listeners = new ChangeSupport(this);
    }

    @Override
    public Map<String, String> getProperties() {
        return base.getProperties();
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        this.listeners.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        this.listeners.removeChangeListener(l);
    }

    void update() {
        final Runnable act = () -> listeners.fireChange();
        if (ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess()) {
            act.run();
        } else {
            ProjectManager.mutex().readAccess(act);
        }
    }
}
