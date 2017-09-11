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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.gsf.testrunner.api;

import java.util.Set;
import javax.swing.event.ChangeListener;

/**
 * Handles rerunning a test execution.
 *
 * @author Erno Mononen
 */
public interface RerunHandler {

    /**
     * Reruns the test execution.
     */
    void rerun();

    /**
     * Reruns the provided tests.
     * @param type the type of rerun to be executed
     */
    void rerun(Set<Testcase> tests);

    /**
     * @return true if re-running is enabled (i.e. it is possible to
     * rerun the execution and it has finished).
     * @param type the type of rerun to verify
     */
    boolean enabled(RerunType type);

    /**
     * Adds a listener for getting notified about the enabled state.
     * @param listener the listener to add.
     */
    void addChangeListener(ChangeListener listener);

    /**
     * Removes the given listener.
     * 
     * @param listener the listener to remove.
     */
    void removeChangeListener(ChangeListener listener);

}
