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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.api.validation.adapters;

import org.netbeans.validation.api.Problem;
import org.netbeans.validation.api.ui.ValidationUI;
import org.openide.WizardDescriptor;

/**
 *
 * @author Tim Boudreau
 */
public class WizardDescriptorAdapter implements ValidationUI {
    private final WizardDescriptor wiz;
    private final Type type;

    /**
     * 
     * @since 1.22
     */
    public enum Type {
        VALID, MESSAGE, VALID_AND_MESSAGE
    }
    public WizardDescriptorAdapter(WizardDescriptor d) {
        this(d, Type.VALID_AND_MESSAGE);
    }
    
    /**
     * allow to decide what ui elements should be affected, button enablement or error message or both.
     * @since 1.22
     */ 
    public WizardDescriptorAdapter(WizardDescriptor d, Type type) {
        this.wiz = d;
        this.type = type;
    }

    @Override
    public void clearProblem() {
        if (type != Type.VALID) {
            wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        }
        if (type != Type.MESSAGE) {
            wiz.setValid(true);
        }
    }

    @Override
    public void showProblem(Problem p) {
        switch (p.severity()) {
            case INFO :
                if (type != Type.VALID) {
                    wiz.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, p.getMessage());
                }
                if (type != Type.MESSAGE) {
                    wiz.setValid(true);
                }
                break;
            case WARNING :
                if (type != Type.VALID) {
                    wiz.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, p.getMessage());
                }
                if (type != Type.MESSAGE) {
                    wiz.setValid(true);
                }
                break;
            case FATAL :
                if (type != Type.VALID) {
                    wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, p.getMessage());
                }
                if (type != Type.MESSAGE) {
                    wiz.setValid(false);
                }
                break;
        }
    }
}
