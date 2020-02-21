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

namespace bug258143 {
  void printf(const char *);
  
  void foo(bool a) {
    printf("bool!\n");
  }

  void foo(short a) {
    printf("short!\n");
  }

  void foo(unsigned short a) {
    printf("short!\n");
  }

  void foo(int a) {
    printf("int!\n");
  }

  void foo(unsigned int a) {
    printf("unsigned int!\n");
  }

  void foo(long a) {
    printf("long!\n");
  }

  void foo(unsigned long a) {
    printf("unsigned long!\n");
  }

  float foo(float a) {
    printf("float!\n");
    return a;
  }

  void foo(double a) {
    printf("double!\n");
  }

  void foo(long double a) {
    printf("long double!\n");
  }

  namespace bla {
    float f;
  }

  int main258143() {
    static bool b = true;
    static char c = 0;
    static unsigned char uc = 0;
    static short s = 0;
    static unsigned short us = 0;
    static int i = 0;
    static unsigned int ui = 0;
    static long l = 0;
    static unsigned long ul = 0;
    static float f = 0;
    static double d = 0;
    static long double ld = 0;

    struct AAA {
      int x;

      int boo() {
        foo(bla::f * i); // float
        return 10;
      }
    };

    foo(c && c); // bool
    foo(c || s); // bool
    foo(ld && i); // bool
    foo(ld / ul); // long double
    foo(d - l); // double
    foo(bla::f * i); // float
    foo(f * i);  // float
    foo(s + ul); // unsigned long
    foo(l + ui); // long (unsigned long???)
    foo(i + l); // long
    foo(ui + i); // unsigned int
    foo(s + i); // int
    foo(c & s); // int
    foo(uc * us); // int  
    return 0;
  }

  int a = 1;
  float z = foo(bla::f * a); 
}