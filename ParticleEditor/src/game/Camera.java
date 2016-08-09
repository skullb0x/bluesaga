package game;

import org.newdawn.slick.Input;

public class Camera {

  public float x;
  public float y;
  private float myCameraMoveSpeed;

  public Camera() {
    x = 0;
    y = 0;
    myCameraMoveSpeed = 400.0f;
  }

  public void Update(float aElapsedTime) {}

  public void HandleInput(org.newdawn.slick.Input aInput, float aElapsedTime) {

    if (aInput.isKeyDown(Input.KEY_UP)) {
      y += myCameraMoveSpeed * aElapsedTime;
    }
    if (aInput.isKeyDown(Input.KEY_DOWN)) {
      y -= myCameraMoveSpeed * aElapsedTime;
    }
    if (aInput.isKeyDown(Input.KEY_LEFT)) {
      x += myCameraMoveSpeed * aElapsedTime;
    }
    if (aInput.isKeyDown(Input.KEY_RIGHT)) {
      x -= myCameraMoveSpeed * aElapsedTime;
    }
  }
}
