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
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.spi;

import java.util.List;
import org.netbeans.modules.debugger.jpda.models.CallStackFrameImpl;

/**
 * A provider of strata.
 * Use when a default strata detection is not sufficient.
 * Register the implementation into the debugger session lookup.
 * 
 * @author Martin
 */
public interface StrataProvider {
    
    /**
     * Provide the default stratum of this call stack frame.
     * Do not call {@link CallStackFrameImpl#getDefaultStratum()} or
     * {@link CallStackFrameImpl#getAvailableStrata()} in this method.
     * The {@link CallStackFrameImpl} is populated with what you return here.
     * @param csf the stack frame to find the default strata for.
     * @return the desired default strata, or <code>null</code> to use the default impl.
     */
    String getDefaultStratum(CallStackFrameImpl csf);
    
    /**
     * Provide the list of strata of this call stack frame.
     * Do not call {@link CallStackFrameImpl#getDefaultStratum()} or
     * {@link CallStackFrameImpl#getAvailableStrata()} in this method.
     * The {@link CallStackFrameImpl} is populated with what you return here.
     * @param csf the stack frame to find the available strata for.
     * @return the desired available strata, or <code>null</code> to use the default impl.
     */
    List<String> getAvailableStrata(CallStackFrameImpl csf);
    
    int getStrataLineNumber(CallStackFrameImpl csf, String stratum);
}
