/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.mindmapmode.file;

import java.awt.EventQueue;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.TimerTask;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.url.UrlManager;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.map.MMapModel;

public class DoAutomaticSave extends TimerTask {
	static final String AUTOSAVE_EXTENSION = "autosave";
	/**
	 * This value is compared with the result of
	 * getNumberOfChangesSinceLastSave(). If the values coincide, no further
	 * automatic saving is performed until the value changes again.
	 */
	private int changeState;
	final private boolean filesShouldBeDeletedAfterShutdown;
	final private MapModel model;
	final private int numberOfFiles;
	final private boolean useSingleDirectory;
	static final String SINGLE_BACKUP_DIR = ".backup";
	static final String BACKUP_DIR = ".backup";

	public DoAutomaticSave( final MapModel model, final int numberOfTempFiles,
	                       final boolean filesShouldBeDeletedAfterShutdown, boolean useSingleDirectory) {
		this.model = model;
		numberOfFiles = ((numberOfTempFiles > 0) ? numberOfTempFiles : 1);
		this.filesShouldBeDeletedAfterShutdown = filesShouldBeDeletedAfterShutdown;
		this.useSingleDirectory = useSingleDirectory;
		changeState = model.getNumberOfChangesSinceLastSave();
	}

	@Override
	public void run() {
		/* Map is dirty enough? */
		if (model.getNumberOfChangesSinceLastSave() == changeState) {
			return;
		}
		changeState = model.getNumberOfChangesSinceLastSave();
		if (changeState == 0) {
			/* map was recently saved. */
			return;
		}
		try {
			cancel();
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					/* Now, it is dirty, we save it. */
					try {
						final File pathToStore;
						final URL url = model.getURL();
						final File file = new File(url != null ? url.getFile() //
						        : model.getTitle() + UrlManager.FREEPLANE_FILE_EXTENSION);
						if (url == null || useSingleDirectory) {
							final String freeplaneUserDirectory = ResourceController.getResourceController()
							    .getFreeplaneUserDirectory();
							pathToStore = new File(freeplaneUserDirectory, SINGLE_BACKUP_DIR);
						}
						else {
							pathToStore = new File(file.getParent(), BACKUP_DIR);
						}
						pathToStore.mkdirs();
						final File tempFile = MFileManager.renameBackupFiles(pathToStore, file, numberOfFiles,
						    AUTOSAVE_EXTENSION);
						if (tempFile == null) {
							return;
						}
						if (filesShouldBeDeletedAfterShutdown) {
							tempFile.deleteOnExit();
						}
						MModeController modeController = ((MModeController) Controller.getCurrentModeController());
						((MFileManager) UrlManager.getController())
						    .saveInternal((MMapModel) model, tempFile, true /*=internal call*/);
						modeController.getController().getViewController()
						    .out(TextUtils.format("automatically_save_message", tempFile));
					}
					catch (final Exception e) {
						LogUtils.severe("Error in automatic MapModel.save(): ", e);
					}
				}
			});
		}
		catch (final InterruptedException e) {
			LogUtils.severe(e);
		}
		catch (final InvocationTargetException e) {
			LogUtils.severe(e);
		}
	}
}
