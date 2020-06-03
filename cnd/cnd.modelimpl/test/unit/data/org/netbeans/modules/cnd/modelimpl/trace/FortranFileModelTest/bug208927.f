subroutine test2(NN,r,s)
    implicit none
    
    real*8, intent(in)  :: r,s
    real*8 :: u
    real*8, intent(out) :: NN(3)
    
    u = 1. - r - s
    
    ! Comment with semicolon-> ;
    
    NN = (/ u, r,   &
         s /)
    
end