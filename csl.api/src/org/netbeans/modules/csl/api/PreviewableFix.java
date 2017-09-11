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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.csl.api;

/**
 * This interface is implemented by HintFix implementations that are also "previewable";
 * these fixes can provide their implementation as a list of edit operations that
 * can be used to compute diff views etc.
 * 
 * @todo Don't make this extend fix; perhaps I can use previewable elsewhere as well?
 * 
 * @author Tor Norbye
 */
public interface PreviewableFix extends HintFix {
    /**
     * Return true if this fix can provide a fix.
     * (This isn't determined only by implementing fix, since some fix implementations
     * may be previewable in some cases and not in others. For example, the CamelCaseNames
     * fix can preview name fixes for local variables, but not for global renames.)
     * 
     * @return true iff this fix can generate a preview.
     */
    boolean canPreview();
    
    /** 
     * Return a list of edits which the {@link #implement} method will
     * perform. You can return null (or an empty list) here, but if you
     * provide a list of edits, the IDE will use the edit list to offer
     * a preview of the hint.
     * <p>
     * This method will not be called for interactive hints (hints which
     * return true from {@link #isInteractive}.)
     * 
     * @return an EditList containing edits that can preview
     */
    EditList getEditList() throws Exception;
}
