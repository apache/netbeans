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
package org.netbeans.modules.javaee.project.spi;

import java.net.URL;
import org.netbeans.modules.javaee.project.api.ClientSideDevelopmentSupport;
import org.netbeans.modules.javaee.project.api.ClientSideDevelopmentSupport.Pattern;
import org.netbeans.modules.web.common.spi.ServerURLMappingImplementation;
import org.openide.filesystems.FileObject;

/**
 * URL mapping is handled by default by {@link ServerURLMappingImplementation}, But Java EE frameworks are able often
 * change significantly appearance of the created URL. It means that we need to obtain also frameworks specific mapping.
 * Such mapping can be provided by implementation of this interface.
 * @see ServerURLMappingImplementation
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public interface FrameworkServerURLMapping {

    /**
     * Searches existing file for given url.
     * @param docRoot documentation root of the project
     * @param mapping mapping pattern to be examined
     * @param uri URI including servlet mapping
     * @param urlQuery query of the url - i.e. ?ln=css&param=5
     * @return file which corresponds to the given URL or {@code null} if no such file exists
     */
    FileObject convertURLtoFile(FileObject docRoot, Pattern mapping, String uri, String urlQuery);

    /**
     * Guesses from file path to real browser URL.
     * @param file file
     * @param relPath relative URI of the file in its project
     * @return guessed relative path for the browser
     */
    String convertFileToRelativeURL(FileObject file, String relPath);

}
