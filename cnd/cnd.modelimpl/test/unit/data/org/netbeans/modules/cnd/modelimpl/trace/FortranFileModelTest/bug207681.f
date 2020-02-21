subroutine CSDcalc(iw)
    use mtr
    use param
    implicit real*8 (A - H, O - Z)
    integer(8) iii

    x(  -nx - 1) = -Xmax
    hx = Xmax/(nL - 1)

    do i = -nx, nx + 1
        x(i) = x(i - 1) + hx
    end do

    Nd = Yl(1, its)
    do i = 0, nx + 1
        Nd(i) = Yl(i + 1, its) !Initial conditions
    end do
    nstep = 1000
    ii = 0
    tfin = ts(it)
    itim = 1
    !   write(*, *) ts(its), ts(it)
    dt = (tfin - ts(its))/nstep
    do t = ts(its), tfin, dt
        do i = -nx + 1, nx + 1
            x(i) = x(i) + Uu(max(x(i), 0d0), t) * dt
            if (x(i) .gt. 0) then
                Nd(i) = Nd(i)/(1d0 + dUdx * dt)
            else
                Nd(i) = exp(Znn(t))
            end if
            if (mod(ii, 999) .eq. 0 .and. iw == 1) write(12, '(I5,2e15.5)') i, x(i), log(Nd(i)) !.and. x(i) >= 0  !Nd(i).gt.0d0.and.
        end do
        if (mod(ii, 999) .eq. 0.and.iw == 1) write(12, *)
        if (mod(ii, 999) .eq. 0.and.iw == 1) write(12, *)
        ii = ii + 1
    end do
endsubroutine CSDcalc

real*8  function tt(z, x0, x1, x2, y0, y1, y2)
    implicit real*8 (a - h, o - z)
    tt = (z - x1)*(z - x2) * y0/(x0 - x1)/(x0 - x2)+(z - x0)*(z - x2) * y1/(x1 - x0)/(x1 - x2)+(z - x0)*(z - x1) * y2/(x2 - x0)/(x2 - x1)
end function tt

real*8  function dtt(z, x0, x1, x2, y0, y1, y2)
    implicit real*8 (a - h, o - z)
    t1 = y1 - y2
    t2 = x0**2
    t7 = y2 - y0
    t8 = x1**2
    dtt = (t1 * t2 - 2 * z * t1 * x0 + t7 * t8 - 2 * z * t7 * x1 - 2 * x2 * (-x2/2 + z)*(y0 - y1))/(x0 - x1)/(x0 - x2)/(x1 - x2)
end function dtt

subroutine read_data
    use MTR
    use param
    implicit real*8 (A - H, O - Z)
    character*190 str
    character*2 c1
    Yl(:, 0) = -100d0
    ts(0) = 0
    nm = 10
    read(12, '(A100)', end = 111) str
    read(12, '(A100)', end = 111) str
    read(str, *, end = 111) c1, (ts(i), i = 1, nt)
    !   write(*,'(10i10)')0,int(ts)
    do i = 1, nm
        read(12, *, end = 111) xx(i), (CSD(i, j), j = 1, nt)
        !       write(*,'(10f10.3)')xx(i),(CSD(i,j),j=1,nt)
    end do
    do j = 1, nt
        do i = 1, nm
            if (CSD(i, j) .ne. - 99.0d0) then
                x1 = xx(i)
                x2 = xx(i + 1)
                exit
            end if
        end do
        ii = 1
        do while (xl(ii) < x1) !linear interpolation to 0
            Yl(ii, j) = CSD(i, j)+(CSD(i + 1, j) - CSD(i, j))*(xl(ii) - x1)/(x2 - x1)
            !           write(*,*)ii,xl(ii),Yl(ii,j)
            ii = ii + 1
        end do

        do k = ii, nl
            do ix = i, nm
                if (xl(k) >= xx(ix - 1).and.xl(k) < xx(ix)) exit
            end do
            if (ix < nm + 1.and.CSD(ix, J) .ne. - 98d0) then
                Yl(k, j) = CSD(ix - 1, j)+(CSD(ix, j) - CSD(ix - 1, j))*(xl(k) - xx(ix - 1))/(xx(ix) - xx(ix - 1))
            else
                Yl(k, j) = -100d0
            end if
            !           write(*,*)k,xl(k),Yl(k,j)
        end do
    end do
    return
    111 continue
    write(*, *) 'error reading files'
