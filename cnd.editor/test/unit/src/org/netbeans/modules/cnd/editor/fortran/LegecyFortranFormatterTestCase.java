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

/**
 *
 */
public class LegecyFortranFormatterTestCase extends FortranEditorBase {

    public LegecyFortranFormatterTestCase(String testMethodName) {
        super(testMethodName);
    }

    @Override
    protected void assertDocumentText(String msg, String expectedText) {
        super.assertDocumentText(msg, expectedText);
        reformat();
        super.assertDocumentText(msg+" (not stable)", expectedText);
    }

    public void testProgramFree() {
        setLoadDocumentText(
                "program p\n" +
                " character(10) :: line = \"!23!56!8!0\" ! Line\n" +
                "   print *, line\n" +
                "  end program");
        setDefaultsOptions(true);
        reformat();
        assertDocumentText("Incorrect program reformat (free form)",
                "program p\n" +
                "    character(10) :: line = \"!23!56!8!0\" ! Line\n" +
                "    print *, line\n" +
                "end program");
    }

    public void testProgramFixed() {
        setLoadDocumentText(
                "program p\n" +
                " character(10) :: line = \"!23!56!8!0\" ! Line\n" +
                "   print *, line\n" +
                "  end program");
        setDefaultsOptions(false);
        reformat();
        assertDocumentText("Incorrect program reformat (fixed form)",
                "      program p\n" +
                "          character(10) :: line = \"!23!56!8!0\" ! Line\n" +
                "          print *, line\n" +
                "      end program");
    }

    public void testProgram2Free() {
        setLoadDocumentText(
                "program p\n" +
                "   print *, 'Hello World'\n" +
                "  endprogram");
        setDefaultsOptions(true);
        reformat();
        assertDocumentText("Incorrect program reformat (free form)",
                "program p\n" +
                "    print *, 'Hello World'\n" +
                "endprogram");
    }

    public void testProgram2Fixed() {
        setLoadDocumentText(
                "program p\n" +
                "   print *, 'Hello World'\n" +
                "  endprogram");
        setDefaultsOptions(false);
        reformat();
        assertDocumentText("Incorrect program reformat (fixed form)",
                "      program p\n" +
                "          print *, 'Hello World'\n" +
                "      endprogram");
    }

    public void testBlockDataFree() {
        setLoadDocumentText(
                "BLoCKdatA Unit\n" +
                "DoublePrecision A\n" +
                "datA a/1d0/\n" +
                " COMMOn /a/ a\n" +
                "eNDBLOCKdata uniT\n" +
                "enD");
        setDefaultsOptions(true);
        reformat();
        assertDocumentText("Incorrect block data reformat (free form)",
                "BLoCKdatA Unit\n" +
                "    DoublePrecision A\n" +
                "    datA a/1d0/\n" +
                "    COMMOn /a/ a\n" +
                "eNDBLOCKdata uniT\n" +
                "enD");
    }

    public void testBlockDataFixed() {
        setLoadDocumentText(
                "BLoCKdatA Unit\n" +
                "DoublePrecision A\n" +
                "datA a/1d0/\n" +
                " COMMOn /a/ a\n" +
                "eNDBLOCKdata uniT\n" +
                "enD");
        setDefaultsOptions(false);
        reformat();
        assertDocumentText("Incorrect block data reformat (fixed form)",
                "      BLoCKdatA Unit\n" +
                "          DoublePrecision A\n" +
                "          datA a/1d0/\n" +
                "          COMMOn /a/ a\n" +
                "      eNDBLOCKdata uniT\n" +
                "      enD");
    }

    public void testStatementsFree() {
        setLoadDocumentText(
                "program A\n" +
                "integer::j(5)/1,2,3,4,5/,i\n" +
                "i=1\n" +
                "if (j(3)==i+2) then\n" +
                "i=j(5)\n" +
                "elseif (i<3) then\n" +
                "i=2\n" +
                "endif\n" +
                "where(j>2)\n" +
                "j=10\n" +
                "elsewhere(j==1)\n" +
                "j=0\n" +
                "endwhere\n" +
                "print *,j\n" +
                "endprogram A");
        setDefaultsOptions(true);
        reformat();
        assertDocumentText("Incorrect statements reformat (free form)",
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    if (j(3) == i + 2) then\n" +
                "        i = j(5)\n" +
                "    elseif (i < 3) then\n" +
                "        i = 2\n" +
                "    endif\n" +
                "    where(j > 2)\n" +
                "        j = 10\n" +
                "    elsewhere(j == 1)\n" +
                "        j = 0\n" +
                "    endwhere\n" +
                "    print *, j\n" +
                "endprogram A");
    }

