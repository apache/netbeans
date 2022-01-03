c
c     --------------------------------
      character function d2ch ( idig )
c     --------------------------------
c
c     Converts a digit 0 <= idig <= 9 into a corresponding character
c
      implicit none
c
      integer idig
c
      if ( (idig .ge. 0).and.(idig.le.9) ) then
        d2ch = char(48 + idig)
      else
        write(*,*) 'd2ch error : idig =', idig,' is not a digit'
      endif
c
      return
      end
c
c     ------------------------------------------------------------
      subroutine SetViewAngle ( dPhi, dTheta, CTH, STH, CPH, SPH )
c     ------------------------------------------------------------
c
c     Sets sines and cosines of the viewing angle
c
c     input  : dPhi, dTheta - viewing angles in degrees
c     output : CPH, SPH, CTH, STH - cos(Phi),sin(Phi),cos(Theta),sin(Theta)
c
      implicit none
c
      real pi, d2r
      parameter ( pi  = 3.14159265358978245 )
      parameter ( d2r = pi / 180.0 )
c
      real dPhi, dTheta, Phi, Theta, CPH, SPH, CTH, STH
c
      Phi   = dPhi * d2r
      CPH   = cos(Phi)
      SPH   = sin(Phi)
      Theta = dTheta * d2r
      CTH   = cos(Theta)
      STH   = sin(Theta)
c
      return
      end
c
c     ---------------------------------
      subroutine InitGIFDevice ( name )
c     ---------------------------------
c
c     Open GIF file with name 'name'
c
      implicit none
c
      real ScreenSize
      character*15 name
c
      call pgbegin ( 0 , name , 1 , 1 )
      call pgpap ( 7.515 , 0.75 )  ! 640x480
c      call pgpap ( 3.0 , 1.0 )  ! 256x256
      call pgask ( .false. )
      call pgenv ( -0.5 , 0.5 , -0.5 , 0.5 , 1 , -2 )
      call pgslw ( 1 ) ! set minimum possible linewidth
c
      return
      end
c
c     ------------------------------------------
      integer function InitDevice ( name , nch )
c     ------------------------------------------
c
c     Open an arbitrary PGPLOT device
c
c     input : name - the name of PGPLOT device
c             nch  - number of characters in the Device name
c                    (length of the string name)
c
      implicit none
c
      integer nch
      character*50 name
c
      real w,sd
      integer pgopen, istat
      external pgopen
c
      istat = pgopen ( name(1:nch) )
      if ( istat .le. 0 ) then
        write(*,*) 'error: InitDevice: cannot open device',name
        stop
      else
        InitDevice = istat
      endif
c
      w  = 10.0
      sd = 1.0
c
      call PGPAP ( w , sd )
      call PGASK ( .false. )
      call PGENV ( -0.5 , 0.5 , -0.5 , 0.5 , 1 , -2 )
      call PGSLW ( 1 ) ! set minimum possible linewidth
      call PGSCF ( 2 )
c
      return
      end
c

c     --------------------------------

      subroutine DrawFrame ( F , psu )
c     --------------------------------
c
c     Draws a wireframe
c
c     input : F(8,3)  - 3D coordinates of the 8 frame corners
c             psu(11) - plot setup
c
      implicit none
c
      real F(8,3), psu(11)
c
      real Fp(8,2) ! array of coordinates of projected corners
c
      integer i
      do i = 1 , 8
        call OnScreen ( F(i,1) , F(i,2) , F(i,3) , psu ,
     &                  Fp(i,1) , Fp(i,2)
     &                 )
      enddo
c
      call PGSCI ( 1 )  ! draw the frame with a color negative to the BG
c
      call Line ( Fp(1,1) , Fp(1,2) , Fp(2,1) , Fp(2,2) )
      call Line ( Fp(1,1) , Fp(1,2) , Fp(4,1) , Fp(4,2) )
      call Line ( Fp(2,1) , Fp(2,2) , Fp(3,1) , Fp(3,2) )
      call Line ( Fp(3,1) , Fp(3,2) , Fp(4,1) , Fp(4,2) )
      call Line ( Fp(5,1) , Fp(5,2) , Fp(6,1) , Fp(6,2) )
      call Line ( Fp(5,1) , Fp(5,2) , Fp(8,1) , Fp(8,2) )
      call Line ( Fp(6,1) , Fp(6,2) , Fp(7,1) , Fp(7,2) )
      call Line ( Fp(7,1) , Fp(7,2) , Fp(8,1) , Fp(8,2) )
      call Line ( Fp(1,1) , Fp(1,2) , Fp(5,1) , Fp(5,2) )
      call Line ( Fp(2,1) , Fp(2,2) , Fp(6,1) , Fp(6,2) )
      call Line ( Fp(3,1) , Fp(3,2) , Fp(7,1) , Fp(7,2) )
      call Line ( Fp(4,1) , Fp(4,2) , Fp(8,1) , Fp(8,2) )
