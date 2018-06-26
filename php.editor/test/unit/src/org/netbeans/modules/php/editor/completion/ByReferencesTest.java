/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.completion;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class ByReferencesTest extends PHPCodeCompletionTestBase {

    public ByReferencesTest(String testName) {
        super(testName);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/byReferences/"))
            })
        );
    }

    public void testByReferences01() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort1(^Collection $data) {", false);
    }

    public void testByReferences02() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort1(Colle^ction $data) {", false);
    }

    public void testByReferences03() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort2(^array &$data) {", false);
    }

    public void testByReferences04() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort2(arr^ay &$data) {", false);
    }

    public void testByReferences05() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function my_sort3(^MyClass &$data) {", false);
    }

    public void testByReferences06() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function my_sort3(MyC^lass &$data) {", false);
    }

    public void testByReferences07() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort4(^$data) {", false);
    }

    public void testByReferences08() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort5(^&$data) {", false);
    }

    public void testByReferences09() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort5(&^$data) {", false);
    }

    public void testByReferences10() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function my_sort6(^&$data) {", false);
    }

    public void testByReferences11() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function my_sort6(&^$data) {", false);
    }

    public void testByReferences12() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort7(^array &$data1, Collection &$data2) {", false);
    }

    public void testByReferences13() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort7(ar^ray &$data1, Collection &$data2) {", false);
    }

    public void testByReferences14() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort7(array &$data1, ^Collection &$data2) {", false);
    }

    public void testByReferences15() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort7(array &$data1, Coll^ection &$data2) {", false);
    }

    public void testByReferences16() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function my_sort8(^array &$data1, Collection &$data2) {", false);
    }

    public void testByReferences17() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function my_sort8(arr^ay &$data1, Collection &$data2) {", false);
    }

    public void testByReferences18() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function my_sort8(array &$data1, ^Collection &$data2) {", false);
    }

    public void testByReferences19() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function my_sort8(array &$data1, Coll^ection &$data2) {", false);
    }

    public void testByReferences20() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort9(^array $data1, Collection &$data2) {", false);
    }

    public void testByReferences21() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort9(arr^ay $data1, Collection &$data2) {", false);
    }

    public void testByReferences22() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort9(array $data1, ^Collection &$data2) {", false);
    }

    public void testByReferences23() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort9(array $data1, Coll^ection &$data2) {", false);
    }

    public void testByReferences24() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort10(^array &$data1, Collection $data2) {", false);
    }

    public void testByReferences25() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort10(arr^ay &$data1, Collection $data2) {", false);
    }

    public void testByReferences26() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort10(array &$data1, ^Collection $data2) {", false);
    }

    public void testByReferences27() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort10(array &$data1, Colle^ction $data2) {", false);
    }

    public void testByReferences28() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function my_sort11(^array $data1, Collection &$data2) {", false);
    }

    public void testByReferences29() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function my_sort11(arr^ay $data1, Collection &$data2) {", false);
    }

    public void testByReferences30() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function my_sort11(array $data1, ^Collection &$data2) {", false);
    }

    public void testByReferences31() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function my_sort11(array $data1, Collec^tion &$data2) {", false);
    }

    public void testByReferences32() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort12(^array $data1, Collection $data2) {", false);
    }

    public void testByReferences33() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort12(arr^ay $data1, Collection $data2) {", false);
    }

    public void testByReferences34() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort12(array $data1, ^Collection $data2) {", false);
    }

    public void testByReferences35() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort12(array $data1, Colle^ction $data2) {", false);
    }

    public void testByReferences36() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort13(array &$data1, ^Collection &...$data2) {", false);
    }

    public void testByReferences37() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort13(array &$data1, Coll^ection &...$data2) {", false);
    }

}
