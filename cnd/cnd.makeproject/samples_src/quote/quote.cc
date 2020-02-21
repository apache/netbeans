/*
 * Copyright (c) 2009-2010, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Oracle nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
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
        if (cin.eof() || cin.fail()) {
            exit(EXIT_FAILURE);
        }
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
        if (cin.eof() || cin.fail()) {
            exit(EXIT_FAILURE);
        }
    
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
        if (cin.eof() || cin.fail()) {
            exit(EXIT_FAILURE);
        }
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
