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

package org.netbeans.modules.bugtracking.dummies;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.netbeans.modules.bugtracking.TestKit;
import org.netbeans.modules.bugtracking.TestRepository;
import org.netbeans.modules.bugtracking.spi.*;
import org.netbeans.modules.bugtracking.ui.repository.RepositoryComboSupport;
import org.openide.util.Lookup;

/**
 *
 * @author Marian Petras
 */
public class DummyRepository extends TestRepository {

    private static final Image icon;

    static {
        try {
            InputStream is = DummyRepository.class.getResourceAsStream(
                    "/org/netbeans/modules/bugtracking/dummies/DummyRepositoryIcon.png");
            icon = ImageIO.read(is);
            is.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private final DummyBugtrackingConnector connector;
    private final String id;
    private RepositoryInfo info;
    private final boolean canAttach;

    public DummyRepository(DummyBugtrackingConnector connector, String id, boolean canAttach) {
        this.connector = connector;
        this.id = id;
        this.canAttach = canAttach;
        info = new RepositoryInfo(id, DummyBugtrackingConnector.ID, null, "Dummy repository \"" + id + '"', "dummy repository created for testing purposes", null, null, null, null);
    }

    @Override
    public Image getIcon() {
        return icon;
    }

    @Override
    public RepositoryInfo getInfo() {
        return info;
    }

    @Override
    public void remove() {
        connector.removeRepository(TestKit.getRepository(this));
    }

    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    @Override
    public String toString() {
        return getInfo().getDisplayName();
    }

    @Override
    public boolean canAttachFile() {
        return canAttach;
    }
    
}
