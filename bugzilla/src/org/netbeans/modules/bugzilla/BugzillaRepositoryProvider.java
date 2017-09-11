/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * <p/>
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 * <p/>
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 * <p/>
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * <p/>
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 * <p/>
 * Contributor(s):
 * <p/>
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugzilla;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.team.spi.NBRepositoryProvider;
import org.netbeans.modules.team.spi.OwnerInfo;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaRepositoryProvider implements RepositoryProvider<BugzillaRepository, BugzillaQuery, BugzillaIssue>, NBRepositoryProvider<BugzillaQuery, BugzillaIssue> {

    @Override
    public Image getIcon(BugzillaRepository r) {
        return r.getIcon();
    }

    @Override
    public RepositoryInfo getInfo(BugzillaRepository r) {
        return r.getInfo();
    }

    @Override
    public void removed(BugzillaRepository r) {
        r.remove();
    }

    @Override
    public RepositoryController getController(BugzillaRepository r) {
        return r.getController();
    }

    @Override
    public BugzillaQuery createQuery(BugzillaRepository r) {
        return r.createQuery();
    }

    @Override
    public BugzillaIssue createIssue(BugzillaRepository r) {
        return r.createIssue();
    }

    @Override
    public Collection<BugzillaQuery> getQueries(BugzillaRepository r) {
        return r.getQueries();
    }

    @Override
    public Collection<BugzillaIssue> simpleSearch(BugzillaRepository r, String criteria) {
        return r.simpleSearch(criteria);
    }

    @Override
    public Collection<BugzillaIssue> getIssues(BugzillaRepository r, String... id) {
        return r.getIssues(id);
    }

    @Override
    public boolean canAttachFiles(BugzillaRepository r) {
        return true;
    }
    
    @Override
    public void removePropertyChangeListener(BugzillaRepository r, PropertyChangeListener listener) {
        r.removePropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(BugzillaRepository r, PropertyChangeListener listener) {
        r.addPropertyChangeListener(listener);
    }

    @Override
    public BugzillaIssue createIssue(BugzillaRepository r, String summary, String description) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /************************************************************************************
     * NB Bugzilla
     ************************************************************************************/
    
    @Override
    public void setIssueOwnerInfo(BugzillaIssue i, OwnerInfo info) {
        i.setOwnerInfo(info);
    }

    @Override
    public void setQueryOwnerInfo(BugzillaQuery q, OwnerInfo info) {
        q.setOwnerInfo(info);
    }
}
