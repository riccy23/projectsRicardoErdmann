.vers 7

_main:
    asf     1
    pushc   3
    newa
    popg    0

    pushg   0
    pushc   0
    pushc   10
    putfa

    pushg   0
    pushc   1
    pushc   20
    putfa

    pushg   0
    pushc   2
    pushc   30
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