    public void testStatementsFixed() {
        setLoadDocumentText(
                "      program A\n" +
                "      integer::j(5)/1,2,3,4,5/,i\n" +
                "      i=1\n" +
                "      if(i.ne.5) goto 5\n" +
                "5     if (j(3)==i+2) then\n" +
                "      i=j(5)\n" +
                "      elseif (i<3) then\n" +
                "      i=2\n" +
                "      endif\n" +
                "      where(j>2)\n" +
                "      j=10\n" +
                "      elsewhere(j==1)\n" +
                "      j=0\n" +
                "      endwhere\n" +
                "      print *,j\n" +
                "endprogram A");
        setDefaultsOptions(false);
        reformat();
        assertDocumentText("Incorrect statements reformat (fixed form)",
                "      program A\n" +
                "          integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "          i = 1\n" +
                "          if (i .ne. 5) goto 5\n" +
                " 5        if (j(3) == i + 2) then\n" +
                "              i = j(5)\n" +
                "          elseif (i < 3) then\n" +
                "              i = 2\n" +
                "          endif\n" +
                "          where(j > 2)\n" +
                "              j = 10\n" +
                "          elsewhere(j == 1)\n" +
                "              j = 0\n" +
                "          endwhere\n" +
                "          print *, j\n" +
                "      endprogram A");
    }

    public void testStatements2Free() {
        setLoadDocumentText(
                "program A\n" +
                "integer::j(5)/1,2,3,4,5/,i\n" +
                "i=1\n" +
                "if (j(3)==i+2) then\n" +
                "i=j(5)\n" +
                "else if (i<3) then\n" +
                "i=2\n" +
                "end if\n" +
                "where(j>2)\n" +
                "j=10\n" +
                "else where(j==1)\n" +
                "j=0\n" +
                "end where\n" +
                "print *,j\n" +
                "end program A");
        setDefaultsOptions(true);
        reformat();
        assertDocumentText("Incorrect statements reformat (free form)",
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    if (j(3) == i + 2) then\n" +
                "        i = j(5)\n" +
                "    else if (i < 3) then\n" +
                "        i = 2\n" +
                "    end if\n" +
                "    where(j > 2)\n" +
                "        j = 10\n" +
                "    else where(j == 1)\n" +
                "        j = 0\n" +
                "    end where\n" +
                "    print *, j\n" +
                "end program A");
    }

    public void testStatements2Fixed() {
        setLoadDocumentText(
                "      program A\n" +
                "      integer::j(5)/1,2,3,4,5/,i\n" +
                "      i=1\n" +
                "      if(i.ne.5) goto 5\n" +
                "   5  if (j(3)==i+2) then\n" +
                "      i=j(5)\n" +
                "      else if (i<3) then\n" +
                "      i=2\n" +
                "      end if\n" +
                "      where(j>2)\n" +
                "      j=10\n" +
                "      else where(j==1)\n" +
                "      j=0\n" +
                "      end where\n" +
                "      print *,j\n" +
                "      end program A");
        setDefaultsOptions(false);
        reformat();
        assertDocumentText("Incorrect statements reformat (fixed form)",
                "      program A\n" +
                "          integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "          i = 1\n" +
                "          if (i .ne. 5) goto 5\n" +
                " 5        if (j(3) == i + 2) then\n" +
                "              i = j(5)\n" +
                "          else if (i < 3) then\n" +
                "              i = 2\n" +
                "          end if\n" +
                "          where(j > 2)\n" +
                "              j = 10\n" +
                "          else where(j == 1)\n" +
                "              j = 0\n" +
                "          end where\n" +
                "          print *, j\n" +
                "      end program A");
    }

