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
public class FortranFormatterFixedTestCase extends FortranEditorBase {

    public FortranFormatterFixedTestCase(String testMethodName) {
        super(testMethodName);
    }

    @Override
    protected void assertDocumentText(String msg, String expectedText) {
        super.assertDocumentText(msg, expectedText);
        reformat();
        super.assertDocumentText(msg+" (not stable)", expectedText);
    }

    public void testModule2Fixed() {
        setLoadDocumentText(
                "      module IF\n" +
                "      integer*8 procedure\n" +
                "      dimension x(10)\n" +
                "      contains\n" +
                "      real*8 function factorial(procedure)\n" +
                "      integer*8 procedure\n" +
                "      factorial=1\n" +
                "      do i=1,procedure\n" +
                "      factorial=factorial*dble(i)\n" +
                "      enddo\n" +
                "      end function\n" +
                "      end module IF\n" +
                "      module common\n" +
                "      real object(3)\n" +
                "      integer coutner\n" +
                "      contains\n" +
                "      integer*8 recursive function fact(n) result(object)\n" +
                "      integer*8 n, k, object\n" +
                "      k=n\n" +
                "      if(n.eq.0) k=1\n" +
                "      if(n.gt.1) k=k*fact(k-1)\n" +
                "      object=k\n" +
                "      end function fact\n" +
                "      end module common\n" +
                "      use IF\n" +
                "      use common\n" +
                "      real*8 module1\n" +
                "      integer*8 int\n" +
                "      int=10\n" +
                "      module1=+fact(int)+factorial(int)\n" +
                "      write(*,*)module1\n" +
                "      end");
        setDefaultsOptions(false);
        reformat();
        assertDocumentText("Incorrect module reformat (fixed form)",
                "      module IF\n" +
                "          integer*8 procedure\n" +
                "          dimension x(10)\n" +
                "      contains\n" +
                "          real*8 function factorial(procedure)\n" +
                "              integer*8 procedure\n" +
                "              factorial = 1\n" +
                "              do i = 1, procedure\n" +
                "                  factorial = factorial * dble(i)\n" +
                "              enddo\n" +
                "          end function\n" +
                "      end module IF\n" +
                "      module common\n" +
                "          real object(3)\n" +
                "          integer coutner\n" +
                "      contains\n" +
                "          integer*8 recursive function fact(n) result(object)\n" +
                "              integer*8 n, k, object\n" +
                "              k = n\n" +
                "              if (n .eq. 0) k = 1\n" +
                "              if (n .gt. 1) k = k * fact(k - 1)\n" +
                "              object = k\n" +
                "          end function fact\n" +
                "      end module common\n" +
                "      use IF\n" +
                "      use common\n" +
                "      real*8 module1\n" +
                "      integer*8 int\n" +
                "      int = 10\n" +
                "      module1 = +fact(int) + factorial(int)\n" +
                "      write(*, *) module1\n" +
                "      end");
    }

