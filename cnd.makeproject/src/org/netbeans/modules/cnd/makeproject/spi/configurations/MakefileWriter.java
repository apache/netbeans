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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.makeproject.spi.configurations;

import java.io.IOException;
import java.io.Writer;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;

public interface MakefileWriter {
    /**
     * Writes first section of generated makefile
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writePrelude(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;

    /**
     * Writes main build target
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writeBuildTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;

    /**
     * Writes build test target
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writeBuildTestTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;

    /**
     * Writes test target
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writeRunTestTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;

    /**
     * Writes all compile targets (only for managed projects)
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writeCompileTargets(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;

    /**
     * Writes all compile targets (only for managed projects)
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writeCompileTestTargets(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;

    /**
     * Writes link target (only for linked projects)
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writeLinkTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;

    /**
     * Writes link test target (only for linked projects)
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writeLinkTestTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;

    /**
     * Writes writes archive target (only for archive projects)
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writeArchiveTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;

    /**
     * Writes target for unmanaged projects
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writeMakefileTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;

    /**
     * Writes target for QT projects
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param bw  output stream to generated makefile
     */
    void writeQTTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;

    /**
     * Writes clan target
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writeCleanTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;

    /**
     * Writes targets for sub projects
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writeSubProjectBuildTargets(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;

    /**
     * Writes clean target for sub projects
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writeSubProjectCleanTargets(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;
    
    /**
     * Writes dependency checking target
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writeDependencyChecking(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;
}