    public void testEnumAndSelectFree() {
        setLoadDocumentText(
                "Enum Enum\n" +
                "Enumerator zero, one, two\n" +
                "endEnum Enum\n" +
                "Type Enum1\n" +
                "Type(Enum) Enum\n" +
                "endType Enum1\n" +
                "Type(Enum),parameter::Enum2/zero/\n" +
                "Type(Enum1) Enum,Enum3\n" +
                "Enum3.Enum=two\n" +
                "selectCase(Enum3.Enum)\n" +
                " case(zero)\n" +
                " print *, \" zero \", Enum3.Enum\n" +
                " case(one)\n" +
                " print *, \" one \", Enum3.Enum\n" +
                " case(two)\n" +
                " print *, \" two \", Enum3.Enum\n" +
                "endSelect");
        setDefaultsOptions(true);
        reformat();
        assertDocumentText("Incorrect enum and select reformat (free form)",
                "Enum Enum\n" +
                "    Enumerator zero, one, two\n" +
                "endEnum Enum\n" +
                "Type Enum1\n" +
                "    Type(Enum) Enum\n" +
                "endType Enum1\n" +
                "Type(Enum), parameter :: Enum2/zero/\n" +
                "Type(Enum1) Enum, Enum3\n" +
                "Enum3.Enum = two\n" +
                "selectCase (Enum3.Enum)\n" +
                "    case(zero)\n" +
                "        print *, \" zero \", Enum3.Enum\n" +
                "    case(one)\n" +
                "        print *, \" one \", Enum3.Enum\n" +
                "    case(two)\n" +
                "        print *, \" two \", Enum3.Enum\n" +
                "endSelect");
    }

    public void testEnumAndSelectFixed() {
        setLoadDocumentText(
                "      Enum Enum\n" +
                "      Enumerator zero, one, two\n" +
                "      endEnum Enum\n" +
                "      Type Enum1\n" +
                "      Type(Enum) Enum\n" +
                "      endType Enum1\n" +
                "      Type(Enum),parameter::Enum2/zero/\n" +
                "      Type(Enum1) Enum,Enum3\n" +
                "      Enum3.Enum=two\n" +
                "      selectCase(Enum3.Enum)\n" +
                "       case(zero)\n" +
                "       print *, \" zero \", Enum3.Enum\n" +
                "       case(one)\n" +
                "       print *, \" one \", Enum3.Enum\n" +
                "       case(two)\n" +
                "       print *, \" two \", Enum3.Enum\n" +
                "      endSelect");
        setDefaultsOptions(false);
        reformat();
        assertDocumentText("Incorrect enum and select reformat (fixed form)",
                "      Enum Enum\n" +
                "          Enumerator zero, one, two\n" +
                "      endEnum Enum\n" +
                "      Type Enum1\n" +
                "          Type(Enum) Enum\n" +
                "      endType Enum1\n" +
                "      Type(Enum), parameter :: Enum2/zero/\n" +
                "      Type(Enum1) Enum, Enum3\n" +
                "      Enum3.Enum = two\n" +
                "      selectCase (Enum3.Enum)\n" +
                "          case(zero)\n" +
                "              print *, \" zero \", Enum3.Enum\n" +
                "          case(one)\n" +
                "              print *, \" one \", Enum3.Enum\n" +
                "          case(two)\n" +
                "              print *, \" two \", Enum3.Enum\n" +
                "      endSelect");
    }

    public void testModuleFree() {
        setLoadDocumentText(
                " Module A\n" +
                "   INTERFACE\n" +
                "       SUBROUTINE EXT1(X, Y, Z)\n" +
                "           REAL, DIMENSION (100, 100) :: X, Y, Z\n" +
                "       ENDSUBROUTINE EXT1\n" +
                "       SUBROUTINE EXT2(X, Z)\n" +
                "           REAL X;COMPLEX (KIND = 4) Z (2000)\n" +
                "       ENDSUBROUTINE EXT2\n" +
                "       FUNCTION EXT3 (P, Q)\n" +
                "          LOGICAL EXT3 ; INTEGER P (1000) ; LOGICAL Q (1000)\n" +
                "       ENDFUNCTION EXT3\n" +
                "   ENDINTERFACE\n" +
                " EndModule");
        setDefaultsOptions(true);
        reformat();
        assertDocumentText("Incorrect module reformat (free form)",
                "Module A\n" +
                "    INTERFACE\n" +
                "        SUBROUTINE EXT1(X, Y, Z)\n" +
                "            REAL, DIMENSION (100, 100) :: X, Y, Z\n" +
                "        ENDSUBROUTINE EXT1\n" +
                "        SUBROUTINE EXT2(X, Z)\n" +
                "            REAL X; COMPLEX (KIND = 4) Z(2000)\n" +
                "        ENDSUBROUTINE EXT2\n" +
                "        FUNCTION EXT3(P, Q)\n" +
                "            LOGICAL EXT3; INTEGER P(1000); LOGICAL Q(1000)\n" +
                "        ENDFUNCTION EXT3\n" +
                "    ENDINTERFACE\n" +
                "EndModule");
    }


