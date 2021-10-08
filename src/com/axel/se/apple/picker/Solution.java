package com.axel.se.apple.picker;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class APICaller {

  public Map<String,String> getStaticData() {
    return new Level8().getLevelData();
  }
}

interface SolutionInterface {
  String update(Map<String,String> state);
}

class Distance {
  final int distance;
  final Position position;

  public Distance(Position player, Position position) {
    this.distance = distance(player,position);
    this.position = position;
  }

  public int getDistance() {
    return distance;
  }

  public Position getPosition() {
    return position;
  }

  private int distance(Position p1, Position p2) {
    double distance = Math.sqrt(Math.pow(p2.x - p1.x,2)  + Math.pow(p2.y - p1.y,2));
    return (int) (distance * 1000);
  }
}

class Boundary {
  // boundaries
  // x >= 0 && x < boardWidth
  // y >= 0 && y < boardHeight
  //(0,boardWith-1) (boardHeight-1,0)

  public int boardWidth;
  public int boardHeight;

  public Boundary(int boardWidth, int boardHeight) {
    this.boardWidth = boardWidth;
    this.boardHeight = boardHeight;
  }
  public boolean inBoundary(Player player) {
    return player.position.x >= 0 && player.position.x < boardWidth
        && player.position.y >= 0 && player.position.y < boardHeight;
  }

  public boolean inBoundary(Position position) {
    return position.x >= 0 && position.x < boardWidth
        && position.y >= 0 && position.y < boardHeight;
  }
}

public class Solution implements SolutionInterface {

  public static final String PLAYER_KEY = "player";
  public static final String APPLES_KEY = "apples";
  public static final String BUSHES_KEY = "bushes";

  public static final String OBSTACLES_KEY = "obstacles";
  public static final String BOARD_WIDTH_KEY = "board_width";
  public static final String BOARD_HEIGHT_KEY = "board_height";

  private APICaller api;
  private int boardWidth;
  private int boardHeight;
  private Boundary boundary;

  public int getBoardWidth() {
    return boardWidth;
  }

  public int getBoardHeight() {
    return boardHeight;
  }

  public Boundary getBoundary() {
    return boundary;
  }

  public List<Position> obstacles = List.of();

  public Solution(APICaller api) {
    this.api = api;
    init();
  }

  public void init() {
    if (api.getStaticData() != null) {
      if (api.getStaticData().containsKey(OBSTACLES_KEY)) {
        obstacles = Position.parseTuplesToPoint(api.getStaticData().get(OBSTACLES_KEY));
      }
      if (api.getStaticData().containsKey(BOARD_WIDTH_KEY)) {
        boardWidth = Integer.parseInt(api.getStaticData().get(BOARD_WIDTH_KEY));
      }

      if (api.getStaticData().containsKey(BOARD_HEIGHT_KEY)) {
        boardHeight = Integer.parseInt(api.getStaticData().get(BOARD_HEIGHT_KEY));
      }

      if (boardHeight != 0 && boardWidth != 0) {
        this.boundary = new Boundary(boardWidth, boardHeight);
      }
    }
  }

  public static Command getDirection(Position player, Position apple,
      List<Position> obstacles,Boundary boundary) {

    int yDirection = apple.y-player.y;
    int xDirection = apple.x-player.x;

    if(player.y == apple.y) {
      if(xDirection > 0) {

        /*
        // TODO:  algorithm for obstacle detection
        Position next = player.move(new Position(-1, 0), boundary);
        while(obstacles.contains(next)) {
          next =  player.move(new Position(-1, 0), boundary);
        }
         */

        return Command.WALK_SOUTH_EAST;
      } else {
        return Command.WALK_NORTH_WEST;
      }
    } else {
      if(yDirection > 0) {
        return Command.WALK_SOUTH_WEST;
      } else {
        return Command.WALK_NORTH_EAST;
      }
    }
  }

