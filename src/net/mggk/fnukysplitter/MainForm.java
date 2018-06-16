package net.mggk.fnukysplitter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainForm {


    private JPanel MainPanel;
    private JTextField tfFileDir;
    private JLabel lblAppTitle;
    private JLabel lblSelectFile;
    private JButton btnBrowseFile;
    private JButton btnStartSplitter;
    private JLabel lblSpoof;
    private JTextField tfSpoof;
    private JTextArea taLogs;

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
                btnStartSplitter.setEnabled(false);
                extractXCI();
            }
        });
    }

    public void extractXCI(){
        SwingWorker sw = new SwingWorker(){
            protected Object doInBackground() throws Exception {
                //Getting file info
                File xciFile = new File(tfFileDir.getText());
                //If you're getting here, well everything is ready for splitting, we're settings tool directory
                String toolPath = System.getProperty("user.dir").concat("\\tools");
                //Generating first hactool command to extract game
                ProcessBuilder hacTool = new ProcessBuilder(
                        "cmd.exe", "/c", "cd \""+toolPath+"\" && hactool.exe -k \""+toolPath+"\\keys.txt\" -t xci \""+xciFile.getAbsolutePath()+"\" --outdir=\""+toolPath+"\\out\"");
                hacTool.redirectErrorStream(true);
                //Creating process and execute it
                Process p = null;
                try {
                    p = hacTool.start();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                //Reading output and put this in the log textarea
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = null;
                while (true) {
                    try {
                        line = r.readLine();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    if (line == null) { break; }
                    taLogs.append(line.concat(System.getProperty("line.separator")));
                }
                return null;
            }
            public void done(){
                if(SwingUtilities.isEventDispatchThread()){
                    btnStartSplitter.setEnabled(true);
                    JOptionPane.showMessageDialog(null, "Game Ready :D");
                }

            }
        };
        sw.execute();
    }
}
