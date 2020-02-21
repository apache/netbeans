/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.api.remote;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Since the ServerList is updated from the Tools->Options panel, changes must be cached
 * until the OK button is pressed (T->O updates aren't immediately applied).
 * 
 */
public final class ServerUpdateCache {

    private List<ServerRecord> hosts;
    private ServerRecord defaultRecord;
    private static final Logger log = Logger.getLogger("cnd.remote.logger"); // NOI18N
    
    public ServerUpdateCache() {
        hosts = null;
        defaultRecord = null;
    }
    
    public synchronized List<ServerRecord> getHosts() {
        List<ServerRecord> h = hosts;
        if (h == null) {
            throw new IllegalStateException("hosts should not be null"); //NOI18N
        }
        return new ArrayList<ServerRecord>(hosts);
    }

    public synchronized void setHosts(Collection<? extends ServerRecord> newHosts) {
        hosts = new ArrayList<ServerRecord>(newHosts);
        fixDefaultRecordIfNeed();
    }

    private void fixDefaultRecordIfNeed() {
        if (defaultRecord == null || !hosts.contains(defaultRecord)) {
            if (!hosts.isEmpty()) {
                defaultRecord = hosts.get(0);
            }
        }
    }

    public synchronized ServerRecord getDefaultRecord() {
        fixDefaultRecordIfNeed();
        return defaultRecord;
    }

    public synchronized void setDefaultRecord(ServerRecord record) {
        assert hosts.contains(record);
        defaultRecord = record;
    }
}
