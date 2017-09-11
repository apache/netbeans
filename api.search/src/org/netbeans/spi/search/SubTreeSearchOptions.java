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
package org.netbeans.spi.search;

import java.util.List;
import org.netbeans.api.annotations.common.NonNull;

/**
 * This class defines default search options for a node and its subnodes.
 *
 * It only applies if no {@link SearchInfoDefinition} is found in the lookup of
 * node where search starts.
 *
 * If an instance of this class is found in lookup of a node it will be used for
 * setting default search options.
 *
 * It is mainly useful in project nodes to set which folders and files are
 * skipped (filtered).
 *
 * <div class="nonnormative">
 * <p>Example:</p>
 * <pre>
 * {@code 
 * 
 * public class MyNode extends AbstractNode {
 *   public MyNode() { 
 *     super(Lookups.singleton(new SubTreeSearchOptions() {
 *       public List<SearchFilterDefinition> getFilters() {
 *        // return list of SearchFilterDefinitions objects.
 *       }
 *     }));
 *   }
 *   ...
 * }}</pre>
 * </div>
 * 
 * @author jhavlin
 */
public abstract class SubTreeSearchOptions {

    /**
     * Get list of filters that will be used for searching under a node.
     */
    public abstract @NonNull
    List<SearchFilterDefinition> getFilters();
}
