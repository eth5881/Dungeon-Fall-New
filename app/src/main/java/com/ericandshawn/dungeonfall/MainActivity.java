package com.ericandshawn.dungeonfall;

import android.hardware.SensorManager;
import android.view.View;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;


public class MainActivity extends SimpleBaseGameActivity implements IAccelerationListener, IOnSceneTouchListener {
    // ===========================================================
    // Constants
    // ===========================================================

    protected static final int CAMERA_WIDTH = 1200;
    protected static final int CAMERA_HEIGHT = 1920;

    // ===========================================================
    // Fields
    // ===========================================================

    private BitmapTextureAtlas mBitmapTextureAtlas;
    private BitmapTextureAtlas mBackgroundBitmapTextureAtlas;

    private Scene mScene;

    protected ITiledTextureRegion mBat;
    protected ITiledTextureRegion mGold;
    protected ITextureRegion mPlayer;
    protected ITextureRegion mPlatform;
    protected ITextureRegion mSpikedPlatform;
    protected ITextureRegion mBg;

    protected PhysicsWorld mPhysicsWorld;

    private Sprite player;
    private Body playerBody;

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    public EngineOptions onCreateEngineOptions() {
        Toast.makeText(this, "Touch the screen to drop hero.", Toast.LENGTH_LONG).show();

        final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

        return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new FillResolutionPolicy(), camera);
    }

    @Override
    public void onCreateResources() {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

        //setup random background
        int bg = (int)(Math.round(Math.random()*8));
        if(bg == 0){
            bg = 8;
        }
        this.mBackgroundBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 512, 512, TextureOptions.BILINEAR);
        this.mBg = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBackgroundBitmapTextureAtlas, this, "" + bg + ".png", 0, 0);
        this.mBackgroundBitmapTextureAtlas.load();

        //setup game sprites
        this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 512, 256, TextureOptions.BILINEAR);
        this.mPlayer = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "hero.png", 0, 0);
        this.mBat = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "bat.png", 35, 0, 3, 1);
        this.mGold = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "gold.png", 185, 0, 8, 1);
        this.mPlatform = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "platform.png", 0, 51);
        this.mSpikedPlatform = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "stakes.png", 71, 51);
        this.mBitmapTextureAtlas.load();
    }

    @Override
    public Scene onCreateScene() {
        this.mEngine.registerUpdateHandler(new FPSLogger());

        this.mScene = new Scene();
        this.mScene.setOnSceneTouchListener(this);

        this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);

        final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
        //final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT, CAMERA_WIDTH, 2, vertexBufferObjectManager);
        //final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 2, vertexBufferObjectManager);
        final Rectangle left = new Rectangle(0, 0, 0, CAMERA_HEIGHT, vertexBufferObjectManager);
        final Rectangle right = new Rectangle(CAMERA_WIDTH, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);

        final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
        //PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyDef.BodyType.StaticBody, wallFixtureDef);
        //PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyDef.BodyType.StaticBody, wallFixtureDef);
        PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyDef.BodyType.StaticBody, wallFixtureDef);
        PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyDef.BodyType.StaticBody, wallFixtureDef);

        //this.mScene.attachChild(ground);
        //this.mScene.attachChild(roof);
        this.mScene.attachChild(left);
        this.mScene.attachChild(right);

        this.mScene.registerUpdateHandler(this.mPhysicsWorld);
        this.mScene.registerUpdateHandler(new IUpdateHandler() {
            public void reset() {
            }
            public void onUpdate(float pSecondsElapsed) {
                //Game loop
                if(player != null) {
                    if (player.getY() > CAMERA_HEIGHT) {
                        mPhysicsWorld.destroyBody(playerBody);
                        mScene.detachChild(player);
                        player = null;
                    }
                }
            }
        });

        this.setBackgroundImage();
        this.addFloorItems();

        return this.mScene;
    }

    @Override
    public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
        if (this.mPhysicsWorld != null) {
            if (pSceneTouchEvent.isActionDown()) {
                this.addPlayer(pSceneTouchEvent.getX(), -150);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onAccelerationAccuracyChanged(final AccelerationData pAccelerationData) {

    }

    @Override
    public void onAccelerationChanged(final AccelerationData pAccelerationData) {
        final Vector2 gravity = Vector2Pool.obtain(pAccelerationData.getX()*2, SensorManager.GRAVITY_EARTH*1.5f);
        this.mPhysicsWorld.setGravity(gravity);
        Vector2Pool.recycle(gravity);
    }

    @Override
    public void onResumeGame() {
        super.onResumeGame();

        this.enableAccelerationSensor(this);
    }

    @Override
    public void onPauseGame() {
        super.onPauseGame();

        this.disableAccelerationSensor();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        final View decorView = getWindow().getDecorView();
        if (hasFocus) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    // ===========================================================
    // Methods
    // ===========================================================

    private void addPlayer(final float pX, final float pY) {
        final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1, 0.7f, 0.3f);

        this.player = new Sprite(pX, pY, this.mPlayer, this.getVertexBufferObjectManager());
        this.player.setScale(3,3);
        this.playerBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, this.player, BodyDef.BodyType.DynamicBody, objectFixtureDef);

        this.mScene.attachChild(this.player);
        this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(this.player, this.playerBody, true, true));
    }

    private void addFloorItems(){
        final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1, 0, 0.3f);

        for(int i=0;i<12;i++){
            if (Math.random() < 0.6) {
                //add bat
                final AnimatedSprite bat;
                final Body batBody;
                bat = new AnimatedSprite((int) (Math.random() * 900), (int) (Math.random() * 1700), this.mBat, this.getVertexBufferObjectManager());
                bat.setScale(3, 3);
                batBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld, bat, BodyDef.BodyType.StaticBody, objectFixtureDef);
                bat.animate(100);
                this.mScene.attachChild(bat);
                this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(bat, batBody, true, true));
            } else if (Math.round(Math.random() * 10) >= 9) {
                //add platform
                final Sprite platform;
                final Body platformBody;
                platform = new Sprite((int) (Math.random() * 900), (int) (Math.random() * 1700), this.mPlatform, this.getVertexBufferObjectManager());
                platform.setScale(3, 3);
                platformBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, platform, BodyDef.BodyType.StaticBody, objectFixtureDef);
                this.mScene.attachChild(platform);
                this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(platform, platformBody, true, true));
            } else if (Math.round(Math.random() * 10) == 10) {
                //add platform with stakes
                final Sprite stakes;
                final Body stakesBody;
                stakes = new Sprite((int) (Math.random() * 900), (int) (Math.random() * 1700), this.mSpikedPlatform, this.getVertexBufferObjectManager());
                stakes.setScale(3, 3);
                stakesBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, stakes, BodyDef.BodyType.StaticBody, objectFixtureDef);
                this.mScene.attachChild(stakes);
                this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(stakes, stakesBody, true, true));
            } else {
                //add gold
                final AnimatedSprite gold;
                final Body goldBody;
                gold = new AnimatedSprite((int) (Math.random() * 900), (int) (Math.random() * 1700), this.mGold, this.getVertexBufferObjectManager());
                gold.setScale(3, 3);
                goldBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld, gold, BodyDef.BodyType.StaticBody, objectFixtureDef);
                gold.animate(100);
                this.mScene.attachChild(gold);
                this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(gold, goldBody, true, true));
            }
        }
    }

    private void setBackgroundImage() {
        final Sprite background;
        background = new Sprite(CAMERA_WIDTH/2-135, CAMERA_HEIGHT/2-240, this.mBg, this.getVertexBufferObjectManager());
        background.setScale(4,4);
        this.mScene.attachChild(background);
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}