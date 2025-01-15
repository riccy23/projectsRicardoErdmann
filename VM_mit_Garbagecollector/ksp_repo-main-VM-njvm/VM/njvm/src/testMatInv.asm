//
// version
//
        .vers   7

//
// execution framework
//
__start:
        call    _main
        halt

//
// void main()
//
_main:
        asf     0           // Beginne Stack-Frame

        // Test 1: Array korrekt erstellen und beschreiben
        pushc   3           // Array mit 3 Feldern erstellen
        newa                // Neues Array erstellen
        dup                 // Array auf Stack duplizieren
        pushc   0           // Index 0
        pushc   42          // Wert 42
        putfa               // Schreibe Wert in Index 0
        dup                 // Array erneut duplizieren
        pushc   1           // Index 1
        pushc   43          // Wert 43
        putfa               // Schreibe Wert in Index 1
        dup                 // Array erneut duplizieren
        pushc   2           // Index 2
        pushc   44          // Wert 44
        putfa               // Schreibe Wert in Index 2

        // Test 2: Fehlerhafte Zugriffe (Index out of bounds)
        pushc   3           // Array mit 3 Feldern erstellen
        newa                // Neues Array erstellen
        dup                 // Array auf Stack duplizieren
        pushc   3           // Index 3 (außerhalb der Grenze)
        pushc   99          // Wert 99
        putfa               // Fehler: Index außerhalb der Grenze

        // Test 3: Nil-Referenz
        pushn               // Nil auf den Stack
        pushc   0           // Index 0
        pushc   77          // Wert 77
        putfa               // Fehler: Zugriff auf nil-Referenz

        rsf
        ret

