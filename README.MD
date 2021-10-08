
## Futureskill Apple Picker exercise 
* https://app.futureskill.com/exercise/5e7cda6beaf1b82735e5f1a0

### Math for developer
* https://www.youtube.com/playlist?list=PLW3Zl3wyJwWOpdhYedlD-yCB7WQoHf-My

### Game information
```
Example state
{apples=(2,4), player=(2,2), bushes=(2,0), (1,2), (4,2), (3,3), (3,4)}

directional vectors
NW (-1,0)
NE (0,-1)
SW (0,1)
SE (1,0)

      /\
   NW/ \NE
    /  \
    \  /
  SW\ /SE
    \/

5x5 board
               NE
    (0,0) (1,0) (2,0) (3,0) (4,0)
    (0,1) (1,1) (2,1) (3,1) (4,1)
 NW (0,2) (1,2) (2,2) (3,2) (4,2) SE
    (0,3) (1,3) (2,3) (3,3) (4,3)
    (0,4) (1,4) (2,4) (3,4) (4,4)
              SW
```

```
/**
 * Executes a single step of the apple pickers logical loop. This
 * function will be called repeatedly until the levels goal has been
 * reached or untill the update limit has been reached
 * {apples=(2,4), player=(2,2), bushes=(2,0), (1,2), (4,2), (3,3), (3,4)}
 */
/*
 */
```
#### Level 7
```
{
    level_name=Non-linear walk, 
    level=7, 
    board_height=5, 
    hint=Walk over to the apples and pick them up! You can move one tile per update cycle. You have to get all the apples!, 
    board_width=5, 
    obstacles=(0,0)
}
```
#### Level 8
```
{
    level_name=Avoid obstacles, 
    level=8, 
    board_height=7, 
    hint=Walk over to the apples and pick them up! You can move one tile per update cycle. There are some obstacles that you have to walk around., 
    board_width=3, 
    obstacles=(0,0), (1,2), (1,3), (0,5), (1,5), (0,3)
    
    {apples=(2,0), (0,2), (0,4), player=(0,6), bushes=(2,1)}
}
```


