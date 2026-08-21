# This is just a current info about what is happening behind the curtain.

1. Trying first to remove the recursive Insert delete from the AVL,RBT,Treap may be possible but I need to benchmark that too.: Cancelled Not used I used Iterative approach but the stack of JVM is faster than the iterative Stack I used. The reason can be seen from commit [0dde5c8](https://github.com/Chaos-vy/ChaosTree/commit/6e31f43b2f847e5ae04ffebd4aab8dd393dc23d8)
2. Working on Map
3. Still learning so it will take time
4. Major changes with exception and Benchmark will be seen from 2.0.0
5. Revised Test will be made.

## Changes Made till now

1. Removed delete all DSA mind logic LOL!.
> After changing that I ended up on my test failure where I still used DSA logic going to change it or do I first revise then change the Test I think I should do All!
2. So I also removed the DSA logic of mergeAll also -> the test were passed.
3. All The Test coverage of BinaryTree on Emptiness has been refactored
4. 21 aug Major bug fix: clear did not update the rolling hashcode value, equals after clear() call did not work which was breaking java contract.
