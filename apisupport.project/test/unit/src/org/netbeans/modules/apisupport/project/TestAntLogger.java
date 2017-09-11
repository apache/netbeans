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

package org.netbeans.modules.apisupport.project;

import java.io.File;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.openide.util.Lookup;

/**
 *  Implementation of simple AntLogger. To enable/disable use  setEnabled(boolean ) method.
 *
 * @author pzajac
 */
@org.openide.util.lookup.ServiceProvider(service=org.apache.tools.ant.module.spi.AntLogger.class)
public class TestAntLogger extends AntLogger {
    boolean bEnabled;


    public void setEnabled(boolean  bEnabled) {
        this.bEnabled = true;
    }
    public void messageLogged(AntEvent event) {
        if (bEnabled) {
            System.out.println(event.getMessage());
        }
    }

    public boolean interestedInSession(AntSession session) {
        return bEnabled;
    }

    public boolean interestedInScript(File script, AntSession session) {
        return bEnabled;
    }

    public boolean interestedInAllScripts(AntSession session) {
        return bEnabled;
    }
    
   public void targetStarted(AntEvent event) {
        System.out.println("target started:" + event.getTargetName());
    }

    public String[] interestedInTasks(AntSession session) {
        return (bEnabled) ? ALL_TASKS : new String[0];
    }

    public String[] interestedInTargets(AntSession session) {
        return (bEnabled) ? ALL_TARGETS : new String[0];
    }

    public int[] interestedInLogLevels(AntSession session) {
        return (bEnabled) ?
                 new int[]{AntEvent.LOG_INFO,AntEvent.LOG_WARN,AntEvent.LOG_ERR}:
                 new int[0];
    }

    public static TestAntLogger getDefault() {
        // XXX would be clearer to remove M-I/s reg and use MockLookup instead
        return (TestAntLogger) Lookup.getDefault().lookupItem(
                new Lookup.Template<AntLogger>(AntLogger.class,
                                   "org.netbeans.modules.apisupport.project.TestAntLogger",
                                   null)).getInstance();
    }

}
