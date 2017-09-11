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

package org.netbeans.performance.bundle;

import org.netbeans.performance.Benchmark;
import java.util.Properties;

/**
 * Benchmark measuring the difference between using plain Properties
 * vs. Properties that intern either keys of both keys and vaules.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public class PropertiesTest extends Benchmark {

    public PropertiesTest (String name) {
        super (name);
    }

    Properties[] holder;

    protected void setUp () {
        holder = new Properties[getIterationCount ()];
    }

    /** Creates an instance of standard java.util.Properties and feeds it with
     * a stream from a Bundle.properties.file
     */
    public void testOriginalProperties () throws Exception {
        int count = getIterationCount ();

        while (count-- > 0) {
            holder[count] = new Properties ();
            holder[count].load (
                PropertiesTest.class.getResourceAsStream ("Bundle.properties"));
        }
    }

    /** Creates an instance of a special subclass of java.util.Properties
     * which interns keys during properties parsing, then 
     * feeds it with a stream from a Bundle.properties.file
     */
    public void testInternKeys () throws Exception {
        int count = getIterationCount ();
        
        while (count-- > 0) {
            holder[count] = new KeyProperties ();
            holder[count].load (
                PropertiesTest.class.getResourceAsStream ("Bundle.properties"));
        }
    }
    
    /** Creates an instance of a special subclass of java.util.Properties
     * which interns both keys and parsed strings during properties parsing,
     * then feeds it with a stream from a Bundle.properties.file
     */
    public void testInternBoth () throws Exception {
        int count = getIterationCount ();

        while (count-- > 0) {
            holder[count] = new BothProperties ();
            holder[count].load (
                PropertiesTest.class.getResourceAsStream ("Bundle.properties"));
        }
    }


    public static void main (String[] args) {
	simpleRun (PropertiesTest.class);
    }

    private static class KeyProperties extends java.util.Properties {
        public KeyProperties () {
            super ();
        }

        public Object put (Object key, Object value) {
            return super.put (key.toString ().intern (), value);
        }
    }

    private static class BothProperties extends java.util.Properties {
        public BothProperties () {
            super ();
        }

        public Object put (Object key, Object value) {
            return super.put (key.toString ().intern (),
                              value.toString ().intern ());
        }
    }
}
