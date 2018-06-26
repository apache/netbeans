/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.spi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.netbeans.api.annotations.common.CheckForNull;

/**
 * Encapsulates annotation line parser.
 *
 * <p>This interface allows providing support for custom PHP annotation parsers.</p>
 *
 * <p>Globally available instances of this interface are registered
 * in the <code>{@value org.netbeans.modules.php.api.annotations.PhpAnnotations#ANNOTATIONS_LINE_PARSERS_PATH}</code>
 * in the module layer, see {@link Registration}.
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public interface AnnotationLineParser {

    /**
     * Parses the given line.
     *
     * <p>Returns parsed line if that line belongs to this line parser,
     * {@code null} otherwise.</p>
     *
     * @param line
     * @return parsed line; can be {@code null}
     */
    @CheckForNull
    public AnnotationParsedLine parse(String line);

    /**
     * Declarative registration of a singleton PHP Annotation Line Parser.
     * that is <b>globally</b> available.
     * <p>
     * By marking an implementation class or a factory method with this annotation,
     * you automatically register that implementation, normally in
     * {@link org.netbeans.modules.php.api.annotations.PhpAnnotations#ANNOTATIONS_LINE_PARSERS_PATH}.
     * The class must be public and have:
     * <ul>
     *  <li>a public no-argument constructor, or</li>
     *  <li>a public static factory method.</li>
     * </ul>
     *
     * <p>Example of usage:
     * <pre>
     * package my.module;
     * import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;
     * &#64;AnnotationLineParser.Registration(position=100)
     * public class MyProvider implements AnnotationLineParser {...}
     * </pre>
     * <pre>
     * package my.module;
     * import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;
     * public class MyProvider implements AnnotationLineParser {
     *     &#64;AnnotationLineParser.Registration(position=100)
     *     public static AnnotationLineParser getInstance() {...}
     * }
     * </pre>
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface Registration {

        /**
         * An optional position in which to register this parser relative to others.
         * Lower-numbered services are returned in the lookup result first.
         * Parsers with no specified position are returned last.
         */
        int position() default Integer.MAX_VALUE;

    }

}
