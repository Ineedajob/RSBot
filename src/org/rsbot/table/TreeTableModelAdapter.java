package org.rsbot.table;

/*
 * @(#)TreeTableModelAdapter.java 1.2 98/10/27
 * 
 * Copyright 1997, 1998 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *  - Neither the name of Sun Microsystems nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;

/**
 * This is a wrapper class takes a TreeTableModel and implements the table model
 * interface. The implementation is trivial, with all of the event dispatching
 * support provided by the superclass: the AbstractTableModel.
 * 
 * @version 1.2 10/27/98
 * @author Philip Milne
 * @author Scott Violet
 */
public class TreeTableModelAdapter extends AbstractTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7665730921966220399L;
	JTree tree;
	TreeTableModel treeTableModel;

	public TreeTableModelAdapter(final TreeTableModel treeTableModel, final JTree tree) {
		this.tree = tree;
		this.treeTableModel = treeTableModel;

		tree.addTreeExpansionListener(new TreeExpansionListener() {
			public void treeCollapsed(final TreeExpansionEvent event) {
				fireTableDataChanged();
			}

			// Don't use fireTableRowsInserted() here; the selection model
			// would get updated twice.
			public void treeExpanded(final TreeExpansionEvent event) {
				fireTableDataChanged();
			}
		});

		// Install a TreeModelListener that can update the table when
		// tree changes. We use delayedFireTableDataChanged as we can
		// not be guaranteed the tree will have finished processing
		// the event before us.
		treeTableModel.addTreeModelListener(new TreeModelListener() {
			public void treeNodesChanged(final TreeModelEvent e) {
				delayedFireTableDataChanged();
			}

			public void treeNodesInserted(final TreeModelEvent e) {
				delayedFireTableDataChanged();
			}

			public void treeNodesRemoved(final TreeModelEvent e) {
				delayedFireTableDataChanged();
			}

			public void treeStructureChanged(final TreeModelEvent e) {
				delayedFireTableDataChanged();
			}
		});
	}

	// Wrappers, implementing TableModel interface.

	/**
	 * Invokes fireTableDataChanged after all the pending events have been
	 * processed. SwingUtilities.invokeLater is used to handle this.
	 */
	protected void delayedFireTableDataChanged() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				fireTableDataChanged();
			}
		});
	}

	@Override
	public Class<?> getColumnClass(final int column) {
		return treeTableModel.getColumnClass(column);
	}

	public int getColumnCount() {
		return treeTableModel.getColumnCount();
	}

	@Override
	public String getColumnName(final int column) {
		return treeTableModel.getColumnName(column);
	}

	public int getRowCount() {
		return tree.getRowCount();
	}

	public Object getValueAt(final int row, final int column) {
		return treeTableModel.getValueAt(nodeForRow(row), column);
	}

	@Override
	public boolean isCellEditable(final int row, final int column) {
		return treeTableModel.isCellEditable(nodeForRow(row), column);
	}

	protected Object nodeForRow(final int row) {
		final TreePath treePath = tree.getPathForRow(row);
		return treePath.getLastPathComponent();
	}

	@Override
	public void setValueAt(final Object value, final int row, final int column) {
		treeTableModel.setValueAt(value, nodeForRow(row), column);
	}
}
