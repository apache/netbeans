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

package org.netbeans.modules.debugger.ui.actions;

import org.netbeans.api.project.Project;
import org.openide.util.NbBundle;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


/**
 * A utility class for submitting UI Gestures Collector records
 * @author Martin Entlicher
 */
class GestureSubmitter {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final Logger USG_LOGGER = Logger.getLogger("org.netbeans.ui.metrics.debugger"); // NOI18N

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    static void logDebugProject(Project project) {
        List params = new ArrayList();
        if (project != null) {
            params.add(0, project.getClass().getName());
        }
        log("USG_PROJECT_DEBUG", params); // NOI18N
    }

    static void logAttach(String attachTypeName) {
        List params = new ArrayList();
        params.add(attachTypeName);
        log("USG_DEBUG_ATTACH", params); // NOI18N
    }

    private static void log(String type, List<Object> params) {
        LogRecord record = new LogRecord(Level.INFO, type);
        record.setResourceBundle(NbBundle.getBundle(GestureSubmitter.class));
        record.setResourceBundleName(GestureSubmitter.class.getPackage().getName() + ".Bundle"); // NOI18N
        record.setLoggerName(USG_LOGGER.getName());

        record.setParameters(params.toArray(new Object[params.size()]));

        USG_LOGGER.log(record);
    }
}
