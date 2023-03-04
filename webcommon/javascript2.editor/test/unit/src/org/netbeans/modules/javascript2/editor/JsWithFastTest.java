/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.javascript2.editor;

/**
 *
 * @author Petr Pisl
 */
public class JsWithFastTest extends JsWithBase {
    
    public JsWithFastTest(String testName) {
        super(testName);
    }
    
    public void testWith_01() throws Exception {
        checkOccurrences("testfiles/with/test01.js", "    console.log(getFirst^Name());", true);
    }
    
    public void testWith_02() throws Exception {
        checkOccurrences("testfiles/with/test01.js", "    console.l^og(getFirstName());", true);
    }
    
    public void testWith_03() throws Exception {
        checkOccurrences("testfiles/with/test01.js", "    conso^le.log(getFirstName());", true);
    }
    
    public void testWith_04() throws Exception {
        checkOccurrences("testfiles/with/test01.js", "with(rom^an) {", true);
    }
    
    public void testMarkOccurrenceWoman_01() throws Exception {
        checkOccurrences("testfiles/with/woman.js", "console.log(martin.address.cit^y);", true);
    }
    
    public void testMarkOccurrenceWoman_02() throws Exception {
        checkOccurrences("testfiles/with/woman.js", "console.log(martin.addre^ss.city);", true);
    }
    
    public void testMarkOccurrenceWoman_03() throws Exception {
        checkOccurrences("testfiles/with/woman.js", "console.log(mar^tin.address.city);", true);
    }
    
    public void testMarkOccurrenceWoman_04() throws Exception {
        checkOccurrences("testfiles/with/woman.js", "console.lo^g(martin.address.city);", true);
    }
    
    public void testMarkOccurrenceWoman_05() throws Exception {
        checkOccurrences("testfiles/with/woman.js", "conso^le.log(martin.address.city);", true);
    }
    
    public void testIssue232804() throws Exception {
        checkSemantic("testfiles/markoccurences/issue232804.js"); 
    }
    
    public void testIssue232804_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue232804.js","with (tes^tWith02) {", true);
    }
    
