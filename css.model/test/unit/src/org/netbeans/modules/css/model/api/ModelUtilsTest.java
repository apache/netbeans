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
package org.netbeans.modules.css.model.api;

import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.web.common.api.LexerUtils;

/**
 *
 * @author marekfukala
 */
public class ModelUtilsTest extends ModelTestBase {

    public ModelUtilsTest(String name) {
        super(name);
    }
    
    public void testFindMatchingMedia() {
        final Model m1 = createModel("@media screen { div {} } @media xxx { div {} } @media print { } ");
        final AtomicReference<Media> mr = new AtomicReference<>();
        ModelVisitor visitor = new ModelVisitor.Adapter() {

            @Override
            public void visitMedia(Media media) {
                CharSequence mql =  LexerUtils.trim(m1.getElementSource(media.getMediaQueryList()));
                if(mql.equals("print")) {
                    mr.set(media);
                }
            }
            
        };
        m1.getStyleSheet().accept(visitor);
        
        Media print = mr.get();
        assertNotNull(print);
        
        final Model m2 = createModel("@media xxx { div {} } .clz {}  @media print { } ");
        m2.getStyleSheet().accept(visitor);
        
        Media print2 = mr.get();
        assertNotNull(print2);
        
        ModelUtils utils = new ModelUtils(m2);
        Media match = utils.findMatchingMedia(m1, print);
        
        assertNotNull(match);
        assertSame(print2, match);
        
    }
}
