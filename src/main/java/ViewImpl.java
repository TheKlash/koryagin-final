import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ViewImpl implements View, Model.Callback{

    private JFrame frame;
    private JButton runButton;
    private JTextArea codeArea;
    private JTextArea resultArea;
    private JTextArea taskArea;
    private Callback callback;

    public ViewImpl() {
        frame = new JFrame();
        frame.setSize(800, 600);
        frame.setTitle("Решение уравнений");

        codeArea = new JTextArea(10, 20);
        runButton = new JButton("Запустить");
        runButton.addActionListener(new onRunClicked());
        resultArea = new JTextArea(10, 20);
        resultArea.setEditable(false);
        taskArea = new JTextArea(10, 20);
        taskArea.setEditable(false);

        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
        frame.add(new JLabel("Введите код"));
        frame.add(codeArea);
        frame.add(runButton);
        frame.add(new JLabel("Результат"));
        frame.add(resultArea);
        frame.add(new JLabel("Задание"));
        frame.add(taskArea);
    }

    //Public methods region

    public void run() {
        try {
            taskArea.append(loadTask());
        }
        catch (IOException e) {
            showError("Не удалось загрузить задание");
        }
        frame.setVisible(true);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    //Private methods region

    private String loadTask() throws IOException {

        String task = "";

        BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\Klash\\IdeaProjects\\koryginfinal\\src\\main\\java\\task"));
        String s;
        while ((s = reader.readLine()) != null)
            task = task.concat(s + "\n");
        reader.close();

        return task;
    }

    private void showError(String message, int pos) {
        Highlighter h = codeArea.getHighlighter();
        try {
            h.addHighlight(pos, pos+1, DefaultHighlighter.DefaultPainter);
        }
        catch (BadLocationException e) {
            e.printStackTrace();
        }
        JOptionPane.showMessageDialog(new JFrame(), message, "Ошибка!", JOptionPane.ERROR_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(new JFrame(), message, "Ошибка!", JOptionPane.ERROR_MESSAGE);
    }

    //Components listeners region
    private class onRunClicked implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            resultArea.setText(" ");
            Highlighter h = codeArea.getHighlighter();
            h.removeAllHighlights();
            String lines = codeArea.getText();
            callback.solve(lines);
        }
    }

    //Callbacks region

    public void onSuccess(ArrayList<Equation> results) {
        for (Equation result: results) {
            resultArea.append("УРАВНЕНИЕ " + result.getLeftVariable() + " РЕШЕНО" + "\n");
            resultArea.append(result.getLeftVariable() + "\n");
            String[] roots = result.getRoots();
            if (result.isErrorFlag())
                showError(result.getError(), result.getErrorPosition());
            else {
                if (roots == null)
                    resultArea.append("КОРНЕЙ НЕТ" + "\n");
                else {
                    String var = result.getRootVariable();
                    if (roots.length == 2) {
                        resultArea.append(var + "1 = " + roots[0] + " " + var + "2 = " + roots[1] + "\n");
                    } else {
                        resultArea.append(var + " = " + roots[0] + "\n");
                    }
                }
                resultArea.append("-----------------------------------------------------" + "\n");
            }
        }
    }

    public void onFailure(String message, Token failedToken) {
        showError(message, failedToken.getCharNum());
    }
}
