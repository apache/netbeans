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

package org.netbeans.modules.refactoring.java.ui;

import org.netbeans.modules.refactoring.java.spi.ui.JavaActionsImplementationProvider;
import org.openide.util.Lookup;

/**
 * @author Jan Becicka
 */
public final class JavaActionsImplementationFactory {
    
    private JavaActionsImplementationFactory(){}
    
    private static final Lookup.Result<JavaActionsImplementationProvider> implementations =
        Lookup.getDefault().lookup(new Lookup.Template(JavaActionsImplementationProvider.class));

    public static boolean canChangeParameters(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canChangeParameters(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static void doChangeParameters(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canChangeParameters(lookup)) {
                rafi.doChangeParameters(lookup);
                return;
            }
        }
    }

    public static boolean canIntroduceParameter(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canIntroduceParameter(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static void doIntroduceParameter(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canIntroduceParameter(lookup)) {
                rafi.doIntroduceParameter(lookup);
                return;
            }
        }
    }
    
    public static boolean canEncapsulateFields(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canEncapsulateFields(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static void doEncapsulateFields(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canEncapsulateFields(lookup)) {
                rafi.doEncapsulateFields(lookup);
                return;
            }
        }
    }
    public static boolean canExtractInterface(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canExtractInterface(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static void doExtractInterface(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canExtractInterface(lookup)) {
                rafi.doExtractInterface(lookup);
                return;
            }
        }
    }
    
    public static void doExtractSuperclass(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canExtractSuperclass(lookup)) {
                rafi.doExtractSuperclass(lookup);
                return;
            }
        }
    }
    
    public static boolean canExtractSuperclass(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canExtractSuperclass(lookup)) {
                return true;
            }
        }
        return false;
    }

    public static void doInnerToOuter(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canInnerToOuter(lookup)) {
                rafi.doInnerToOuter(lookup);
                return;
            }
        }
    }
    
    public static boolean canInnerToOuter(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canInnerToOuter(lookup)) {
                return true;
            }
        }
        return false;
    }

    public static void doPullUp(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canPullUp(lookup)) {
                rafi.doPullUp(lookup);
                return;
            }
        }
    }
    
    public static boolean canPullUp(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canPullUp(lookup)) {
                return true;
            }
        }
        return false;
    }

    public static void doPushDown(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canPushDown(lookup)) {
                rafi.doPushDown(lookup);
                return;
            }
        }
    }
    
    public static boolean canPushDown(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canPushDown(lookup)) {
                return true;
            }
        }
        return false;
    }

    public static void doUseSuperType(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canUseSuperType(lookup)) {
                rafi.doUseSuperType(lookup);
                return;
            }
        }
    }
    
    public static boolean canUseSuperType(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canUseSuperType(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean canInline(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canInline(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static void doInline(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canInline(lookup)) {
                rafi.doInline(lookup);
                return;
            }
        }
    }
    
    public static boolean canIntroduceLocalExtension(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canIntroduceLocalExtension(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static void doIntroduceLocalExtension(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canIntroduceLocalExtension(lookup)) {
                rafi.doIntroduceLocalExtension(lookup);
                return;
            }
        }
    }
}