c
      return
      end

c     -------------------------------------
      subroutine Line ( X1 , Y1 , X2 , Y2 )
c     -------------------------------------
c
c     Draws a line between points (X1,Y1) and (X2,Y2)
c     with clipping disabled
c
      implicit none
c
      real X1 , X2 , Y1 , Y2
c
      real  Xl(2) , Yl(2)
c
      call PGSCLP ( 0 )    ! disable clipping at the viewport edges
c
      Xl(1) = X1
      Yl(1) = Y1
      Xl(2) = X2
      Yl(2) = Y2
c
      call PGLine ( 2 , Xl , Yl )
c
      return
      end
c
c     -------------------------------------------------
      subroutine OnScreen ( X , Y , Z , psu , sX , sY )
c     -------------------------------------------------
c
c     From 3D coordinates of a point calculates a 2D screen coordinates
c
c     input  : X , Y , Z  - 3D coordinates of a point
c                           ( in the range [-0.5+Xb,0.5+Xb] by
c                             convention, where Xb is a coordinate
c                             of the box center )
c              psu(1)  = Xb    ! x-coordinate (3D) of the box center
c              psu(2)  = Yb    ! y-coordinate (3D) of the box center
c              psu(3)  = Zb    ! z-coordinate (3D) of the box center
c              psu(4)  = Scale ! scale of the plot for zoom in/out
c              psu(5)  = CTH   ! cosine of the viewing angle Theta
c              psu(6)  = STH   ! sine -"-
c              psu(7)  = CPH   ! cosine of the viewing angle Phi
c              psu(8)  = SPH   ! sine -"-
c              psu(9)  = Xlook ! x-coordinate (3D) of the viewing point
c              psu(10) = Ylook ! y-coordinate (3D) of the viewing point
c              psu(11) = Zlook ! z-coordinate (3D) of the viewing point
c
c     output : sX , sY - 2D screen coordinates
c
      implicit none
c
      real X, Y, Z, psu(11)
      real sX , sY
c
      real Xd, Yd, Zd
c
      Xd = X*psu(5)*psu(7) + Y*psu(5)*psu(8) - Z*psu(6) - psu(1)
      Yd = Y*psu(7) - X*psu(8) - psu(2)
      Zd = X*psu(6)*psu(7) + Y*psu(6)*psu(8) + Z*psu(5) - psu(3)
      sX = psu(4) * psu(11) * Xd / abs(psu(11)-Zd) + psu(9)
      sY = psu(4) * psu(11) * Yd / abs(psu(11)-Zd) + psu(10)
c
      return
      end
c
c     --------------------------
      subroutine InitFrame ( F )
c     --------------------------
c
c     Sets array F(8,3) with coordinates of the box corners
c
      implicit none
c
      real F(8,3)
c
      F(1,1) = -0.5
      F(1,2) = -0.5
      F(1,3) = -0.5
      F(2,1) = -0.5
      F(2,2) =  0.5
      F(2,3) = -0.5
      F(3,1) =  0.5
      F(3,2) =  0.5
      F(3,3) = -0.5
      F(4,1) =  0.5
      F(4,2) = -0.5
      F(4,3) = -0.5
      F(5,1) = -0.5
      F(5,2) = -0.5
      F(5,3) =  0.5
      F(6,1) = -0.5
      F(6,2) =  0.5
      F(6,3) =  0.5
      F(7,1) =  0.5
      F(7,2) =  0.5
      F(7,3) =  0.5
      F(8,1) =  0.5
      F(8,2) = -0.5
      F(8,3) =  0.5
c
      return
      end
c
c     --------------------------------------------
      SUBROUTINE PALETT ( TYPE , CONTRA , BRIGHT )
c     --------------------------------------------
c
c     Sets a "palette" of colors in the range of defined color indices
c     This subroutine is distributed with PGPLOT in one of the demos
c
      INTEGER TYPE
      REAL CONTRA, BRIGHT
C
      REAL GL(2), GR(2), GG(2), GB(2)
      REAL RL(9), RR(9), RG(9), RB(9)
      REAL HL(5), HR(5), HG(5), HB(5)
      REAL CL(5), CR(5), CG(5), CB(5)
      REAL WL(10), WR(10), WG(10), WB(10)
      REAL AL(20), AR(20), AG(20), AB(20)
C
      DATA GL /0.0, 1.0/
      DATA GR /0.0, 1.0/
      DATA GG /0.0, 1.0/
      DATA GB /0.0, 1.0/
