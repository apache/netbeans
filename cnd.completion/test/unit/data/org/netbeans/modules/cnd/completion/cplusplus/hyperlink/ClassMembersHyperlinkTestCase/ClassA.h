/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

typedef int myInt;
class ClassA {
public:
    virtual ~ClassA(); // in test testDestructors
    
public:
    ClassA(); // in test testConstructors

    void publicFoo(); // in test testPublicMethods
    void publicFoo(int a); // in test testPublicMethods
    void publicFoo(int a, double b); // in test testPublicMethods
    void publicFoo(ClassA a); // !!!FAILED!!!
    void publicFoo(const ClassA &a); // !!!FAILED!!!
    
    static void publicFooSt(); // in test testPublicMethods
    
protected:
    ClassA(int a); // in test testConstructors
    
    void protectedFoo(); // in test testProtectedMethods
    void protectedFoo(int a); // in test testProtectedMethods
    void protectedFoo(int a, double b); // in test testProtectedMethods
    void protectedFoo(const ClassA* const ar[]);    // !!!FAILED!!!
    
    static void protectedFooSt(); // in test testProtectedMethods
private:
    ClassA(int a, double b); // in test testConstructors
    void privateFoo(); // in test testPrivateMethods
    void privateFoo(int a); // in test testPrivateMethods
    void privateFoo(int a, double b); // in test testPrivateMethods
    void privateFoo(const ClassA *a); // in test testPrivateMethods
    
    static void privateFooSt(); // in test testPrivateMethods
// members
public:
    int publicMemberInt;
    double publicMemberDbl;
    static int publicMemberStInt;
    
protected:
    int protectedMemberInt;
    double protectedMemberDbl;
    static int protectedMemberStInt;
    
private:
    int privateMemberInt;
    double privateMemberDbl;
    static int privateMemberStInt;
    
//operators
public:
    ClassA& operator= (const ClassA& obj); // in test testOperators
protected:
    ClassA& operator+ (const ClassA& obj); // in test testOperators
private:
    ClassA& operator- (const ClassA& obj); // in test testOperators
    
private:
    ClassA* classMethodRetClassAPtr();
    const ClassA& classMethodRetClassARef();
    
    typedef int myInnerInt;

    myInt classMethodRetMyInt();
    
    myInnerInt classMethodRetMyInnerInt();
    
private:
    friend ostream& operator<< (ostream&, const ClassA&);

public:
    friend void friendFoo();
};


