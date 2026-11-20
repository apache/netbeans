@extends('my.layout')

<div>
    {{-- BLADE COMMENT --}}
    @if ($test)
        {{-- REGULAR ECHO --}}
        {{ $x }}
    
        {{-- RAW ECHO --}}
        {!! $x !!}
    @endif

    @verbatim
        <div>{{escaped}}</div>
    @endverbatim    
</div>