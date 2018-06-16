package net.mggk.fnukysplitter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class MainForm {


    private JPanel MainPanel;
    private JTextField tfFileDir;
    private JLabel lblAppTitle;
    private JLabel lblSelectFile;
    private JButton btnBrowseFile;
    private JButton btnStartSplitter;
    private JLabel lblSpoof;
    private JTextField tfSpoof;

    //App entry point
    public static void main(String[] args){
        JFrame frame = new JFrame("MainForm");
        frame.setContentPane(new MainForm().MainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("FNUKY SPLITTER - V0.1");
        frame.pack();
        frame.setVisible(true);
    }
    //Class constructor
    public MainForm(){

        //Setting windows style
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        //Clicking the browse xci file button
        btnBrowseFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                //Setting file filter
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                        "XCI File", "xci");
                chooser.setFileFilter(filter);
                int returnVal = chooser.showOpenDialog(null);
                //Setting file dir in the textField
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    tfFileDir.setText(chooser.getSelectedFile().getAbsolutePath());
                }
            }
        });

        //Clicking the go button
        btnStartSplitter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Checking if the textfield is filled
                if(tfFileDir.getText().isEmpty()){
                    JOptionPane.showMessageDialog(null, "Please select a file to split & patch !");
                    return;
                }
                //Checking if the file exists
                File xciFile = new File(tfFileDir.getText());
                if(xciFile.exists() == false || xciFile.isDirectory() == true){
                    JOptionPane.showMessageDialog(null, "The selected file doesn't exist !");
                    return;
                }
                //If you're getting here, well everything is ready for splitting, we're settings tool directory
                String toolPath = System.getProperty("user.dir").concat("\\tools");
            }
        });
    }
}
