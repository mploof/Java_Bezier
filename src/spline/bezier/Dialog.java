package spline.bezier;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

public class Dialog extends JDialog {

    static Bezier        bezier;
    static BezierUI      graph;

    private final JPanel contentPanel = new JPanel();
    private JTextField   maxYField;
    private JTextField   minYField;
    private JTextField   maxXField;
    private JTextField   minXField;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            Dialog dialog = new Dialog();
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the dialog.
     */
    public Dialog() {
        setBounds(100, 100, 555, 466);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        {
            initBezier();
            contentPanel.setLayout(new MigLayout("", "[50][][424px,grow]", "[grow][][50]"));
            {
                JPanel panel = new JPanel();
                contentPanel.add(panel, "cell 0 0,grow");
                panel.setLayout(new MigLayout("", "[grow]", "[][][grow][][]"));
                {
                    JLabel lblMax = new JLabel("Max");
                    panel.add(lblMax, "cell 0 0,alignx center");
                }
                {
                    maxYField = new JTextField();
                    maxYField.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent arg0) {
                            graph.setMaxY(Integer.parseInt(maxYField.getText()));
                        }
                    });
                    panel.add(maxYField, "cell 0 1,growx");
                    maxYField.setColumns(10);
                }
                {
                    JLabel lblMin = new JLabel("Min");
                    panel.add(lblMin, "cell 0 3,alignx center");
                }
                {
                    minYField = new JTextField();
                    minYField.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            graph.setMinY(Integer.parseInt(minYField.getText()));
                        }
                    });
                    panel.add(minYField, "cell 0 4,growx");
                    minYField.setColumns(10);
                }
            }
            contentPanel.add(graph, "cell 2 0,grow");
        }
        {
            JPanel panel = new JPanel();
            contentPanel.add(panel, "cell 2 2,grow");
            panel.setLayout(new MigLayout("", "[40][grow][40]", "[][]"));
            {
                JLabel lblMin_1 = new JLabel("Min");
                panel.add(lblMin_1, "cell 0 0,alignx center");
            }
            {
                JLabel lblMax_1 = new JLabel("Max");
                panel.add(lblMax_1, "cell 2 0,alignx center");
            }
            {
                minXField = new JTextField();
                minXField.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        graph.setMinX(Integer.parseInt(minXField.getText()));
                    }
                });
                panel.add(minXField, "cell 0 1,growx");
                minXField.setColumns(10);
            }
            {
                maxXField = new JTextField();
                maxXField.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        graph.setMaxX(Integer.parseInt(maxXField.getText()));
                    }
                });
                panel.add(maxXField, "cell 2 1,growx");
                maxXField.setColumns(10);
            }
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        defineBezier();
                    }
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }

        }

        // Setup the text fields
        minXField.setText(Integer.toString(graph.getMinX()));
        maxXField.setText(Integer.toString(graph.getMaxX()));
        minYField.setText(Integer.toString(graph.getMinY()));
        maxYField.setText(Integer.toString(graph.getMaxY()));
    }

    private void initBezier() {
        bezier = new Bezier();
        graph = new BezierUI();
        bezier.registerObserver(graph);
    }

    private void defineBezier() {

        bezier.setKnotCount(3);
        bezier.setNextX(0);
        bezier.setNextX(10f);
        bezier.setNextX(20f);
        bezier.setNextX(50f);

        bezier.setNextX(60f);
        bezier.setNextX(70f);
        bezier.setNextX(100f);

        bezier.setNextY(0f);
        bezier.setNextY(75f);
        bezier.setNextY(25f);
        bezier.setNextY(50f);

        bezier.setNextY(10f);
        bezier.setNextY(30f);
        bezier.setNextY(100f);
    }

}
