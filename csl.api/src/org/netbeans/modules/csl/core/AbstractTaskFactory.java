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

package org.netbeans.modules.csl.core;

import java.util.Collection;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;

/**
 *
 * @author vita
 */
public abstract class AbstractTaskFactory extends TaskFactory {

    @Override
    public final Collection<? extends SchedulerTask> create(Snapshot snapshot) {
        String mimeType = snapshot.getMimeType();
        Language l = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
        if (l == null) {
            // not a CSL language
            return null;
        }

        if (!topLevelLanguageOnly || isTopLevel(snapshot)) {
            return createTasks(l, snapshot);
        } else {
            return null;
        }
    }

    /**
     * Creates a set of tasks for a given <code>Snapshot</code>. The <code>language</code>
     * passed in is a registered CSL language relevant for the <code>snapshot</code>'s
     * mimetype.
     *
     * @param language The language appropriate for the <code>snapshot</code>'s mimetype;
     *   never <code>null</code>.
     * @param snapshot The snapshot to create tasks for.
     * 
     * @return The set of tasks or <code>null</code>.
     */
    protected abstract Collection<? extends SchedulerTask> createTasks(Language language, Snapshot snapshot);

    /**
     * Creates new <code>AbstractTaskFactory</code>.
     *
     * @param topLevelLanguageOnly If <code>true<code>, the <code>createTasks</code>
     *   method will be called only for <code>Snapshot</code>s of the whole file,
     *   but not for embedded sections.
     */
    protected AbstractTaskFactory(boolean topLevelLanguageOnly) {
        this.topLevelLanguageOnly = topLevelLanguageOnly;
    }
    
    private final boolean topLevelLanguageOnly;

    private static boolean isTopLevel(Snapshot snapshot) {
        // XXX: this is not correct; we should change Source to chache snapshots
        // and simply check snapshot.getSource().getSnapshot() == snapshot
        return snapshot.getSource().getMimeType().equals(snapshot.getMimeType());
    }
}
