/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.happydroids.droidtowers.DroidTowersGame;
import com.happydroids.droidtowers.TowerAssetManager;
import com.happydroids.droidtowers.TowerConsts;
import com.happydroids.droidtowers.achievements.AchievementEngine;
import com.happydroids.droidtowers.achievements.TutorialEngine;
import com.happydroids.droidtowers.controllers.AvatarLayer;
import com.happydroids.droidtowers.gamestate.GameState;
import com.happydroids.droidtowers.grid.GameGrid;
import com.happydroids.droidtowers.input.CameraController;
import com.happydroids.droidtowers.input.GestureTool;
import com.happydroids.droidtowers.input.InputSystem;
import com.happydroids.droidtowers.input.PickerTool;

public class HeadsUpDisplay extends WidgetGroup {
	private static final String TAG = HeadsUpDisplay.class.getSimpleName();

	private TextureAtlas hudAtlas;
	private OrthographicCamera camera;
	private GameGrid gameGrid;
	private static HeadsUpDisplay instance;
	private ToolTip mouseToolTip;
	private RadialMenu toolMenu;
	private final StackGroup notificationStack;
	private HudToolButton toolButton;
	private ImageButtonStyle toolButtonStyle;
	private final StatusBarPanel statusBarPanel;
	private final HeaderButtonBar headerButtonBar;
	private AchievementButton achievementButton;
	private ImageButton avatarsButton;
	private ImageButton viewNeighborsButton;
	private GridObjectPopOver gridObjectPopOver;
	private TutorialStepNotification tutorialStep;

	public HeadsUpDisplay(Stage stage, OrthographicCamera camera, CameraController cameraController, GameGrid gameGrid, final AvatarLayer avatarLayer,
			AchievementEngine achievementEngine, TutorialEngine tutorialEngine, final GameState gameState) {
		super();

		HeadsUpDisplay.instance = this;

		notificationStack = new StackGroup();

		this.setStage(stage);
		this.camera = camera;
		this.gameGrid = gameGrid;

		hudAtlas = TowerAssetManager.textureAtlas("hud/buttons.txt");

		statusBarPanel = new StatusBarPanel();
		statusBarPanel.setX(0);
		statusBarPanel.setY(stage.getHeight() - statusBarPanel.getHeight());
		addActor(statusBarPanel);

		mouseToolTip = new ToolTip();
		addActor(mouseToolTip);
		addActor(new ExpandLandOverlay(this.gameGrid, avatarLayer, cameraController));

		buildToolButtonMenu();

		headerButtonBar = new HeaderButtonBar(hudAtlas, gameGrid);
		addActor(headerButtonBar);
		headerButtonBar.setX(stage.getWidth() - headerButtonBar.getWidth() - 10);
		headerButtonBar.setY(stage.getHeight() - headerButtonBar.getHeight() - 10);

		achievementButton = new AchievementButton(hudAtlas, achievementEngine);
		achievementButton.setX(10);
		achievementButton.setY(stage.getHeight() - statusBarPanel.getHeight() - achievementButton.getHeight() - 10);
		achievementButton.getParticleEffect().setPosition(achievementButton.getX() + achievementButton.getWidth() / 2,
				achievementButton.getY() + achievementButton.getHeight() / 2);

		addActor(achievementButton);

		if (TowerConsts.ENABLE_AVATAR_LIST_WINDOW) {
			avatarsButton = TowerAssetManager.imageButton(hudAtlas.findRegion("view-neighbors"));
			avatarsButton.setX(10);
			avatarsButton.setY(achievementButton.getY() - avatarsButton.getHeight() - 10);

			avatarsButton.addListener(new VibrateClickListener() {
				@Override
				public void onClick(InputEvent event, float x, float y) {
					new AvatarListWindow(getStage(), avatarLayer).show();
				}
			});

			addActor(avatarsButton);
		}

		if (TowerConsts.DEBUG) {
			ImageButton debugButton = TowerAssetManager.imageButton(hudAtlas.findRegion("debug-menu"));
			debugButton.layout();
			debugButton.setX(achievementButton.getX() + achievementButton.getWidth() + 10);
			debugButton.setY(achievementButton.getY());
			debugButton.addListener(new VibrateClickListener() {
				@Override
				public void onClick(InputEvent event, float x, float y) {
					new DebugWindow(DroidTowersGame.getRootUiStage()).show();
				}
			});

			addActor(debugButton);
		}

		notificationStack.pad(10);
		notificationStack.setX(0);
		notificationStack.setY(0);
		addActor(notificationStack);

		this.getStage().addActor(this);
	}

	private void buildToolButtonMenu() {
		toolButton = new HudToolButton(hudAtlas);
		toolButton.setX(getStage().getWidth() - toolButton.getWidth() - 10);
		toolButton.setY(10);
		addActor(toolButton);

		toolMenu = new ToolMenu(hudAtlas, toolButton);

		toolButtonStyle = toolButton.getStyle();
		toolButton.addListener(new VibrateClickListener() {
			public void onClick(InputEvent event, float x, float y) {
				Gdx.app.log(TAG, "Current tool: " + InputSystem.instance().getCurrentTool());
				if (InputSystem.instance().getCurrentTool() instanceof PickerTool) {
					if (toolMenu.isVisible()) {
						toolMenu.close();
						toolMenu.remove();
					} else {
						getStage().addActor(toolMenu);
						toolMenu.setX(toolButton.getX() + 20f);
						toolMenu.setY(toolButton.getY());
						toolMenu.show();
						TutorialEngine.instance().moveToStepWhenReady("tutorial-unlock-lobby");
					}
				} else {
					InputSystem.instance().switchTool(GestureTool.PICKER, null);
				}
			}
		});
	}

	public static void showToast(String message, Object... objects) {
		Toast toast = new Toast();
		toast.setMessage(String.format(message, objects));
		instance().getStage().addActor(toast);
		toast.show();
	}

	public float getPrefWidth() {
		return getStage().getWidth();
	}

	public float getPrefHeight() {
		return getStage().getHeight();
	}

	public static StackGroup getNotificationStack() {
		return instance.notificationStack;
	}

	public static HeadsUpDisplay instance() {
		return instance;
	}

	public void setTutorialStepNotification(TutorialStepNotification nextStep) {
		if (tutorialStep != null) {
			tutorialStep.remove();
		}

		tutorialStep = nextStep;

		addActor(nextStep);

		nextStep.pack();
		nextStep.setX(20);
		nextStep.setY(statusBarPanel.getY() - nextStep.getHeight() - 20);
	}

	public AchievementButton getAchievementButton() {
		return achievementButton;
	}

	public GridObjectPopOver getGridObjectPopOver() {
		return gridObjectPopOver;
	}

	public void toggleViewNeighborsButton(boolean state) {
		// noinspection PointlessBooleanExpression
		if (TowerConsts.ENABLE_HAPPYDROIDS_CONNECT && viewNeighborsButton != null) {
			viewNeighborsButton.setVisible(state);
		}
	}
}
