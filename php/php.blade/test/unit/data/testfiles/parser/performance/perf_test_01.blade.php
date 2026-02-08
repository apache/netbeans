@extends('layout.layout')
<div>
    @if (count($records) === 1)
    I have one record!
    @elseif (count($records) > 1)
    I have multiple records!
    @else
    I don't have any records!
    @endif
</div>

@sectionMissing('navigation')
    <div class="pull-right">
        @include('default-navigation')
    </div>
@endif

@for ($i = 0; $i < 10; $i++)
    The current value is {{ $i }}
@endfor
 
@foreach ($users as $user)
    <p>This is user {{ $user->id }}</p>
@endforeach
 
@forelse ($users as $user)
    <li>{{ $user->name }}</li>
@empty
    <p>No users</p>
@endforelse
 
@while (true)
    <p>I'm looping forever.</p>
@endwhile
    
@php
    $isActive = false;
    $hasError = true;
@endphp
 
<span @class([
    'p-4',
    'font-bold' => $isActive,
    'text-gray-500' => ! $isActive,
    'bg-red' => $hasError,
])></span>
 
<span class="p-4 text-gray-500 bg-red"></span>

<div>
    @include('shared.errors')
 
    <form>
        <!-- Form Contents -->
    </form>
</div>

@once
    @push('scripts')
        <script>
            // Your custom JavaScript...
        </script>
    @endpush
@endonce
        
@use('App\Models\Flight', 'FlightModel')
