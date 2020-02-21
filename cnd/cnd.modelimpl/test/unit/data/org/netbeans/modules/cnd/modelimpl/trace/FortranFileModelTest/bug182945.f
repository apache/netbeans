      module constants
         integer, parameter :: np=2000, dbl=selected_real_kind(14,100)
         real(dbl) :: g=9.807,dtmin=.001
      end module constants

      program fall
      use constants
      implicit none
!
!   Program to calculate the dynamics
!
!   Module "constants" is used for ...
      real(dbl) dt
      print *,g
      stop
      end
