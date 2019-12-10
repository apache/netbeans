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
package org.openide.util.spi;

import java.io.IOException;
import java.net.URL;
import javax.swing.Icon;
import org.openide.util.lookup.ServiceProvider;

/**
 * SVG image loader. This is an optional service provider. If implemented, a single instance should
 * be placed in the default lookup (e.g. via the {@link ServiceProvider} annotation).
 */
public interface SVGLoader {
    /**
     * Load an SVG image as an {@link Icon}. The SVG document's root element must contain explicit
     * width/height attributes.
     *
     * @param url may not be null
     * @return may not be null
     * @throws IOException in case of loading or parsing errors
     */
    public Icon loadIcon(URL url) throws IOException;
}