end subroutine read_data

real*8  function Zn(t)
    use mtr
    implicit real*8 (A - H, O - Z)
    Zn = Yl(1, its)+(Yl(1, it) - Yl(1, its))*(t - ts(its))/(ts(it) - ts(its))
end function Zn

real*8  function Znn(t)
    use param
    use mtr
    implicit real*8 (A - H, O - Z)
    Jz = 1 + (nj - 1) * (t - tn0(1))/(tn0(nj) - tn0(1))

    J1 = Jz - 1
    J2 = Jz
    J3 = Jz + 1
    if (Jz == nj) then
        J1 = Jz - 2
        J2 = Jz - 1
        J3 = Jz
    end if

    if (Jz == 1) then
        J1 = Jz
        J2 = Jz + 1
        J3 = Jz + 2
    end if
    Znn = tt(t, tn0(J1), tn0(j2), tn0(J3), Zn0(J1), Zn0(J2), Zn0(J3))
end function Znn

real*8  function Uu(z, t)
    use param
    use mtr
    implicit real*8 (A - H, O - Z)
    Jz = 1 + (nj - 1) * (t - tn0(1))/(tn0(nj) - tn0(1))
    J1 = Jz - 1
    J2 = Jz
    J3 = Jz + 1
    if (Jz == nj) then
        J1 = Jz - 2
        J2 = Jz - 1
        J3 = Jz
    end if

    if (Jz == 1) then
        J1 = Jz
        J2 = Jz + 1
        J3 = Jz + 2
    end if
    Ft = tt(t, tn0(J1), tn0(j2), tn0(J3), FtU(J1), FtU(J2), FtU(J3))

    Jz = 1 + (nu - 1) * z/xmax
    J1 = Jz - 1
    J2 = Jz
    J3 = Jz + 1
    if (Jz == nu) then
        J1 = Jz - 2
        J2 = Jz - 1
        J3 = Jz
    end if

    if (Jz == 1) then
        J1 = Jz
        J2 = Jz + 1
        J3 = Jz + 2
    end if

    if (z < xmax) then
        Uu = Ft * tt(z, xu(J1), xu(j2), xu(J3), U(J1), U(J2), U(J3))
        dUdx = Ft * dtt(z, xu(J1), xu(j2), xu(J3), U(J1), U(J2), U(J3))
    end if
end function Uu

subroutine fsq(Xp, F)
    use param
    use mtr
    implicit real*8 (a - h, o - z)
    real*8 Xp(nu + 2 * nj)
    U = Xp(1:nu)
    FtU = Xp(nu + 1:nu + nj)
    Zn0 = Xp(nu + nj + 1:nu + 2 * nj)
    call CSDcalc(0)

    F = 0.0d0

    do j = 1, nl
        iok = 0
        do i = -nx, nx
            if (x(i - 1) <= xl(j).and. x(i) > xl(j)) then
                Csdc = (log(Nd(i - 1))+(log(Nd(i)) - log(Nd(i - 1)))*(xl(j) - x(i - 1))/(x(i) - x(i - 1)))
                F = F + sqrt((log(Yl(j, it)) - Csdc)**2)/nl !correct later
                iok = 1
            end if
        end do
        if (iok == 0) F = F + sqrt(log(Yl(j, it)))/nl
    end do


    write(117, *)
    write(*, "(A5,F10.5)") 'sq=', F
    if (abs(F - 0.023) < 0.001) then
        do i = -nx, nx
            write(102, *) x(i), log(Nd(i))
        end do
        write(102, *)
        write(102, *)

        do i = 1, nl
            write(102, *) xl(i), log(Yl(i, it))
        end do

!        write(*, *) Xp
        open(122, file = 'par' // fn // '.txt')
        write(122, '(100e15.7)') Xp
        close(122)

        !stop
    end if
    !  write(*,'(5e12.5)')Zn0
    return

end subroutine fsq

subroutine fcons(x, fc)
    use param
    double precision fc, s
    double precision x(nu + 2 * nj)

    fc = 0.d0
    do i = 1, nu
        ! Nonnegative values:
        s = -x(i) !max(-x(i) - 0.01, x(i) - 0.025)
        if (s .gt. fc) then
            fc = s
        endif
    enddo
end subroutine fcons