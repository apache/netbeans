module module1
implicit none

contains

function myadd(a, b)
real(8) myadd
real(8) a, b
myadd=a + b
end function myadd

subroutine mytrig(x, sinx, cosx, tanx)
real(8) x
real(8) sinx, cosx, tanx
sinx = sin(x)
cosx = cos(x)
tanx = tan(x)
end subroutine mytrig

end module module1