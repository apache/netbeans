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

package org.netbeans;

import java.io.IOException;
import java.util.jar.Manifest;

/** Exception thrown indicating that a module's contents are ill-formed.
 * This could be a parse error in the manifest, or an inability to load
 * certain resources from the classloader.
 * ErrorManager should be used where needed to attach related exceptions
 * or user-friendly annotations.
 * @author Jesse Glick
 */
public final class InvalidException extends IOException {

    private final Module m;
    private final Manifest man;
    private String localizedMessage;

    public InvalidException(String detailMessage) {
        super(detailMessage);
        m = null;
        man = null;
    }
    
    public InvalidException(Module m, String detailMessage) {
        super(m + ": " + detailMessage); // NOI18N
        this.m = m;
        this.man = null;
    }

    InvalidException(String msg, Manifest manifest) {
        super(msg);
        this.m = null;
        this.man = manifest;
    }

    public InvalidException(Module m, String detailMessage, String localizedMessage) {
        this(m, detailMessage);
        this.localizedMessage = localizedMessage;
    }

    /** Affected module. May be null if this is hard to determine
     * (for example a problem which would make the module ill-formed,
     * during creation or reloading).
     */
    public Module getModule() {
        return m;
    }

    /** The manifest that caused this exception. Can be null, if the
     * manifest cannot be obtained.
     * @return manifest that contains error 
     * @since 2.20
     */
    public Manifest getManifest() {
        if (man != null) {
            return man;
        }
        if (m != null) {
            return m.getManifest();
        }
        return null;
    }

    @Override
    public String getLocalizedMessage() {
        if (localizedMessage != null) {
            return localizedMessage;
        } else {
            return super.getLocalizedMessage();
        }
    }
}
