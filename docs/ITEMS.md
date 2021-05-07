# Plans for MTr item transfer

## Simulated inventory
Will need to simulate pending operations for an `IItemHandler`, and handle cases where the simulated operation fails later gracefully.

## Graph operations
I assume a Dijkstra from the source to collect all targets by distance will be enough.
That can be cached, I assume.

## Items travelling through the network
The only complex bit!
List of corner cases that must be dealt with:
* Pipe is broken with the item inside.
* Pipe in the path is broken, so the item will not be able to enter it later.
* Part of the network is unloaded.
* Target is broken.
* Insertion into the target fails.
* Pipe connection (to a connected pipe or inventory) is broken with an item inside.