C
      DATA RL /-0.5, 0.0, 0.17, 0.33, 0.50, 0.67, 0.83, 1.0, 1.7/
      DATA RR / 0.0, 0.0,  0.0,  0.0,  0.6,  1.0,  1.0, 1.0, 1.0/
      DATA RG / 0.0, 0.0,  0.0,  1.0,  1.0,  1.0,  0.6, 0.0, 1.0/
      DATA RB / 0.0, 0.3,  0.8,  1.0,  0.3,  0.0,  0.0, 0.0, 1.0/
C
      DATA HL /0.0, 0.2, 0.4, 0.6, 1.0/
      DATA HR /0.0, 0.5, 1.0, 1.0, 1.0/
      DATA HG /0.0, 0.0, 0.5, 1.0, 1.0/
      DATA HB /0.0, 0.0, 0.0, 0.3, 1.0/
C
      DATA CL /0.0, 0.2, 0.4, 0.6, 1.0/
      DATA CR /0.0, 0.0, 0.0, 0.3, 1.0/
      DATA CG /0.0, 0.0, 0.5, 1.0, 1.0/
      DATA CB /0.0, 0.5, 1.0, 1.0, 1.0/
C
      DATA WL /0.0, 0.5, 0.5, 0.7, 0.7, 0.85, 0.85, 0.95, 0.95, 1.0/
      DATA WR /0.0, 1.0, 0.0, 0.0, 0.3,  0.8,  0.3,  1.0,  1.0, 1.0/
      DATA WG /0.0, 0.5, 0.4, 1.0, 0.0,  0.0,  0.2,  0.7,  1.0, 1.0/
      DATA WB /0.0, 0.0, 0.0, 0.0, 0.4,  1.0,  0.0,  0.0, 0.95, 1.0/
C
      DATA AL /0.0, 0.1, 0.1, 0.2, 0.2, 0.3, 0.3, 0.4, 0.4, 0.5,
     :         0.5, 0.6, 0.6, 0.7, 0.7, 0.8, 0.8, 0.9, 0.9, 1.0/
      DATA AR /0.0, 0.0, 0.3, 0.3, 0.5, 0.5, 0.0, 0.0, 0.0, 0.0,
     :         0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0/
      DATA AG /0.0, 0.0, 0.3, 0.3, 0.0, 0.0, 0.0, 0.0, 0.8, 0.8,
     :         0.6, 0.6, 1.0, 1.0, 1.0, 1.0, 0.8, 0.8, 0.0, 0.0/
      DATA AB /0.0, 0.0, 0.3, 0.3, 0.7, 0.7, 0.7, 0.7, 0.9, 0.9,
     :         0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0/
C
      IF (TYPE.EQ.1) THEN
C        -- gray scale
         CALL PGCTAB(GL, GR, GG, GB, 2, CONTRA, BRIGHT)
      ELSE IF (TYPE.EQ.2) THEN
C        -- rainbow
         CALL PGCTAB(RL, RR, RG, RB, 9, CONTRA, BRIGHT)
      ELSE IF (TYPE.EQ.3) THEN
C        -- heat
         CALL PGCTAB(HL, HR, HG, HB, 5, CONTRA, BRIGHT)
      ELSE IF (TYPE.EQ.4) THEN
C        -- freeze
         CALL PGCTAB(CL, CR, CG, CB, 5, CONTRA, BRIGHT)
      ELSE IF (TYPE.EQ.5) THEN
C        -- AIPS
         CALL PGCTAB(AL, AR, AG, AB, 20, CONTRA, BRIGHT)
      END IF
      END
c
c     ------------------------------------------------
      SUBROUTINE FIDDLE ( P , SIGN , CONTRA , BRIGHT )
c     ------------------------------------------------

      end
      subroutine SetViewAngle2 ( dPhi, dTheta, CTH, STH, CPH, SPH )
c     ------------------------------------------------------------
c
c     Sets sines and cosines of the viewing angle
c
c     input  : dPhi, dTheta - viewing angles in degrees
c     output : CPH, SPH, CTH, STH - cos(Phi),sin(Phi),cos(Theta),sin(Theta)
c
      implicit none
c
      real pi, d2r
      parameter ( pi  = 3.14159265358978245 )
      parameter ( d2r = pi / 180.0 )
c
      real dPhi, dTheta, Phi, Theta, CPH, SPH, CTH, STH
c
      Phi   = dPhi * d2r
      CPH   = cos(Phi)
      SPH   = sin(Phi)
      Theta = dTheta * d2r
      CTH   = cos(Theta)
      STH   = sin(Theta)
c
      return
      end