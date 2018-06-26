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

// New Types:
var obj = {};
var set = new Set();
set.add("one").add("two");
var weakSet = new WeakSet();
//weakSet.add("one").add("two");
var map = new Map();
map.set("oneKey", "oneValue");
map.set("twoKey", "twoValue");
var weakMap = new WeakMap();
//weakMap.set("oneKey", "oneValue");
//weakMap.set("twoKey", "twoValue");
var symbol = Symbol("symbolKey");
var promise = new Promise(function(resolve, reject) { resolve(true); });
var iter = ['a', 'b', 'c'].entries();   //console.log('set = '+set);
var gen = function*() {
    var pre = 0, cur = 1;
    for (;;) {
      var temp = pre;
      pre = cur;
      cur += temp;
      yield cur;
    }
  };
  
var it1 = iter.next();
var it2 = iter.next();
var it3 = iter.next();  //console.log('it1 = '+it1.value+", it2 = "+it2.value+', it3 = '+it3.value);
var it4 = iter.next(); //breakpoint

// Template Strings

const n = 10;
var ts = `The n = ${n}.`;
var ts2 = `Multi
line
String`;
ts2.length;
