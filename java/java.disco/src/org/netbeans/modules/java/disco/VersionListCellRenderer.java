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
package org.netbeans.modules.java.disco;

import io.foojay.api.discoclient.pkg.TermOfSupport;
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

    @Override
    public Component getListCellRendererComponent(JList list, @Nullable Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (lts.containsKey((Integer) value))
            value = LTSes.text((Integer) value, lts.get((Integer) value));
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        return this;
    }

    public void setLTS(@NonNull Map<Integer, TermOfSupport> lts) {
        this.lts = lts;
    }
}
