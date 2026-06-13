@extends('my.layout')

<div>
    {{-- BLADE COMMENT --}}
    @if ($test)
        {{-- REGULAR ECHO --}}
        {{ $x }}
    
        {{-- RAW ECHO --}}
        {!! $x !!}
    @endif

    <x-alert :name="warning">
        
    </x-alert>
    
    @custom('local')
    
    @verbatim
        <div>{{escaped}}</div>
    @endverbatim    
</div>
