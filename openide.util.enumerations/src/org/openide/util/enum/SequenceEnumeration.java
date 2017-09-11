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
package org.openide.util.enum;

import java.util.Enumeration;


/**
 * Composes several enumerations into one.
 * @deprecated JDK 1.5 treats enum as a keyword so this class was
 *             replaced by {@link org.openide.util.Enumerations#concat}.
 * @author Jaroslav Tulach, Petr Nejedly
 */
public class SequenceEnumeration extends Object implements Enumeration {
    /** enumeration of Enumerations */
    private Enumeration en;

    /** current enumeration */
    private Enumeration current;

    /** is {@link #current} up-to-date and has more elements?
    * The combination <CODE>current == null</CODE> and
    * <CODE>checked == true means there are no more elements
    * in this enumeration.
    */
    private boolean checked = false;

    /** Constructs new enumeration from already existing. The elements
    * of <CODE>en</CODE> should be also enumerations. The resulting
    * enumeration contains elements of such enumerations.
    *
    * @param en enumeration of Enumerations that should be sequenced
    */
    public SequenceEnumeration(Enumeration en) {
        this.en = en;
    }

    /** Composes two enumerations into one.
    * @param first first enumeration
    * @param second second enumeration
    */
    public SequenceEnumeration(Enumeration first, Enumeration second) {
        this(new ArrayEnumeration(new Enumeration[] { first, second }));
    }

    /** Ensures that current enumeration is set. If there aren't more
    * elements in the Enumerations, sets the field <CODE>current</CODE> to null.
    */
    private void ensureCurrent() {
        while ((current == null) || !current.hasMoreElements()) {
            if (en.hasMoreElements()) {
                current = (Enumeration) en.nextElement();
            } else {
                // no next valid enumeration
                current = null;

                return;
            }
        }
    }

    /** @return true if we have more elements */
    public boolean hasMoreElements() {
        if (!checked) {
            ensureCurrent();
            checked = true;
        }

        return current != null;
    }

    /** @return next element
    * @exception NoSuchElementException if there is no next element
    */
    public synchronized Object nextElement() {
        if (!checked) {
            ensureCurrent();
        }

        if (current != null) {
            checked = false;

            return current.nextElement();
        } else {
            checked = true;
            throw new java.util.NoSuchElementException();
        }
    }
}
