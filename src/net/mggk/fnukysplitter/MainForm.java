package net.mggk.fnukysplitter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static java.nio.file.StandardCopyOption.*;

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
    private JScrollPane jspLogs;

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

        //Adding listener to the textArea to autoscroll logs
        taLogs.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {

            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                taLogs.setCaretPosition(taLogs.getDocument().getLength());
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {

            }
        });
    }

    public void extractXCI(){
        SwingWorker sw = new SwingWorker(){
            protected Object doInBackground() throws Exception {
                //If you're getting here, well everything is ready for splitting, we're settings tool directory
                //Extracting XCI
                appendToLog("-> EXTRACTING XCI");
                if(!executeHacToolCommad("-t xci \"#XCIFILE#\" --outdir=\"#TOOLPATH#\\out\"")){
                    JOptionPane.showMessageDialog(null, "Oups... An error occured, please check the logs");
                    return null;
                }
                //Getting main NCA file
                appendToLog("-> PREPARING NCA");
                if(!definingMainNCAFile()){
                    JOptionPane.showMessageDialog(null, "Oups... An error occured, please check the logs");
                    return null;
                }
                //Extracting romfs.bin and exeFS
                appendToLog("-> EXTRACTING NCA");
                if(!executeHacToolCommad("\"#FINALPATH#\\main.nca\" --romfs=\"#FINALPATH#\\romfs.bin\" --exefsdir=\"#FINALPATH#\\exefs\"")){
                    JOptionPane.showMessageDialog(null, "Oups... An error occured, please check the logs");
                    return null;
                }
                //Spoofing game
                appendToLog("-> SPOOFING NPDM");

                JOptionPane.showMessageDialog(null, "Game Ready :D");
                return null;
            }
            public void done(){
                if(SwingUtilities.isEventDispatchThread()){
                    btnStartSplitter.setEnabled(true);

                }

            }
        };
        sw.execute();
    }

    public boolean executeHacToolCommad(String cmd){
        //Getting file info
        String xciFile = tfFileDir.getText();
        //Getting tools directory
        String toolPath = System.getProperty("user.dir").concat("\\tools");
        //Settings final directory
        String finalPath = System.getProperty("user.dir").concat("\\tools\\final");
        //Replace tags
        cmd = cmd.replace("#XCIFILE#",xciFile);
        cmd = cmd.replace("#TOOLPATH#", toolPath);
        cmd = cmd.replace("#FINALPATH#", finalPath);
        //Generating first hactool command to extract game
        ProcessBuilder hacTool = new ProcessBuilder(
                "cmd.exe", "/c", "cd \""+toolPath+"\" && hactool.exe -k \""+toolPath+"\\keys.txt\" ".concat(cmd));
        hacTool.redirectErrorStream(true);
        //Creating process and execute it
        Process p = null;
        try {
            p = hacTool.start();
        } catch (IOException e1) {
            e1.printStackTrace();
            return false;
        }
        //Reading output and put this in the log textarea
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = null;
        while (true) {
            try {
                line = r.readLine();
            } catch (IOException e1) {
                return false;
            }
            if (line == null) { break; }
            appendToLog(line);
        }
        //Extracting
        return true;
    }


    public boolean definingMainNCAFile(){
        //Settings output directory
        String outPath = System.getProperty("user.dir").concat("\\tools\\out\\secure");
        String finalPath = System.getProperty("user.dir").concat("\\tools\\final");
        //Check if final path exists
        if(!Files.exists(Paths.get(finalPath))){
            try {
                Files.createDirectory(Paths.get(finalPath));
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        String fileName = null;
        Long largestFileSize = 0L;
        File searchDir = new File(outPath);
        //taLogs.append("Searching biggest NCA File".concat(System.getProperty("line.separator")));
        //Reading all files
        for (File aFile : searchDir.listFiles())
        {
            if (largestFileSize < aFile.length())
            {
                largestFileSize = aFile.length();
                fileName = aFile.getAbsolutePath();
            }
        }
        if(fileName == null){
            appendToLog("No NCA File Found");
            return false;
        }
        appendToLog("Biggest NCA File Found, starting copy");
        Path source = Paths.get(fileName);
        Path dest = Paths.get(finalPath.concat("\\main.nca"));
        try {
            //Let's move the file
            Files.move(source,dest,StandardCopyOption.REPLACE_EXISTING);
            //Checking if file exists
            if(!dest.toFile().exists()){
                appendToLog("Error while moving NCA");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        appendToLog("File Moved !");
        return true;
    }

    public boolean spoofingNCA(){

        return true;
    }

    public void appendToLog(String text){
        taLogs.append(text.concat(System.getProperty("line.separator")));
    }
}
