           PID: 421957 (HytaleClient)
           UID: 1000 (pj)
           GID: 1000 (pj)
        Signal: 6 (ABRT)
     Timestamp: Sun 2026-05-10 17:45:31 CEST (50s ago)
  Command Line: /home/pj/.var/app/com.hypixel.HytaleLauncher/data/Hytale/install/pre-release/package/game/latest/Client/HytaleClient --app-dir /home/pj/.var/app/com.hypixel.HytaleLauncher/data/Hytale/install/pre-release/package/game/latest --user-dir /home/pj/.var/app/com.hypixel.HytaleLauncher/data/Hytale/UserData --java-exec /home/pj/.var/app/com.hypixel.HytaleLauncher/data/Hytale/install/pre-release/package/jre/latest/bin/java --auth-mode authenticated --uuid 005d9c29-71e0-46ae-93ef-a3f61a0d9783 --name Patzifista
    Executable: /home/pj/.var/app/com.hypixel.HytaleLauncher/data/Hytale/install/pre-release/package/game/latest/Client/HytaleClient
 Control Group: /user.slice/user-1000.slice/user@1000.service/app.slice/app-flatpak-com.hypixel.HytaleLauncher-2398451425.scope
          Unit: user@1000.service
     User Unit: app-flatpak-com.hypixel.HytaleLauncher-2398451425.scope
         Slice: user-1000.slice
     Owner UID: 1000 (pj)
       Boot ID: 761882f8da264a1c9ce95ca30b57a0f6
    Machine ID: 5a1225b41ddb4cc0a10fdfc390077c82
      Hostname: HoloChan
       Storage: /var/lib/systemd/coredump/core.HytaleClient.1000.761882f8da264a1c9ce95ca30b57a0f6.421957.1778427931000000.zst (present)
  Size on Disk: 2.1G
       Message: Process 421957 (HytaleClient) of user 1000 dumped core.

                Module libsodium.so without build-id.
                Stack trace of thread 106:
                #0  0x00007fb9fc36b344 n/a (/usr/lib/x86_64-linux-gnu/libc.so.6 + 0x9a344)
                #1  0x00007fb9fc31250e n/a (/usr/lib/x86_64-linux-gnu/libc.so.6 + 0x4150e)
                #2  0x00007fb9fc2f9882 n/a (/usr/lib/x86_64-linux-gnu/libc.so.6 + 0x28882)
                #3  0x00007faa69f30ef0 n/a (/usr/lib/x86_64-linux-gnu/GL/default/lib/libgallium-25.3.3.so + 0xb30ef0)
                #4  0x00007faa69f350e0 n/a (/usr/lib/x86_64-linux-gnu/GL/default/lib/libgallium-25.3.3.so + 0xb350e0)
                #5  0x00007faa69a05201 n/a (/usr/lib/x86_64-linux-gnu/GL/default/lib/libgallium-25.3.3.so + 0x605201)
                #6  0x00007faa69a3e66c n/a (/usr/lib/x86_64-linux-gnu/GL/default/lib/libgallium-25.3.3.so + 0x63e66c)
                #7  0x00007fb9fc369261 n/a (/usr/lib/x86_64-linux-gnu/libc.so.6 + 0x98261)
                #8  0x00007fb9fc3edd54 n/a (/usr/lib/x86_64-linux-gnu/libc.so.6 + 0x11cd54)
                ELF object binary architecture: AMD x86-64