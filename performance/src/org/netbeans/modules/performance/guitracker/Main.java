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
package org.netbeans.modules.performance.guitracker;

/**
 * An entry point to start an application that initializes event logging and
 * passes logic to original main class that is specified as first parameter
 *
 * @author Radim Kubacki
 */
public class Main {

    public Main() {
    }

    public static void main(String[] args) {
        String clzName = System.getProperty("guitracker.mainclass");
        if (clzName == null) {
            throw new IllegalStateException("No main class defined. Use -Dguitracker.mainclass=<classname>");
        }
        // init tracker and EQ now
        ActionTracker tr;

        LoggingRepaintManager rm;

        LoggingEventQueue leq;

        // load our EQ and repaint manager
        tr = ActionTracker.getInstance();
        rm = new LoggingRepaintManager(tr);
        rm.setEnabled(true);
        leq = new LoggingEventQueue(tr);
        leq.setEnabled(true);
        tr.setInteractive(true);
        tr.connectToAWT(true);
        tr.startNewEventList("ad hoc");
        tr.startRecording();

        try {
            Class<?> clz = Class.forName(clzName);
            clz.getMethod("main", String[].class).invoke(null, (Object) args);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot pass control to " + clzName, ex);
        }
    }
}
