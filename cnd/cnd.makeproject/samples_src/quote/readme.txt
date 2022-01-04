Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

// This sample program is named quote. It models a simple hardware support service. The program
// runs in a console window. It is written in standard C++. For the user interface the
// program uses C++ standard stream IO and is portable across Windows XP, Linux
// and Solaris.
// 
// The user is prompted to identify the customer and the system for which a support quote
// is required. The system can consist of CPU, Disk and Memory modules. The individual
// modules types can have multiple physical units. For example, the Disk module can
// consist of 1 to 10 standard or raid disks. The quote program computes an ad-hoc
// complexity metric for each module, and from these the program computes an ad-hoc
// support metric for the system. The quote is tailored for the customer through an ad-hoc
// discount code.
//
// The program is structured in three parts: (1) system, (2) customer, and
// (3) user interface.

// The system consists of 5 classes:
//	Module: Base class
//	CPU: derived from Module
//	Disk: derived from Module
//	Memory: derived from Module
//	System: collection of Modules (implememted using vector from the STL)

//The customer consists of 2 classes
//	NameList: collection of known customers (implemented as a singleton class)
//	Customer

//The user interface consists of a sequence of cout and cin statements, that serve to
// prompt the user for the customer name and the system description. The user interface
// is in the main function. The user may choose to identify the customer at the time the
// quote program is launched, through a command line parameter.


