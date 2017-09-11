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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.csl.api;

import org.netbeans.api.annotations.common.NonNull;


/**
 * A formatter used to format items for navigation, code completion, etc.
 * Language plugins should build up HTML strings by calling logical
 * methods on this class, and suitable HTML will be constructed (using
 * whatever colors and attributes are appropriate for the different logical 
 * sections and so on). This places formatting logic within the IDE such that
 * it can be theme sensitive (and changed without replicating logic in the plugins).
 *
 * @author Tor Norbye
 */
public abstract class HtmlFormatter {
    protected int textLength;
    protected int maxLength = Integer.MAX_VALUE;
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }
    public abstract void reset();
    public abstract void appendHtml(String html);
    public void appendText(@NonNull String text) {
        appendText(text, 0, text.length());
    }
    public abstract void appendText(@NonNull String text, int fromInclusive, int toExclusive);

    public abstract void emphasis(boolean start);
    public abstract void name(@NonNull ElementKind kind, boolean start);
    public abstract void parameters(boolean start);
    public abstract void active(boolean start);
    public abstract void type(boolean start);
    public abstract void deprecated(boolean start);
    
    public abstract @NonNull String getText();
}
