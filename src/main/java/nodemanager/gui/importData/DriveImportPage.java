/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nodemanager.gui.importData;

import java.awt.GridLayout;
import java.util.ArrayList;
import javax.swing.*;
import nodemanager.files.FileType;
import nodemanager.files.VersionLog;
import nodemanager.files.WayfindingManifest;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import nodemanager.NodeManager;
import nodemanager.files.MapFile;
import nodemanager.files.NodeConnFile;
import nodemanager.files.NodeCoordFile;
import nodemanager.files.NodeLabelFile;
import nodemanager.gui.ApplicationBody;
import nodemanager.io.GoogleDriveUploader;
import nodemanager.gui.ApplicationPage;
import nodemanager.model.Graph;

/**
 * Acts as the body of the import dialog whenever the user clicks the import from drive button.
 * Allows the user to specify which wayfindingTypeSelector of wayfinding they are importing from, as well as what to name the export.
 
 TODO: combine this and ExportBody
 * @author Matt Crow
 */
public class DriveImportPage extends ApplicationPage {
    private final JComboBox<String> wayfindingTypeSelector;
    private final JComboBox<String> exportSelector;
    private final ArrayList<FileTypeCheckBox> cbs;
    
    private VersionLog v;
    
    private final HashMap<String, String> nameToUrl;
    
    private final JButton importButton;
    private final JTextArea msg;
    
    public DriveImportPage(ApplicationBody parent){
        super(parent);
        setLayout(new GridLayout(8, 1));
        
        nameToUrl = new HashMap<>();
        
        wayfindingTypeSelector = new JComboBox<>();
        wayfindingTypeSelector.addItemListener((e)->{
            updateExportSelector();
        });
        add(wayfindingTypeSelector);
        
        exportSelector = new JComboBox<>();
        add(exportSelector);
        
        msg = new JTextArea();
        msg.setEditable(false);
        msg.setText("Please hold while I download the version log...");
        add(msg);
        
        cbs = new ArrayList<>();
        FileTypeCheckBox temp;
        
        temp = new FileTypeCheckBox(FileType.NODE_COORD, new NodeCoordFile());
        cbs.add(temp);
        add(temp);
        
        temp = new FileTypeCheckBox(FileType.NODE_CONN, new NodeConnFile());
        cbs.add(temp);
        add(temp);
        
        temp = new FileTypeCheckBox(FileType.LABEL, new NodeLabelFile());
        cbs.add(temp);
        add(temp);
        
        temp = new FileTypeCheckBox(FileType.MAP_IMAGE, new MapFile());
        cbs.add(temp);
        add(temp);
        
        importButton = new JButton("Import");
        importButton.addActionListener((e)->{            
            msg.setText("Beginning download...");
            WayfindingManifest man = new WayfindingManifest();
            GoogleDriveUploader.download(nameToUrl.get((String)exportSelector.getSelectedItem()))
            .addOnSucceed((s)->{
                // problem: doesn't have URLs yet
                importManifest(man, NodeManager.getInstance().getGraph());
                msg.setText("Done!");
            }).addOnFail((err)->{
                msg.setText(err.getMessage());
            });
        });
        add(importButton);
        
        v = new VersionLog();
        GoogleDriveUploader.download(VersionLog.DEFAULT_VERSION_LOG_ID).addOnSucceed((stream)->{
            try {
                v.readGraphDataFromFile(null, stream);
                importVersionLog(v);
                msg.setText("Ready to import!");
            } catch (IOException ex) {
                ex.printStackTrace();
                msg.setText("an error occurred while downloading the version log");
            }
        });
        
        updateExportSelector();
    }
    
    private void importVersionLog(VersionLog v){
        //clear old data
        nameToUrl.clear();
        wayfindingTypeSelector.removeAllItems();
        exportSelector.removeAllItems();
        
        //add new data
        String[] types = v.getTypes();
        for(String type : types){
            wayfindingTypeSelector.addItem(type);
        }
        wayfindingTypeSelector.setSelectedIndex(0);
        
        updateExportSelector();
    }
    
    private void updateExportSelector(){
        exportSelector.removeAllItems();
        nameToUrl.clear();
        
        String selectedType = (String)wayfindingTypeSelector.getSelectedItem();
        String[] exportUrls = v.getExportsFor(selectedType);
        final LinkedList<String> exportNames = new LinkedList<>();
        
        for(String url : exportUrls){
            try {
                GoogleDriveUploader.getFileName(url).addOnSucceed((fileName)->{
                    nameToUrl.put(fileName, url);
                    exportNames.addFirst(fileName); //orders from newest to oldest
                }).getExcecutingThread().join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        
        exportNames.forEach((name)->{
            exportSelector.addItem(name);
        });
        
        revalidate();
        repaint();
    }
    
    private void importManifest(WayfindingManifest man, Graph g) {
        cbs.stream().filter((cb)->cb.isSelected()).filter((cb)->man.containsUrlFor(cb.getFileType())).forEach((cb)->{
            try {
                man.importFileFor(cb.getFileType(), g).getExcecutingThread().join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        });
        this.getApplicationBody().switchToPage(ApplicationBody.EDIT);
    }
}