    public void testModuleFixed() {
        setLoadDocumentText(
                " Module A\n" +
                "   INTERFACE\n" +
                "       SUBROUTINE EXT1 (X, Y, Z)\n" +
                "           REAL, DIMENSION (100, 100) :: X, Y, Z\n" +
                "       ENDSUBROUTINE EXT1\n" +
                "       SUBROUTINE EXT2 (X, Z)\n" +
                "           REAL X;COMPLEX (KIND = 4) Z (2000)\n" +
                "       ENDSUBROUTINE EXT2\n" +
                "       FUNCTION EXT3 (P, Q)\n" +
                "           LOGICAL EXT3 ; INTEGER P (1000) ; LOGICAL Q (1000)\n" +
                "       ENDFUNCTION EXT3\n" +
                "   ENDINTERFACE\n" +
                " EndModule");
        setDefaultsOptions(false);
        reformat();
        assertDocumentText("Incorrect module reformat (fixed form)",
                "      Module A\n" +
                "          INTERFACE\n" +
                "              SUBROUTINE EXT1(X, Y, Z)\n" +
                "                  REAL, DIMENSION (100, 100) :: X, Y, Z\n" +
                "              ENDSUBROUTINE EXT1\n" +
                "              SUBROUTINE EXT2(X, Z)\n" +
                "                  REAL X; COMPLEX (KIND = 4) Z(2000)\n" +
                "              ENDSUBROUTINE EXT2\n" +
                "              FUNCTION EXT3(P, Q)\n" +
                "                  LOGICAL EXT3; INTEGER P(1000); LOGICAL Q(1000)\n" +
                "              ENDFUNCTION EXT3\n" +
                "          ENDINTERFACE\n" +
                "      EndModule");
    }

    public void testContainsAndForallFree() {
        setLoadDocumentText(
                "implicit none\n" +
                "integer(4) i, j(0:9)\n" +
                "do i=1,10\n" +
                " call a(i);j(10-i)=i\n" +
                "enddo\n" +
                "forall (i=2:7,J(I)<>3)\n" +
                " j(i)=100\n" +
                "endforall\n" +
                " print *, j\n" +
                "contains\n" +
                "subroutine a(i,j)\n" +
                " integer, intent(inout)::i\n" +
                " integer, optional, intent(in                   out)::j\n" +
                " print *,i\n" +
                "end subroutine\n" +
                "End");
        setDefaultsOptions(true);
        reformat();
        assertDocumentText("Incorrect contains and forall reformat (free form)",
                "implicit none\n" +
                "integer(4) i, j(0:9)\n" +
                "do i = 1, 10\n" +
                "    call a(i); j(10 - i) = i\n" +
                "enddo\n" +
                "forall (i = 2:7, J(I) <> 3)\n" +
                "    j(i) = 100\n" +
                "endforall\n" +
                "print *, j\n" +
                "contains\n" +
                "subroutine a(i, j)\n" +
                "    integer, intent(inout) :: i\n" +
                "    integer, optional, intent(in out) :: j\n" +
                "    print *, i\n" +
                "end subroutine\n" +
                "End");
    }

    public void testContainsAndForallFixed() {
        setLoadDocumentText(
                "      implicit none\n" +
                "      integer(4) i, j(0:9)\n" +
                "      do i=1,10\n" +
                "       call a(i);j(10-i)=i\n" +
                "      enddo\n" +
                "      forall (i=2:7,J(I)<>3)\n" +
                "       j(i)=100\n" +
                "      endforall\n" +
                "       print *, j\n" +
                "       contains\n" +
                "      subroutine a(i,j)\n" +
                "       integer, intent(inout)::i\n" +
                "       integer, optional, intent(in                   out)::j\n" +
                "       print *,i\n" +
                "      end subroutine\n" +
                "      End");
        setDefaultsOptions(false);
        reformat();
        assertDocumentText("Incorrect contains and forall reformat (fixed form)",
                "      implicit none\n" +
                "      integer(4) i, j(0:9)\n" +
                "      do i = 1, 10\n" +
                "          call a(i); j(10 - i) = i\n" +
                "      enddo\n" +
                "      forall (i = 2:7, J(I) <> 3)\n" +
                "          j(i) = 100\n" +
                "      endforall\n" +
                "      print *, j\n" +
                "      contains\n" +
                "      subroutine a(i, j)\n" +
                "          integer, intent(inout) :: i\n" +
                "          integer, optional, intent(in out) :: j\n" +
                "          print *, i\n" +
                "      end subroutine\n" +
                "      End");
    }

