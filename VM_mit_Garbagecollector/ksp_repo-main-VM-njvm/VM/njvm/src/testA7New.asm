
.vers 7
__start:
    call _main
    halt

_main:
    asf 0
    new 2       
    popl -1      
    pushl -1     
    pushn        
    putf 0       
    rsf
    ret

