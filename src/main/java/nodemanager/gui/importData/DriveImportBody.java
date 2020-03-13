/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nodemanager.gui.importData;

import java.awt.Container;
import java.awt.GridLayout;
import java.util.ArrayList;
import javax.swing.*;
import files.FileType;
import files.VersionLog;
import files.WayfindingManifest;
import java.io.IOException;
import nodemanager.io.GoogleDriveUploader;

/**
 * Acts as the body of the import dialog whenever the user clicks the import from drive button.
 * Allows the user to specify which version of wayfinding they are importing from, as well as what to name the export.
 * 
 * TODO: combine this and ExportBody
 * @author Matt Crow
 */
public class DriveImportBody extends Container{
    private JComboBox<String> version;
    private JComboBox<String> exportSelector;
    private ArrayList<FileTypeCheckBox> cbs;
    private String[] exportIds;
    private String[] exportNames;
    
    private VersionLog v;
    
    private final JButton importButton;
    private final JTextArea msg;
    
    public DriveImportBody(){
        super();
        setLayout(new GridLayout(8, 1));
        
        
        version = new JComboBox<>();
        version.addItemListener((e)->{
            updateExportSelector();
        });
        add(version);
        
        exportSelector = new JComboBox<>();
        add(exportSelector);
        
        msg = new JTextArea();
        msg.setEditable(false);
        msg.setText("Please hold while I download the version log...");
        add(msg);
        
        cbs = new ArrayList<>();
        FileTypeCheckBox temp;
        for(FileType t : new FileType[]{
            FileType.NODE_COORD,
            FileType.NODE_CONN,
            FileType.LABEL,
            FileType.MAP_IMAGE
        }){
            temp = new FileTypeCheckBox(t);
            cbs.add(temp);
            add(temp);
        }
        
        importButton = new JButton("Import");
        importButton.addActionListener((e)->{            
            msg.setText("Beginning download...");
            WayfindingManifest man = new WayfindingManifest();
            GoogleDriveUploader.download(exportIds[exportSelector.getSelectedIndex()])
            .addOnSucceed((s)->{
                try {
                    man.setContents(s);
                    importManifest(man);
                    msg.setText("Done!");
                } catch (IOException ex) {
                    msg.setText(ex.getMessage());
                }
            }).addOnFail((err)->{
                msg.setText(err.getMessage());
            });
        });
        add(importButton);
        
        v = new VersionLog();
        v.download().addOnSucceed((stream)->{
            for(String type : v.getTypes()){
                version.addItem(type);
            }
            msg.setText("Ready to import!");
        });
        
        updateExportSelector();
    }
    
    private void importManifest(WayfindingManifest man){
        cbs.stream().filter((cb)->cb.isSelected()).filter((cb)->man.containsUrlFor(cb.getFileType())).forEach((cb)->{
            try {
                man.getFileFor(cb.getFileType()).addOnSucceed((file)->{
                    file.importData();
                }).getExcecutingThread().join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        });
    }
    
    private void updateExportSelector(){
        if(!v.isDownloaded()){
            return;
        }
        exportIds = v.getExportIdsFor(version.getSelectedItem().toString());
        exportNames = v.getExportNamesFor(version.getSelectedItem().toString());
        exportSelector.removeAllItems();
        
        //we want to order it by newest to oldest, so we have to reverse both of them
        for(int i = exportNames.length -1; i >= 0; i--){
            exportSelector.addItem(exportNames[i]);
        }
        
        String temp;
        for(int i = 0; i < exportIds.length / 2; i++){
            temp = exportIds[i];
            exportIds[i] = exportIds[exportIds.length - 1 - i];
            exportIds[exportIds.length - 1 - i] = temp;
        }
        revalidate();
        repaint();
    }
}
