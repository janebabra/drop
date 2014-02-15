package com.badlogic.drop;

import java.util.Iterator;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class Drop implements ApplicationListener {
	Texture dropImage;
	Texture bucketImage;
	Sound dropSound;
	Music rainMusic;

	OrthographicCamera camera;
	SpriteBatch batch;

	Rectangle bucket;

	Array<Rectangle> rainDrops;
	long lastDropTime;

	@Override
	public void create() {
		Gdx.app.log("Manpreet-gdx", "In create ...");
		// load the images from assets (Texture does the loading & stores it in
		// video RAM)
		dropImage = new Texture(Gdx.files.internal("droplet.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));

		// load sound & music from assets
		dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

		// start the background music now
		rainMusic.setLooping(true);
		rainMusic.play();

		// create orthographic camera
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

		// create a sprite batch
		batch = new SpriteBatch();

		// create the bucket rectangle
		bucket = new Rectangle();
		bucket.x = 800 / 2 - 48 / 2;
		bucket.y = 20;
		bucket.width = 48;
		bucket.height = 48;

		Gdx.app.log("Manpreet-gdx", "starting the spawn for first time ...");

		// spawn the raindrop
		rainDrops = new Array<Rectangle>();
		spawnRaindrop();
		Gdx.app.log("Manpreet-gdx", "spawn for first time done ...");
	}

	/**
	 * create a raindrop
	 */
	private void spawnRaindrop() {
		Gdx.app.log("Manpreet-gdx", "in spawn ...");
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, 800 - 48);
		raindrop.y = 480;
		raindrop.width = 48;
		raindrop.height = 48;
		rainDrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
		Gdx.app.log("Manpreet-gdx", "spawn done ...");
	}

	@Override
	public void render() {
		// bucket rendering starts
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		// update the camera
		camera.update();

		// sprite batch should use coordinate system specified by the camera &
		// then start the batch (do all drawing at once to make opengl happy)
		// finally end it
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		Gdx.app.log("Manpreet-gdx", "start drawing ...");
		batch.draw(bucketImage, bucket.x, bucket.y);
		for (Rectangle raindrop : rainDrops) {
			Gdx.app.log("Manpreet-gdx", "draw spawn ..." + raindrop.x + " "
					+ raindrop.y);
			batch.draw(dropImage, raindrop.x, raindrop.y);
		}
		batch.end();

		// move the bucket
		if (Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			bucket.x = touchPos.x - 48 / 2;
		}

		// make sure the bucket stays within the screen bounds
//		if (bucket.x < 0)
//			bucket.x = 0;
//		if (bucket.x > 800 - 48)
//			bucket.x = 800 - 48;

		// spawn raindrop after threshold
		if (TimeUtils.nanoTime() - lastDropTime > 1000000000) {
			spawnRaindrop();
		}

		// move the raindrops
		Iterator<Rectangle> it = rainDrops.iterator();
		while (it.hasNext()) {
			Rectangle raindrop = it.next();
			raindrop.y -= 200 * Gdx.graphics.getDeltaTime();

			if (raindrop.y + 48 < 0) {
				it.remove();
			}

			if (raindrop.overlaps(bucket)) {
				dropSound.play();
				it.remove();
			}
		}
	}

	@Override
	public void dispose() {
		// dispose all inactive resources
		bucketImage.dispose();
		dropImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
		batch.dispose();
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}
