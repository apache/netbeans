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
package org.openide.util.svg;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.openide.util.ImageUtilities;

/**
 *
 * @author sdedic
 */
public class SVGLoaderImplTest extends NbTestCase {

    public SVGLoaderImplTest(String name) {
        super(name);
    }
    
    public void testLoadInnocentImage() throws Exception {
        Image im = ImageUtilities.loadImage("org/openide/util/svg/innocent.svg", false); // NOI18N
        assertNotNull("Image must not load", im);
    }
    
    public void testLoadImageWithExternalHref() throws Exception {
        class H extends Handler {
            List<LogRecord> recorded = new ArrayList<>();

            @Override
            public void publish(LogRecord lr) {
                recorded.add(lr);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        }
        H h = new H();
        Logger.getLogger(ImageUtilities.class.getName()).addHandler(h);
        try {
            Image im = ImageUtilities.loadImage("org/openide/util/svg/externalHref.svg", false); // NOI18N
            assertNull("Image must not load", im);
        } finally {
            Logger.getLogger(ImageUtilities.class.getName()).removeHandler(h);
        }
        Optional<LogRecord> report = h.recorded.stream().filter(lr ->
                    lr.getLevel().intValue() >= Level.INFO.intValue() &&
                    lr.getSourceMethodName().equals("getIcon")).
                findFirst();
        assertTrue("getIcon should have reported a message", report.isPresent());
        assertTrue("should report failed load", report.get().getMessage().startsWith("Failed to load SVG"));
    }
}
