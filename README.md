# CoralCove (JavaFX)

Offline resort-group desk. Same rules and SQLite schema as the Swing CoralCove app. This tree is separate and does not change that project.

## Run

JDK 21 or newer. This machine has JDK 23, and the sources compile with `--release 21`. From this directory, if Maven is not on your PATH:

```
mvnw.cmd javafx:run
```

If `mvn` is already installed, `mvn javafx:run` does the same thing.

The window title is `CoralCove | Resort Management`. The database is `data/coralcove.db` next to the working directory, unless `CORALCOVE_DB` is set. The log is `data/coralcove.log`.

Seeded login: `admin` / `Admin@123`. The sign-in screen then asks for a new password (8 characters, a letter, and a digit) before the desk opens. Closing that step returns to sign-in.

## Test

```
mvnw.cmd test
```

Tests use a temporary database and do not open a window.

## Package

`mvn javafx:jlink` is not the supported package step. The app is not a Java module, and SQLite JDBC stays on the classpath. Run it with `mvnw.cmd javafx:run` from this directory so `data/` is created here.
