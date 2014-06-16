/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.pipeline;

import java.io.IOException;

import org.acra.ACRA;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.happydroids.droidtowers.utils.PNG;

public class GenerateAssetManagerFileList {
	private static FileHandle assetsDir = new FileHandle("assets/");
	private static AssetList managedFiles = new AssetList();

	public static void main(String[] args) {
		GdxNativesLoader.load();

		preloadDirectory("swatches/", ".png", Texture.class);

		preloadFile(assetsDir.child("backgrounds/splash1.txt"),
				TextureAtlas.class);
		preloadFile(assetsDir.child("backgrounds/splash2.txt"),
				TextureAtlas.class);
		preloadFile(assetsDir.child("backgrounds/cityscape-middle.png"),
				Texture.class);
		preloadFile(assetsDir.child("happydroid.txt"), TextureAtlas.class);
		preloadFile(assetsDir.child("droid-santa-hat.png"), Texture.class);

		preloadFile(assetsDir.child("default-skin.json"), Skin.class);
		preloadFile(assetsDir.child("backgrounds/clouds.txt"),
				TextureAtlas.class);
		preloadFile(assetsDir.child("hud/skin.txt"), TextureAtlas.class);
		preloadFile(assetsDir.child("hud/menus.txt"), TextureAtlas.class);
		preloadFile(assetsDir.child("hud/buttons.txt"), TextureAtlas.class);
		preloadFile(assetsDir.child("hud/window-bg.png"), Texture.class);
		preloadFile(assetsDir.child("hud/dialog-bg.png"), Texture.class);

		addDirectoryToAssetManager("backgrounds/", ".txt", TextureAtlas.class);
		addDirectoryToAssetManager("designer/housing/", ".txt",
				TextureAtlas.class);
		addDirectoryToAssetManager("movies/", ".txt", TextureAtlas.class);
		addDirectoryToAssetManager("backgrounds/", ".png", Texture.class);
		addDirectoryToAssetManager("hud/rating-bars/", ".png", Texture.class);
		addDirectoryToAssetManager("hud/", ".txt", TextureAtlas.class);
		addDirectoryToAssetManager("rooms/", ".txt", TextureAtlas.class);

		addFileEntry(assetsDir.child("characters.txt"), TextureAtlas.class);
		addFileEntry(assetsDir.child("transport.txt"), TextureAtlas.class);
		addFileEntry(assetsDir.child("rain-drop.png"), Texture.class);
		addFileEntry(assetsDir.child("snow.png"), Texture.class);
		addFileEntry(assetsDir.child("decals.png"), Texture.class);
		addFileEntry(assetsDir.child("tower-sign.png"), Texture.class);
		addFileEntry(assetsDir.child("tower-sign-poll.png"), Texture.class);

		for (FileHandle file : assetsDir.child("sound/").child("music/").list()) {
			managedFiles.addMusic(cleanFilename(file.path()));
		}

		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			new FileHandle("assets/assets.json").writeString(
					mapper.writeValueAsString(managedFiles), false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void makeSwatch(FileHandle swatchesDir,
			String swatchFilename, Color color) {
		FileHandle swatchFile = swatchesDir.child(swatchFilename);
		if (swatchFile.exists()) {
			return;
		}

		try {
			Pixmap pixmap = new Pixmap(2, 2, Pixmap.Format.RGB888);
			pixmap.setColor(color);
			pixmap.fill();

			byte[] bytes = PNG.toPNG(pixmap);
			swatchFile.writeBytes(bytes, false);
		} catch (IOException e) {
			ACRA.getErrorReporter().handleException(e);
		}
	}

	private static void preloadDirectory(String folder, String suffix,
			Class clazz) {
		for (FileHandle child : assetsDir.child(folder).list(suffix)) {
			preloadFile(child, clazz);
		}
	}

	private static void preloadFile(FileHandle child, Class clazz) {
		if (!verifyFile(child)) {
			managedFiles.preload(cleanFilename(child.path()),
					checkForHDVersion(child), clazz);
		}
	}

	private static void addDirectoryToAssetManager(String folder,
			String suffix, Class clazz) {
		for (FileHandle child : assetsDir.child(folder).list(suffix)) {
			addFileEntry(child, clazz);
		}
	}

	private static void addFileEntry(FileHandle child, Class clazz) {
		if (!verifyFile(child)) {
			managedFiles.normal(cleanFilename(child.path()),
					checkForHDVersion(child), clazz);
		}
	}

	private static boolean verifyFile(FileHandle child) {
		if (!child.exists()) {
			throw new RuntimeException("File not found: " + child.path());
		} else if (child.name().contains("-hd")) {
			System.out.println("Skipping HD asset: " + child.path());
			return true;
		}
		return false;
	}

	public static String checkForHDVersion(FileHandle fileHandle) {
		String hdpiFileName = fileHandle.parent() + "/"
				+ fileHandle.nameWithoutExtension() + "-hd."
				+ fileHandle.extension();
		if (hdpiFileName.startsWith("/")) {
			hdpiFileName = hdpiFileName.substring(1);
		}

		if (new FileHandle(hdpiFileName).exists()) {
			return cleanFilename(hdpiFileName);
		}

		return null;
	}

	private static String cleanFilename(String fileName) {
		return fileName.replace("assets/", "");
	}
}
