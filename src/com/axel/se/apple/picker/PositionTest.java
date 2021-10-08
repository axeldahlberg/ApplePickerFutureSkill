package com.axel.se.apple.picker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PositionTest {

  @Test
  public void testPositionAdd() {
    Position current = new Position(1, 1);
    Boundary boundary = new Boundary(5, 5);
    Position next = current.move(new Position(0,1), boundary);
    assertEquals(next.x,current.x);
    assertEquals(2,next.y);
  }

  @Test
  public void testPositionAddFourTimes() {
    Position current = new Position(0, 0);
    Boundary boundary = new Boundary(5, 5);
    for(int i =0;i<5;i++) {
      current = current.move(new Position(0,1), boundary);
    }
    System.out.println(current);
    assertEquals(0,current.x);
    assertEquals(4,current.y);

    for(int i =0;i<5;i++) {
      current = current.move(new Position(1,0), boundary);
    }
    assertEquals(4,current.y);
    assertEquals(4,current.x);
  }

}