  public String update(Map<String, String> state) {
    Position playerPosition = Position.parseTupleToPoint(state.get(PLAYER_KEY)).get();
    List<Position> applePositions = Position.parseTuplesToPoint(state.get(APPLES_KEY))
        .stream()
        .map((p) -> new Distance(playerPosition, p))
        .sorted((distance, t1) -> distance.getDistance() > t1.getDistance() ? 1 : 0)
        .map(Distance::getPosition)
        .collect(Collectors.toList());


    Queue<Position> appleQueue = new ArrayDeque<>(applePositions);
    if(applePositions.contains(playerPosition)) {
     return Command.PICK.getCommand();
    }

    var apple = appleQueue.poll();

    if(appleQueue.isEmpty()) {
      return Command.WIN.getCommand();
    }

    return getDirection(playerPosition,apple,obstacles,boundary).getCommand();
  }

  public static String listOfPositionsToString(List<Position> positions) {
    return positions.stream().map(Position::toString).collect(
        Collectors.joining(","));
  }

  public void updateState(Map<String,String> state,Player player,List<Position> applePositions,Boundary boundary) {
    if(!boundary.inBoundary(player)) {
      return;
    }
    state.put(PLAYER_KEY,player.position.toString());
    state.put(APPLES_KEY,listOfPositionsToString(applePositions));
  }

  public static void main(String[] args) {
    APICaller apiCaller = new APICaller();
    Map<String,String> state = new HashMap<>();

    Level8 level8 = new Level8();
    //{apples=(2,0), (0,2), (0,4), player=(0,6), bushes=(2,1)}
    state.put(APPLES_KEY, listOfPositionsToString(level8.getApplesList()));
    state.put(BUSHES_KEY, listOfPositionsToString(level8.getBushesList()));
    state.put(PLAYER_KEY,level8.getPlayerPosition().toString());

    Solution solution = new Solution(apiCaller);
    Optional<Position> startPosition = Position.parseTupleToPoint(state.get(PLAYER_KEY));

    Player player = new Player(startPosition.get());
    List<Position> applePositions = Position.parseTuplesToPoint(state.get(APPLES_KEY));
    boolean winFlag = false;

    // Game loop
    for(int i = 0; i < 70;i++) {
      String strCommand = solution.update(state);
      Command command = Command.translate(strCommand);
      if(command == Command.WIN) {
        winFlag = true;
        break;
      } else if(command == Command.PICK) {

        Position pickPosition = player.getPosition();
        boolean remove = applePositions.remove(pickPosition);
        if(remove) {
          GameLogger.pick(pickPosition);
        }

      } else {
        Position vectorAdd = Position.commandToPosition(command);
        GameLogger.walk(command, player, solution.getBoundary());
        player.move(vectorAdd, solution.getBoundary());
      }
      solution.updateState(state,player,applePositions,solution.getBoundary());
    }

    if(winFlag) {
      GameLogger.win();
    } else {
      GameLogger.loose();
    }
  }
}

class Player {

  public int numberOfApplesPicked = 0;

  public Position position;

  public Player(Position startPosition) {
    this.position = startPosition;
  }

  public void setPosition(Position position) {
    this.position = position;
  }

  public void incrementPickedApples() {
    numberOfApplesPicked++;
  }

  public void move(Position add,Boundary boundary) {
    this.position = position.move(add, boundary);
  }

  public Position getPosition() {
    return position;
  }
}

class GameLogger {
  public static void pick(Position pickPosition) {
    System.out.println("PICK apple at " + pickPosition);
  }

  public static void walk(Command command,Player player,Boundary boundary) {
    System.out.println("[" + command.getCommand() + "] Player " + player.getPosition() + " in boundary= " + boundary.inBoundary(player));
  }

  public static void win() {
    System.out.println("Winner all apples are picked");
  }

  public static void loose() {
    System.out.println("Looser you ran out of time!");
  }
}

enum Command {
  WALK_NORTH_EAST("walk 0"), // (0,-1)
  WALK_NORTH_WEST("walk 3"), // (-1,0)
  WALK_SOUTH_WEST("walk 2"), // (0,1)
  WALK_SOUTH_EAST("walk 1"), // (1,0)
  PICK("pick"),
  WIN("win"),
  UNKNOWN("unknown"),
  ;

  private String command;

  private Command(String command) {
    this.command = command;
  }

  public String getCommand() {
    return command.toString();
  }

