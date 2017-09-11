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
package org.openide.modules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a code that implements constructor contract for backward compatibility.
 * A static void method must be annotated, name is irrelevant. The method must
 * take the enclosing type marked by <code>@PatchFor</code> implementation type
 * as its first parameter. The rest of parameter types will be used to generate
 * a constructor in the original API class, with the same thrown exceptions and
 * access modifiers.
 * <p/>
 * The generated constructor will first call
 * <b>the default constructor</b> of the API class, then delegate the
 * initialization work to the static method passing <code>this</code> typed as
 * the {@link PatchFor @PatchFor}
 * class as the first parameter. Formally, at compile-time, the API class does
 * not derive from the <code>@PatchFor</code> supertype. Passing the new
 * instance typed as the API class would require type casting to the
 * <code>@PatchFor</code> supertype in order to access the data for the injected
 * code.
 * <p/>
 * The annotation has only effect if the method's declaring class was annotated
 * using {@link PatchFor @PatchFor}.
 * <p/>
 * Take, for example, the following code from <code>openide.filesystems</code> module:
 * <code><pre>
 *  &#064PatchFor(JarFileSystem.class)
 * public abstract class JarFileSystemCompat extends AbstractFileSystem {
 *    &#064ConstructorDelegate
 *   public static void createJarFileSystemCompat(JarFileSystemCompat jfs, FileSystemCapability cap) throws IOException {
 *     ...
 *   }
 * }
 * </pre></code>
 * Will cause generate, at runtime (during class loading), a new constructor in the
 * <code>JarFileSystem</code> class: 
 * <code><pre>
 *      JarFileSystem(FileSystemCapability c) throws IOException {
 *          this();
 *          JarFileSystemCompat.createJarFileSystemCompat(this, c);
 *      }
 * </pre></code>
 * <p/>
 * In the case it is necessary to invoke a <b>non-default</b> constructor, the
 * {@link #delegateParams()} attribute enumerates which parameters of the static 
 * annotated method are also passed to the non-default constructor. Parameter positions
 * are zero-based (0 = the object type itself) and must be listed in the same order 
 * and have the same types as in the invoked constructor declaration.
 *
 * <code><pre>
 *  &#064PatchFor(JarFileSystem.class)
 * public abstract class JarFileSystemCompat extends AbstractFileSystem {
 *    &#064ConstructorDelegate(delegateParams = {1, 2} )
 *   public static void createJarFileSystemCompat(JarFileSystemCompat jfs, FileSystemCapability cap, File f) throws IOException {
 *     ...
 *   }
 * }
 * </pre></code>
 * will produce an equivalent of
 * <code><pre>
 *      JarFileSystem(FileSystemCapability cap, File f) throws IOException {
 *          this(f);
 *          JarFileSystemCompat.createJarFileSystemCompat(this, cap, f);
 *      }
 * </pre></code>
 * 
 * @see PatchFor
 * @since 7.44
 * @author sdedic
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
public @interface ConstructorDelegate {
    /**
     * Specifies the position of parameters passed to the real class' constructor.
     * The array must contain an item for each parameter passed to the real constructor, in the
     * order specified by the invoked constructor signature.
     * Each item is a zero-based parameter position in the static creation method signature.
     * Order and type of the referenced parameters must match the target constructor signature.
     * @return 
     */
    public int[] delegateParams() default -1;
}