    public void testDoFixed() {
        setLoadDocumentText(
                "      PROGRAM TEST\n" +
                "      character  i1ad1(25), i1ad2(7,7), i1ad3(4,4,4)\n" +
                "      character  i1ad2r(5,5), i1ad3r(5,5,5)\n" +
                "      integer    shape2(2), shape3(3)\n" +
                "      character  pad(5)\n" +
                "      integer    order1(1), order2(2), order3(3)\n" +
                "      write(*,*)reshape((/'a','b','c','d','e','f'/), (/2_1,3_1/))\n" +
                "      write(*,*)'=1='\n" +
                "      write(*,*)reshape((/'a','b','c','d','e','f'/), (/2_2,4_2/),  (/'x','y'/))\n" +
                "      write(*,*)'=2='\n" +
                "      write(*,*)reshape((/'a','b','c','d','e','f'/), (/2_8,4_8/),  (/'x','y'/),(/2_1,1_1/))\n" +
                "      write(*,*)'=3='\n" +
                "      write(*,*)reshape((/'a','b','c','d','e','f'/), (/2,3/),  ORDER=(/2,1/))\n" +
                "      write(*,*)'=4='\n" +
                "      write(*,*)reshape(SHAPE=(/2,3/),SOURCE=(/'a','b','c','d','e','f'/)  ,ORDER=(/2,1/))\n" +
                "      write(*,*)'=5='\n" +
                "      write(*,*)reshape(ORDER=(/2,1/),PAD=(/0,0/),SHAPE=(/2,4/), SOURCE=(/'a','b','c','d','e','f'/))\n" +
                "      write(*,*)'=6='\n" +
                "      do i=1,25\n" +
                "      i1ad1(i)=CHAR(i+40)\n" +
                "      enddo\n" +
                "      k=0\n" +
                "      do i=1,7\n" +
                "      do j=1,7\n" +
                "      i1ad2(i,j)=CHAR(k+40)\n" +
                "      k=k+1\n" +
                "      enddo\n" +
                "      enddo\n" +
                "      l=0\n" +
                "      do i=1,4\n" +
                "      do j=1,4\n" +
                "      do k=1,4\n" +
                "      i1ad3(i,j,k)=CHAR(l+40)\n" +
                "      l=l+1\n" +
                "      enddo\n" +
                "      enddo\n" +
                "      enddo\n" +
                "      shape2=5\n" +
                "      shape3=5\n" +
                "      order3(1)=1\n" +
                "      order3(2)=2\n" +
                "      order3(3)=3\n" +
                "      pad='z'\n" +
                "      i1ad2r=reshape(i1ad1,shape2,ORDER=(/2,1/))\n" +
                "      i1ad3r=reshape(i1ad1,shape3,pad,order3)\n" +
                "      do i=1,shape2(2)\n" +
                "      write(*,*)(i1ad2r(i,j),j=1,shape2(1))\n" +
                "      enddo\n" +
                "      write(*,*)'===================='\n" +
                "      do i=1,shape3(1)\n" +
                "      do j=1,shape3(2)\n" +
                "      write(*,*)(i1ad3r(i,j,k),k=1,shape3(3))\n" +
                "      enddo\n" +
                "      enddo\n" +
                "      write(*,*)'===================='\n" +
                "      i1ad3r=reshape(i1ad2,shape3,pad)\n" +
                "      do i=1,shape3(1)\n" +
                "      do j=1,shape3(2)\n" +
                "      write(*,*)(i1ad3r(i,j,k),k=1,shape3(3))\n" +
                "      enddo\n" +
                "      enddo\n" +
                "      write(*,*)'======================'\n" +
                "      end");
        setDefaultsOptions(false);
        reformat();
        assertDocumentText("Incorrect do reformat (fixed form)",
                "      PROGRAM TEST\n" +
                "          character i1ad1(25), i1ad2(7, 7), i1ad3(4, 4, 4)\n" +
                "          character i1ad2r(5, 5), i1ad3r(5, 5, 5)\n" +
                "          integer shape2(2), shape3(3)\n" +
                "          character pad(5)\n" +
                "          integer order1(1), order2(2), order3(3)\n" +
                "          write(*, *) reshape((/'a', 'b', 'c', 'd', 'e', 'f'/), (/2_1, 3_1/))\n" +
                "          write(*, *) '=1='\n" +
                "          write(*, *) reshape((/'a', 'b', 'c', 'd', 'e', 'f'/), (/2_2, 4_2/), (/'x', 'y'/))\n" +
                "          write(*, *) '=2='\n" +
                "          write(*, *) reshape((/'a', 'b', 'c', 'd', 'e', 'f'/), (/2_8, 4_8/), (/'x', 'y'/), (/2_1, 1_1/))\n" +
                "          write(*, *) '=3='\n" +
                "          write(*, *) reshape((/'a', 'b', 'c', 'd', 'e', 'f'/), (/2, 3/), ORDER = (/2, 1/))\n" +
                "          write(*, *) '=4='\n" +
                "          write(*, *) reshape(SHAPE = (/2, 3/), SOURCE = (/'a', 'b', 'c', 'd', 'e', 'f'/), ORDER = (/2, 1/))\n" +
                "          write(*, *) '=5='\n" +
                "          write(*, *) reshape(ORDER = (/2, 1/), PAD = (/0, 0/), SHAPE = (/2, 4/), SOURCE = (/'a', 'b', 'c', 'd', 'e', 'f'/))\n" +
                "          write(*, *) '=6='\n" +
                "          do i = 1, 25\n" +
                "              i1ad1(i) = CHAR(i + 40)\n" +
                "          enddo\n" +
                "          k = 0\n" +
                "          do i = 1, 7\n" +
                "              do j = 1, 7\n" +
                "                  i1ad2(i, j) = CHAR(k + 40)\n" +
                "                  k = k + 1\n" +
                "              enddo\n" +
                "          enddo\n" +
                "          l = 0\n" +
                "          do i = 1, 4\n" +
                "              do j = 1, 4\n" +
                "                  do k = 1, 4\n" +
                "                      i1ad3(i, j, k) = CHAR(l + 40)\n" +
                "                      l = l + 1\n" +
                "                  enddo\n" +
                "              enddo\n" +
                "          enddo\n" +
                "          shape2 = 5\n" +
                "          shape3 = 5\n" +
                "          order3(1) = 1\n" +
                "          order3(2) = 2\n" +
                "          order3(3) = 3\n" +
                "          pad = 'z'\n" +
                "          i1ad2r = reshape(i1ad1, shape2, ORDER = (/2, 1/))\n" +
                "          i1ad3r = reshape(i1ad1, shape3, pad, order3)\n" +
                "          do i = 1, shape2(2)\n" +
                "              write(*, *) (i1ad2r(i, j), j = 1, shape2(1))\n" +
                "          enddo\n" +
                "          write(*, *) '===================='\n" +
                "          do i = 1, shape3(1)\n" +
                "              do j = 1, shape3(2)\n" +
                "                  write(*, *) (i1ad3r(i, j, k), k = 1, shape3(3))\n" +
                "              enddo\n" +
                "          enddo\n" +
                "          write(*, *) '===================='\n" +
                "          i1ad3r = reshape(i1ad2, shape3, pad)\n" +
                "          do i = 1, shape3(1)\n" +
                "              do j = 1, shape3(2)\n" +
                "                  write(*, *) (i1ad3r(i, j, k), k = 1, shape3(3))\n" +
                "              enddo\n" +
                "          enddo\n" +
                "          write(*, *) '======================'\n" +
                "      end");
    }

