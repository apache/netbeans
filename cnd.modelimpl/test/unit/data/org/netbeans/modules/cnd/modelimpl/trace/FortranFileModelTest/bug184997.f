subroutine swap_real(a1, a2)

   implicit none

!  Input/Output
   real, intent(inout) :: a1(:), a2(:)

!  Locals
   integer :: lb(1), ub(1)  ! Lower and Upper bounds
   integer :: i
   real :: a

!  Get bounds
   lb = lbound(a1)
   ub = ubound(a1)

!  Swap
   do i = lb(1), ub(1)
      a = a1(i)
      a1(i) = a2(i)
      a2(i) = a
   end do

end subroutine swap_real