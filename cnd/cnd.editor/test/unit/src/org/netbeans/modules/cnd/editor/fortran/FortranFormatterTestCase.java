/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.editor.fortran;

import org.netbeans.modules.cnd.editor.fortran.options.FortranCodeStyle;

/**
 *
 */
public class FortranFormatterTestCase extends FortranEditorBase {

    public FortranFormatterTestCase(String testMethodName) {
        super(testMethodName);
    }

    @Override
    protected void assertDocumentText(String msg, String expectedText) {
        super.assertDocumentText(msg, expectedText);
        reformat();
        super.assertDocumentText(msg+" (not stable)", expectedText);
    }

    public void testProgramFormat() {
        setLoadDocumentText(
                "  program   p\n"+
                "  i = 6\n"+
                " end  program\n"
                );
        setDefaultsOptions(true);
        reformat();
        assertDocumentText("Incorrect program reformat",
                "program p\n"+
                "    i = 6\n"+
                "end program\n"
                );
    }

    public void testIfFormat() {
        setLoadDocumentText(
                "subroutine  p\n"+
                "  if (i .eq. 6) then\n"+
                "  i =5\n"+
                "  else\n"+
                "  i=8\n"+
                "  endif\n"+
                " end  subroutine\n"
                );
        setDefaultsOptions(true);
        reformat();
        assertDocumentText("Incorrect program reformat",
                "subroutine p\n"+
                "    if (i .eq. 6) then\n"+
                "        i = 5\n"+
                "    else\n"+
                "        i = 8\n"+
                "    endif\n"+
                "end subroutine\n"
                );
    }

    public void testEleIfFormat() {
        setLoadDocumentText(
                "subroutine  p\n"+
                "  if (i .eq. 6) then \n"+
                "  i =5\n"+
                "  elseif (i.eq.9) then \n"+
                "  i=8\n"+
                "  else\n"+
                "  i=18\n"+
                "  endif\n"+
                " end  subroutine\n"
                );
        setDefaultsOptions(true);
        reformat();
        assertDocumentText("Incorrect program reformat",
                "subroutine p\n"+
                "    if (i .eq. 6) then\n"+
                "        i = 5\n"+
                "    elseif (i .eq. 9) then\n"+
                "        i = 8\n"+
                "    else\n"+
                "        i = 18\n"+
                "    endif\n"+
                "end subroutine\n"
                );
    }

    public void testEleIfFormat2() {
        setLoadDocumentText(
                "subroutine  p\n"+
                "  if (i .eq. 6) then \n"+
                "  i =5\n"+
                "  else if (i.eq.9) then \n"+
                "  i=8\n"+
                "  else\n"+
                "  i=18\n"+
                "  endif\n"+
                " end  subroutine\n"
                );
        setDefaultsOptions(true);
        reformat();
        assertDocumentText("Incorrect program reformat",
                "subroutine p\n"+
                "    if (i .eq. 6) then\n"+
                "        i = 5\n"+
                "    else if (i .eq. 9) then\n"+
                "        i = 8\n"+
                "    else\n"+
                "        i = 18\n"+
                "    endif\n"+
                "end subroutine\n"
                );
    }

    public void testTypeFormat() {
        setLoadDocumentText(
                "  type   point\n"+
                "  real :: X,Y\n"+
                " end  type  point\n"
                );
        setDefaultsOptions(true);
        reformat();
        assertDocumentText("Incorrect type reformat",
                "type point\n"+
                "    real :: X, Y\n"+
                "end type point\n"
                );
    }

    public void testTypeFormat2() {
        setLoadDocumentText(
                "  type   point\n"+
                "  real :: X,Y\n"+
                " end  type  point\n"+
                "TYPE (point) aPoint\n"+
                "TYPE (point(4)) :: aPoints = point(1,2,3,4)\n"
                );
        setDefaultsOptions(true);
        reformat();
        assertDocumentText("Incorrect type reformat",
                "type point\n"+
                "    real :: X, Y\n"+
                "end type point\n"+
                "TYPE (point) aPoint\n"+
                "TYPE (point(4)) :: aPoints = point(1, 2, 3, 4)\n"
                );
    }

