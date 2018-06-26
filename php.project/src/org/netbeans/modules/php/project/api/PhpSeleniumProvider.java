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

package org.netbeans.modules.php.project.api;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;

/**
 * This class can be found in project's lookup and used by Selenium module.
 * @author Tomas Mysik
 * @since 2.9
 */
public interface PhpSeleniumProvider {

    /**
     * Get the root directory of Selenium test files. Return <code>null</code>
     * if such directory is not set.
     * @param showCustomizer <code>true</code> if a dialog which allows to select such directory should be shown
     *                       (suitable for cases when such folder is not set up yet)
     * @return the root directory of Selenium test files or <code>null</code> if such directory is not set
     */
    FileObject getTestDirectory(boolean showCustomizer);

    /**
     * Run all the Selenium tests (similar to Test action on project's node).
     * <p>
     * Does nothing in case that {@link #getSeleniumTestDirectory(boolean)} returns <code>null</code>.
     */
    void runAllTests();
    
    /**
     * Check whether this implementation supports given FileObjects.
     *
     * @param activatedFOs FileObjects to check
     * @return {@code true} if this instance supports given FileObjects, {@code false} otherwise
     */
    boolean isSupportEnabled(FileObject[] activatedFOs);
    
    /**
     * Finds <code>SourceGroup</code>s where a test for the given class
     * can be created (so that it can be found by the projects infrastructure
     * when a test for the class is to be opened or run).
     *
     * @param createdSourceRoots
     * @param  fileObject  <code>FileObject</code> to find target
     *                     <code>SourceGroup</code>(s) for
     * @return  a list of objects - each of them can be either
     *          a <code>SourceGroup</code> for a possible target folder
     *          or simply a <code>FileObject</code> representing a possible
     *          target folder (if <code>SourceGroup</code>) for the folder
     *          was not found);
     *          the returned list may be empty but not <code>null</code>
     */
    List<Object> getTestSourceRoots(Collection<SourceGroup> createdSourceRoots, FileObject refFileObject);
}
