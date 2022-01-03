module module1
    integer :: n
contains
    recursive subroutine sub1(x)
        integer, intent(inout) :: x
        integer :: y
        y = 0
        if (x < n) then
            x = x + 1
            y = x**2
            print *, 'x = ', x, ', y = ', y
            call sub1(x)
            print *, 'x = ', x, ', y = ', y
        end if
    end
end

program main
    use module1
    integer :: x = 0
    print *, 'Enter number of repeats'
    read (*, *) n
    call sub1(x)
end program main