    public void testTypeFormat3() {
        setLoadDocumentText(
                "! definitions\n"+
                "Module DEFINITIONS\n"+
                "  type   point\n"+
                "  PRIVATE\n"+
                "  real :: X,Y\n"+
                "! public interface\n"+
                "  INTEGER, PUBLIC :: spin\n"+
                " CONTAINS\n"+
                "PROCEDURE, PASS :: LENGTH => POINT_LENGTH\n"+
                "  PROCEDURE (OPEN_FILE), DEPEND, PASS(HANDLE) :: OPEN\n"+
                " end  type  point\n"+
                "END Module DEFINITIONS\n"
                );
        setDefaultsOptions(true);
        reformat();
        assertDocumentText("Incorrect type reformat",
                "! definitions\n"+
                "Module DEFINITIONS\n"+
                "    type point\n"+
                "        PRIVATE\n"+
                "        real :: X, Y\n"+
                "        ! public interface\n"+
                "        INTEGER, PUBLIC :: spin\n"+
                "    CONTAINS\n"+
                "        PROCEDURE, PASS :: LENGTH => POINT_LENGTH\n"+
                "        PROCEDURE (OPEN_FILE), DEPEND, PASS(HANDLE) :: OPEN\n"+
                "    end type point\n"+
                "END Module DEFINITIONS\n"
                );
    }

    public void testModule2Free() {
        setLoadDocumentText(
                "  module IF\n" +
                "  integer*8 procedure\n" +
                "  dimension x(10)\n" +
                "  contains\n" +
                "  real*8 function factorial(procedure)\n" +
                "  integer*8 procedure\n" +
                "  factorial=1\n" +
                "  do i=1,procedure\n" +
                "  factorial=factorial*dble(i)\n" +
                "  enddo\n" +
                "  end function\n" +
                "  end module IF\n" +
                "  module common\n" +
                "  real object(3)\n" +
                "  integer coutner\n" +
                "  contains\n" +
                "  integer*8 recursive function fact(n) result(object)\n" +
                "  integer*8 n, k, object\n" +
                "  k=n\n" +
                "  if(n.eq.0) k=1\n" +
                "  if(n.gt.1) k=k*fact(k-1)\n" +
                "  object=k\n" +
                "  end function fact\n" +
                "  end module common\n" +
                "  use IF\n" +
                "  use common\n" +
                "  real*8 module1\n" +
                "  integer*8 int\n" +
                "  int=10\n" +
                "  module1=+fact(int)+factorial(int)\n" +
                "  write(*,*)module1\n" +
                "  end");
        setDefaultsOptions(true);
        reformat();
        assertDocumentText("Incorrect module reformat (free form)",
                "module IF\n" +
                "    integer*8 procedure\n" +
                "    dimension x(10)\n" +
                "contains\n" +
                "    real*8 function factorial(procedure)\n" +
                "        integer*8 procedure\n" +
                "        factorial = 1\n" +
                "        do i = 1, procedure\n" +
                "            factorial = factorial * dble(i)\n" +
                "        enddo\n" +
                "    end function\n" +
                "end module IF\n" +
                "module common\n" +
                "    real object(3)\n" +
                "    integer coutner\n" +
                "contains\n" +
                "    integer*8 recursive function fact(n) result(object)\n" +
                "        integer*8 n, k, object\n" +
                "        k = n\n" +
                "        if (n .eq. 0) k = 1\n" +
                "        if (n .gt. 1) k = k * fact(k - 1)\n" +
                "        object = k\n" +
                "    end function fact\n" +
                "end module common\n" +
                "use IF\n" +
                "use common\n" +
                "real*8 module1\n" +
                "integer*8 int\n" +
                "int = 10\n" +
                "module1 = +fact(int) + factorial(int)\n" +
                "write(*, *) module1\n" +
                "end");
    }

