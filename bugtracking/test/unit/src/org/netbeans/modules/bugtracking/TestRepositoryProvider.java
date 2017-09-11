/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;
import org.openide.util.Lookup;

/**
 *
 * @author tomas
 */
public class TestRepositoryProvider implements RepositoryProvider<TestRepository, TestQuery, TestIssue> {

    @Override
    public RepositoryInfo getInfo(TestRepository r) {
        return r.getInfo();
    }

    @Override
    public Image getIcon(TestRepository r) {
        return r.getIcon();
    }

    @Override
    public Collection<TestIssue> getIssues(TestRepository r, String... ids) {
        return r.getIssues(ids);
    }

    @Override
    public void removed(TestRepository r) {
        r.remove();
    }

    @Override
    public RepositoryController getController(TestRepository r) {
        return r.getController();
    }

    @Override
    public TestQuery createQuery(TestRepository r) {
        return r.createQuery();
    }

    @Override
    public TestIssue createIssue(TestRepository r) {
        return r.createIssue();
    }

    @Override
    public Collection<TestQuery> getQueries(TestRepository r) {
        return (Collection<TestQuery>) r.getQueries();
    }

    @Override
    public Collection<TestIssue> simpleSearch(TestRepository r, String criteria) {
        return (Collection<TestIssue>) r.simpleSearch(criteria);
    }

    @Override
    public void removePropertyChangeListener(TestRepository r, PropertyChangeListener listener) {
        r.removePropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(TestRepository r, PropertyChangeListener listener) {
        r.addPropertyChangeListener(listener);
    }

    @Override
    public TestIssue createIssue(TestRepository r, String summary, String description) {
        return r.createIssue(summary, description);
    }

    @Override
    public boolean canAttachFiles(TestRepository r) {
        return r.canAttachFile();
    }

}
