package org.freeplane.plugin.workspace.config.creator;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.util.LogUtils;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.config.node.FilesystemFolderNode;
import org.freeplane.plugin.workspace.config.node.WorkspaceNode;

public class FilesystemFolderCreator extends ConfigurationNodeCreator {

	public FilesystemFolderCreator(IndexedTree tree) {
		super(tree);
	}

	@Override
	public WorkspaceNode getNode(String id, XMLElement data) {
		FilesystemFolderNode node = new FilesystemFolderNode(id);
		String name = data.getAttribute("name", null);
		node.setName(name==null? "folder" : name);
		String path = data.getAttribute("path", null);
				
		if(path!=null) {
			LogUtils.info("FilesystemPath: "+path);
			URL url = null;
			try {
				url = new URL("file:///"+path);
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
			}
			node.setFolderPath(url);
		}
		return node;
	}
	
	public void endElement(final Object parent, final String tag, final Object userObject, final XMLElement lastBuiltElement) {
		super.endElement(parent, tag, userObject, lastBuiltElement);
		final Path path = (Path)userObject;
		if (path.path == null) {
			return;
		}
		final DefaultMutableTreeNode treeNode = tree.get(path.path);
		if(treeNode.getChildCount() == 0) {
			treeNode.add(new DefaultMutableTreeNode(new Boolean(true)));
		}
		
	}

}