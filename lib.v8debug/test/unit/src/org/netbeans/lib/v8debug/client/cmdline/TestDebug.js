/* 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

// When modified, adjust the V8DebugTest.

var glob_n = 100;

var glob_m = glob_n % 27;

glob_m = Math.sin(glob_m) + Math.cos(glob_n);

(function () {
    vars();
    longStack(20);
    arrays([0, 1, 2, 3]);
    objects();
    var cl = closures();
    cl.getValue();
    scope();
    testThis();
    testBreakpoints();
    testReferences();
    testExceptionBreakpoints();
})();

function vars() {
    var int = -2;
    var double = -2.3;
    var dnan = Math.acos(2); // NaN
    var dinf = Math.exp(1000000);
    var dninf = Math.log(0);
    var boolean = true;
    var s1 = "";
    var s2 = "abc";
    var s3 = 's';
    var s4 = '\u0011';
    var f1 = function(){};
    var f2 = function myF2(){ return true; };
    var undef;
    var nul = null;
    var obj = {};
    s1.length;          // breakpoint
    f1();
}

function longStack(n) {
    var local_i = 10;
    if (n > 0) {
        longStack(n-1);
    } else {
        //process.stdout.write("Have a long stack here.");
        glob_n += n;
    }
}

function arrays(arg_a) {
    var empty = [];
    var months = ['Jan', "Feb", 'Mar'];
    var d2 = [[], [], [[], []]];
    var d3 = [1, 2, 3, 1];
    d3["a"] = "b";
    d3[55] = null;
    d3[10000000000] = Infinity;
    d3[1e13] = -Infinity;
    d3[1.5] = 5.1;
    d3[-2] = -3;
    console.log(d3);
    var d4 = [];
    d4[-1] = -Infinity;
    months.length;      // breakpoint
}

function objects() {
    var o1 = {};
    o1[0] = -1;
    //o1["0"] = -2;
    var o2 = { a : 'b' };
    var o3 = { "o" : o2 };
    var str = "This is a string";
    var o4 = { 0 : {},
               1 : 1,
               "1" : 11,
               ab : {
                   vol : "bbb",
                   year : 2014,
                   fnc : function (i) { return i+1; }
               },
               null : 0,
               true : false,
               3.3333333 : "three and third",
               500 : "five hundred",
               undefined : {},
               NaN : [ NaN ],
               Infinity : [],
               12e3 : 12e-3,
               '' : "Empty"
            };
    // console.log(o4);
    var o5 = null;
    o4.ab.vol;          // breakpoint
}

function closures() {
    var private = 'p';
    
    return {
        append : function(ap) {
            private = private + ap;
        },
        getValue : function () {
            return private;
        }
    };
}

function scope() {
    
    var a = 1, aa = 0, b = 2, c = 3;
    
    function inner() {
        var b = 20, c = 30, d = 40;
        a = a + b;
    }
    
    b += a;
    inner();            // breakpoint
    c += a;
}

function testThis() {
    var t = this;
    var obj = { fnc : function() {
            var ft = this;
            return ft;
    } };
    var fo = obj.fnc();
    return fo === obj;
}

function testBreakpoints() {
    var n = 1000;
    var i;
    var sum = 0;
    for (i = 0; i <= n; i++) {
        sum += i;       // count and conditional breakpoint
    }
    if (sum === n*(n+1)/2) {
        return true;
    } else {
        return false;
    }
}

function testReferences() {
    var r1 = { ref : {} };
    var r2 = r1.ref;
    var r3 = function (r1) {
        var rf1 = [];
        rf1[5] = r1;
        return rf1;
    }();
    var r4 = function refFunc(ref) {
        console.log(r2);
    };
    function Person(name, age, sex) {
        this.name = name;
        this.age = age;
        this.sex = sex;
    }
    var john = new Person("John", 30, 'm');
    var sandra = new Person("Sandra", 29, 'f');
    r4();               // breakpoint
}

function testExceptionBreakpoints() {
    try {
        throw "Test throw.";
    } catch (exc) {
        console.log(exc);
    }
    try {
        throw 1.5;
    } catch (exc) {
        console.log(exc);
    }
    try {
        throw false;
    } catch (exc) {
        console.log(exc);
    }
    try {
        var a = 10;
        a.getOwnPropertyName("oops");
    } catch (exc) {
        console.log(exc);
    }
    try {
        throw new Error("Test Error.");
    } catch (err) {
        console.log(err);
    }
    throw "exiting...";
}
