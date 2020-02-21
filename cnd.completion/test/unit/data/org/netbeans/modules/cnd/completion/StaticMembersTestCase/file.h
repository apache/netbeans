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

#ifndef _FILE_H_
#define _FILE_H_

class ClassC;

class ClassA {
public:
    int aPub;
    int static aPubSt;
    void aPubFun();
    void static aPubFunSt();
protected:
    int aProt;
    int static aProtSt;
    void aProtFun() {}
    void static aProtFunSt();
private:
    int aPriv;
    int static aPrivSt;
    void aPrivFun() {}
    void static aPrivFunSt();
};
 
class ClassB : private ClassA {
public:
    int bPub;  
    int static bPubSt;
    void bPubFun() {}
    void static bPubFunSt();
protected:
    int bProt;
    int static bProtSt;
    void bProtFun();
    void static bProtFunSt();
private:
    int bPriv;
    int static bPrivSt;
    void bPrivFun() {}
    void static bPrivFunSt();
};

class ClassC {
public:
    int cPub;   
    int static cPubSt;
    void cPubFun() {}
    void static cPubFunSt();
protected:
    int cProt;
    int static cProtSt;
    void cProtFun() {}
    void static cProtFunSt();
private:
    int cPriv;
    int static cPrivSt;
    void cPrivFun();
    void static cPrivFunSt();
};
 
class ClassD : public ClassB, protected ClassC {
public:
    int dPub;
    int static dPubSt;
    void dPubFun();
    void static dPubFunSt();
protected:
    int dProt;
    int static dProtSt;
    void dProtFun() {}
    void static dProtFunSt();
private:
    int dPriv;
    int static dPrivSt;
    void dPrivFun() {}
    void static dPrivFunSt();
};

class ClassE : protected ClassC {
public:
    int ePub;
    int static ePubSt;
    void ePubFun();
    void static ePubFunSt();
protected:
    int eProt;
    int static eProtSt;
    void eProtFun() {}
    void static eProtFunSt();
private:
    int ePriv;
    int static ePrivSt;
    void ePrivFun() {}
    void static ePrivFunSt();
};

#endif
