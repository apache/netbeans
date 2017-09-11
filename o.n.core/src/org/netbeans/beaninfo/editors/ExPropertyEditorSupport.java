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
 * ExPropertyEditorSupport.java
 *
 * Created on March 26, 2003, 4:38 PM
 */

package org.netbeans.beaninfo.editors;
import java.beans.*;
import org.openide.explorer.propertysheet.*;
/** Support class for ExPropertyEditor which provides means for validating
 *  hints from the PropertyEnv instance passed to attachEnv.  Forces
 *  subclasses to be fail-fast in the case that illegal values are passed
 *  via the PropertyEnv (the alternative is cryptic error messages when
 *  the editor tries to use the hints).
 * @author  Tim Boudreau
 * @version 1.0
 */
public abstract class ExPropertyEditorSupport extends PropertyEditorSupport implements ExPropertyEditor {
    
    /** Creates a new instance of ExPropertyEditorSupport */
    protected ExPropertyEditorSupport() {
    }
    
    /** Implementation of PropertyEditorSupport.attachEnv().  This method
     *  is final to ensure that the values from the env are validated.
     *  Subclasses should override attachEnvImpl to provide the actual
     *  attaching behavior.  attachEnvImpl is called first, then
     *  validateEnv (to avoid fetching the values twice). */
    public final void attachEnv(PropertyEnv env) {
        attachEnvImpl(env);
        validateEnv(env);
    }
    
    /** Perform the actual attaching of the PropertyEnv.  */
    protected abstract void attachEnvImpl(PropertyEnv env);
    
    /** Validate values stored in the PropertyEnv.  This method allows
     *  subclasses to be fail-fast if they are supplied illegal values
     *  as hints from the PropertyEnv.  Subclasses should confirm that any
     *  hints used by their property editor are valid values.  If they
     *  are not valid, an EnvException should be thrown with a clear
     *  description of the problem.  */
    protected abstract void validateEnv(PropertyEnv env);
    
    /** This class exists to enable unit tests to differentiate
     *  between code bugs in the editors and invalid values from
     *  the propertyEnv.  */
    public static class EnvException extends IllegalArgumentException {
        public EnvException(String s) { super(s); }
    }
    
    /** Utility method to convert an array of Objects into a comma
     *  delimited string. */
    protected static final String arrToStr(Object[] s) {
        if (s == null) return "null"; //NOI18N
        StringBuilder out = new StringBuilder(s.length * 10);
        for (int i=0; i < s.length; i++) {
            if (s[i] != null) {
                out.append(s[i]);
            } else {
                out.append("null");
            }
            if (i != s.length-1) {
                out.append(","); //NOI18N
            }
        }
        return out.toString();
    }
}
