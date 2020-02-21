@echo off
rem  - LABEL INDICATING THE BEGINNING OF THE DOCUMENT.
:begin
choice /N /C:123 pick a number (1, 2, or 3)%1
if errorlevel ==3 goto three
:end
