package org.eclipse.swt.tests.junit.performance;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class DragAndDropPerformanceTest {

    private static final int NUM_ITEMS = 1000; // Number of items to create for testing

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Drag-and-Drop Performance Test");
        shell.setSize(800, 600);
        shell.setLayout(null);

        // Create a Tree widget
        Tree tree = new Tree(shell, SWT.BORDER);
        tree.setBounds(10, 10, 350, 550);
        for (int i = 0; i < NUM_ITEMS; i++) {
            TreeItem item = new TreeItem(tree, SWT.NONE);
            item.setText("Tree Item " + i);
        }

        // Create a Table widget
        Table table = new Table(shell, SWT.BORDER | SWT.MULTI);
        table.setBounds(400, 10, 350, 550);
        for (int i = 0; i < NUM_ITEMS; i++) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText("Table Item " + i);
        }

        // Enable drag support on Tree
        DragSource dragSource = new DragSource(tree, DND.DROP_MOVE | DND.DROP_COPY);
        dragSource.setTransfer(new Transfer[] { TextTransfer.getInstance() });
        dragSource.addDragListener(new DragSourceListener() {
            @Override
            public void dragStart(DragSourceEvent event) {
                if (tree.getSelectionCount() == 0) {
                    event.doit = false;
                }
            }

            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = tree.getSelection()[0].getText();
            }

            @Override
            public void dragFinished(DragSourceEvent event) {
                if (event.detail == DND.DROP_MOVE) {
                    tree.getSelection()[0].dispose();
                }
            }
        });

        // Enable drop support on Table
        DropTarget dropTarget = new DropTarget(table, DND.DROP_MOVE | DND.DROP_COPY);
        dropTarget.setTransfer(new Transfer[] { TextTransfer.getInstance() });
        dropTarget.addDropListener(new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetEvent event) {
                event.detail = DND.DROP_COPY;
            }

            @Override
            public void dragLeave(DropTargetEvent event) {}

            @Override
            public void dragOver(DropTargetEvent event) {}

            @Override
            public void dropAccept(DropTargetEvent event) {}

            @Override
            public void drop(DropTargetEvent event) {
                if (event.data instanceof String) {
                    TableItem item = new TableItem(table, SWT.NONE);
                    item.setText((String) event.data);
                }
            }

			@Override
			public void dragOperationChanged(DropTargetEvent event) {
				// TODO Auto-generated method stub

			}
        });

        // Simulate drag-and-drop performance
        shell.open();
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < NUM_ITEMS; i++) {
            simulateDragAndDrop(tree, table);
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Drag-and-Drop performance test completed.");
        System.out.println("Total time for " + NUM_ITEMS + " operations: " + (endTime - startTime) + " ms");

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        display.dispose();
    }

    private static void simulateDragAndDrop(Tree tree, Table table) {
        while (tree.getItemCount() > 0) { // Process items until Tree is empty
            TreeItem treeItem = tree.getItem(0); // Always pick the first item
            if (treeItem != null) {
                // Simulate "dragSetData" and "drop" events
                String text = treeItem.getText();
                TableItem tableItem = new TableItem(table, SWT.NONE);
                tableItem.setText(text);

                // Simulate "dragFinished" (move operation)
                treeItem.dispose(); // Dispose the processed TreeItem
            }
        }
    }

}

