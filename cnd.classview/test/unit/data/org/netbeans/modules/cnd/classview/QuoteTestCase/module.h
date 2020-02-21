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

#if !defined MODULE_H
#define MODULE_H

//#include <iostream>

using namespace std;

// Base class

class Module {
public:
    Module();
    Module(const char* description, const char* vendor, int type, int category, int units);
    virtual ~Module(); //destructor is virtual since derived classes may have distinct destructor

    Module(const Module& obj); //copy constructor
    Module& operator= (const Module& obj); //overload of assignment operator "="

    void SetDescription(const char* description);
    const char* GetDescription() const;

    void SetVendor(const char* v);
    const char* GetVendor() const;

    void SetType(int type);
    int GetTypeID() const;
    virtual const char* GetType() const = 0;

    void SetCategory(int category);
    int GetCategoryID() const;
    virtual const char* GetCategory() const = 0;

    void SetUnits(int u);
    int GetUnits() const;

    void SetSupportMetric(int m);
    int GetSupportMetric() const;

protected:    
    virtual void ComputeSupportMetric() = 0; //metric is defined in derived classes

 private:
    string  description;
    string  vendor; //this anticipates future functionality
    int     type;
    int     category;
    int     units;
    int     supportMetric; //default value

friend ostream& operator<< (ostream&, const Module&);
};

#endif // MODULE_H
