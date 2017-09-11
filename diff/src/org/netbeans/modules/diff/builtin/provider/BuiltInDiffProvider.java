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

package org.netbeans.modules.diff.builtin.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.openide.util.NbBundle;

import org.netbeans.api.diff.Difference;
import org.netbeans.spi.diff.DiffProvider;
import org.netbeans.modules.diff.DiffModuleConfig;

/**
 *
 * @author  Martin Entlicher
 */
public class BuiltInDiffProvider extends DiffProvider implements java.io.Serializable {

    private Options options = DiffModuleConfig.getDefault().getOptions();
    
    static final long serialVersionUID = 1L;

    /** Creates a new instance of BuiltInDiffProvider */
    public BuiltInDiffProvider() {
    }
    
    /**
     * Get the display name of this diff provider.
     */
    public String getDisplayName() {
        return NbBundle.getMessage(BuiltInDiffProvider.class, "BuiltInDiffProvider.displayName");
    }
    
    /**
     * Get a short description of this diff provider.
     */
    public String getShortDescription() {
        return NbBundle.getMessage(BuiltInDiffProvider.class, "BuiltInDiffProvider.shortDescription");
    }
    
    /**
     * Create the differences of the content two streams.
     * @param r1 the first source
     * @param r2 the second source to be compared with the first one.
     * @return the list of differences found, instances of {@link Difference};
     *        or <code>null</code> when some error occured.
     */
    public Difference[] computeDiff(Reader r1, Reader r2) throws IOException {
        if (options == null) {
            // blind fix of #144033, probably a deserialization issue?
            options = DiffModuleConfig.getDefault().getOptions();
        }
        return HuntDiff.diff(getLines(r1), getLines(r2), options);   
    }
    
    private String[] getLines(Reader r) throws IOException {
        BufferedReader br = new BufferedReader(r);
        String line;
        List<String> lines = new ArrayList<String>();
        while ((line = br.readLine()) != null) {
            lines.add(line);
        }
        return lines.toArray(new String[lines.size()]);
    }


    /**
     * @param options a new set of diff options
     */
    public void setOptions(Options options) {
        this.options = options;
    }

    public static class Options {
        /**
         * True to ignore leading and trailing whitespace when computing diff, false otherwise.
         */
        public boolean ignoreLeadingAndtrailingWhitespace;

        /**
         * True to ignore inner (not leading or trailing) whitespace when computing diff, false otherwise.
         */
        public boolean ignoreInnerWhitespace;

        /**
         * True to ignore changes in case.
         */
        public boolean ignoreCase;
    }
    
}
