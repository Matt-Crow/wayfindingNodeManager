package files;

import com.google.api.services.drive.model.File;
import java.io.IOException;
import java.io.InputStream;
import nodemanager.io.DriveIOOp;
import nodemanager.io.GoogleDriveUploader;
import nodemanager.io.VersionLog;

/**
 * Provides a base for the classes used to interface
 * with the files used by the program. 
 * 
 * It provides the generic template for the files used
 * by the Wayfinding program. Note that these are not
 * specifically files on the local file system or Google Drive.
 * 
 * @author Matt Crow (greengrappler12@gmail.com)
 */
public abstract class AbstractWayfindingFile {
    private String name;
    private final FileType type; 
    
    public static String NL = System.getProperty("line.separator");
    
    /**
     * Creates an AbstracteWayfindingFile. Note that this does not actually do anything with files yet.
     * @param title what to call this file when it is saved or uploaded
     * @param t what type of file this will connect to. Used to get file extention and MIME type.
     */
    public AbstractWayfindingFile(String title, FileType t){
        name = title;
        type = t;
    }
    
    public static AbstractWayfindingFile fromType(FileType t){
        AbstractWayfindingFile ret = null;
        switch(t){
            case NODE_COORD:
                ret = new NodeCoordFile();
                break;
            case NODE_CONN:
                ret = new NodeConnFile();
                break;
            case LABEL:
                ret = new NodeLabelFile();
                break;
            case MAP_IMAGE:
                ret = new MapFile();
                break;
            case MANIFEST:
                ret = new WayfindingManifest();
                break;
            case VERSION_LOG:
                ret = new VersionLog();
                break;
            default:
                throw new UnsupportedOperationException("Cannot decode file from type " + t);
        }
        return ret;
    }
    
    public final FileType getType(){
        return type;
    }
    
    /**
     * Gets the File to upload.
     * Returns this' local copy, if it exists,
     * otherwise, returns a temporary file.
     * @return 
     */
    public final java.io.File getFileToUpload() throws IOException{
        return createTemp();
    }
    
    /**
     * Saves this' contents to the local file system.
     * If this has a local file associated with it,
     * writes to that file,
     * otherwise, creates a new file on the local system in the given directory and writes to it.
     * The contents of the file are given by this.getContentsToWrite()
     * @param directory the directory to save the file to.
     * @param fileName the name of the file to save this as
     * @return the file created or updated
     */
    public java.io.File save(String directory, String fileName){
        java.io.File f = new java.io.File(directory + java.io.File.separator + fileName + "." + type.getFileExtention());
        writeToFile(f);
        return f;
    }
    
    /**
     * Creates a temporary file on the local system
     * @return the newly created file, or null if it failed.
     * @throws IOException 
     */
    public final java.io.File createTemp() throws IOException{
        java.io.File temp = java.io.File.createTempFile("wayfindingNodeManagerTempFile", type.getFileExtention());
        temp.deleteOnExit();
        temp = save(temp.getParent(), "wayfindingNodeManagerTempFile");
        return temp;
    }
    
    /**
     * Uploads this' local copy to the drive. 
     * If a local copy isn't set, 
     * creates a temporary file to hold the data from the program,
     * then uploads that temporary file.
     * 
     * @param folderId the id of the folder on the google drive to upload to.
     * @return a DriveIOOp. See its file to see what it does
     */
    public DriveIOOp<File> upload(String folderId){
        return GoogleDriveUploader.uploadFile(this, folderId);
    }
    
    /**
     * Loads this' data into the program
     * @return a DriveIOOp containing this' data. 
     * @throws java.lang.Exception if neither 
     * this' local nor drive copies have been set
     */
    /*
    public final DriveIOOp<InputStream> importData() throws Exception{
        DriveIOOp<InputStream> ret = null;
        if(localCopy != null){
            try {
                readStream(new FileInputStream(localCopy));
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        } else if(driveCopy != null){
            ret = GoogleDriveUploader
                    .download(driveCopy.getId())
                    .addOnSucceed((stream)->readStream(stream));
        } else {
            throw new Exception("Cannot import if neither localCopy not driveCopy have been set!");
        }
        return ret;
    }*/
    
    public void importData(){
        //subclasses will override this to import their data into the program
    }
    
    
    /**
     * Reads the contents of an InputStream,
     * then decides what to do with the content
     * @param s 
     */
    public abstract void readStream(InputStream s) throws IOException;
    
    /**
     * Defined in each direct subclass (AbstractCsvFile, MapFile).
     * Called by save() after a new file has been created. 
     * See the aforementioned classes for more details. 
     * @param f the file to write to.
     */
    public abstract void writeToFile(java.io.File f);
}
