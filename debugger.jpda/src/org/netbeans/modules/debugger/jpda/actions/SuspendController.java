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

package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;

/**
 * Implementation of this interface can be used to control debugger suspension.
 * In some situation it can happen that it's better not to allow debugger to
 * suspend the program in the current state. These are situations like when
 * the debuggee is holding native locks that affect other applications on the
 * system. For instance under X-Windows when debuggee is holding an AWT lock,
 * no other applications (including the debugger application) can gain focus,
 * thus it's not desired to suspend debuggee in such state, or the implementation
 * code can perform actions leading to the releasing of such a lock.
 * 
 * @author Martin Entlicher
 */
public interface SuspendController {
    
    /**
     * Decide whether the thread can be suspended, or perform any steps
     * that are necessary.
     * @param tRef An already suspended thread
     * @return <code>true</code> when the thread can remain suspended, or
     *         <code>false</code> when the thread needs to be resumed.
     */
    boolean suspend(ThreadReference tRef);
    
    /**
     * Decide whether the whole virtual machine can be suspended,
     * or perform any steps that are necessary.
     * @param vm An already suspended virtual machine
     * @return <code>true</code> when the virtual machine can remain suspended, or
     *         <code>false</code> when it needs to be resumed.
     */
    boolean suspend(VirtualMachine vm);
}
