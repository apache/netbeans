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

package org.netbeans.api.javahelp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registers a help set.
 * A help set reference according to {@code -//NetBeans//DTD JavaHelp Help Set Reference 1.0//EN} is created.
 * If the help set specifies a search view, the search indexer will also be run;
 * all {@code *.html} and {@code *.htm} in the package containing the help set, and its subpackages, will be indexed.
 * @since org.netbeans.modules.javahelp/1 2.20
 * @see <a href="@TOP@/apichanges.html#HelpSetRegistration">how to convert to this annotation</a>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PACKAGE)
public @interface HelpSetRegistration {

    /**
     * Relative location of a help set file.
     * Typically matches: {@code -//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 2.0//EN}
     */
    String helpSet();

    /**
     * Whether the help set should be merged into the master help set.
     */
    boolean merge() default true;

    /**
     * Position of help set reference.
     */
    int position() default Integer.MAX_VALUE;

    /**
     * Helpset-relative HTML filenames to exclude from indexing.
     */
    String[] excludes() default {"credits.html"};

}
