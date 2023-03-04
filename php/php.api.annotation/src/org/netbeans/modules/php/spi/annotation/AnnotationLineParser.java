/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
