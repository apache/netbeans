/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.api.io;

import org.netbeans.api.intent.Intent;
import org.netbeans.modules.io.HyperlinkAccessor;
import org.netbeans.spi.io.support.HyperlinkType;

/**
 * Implementation of accessor that enables retrieving information about
 * hyperlinks in SPI.
 *
 * @author jhavlin
 */
class HyperlinkAccessorImpl extends HyperlinkAccessor {

    @Override
    public HyperlinkType getType(Hyperlink hyperlink) {
        if (hyperlink instanceof Hyperlink.OnClickHyperlink) {
            return HyperlinkType.FROM_RUNNABLE;
        } else if (hyperlink instanceof Hyperlink.IntentHyperlink) {
            return HyperlinkType.FROM_INTENT;
        } else {
            throw new IllegalArgumentException("Unknown hyperlink.");   //NOI18N
        }
    }

    @Override
    public boolean isImportant(Hyperlink hyperlink) {
        return hyperlink.isImportant();
    }

    @Override
    public Runnable getRunnable(Hyperlink hyperlink) {
        if (hyperlink instanceof Hyperlink.OnClickHyperlink) {
            return ((Hyperlink.OnClickHyperlink) hyperlink).getRunnable();
        } else {
            throw new IllegalArgumentException(
                    "Not an FROM_RUNNABLE link.");                      //NOI18N
        }
    }

    @Override
    public Intent getIntent(Hyperlink hyperlink) {
        if (hyperlink instanceof Hyperlink.IntentHyperlink) {
            return ((Hyperlink.IntentHyperlink) hyperlink).getIntent();
        } else {
            throw new IllegalArgumentException(
                    "Not a FROM_INTENT link");                          //NOI18N
        }
    }
}
