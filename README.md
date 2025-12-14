# SpaceSignal

**Ein libGDX-basiertes Retro-Shooter-Spiel**  
Entwickelt im Rahmen des Moduls *Programmieren 1* an der Hochschule Bielefeld (HSBI).

**SpaceSignal** ist ein 2D-Retro-Shooter im Stil von *Space Invaders*, erweitert um vier kreative und abwechslungsreiche Spielmodi. Jeder Modus nutzt unterschiedliche Mechaniken, demonstriert dabei aber die Prinzipien der objektorientierten Programmierung (Vererbung, Polymorphie, Kapselung) und die Möglichkeiten des libGDX-Frameworks.

SpaceSignal ist ein isometrisches Pixel-Art-Spiel mit prozedural generiertem Terrain. Der Spieler steuert ein Raumschiff, sammelt verteilte Signale in einer dynamisch erzeugten Welt und erreicht durch das Einsammeln aller Signale das Spielziel. Das Projekt dient der Vertiefung von Kenntnissen in objektorientierter Programmierung mit Java sowie im Game Development mit der libGDX-Bibliothek.

Das Projekt wurde mit [gdx-liftoff](https://github.com/libgdx/gdx-liftoff) generiert und basiert auf dem Game-Development-Framework [libGDX](https://libgdx.com/).

## Spielbeschreibung

Das Spiel enthält vier einzigartige Modi:

- **Classic Mode**  
  Endloser Wellen-Modus mit zunehmender Schwierigkeit. Gegner spawnen in Wellen, der Spieler kann Upgrades (z. B. bessere Geschosse, Geschwindigkeit) einsammeln und versucht, möglichst lange zu überleben.

- **Boss Rush Mode**  
  Aufeinanderfolgende, immer stärkere Bosse mit Health-Bar und speziellen Angriffsmustern. Ziel: Alle Bosse besiegen und den finalen Sieg erreichen.

- **Flappy Mode**  
  Kreative Mischung aus Shooter und Flappy-Bird. Der Spieler fliegt durch Hindernisse (Pipes), kann nicht schießen, sammelt aber Power-Ups und muss Kollisionen vermeiden.

- **Asteroid Dodger Mode**  
  Freie 360°-Bewegung (WASD/Pfeiltasten). Asteroiden kommen aus allen Richtungen mit verschiedenen Verhaltenstypen (Normal, Spinning, Homing, Bouncing). Power-Ups (Schild, Slow-Motion, Score-Multiplier), Combo-System und exponentiell steigende Schwierigkeit sorgen für intensives Gameplay.

Alle Modi teilen eine gemeinsame Basisarchitektur (`GameMode`-Superklasse) und wiederverwendbare Komponenten (Player, Bullet, AssetManager, AudioManager).

## Voraussetzungen

- **Java Development Kit (JDK)**: Version 17 oder höher (empfohlen: OpenJDK oder Oracle JDK)
- **Gradle**: Wird über den enthaltenen Gradle-Wrapper bereitgestellt – keine separate Installation erforderlich
- **IDE** (optional, aber empfohlen): IntelliJ IDEA, Android Studio oder Eclipse

## Projektstruktur

- **`core`**: Plattformunabhängiger Kern mit der gesamten Spiel-Logik
- **`lwjgl3`**: Desktop-Plattform (Windows, macOS, Linux) basierend auf LWJGL 3

Weitere Plattformen (z. B. Android, iOS, Web) können bei Bedarf erweitert werden.

## Abhängigkeiten und Build-System

Das Projekt verwendet **Gradle** zur Verwaltung von Abhängigkeiten und zum Build-Prozess. Der Gradle-Wrapper (`gradlew` / `gradlew.bat`) ist im Repository enthalten.

## Installation und Ausführung

### 1. Repository klonen

```bash
git clone <Ihr-Repository-URL>
cd SpaceSignal
```

### 2. Projekt bauen
```bash
./gradlew build
```

### 3. Anwendung starten (Desktop)
```bash
./gradlew lwjgl3:run
```

### 4. Erzeugen einer ausführbaren JAR-Datei
```bash
lwjgl3/build/libs/SpaceSignal-lwjgl3.jar
```
# Die fertige JAR-Datei befindet sich anschließend unter:

```text
lwjgl3/build/libs/SpaceSignal-lwjgl3.jar
```
# Starten Sie die JAR mit:
```bash
java -jar lwjgl3/build/libs/SpaceSignal-lwjgl3.jar
```
