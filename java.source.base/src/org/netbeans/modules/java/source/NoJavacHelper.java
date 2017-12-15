 /*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.java.source;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.openide.modules.OnStart;
import sun.misc.Unsafe;

/**
 *
 * @author lahvac
 */
public class NoJavacHelper {

    public static boolean hasWorkingJavac() {
        try {
            Class.forName("javax.lang.model.element.ModuleElement");
            return true;
        } catch (ClassNotFoundException ex) {
            //OK
            return false;
        }
    }

    public static boolean hasNbJavac() {
        try {
            Class.forName("com.sun.tools.javac.comp.Repair");
            return true;
        } catch (ClassNotFoundException ex) {
            //OK
            return false;
        }
    }

    @OnStart
    public static class FixClasses implements Runnable {

        @Override
        public void run() {
            if (!hasWorkingJavac()) {
                ClassWriter w = new ClassWriter(0);
                w.visit(Opcodes.V1_8, Opcodes.ACC_ABSTRACT | Opcodes.ACC_PUBLIC, "com/sun/tools/javac/code/Scope$WriteableScope", null, "com/sun/tools/javac/code/Scope", null);
                byte[] classData = w.toByteArray();
                try {
                    Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                    theUnsafe.setAccessible(true);
                    Unsafe unsafe = (Unsafe) theUnsafe.get(null);
                    Class scopeClass = Class.forName("com.sun.tools.javac.code.Scope");
                    unsafe.defineClass("com.sun.tools.javac.code.Scope$WriteableScope", classData, 0, classData.length, scopeClass.getClassLoader(), scopeClass.getProtectionDomain());
                } catch (Throwable t) {
                    //ignore...
                    Logger.getLogger(NoJavacHelper.class.getName()).log(Level.FINE, null, t);
                }
            }
        }

    }
}
