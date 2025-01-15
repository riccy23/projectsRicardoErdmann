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
        call    _readInteger
        pushr
        popl    0
        pushl   0
        pushc   1
        le
        brf     __1
        pushc   78
        call    _writeCharacter
        drop    1
        pushc   111
        call    _writeCharacter
        drop    1
        pushc   10
        call    _writeCharacter
        drop    1
        jmp     __0
__1:
        pushc   2
        popl    1
        pushc   1
        popl    2
        jmp     __3
__2:
        pushl   0
        pushl   1
        mod
        pushc   0
        eq
        brf     __5
        pushc   0
        popl    2
__5:
        pushl   1
        pushc   1
        add
        popl    1
__3:
        pushl   1
        pushl   1
        mul
        pushl   0
        le
        dup
        brf     __6
        drop    1
        pushl   2
__6:
        brt     __2
__4:
        pushl   2
        brf     __7
        pushc   89
        call    _writeCharacter
        drop    1
        pushc   101
        call    _writeCharacter
        drop    1
        pushc   115
        call    _writeCharacter
        drop    1
        pushc   10
        call    _writeCharacter
        drop    1
        jmp     __8
__7:
        pushc   78
        call    _writeCharacter
        drop    1
        pushc   111
        call    _writeCharacter
        drop    1
        pushc   10
        call    _writeCharacter
        drop    1
__8:
__0:
        rsf
        ret
