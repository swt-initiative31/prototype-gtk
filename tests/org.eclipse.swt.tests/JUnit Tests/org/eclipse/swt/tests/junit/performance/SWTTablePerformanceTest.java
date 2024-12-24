package org.eclipse.swt.tests.junit.performance;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class SWTTablePerformanceTest {

    private static final int NUM_ROWS = 100000;  // Number of rows to populate
    private static final int NUM_COLUMNS = 10; // Number of columns

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("SWT Table Performance Test");
        shell.setSize(800, 600);

        Table table = new Table(shell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
        table.setBounds(10, 10, 760, 540);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        // Create columns
        for (int i = 0; i < NUM_COLUMNS; i++) {
            TableColumn column = new TableColumn(table, SWT.NONE);
            column.setText("Column " + (i + 1));
            column.setWidth(100);
        }

        // Populate the table and measure time
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < NUM_ROWS; i++) {
            TableItem item = new TableItem(table, SWT.NONE);
            for (int j = 0; j < NUM_COLUMNS; j++) {
                item.setText(j, "Row " + i + " Col " + j);
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Table populated with " + NUM_ROWS + " rows in " + (endTime - startTime) + " ms");

        // Test sorting performance
        long sortStartTime = System.currentTimeMillis();
        table.getColumn(0).addListener(SWT.Selection, e -> table.setSortColumn(table.getColumn(0)));
        table.getColumn(0).notifyListeners(SWT.Selection, null);
        long sortEndTime = System.currentTimeMillis();
        System.out.println("Table sorted in " + (sortEndTime - sortStartTime) + " ms");

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}

