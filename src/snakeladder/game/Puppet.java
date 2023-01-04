package snakeladder.game;

import ch.aplu.jgamegrid.*;
import java.awt.Point;
import java.util.Hashtable;

public class Puppet extends Actor
{
  private final String DOWN_PATH_TAG = "down";
  private final String UP_PATH_TAG = "up";
  private final int MAX_ROLL = 6;
  
  private GamePane gamePane;
  private NavigationPane navigationPane;
  private int cellIndex = 0;
  private int nbSteps;
  private Connection currentCon = null;
  private int y;
  private int dy;
  private boolean isAuto;
  private String puppetName;
  private Hashtable<Integer, Integer> diceStatistics = new Hashtable<Integer, Integer>();
  private Hashtable<String, Integer> pathStatistics = new Hashtable<String, Integer>();
  private Boolean goBack = false;

  Puppet(GamePane gp, NavigationPane np, String puppetImage)
  {
    super(puppetImage);
    this.gamePane = gp;
    this.navigationPane = np;
    for (int i = navigationPane.getNumberOfDice(); i <= navigationPane.getNumberOfDice()*MAX_ROLL; i++) {
      diceStatistics.put(i, 0);
    }
    pathStatistics.put(UP_PATH_TAG, 0);
    pathStatistics.put(DOWN_PATH_TAG, 0);
  }

  public boolean isAuto() {
    return isAuto;
  }

  public void setAuto(boolean auto) {
    isAuto = auto;
  }

  public String getPuppetName() {
    return puppetName;
  }

  public void setPuppetName(String puppetName) {
    this.puppetName = puppetName;
  }

  // Print statistics for player's dice roll count
  public void printDiceStats() {
    System.out.print(this.puppetName + " rolled: ");
    for (int i = navigationPane.getNumberOfDice(); i <= navigationPane.getNumberOfDice()*MAX_ROLL; i++) {
      System.out.print(i + "-" + diceStatistics.get(i));
      if (i < navigationPane.getNumberOfDice()*MAX_ROLL) {
        System.out.print(", ");
      } else {
        System.out.print("\n");
      }
    }
  }

  // Print statistics for player's path symbol traversals
  public void printPathStats() {
    System.out.println(this.puppetName + " traversed: up-" + pathStatistics.get(UP_PATH_TAG) + ", down-" + pathStatistics.get(DOWN_PATH_TAG));
  }

  void go(int nbSteps)
  {
    if (cellIndex == 100)  // after game over
    {
      cellIndex = 0;
      setLocation(gamePane.startLocation);
    }
    this.nbSteps = nbSteps;
    setActEnabled(true);
  }

  void resetToStartingPoint() {
    cellIndex = 0;
    setLocation(gamePane.startLocation);
    setActEnabled(true);
  }

  int getCellIndex() {
    return cellIndex;
  }

  private void moveToNextCell()
  {
    int tens = cellIndex / 10;
    int ones = cellIndex - tens * 10;
    if (tens % 2 == 0)     // Cells starting left 01, 21, .. 81
    {
      if (ones == 0 && cellIndex > 0)
        setLocation(new Location(getX(), getY() - 1));
      else
        setLocation(new Location(getX() + 1, getY()));
    }
    else     // Cells starting left 20, 40, .. 100
    {
      if (ones == 0)
        setLocation(new Location(getX(), getY() - 1));
      else
        setLocation(new Location(getX() - 1, getY()));
    }
    cellIndex++;
  }

  
  // liz code to move back to prev cell if player lands on same as opponent

  private void moveToPrevCell(){

    int cellNum = cellIndex;
    int tens = cellNum / 10;
    int ones = cellNum - tens * 10;
    if (tens % 2 == 0)
    {
      if (ones == 0 && cellIndex > 0)
        setLocation(new Location(getX(), getY() + 1));
      else if(cellNum % 10 == 0)
        setLocation(new Location(getX() + 1, getY()));
      else{
        setLocation(new Location(getX() - 1, getY()));
      }
    }
    else{
    {
      if (ones == 1 && cellIndex > 0)
        setLocation(new Location(getX(), getY() + 1));
      else if (cellNum % 10 == 0)
        setLocation(new Location(getX() - 1, getY()));
      else{
        setLocation(new Location(getX() + 1, getY()));
      }
    }
    cellIndex--;
    }
  }