    public void testDoFree() {
        setLoadDocumentText(
                "  PROGRAM TEST\n" +
                "  character  i1ad1(25), i1ad2(7,7), i1ad3(4,4,4)\n" +
                "  character  i1ad2r(5,5), i1ad3r(5,5,5)\n" +
                "  integer    shape2(2), shape3(3)\n" +
                "  character  pad(5)\n" +
                "  integer    order1(1), order2(2), order3(3)\n" +
                "  write(*,*)reshape((/'a','b','c','d','e','f'/), (/2_1,3_1/))\n" +
                "  write(*,*)'=1='\n" +
                "  write(*,*)reshape((/'a','b','c','d','e','f'/), (/2_2,4_2/),  (/'x','y'/))\n" +
                "  write(*,*)'=2='\n" +
                "  write(*,*)reshape((/'a','b','c','d','e','f'/), (/2_8,4_8/),  (/'x','y'/),(/2_1,1_1/))\n" +
                "  write(*,*)'=3='\n" +
                "  write(*,*)reshape((/'a','b','c','d','e','f'/), (/2,3/),  ORDER=(/2,1/))\n" +
                "  write(*,*)'=4='\n" +
                "  write(*,*)reshape(SHAPE=(/2,3/),SOURCE=(/'a','b','c','d','e','f'/)  ,ORDER=(/2,1/))\n" +
                "  write(*,*)'=5='\n" +
                "  write(*,*)reshape(ORDER=(/2,1/),PAD=(/0,0/),SHAPE=(/2,4/), SOURCE=(/'a','b','c','d','e','f'/))\n" +
                "  write(*,*)'=6='\n" +
                "  do i=1,25\n" +
                "  i1ad1(i)=CHAR(i+40)\n" +
                "  enddo\n" +
                "  k=0\n" +
                "  do i=1,7\n" +
                "  do j=1,7\n" +
                "  i1ad2(i,j)=CHAR(k+40)\n" +
                "  k=k+1\n" +
                "  enddo\n" +
                "  enddo\n" +
                "  l=0\n" +
                "  do i=1,4\n" +
                "  do j=1,4\n" +
                "  do k=1,4\n" +
                "  i1ad3(i,j,k)=CHAR(l+40)\n" +
                "  l=l+1\n" +
                "  enddo\n" +
                "  enddo\n" +
                "  enddo\n" +
                "  shape2=5\n" +
                "  shape3=5\n" +
                "  order3(1)=1\n" +
                "  order3(2)=2\n" +
                "  order3(3)=3\n" +
                "  pad='z'\n" +
                "  i1ad2r=reshape(i1ad1,shape2,ORDER=(/2,1/))\n" +
                "  i1ad3r=reshape(i1ad1,shape3,pad,order3)\n" +
                "  do i=1,shape2(2)\n" +
                "  write(*,*)(i1ad2r(i,j),j=1,shape2(1))\n" +
                "  enddo\n" +
                "  write(*,*)'===================='\n" +
                "  do i=1,shape3(1)\n" +
                "  do j=1,shape3(2)\n" +
                "  write(*,*)(i1ad3r(i,j,k),k=1,shape3(3))\n" +
                "  enddo\n" +
                "  enddo\n" +
                "  write(*,*)'===================='\n" +
                "  i1ad3r=reshape(i1ad2,shape3,pad)\n" +
                "  do i=1,shape3(1)\n" +
                "  do j=1,shape3(2)\n" +
                "  write(*,*)(i1ad3r(i,j,k),k=1,shape3(3))\n" +
                "  enddo\n" +
                "  enddo\n" +
                "  write(*,*)'======================'\n" +
                "  end");
        setDefaultsOptions(true);
        reformat();
        assertDocumentText("Incorrect do reformat (free form)",
                "PROGRAM TEST\n" +
                "    character i1ad1(25), i1ad2(7, 7), i1ad3(4, 4, 4)\n" +
                "    character i1ad2r(5, 5), i1ad3r(5, 5, 5)\n" +
                "    integer shape2(2), shape3(3)\n" +
                "    character pad(5)\n" +
                "    integer order1(1), order2(2), order3(3)\n" +
                "    write(*, *) reshape((/'a', 'b', 'c', 'd', 'e', 'f'/), (/2_1, 3_1/))\n" +
                "    write(*, *) '=1='\n" +
                "    write(*, *) reshape((/'a', 'b', 'c', 'd', 'e', 'f'/), (/2_2, 4_2/), (/'x', 'y'/))\n" +
                "    write(*, *) '=2='\n" +
                "    write(*, *) reshape((/'a', 'b', 'c', 'd', 'e', 'f'/), (/2_8, 4_8/), (/'x', 'y'/), (/2_1, 1_1/))\n" +
                "    write(*, *) '=3='\n" +
                "    write(*, *) reshape((/'a', 'b', 'c', 'd', 'e', 'f'/), (/2, 3/), ORDER = (/2, 1/))\n" +
                "    write(*, *) '=4='\n" +
                "    write(*, *) reshape(SHAPE = (/2, 3/), SOURCE = (/'a', 'b', 'c', 'd', 'e', 'f'/), ORDER = (/2, 1/))\n" +
                "    write(*, *) '=5='\n" +
                "    write(*, *) reshape(ORDER = (/2, 1/), PAD = (/0, 0/), SHAPE = (/2, 4/), SOURCE = (/'a', 'b', 'c', 'd', 'e', 'f'/))\n" +
                "    write(*, *) '=6='\n" +
                "    do i = 1, 25\n" +
                "        i1ad1(i) = CHAR(i + 40)\n" +
                "    enddo\n" +
                "    k = 0\n" +
                "    do i = 1, 7\n" +
                "        do j = 1, 7\n" +
                "            i1ad2(i, j) = CHAR(k + 40)\n" +
                "            k = k + 1\n" +
                "        enddo\n" +
                "    enddo\n" +
                "    l = 0\n" +
                "    do i = 1, 4\n" +
                "        do j = 1, 4\n" +
                "            do k = 1, 4\n" +
                "                i1ad3(i, j, k) = CHAR(l + 40)\n" +
                "                l = l + 1\n" +
                "            enddo\n" +
                "        enddo\n" +
                "    enddo\n" +
                "    shape2 = 5\n" +
                "    shape3 = 5\n" +
                "    order3(1) = 1\n" +
                "    order3(2) = 2\n" +
                "    order3(3) = 3\n" +
                "    pad = 'z'\n" +
                "    i1ad2r = reshape(i1ad1, shape2, ORDER = (/2, 1/))\n" +
                "    i1ad3r = reshape(i1ad1, shape3, pad, order3)\n" +
                "    do i = 1, shape2(2)\n" +
                "        write(*, *) (i1ad2r(i, j), j = 1, shape2(1))\n" +
                "    enddo\n" +
                "    write(*, *) '===================='\n" +
                "    do i = 1, shape3(1)\n" +
                "        do j = 1, shape3(2)\n" +
                "            write(*, *) (i1ad3r(i, j, k), k = 1, shape3(3))\n" +
                "        enddo\n" +
                "    enddo\n" +
                "    write(*, *) '===================='\n" +
                "    i1ad3r = reshape(i1ad2, shape3, pad)\n" +
                "    do i = 1, shape3(1)\n" +
                "        do j = 1, shape3(2)\n" +
                "            write(*, *) (i1ad3r(i, j, k), k = 1, shape3(3))\n" +
                "        enddo\n" +
                "    enddo\n" +
                "    write(*, *) '======================'\n" +
                "end");
    }

