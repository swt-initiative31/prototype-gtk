package org.eclipse.swt.tests.junit.performance;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class TextRenderingPerformanceTest {

    private static final int NUM_LABELS = 1000; // Number of labels for the test
    private static final int NUM_TEXT_FIELDS = 1000; // Number of text fields for the test
    private static final int LARGE_TEXT_SIZE = 10000; // Size of large text for testing

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Text Rendering Performance Test");
        shell.setSize(800, 600);
        shell.setLayout(null);

        // Test rendering performance of Label
        long labelStartTime = System.currentTimeMillis();
        for (int i = 0; i < NUM_LABELS; i++) {
            Label label = new Label(shell, SWT.NONE);
            label.setText("Label " + i);
            label.setBounds(10, i * 20, 200, 20);
        }
        long labelEndTime = System.currentTimeMillis();
        System.out.println("Rendering " + NUM_LABELS + " Labels took: " + (labelEndTime - labelStartTime) + " ms");

        // Test rendering performance of Text widget
        long textStartTime = System.currentTimeMillis();
        for (int i = 0; i < NUM_TEXT_FIELDS; i++) {
            Text text = new Text(shell, SWT.BORDER);
            text.setText("Text Field " + i);
            text.setBounds(220, i * 20, 200, 20);
        }
        long textEndTime = System.currentTimeMillis();
        System.out.println("Rendering " + NUM_TEXT_FIELDS + " Text fields took: " + (textEndTime - textStartTime) + " ms");

        // Test rendering performance of StyledText with large text
        StyledText styledText = new StyledText(shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        styledText.setBounds(10, 500, 760, 60);
        styledText.setFont(new Font(display, "Arial", 12, SWT.NORMAL));
        StringBuilder largeText = new StringBuilder();
        for (int i = 0; i < LARGE_TEXT_SIZE; i++) {
            largeText.append("Line ").append(i).append("\n");
        }
        long styledTextStartTime = System.currentTimeMillis();
        styledText.setText(largeText.toString());
        long styledTextEndTime = System.currentTimeMillis();
        System.out.println("Rendering large text in StyledText took: " + (styledTextEndTime - styledTextStartTime) + " ms");

        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        display.dispose();
    }
}
