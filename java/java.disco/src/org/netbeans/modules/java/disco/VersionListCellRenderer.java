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
package org.netbeans.modules.java.disco;

import eu.hansolo.jdktools.TermOfSupport;
import java.awt.Component;
import java.util.Collections;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import org.checkerframework.checker.guieffect.qual.UIType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@UIType
public class VersionListCellRenderer extends DefaultListCellRenderer {

    private Map<Integer, TermOfSupport> lts = Collections.EMPTY_MAP;
    private int current = -1;

    @Override
    public Component getListCellRendererComponent(JList list, @Nullable Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Integer feature = (Integer) value;
        if (lts.containsKey(feature) && lts.get(feature) == TermOfSupport.LTS) {
            value = LTSes.text(feature, lts.get(feature));
        }
        if (isEA(feature)) {
            value += " (EA)";
        }
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        return this;
    }

    public boolean isEA(int feature) {
        return current != -1 && feature > current;
    }

    public void setLTS(@NonNull Map<Integer, TermOfSupport> lts) {
        this.lts = lts;
    }

    public void setCurrentJDK(int current) {
        this.current = current;
    }
}
