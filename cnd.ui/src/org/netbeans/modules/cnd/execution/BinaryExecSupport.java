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
package org.netbeans.modules.cnd.execution;

import java.io.IOException;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/** Support for execution of a class file. Looks for the class with
 * the same base name as the primary file, locates a main method
 * in it, and starts it.
 *
 */
public final class BinaryExecSupport extends ExecutionSupport {

    private static final String PROP_RUN_DIRECTORY = "rundirectory"; // NOI18N

    /** new BinaryExecSupport */
    public BinaryExecSupport(Entry entry) {
        super(entry);
    }

    @Override
    public void addProperties(Sheet.Set set) {
        set.put(createRunDirectoryProperty());
    }

    /**
     *  Create the run directory property.
     *
     *  @return The run directory property
     */
    private PropertySupport<String> createRunDirectoryProperty() {

        return new PropertySupport.ReadWrite<String>(PROP_RUN_DIRECTORY, String.class,
                getString("PROP_RUN_DIRECTORY"), // NOI18N
                getString("HINT_RUN_DIRECTORY")) { // NOI18N

            @Override
            public String getValue() {
                return getRunDirectory();
            }

            @Override
            public void setValue(String val) {
                setRunDirectory(val);
            }

            @Override
            public boolean supportsDefaultValue() {
                return true;
            }

            @Override
            public void restoreDefaultValue() {
                setValue(null);
            }

            @Override
            public boolean canWrite() {
                return getEntry().getFile().getParent().canWrite();
            }
        };
    }

    /**
     *  Get the the run directory, the directory to invoke make from.
     *
     *  @return the run directory
     */
    public String getRunDirectory() {
        String dir = (String) getEntry().getFile().getAttribute(PROP_RUN_DIRECTORY);

        if (dir == null) {
            dir = "."; // NOI18N
            setRunDirectory(dir);
        }

        return dir;
    }

    /**
     *  Set the run directory
     *
     *  @param target the run directory
     */
    public void setRunDirectory(String dir) {
        try {
            getEntry().getFile().setAttribute(PROP_RUN_DIRECTORY, dir);
        } catch (IOException ex) {
            //String msg = MessageFormat.format("INTERNAL ERROR: Cannot set run directory", // NOI18N
            //        new Object[]{FileUtil.toFile(getEntry().getFile()).getPath()});

            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                ex.printStackTrace();
            }
        }
    }
    private String getString(String s) {
        return NbBundle.getBundle(BinaryExecSupport.class).getString(s);
    }
}