    public void testMapFree() {
        setLoadDocumentText(
                "  program\n" +
                "  structure /explorer1/\n" +
                "  logical*1:: var\n" +
                "  integer*4 :: i\n" +
                "  end structure\n" +
                "  record /explorer1/ example1\n" +
                "  structure /explorer2/\n" +
                "  union\n" +
                "  map\n" +
                "  logical*1:: var\n" +
                "  end map\n" +
                "  map\n" +
                "  integer*4 :: j\n" +
                "  end map\n" +
                "  end union\n" +
                "  integer*4 :: i\n" +
                "  end structure\n" +
                "  record /explorer2/ example2\n" +
                "  example1.var=.TRUE.\n" +
                "  example1.i=1\n" +
                "  print *, 'Simple structure', loc(example1.i)-loc(example1.var)\n" +
                "  example2.var=.FALSE.\n" +
                "  example2.i=1\n" +
                "  print *, 'Union map - var', loc(example2.i)-loc(example2.var)\n" +
                "  example2.j=1\n" +
                "  example2.i=1\n" +
                "  print *, 'Union map - integer', loc(example2.i)-loc(example2.j)\n" +
                "  end");
        setDefaultsOptions(true);
        reformat();
        assertDocumentText("Incorrect map reformat (free form)",
                "program\n" +
                "    structure /explorer1/\n" +
                "        logical*1 :: var\n" +
                "        integer*4 :: i\n" +
                "    end structure\n" +
                "    record /explorer1/ example1\n" +
                "    structure /explorer2/\n" +
                "        union\n" +
                "            map\n" +
                "                logical*1 :: var\n" +
                "            end map\n" +
                "            map\n" +
                "                integer*4 :: j\n" +
                "            end map\n" +
                "        end union\n" +
                "        integer*4 :: i\n" +
                "    end structure\n" +
                "    record /explorer2/ example2\n" +
                "    example1.var = .TRUE.\n" +
                "    example1.i = 1\n" +
                "    print *, 'Simple structure', loc(example1.i) - loc(example1.var)\n" +
                "    example2.var = .FALSE.\n" +
                "    example2.i = 1\n" +
                "    print *, 'Union map - var', loc(example2.i) - loc(example2.var)\n" +
                "    example2.j = 1\n" +
                "    example2.i = 1\n" +
                "    print *, 'Union map - integer', loc(example2.i) - loc(example2.j)\n" +
                "end");
    }

