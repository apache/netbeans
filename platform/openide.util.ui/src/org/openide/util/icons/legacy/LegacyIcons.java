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
package org.openide.util.icons.legacy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.icons.GeneralIcon;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.spi.LegacyIconLocator;
import org.openide.util.spi.WhateverIcon;

/**
 *
 * @author lkishalmi
 */
@ServiceProvider(service = LegacyIconLocator.class )
public class LegacyIcons implements LegacyIconLocator {

    final Map<String, WhateverIcon> locationMap = new HashMap<>();

    public LegacyIcons() {
        for (GeneralIcon icon : GeneralIcon.values()) {
            try (InputStream is = LegacyIcons.class.getClassLoader().getResourceAsStream(icon.name() + ".locations")) {
                if (is != null) {
                    BufferedReader ir = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                    while (ir.ready()) {
                        locationMap.put(ir.readLine(), icon);
                    }
                    
                }
            } catch (IOException ex) {

            }
        }
    }

    @Override
    public WhateverIcon findIcon(String name) {
        return locationMap.get(name);
    }

}
