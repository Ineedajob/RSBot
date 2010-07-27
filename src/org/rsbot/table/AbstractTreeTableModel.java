package org.rsbot.table;

/*
 * @(#)AbstractTreeTableModel.java 1.2 98/10/27
 * 
 * Copyright 1997, 1998 by Sun Microsystems, Inc., 901 San Antonio Road, Palo
 * Alto, California, 94303, U.S.A. All rights reserved.
 * 
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with Sun.
 */

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

/**
 * @version 1.2 10/27/98 An abstract implementation of the TreeTableModel
 *          interface, handling the list of listeners.
 * @author Philip Milne
 */

public abstract class AbstractTreeTableModel implements TreeTableModel {
	protected EventListenerList listenerList = new EventListenerList();
	protected Object root;

	public AbstractTreeTableModel(final Object root) {
		this.root = root;
	}

	//
	// Default implmentations for methods in the TreeModel interface.
	//

	public void addTreeModelListener(final TreeModelListener l) {
		listenerList.add(TreeModelListener.class, l);
	}

	/*
	 * Notify all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * parameters passed into the fire method.
	 * 
	 * @see EventListenerList
	 */
	protected void fireTreeNodesChanged(final Object source, final Object[] path, final int[] childIndices, final Object[] children) {
		// Guaranteed to return a non-null array
		final Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				// Lazily create the event:
				if (e == null) {
					e = new TreeModelEvent(source, path, childIndices, children);
				}
				((TreeModelListener) listeners[i + 1]).treeNodesChanged(e);
			}
		}
	}

	/*
	 * Notify all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * parameters passed into the fire method.
	 * 
	 * @see EventListenerList
	 */
	protected void fireTreeNodesInserted(final Object source, final Object[] path, final int[] childIndices, final Object[] children) {
		// Guaranteed to return a non-null array
		final Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				// Lazily create the event:
				if (e == null) {
					e = new TreeModelEvent(source, path, childIndices, children);
				}
				((TreeModelListener) listeners[i + 1]).treeNodesInserted(e);
			}
		}
	}

	/*
	 * Notify all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * parameters passed into the fire method.
	 * 
	 * @see EventListenerList
	 */
	protected void fireTreeNodesRemoved(final Object source, final Object[] path, final int[] childIndices, final Object[] children) {
		// Guaranteed to return a non-null array
		final Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				// Lazily create the event:
				if (e == null) {
					e = new TreeModelEvent(source, path, childIndices, children);
				}
				((TreeModelListener) listeners[i + 1]).treeNodesRemoved(e);
			}
		}
	}

	/*
	 * Notify all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * parameters passed into the fire method.
	 * 
	 * @see EventListenerList
	 */
	protected void fireTreeStructureChanged(final Object source, final Object[] path, final int[] childIndices, final Object[] children) {
		// Guaranteed to return a non-null array
		final Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				// Lazily create the event:
				if (e == null) {
					e = new TreeModelEvent(source, path, childIndices, children);
				}
				((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
			}
		}
	}

	public Class<?> getColumnClass(final int column) {
		if (column == 0)
			return TreeTableModel.class;
		return Object.class;
	}

	// This is not called in the JTree's default mode: use a naive
	// implementation.
	public int getIndexOfChild(final Object parent, final Object child) {
		for (int i = 0; i < getChildCount(parent); i++) {
			if (getChild(parent, i).equals(child))
				return i;
		}
		return -1;
	}

	public Object getRoot() {
		return root;
	}

	/**
	 * By default, make the column with the Tree in it the only editable one.
	 * Making this column editable causes the JTable to forward mouse and
	 * keyboard events in the Tree column to the underlying JTree.
	 */
	public boolean isCellEditable(final Object node, final int column) {
		return column == 0;// getColumnClass(column) == TreeTableModel.class;
	}

	public boolean isLeaf(final Object node) {
		return getChildCount(node) == 0;
	}

	//
	// Default impelmentations for methods in the TreeTableModel interface.
	//

	public void removeTreeModelListener(final TreeModelListener l) {
		listenerList.remove(TreeModelListener.class, l);
	}

	public void setValueAt(final Object aValue, final Object node, final int column) {
	}

	public void valueForPathChanged(final TreePath path, final Object newValue) {
	}

	// Left to be implemented in the subclass:

	/*
	 * public Object getChild(Object parent, int index) public int
	 * getChildCount(Object parent) public int getColumnCount() public String
	 * getColumnName(Object node, int column) public Object getValueAt(Object
	 * node, int column)
	 */
}
