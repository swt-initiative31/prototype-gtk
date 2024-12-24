package org.eclipse.swt.tests.junit.performance;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class CustomDrawingPerformanceTest {

    private static final int NUM_RECTANGLES = 100000; // Number of shapes to draw

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Custom Drawing Performance Test");
        shell.setSize(800, 600);
        shell.setLayout(null);

        // Create a Canvas for custom drawing
        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(10, 10, 780, 580);

        // Attach a PaintListener for custom drawing
        canvas.addListener(SWT.Paint, event -> {
            long startTime = System.currentTimeMillis();
            GC gc = event.gc;
            Color color = display.getSystemColor(SWT.COLOR_BLUE);
            gc.setBackground(color);

            // Draw NUM_RECTANGLES rectangles
            for (int i = 0; i < NUM_RECTANGLES; i++) {
                int x = (int) (Math.random() * 780);
                int y = (int) (Math.random() * 580);
                int width = (int) (Math.random() * 50);
                int height = (int) (Math.random() * 50);
                gc.fillRectangle(x, y, width, height);
            }

            long endTime = System.currentTimeMillis();
            System.out.println("Custom drawing took: " + (endTime - startTime) + " ms");
        });

        shell.open();

        // Trigger continuous redraws to simulate intensive custom drawing
        display.timerExec(100, new Runnable() {
            @Override
            public void run() {
                if (!shell.isDisposed()) {
                    canvas.redraw();
                    display.timerExec(100, this); // Schedule next execution
                }
            }
        });

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        display.dispose();
    }
}

