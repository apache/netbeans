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
package org.netbeans.modules.xml.sync;

import org.openide.nodes.*;

/**
 * Same data may have multiple representations. A representation can
 * be described using this interface.
 *
 * @author  Petr Kuzel
 * @version
 */
public interface Representation {

    /**
     * @return select button diplay name used during notifying concurent modification
     * conflict.
     */
    public String getDisplayName();

    /**
     * Is this representation modified since last update?
     * Warning isModified() does not equals representationChanged().
     */
    public boolean isModified();

    /**
     * Determine whether given representation is valid. E.g. tree
     * represnattion is valid just if parsed successfully.
     */
    public boolean isValid();
    
    /**
     * Update the representation without marking it as modified.
     */
    public void update(Object change);

    /**
     * Return prefered update class or null if does not matter.
     * //??? Could return <codE>Class[]</code> in future.
     */
    public Class getUpdateClass();

    /**
     * Return modification passed as update parameter to all slave representations.
     * @param type if null return arbitrary representation
     * @return Change or null if change of given type can not be returned
     */
    public Object getChange(Class type);

    /**
     * Does this representation wraps given model?
     */
    public boolean represents(Class type);

    /**
     * Returnrepresentation level: 0 = file, 1 = byte buffer [text],
     * 2 = structural model, 3 = semantics model ... A higher level
     * representaion requires that lower level representation is
     * loaded too.
     *
     * @return
     */
    public int level();
}
