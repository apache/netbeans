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

/*
 * Diff.java
 *
 * Created on February 2, 2001, 2:53 PM
 */

package org.netbeans.junit.diff;

/**
 * This interface must be implemented by any class used as file-diff facility in assertFile functions.
 * It declares two functions, which are called whenever the file comparision is required. Their meaning
 * is identical, they only differ by arguments types.
 *
 * Generally, they both take three parameters, the first two specify files being compared and the third
 * is the file, where comparision results are stored. Third paramtere can be null in case no additional
 * output except the return value is needed.
 *
 * @author Jan Becicka
 * @version 0.1
 * @see junit.framework.Assert Assert class
 */
public interface Diff {
    
   /**
    * @param first first file to compare
    * @param second second file to compare
    * @param diff difference file, caller can pass null value, when results are not needed.
    * @return true iff files differ
    */
    public boolean diff(final java.io.File first, final java.io.File second, java.io.File diff) throws java.io.IOException;
    
   /**
    * @param first first file to compare
    * @param second second file to compare
    * @param diff difference file, caller can pass null value, when results are not needed.
    * @return true iff files differ
    */
    public boolean diff(final String first, final String second, String diff) throws java.io.IOException;
    
}
