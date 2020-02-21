// -*- C++ -*-
// File:   cstdlib.h
// Author: as204739
//
// Created on June 13, 2007, 3:58 PM
//

#ifndef _STDLIB_H
#define	_STDLIB_H

#define EXIT_SUCCESS 0

namespace std
{
  extern "C" void abort(void);
  extern "C" int atexit(void (*)());
  extern "C" void exit(int);
} // namespace std

#endif	/* _STDLIB_H */

