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

#include <iostream>
#include <list>
#include <cstdlib>

#include "customer.h"
#include "system.h"
#include "disk.h"
#include "cpu.h"
#include "memory.h"

namespace {
    list<Customer> customers;

    void outCustomersList() {
        cout << "Customer list (" << customers.size() << " customer(s)):" << endl;
        cout << "--------------------------------------" << endl;
    
        for(list<Customer>::iterator it = customers.begin(); it != customers.end(); ++it) {
            cout << (*it) << endl;
        }
    
        cout << endl;
    }

    void fetchCustomersList() {
        // Just do some static initialization...
        // But could be fetching this info from database, for instance
        
        customers.push_back(Customer("John", 10));
        customers.push_back(Customer("Mike", 0));
        customers.push_back(Customer("Peter", 13));
        customers.push_back(Customer("Ann", 11));
        customers.push_back(Customer("Tom", 9));
    }

    int getDiscountFor(string name) {
        for(list<Customer>::iterator it = customers.begin(); it != customers.end(); ++it) {
            if ((*it).GetName() == name) {
                return (*it).GetDiscount();
            }
        }
    
        return -1;
    }

    int readNumberOf(const char* item, int min, int max) {
        cout << "Enter number of " << item << " (" << min << " <= N <= " << max << "): ";
    
        string s;
        getline(cin, s);
        int amount = strtol(s.c_str(), 0, 10);
    
        cout << endl;
    
        if (amount < min) {
            cout << "number of " << item << " cannot be less than " << min << '.' << endl;
            cout << min << " item is taken" << endl << endl;
        
            amount = min;
        } else if (amount > max) {
            cout << "number of " << item << " cannot be more than " << max << '.' << endl;
            cout << max << " items is taken" << endl << endl;
        
            amount = max;
        }
    
        return amount;
    }

    char readChar(const char* prompt, char defaultAnswer) {
        cout << prompt << ": [" << (char)toupper(defaultAnswer) << "] ";
    
        string s;
        getline(cin, s);
    
        cout << endl;
    
        return toupper(s[0]);
    }
} // End of local namespace    

int main(int argc, char** argv) {
    cout << "Support metric quote program" << endl << endl;
    
    fetchCustomersList();
    
    int discount = -1;
    string customerName;
    
    do {
        outCustomersList();
        cout << "Enter customer name: ";
        getline(cin, customerName);
        discount = getDiscountFor(customerName);
        
        if (discount == -1) {
            cout << "Cannot get discount value for customer " << customerName << '.' << endl;
            cout << "Please choose customer from list." << endl;
        }
    }
    while (discount == -1);
    
    //Define system collection .. this is the list of modules
    System MySystem;
    cout << "Customer " << customerName << " has discount " << discount << '%' << endl;
    cout << "Now, let's configure system for " << customerName << endl;
    
    char response = readChar("Enter CPU module type (M for middle, E for high; Q - exit)", 'M');
    
    int type = 0;
    
    switch (response) {
        case 'Q':
            return 2; //default user requested termination
            
        case 'E': 
            type = Cpu::HIGH; 
            break;
            
        case 'M':
        default : 
            type = Cpu::MEDIUM;
            break;
    }
    
    int amount = readNumberOf("CPUs", 1, 10);
    
    Cpu MyCpu(type, 0, amount); // Create CPU module object
    
    MySystem.AddModule(&MyCpu); // Add CPU Module to system specification
    
    response = readChar("Enter disk module type: (S for single disks, R for RAID; Q - exit)", 'S');
    
    switch (response) {
        case 'Q': 
            return 2; //premature user requested termination
            
        case 'R': 
            type = Disk::RAID; 
            break;
            
        case 'S':
        default : 
            type = Disk::SINGLE;
            break;
    }
    
    amount = readNumberOf("disks", 1, 10);
    
    Disk MyDisk(type, 0, amount); //Create disk module object
    MySystem.AddModule(&MyDisk); //Add Disk Module to system specification
    
    response = readChar("Enter memory module type: (S for standard, F for fast, U for ultra; Q - exit)", 'S');
    
    switch (response) {
        case 'Q': 
            return 2; //premature user requested termination
            
        case 'F': 
            type = Memory::FAST; 
            break;
            
        case 'U':  
            type = Memory::ULTRA; 
            break;
            
        case 'S':
        default : 
            type = Memory::STANDARD; 
            break;
    }
    
    amount = readNumberOf("sub-modules", 1, 10);
    
    Memory MyMemory(type, 0, amount); //Create memory module object
    MySystem.AddModule(&MyMemory); //Add Memory Module to system specification
    
    // end of system specification
    
    // summarize system specification
    int metric = MySystem.GetSupportMetric();
    cout << MySystem << endl;

    // end of system specification
    cout << "Overall Quote Support Metric for configured system for customer " << customerName << ':' << endl;
    cout << "-----------------------------------------------------------------------" << endl << endl;
    cout << "Quote Discount: " << discount << '%' << endl;
    cout << "Quote Support Metric: " << metric << endl << endl;
    
    return (EXIT_SUCCESS);
}
