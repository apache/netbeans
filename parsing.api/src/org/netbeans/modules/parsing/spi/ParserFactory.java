/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.parsing.spi;

import java.util.Collection;
import org.netbeans.modules.parsing.api.Snapshot;


/**
 * The factory for {@link Parser}s. <code>ParserFactory</code> implemementations
 * can be registered in <code>MimeLoolup</code> either for a specific
 * mime type or for all languages. The example below shows <code>ParserFactory</code>
 * registration for <code>text/x-something</code> mime type in an XML layer.
 *
 * <pre>
 * &lt;folder name="Editors"&gt;
 *  &lt;folder name="text"&gt;
 *   &lt;folder name="x-something"&gt;
 *    &lt;file name="org-some-module-MyParserFactory.instance" /&gt;
 *   &lt;/folder&gt;
 *  &lt;/folder&gt;
 * &lt;/folder&gt;
 * </pre>
 * 
 * @author Jan Jancura
 */
public abstract class ParserFactory {

    /**
     * Creates a new instance of {@link Parser}. In general parsers are created
     * for either one or several snapshots (ie. document sections), which are provided
     * to this method. It is guaranteed that all snapshots in the collection will be
     * of the same mime type and it will be the mime type, which this factory
     * was registered for (ie. in <code>MimeLookup</code>). Typical factories won't
     * need the snapshots for creating a parser, but factories that serve multiple
     * languages (eg. in GSF or other language support frameworks) may find this useful.
     *
     * <p>It is important to remember that the snapshots are provided <b>only</b>
     * for the factory and they reflect document states at the time when the factory
     * is called. Since the parsing infrastructure may cache parser instances it
     * will provide a new snapshot when it asks <code>Parser</code>s to do their job.
     * Therefore the snapshots here may only be used for the parser creation, but must
     * <b>never</b> be used for parsing!
     * 
     * @param snapshots Snaphots of documents, which the new parser will be asked
     *   to parse.
     *
     * @return The new <code>Parser</code> instance or <code>null</code> if this
     *   factory does not have a parser suitable for parsing this type of snapshots.
     */
    public abstract Parser createParser(Collection<Snapshot> snapshots);
}
