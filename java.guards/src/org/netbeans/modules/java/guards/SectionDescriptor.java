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

package org.netbeans.modules.java.guards;

/** Class for holding information about the one special (guarded)
* comment. It is created by GuardedReader and used by
* JavaEditor to creating the guarded sections.
*/
public final class SectionDescriptor {
    /** Type - one of T_XXX constant */
    private GuardTag type;

    /** Name of the section comment */
    private String name;

    /** offset of the begin */
    private int begin;

    /** offset of the end */
    private int end;

    /** Simple constructor */
    public SectionDescriptor(GuardTag type) {
        this.type = type;
        name = null;
        begin = 0;
        end = 0;
    }
    
    public SectionDescriptor(GuardTag type, String name, int begin, int end) {
        this.type = type;
        this.name = name;
        this.begin = begin;
        this.end = end;
    }

    /** offset of the begin */
    public int getBegin() {
        return begin;
    }

    /** Name of the section comment */
    public String getName() {
        return name;
    }

    /** offset of the end */
    public int getEnd() {
        return end;
    }

    public GuardTag getType() {
        return type;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public void setName(String name) {
        this.name = name;
    }


}
