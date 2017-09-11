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

package org.netbeans.nbbuild;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Mapper;

/** Expand the comma-separated list of properties to 
 *  their values and assing it to single property
 * 
 * @author Michal Zlamal
 */
public class ResolveList extends Task {
    
    private List<String> properties;
    private String name;
    private Mapper mapper;

    /** Comma-separated list of properties to expand */
    public void setList (String s) {
        StringTokenizer tok = new StringTokenizer (s, ", ");
        properties = new ArrayList<String>();
        while (tok.hasMoreTokens ())
            properties.add(tok.nextToken ());
    }

    /** New property name */
    public void setName(String s) {
        name = s;
    }

    /** Mapper to be applied to each property in the list before its
     * value is taken
     */
    public void addMapper(Mapper m) {
        this.mapper = m;
    }

    @Override
    public void execute () throws BuildException {
        if (name == null) throw new BuildException("name property have to be set", getLocation());
        String value = "";
        String prefix = "";
        for (String property: properties) {
            String[] props;
            if (mapper != null) {
                props = mapper.getImplementation().mapFileName(property);
            } else {
                props = new String[] { property };
            }

            for (String p : props) {
                String oneValue = getProject().getProperty( p );
                if (oneValue != null && oneValue.length() > 0) {
                    value += prefix + oneValue;
                    prefix = ",";
                }
            }
        }
        
        getProject().setNewProperty(name,value);
    }        
}
