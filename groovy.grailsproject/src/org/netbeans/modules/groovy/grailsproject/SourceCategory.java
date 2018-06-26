/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.groovy.grailsproject;

/**
 * Represents a grails source category.
 * @author Bruno Flavio
 */
public class SourceCategory {

    private final String relativePath;

    private final String command;

    private final String suffix;

    /**
     * Creates a grails source category.
     * @param relativePath place where files of this category should be stored
     * @param command grails command that should be invoked to create a file of this category
     * @param suffix suffix present in every file name of this category
     */
    SourceCategory(String relativePath, String command, String suffix) {
        this.relativePath = relativePath;
        this.command = command;
        this.suffix = suffix;
    }

    /**
     * Returns this category relative path, i.e the folder where source files
     * of this category should be placed.
     * 
     * @return the relative path.
     */
    public String getRelativePath() {
        return relativePath;
    }

    /**
     * Returns the grails command that creates files of this category.
     * @return the grails command.
     */
    public String getCommand() {
        return command;
    }

    /**
     * Returns the filename suffix that should be applied by convention to this
     * source category.
     * @return  filename suffix.
     */
    public String getSuffix() {
        return suffix;
    }
}
