//
// version
//
        .vers   4

//
// execution framework
//
__start:
        call    _main
        call    _exit
__stop:
        jmp     __stop

//
// Integer readInteger()
//
_readInteger:
        asf     0
        rdint
        popr
        rsf
        ret

//
// void writeInteger(Integer)
//
_writeInteger:
        asf     0
        pushl   -3
        wrint
        rsf
        ret

//
// Character readCharacter()
//
_readCharacter:
        asf     0
        rdchr
        popr
        rsf
        ret

//
// void writeCharacter(Character)
//
_writeCharacter:
        asf     0
        pushl   -3
        wrchr
        rsf
        ret

//
// Integer char2int(Character)
//
_char2int:
        asf     0
        pushl   -3
        popr
        rsf
        ret

//
// Character int2char(Integer)
//
_int2char:
        asf     0
        pushl   -3
        popr
        rsf
        ret

//
// void exit()
//
_exit:
        asf     0
        halt
        rsf
        ret

//
// void main()
//
_main:
        asf     3
        pushc   0
        popl    2
        pushc   0
        popl    0
        jmp     __2
__1:
        pushc   0
        popl    1
        jmp     __5
__4:
        pushl   0
        pushl   1
        add
        pushc   2
        mod
        pushc   0
        eq
        brf     __7
        pushl   2
        pushc   1
        add
        popl    2
__7:
        pushl   1
        pushc   1
        add
        popl    1
__5:
        pushl   1
        pushc   5
        lt
        brt     __4
__6:
        pushl   0
        pushc   1
        add
        popl    0
__2:
        pushl   0
        pushc   5
        lt
        brt     __1
__3:
        pushl   2
        call    _writeInteger
        drop    1
        pushc   10
        call    _writeCharacter
        drop    1
__0:
        rsf
        ret
