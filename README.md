# Formula One Database

A desktop Formula One season simulator written in Java. It uses a Swing interface, a bundled SQLite database, and a small simulation engine for qualifying and race weekends. Season progress, qualifying attempts, race classifications, and lap times are saved locally.

## Features

- Browse the drivers, teams, circuits, and sponsors stored in the project database.
- Create and manage up to three season save slots.
- Generate a season calendar from the Grand Prix entries in the database.
- Simulate qualifying over Q1, Q2, and Q3, save lap attempts, and build a starting grid.
- Simulate race lap times and overtakes, then save the finishing order, points, fastest lap, and lap history.
- View driver and team standings, plus saved race classifications and lap details.

## Requirements

- A Java Development Kit (JDK) with Swing support.
- The files and folders in this repository, including `lib/`, `assets/`, and `F1Database.sqlite3`.

The project has no Maven or Gradle build file. The repository includes the Java dependencies it uses:

| File | Purpose |
| --- | --- |
| `lib/sqlite-jdbc-3.53.4.0.jar` | Connects Java to SQLite. |
| `lib/webp-imageio-0.11.0.jar` | Adds WebP image support for image assets. |
| `lib/kotlin-stdlib-2.4.0.jar` | Runtime dependency used by the WebP image library. |

## Run in Visual Studio Code

1. Open the project root folder in Visual Studio Code with the Java extension installed.
2. Ensure the bundled libraries are present under `lib/` and the database is present at the project root.
3. Select **Run F1 UI** from the Run and Debug configuration list, or press **F5**.

The checked-in VS Code configuration compiles sources from `src/` into `bin/` and launches `app.App`.

## Build and run from a terminal

Run these commands from the project root so the relative database and asset paths resolve correctly.

On macOS or Linux:

```sh
mkdir -p bin
javac -cp 'lib/*' -d bin $(find src -name '*.java')
java -cp 'bin:lib/*' app.App
```

On Windows PowerShell, use `;` as the classpath separator:

```powershell
$sources = Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object { $_.FullName }
New-Item -ItemType Directory -Force bin | Out-Null
javac -cp "lib/*" -d bin $sources
java -cp "bin;lib/*" app.App
```

## Data and saves

The application connects to `jdbc:sqlite:F1Database.sqlite3`. This is a relative path, so launch the program with the project root as its working directory. The supplied database contains the reference data and base tables the application expects; an empty SQLite file is not a substitute for it.

The database includes reference tables for teams, drivers, circuits, Grand Prix events, sponsors, and performance ratings. Season data is stored separately in save slots, seasons, and season events. Qualifying and race tables retain the session attempts and per-lap results needed to resume or review a season.

There are three save slots. Starting a season stores an ordered list of the current Grand Prix calendar in that slot. Deleting a season removes its events and their saved results through the database's cascading relationships. Slot names and reference data are separate from season progress.

On its first connection, `database.Database` enables SQLite foreign-key enforcement and applies limited compatibility updates: it ensures `save_slots.last_played` exists, seeds slots 1–3 if needed, and rebuilds an older `race_results` table if `result_status` is still required. These updates do not create the full application schema or populate reference data.

## How the simulation works

### Qualifying

Qualifying advances in order from Q1 to Q2 to Q3. Q1 uses all drivers; the fastest 16 advance to Q2, and the fastest 10 from Q2 advance to Q3. Each attempt is calculated from the circuit qualifying benchmark and a weighted combination of team pace, driver pace, and driver experience. Random variation affects both the driver's effective rating and individual lap times. Q1 and Q2 drivers receive two to four simulated laps; Q3 drivers receive one or two.

The best lap determines session order. Attempts are stored by session, which allows the next session to be selected from saved state. Once Q3 is saved, the application combines Q3 finishers with drivers eliminated in Q2 and Q1 to create the final classification and race grid.

### Race

A race requires a qualifying grid. Each driver receives baseline pace, lap-time variation, and an overtaking rating based on the circuit, driver attributes, and team ratings. The simulator generates a lap for every driver and then checks adjacent cars for successful passes. A faster lap gives the following driver a chance to overtake; racecraft and the size of the lap-time advantage affect that chance.

Finishing positions award points to the top ten: 25, 18, 15, 12, 10, 8, 6, 4, 2, and 1. Other classified finishers receive zero points. Fastest lap is saved for display but does not currently award an extra point. The race repository saves the result rows and lap times together, marks the event complete, and completes the season after its final event. The simulator currently classifies every simulated driver as finished; the database also supports `DNF` and `DSQ` statuses for result records.

### Standings and results

Driver standings total each driver's points from finished races in the selected season. Team standings combine points earned by drivers on the team. Every entry receives a distinct sequential position; when points are tied, names are ordered alphabetically without regard to case. Results queries read stored classifications and derive lap counts, total race times, and best laps from the lap records.

## Project structure

```text
src/
  app/          Application entry point
  database/     SQLite connection and compatibility updates
  gui/          Swing screens and UI components
  model/        Domain and result data objects
  repository/   SQL queries and persistence
  service/      Season, qualifying, race, and standings workflows
assets/         Logos, portraits, flags, and other interface images
lib/            Bundled Java dependencies
F1Database.sqlite3
```

### Main packages

- `app.App` loads the shared simulation data and starts the Swing window on the event-dispatch thread.
- `model` holds the objects passed between the interface, services, and repositories, such as `Driver`, `RaceResult`, and `WeekendEvent`.
- `repository` owns SQL access. Each repository opens connections through `database.Database` and maps database rows to model objects.
- `service` coordinates application workflows. For example, `NextQualifyingSessionService` decides which qualifying phase runs next, while `RaceService` checks the grid, simulates the race, calculates points, and asks `RaceRepository` to save it.
- `gui` contains the Swing interface. `assets` contains the image files referenced by the application.

## Troubleshooting

- **SQLite driver is missing:** make sure `lib/sqlite-jdbc-3.53.4.0.jar` is on the runtime classpath. The supplied launch configuration already includes it.
- **The database appears empty or a table is missing:** confirm that the working directory is the project root and that `F1Database.sqlite3` is present. Compatibility updates only handle a small number of schema changes; they do not initialize a new database.
- **Images do not appear:** run from the project root and keep the `assets/` directory in place. WebP images require the bundled ImageIO plugin and its Kotlin runtime dependency.
- **The project does not compile:** confirm a JDK is selected in the Java extension and that the `lib/` dependencies have not been moved or renamed.

## Product notes

`F1 Simulator PRD.pdf` contains the product requirements document associated with the project.