    public void testPreprocessorFree() {
        setLoadDocumentText(
                " #include \"file\"\n" +
                " #define A\n" +
                " #if defined A\n" +
                " #undef A\n" +
                " print *, \"this block_1 must be NOT in output text\"\n" +
                " #elif 1\n" +
                " print *, \"this block_2 must be in output text\"\n" +
                " print *, \"and this string too\"\n" +
                " #else\n" +
                " print *, \"this block_3 must be NOT in output text\"\n" +
                " #endif\n" +
                " if (1 > 0) then\n" +
                " #if 0\n" +
                " print *, \"this block_1 must be NOT in output text\"\n" +
                " #elif (1 > 5)\n" +
                " print *, \"this block_3 must be NOT in output text\"\n" +
                " #else\n" +
                " print *, \"this block_2 must be in output text\"\n" +
                " #endif\n" +
                " endif\n" +
                " end");
        setDefaultsOptions(true);
        reformat();
        assertDocumentText("Incorrect preprocessor reformat (free form)",
                "#include \"file\"\n" +
                "#define A\n" +
                "#if defined A\n" +
                "#undef A\n" +
                "print *, \"this block_1 must be NOT in output text\"\n" +
                "#elif 1\n" +
                "print *, \"this block_2 must be in output text\"\n" +
                "print *, \"and this string too\"\n" +
                "#else\n" +
                "print *, \"this block_3 must be NOT in output text\"\n" +
                "#endif\n" +
                "if (1 > 0) then\n" +
                "#if 0\n" +
                "    print *, \"this block_1 must be NOT in output text\"\n" +
                "#elif (1 > 5)\n" +
                "    print *, \"this block_3 must be NOT in output text\"\n" +
                "#else\n" +
                "    print *, \"this block_2 must be in output text\"\n" +
                "#endif\n" +
                "endif\n" +
                "end");
    }

