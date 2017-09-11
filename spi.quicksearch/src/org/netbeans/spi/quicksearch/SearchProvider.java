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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */


package org.netbeans.spi.quicksearch;

/**
 * Main interface of Quick Search API. Implement this interface
 * to provide new group of results for quick search.
 * 
 * In order to plug into Quick Search UI and show quick search results for your
 * providers, implementations of SearchProvider must be registered through xml
 * layer in following way:
 * 
 * <pre>
 *  &lt;folder name="QuickSearch"&gt;
 *      &lt;folder name="MyCategoryID"&gt;
 *          &lt;!--Attribute for localization - provide localized display name of category!--&gt;
 *          &lt;attr name="SystemFileSystem.localizingBundle" stringvalue="org.netbeans.modules.yourmodule.YourBundle"/>
 *          &lt;!--Attribute for command prefix - used to narrow search to this category only!--&gt;
 *          &lt;attr name="command" stringvalue="p"/>
 *          &lt;!--Attribute for category ordering!--&gt;
 *          &lt;attr name="position" intvalue="200"/&gt;
 *          &lt;!--Note that multiple providers can contribute to one category!--&gt;
 *          &lt;file name="org-netbeans-module2-package2-MySearchProviderImpll.instance"/&gt;
 *          &lt;file name="org-netbeans-module2-package3-MySearchProviderImpl2.instance"/&gt;
 *      &lt;/folder&gt;
 *  &lt;/folder&gt;
 * </pre>
 * 
 * @author  Jan Becicka, Dafe Simonek
 */
public interface SearchProvider {
    
    /**
     * Method is called by infrastructure when search operation was requested.
     * Implementors should evaluate given request and fill response object with appropriate results.
     * 
     * Typical implementation would look like follows:
     * 
     * <pre>
     *  for (SearchedItem item : getAllItemsToSearchIn()) {
     *      if (isConditionSatisfied(item, request)) {
     *          if (!response.addResult(item.getRunnable(), item.getDisplayName(),
     *              item.getShortcut(), item.getDisplayHint())) {
     *               break;
     *          }
     *      }
     *  }
     * </pre>
     * 
     * Threading: This method can be called outside EQ thread by infrastructure.
     * 
     * @param request Search request object that contains information what to
     * search for.
     * @param response Search response object that stores search results. Note
     * that it's important to react to return value of SearchResponse.addResult(...)
     * method and stop computation if false value is returned.
     * 
     */
    public void evaluate (SearchRequest request, SearchResponse response);
    
    
}
