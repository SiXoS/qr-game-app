# QR Game Android App

This is the Android App for parsing and executing QR games.
See [QR Game](https://github.com/SiXoS/qr-game) for more information.

## Security

The app has some security measures in place to ensure a good user experience.
The language itself is input/output safe so the only issue we have to worry about
is resources. The applications employs the following security measures:

1. The game is terminated if a program iteration, or game loop,
takes more than a second to run.
2. TODO: If a game has a very low frame rate it is terminated.
3. TODO: If the system signals very low memory, the game is terminated.