    public void testIssue232804_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue232804.js","console.log(app.desc^ription);", true);
    }
    
    public void testInner01_01() throws Exception {
        checkOccurrences("testfiles/with/inner01.js","console.log(st^reet);", true);
    }
    
    public void testInner01_02() throws Exception {
        checkOccurrences("testfiles/with/inner01.js","console.lo^g(street);", true);
    }
    
    public void testInner01_03() throws Exception {
        checkOccurrences("testfiles/with/inner01.js","con^sole.log(street);", true);
    }
    
    public void testInner01_04() throws Exception {
        checkOccurrences("testfiles/with/inner01.js","with(addres^s01) {", true);
    }
    
    public void testInner01_05() throws Exception {
        checkOccurrences("testfiles/with/inner01.js","with (m^an01) {", true);
    }
    
    public void testInner01_06() throws Exception {
        checkOccurrences("testfiles/with/inner01.js","console.log(firstN^ame);", true);
    }
    
    public void testInner01() throws Exception {
        checkSemantic("testfiles/with/inner01.js");
        checkStructure("testfiles/with/inner01.js");
    }
    
    public void testInner01_Structure() throws Exception {
        checkSemantic("testfiles/with/inner01.js"); 
    }
    
    public void testInner02_01() throws Exception {
        checkOccurrences("testfiles/with/inner02.js", "console.log(str^eet);", true);
    }
    
    public void testInner02_02() throws Exception {
        checkOccurrences("testfiles/with/inner02.js","console.lo^g(street);", true);
    }
    
    public void testInner02_03() throws Exception {
        checkOccurrences("testfiles/with/inner02.js","con^sole.log(street);", true);
    }
    
    public void testInner02_04() throws Exception {
        checkOccurrences("testfiles/with/inner02.js","with (addr^ess) {", true);
    }
    
    public void testInner02_05() throws Exception {
        checkOccurrences("testfiles/with/inner02.js","with (m^an02) {", true);
    }
    
    public void testInner02_06() throws Exception {
        checkOccurrences("testfiles/with/inner02.js","console.log(firstN^ame);", true);
    }
    
    public void testInner02() throws Exception {
        checkSemantic("testfiles/with/inner02.js"); 
        checkStructure("testfiles/with/inner02.js");
    }
    
    public void testInner03() throws Exception {
        checkSemantic("testfiles/with/inner03.js"); 
        checkStructure("testfiles/with/inner03.js");
    }
    
    public void testInner03_01() throws Exception {
        checkOccurrences("testfiles/with/inner03.js", "console.log(stre^et);", true);
    }
    
    public void testInner03_02() throws Exception {
        checkOccurrences("testfiles/with/inner03.js", "console.lo^g(street);", true);
    }
    
    public void testInner03_03() throws Exception {
        checkOccurrences("testfiles/with/inner03.js", "cons^ole.log(street);", true);
    }
    
    public void testInner03_04() throws Exception {
        checkOccurrences("testfiles/with/inner03.js", "with(addre^ss){ // from house", true);
    }
    
    public void testInner03_05() throws Exception {
        checkOccurrences("testfiles/with/inner03.js", "console.log(sta^te);", true);
    }
    
    public void testInner03_06() throws Exception {
        checkOccurrences("testfiles/with/inner03.js", "with(ad^dress) { // from ondra", true);
    }
    
    public void testInner03_07() throws Exception {
        checkOccurrences("testfiles/with/inner03.js", "with(ond^ra) { // second", true);
    }
    
    public void testInner03_08() throws Exception {
        checkOccurrences("testfiles/with/inner03.js", "with (hou^se) {", true);
    }
    
    public void testVarInWith_01() throws Exception {
        checkSemantic("testfiles/with/varInWith01.js"); 
        checkStructure("testfiles/with/varInWith01.js");
    }
    
    public void testVarInWith_02() throws Exception {
        checkOccurrences("testfiles/with/varInWith01.js", "var myDataVarInWith = truhl^ik;", true); 
    }
    
    public void testVarInWith_03() throws Exception {
        checkOccurrences("testfiles/with/varInWith01.js", "with (MyContext.ok^no) {", true); 
    }
    
    public void testVarInWith_04() throws Exception {
        checkOccurrences("testfiles/with/varInWith01.js", "with (MyCont^ext.okno) {", true); 
    }
    
    public void testVarInWith_05() throws Exception {
        checkOccurrences("testfiles/with/varInWith01.js", "console.log(myDataVarI^nWith);", true); 
    }
    
    public void testVarInWith_06() throws Exception {
        checkCompletion("testfiles/with/varInWith01.js", "console.log(myDataVarInWith.^kolik);", true); 
    }
    
    public void testIssue232776_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue232776.js","p^rop01 = prop01 + prop02;", true);
    }
    
    public void testIssue232776_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue232776.js","prop01 = prop01 + pro^p02;", true);
    }
    
    public void testIssue232776_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue232776.js","metho^d01();", true);
    }
    
    public void testIssue232776_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue232776.js","with (testWi^th01) {", true);
    }
    
    public void testIssue232777_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue232777.js","app.des^cription = \"new description\";", true);
    }
    
    public void testIssue232777_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue232777.js","ap^p.description = \"new description\";", true);
    }
    
    public void testIssue232792_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue232792.js","getI^nfo();", true);
    }
    
    public void testIssue232792_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue232792.js","A.getN^ame();", true);
    }
    
    public void testIssue232792_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue232792.js","A.B.getN^ame();", true);
    }
    
    public void testIssue234390() throws Exception {
        checkSemantic("testfiles/with/issue234390.js"); 
        checkStructure("testfiles/with/issue234390.js");
    }
    
    public void testIssue234390_01() throws Exception {
        checkOccurrences("testfiles/with/issue234390.js","pe234390.hel^lo();", true);
    }
    
    public void testIssue234390_02() throws Exception {
        checkOccurrences("testfiles/with/issue234390.js","pe2343^90.hello();", true);
    }
    
    public void testIssue234390_03() throws Exception {
        checkOccurrences("testfiles/with/issue234390.js","this.real^name = name;", true);
    }
    
    public void testIssue234400_01() throws Exception {
        checkCompletion("testfiles/with/issue234400.js","o234400.^name = 10;", true);
    }
    
    public void testIssue234381() throws Exception {
        checkSemantic("testfiles/with/issue234381.js"); 
        checkStructure("testfiles/with/issue234381.js");
    }
    
    public void testIssue234381_01() throws Exception {
        checkOccurrences("testfiles/with/issue234381.js","var z = test() * ti^mes;", true);
    }
    
    public void testIssue234381_02() throws Exception {
        checkOccurrences("testfiles/with/issue234381.js","var z = te^st() * times;", true);
    }
    
    public void testIssue234381_03() throws Exception {
        checkCompletion("testfiles/with/issue234381.js","var z = ^test() * times;", true);
    }
    
    public void testIssue234468_01() throws Exception {
        checkDeclaration("testfiles/with/issue234381.js", "var z = test() * time^s;", "this.^times = 3;");
    }
    
    public void testIssue234380() throws Exception {
        checkSemantic("testfiles/with/issue234380.js"); 
        checkStructure("testfiles/with/issue234380.js");
    }
    
    public void testIssue234380_01() throws Exception {
        checkOccurrences("testfiles/with/issue234380.js","with (this.de^n) {", true);
    }
    
    public void testIssue234375() throws Exception {
        checkSemantic("testfiles/with/issue234375.js"); 
        checkStructure("testfiles/with/issue234375.js");
    }
    
    public void testIssue234375_01() throws Exception {
        checkOccurrences("testfiles/with/issue234375.js","console.log(getD^ate());", true);
    }
    
    public void testIssue234375_02() throws Exception {
        checkOccurrences("testfiles/with/issue234375.js","console.log(r^un());", true);
    }
    
    public void testIssue234479_01() throws Exception {
        checkOccurrences("testfiles/with/issue234375.js","console.lo^g(getDate());", true);
    }
    
    public void testIssue234479_02() throws Exception {
        checkOccurrences("testfiles/with/issue234375.js","conso^le.log(getDate());", true);
    }
    
    public void testIssue234373_01() throws Exception {
        checkOccurrences("testfiles/with/issue234373.js","pl^ay(); //Here", true);
    }
    
    public void testIssue234373_02() throws Exception {
        checkCompletion("testfiles/with/issue234373.js","^play(); //Here", true);
    }
    
    public void testIssue235227_01() throws Exception {
        checkOccurrences("testfiles/with/issue235227.js","with (this.mod^al.innerObject) {", true);
    }
    
    public void testIssue235227_02() throws Exception {
        checkOccurrences("testfiles/with/issue235227.js","with (this.modal.inner^Object) {", true);
    }
    
    public void testIssue235227_03() throws Exception {
        checkOccurrences("testfiles/with/issue235227.js","sh^ow();", true);
    }
    
    public void testIssue235227_04() throws Exception {
        checkOccurrences("testfiles/with/issue235227.js","inn^erName = 3;", true);
    }
    
    public void testIssue241640_01() throws Exception {
        checkOccurrences("testfiles/with/issue241640.js","da^te.getYear();", true);
    }
}
