/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.modelimpl.content.project;

import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.KeyFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 * A common ancestor for project components that
 * 1) has key (most likely a project-based one);
 * 2) are able to put themselves into repository.
 *
 * It similar to Identifiable, but doesn't involve UIDs:
 * UIDs are unnecessary for such internal components as different project parts.
 *
 */
public abstract class ProjectComponent implements Persistent, SelfPersistent {

    private final Key key;

    public ProjectComponent(Key key) {
        CndUtils.assertTrueInConsole(key == null || key.getBehavior() == Key.Behavior.LargeAndMutable, "should be LargeAndMutable ", key);
        this.key = key;
    }

    public ProjectComponent(RepositoryDataInput in) throws IOException {
        key = KeyFactory.getDefaultFactory().readKey(in);
        if (TraceFlags.TRACE_PROJECT_COMPONENT_RW) {
            System.err.printf("< ProjectComponent: Reading %s key %s%n", this, key);
        }
    }

    public Key getKey() {
        return key;
    }

    /** conveniency shortcut */
    protected final int getUnitId() {
        return getKey().getUnitId();
    }

    public void put() {
        if (TraceFlags.TRACE_PROJECT_COMPONENT_RW) {
            System.err.printf("> ProjectComponent: store %s by key %s%n", this, key);
        }
        RepositoryUtils.put(key, this);
    }

//    private void putImpl() {
//	if( TraceFlags.TRACE_PROJECT_COMPONENT_RW ) System.err.printf("> ProjectComponent: Putting %s by key %s%n", this, key);
//	RepositoryUtils.put(key, this);
//    }
    @Override
    public void write(RepositoryDataOutput out) throws IOException {
        if (TraceFlags.TRACE_PROJECT_COMPONENT_RW) {
            System.err.printf("> ProjectComponent: Writing %s by key %s%n", this, key);
        }
        writeKey(key, out);
    }

    public static Key readKey(RepositoryDataInput in) throws IOException {
        return KeyFactory.getDefaultFactory().readKey(in);
    }

    public static void writeKey(Key key, RepositoryDataOutput out) throws IOException {
        KeyFactory.getDefaultFactory().writeKey(key, out);
    }

//    public static void setStable(Key key) {
//        Persistent p = RepositoryUtils.tryGet(key);
//        if (p != null) {
//            assert p instanceof ProjectComponent;
//            //ProjectComponent pc = (ProjectComponent) p;
//            // A workaround for #131701
//            //pc.putImpl();
//        }
//    }
}

