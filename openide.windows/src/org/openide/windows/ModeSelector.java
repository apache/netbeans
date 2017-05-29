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

package org.openide.windows;

/**
 * Selects mode which a TopComponent should initially dock into.
 * If a TopComponent being opened is not docked into any Mode, the system selects 
 * the last-used editor-kind Mode, or the default editor mode if no editor was used.
 * Plugin implementors can hint the Window System to use more appropriate
 * mode than the default to open the TopComppnent. 
 * <p/>
 * If none of the registered {@code ModeSelector}s return a valid Mode, the TopComponent
 * will open in the mode selected by the default algorithm. Implementation of WindowManager 
 * may ignore the hint, for example if it conflicts with persisted settings or user choices.
 * <p/>
 * Implementations of {@code ModeSelector} must be registered in the default Lookup.
 * @since 6.77
 */
public interface ModeSelector {
    /**
     * Choose a suitable Mode to open the TopComponent in. The implementation 
     * should return an existing Mode which the TopComponent will dock into. The
     * automatically selected Mode will be passed in {@code preselectedMode}.
     * The implementation can accept the default or ignore the request and return
     * {@code null}.
     * 
     * @param tc the {@link TopComponent} to be opened.
     * @param preselectedMode the default mode for opening
     * @return a more suitable Mode, or {@code null} to use the preselected one.
     */
    public Mode selectModeForOpen(TopComponent tc, Mode preselectedMode);
}
