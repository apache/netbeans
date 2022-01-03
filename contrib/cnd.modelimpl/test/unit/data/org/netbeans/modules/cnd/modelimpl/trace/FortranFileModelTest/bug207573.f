module param
    parameter nu = 10
    real*8 U0, dUdx
    real*8 U(nu)
end module param

module mtr
    parameter (nx = 100)
    parameter nL = 102, nt = 8
    real*8 Nd(-nx - 1:nx + 1), x(-nx - 1:nx + 1)
    real*8 xL(nl), Yl(nl, nt), Y1(nl), ts(nt), xmax
    integer JF(nt), it, its
    real*8 Xx(50, nt), CSD(50, nt)
end module mtr

program main
end program main
