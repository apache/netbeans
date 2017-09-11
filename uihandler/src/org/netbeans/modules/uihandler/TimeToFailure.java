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
package org.netbeans.modules.uihandler;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * It logs MTTF (Mean Time To failure) of exception
 * @author pzajac
 */
final class TimeToFailure {
    private static final String MTTF = "MTTF"; //NOI18N
    // ten minutes
    private static final long TRESHOLD = 60*1000*10;
    static Preferences prefs = NbPreferences.forModule(TimeToFailure.class);
    static long totalTime = 0;
    static long lastAction = 0;
    static void logAction() {
        if (totalTime == -1 ) {
            totalTime = prefs.getLong(MTTF, 0L);
            lastAction = System.currentTimeMillis();
        } else {
            long time = System.currentTimeMillis();
            if ( time - lastAction < TRESHOLD ) {
                totalTime += time - lastAction;
                if(Installer.preferencesWritable) {
                    prefs.putLong(MTTF,totalTime);
                }
            } 
            lastAction = time;
        }
    }

    static LogRecord logFailure() {
        LogRecord lr = new LogRecord(Level.CONFIG,TimeToFailure.class.getName() + ":" + totalTime);
        totalTime = 0;
        lastAction = 0;
        if(Installer.preferencesWritable) {
            prefs.putLong(MTTF,totalTime);
        }
        return lr;
    }
    
}
