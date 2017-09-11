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
package org.netbeans.modules.gsf.codecoverage.api;

import java.util.List;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.openide.filesystems.FileObject;

/**
 * A code coverage provider provides code coverage information for source files.
 * Currently only intended for C0 usage.
 *
 * @todo Handle C1 and C2 types later
 *
 * @author Tor Norbye
 */
public interface CoverageProvider {
    /**
     * Return true iff this provider support custom hit counts. (If false, it only
     * supports whether a line is covered or not, e.g. hit count={zero, nonzero}.
     */
    boolean supportsHitCounts();

    /**
     * Return true iff this provider supports aggregation.
     */
    boolean supportsAggregation();

    /**
     * Is coverage enabled for the given project?
     */
    boolean isEnabled();

    /**
     * Is coverage data being aggregated from run to run?
     */
    boolean isAggregating();

    void setAggregating(boolean aggregating);

    /**
     * Mime types supported by this provider
     */
    public Set<String> getMimeTypes();

    /**
     * This method is called to enable or disable code coverage for the project
     */
    public void setEnabled(boolean enabled);

    /**
     * The user has requested that the coverage data should be cleared out.
     */
    void clear();
    
    /**
     * Get the specific coverage information per line.
     */
    @CheckForNull FileCoverageDetails getDetails(FileObject fo, Document doc);

    /**
     * Get a summary of all the coverage data in the given project, one per measured file.
     */
    @CheckForNull List<FileCoverageSummary> getResults();
    
    /**
     * Gets the name of action that the Test All button in the code coverage bar
     * should invoke. May return <code>null</code>, in which case  if the default 
     * test action will be used.
     * 
     * @return the name of test action or <code>null</code>.
     */
    @CheckForNull String getTestAllAction();
}
