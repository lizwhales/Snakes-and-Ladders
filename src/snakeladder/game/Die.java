package snakeladder.game;

import ch.aplu.jgamegrid.Actor;

public class Die extends Actor
{
  private NavigationPane np;
  private int nb;
  private static int total = 0;

  Die(int nb, NavigationPane np)
  {
    super("sprites/pips" + nb + ".gif", 7);
    this.nb = nb;
    this.np = np;
  }

  public void act()
  {
    showNextSprite();
    if (getIdVisible() == 6)
    {
      setActEnabled(false);
      np.startMoving(total);
    }
  }

  public void setTotal(int n) {
    total = n;
  }

  public int getRoll() {
    return nb;
  }

  public static int getTotal() {
    return total;
  }
}