    public void testMapFixed() {
        setLoadDocumentText(
                "      program\n" +
                "      structure /explorer1/\n" +
                "      logical*1:: var\n" +
                "      integer*4 :: i\n" +
                "      end structure\n" +
                "      record /explorer1/ example1\n" +
                "      structure /explorer2/\n" +
                "      union\n" +
                "      map\n" +
                "      logical*1:: var\n" +
                "      end map\n" +
                "      map\n" +
                "      integer*4 :: j\n" +
                "      end map\n" +
                "      end union\n" +
                "      integer*4 :: i\n" +
                "      end structure\n" +
                "      record /explorer2/ example2\n" +
                "      example1.var=.TRUE.\n" +
                "      example1.i=1\n" +
                "      print *, 'Simple structure', loc(example1.i)-loc(example1.var)\n" +
                "      example2.var=.FALSE.\n" +
                "      example2.i=1\n" +
                "      print *, 'Union map - var', loc(example2.i)-loc(example2.var)\n" +
                "      example2.j=1\n" +
                "      example2.i=1\n" +
                "      print *, 'Union map - integer', loc(example2.i)-loc(example2.j)\n" +
                "      end");
        setDefaultsOptions(false);
        reformat();
        assertDocumentText("Incorrect map reformat (fixed form)",
                "      program\n" +
                "          structure /explorer1/\n" +
                "              logical*1 :: var\n" +
                "              integer*4 :: i\n" +
                "          end structure\n" +
                "          record /explorer1/ example1\n" +
                "          structure /explorer2/\n" +
                "              union\n" +
                "                  map\n" +
                "                      logical*1 :: var\n" +
                "                  end map\n" +
                "                  map\n" +
                "                      integer*4 :: j\n" +
                "                  end map\n" +
                "              end union\n" +
                "              integer*4 :: i\n" +
                "          end structure\n" +
                "          record /explorer2/ example2\n" +
                "          example1.var = .TRUE.\n" +
                "          example1.i = 1\n" +
                "          print *, 'Simple structure', loc(example1.i) - loc(example1.var)\n" +
                "          example2.var = .FALSE.\n" +
                "          example2.i = 1\n" +
                "          print *, 'Union map - var', loc(example2.i) - loc(example2.var)\n" +
                "          example2.j = 1\n" +
                "          example2.i = 1\n" +
                "          print *, 'Union map - integer', loc(example2.i) - loc(example2.j)\n" +
                "      end");
    }

