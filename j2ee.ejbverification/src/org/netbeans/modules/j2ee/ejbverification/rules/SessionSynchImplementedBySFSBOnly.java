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
package org.netbeans.modules.j2ee.ejbverification.rules;

import com.sun.source.tree.Tree;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbverification.EJBAPIAnnotations;
import org.netbeans.modules.j2ee.ejbverification.EJBProblemContext;
import org.netbeans.modules.j2ee.ejbverification.HintsUtils;
import org.netbeans.modules.j2ee.ejbverification.JavaUtils;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

/**
 * Only stateful session bean(SFSB) can implement SessionSynchronization i/f.
 *
 * @author Sanjeeb.Sahoo@Sun.COM, Tomasz.Slota@Sun.COM, Martin Fousek <marfous@netbeans.org>
 */
@Hint(displayName = "#SessionSynchImplementedBySFSBOnly.display.name",
        description = "#SessionSynchImplementedBySFSBOnly.err",
        id = "o.n.m.j2ee.ejbverification.SessionSynchImplementedBySFSBOnly",
        category = "javaee/ejb",
        enabled = true,
        suppressWarnings = "SessionSynchImplementedBySFSBOnly")
@NbBundle.Messages({
    "SessionSynchImplementedBySFSBOnly.display.name=SessionSynchronization implemted by non-SFSB",
    "SessionSynchImplementedBySFSBOnly.err=Only stateful session bean (SFSB) can implement SessionSynchronization interface."
})
public final class SessionSynchImplementedBySFSBOnly {

    private SessionSynchImplementedBySFSBOnly() {
    }

    @TriggerTreeKind(Tree.Kind.CLASS)
    public static Collection<ErrorDescription> run(HintContext hintContext) {
        final List<ErrorDescription> problems = new ArrayList<>();
        final EJBProblemContext ctx = HintsUtils.getOrCacheContext(hintContext);
        if (ctx == null) {
            return problems;
        }

        if (ctx.getEjb() instanceof Session) {
            if (Session.SESSION_TYPE_STATEFUL.equals(ctx.getEjbData().getSessionType())) {
                return problems; // OK, stateful session bean
            }
        }
        for (TypeMirror iface : ctx.getClazz().getInterfaces()) {
            String ifaceName = JavaUtils.extractClassNameFromType(iface);
            if (EJBAPIAnnotations.SESSION_SYNCHRONIZATION.equals(ifaceName)) {
                ErrorDescription err = HintsUtils.createProblem(ctx.getClazz(), hintContext.getInfo(), Bundle.SessionSynchImplementedBySFSBOnly_err());
                problems.add(err);
            }
        }

        return problems;
    }
}