    public void testStructureFree() {
        setLoadDocumentText(
                "program\n" +
                "structure /STR1/\n" +
                " integer*4 s1\n" +
                " structure STR2\n" +
                "  character*36 s2\n" +
                "  structure STR3\n" +
                "   character*36 s3\n" +
                "   structure STR4\n" +
                "    real*4 s4\n" +
                "    structure STR5\n" +
                "     real*4 s5\n" +
                "     structure STR6\n" +
                "      complex*16 s6\n" +
                "      structure STR7\n" +
                "       unsigned*4 s7\n" +
                "       structure STR8\n" +
                "        complex*16 s8\n" +
                "        structure STR9\n" +
                "         integer*8 s9\n" +
                "         structure STR10\n" +
                "          character*16 s10 /'Hello, World!'/\n" +
                "         end structure\n" +
                "        end structure\n" +
                "       end structure\n" +
                "      end structure\n" +
                "     end structure\n" +
                "    end structure\n" +
                "   end structure\n" +
                "  end structure\n" +
                " end structure\n" +
                "end structure\n" +
                "structure /OUTSTR/\n" +
                "  real*4 zxc\n" +
                "  record /STR1/ inex\n" +
                "end structure\n" +
                "record /STR1/ ex1\n" +
                "record /OUTSTR/ example\n" +
                "ex1.STR2.STR3.STR4.STR5.STR6.STR7.STR8.STR9.s9=1\n" +
                "ex1.STR2.STR3.STR4.STR5.STR6.STR7.s7=ex1.STR2.STR3.STR4.STR5.STR6.STR7.STR8.STR9.s9\n" +
                "ex1.STR2.s2='1-st text field'\n" +
                "ex1.STR2.STR3.s3='Second field'\n" +
                "example.zxc=123.45\n" +
                "print *,'ex1=',ex1\n" +
                "print *,'Printing of initial values of outer structure: '\n" +
                "print *,example\n" +
                "example.inex.STR2.STR3.STR4.STR5.STR6.STR7.STR8.STR9.s9=1\n" +
                "example.inex.STR2.STR3.STR4.STR5.STR6.STR7.s7=ex1.STR2.STR3.STR4.STR5.STR6.STR7.STR8.STR9.s9\n" +
                "example.inex.STR2.s2='1-st text field of outer structure'\n" +
                "example.inex.STR2.STR3.s3='Second field of outer structure'\n" +
                "print *,'ex1=',ex1\n" +
                "print *,'Printing of result values of outer structure: '\n" +
                "print *,example\n" +
                "end");
        setDefaultsOptions(true);
        reformat();
        assertDocumentText("Incorrect structure reformat (free form)",
                "program\n" +
                "    structure /STR1/\n" +
                "        integer*4 s1\n" +
                "        structure STR2\n" +
                "            character*36 s2\n" +
                "            structure STR3\n" +
                "                character*36 s3\n" +
                "                structure STR4\n" +
                "                    real*4 s4\n" +
                "                    structure STR5\n" +
                "                        real*4 s5\n" +
                "                        structure STR6\n" +
                "                            complex*16 s6\n" +
                "                            structure STR7\n" +
                "                                unsigned*4 s7\n" +
                "                                structure STR8\n" +
                "                                    complex*16 s8\n" +
                "                                    structure STR9\n" +
                "                                        integer*8 s9\n" +
                "                                        structure STR10\n" +
                "                                            character*16 s10 /'Hello, World!'/\n" +
                "                                        end structure\n" +
                "                                    end structure\n" +
                "                                end structure\n" +
                "                            end structure\n" +
                "                        end structure\n" +
                "                    end structure\n" +
                "                end structure\n" +
                "            end structure\n" +
                "        end structure\n" +
                "    end structure\n" +
                "    structure /OUTSTR/\n" +
                "        real*4 zxc\n" +
                "        record /STR1/ inex\n" +
                "    end structure\n" +
                "    record /STR1/ ex1\n" +
                "    record /OUTSTR/ example\n" +
                "    ex1.STR2.STR3.STR4.STR5.STR6.STR7.STR8.STR9.s9 = 1\n" +
                "    ex1.STR2.STR3.STR4.STR5.STR6.STR7.s7 = ex1.STR2.STR3.STR4.STR5.STR6.STR7.STR8.STR9.s9\n" +
                "    ex1.STR2.s2 = '1-st text field'\n" +
                "    ex1.STR2.STR3.s3 = 'Second field'\n" +
                "    example.zxc = 123.45\n" +
                "    print *, 'ex1=', ex1\n" +
                "    print *, 'Printing of initial values of outer structure: '\n" +
                "    print *, example\n" +
                "    example.inex.STR2.STR3.STR4.STR5.STR6.STR7.STR8.STR9.s9 = 1\n" +
                "    example.inex.STR2.STR3.STR4.STR5.STR6.STR7.s7 = ex1.STR2.STR3.STR4.STR5.STR6.STR7.STR8.STR9.s9\n" +
                "    example.inex.STR2.s2 = '1-st text field of outer structure'\n" +
                "    example.inex.STR2.STR3.s3 = 'Second field of outer structure'\n" +
                "    print *, 'ex1=', ex1\n" +
                "    print *, 'Printing of result values of outer structure: '\n" +
                "    print *, example\n" +
                "end");
    }

