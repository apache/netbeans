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

package org.netbeans.modules.maven.grammar.effpom;

import java.util.List;
import org.netbeans.editor.SideBarFactory;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.maven.grammar.effpom.LocationAwareMavenXpp3Writer.Location;


/**
 * strongly inspired by git's implementation
 * @author mkleint@netbeans.org
 * 
 */
public class AnnotationBarManager implements SideBarFactory {

    private static final Object BAR_KEY = new Object();

    /**
     * Creates initially hidden annotations sidebar.
     * It's called once by target lifetime.
     */
    @Override
    public JComponent createSideBar(JTextComponent target) {
        final AnnotationBar ab = new AnnotationBar(target);
        target.putClientProperty(BAR_KEY, ab);
        return ab;
    }

    /**
     * Shows annotations sidebar.
     */
    public static AnnotationBar showAnnotationBar(JTextComponent target, List<Location> locations) {
        AnnotationBar ab = (AnnotationBar) target.getClientProperty(BAR_KEY);
        assert ab != null;
        ab.annotate(locations);
        return ab;
    }

}

