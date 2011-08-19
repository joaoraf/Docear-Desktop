package org.docear.plugin.pdfutilities.actions;

import java.awt.event.ActionEvent;
import java.net.URI;

import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

@EnabledAction( checkOnNodeChange = true )
public class UpdateMonitoringFolderAction extends AbstractMonitoringAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UpdateMonitoringFolderAction(String key) {
		super(key);		
	}

	public void actionPerformed(ActionEvent e) {
		NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
		URI pdfDir = NodeUtils.getPdfDirFromMonitoringNode(selected);
		URI mindmapDir = NodeUtils.getMindmapDirFromMonitoringNode(selected);
		this.updateNodesAgainstMonitoringDir(selected, pdfDir, mindmapDir);
	}

	@Override
	public void setEnabled() {
		NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
		if(selected == null){
			this.setEnabled(false);
		}
		else{
			this.setEnabled(NodeUtils.isMonitoringNode(selected));
		}
	}

}