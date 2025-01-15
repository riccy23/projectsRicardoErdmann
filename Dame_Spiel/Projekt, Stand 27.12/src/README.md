# <span style="color:#00a1ff">Poke</span><span style="color:#006400">Checkers</span>
***
Erlebe mit PokeCheckers eine aufregenden Kombination aus klassischer Dame und der beliebten Pokémon-Welt! Stelle deine strategischen Fähigkeiten gegen Freunde auf die Probe. Schlage die gegnerischen Pokémon und erlange die Weiterentwicklung deiner Pokémons.  Kämpfe um den Titel des Pokémon-Dame-Meisters! Viel Spaß !

## <span style="color:lightgrey">Screenshots des Spiels </span>
### <span style="color:lightgrey">Startbildschirm (Menü) </span>
<img src="Startscreen.png">

### <span style="color:lightgrey">Ausgangsstellung </span>
<img src="Local.png">

### <span style="color:lightgrey">Markierung wenn Spieler 1 zieht </span>
<img src="SchiggyBlueFields.png">

### <span style="color:lightgrey">Markierung wenn Spieler 2 zieht </span>
<img src="BisasamGreenFields.png">

### <span style="color:lightgrey">Dame des Spieler 1 (Schiggy) </span>
<img src="SchiggyQueen.png">

### <span style="color:lightgrey">Dame des Spieler 2 (Bisasam) </span>
<img src="BisasamQueen.png">

### <span style="color:lightgrey">GameOver-Bildschirme </span>
<img src="GameOverSchiggy.png">
<img src="GameOverBisasam.png">

### <span style="color:lightgrey">Bildschirm des Spieler 2 bevor Spieler 1 das Spiel startet </span>
<img src="Waitingscreen.png">

### <span style="color:lightgrey">Online Modus</span>
<img src="ClientServer.png">

- - -

## <span style="color:lightgrey">Starten der Anwendung</span>
Damit sie die Anwendung starten können, müssen Sie folgende Schritte befolgen: 

- Den Ordner `Projekt` öffnen

<br>

- Den Ordner `src` öffnen

<br>

- Die Datei `MainMain.java` öffnen

<br>

- Je nach IDE ihrer Wahl müssen Sie nun innerhalb der `MainMain.java` den Run-Button finden und drücken um die Programmausführung zu starten.

<br>

- Um zwei Anwendungen für den Online Modus zu starten befolgen Sie folgende Schritte (Diese Schritte können je nach IDE differieren) :

<br>

Schritt 1: Auf die drei Punkte, dann auf "Edit"

  <img src="step1.png">

<br> 

Schritt 2: Auf "Modify options"

  <img src="step2.png">

<br>

Schritt 3: "Allow multiple instances" auswählen

  <img src="step3.png">

<br> 

Schritt 4: Auf "Apply" drücken

  <img src="step4.png">

---

## <span style="color:lightgrey">Entscheidung gegen Schlagzwang</span>
Ich habe mich in meinem Damespiel bewusst gegen die Implementierung des Schlagzwangs entschieden , da dies in meinen Augen, die taktische Freiheit der Spieler einschränkt. Durch das Weglassen des Schlagzwangs wird den Spielern ermöglicht  strategische Stellungen aufzubauen, ohne von zwingenden Schlagoptionen eingeschränkt zu werden, was im Endeffekt, in meinen Augen, für ein angenehmeres Spielerlebnis sorgt.

---

## <span style="color:lightgrey"> Ausführung in der JShell  </span>
Um das Spiel in der `JShell` auszuführen befolgen Sie folgende Schritte: 

<br>

- Den Ordner `Projekt` öffnen
  
<br>

- Die Datei `Copy/Paste für Terminal (JShell)` öffnen
 
<br>

- Mit `strg + A` und `strg + C` alles rauskopieren

<br>

- Terminal der Wahl öffnen und mit `jshell` die JShell öffnen

<br>

- Mit `/edit` das Editierfenster öffnen
 
<br>

- Mit `strg + V` alles in das Editierfenster einfügen

<br>

- Dann auf den Button `Accept` danach auf `Exit`

<br>

- Im Terminal dann `Model model = new Model(1,1);` eingeben um ein Model-Objekt zu erstellen

<br>

- Ein mal nun `model` eingeben um das Spielbrett in voller Größer darzustellen

<br>


 -     Das Spielbrett kann man sich so vorstellen, entsprechend trägt man auch die Koordinaten für einen Zug ein:

          | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
        --|---|---|---|---|---|---|---|---|
        0 | - | - | - | - | - | - | - | - |
        1 | - | - | - | - | - | - | - | - |
        2 | - | - | - | - | - | - | - | - |
        3 | - | - | - | - | - | - | - | - |
        4 | - | - | - | - | - | - | - | - |
        5 | - | - | - | - | - | - | - | - |
        6 | - | - | - | - | - | - | - | - |
        7 | - | - | - | - | - | - | - | - |

  



<br>

- Nun kann man mit folgendem Befehl Züge ausführen (beachte, dass Spieler 1 anfängt, dann -1 usw.):
  `model.getMove().makeMove(model.getBoard(), spieler, new Position(start.x,start.y), new Position(ziel.x,ziel.y));`

<br>

- Nach jedem Zug muss mit `model` wieder das aktuelle Spielbrett ausgegeben werden


## <span style="color:lightgrey">Alle verwendeten Quellen und Bibliotheken</span>
### Bibliotheken:

- [Processing](https://processing.org/reference/libraries/)
- [JUnit](https://junit.org/junit5/)

### Quellen:

- Alle Pokemon Bilder sind von der offiziellen Pokémon Website: https://www.pokemon.com/de/pokedex
- Bilder aus dem Start- und GameOver-Bildschirm wurden mit dem DALLE - Tool von Chat-GPT 4 generiert: https://chat.openai.com