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

package org.netbeans.modules.spring.util;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import org.netbeans.modules.spring.api.beans.model.Location;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class SpringBeansUIs {

    private SpringBeansUIs() {}

    public static GoToBeanAction createGoToBeanAction(SpringBean bean) {
        Location location = bean.getLocation();
        if (location != null) {
            return new GoToBeanAction(FileUtil.toFile(location.getFile()), location.getOffset());
        }
        return null;
    }

    public static final class GoToBeanAction extends AbstractAction {

        private final File file;
        private final int offset;

        public GoToBeanAction(File file, int offset) {
            this.file = file;
            this.offset = offset;
        }

        public void actionPerformed(ActionEvent e) {
            SpringXMLConfigEditorUtils.openFile(file, offset);
        }

        public void invoke() {
            actionPerformed(null);
        }
    }
}
