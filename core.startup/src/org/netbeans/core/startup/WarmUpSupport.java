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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.core.startup;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.lookup.Lookups;

/**
 * This class controls "warm-up" initialization after IDE startup (some time
 * after main window is shown). It scans WarmUp folder for individual tasks
 * to be performed. The tasks should be instance objects implementing Runnable.
 *
 * The tasks may be provided by modules via xml layer.
 *
 * @author Tomas Pavek
 */

final class WarmUpSupport implements Runnable {
    private static final RequestProcessor.Task TASK;
    static {
        RequestProcessor RP = new RequestProcessor("Warm Up");
        TASK = RP.create(new WarmUpSupport(), true);
    } // NOI18N

    private static final Logger err = Logger.getLogger("org.netbeans.core.WarmUpSupport");

    static Task warmUp(long delay) {
        TASK.schedule((int)delay);
        return TASK;
    }
    
    static Task waitTask() {
        return TASK;
    }

    // -------

    @Override
    public void run() {
        err.fine("Warmup starting..."); // NOI18N
        StartLog.logStart("Warmup"); // NOI18N
        try {

        Collection<? extends Lookup.Item<Runnable>> warmObjects =
            Lookups.forPath("WarmUp").lookupResult(Runnable.class).allItems(); // NOI18N
        err.log(Level.FINE, "Found {0} warm up task(s)", warmObjects.size()); // NOI18N

        for (Lookup.Item<Runnable> warmer : warmObjects) {
            try {
                Runnable r = warmer.getInstance();
                r.run();
                StartLog.logProgress("Warmup task executed " + warmer.getId()); // NOI18N
            } catch (Exception ex) {
                Logger.getLogger(WarmUpSupport.class.getName()).log(Level.WARNING, null, ex);
            }
        }
        err.fine("Warmup done."); // NOI18N
        } finally {
        StartLog.logEnd("Warmup"); // NOI18N
        StartLog.impl.flush();
        }
    }
}
