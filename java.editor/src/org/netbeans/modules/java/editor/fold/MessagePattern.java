/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */

package org.netbeans.modules.java.editor.fold;

import java.util.regex.Pattern;

/**
 * Describes a call in the code that obtains a message from the bundle, or bundle instance.
 * @author sdedic
 */
public final class MessagePattern {
    /**
     * Usable as 'bundleFile' param. The code uses ResourceBundle instance stored in a variable/field
     */
    public static final int BUNDLE_FROM_INSTANCE = -2;
    
    /**
     * Usable as 'bundleFile' param. Specifies that the bundle file should be derived from the target class name.
     */
    public static final int BUNDLE_FROM_CLASS = -4;
    
    /**
     * Usable as a value of 'key'. No key param is present, the pattern describes the code to obtain a bundle instance.
     */
    public static final int GET_BUNDLE_CALL = -3;
    
    /**
     * Usable as a value of 'keyParam'. The resource bundle key is derived from the called method name.
     */
    public static final int KEY_FROM_METHODNAME = -5;
    
    /**
     * Name of type that defines the method. May contain wildcards.
     */
    private Pattern ownerTypePattern;
    /**
     * Pattern for the method name
     */
    private Pattern methodNamePattern;
    /**
     * Bundle param defines location of the bundle.  {@link #BUNDLE_FROM_INSTANCE} means
     * that the instance assignment has to be found and inspected for bundle name
     */
    private int bundleParam;
    /**
     * Param index of the message key
     */
    private int keyParam;
    private String bundleFile = "Bundle"; // NOI18N

    public MessagePattern(Pattern ownerTypePattern, Pattern methodNamePattern, int bundleParam, int keyParam) {
        assert bundleParam >= 0 || (bundleParam == BUNDLE_FROM_CLASS || bundleParam == BUNDLE_FROM_INSTANCE);
        assert keyParam >= 0 || (keyParam == KEY_FROM_METHODNAME || keyParam == GET_BUNDLE_CALL);
        
        this.ownerTypePattern = ownerTypePattern;
        this.methodNamePattern = methodNamePattern;
        this.bundleParam = bundleParam;
        this.keyParam = keyParam;
    }

    public Pattern getOwnerTypePattern() {
        return ownerTypePattern;
    }

    public Pattern getMethodNamePattern() {
        return methodNamePattern;
    }

    public int getBundleParam() {
        return bundleParam;
    }

    public int getKeyParam() {
        return keyParam;
    }

    public String getBundleFile() {
        return bundleFile;
    }

    public void setBundleFile(String bundleFile) {
        this.bundleFile = bundleFile;
    }
    
}
