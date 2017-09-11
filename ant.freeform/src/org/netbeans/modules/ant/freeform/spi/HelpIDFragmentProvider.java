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

package org.netbeans.modules.ant.freeform.spi;

/**
 * This interface is used to compute the help context for a freeform project.
 * Each {@link ProjectNature} should register an implementation in its lookup.
 * See {@link #getHelpIDFragment} to find out what are the requirements on the help
 * id fragments.
 *
 * If it is necessary to compute a help context for a freeform project, all
 * {@link HelpIDFragmentProvider}s registered in the project's lookup are asked to 
 * provide the fragments. The fragments are then lexicographically sorted and
 * concatenated (separated by dots) into one string, used as a base for the help id.
 *
 * @author Jan Lahoda
 * @since 1.11.1
 */
public interface HelpIDFragmentProvider {
    
    /**
     * Returns a help id fragment defined by the implementor. The method should return
     * the same string each time it is called (more preciselly, it is required that
     * <code>getHelpIDFragment().equals(getHelpIDFragment())</code>, but is allowed to
     * <code>getHelpIDFragment() != getHelpIDFragment()</code>). The string should be unique
     * among all the freeform project natures. The string is required to match this
     * regular expression: <code>([A-Za-z0-9])+</code>.
     *
     * Please note that the returned fragment is part of the contract between the
     * code and documentation, so be carefull when you need to change it.
     *
     * @return a non-null help id fragment, fullfilling the above conditions.
     */
    public String getHelpIDFragment();
    
}
