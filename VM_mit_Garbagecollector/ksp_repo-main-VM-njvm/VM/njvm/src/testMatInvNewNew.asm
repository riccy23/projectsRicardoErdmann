.vers 7

_main:
    asf     1
    pushc   3
    newa
    popg    0

    pushg   0
    pushc   0
    pushc   42
    putfa

    pushg   0
    pushc   1
    pushc   84
    putfa

    pushg   0
    pushc   2
    pushc   126
    putfa

    pushg   0
    pushc   0
    getfa
    wrint

    pushg   0
    pushc   1
    getfa
    wrint

    pushg   0
    pushc   2
    getfa
    wrint

    rsf
    halt