    public void testPreprocessorFixed() {
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
                " #elif ( 1 > 5)\n" +
                " print *, \"this block_3 must be NOT in output text\"\n" +
                " #else\n" +
                " print *, \"this block_2 must be in output text\"\n" +
                " #endif\n" +
                " endif\n" +
                " end");
        setDefaultsOptions(false);
        reformat();
        assertDocumentText("Incorrect preprocessor reformat (fixed form)",
                "#include \"file\"\n" +
                "#define A\n" +
                "#if defined A\n" +
                "#undef A\n" +
                "      print *, \"this block_1 must be NOT in output text\"\n" +
                "#elif 1\n" +
                "      print *, \"this block_2 must be in output text\"\n" +
                "      print *, \"and this string too\"\n" +
                "#else\n" +
                "      print *, \"this block_3 must be NOT in output text\"\n" +
                "#endif\n" +
                "      if (1 > 0) then\n" +
                "#if 0\n" +
                "          print *, \"this block_1 must be NOT in output text\"\n" +
                "#elif ( 1 > 5)\n" +
                "          print *, \"this block_3 must be NOT in output text\"\n" +
                "#else\n" +
                "          print *, \"this block_2 must be in output text\"\n" +
                "#endif\n" +
                "      endif\n" +
                "      end");
    }

    public void testStructureFixed() {
        setLoadDocumentText(
                "program\n" +
                "structure /STR1/\n" +
                " integer*4 s1\n" +
                " structure STR2\n" +
                "  character*36 s2\n" +
                "  structure STR3\n" +
                "   character*36 s3\n" +
                "   structure STR4\n" +
                "      real*4 s4\n" +
                "    structure STR5\n" +
                "      real*4 s5\n" +
                "      structure STR6\n" +
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
                "       end structure\n" +
                "      end structure\n" +
                "    end structure\n" +
                "   end structure\n" +
                "  end structure\n" +
                " end structure\n" +
                "end structure\n" +
                "structure /OUTSTR/\n" +
                "   real*4 zxc\n" +
                "   record /STR1/ inex\n" +
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
        setDefaultsOptions(false);
        reformat();
        assertDocumentText("Incorrect structure reformat (fixed form)",
                "      program\n" +
                "          structure /STR1/\n" +
                "              integer*4 s1\n" +
                "              structure STR2\n" +
                "                  character*36 s2\n" +
                "                  structure STR3\n" +
                "                      character*36 s3\n" +
                "                      structure STR4\n" +
                "                          real*4 s4\n" +
                "                          structure STR5\n" +
                "                              real*4 s5\n" +
                "                              structure STR6\n" +
                "                                  complex*16 s6\n" +
                "                                  structure STR7\n" +
                "                                      unsigned*4 s7\n" +
                "                                      structure STR8\n" +
                "                                          complex*16 s8\n" +
                "                                          structure STR9\n" +
                "                                              integer*8 s9\n" +
                "                                              structure STR10\n" +
                "                                                  character*16 s10 /'Hello, World!'/\n" +
                "                                              end structure\n" +
                "                                          end structure\n" +
                "                                      end structure\n" +
                "                                  end structure\n" +
                "                              end structure\n" +
                "                          end structure\n" +
                "                      end structure\n" +
                "                  end structure\n" +
                "              end structure\n" +
                "          end structure\n" +
                "          structure /OUTSTR/\n" +
                "              real*4 zxc\n" +
                "              record /STR1/ inex\n" +
                "          end structure\n" +
                "          record /STR1/ ex1\n" +
                "          record /OUTSTR/ example\n" +
                "          ex1.STR2.STR3.STR4.STR5.STR6.STR7.STR8.STR9.s9 = 1\n" +
                "          ex1.STR2.STR3.STR4.STR5.STR6.STR7.s7 = ex1.STR2.STR3.STR4.STR5.STR6.STR7.STR8.STR9.s9\n" +
                "          ex1.STR2.s2 = '1-st text field'\n" +
                "          ex1.STR2.STR3.s3 = 'Second field'\n" +
                "          example.zxc = 123.45\n" +
                "          print *, 'ex1=', ex1\n" +
                "          print *, 'Printing of initial values of outer structure: '\n" +
                "          print *, example\n" +
                "          example.inex.STR2.STR3.STR4.STR5.STR6.STR7.STR8.STR9.s9 = 1\n" +
                "          example.inex.STR2.STR3.STR4.STR5.STR6.STR7.s7 = ex1.STR2.STR3.STR4.STR5.STR6.STR7.STR8.STR9.s9\n" +
                "          example.inex.STR2.s2 = '1-st text field of outer structure'\n" +
                "          example.inex.STR2.STR3.s3 = 'Second field of outer structure'\n" +
                "          print *, 'ex1=', ex1\n" +
                "          print *, 'Printing of result values of outer structure: '\n" +
                "          print *, example\n" +
                "      end");
    }

    public void testDoNumFixed() {
        setLoadDocumentText(
                "      program Bug001\n" +
                "      do 1 i = 1, 67\n" +
                " 1    write ( 6, 100, advance = 'YES') i\n" +
                " 100  format ( 1h, ' ', i2.2)\n" +
                "      end program Bug001");
        setDefaultsOptions(false);
        reformat();
        assertDocumentText("Incorrect function indent (fixed form)",
                "      program Bug001\n" +
                "          do 1 i = 1, 67\n" +
                " 1            write ( 6, 100, advance = 'YES') i\n" +
                " 100      format ( 1h, ' ', i2.2)\n" +
                "      end program Bug001");
    }

    public void testDoDoNumFixed() {
        setLoadDocumentText(
                "      program Bug001\n" +
                "      do 1 i = 1, 67\n" +
                "      do 1 j = 1, 67\n" +
                " 1    write ( 6, 100, advance = 'YES') i\n" +
                " 100  format ( 1h, ' ', i2.2)\n" +
                "      end program Bug001");
        setDefaultsOptions(false);
        reformat();
        assertDocumentText("Incorrect function indent (fixed form)",
                "      program Bug001\n" +
                "          do 1 i = 1, 67\n" +
                "              do 1 j = 1, 67\n" +
                " 1                write ( 6, 100, advance = 'YES') i\n" +
                " 100      format ( 1h, ' ', i2.2)\n" +
                "      end program Bug001");
    }


    public void testCommentFixed() {
        setLoadDocumentText(
                "      program Bug001\n" +
                "C     **************\n" +
                "C     *   Bug001   *\n" +
                "C     **************\n" +
                "      do 1 i = 1, 67\n" +
                "      do 1 j = 1, 67\n" +
                " 1    write ( 6, 100, advance = 'YES') i\n" +
                " 100  format ( 1h, ' ', i2.2)\n" +
                "      end program Bug001");
        setDefaultsOptions(false);
        reformat();
        assertDocumentText("Incorrect function indent (fixed form)",
                "      program Bug001\n" +
                "C     **************\n" +
                "C     *   Bug001   *\n" +
                "C     **************\n" +
                "          do 1 i = 1, 67\n" +
                "              do 1 j = 1, 67\n" +
                " 1                write ( 6, 100, advance = 'YES') i\n" +
                " 100      format ( 1h, ' ', i2.2)\n" +
                "      end program Bug001");
    }

    public void testFunctionFixedContinuation() {
        setLoadDocumentText(
                "      program Bug001\n" +
                "      do 1 i = 1, 67\n" +
                "      do 1 j = 1, 67\n" +
                " 1    write ( 6,\n" +
                "     9100, advance = 'YES') i\n" +
                " 100  format ( 1h, ' ', i2.2)\n" +
                "      end program Bug001");
        setDefaultsOptions(false);
        reformat();
        assertDocumentText("Incorrect function indent (fixed form)",
                "      program Bug001\n" +
                "          do 1 i = 1, 67\n" +
                "              do 1 j = 1, 67\n" +
                " 1                write ( 6,\n" +
                "     9        100, advance = 'YES') i\n" +
                " 100      format ( 1h, ' ', i2.2)\n" +
                "      end program Bug001");
    }

    public void testFunctionFixedContinuation2() {
        setLoadDocumentText(
                "      program Bug001\n" +
                "      do 1 i = 1, 67\n" +
                "      do 1 j = 1, 67\n" +
                " 1    write ( 6,\n" +
                "     *100, advance = 'YES') i\n" +
                " 100  format ( 1h, ' ', i2.2)\n" +
                "      end program Bug001");
        setDefaultsOptions(false);
        reformat();
        assertDocumentText("Incorrect function indent (fixed form)",
                "      program Bug001\n" +
                "          do 1 i = 1, 67\n" +
                "              do 1 j = 1, 67\n" +
                " 1                write ( 6,\n" +
                "     *        100, advance = 'YES') i\n" +
                " 100      format ( 1h, ' ', i2.2)\n" +
                "      end program Bug001");
    }

    public void testFunctionFixedContinuation3() {
        setLoadDocumentText(
                "      int A, B,\n" +
                "     *C,D,\n" +
                "     *E,F\n" +
                "      real A1, B1,\n" +
                "     *C1,D1,\n" +
                "     *E1,F1");
        setDefaultsOptions(false);
        reformat();
        assertDocumentText("Incorrect function indent (fixed form)",
                "      int A, B,\n" +
                "     *    C, D,\n" +
                "     *    E, F\n" +
                "      real A1, B1,\n" +
                "     *    C1, D1,\n" +
                "     *    E1, F1");
    }

    public void testFunctionFixedFistComment() {
        setLoadDocumentText(
                "C\n" +
                "      subroutine bar()\n" +
                "      print *, 'hello from bar...'\n" +
                "      end"
                );
        setDefaultsOptions(false);
        reformat();
        assertDocumentText("Incorrect function indent (fixed form)",
                "C\n" +
                "      subroutine bar()\n" +
                "          print *, 'hello from bar...'\n" +
                "      end"
                );
    }

    public void testFunctionFixedFistComment2() {
        setLoadDocumentText(
                "C This is comment\n" +
                "      subroutine bar()\n" +
                "      print *, 'hello from bar...'\n" +
                "      end"
                );
        setDefaultsOptions(false);
        reformat();
        assertDocumentText("Incorrect function indent (fixed form)",
                "C This is comment\n" +
                "      subroutine bar()\n" +
                "          print *, 'hello from bar...'\n" +
                "      end"
                );
    }

    public void testCommentFixed2() {
        setLoadDocumentText(
                "      program Bug001\n" +
                "**************\n" +
                "*   Bug001   *\n" +
                "**************\n" +
                "      do 1 i = 1, 67\n" +
                "      do 1 j = 1, 67\n" +
                " 1    write ( 6, 100, advance = 'YES') i\n" +
                " 100  format ( 1h, ' ', i2.2)\n" +
                "      end program Bug001");
        setDefaultsOptions(false);
        reformat();
        assertDocumentText("Incorrect function indent (fixed form)",
                "      program Bug001\n" +
                "**************\n" +
                "*   Bug001   *\n" +
                "**************\n" +
                "          do 1 i = 1, 67\n" +
                "              do 1 j = 1, 67\n" +
                " 1                write ( 6, 100, advance = 'YES') i\n" +
                " 100      format ( 1h, ' ', i2.2)\n" +
                "      end program Bug001");
    }

    public void testSampleFixed() {
        setLoadDocumentText(
                "\t#define N 10\n" +
                "\tSUBROUTINE test\n" +
                "! free comment\n" +
                "C fixed comment\n" +
                "\tdo i = 1, N\n" +
                "\tif ( mod(i,2) == 0 ) then\n" +
                "\tprint *, \"even string\"   ! even\n" +
                "\telse\n" +
                "\tprint *, \"odd string\"    ! odd\n" +
                "\tend if\n" +
                "\tend do\n" +
                "\tend\n"
                );
        setDefaultsOptions(false);
        reformat();
        assertDocumentText("Incorrect module reformat (fixed form)",
                "#define N 10\n" +
                "      SUBROUTINE test\n" +
                "          ! free comment\n" +
                "C fixed comment\n" +
                "          do i = 1, N\n" +
                "              if (mod(i, 2) == 0) then\n" +
                "                  print *, \"even string\" ! even\n" +
                "              else\n" +
                "                  print *, \"odd string\" ! odd\n" +
                "              end if\n" +
                "          end do\n" +
                "      end\n"
                );
    }

    public void testCommentFixedFree() {
        setLoadDocumentText(
                "!/*\n" +
                "! *\n"
                );
        setDefaultsOptions(false);
        reformat();
        assertDocumentText("Incorrect module reformat (fixed form)",
                "!/*\n" +
                "! *\n"
                );
    }
}