  public static Command translate(String command) {
    switch (command) {
      case "win":
          return Command.WIN;
      case "walk 0":
        return Command.WALK_NORTH_EAST;
      case "walk 1":
        return Command.WALK_SOUTH_EAST;
      case "walk 2":
        return Command.WALK_SOUTH_WEST;
      case "walk 3":
        return Command.WALK_NORTH_WEST;
      case "pick":
        return Command.PICK;
      default:
        return Command.UNKNOWN;
    }
  }
}

class Position {
  public final int x;
  public final int y;
  public Position(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Position)) {
      return false;
    }

    Position position = (Position) o;

    if (x != position.x) {
      return false;
    }
    return y == position.y;
  }

  @Override
  public int hashCode() {
    int result = x;
    result = 31 * result + y;
    return result;
  }

  @Override
  public String toString() {
    return "("+x+","+y+")";
  }

  public Position move(Position step,Boundary boundary) {
    Position current = this;
    Position newPosition = new Position(x + step.x, y + step.y);
    if(boundary.inBoundary(newPosition)) {
      return newPosition;
    }
    return current;
  }

  public static Position commandToPosition(Command command) {
    switch (command) {
      case WALK_NORTH_WEST:
        return new Position(-1,0);
      case WALK_SOUTH_WEST:
        return new Position(0,1);
      case WALK_SOUTH_EAST:
        return new Position(1,0);
      case WALK_NORTH_EAST:
        return new Position(0,-1);
      default:
        return new Position(0,0); // dont move
    }
  }

  public static List<Position> parseTuplesToPoint(String tuples) {
    Pattern p = Pattern.compile("(\\((\\d+),(\\d+)\\))");
    Matcher matcher = p.matcher(tuples);
    return  matcher.results().map((m) -> new Position(Integer.parseInt(m.group(2)),Integer.parseInt(m.group(3)))).collect(
        Collectors.toList());
  }

  public static Optional<Position> parseTupleToPoint(String tuple) {
    List<Position> positions = parseTuplesToPoint(tuple);
    return positions.size() > 0 ? Optional.of(positions.get(0)) : Optional.empty();
  }
}

interface Level {
  List<Position> getApplesList();
  Map<String,String> getLevelData();
}


class Level7 {
  private final Map<String,String> level7Data = new HashMap<>();

  public static final List<Position> LEVEL7_BUSHES_POSITION = List.of(
      new Position(2, 0),
      new Position(1, 2),
      new Position(4, 2),
      new Position(3, 3),
      new Position(3, 4));

  // add apples (2,0), (1,2), (4,2), (3,3), (3,4)
  public List<Position> applesList = new ArrayList<>(Set.of(
      new Position(2, 2),
      new Position(4, 4),
      new Position(2, 3),
      new Position(1, 2),
      new Position(0, 2)
  ));

  public List<Position> getApplesList() {
    return applesList;
  }

  public Level7() {
    level7Data.put("level_name","Non-linear walk");
    level7Data.put("board_height","5");
    level7Data.put("board_width","5");
    level7Data.put("hint","Walk over to the apples and pick them up! You can move one tile per update cycle. You have to get all the apples!");
    level7Data.put("obstacles","(0,0)");
  }

  public Map<String, String> getLevel7Data() {
    return level7Data;
  }


  public String getStaticDataString() {
    return "{level_name=Non-linear walk, level=7, board_height=5, "
        + "hint=Walk over to the apples and pick them up! You can move one tile per update cycle. You have to get all the apples!,"
        + "board_width=5,"
        + "obstacles=(0,0)}";
  }
}

class Level8  implements Level {
  private final Map<String,String> level8Data = new HashMap<>();

  public Level8() {
    level8Data.put("level_name","Avoid obstacles");
    level8Data.put("board_height","7");
    level8Data.put("board_width","3");
    level8Data.put("obstacles","(0,0), (1,2), (1,3), (0,5), (1,5), (0,3)");
  }

  //{apples=(2,0), (0,2), (0,4), player=(0,6), bushes=(2,1)}
  public List<Position> applesList = new ArrayList<>(Set.of(
      new Position(2, 0),
      new Position(0, 2),
      new Position(0, 4)
  ));

  public List<Position> getApplesList() {
    return applesList;
  }

  public Map<String, String> getLevelData() {
    return level8Data;
  }

  public Position getPlayerPosition() {
    return new Position(0,6);
  }

  public List<Position> getBushesList() {
    return List.of(new Position(2,1));
  }
}
