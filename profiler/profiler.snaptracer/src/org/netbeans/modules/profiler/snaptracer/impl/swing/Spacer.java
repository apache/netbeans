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

package org.netbeans.modules.profiler.snaptracer.impl.swing;

import java.awt.Dimension;
import javax.swing.JPanel;

/**
 * Subclass of a JPanel to be used as a spacer in GridBagLayout. Creates a
 * non-opaque JPanel with null Layout and zero preferred size.
 *
 * @author Jiri Sedlacek
 * @since VisualVM 1.3
 */
public final class Spacer extends JPanel {
    
    private static final Dimension DIMENSION_ZERO = new Dimension(0, 0);


    /**
     * Creates new instance of Spacer.
     *
     * @return new instance of Spacer
     */
    public static Spacer create() { return new Spacer(); }


    public Dimension getPreferredSize() { return DIMENSION_ZERO; }

    private Spacer() {
        super(null);
        setOpaque(false);
    }

}
