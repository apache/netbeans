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

package org.netbeans.modules.spring.api.beans;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Parameters;

/**
 * Encapsulates a group of Spring config files.
 *
 * @author Andrei Badea
 */
public final class ConfigFileGroup {

    private final String name;
    // This needs to be a list to ensure the order is maintained.
    private final List<File> files;

    /**
     * Creates an unnamed group.
     *
     * @param  files the files to be put into this group.
     * @return a new group; never null.
     */
    public static ConfigFileGroup create(List<File> files) {
        return create(null, files);
    }

    /**
     * Creates a group with the given name.
     *
     * @param  name the name or null.
     * @param  files the files to be put into this group.
     * @return a new group; never null.
     */
    public static ConfigFileGroup create(String name, List<File> files) {
        return new ConfigFileGroup(name, files);
    }

    private ConfigFileGroup(String name, List<File> files) {
        Parameters.notNull("files", files);
        this.name = name;
        this.files = new ArrayList<File>(files.size());
        this.files.addAll(files);
    }

    /**
     * Returns the name, if any, of this group.
     *
     * @return the name or null.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the list of config files in this group. The list
     * is modifiable and not live.
     *
     * @return the list of beans configuration files; never null.
     */
    public List<File> getFiles() {
        List<File> result = new ArrayList<File>(files.size());
        result.addAll(files);
        return result;
    }

    public boolean containsFile(File file) {
        // Linear search, but we will hopefully only have a couple of
        // files in the group.
        return files.contains(file);
    }

    @Override
    public String toString() {
        return "ConfigFileGroup[name='" + name + "',files=" + files + "]"; // NOI18N
    }
}
