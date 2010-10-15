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
package org.freeplane.features.mindmapmode.map;

import java.awt.EventQueue;
import java.util.Timer;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.core.undo.UndoHandler;
import org.freeplane.core.util.SysUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.url.UrlManager;
import org.freeplane.features.mindmapmode.file.DoAutomaticSave;
import org.freeplane.features.mindmapmode.file.DummyLockManager;
import org.freeplane.features.mindmapmode.file.LockManager;
import org.freeplane.features.mindmapmode.file.MFileManager;

public class MMapModel extends MapModel {
	private static int unnamedMapsNumber = 1;
	private LockManager lockManager;
	private Timer timerForAutomaticSaving;
	private int titleNumber = 0;

	/**
	 * The current version and all other version that don't need XML update for
	 * sure.
	 */
	MMapModel( final NodeModel root) {
		super(root);
		addExtension(IUndoHandler.class, new UndoHandler());
		setReadOnly(false);
		this.setLockManager(ResourceController.getResourceController().getBooleanProperty(
		    "experimental_file_locking_on") ? new LockManager() : new DummyLockManager());
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				scheduleTimerForAutomaticSaving();
			}
		});
	}

	/**
	 * When a map is closed, this method is called.
	 *
	 * @param mindMapMapModel
	 */
	@Override
	public void destroy() {
		getLockManager().releaseLock();
		getLockManager().releaseTimer();
		/* cancel the timer, if map is closed. */
		if (getTimerForAutomaticSaving() != null) {
			getTimerForAutomaticSaving().cancel();
		}
		super.destroy();
	}

	public LockManager getLockManager() {
		return lockManager;
	}

	public Timer getTimerForAutomaticSaving() {
		return timerForAutomaticSaving;
	}

	@Override
	public String getTitle() {
		if (getURL() != null) {
			return getFile().getName();
		}
		if (titleNumber == 0) {
			titleNumber = MMapModel.unnamedMapsNumber++;
		}
		return TextUtils.getText("mindmap") + titleNumber;
	}

	public void scheduleTimerForAutomaticSaving() {
		if (!(UrlManager.getController() instanceof MFileManager)) {
			return;
		}
		final int numberOfTempFiles = Integer.parseInt(ResourceController.getResourceController().getProperty(
		    "number_of_different_files_for_automatic_save"));
		if (numberOfTempFiles == 0) {
			return;
		}
		final boolean filesShouldBeDeletedAfterShutdown = ResourceController.getResourceController()
		    .getBooleanProperty("delete_automatic_saves_at_exit");
		final int delay = Integer.parseInt(ResourceController.getResourceController().getProperty(
		    "time_for_automatic_save"));
		if (delay == 0) {
			return;
		}
		final boolean useSingleDirectory = ResourceController.getResourceController().getBooleanProperty(
		    "single_backup_directory");
		final Timer timer = SysUtils.createTimer("TimerForAutomaticSaving");
		timer.schedule(new DoAutomaticSave(this, numberOfTempFiles, filesShouldBeDeletedAfterShutdown,
		    useSingleDirectory), delay, delay);
		this.setTimerForAutomaticSaving(timer);
	}

	void setLockManager(final LockManager lockManager) {
		this.lockManager = lockManager;
	}

	void setTimerForAutomaticSaving(final Timer timerForAutomaticSaving) {
		this.timerForAutomaticSaving = timerForAutomaticSaving;
	}
}