  public void act()
  {
    // Track dice roll statistics
    if (Die.getTotal() > 0) {
      this.diceStatistics.replace(Die.getTotal(), this.diceStatistics.get(Die.getTotal())+1);
    }
    
    if ((cellIndex / 10) % 2 == 0)
    {
      if (isHorzMirror())
        setHorzMirror(false);
    }
    else
    {
      if (!isHorzMirror())
        setHorzMirror(true);
    }

    // Animation: Move on connection
    if (currentCon != null)
    {
      int x = gamePane.x(y, currentCon);
      setPixelLocation(new Point(x, y));
      y += dy;

      // Check end of connection
      if ((dy > 0 && (y - gamePane.toPoint(currentCon.locEnd).y) > 0)
        || (dy < 0 && (y - gamePane.toPoint(currentCon.locEnd).y) < 0))
      {
        gamePane.setSimulationPeriod(100);
        setActEnabled(false);
        setLocation(currentCon.locEnd);
        cellIndex = currentCon.cellEnd;
        setLocationOffset(new Point(0, 0));
        currentCon = null;
        // don't go into the next round if it goes back
        if (goBack) {
          goBack = false;
        } else {
          navigationPane.prepareRoll(cellIndex);
        }
      }
      return;
    }

    // Normal movement
    if (nbSteps > 0 || nbSteps == -1)
    {
      if(nbSteps > 0){
      moveToNextCell();
      nbSteps--;
      }else if(nbSteps == -1){
        goBack = true;
        moveToPrevCell();
        nbSteps++;
      }

      if (cellIndex >= 100)  // Game over (added >= instead of ==)
      {
        setActEnabled(false);
        navigationPane.prepareRoll(cellIndex);
        nbSteps = 0;
        return;
      }

      if (nbSteps == 0) {
        // Check if on connection start
        if ((currentCon = gamePane.getConnectionAt(getLocation())) != null) {
          if (currentCon.locEnd.y > currentCon.locStart.y) {
            // DO NOT travel down if rolling the minimum
            if (Die.getTotal() != navigationPane.getNumberOfDice() || cellIndex >= 100) { //(changed to >= )
              gamePane.setSimulationPeriod(50);
              y = gamePane.toPoint(currentCon.locStart).y;
              System.out.println("Total = " + Die.getTotal() + ", Dice = " + navigationPane.getNumberOfDice());
              dy = gamePane.animationStep;
              // Track traversal through paths
              this.pathStatistics.replace(DOWN_PATH_TAG, this.pathStatistics.get(DOWN_PATH_TAG)+1);
            } else {
              System.out.println("Minimum roll. Fall prevented.");
              currentCon = null;
              dy = 0;
              setActEnabled(false);
              navigationPane.prepareRoll(cellIndex);
            }
          } else {
            gamePane.setSimulationPeriod(50);
            y = gamePane.toPoint(currentCon.locStart).y;
            dy = -gamePane.animationStep;
            // Track traversal through paths
            this.pathStatistics.replace(UP_PATH_TAG, this.pathStatistics.get(UP_PATH_TAG)+1);
          }

          if (currentCon instanceof Snake) {
            navigationPane.showStatus("Digesting...");
            navigationPane.playSound(GGSound.MMM);
          } else {
            navigationPane.showStatus("Climbing...");
            navigationPane.playSound(GGSound.BOING);
          }
        } else {
          if(goBack == true){
            setActEnabled(false);
            goBack = false;
          } else {
            setActEnabled(false);
            navigationPane.prepareRoll(cellIndex);
          }
        }
        // Adding logic for puppet to toggle reversal when opponent has an equal or greater amount of ups in comparison to downs
        int numDice = navigationPane.getNumberOfDice();
        int potentialRolls = numDice * 6;
        int i = 1;
        int numUps = 0;
        int numDowns = 0;
        for (Puppet puppet: gamePane.getAllPuppets()) {
          if (puppet != this){
            int opponentCell = puppet.cellIndex;
            for(i = numDice; i <= potentialRolls && opponentCell + i <= 100; i++){
              Location tmpLocation = GamePane.cellToLocation(opponentCell + i);
              Connection tmp = gamePane.getConnectionAt(tmpLocation);
              if(tmp != null){
                if(tmp.cellStart > tmp.cellEnd){
                  numDowns++;
                } else if(tmp.cellStart < tmp.cellEnd){
                  numUps++;
                }
              }
            }
          }
        }
        if (numUps >= numDowns){
          //System.out.println("ups = " + Integer.toString(numUps) + ", downs = " + Integer.toString(numDowns));
          /*for (Connection con : gamePane.getAllConnections()) {
            con.reverseConnection();
          }*/
          boolean toggle = !navigationPane.getIsToggled();
          navigationPane.getToggleButton().setChecked(toggle);
          //System.out.println("the connections are reversed!");
        } else {
          System.out.println("not reversed");
          System.out.println("ups = " + Integer.toString(numUps) + ", downs = " + Integer.toString(numDowns));
        }
      }
    }
    
  }
}
