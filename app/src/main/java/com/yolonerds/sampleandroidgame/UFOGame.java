// Leszek Zychowski

package com.yolonerds.sampleandroidgame;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class UFOGame extends AppCompatActivity implements SensorEventListener {

    // sound

    MediaPlayer mp;
    Intent bgMusic;

    // game

    GameView gv;
    Paint drawPaint = new Paint();
    Bitmap player;
    Bitmap gameOver;
    Rect playerRect;
    List<Bitmap> asteroidList;
    List<Rect> asteroidRectList;
    List<Integer> asteroidYSpeed;
    List<Integer> asteroidXPos;
    List<Integer> asteroidYPos;

    List<Bitmap> bgObjectsList;
    List<Integer> bgObjectsListYSpeed;
    List<Integer> bgObjectsListXPos;
    List<Integer> bgObjectsListYPos;

    private int backgroundColor = Color.BLACK;
    private int playerX = 500, playerY = 1200, playerXSpeed = 10, playerYSpeed = 10;
    private int health = 100;  // start with 100 health

    // gyro

    private float x;
    private float y;
    private float z;

    SensorManager sManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bgMusic = new Intent(this, MusicPlayer.class);
        startService(bgMusic);

        gv = new GameView(this);
        this.setContentView(gv);

        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // initialize lists to store asteroid objects

        asteroidList = new ArrayList<>();
        asteroidRectList = new ArrayList<>();
        asteroidYSpeed = new ArrayList<>();
        asteroidXPos = new ArrayList<>();
        asteroidYPos = new ArrayList<>();

        // initialize lists to store background objects

        bgObjectsList = new ArrayList<>();
        bgObjectsListYSpeed = new ArrayList<>();
        bgObjectsListXPos = new ArrayList<>();
        bgObjectsListYPos = new ArrayList<>();

        // prevent screen dimming as there is little screen tapping involved in this game
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1)
    {
        //Do nothing.
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        x = event.values[2]; // roll
        y = event.values[1]; // pitch
        z = event.values[0]; // yaw
    }

    @Override
    protected void onPause(){
        super.onPause();
        gv.pause();
        sManager.unregisterListener(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        gv.resume();
        sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_FASTEST);
    }

    // --------------------------
    // GameView Class starts here
    // --------------------------

    class GameView extends SurfaceView implements Runnable {

        Thread viewThread = null;
        SurfaceHolder holder;
        Canvas gameCanvas;
        Timer timer;
        boolean threadOK = true;

        public GameView(Context context){
            super(context);
            holder = this.getHolder();
            timer = new Timer();
            player = BitmapFactory.decodeResource(getResources(), R.drawable.ic_ufo);
            gameOver = BitmapFactory.decodeResource(getResources(), R.drawable.gameover);
            mp = MediaPlayer.create(getApplicationContext(), R.raw.hit_sound);
            mp.setLooping(false);
            mp.setVolume(100,100);
        }

        @Override
        public void run(){

            while (threadOK == true){

                if (!holder.getSurface().isValid()){
                    continue;
                }

                gameCanvas = holder.lockCanvas();

                addBgObjects();
                addAsteroids();
                updateAsteroidRectangles();

                playerRect = new Rect(playerX, playerY, playerX + player.getWidth(), playerY + player.getHeight());

                myOnDraw(gameCanvas);
                holder.unlockCanvasAndPost(gameCanvas);

            }
        }

        public void addBgObjects(){
            if (bgObjectsList.size() < 25){

                int randomStartPos = (int)(Math.random() * gameCanvas.getWidth());
                int randomStar = (int)(Math.random() * 1000);

                if (randomStar < 975){
                    bgObjectsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.starline));
                    bgObjectsListYSpeed.add(new Integer((int)(Math.random() * 30 + 10)));
                } else if (randomStar >= 975 && randomStar < 980){
                    bgObjectsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.ice_planet));
                    bgObjectsListYSpeed.add(new Integer((int)(Math.random() * 1 + 1)));
                } else if (randomStar >= 980 && randomStar < 985){
                    bgObjectsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.saturn));
                    bgObjectsListYSpeed.add(new Integer((int)(Math.random() * 1 + 1)));
                } else if (randomStar >= 985 && randomStar < 990) {
                    bgObjectsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.crusty));
                    bgObjectsListYSpeed.add(new Integer((int) (Math.random() * 1 + 1)));
                } else if (randomStar >= 990 && randomStar < 995) {
                    bgObjectsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.earth));
                    bgObjectsListYSpeed.add(new Integer((int) (Math.random() * 1 + 1)));
                } else {
                    bgObjectsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.blue_green));
                    bgObjectsListYSpeed.add(new Integer((int) (Math.random() * 1 + 1)));
                }

                bgObjectsListXPos.add(new Integer(randomStartPos));
                bgObjectsListYPos.add(new Integer(0));
            }
        }

        public void addAsteroids(){
            if (asteroidList.size() < 5){

                int randomStartPos = (int)(Math.random() * gameCanvas.getWidth());

                asteroidList.add(BitmapFactory.decodeResource(getResources(), R.drawable.ic_asteroid));
                asteroidRectList.add(new Rect(randomStartPos,0,20,20));
                asteroidYSpeed.add(new Integer((int)(Math.random() * 30 + 10)));
                asteroidXPos.add(new Integer(randomStartPos));
                asteroidYPos.add(new Integer(0));
            }
        }

        public void updateAsteroidRectangles(){
            for (int i = 0; i < asteroidList.size(); i++){
                asteroidRectList.set(i, new Rect(asteroidXPos.get(i), asteroidYPos.get(i), asteroidXPos.get(i) + asteroidList.get(i).getWidth(), asteroidYPos.get(i) + asteroidList.get(i).getHeight()));
            }
        }

        protected void myOnDraw(Canvas canvas){

            drawPaint.setAlpha(255);

            if (health < 0){
                canvas.drawColor(Color.BLACK);
                canvas.drawBitmap(gameOver,(canvas.getWidth()/2) - (gameOver.getWidth() /2), (canvas.getHeight()/2) - (gameOver.getHeight() /2), drawPaint);
            } else {
                canvas.drawColor(backgroundColor);

                drawbgObjects(canvas);
                drawAsteroids(canvas);

                canvas.drawBitmap(player, playerX, playerY, drawPaint);
                drawPaint.setColor(Color.WHITE);
                drawPaint.setTextSize(50);
                canvas.drawText("health: " + health, 10, canvas.getHeight() - 30, drawPaint);

                onCollision();
                updatePlayerPosition(canvas);
                removeAsteroid(canvas);
                removeBgObjects(canvas);
            }
        }

        // redraw asteroids
        public void drawAsteroids(Canvas canvas){
            for (int i = 0; i < asteroidList.size(); i++){
                asteroidYPos.set(i, new Integer(asteroidYPos.get(i) + asteroidYSpeed.get(i)));
                canvas.drawBitmap(asteroidList.get(i), asteroidXPos.get(i), asteroidYPos.get(i), drawPaint);
            }
        }

        // redraw background objects
        public void drawbgObjects(Canvas canvas){
            for (int i = 0; i < bgObjectsList.size(); i++){
                bgObjectsListYPos.set(i, new Integer(bgObjectsListYPos.get(i) + bgObjectsListYSpeed.get(i)));
                canvas.drawBitmap(bgObjectsList.get(i), bgObjectsListXPos.get(i), bgObjectsListYPos.get(i), drawPaint);
            }
        }

        // handle collision
        public void onCollision(){
            for (int i = 0; i < asteroidList.size(); i++){
                if (Rect.intersects(playerRect, asteroidRectList.get(i))){
                    mp.start();
                    health -= 2;
                    backgroundColor = Color.DKGRAY;
                    player = BitmapFactory.decodeResource(getResources(), R.drawable.ic_ufo_hit);
                    timer.schedule(new CollisionTask(), 100);
                }
            }
        }

        // only handles restart button when health gets below 0
        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
            if (health > 0){
                return true;
            }

            switch(event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    this.pause();
                    playerX = 500;
                    playerY = 1200;
                    playerXSpeed = 10;
                    playerYSpeed = 10;
                    health = 100;  // start with 100 health

                    asteroidList.clear();
                    asteroidRectList.clear();
                    asteroidYSpeed.clear();
                    asteroidXPos.clear();
                    asteroidYPos.clear();

                    this.resume();
                    return true;
            }
            return false;
        }

        public void updatePlayerPosition(Canvas canvas){

            // set player speed
            playerXSpeed = Math.round(x);
            playerYSpeed = Math.round(y) + 30; // add 30 so that the user can have the phone on an angle without the UFO moving (more natural)

            // update player position
            if ((playerX < 0 && playerXSpeed > 0) || (playerX > (canvas.getWidth() - player.getWidth()) && playerXSpeed < 0)){
                // do nothing
            } else {
                playerX += playerXSpeed * -2;
            }

            if ((playerY < 0 && playerYSpeed > 0) || (playerY > (canvas.getHeight() - player.getHeight()) && playerYSpeed < 0)){
                // do nothing
            } else {
                playerY += playerYSpeed * -2;
            }
        }

        // remove asteroid when it passes bottom of the screen
        public void removeAsteroid(Canvas canvas){
            for (int i = 0; i < asteroidList.size(); i++){
                if (asteroidYPos.get(i) > canvas.getHeight() + 30){
                    asteroidList.remove(i);
                    asteroidYSpeed.remove(i);
                    asteroidYPos.remove(i);
                    asteroidXPos.remove(i);
                    asteroidRectList.remove(i);

                    health++;
                }
            }
        }

        // remove background objects when they pass bottom of the screen
        public void removeBgObjects(Canvas canvas){
            for (int i = 0; i < bgObjectsList.size(); i++){
                if (bgObjectsListYPos.get(i) > canvas.getHeight() + 30){
                    bgObjectsList.remove(i);
                    bgObjectsListYSpeed.remove(i);
                    bgObjectsListYPos.remove(i);
                    bgObjectsListXPos.remove(i);
                }
            }
        }

        public void pause(){
            threadOK = false;

            while (true){
                try {
                    viewThread.join();
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
                break;
            }

            viewThread = null;
        }

        public void resume(){
            threadOK = true;
            viewThread = new Thread(this);
            viewThread.start();
        }

    }

    public class CollisionTask extends TimerTask {

        public void run(){
            backgroundColor = Color.BLACK;
            player = BitmapFactory.decodeResource(getResources(), R.drawable.ic_ufo);
        }
    }
}
