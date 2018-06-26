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
package org.netbeans.modules.ws.qaf.utilities;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.diff.LineDiff;

/**
 *   Same as org.netbeans.junit.diff.LineDiff except of compareLines method.
 *
 * @author jungi
 */
public class FilteringLineDiff extends LineDiff {

    private static final Logger LOGGER = Logger.getLogger(FilteringLineDiff.class.getName());
    
    /**
     * Creates a new instance of FilteringLineDiff
     *
     */
    public FilteringLineDiff() {
        this(false, false);
    }

    public FilteringLineDiff(boolean ignoreCase) {
        this(ignoreCase, false);
    }

    public FilteringLineDiff(boolean ignoreCase, boolean ignoreEmptyLines) {
        super(ignoreCase, ignoreEmptyLines);
    }

    /**
     *  Lines beginning with " * Created ", " * @author " or "import " and empty
     * lines are treated equals. One can check import statements by building
     * a project.
     *
     * @param l1 first line to compare
     * @param l2 second line to compare
     * @return true if lines equal
     */
    @Override
    protected boolean compareLines(String l1, String l2) {
        if (super.compareLines(l1, l2)) {
            return true;
        }
        if (((l1.indexOf(" * Created ") == 0) && (l2.indexOf(" * Created ") == 0))
                || ((l1.indexOf(" * @author ") == 0) && (l2.indexOf(" * @author ") == 0))
                || ((l1.indexOf("Created-By: ") == 0) && (l2.indexOf("Created-By: ") == 0))
                || ((l1.indexOf("import ") == 0) && (l2.indexOf("import ") == 0))
                ) {
            return true;
        }
        //we're not interested in changes in whitespaces, only content is important
        if (super.compareLines(l1.trim(), l2.trim())) {
            return true;
        }
        //WA: there's some strange random issue which causes that some
        //"randomly" chosen types are used with FQN
        String pkg = "o.n.m.ws.qaf.rest.crud.service.";//NOI18N
        if (l1.replaceAll(pkg, "").equals(l2.replaceAll(pkg, ""))) {
            LOGGER.log(Level.WARNING, "skiping \"{0}\" and \"{1}\"", new Object[]{l1, l2}); //NOI18N
            return true;
        }
        pkg = "o.n.m.ws.qaf.rest.crud.converter.";//NOI18N
        if (l1.replaceAll(pkg, "").equals(l2.replaceAll(pkg, ""))) {
            LOGGER.log(Level.WARNING, "skiping \"{0}\" and \"{1}\"", new Object[]{l1, l2}); //NOI18N
            return true;
        }
        pkg = "org.codehaus.jettison.json.";//NOI18N
        if (l1.replaceAll(pkg, "").equals(l2.replaceAll(pkg, "")) || l1.startsWith("import " + pkg) || l2.startsWith("import " + pkg)) {
            LOGGER.log(Level.WARNING, "skiping \"{0}\" and \"{1}\"", new Object[]{l1, l2}); //NOI18N
            return true;
        }
        //to avoid having two sets of golden files (for ant/maven based projects)
        if (l1.contains("private static String DEFAULT_PU = ")) { //NOI18N
            LOGGER.log(Level.WARNING, "skiping \"{0}\" and \"{1}\"", new Object[]{l1, l2}); //NOI18N
            return l2.trim().startsWith("private static String DEFAULT_PU = "); //NOI18N
        }
        return false;
    }
}