    public void testModuleInterface() {
        setLoadDocumentText(
                "module QUADRUPLE_PRECISION\n" +
                "interface operator (+)\n" +
                "  module procedure LONGADD\n" +
                "  module procedure QC_ADD\n" +
                "end interface\n" +
                "end module QUADRUPLE_PRECISION"
                );
        setDefaultsOptions(true);
        reformat();
        assertDocumentText("Incorrect function indent (free form)",
                "module QUADRUPLE_PRECISION\n" +
                "    interface operator (+)\n" +
                "        module procedure LONGADD\n" +
                "        module procedure QC_ADD\n" +
                "    end interface\n" +
                "end module QUADRUPLE_PRECISION"
                );
    }
    public void testColonInParen() {
        setLoadDocumentText(
                "do I = 1, 15\n" +
                "  if (STR2(I:I) /= \" \") then\n" +
                "    exit\n" +
                "  end if\n" +
                "end do");
        setDefaultsOptions(true);
        reformat();
        assertDocumentText("Incorrect function indent (fixed form)",
                "do I = 1, 15\n" +
                "    if (STR2(I:I) /= \" \") then\n" +
                "        exit\n" +
                "    end if\n" +
                "end do");
    }

    public void testStatementContinuation() {
        setLoadDocumentText(
                "pure function big_plus_big(x, y) result(bb)\n" +
                "  if (x % digit(nr_of_digits) /= 0 .or. & \n" +
                "  y % digit(nr_of_digits) /= 0) then\n" +
                "  return\n" +
                "end if\n" +
                "end function big_plus_big");
        setDefaultsOptions(true);
        reformat();
        assertDocumentText("Incorrect function indent (fixed form)",
                "pure function big_plus_big(x, y) result(bb)\n" +
                "    if (x % digit(nr_of_digits) /= 0 .or. &\n" +
                "        y % digit(nr_of_digits) /= 0) then\n" +
                "        return\n" +
                "    end if\n" +
                "end function big_plus_big");
    }

    public void testSampleFree() {
        setLoadDocumentText(
                "\t#define N 10\n" +
                "\tSUBROUTINE test\n" +
                "! free comment\n" +
                "\tdo i = 1, N\n" +
                "\tif ( mod(i,2) == 0 ) then\n" +
                "\tprint *, \"even string\"   ! even\n" +
                "\telse\n" +
                "\tprint *, \"odd string\"    ! odd\n" +
                "\tend if\n" +
                "\tend do\n" +
                "\tend\n"
                );
        setDefaultsOptions(true);
        reformat();
        assertDocumentText("Incorrect module reformat (fixed form)",
                "#define N 10\n" +
                "SUBROUTINE test\n" +
                "    ! free comment\n" +
                "    do i = 1, N\n" +
                "        if (mod(i, 2) == 0) then\n" +
                "            print *, \"even string\" ! even\n" +
                "        else\n" +
                "            print *, \"odd string\" ! odd\n" +
                "        end if\n" +
                "    end do\n" +
                "end\n"
                );
    }

    public void testIZ_270651() {
        setLoadDocumentText(
                "interface assignment(=)\n" +
                "  module procedure c_to_s_assign, s_toc_assign\n" +
                "end interface\n" +
                "\n" +
                "interface operator(//)\n" +
                "  module procedure string_concat\n" +
                "end interface\n");
        setDefaultsOptions(true);
        reformat();
        assertDocumentText("Incorrect function indent (free form)",
                "interface assignment( = )\n" +
                "    module procedure c_to_s_assign, s_toc_assign\n" +
                "end interface\n" +
                "\n" +
                "interface operator( // )\n" +
                "    module procedure string_concat\n" +
                "end interface\n");
    }
}