    public void testIfFree() {
        setLoadDocumentText(
                "  implicit double precision (a-h)\n" +
                "  implicit doubleprecision (o-z)\n" +
                "  do i=-1,1\n" +
                "  if (i.eq.0) then\n" +
                "  write(*,*)a(i)\n" +
                "  elseif(i.gt.0) then\n" +
                "  write(*,*)b(i)\n" +
                "  else if(i.lt.0) then\n" +
                "  write(*,*)c(i)\n" +
                "  endif\n" +
                "  enddo\n" +
                "  end\n" +
                "  real*8 function a(n)\n" +
                "  a=dble(n+10)\n" +
                "  return\n" +
                "  endfunction\n" +
                "  double precision function b(n)\n" +
                "  b=dble(n*10)\n" +
                "  end\n" +
                "  doubleprecision function c(n)\n" +
                "  c=dble(n-10)\n" +
                "  end function");
        setDefaultsOptions(true);
        reformat();
        assertDocumentText("Incorrect if reformat (free form)",
                "implicit double precision (a - h)\n" +
                "implicit doubleprecision (o - z)\n" +
                "do i = -1, 1\n" +
                "    if (i .eq. 0) then\n" +
                "        write(*, *) a(i)\n" +
                "    elseif (i .gt. 0) then\n" +
                "        write(*, *) b(i)\n" +
                "    else if (i .lt. 0) then\n" +
                "        write(*, *) c(i)\n" +
                "    endif\n" +
                "enddo\n" +
                "end\n" +
                "real*8 function a(n)\n" +
                "    a = dble(n + 10)\n" +
                "    return\n" +
                "endfunction\n" +
                "double precision function b(n)\n" +
                "    b = dble(n * 10)\n" +
                "end\n" +
                "doubleprecision function c(n)\n" +
                "    c = dble(n - 10)\n" +
                "end function");
    }

    public void testIfFixed() {
        setLoadDocumentText(
                "        implicit double precision (a-h)\n" +
                "        implicit doubleprecision (o-z)\n" +
                "        do i=-1,1\n" +
                "        if (i.eq.0) then\n" +
                "        write(*,*)a(i)\n" +
                "        elseif(i.gt.0) then\n" +
                "        write(*,*)b(i)\n" +
                "        else if(i.lt.0) then\n" +
                "        write(*,*)c(i)\n" +
                "        endif\n" +
                "        enddo\n" +
                "        end\n" +
                "        real*8 function a(n)\n" +
                "        a=dble(n+10)\n" +
                "        return\n" +
                "        endfunction\n" +
                "        double precision function b(n)\n" +
                "        b=dble(n*10)\n" +
                "        end\n" +
                "        doubleprecision function c(n)\n" +
                "        c=dble(n-10)\n" +
                "        end function");
        setDefaultsOptions(false);
        reformat();
        assertDocumentText("Incorrect if reformat (fixed form)",
                "      implicit double precision (a - h)\n" +
                "      implicit doubleprecision (o - z)\n" +
                "      do i = -1, 1\n" +
                "          if (i .eq. 0) then\n" +
                "              write(*, *) a(i)\n" +
                "          elseif (i .gt. 0) then\n" +
                "              write(*, *) b(i)\n" +
                "          else if (i .lt. 0) then\n" +
                "              write(*, *) c(i)\n" +
                "          endif\n" +
                "      enddo\n" +
                "      end\n" +
                "      real*8 function a(n)\n" +
                "          a = dble(n + 10)\n" +
                "          return\n" +
                "      endfunction\n" +
                "      double precision function b(n)\n" +
                "          b = dble(n * 10)\n" +
                "      end\n" +
                "      doubleprecision function c(n)\n" +
                "          c = dble(n - 10)\n" +
                "      end function");
    }
}
