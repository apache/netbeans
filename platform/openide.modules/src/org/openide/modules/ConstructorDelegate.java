/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
 * <p>
 * The generated constructor will first call
 * <b>the default constructor</b> of the API class, then delegate the
 * initialization work to the static method passing <code>this</code> typed as
 * the {@link PatchFor @PatchFor}
 * class as the first parameter. Formally, at compile-time, the API class does
 * not derive from the <code>@PatchFor</code> supertype. Passing the new
 * instance typed as the API class would require type casting to the
 * <code>@PatchFor</code> supertype in order to access the data for the injected
 * code.
 * <p>
 * The annotation has only effect if the method's declaring class was annotated
 * using {@link PatchFor @PatchFor}.
 * <p>
 * Take, for example, the following code from <code>openide.filesystems</code> module:
 * <pre>{@code 
 *  &#064PatchFor(JarFileSystem.class)
 * public abstract class JarFileSystemCompat extends AbstractFileSystem {
 *    &#064ConstructorDelegate
 *   public static void createJarFileSystemCompat(JarFileSystemCompat jfs, FileSystemCapability cap) throws IOException {
 *     ...
 *   }
 * }
 * }</pre>
 * Will cause generate, at runtime (during class loading), a new constructor in the
 * <code>JarFileSystem</code> class: 
 * <pre>{@code
 *      JarFileSystem(FileSystemCapability c) throws IOException {
 *          this();
 *          JarFileSystemCompat.createJarFileSystemCompat(this, c);
 *      }
 * }</pre>
 * <p>
 * In the case it is necessary to invoke a <b>non-default</b> constructor, the
 * {@link #delegateParams()} attribute enumerates which parameters of the static 
 * annotated method are also passed to the non-default constructor. Parameter positions
 * are zero-based (0 = the object type itself) and must be listed in the same order 
 * and have the same types as in the invoked constructor declaration.
 *
 * <pre>{@code
 *  &#064PatchFor(JarFileSystem.class)
 * public abstract class JarFileSystemCompat extends AbstractFileSystem {
 *    &#064ConstructorDelegate(delegateParams = {1, 2} )
 *   public static void createJarFileSystemCompat(JarFileSystemCompat jfs, FileSystemCapability cap, File f) throws IOException {
 *     ...
 *   }
 * }
 * }</pre>
 * will produce an equivalent of
 * <pre>{@code
 *      JarFileSystem(FileSystemCapability cap, File f) throws IOException {
 *          this(f);
 *          JarFileSystemCompat.createJarFileSystemCompat(this, cap, f);
 *      }
 * }</pre>
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
