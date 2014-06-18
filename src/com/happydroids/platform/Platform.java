/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.platform;/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

import java.io.File;

public class Platform {
	public static Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
	public static PlatformBrowserUtil browserUtil;
	public static PlatformProtocolHandler protocolHandler;
	private static PlatformDialogOpener dialogOpener;

	public static Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
		return uncaughtExceptionHandler;
	}

	public static PlatformBrowserUtil getBrowserUtil() {
		return browserUtil;
	}

	public static void setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
		Platform.uncaughtExceptionHandler = uncaughtExceptionHandler;
	}

	public static void setBrowserUtil(PlatformBrowserUtil browserUtil) {
		Platform.browserUtil = browserUtil;
	}

	public static void setProtocolHandler(PlatformProtocolHandler protocolHandler) {
		Platform.protocolHandler = protocolHandler;
	}

	public static PlatformProtocolHandler getProtocolHandler() {
		return protocolHandler;
	}

	public static Platforms getOSType() {
		String os = System.getProperty("os.name").toLowerCase();

		if (os.contains("mac")) {
			return Platforms.Mac;
		} else if (os.contains("win")) {
			return Platforms.Windows;
		} else if (os.contains("nix") || os.contains("nux") || os.contains("sun")) {
			return Platforms.Unix;
		}

		return Platforms.Unknown;
	}

	public static File getAppRoot() {
		String userHome = System.getProperty("user.home");
		String appName = "DroidTowers";
		String path = null;
		switch (getOSType()) {
		case Mac:
			path = String.format("%s/Library/Application Support/%s", userHome, appName);
			break;
		case Unix:
			path = String.format("%s/.%s", userHome, appName);
			break;
		case Windows:
			String appDataPath = System.getenv("APPDATA");
			if (appDataPath != null) {
				path = String.format("%s/.%s", appDataPath, appName);
			} else {
				path = String.format("%s/.%s", userHome, appName);
			}
			break;
		}

		File workingDir = new File(path);
		if (!workingDir.exists()) {
			boolean madeDir = workingDir.mkdir();
			if (!madeDir) {
				throw new RuntimeException("Could not create the required local storage.");
			}
		}

		return workingDir;
	}

	public static void dispose() {
		protocolHandler = null;
		browserUtil = null;
		uncaughtExceptionHandler = null;
	}

	public static PlatformDialogOpener getDialogOpener() {
		return dialogOpener;
	}

	public static void setDialogOpener(PlatformDialogOpener dialogOpener) {
		Platform.dialogOpener = dialogOpener;
	}

}
