/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *  $Revision$
 */

package org.jajuk.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.Util;
import org.jajuk.util.error.CannotRenameException;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Convenient class to manage files
 * @Author Bertrand Florat 
 * @created 17 oct. 2003
 */
public class FileManager extends ItemManager implements Observer{
    /**Flag the fact a rate has change for a track, used by bestof view refresh for perfs*/
    private boolean bRateHasChanged = true;
    /**Best of files*/
    private ArrayList alBestofFiles = new ArrayList(20);
    /**Novelties files*/
    private ArrayList alNovelties = new ArrayList(20);
    /**Self instance*/
    private static FileManager singleton;
    
    /**
     * No constructor available, only static access
     */
    private FileManager() {
        super();
        //---register properties---
        //ID
        registerProperty(new PropertyMetaInformation(XML_ID,false,true,false,false,false,String.class,null,null));
        //Name
        registerProperty(new PropertyMetaInformation(XML_NAME,false,true,true,true,false,String.class,null,null));
        //Directory
        registerProperty(new PropertyMetaInformation(XML_DIRECTORY,false,true,true,false,true,String.class,null,null));
        //Track
        registerProperty(new PropertyMetaInformation(XML_TRACK,false,true,true,false,false,String.class,null,null));
        //Size
        registerProperty(new PropertyMetaInformation(XML_SIZE,false,true,true,false,false,Long.class,null,null));
        //Quality
        registerProperty(new PropertyMetaInformation(XML_QUALITY,false,true,true,false,false,Long.class,null,0));
    }
    
    /**
     * @return singleton
     */
    public static FileManager getInstance(){
        if (singleton == null){
            singleton = new FileManager();
        }
        return singleton;
    }
    
    /**
     * Register an File with a known id
     * 
     * @param sName
     */
    public File registerFile(String sId, String sName, Directory directory, 
            Track track, long lSize, long lQuality) {
        synchronized(FileManager.getInstance().getLock()){
            if ( !hmItems.containsKey(sId)){
                File file = null;
                file = new File(sId, sName, directory, track, lSize, lQuality);
                hmItems.put(sId,file);
                //add to track
                track.addFile(file);
                //add to directory
                file.getDirectory().addFile(file);
                if ( directory.getDevice().isRefreshing() && Log.isDebugEnabled()){
                    Log.debug("registrated new file: "+ file); //$NON-NLS-1$
                }
            }
            return (File)hmItems.get(sId);
        }
    }
    
    /**
     * Get file hashcode (ID)
     * @param sName
     * @param device
     * @param dir
     * @return file ID
     */
    protected static String getID(String sName,Device device,Directory dir){
        return MD5Processor.hash(
            new StringBuffer(device.getName()).
            append(device.getUrl()).
            append(dir.getRelativePath()).
            append(sName).toString().toString());
    }
    
    /**
     * Change a file name
     * @param fileOld
     * @param sNewName
     * @return new file
     */
    public File changeFileName(org.jajuk.base.File fileOld,String sNewName) throws JajukException{
        synchronized(FileManager.getInstance().getLock()){
            //check given name is different
            
            if (fileOld.getName().equals(sNewName)){
                return fileOld;
            }
            //check if this file still exists
            if (!fileOld.getIO().exists()){
                throw new CannotRenameException("135"); //$NON-NLS-1$
            }
            java.io.File fileNew = new java.io.File(fileOld.getIO().getParentFile().getAbsolutePath()
                +java.io.File.separator+sNewName);
            //recalculate file ID
            Directory dir = fileOld.getDirectory(); 
            String sNewId = MD5Processor.hash(new StringBuffer(dir.getDevice().getName())
                .append(dir.getDevice().getUrl()).append(dir.getRelativePath())
                .append(sNewName).toString());
            //create a new file (with own fio and sAbs)
            org.jajuk.base.File fNew = new File(sNewId,sNewName,fileOld.getDirectory(),fileOld.getTrack(),fileOld.getSize(),fileOld.getQuality());
            fNew.setProperties(fileOld.getProperties()); //transfert all properties (inc id and name)
            fNew.setProperty(XML_ID,sNewId); //reset new id and name
            fNew.setProperty(XML_NAME,sNewName); //reset new id and name
            //check file name and extension
            if (!(Util.getExtension(fileNew).equals(Util.getExtension(fileOld.getIO())))){ //no extension change
                throw new CannotRenameException("134"); //$NON-NLS-1$
            }
            //check if future file exists (under windows, file.exists return true even with different case so we test file name is different)
            if ( !fileNew.getName().equalsIgnoreCase(fileOld.getName()) && fileNew.exists()){
                throw new CannotRenameException("134"); //$NON-NLS-1$
            }
            //try to rename file on disk
            try{
                fileOld.getIO().renameTo(fileNew);
            }
            catch(Exception e){
                throw new CannotRenameException("134"); //$NON-NLS-1$
            }
            //OK, remove old file and register this new file
            removeFile(fileOld);
            if ( !hmItems.containsKey(sNewId)){
                hmItems.put(sNewId,fNew);
            }
            //notify everybody for the file change
            Properties properties = new Properties();
            properties.put(DETAIL_OLD,fileOld);
            properties.put(DETAIL_NEW,fNew);
            //change directory reference
            dir.changeFile(fileOld,fNew);
            //Notify interested items (like history manager)
            ObservationManager.notifySync(new Event(EVENT_FILE_NAME_CHANGED,properties));
            return fNew;
        }
    }
    
    /**
     * Change a file directory
     * @param old old file
     * @param newDir new dir
     * @return new file or null if an error occurs
     */
    public File changeFileDirectory(File old,Directory newDir) throws JajukException{
        synchronized(FileManager.getInstance().getLock()){
            //recalculate file ID
            
            String sNewId = MD5Processor.hash(new StringBuffer(newDir.getDevice().getName())
                .append(newDir.getDevice().getUrl()).append(newDir.getRelativePath())
                .append(old.getName()).toString());
            //create a new file (with own fio and sAbs)
            File fNew = new File(sNewId,old.getName(),newDir,old.getTrack(),old.getSize(),
                old.getQuality());
            fNew.setProperties(old.getProperties()); //transfert all properties (inc id)
            fNew.setProperty(XML_ID,sNewId); //reset new id and name
            //OK, remove old file and register this new file
            removeFile(old);
            if ( !hmItems.containsKey(sNewId)){
                hmItems.put(sNewId,fNew);
            }
            return fNew;
        }
    }
    
    
    
    /**
     * Clean all references for the given device
     * @param sId : Device id
     */
    public void cleanDevice(String sId) {
        synchronized(FileManager.getInstance().getLock()){
            Iterator it = hmItems.values().iterator();
            
            while (it.hasNext()) {
                File file = (File) it.next();
                if (file.getDirectory()==null || file.getDirectory().getDevice().getId().equals(sId)) {
                    it.remove();  //this is the right way to remove entry in the hashmap
                }
            }
            //cleanup sorted array
            it = hmItems.values().iterator();
            while (it.hasNext()){
                File file = (File) it.next();
                if (file.getDirectory() == null 
                        || file.getDirectory().getDevice().getId().equals(sId)) {
                    it.remove();  //this is the right way to remove entry 
                }
            }
        }
    }
    
    /**
     * Remove a file reference
     * @param file
     */
    public void removeFile(File file){
        synchronized(FileManager.getInstance().getLock()){
            hmItems.remove(file.getId());
            file.getDirectory().removeFile(file);
        }
    }
        
    /**
     * Return file by full path
     * @param sPath : full path
     * @return file or null if given path is not known
     */
    
    public File getFileByPath(String sPath) {
        synchronized(FileManager.getInstance().getLock()){
            File fOut = null;
            
            java.io.File fToCompare = new java.io.File(sPath);
            Iterator it = hmItems.values().iterator();
            while ( it.hasNext()){
                File file = (File)it.next();
                if (file.getIO().equals(fToCompare)){ //we compare io files and not paths
                    //to avoid dealing with path name issues
                    fOut = file;
                    break;
                }
            }
            return fOut;
        }
    }
    
    /**
     * @return All accessible files of the collection
     */
    public ArrayList getReadyFiles(){
        synchronized(FileManager.getInstance().getLock()){
            // create a tempory table to remove unmounted files
            
            ArrayList alEligibleFiles = new ArrayList(1000);
            Iterator it = hmItems.values().iterator();
            while ( it.hasNext()){
                File file = (File)it.next();
                if (file.isReady()){
                    alEligibleFiles.add(file);
                }
            }
            return alEligibleFiles;
        }
    }
    
    /**
     * Return a shuffle mounted file from the entire collection
     * @return
     */
    public File getShuffleFile(){
        synchronized(FileManager.getInstance().getLock()){
            ArrayList alEligibleFiles = getReadyFiles();
            
            if (alEligibleFiles.size() ==0 ){
                return null;
            }
            return (File)alEligibleFiles.get((int)(Math.random()*alEligibleFiles.size()));
        }
    }
    
    /**
     * Return a playlist with the entire accessible shuffle collection 
     * @return The entire accessible shuffle collection
     */
    public ArrayList getGlobalShufflePlaylist(){
        synchronized(FileManager.getInstance().getLock()){
            ArrayList alEligibleFiles = getReadyFiles();
            
            Collections.shuffle(alEligibleFiles);
            return alEligibleFiles;
        }
    }
    
    /**
     * Return a shuffle mounted file from the noveties
     * @return
     */
    public synchronized File getNoveltyFile(){
        synchronized(FileManager.getInstance().getLock()){
            ArrayList alEligibleFiles = getGlobalNoveltiesPlaylist();
            return (File)alEligibleFiles.get((int)(Math.random()*alEligibleFiles.size()));
        }
    }
    
    /**
     * Return a playlist with the entire accessible shuffled novelties collection 
     * @return The entire accessible novelties collection or null if none track in given time interval
     */
    public ArrayList getGlobalNoveltiesPlaylist(){
        synchronized(FileManager.getInstance().getLock()){
            return getGlobalNoveltiesPlaylist(true);
        }
    }
    
    /**
     * Return a playlist with the entire accessible novelties collection 
     * @param bHideUnmounted 
     * @return The entire accessible novelties collection or null if none track in given time interval
     */
    public ArrayList getGlobalNoveltiesPlaylist(boolean bHideUnmounted){
        synchronized(TrackManager.getInstance().getLock()){
            ArrayList alEligibleFiles = new ArrayList(1000);
            Iterator it = TrackManager.getInstance().getItems().iterator(); //search in tracks, not files to avoid duplicates items
            Date now = new Date();
            while ( it.hasNext()){
                Track track = (Track)it.next();
                File file = track.getPlayeableFile(bHideUnmounted); //try to get a mounted file (can return null)
                if (file == null){//none mounted file, take first file we find
                    continue;
                }
                int iTrackAge = (int)((now.getTime()-track.getAdditionDate().getTime())/86400000); //)/1000/60/60/24;
                if ( iTrackAge <= ConfigurationManager.getInt(CONF_OPTIONS_NOVELTIES_AGE)){
                    alEligibleFiles.add(file);
                }
            }
            if (alEligibleFiles.size() ==0 ){
                return null;
            }
            //sort alphabetinaly and by date, newest first
            Collections.sort(alEligibleFiles,new Comparator() {
                public int compare(Object o1, Object o2) {
                    File file1 = (File)o1;
                    File file2 = (File)o2;
                    String sCompared1 = file1.getTrack().getAdditionDate().getTime()+file1.getAbsolutePath(); 
                    String sCompared2 = file2.getTrack().getAdditionDate().getTime()+file2.getAbsolutePath(); 
                    return sCompared2.compareTo(sCompared1);            
                }
            }); 
            return alEligibleFiles;
        }
    }
    
    /**
     * Return a shuffle mounted file from the entire collection with rate weight ( best of mode )
     * @return
     */
    public File getBestOfFile(){
        synchronized(FileManager.getInstance().getLock()){
            TreeSet ts = getSortedByRate();
            
            FileScore fscore = (FileScore)ts.last();
            return fscore.getFile(); //return highest score file
        }
    }
    
    /**
     * 
     * @return a sorted set of the collection by rate, lowest first
     */
    private TreeSet getSortedByRate(){
        synchronized(TrackManager.getInstance().getLock()){
            //create a tempory table to remove unmounted files
            TreeSet tsEligibleFiles = new TreeSet();
            Iterator it = TrackManager.getInstance().getItems().iterator(); //search in tracks, not files to avoid duplicates items
            while ( it.hasNext()){
                File file = ((Track)it.next()).getPlayeableFile(); //can return null
                if (file!= null && file.isReady()){ //test if file is null!
                    long lRate = file.getTrack().getRate();
                    long lScore = (long)(Math.random()*(100/(file.getTrack().getSessionHits()+1))*Math.log(lRate));  //computes score for each file ( part of shuffleness, part of hits weight )
                    tsEligibleFiles.add(new FileScore(file,lScore));
                }
            }
            return tsEligibleFiles;
        }
    }
    
    /**
     * Return a playlist with the entire accessible bestof collection, best first
     * @return The entire accessible bestof collection
     */
    public ArrayList getGlobalBestofPlaylist(){
        synchronized(FileManager.getInstance().getLock()){
            TreeSet ts = getSortedByRate();
            
            ArrayList al = new ArrayList(ts.size());
            Iterator it = ts.iterator();
            while (it.hasNext()){
                FileScore fs = (FileScore)it.next();
                al.add(fs.getFile());
            }
            Collections.reverse(al); //reverse to have best first
            return al;
        }
    }
    
    /**
     * Return CONF_BESTOF_SIZE top files
     * @return top files
     */
    public ArrayList getBestOfFiles(){
        synchronized(FileManager.getInstance().getLock()){
            return getBestOfFiles(true);
        }
    }
    
    /**
     * Return CONF_BESTOF_SIZE top files
     * @param bHideUnmounted 
     * @return top files
     */
    public ArrayList getBestOfFiles(boolean bHideUnmounted){
        synchronized(TrackManager.getInstance().getLock()){
            if (FileManager.getInstance().hasRateChanged() || alBestofFiles == null){  //test a rate has changed for perfs
                //clear data
                alBestofFiles.clear();
                int iNbBestofFiles = Integer.parseInt(ConfigurationManager.getProperty(CONF_BESTOF_SIZE));
                //create a tempory table to remove unmounted files
                ArrayList alEligibleFiles = new ArrayList(iNbBestofFiles);
                Iterator it = TrackManager.getInstance().getItems().iterator();
                while ( it.hasNext()){
                    Track track = (Track)it.next();
                    File file = track.getPlayeableFile(bHideUnmounted);
                    if (file != null){
                        long lRate = file.getTrack().getRate();
                        alEligibleFiles.add(new FileScore(file,lRate));
                    }
                }
                Collections.sort(alEligibleFiles);
                Collections.reverse(alEligibleFiles); //reverse score
                int i = 0;
                while (i<alEligibleFiles.size() && i<iNbBestofFiles){
                    File file = ((FileScore)alEligibleFiles.get(i)).getFile();
                    alBestofFiles.add(file);
                    i++;
                }
                setRateHasChanged(false);
            }
            return alBestofFiles;
        }
    }
    
    /** Return next mounted file ( used in continue mode )
     * @param file : a file
     * @return next file from entire collection
     */
    public File getNextFile(File file){
        synchronized(FileManager.getInstance().getLock()){
            if (file  == null){
                return null;
            }
            File fileNext = null;
            //look for a correct file from index to collection end
            boolean bOk = false;
            boolean bStart = false;
            Iterator it = getItems().iterator();
            while (it.hasNext()){
                fileNext = (File)it.next();
                if (!bStart && fileNext.equals(file)){
                    bStart = true; //OK, begin to concidere files from this one
                    continue;
                }
                if (bStart && fileNext.isReady()){  //file must be on a mounted device not refreshing
                    bOk = true;
                    break;
                }
            }
            if (bOk){
                return fileNext;
            }
            else if (!ConfigurationManager.getBoolean(CONF_OPTIONS_RESTART)){
                return null;
            }
            //ok, if we are in restart collection mode, restart from collection begin to file index
            it = getItems().iterator();
            while (it.hasNext()){
                fileNext = (File)it.next();
                if (fileNext.isReady()){  //file must be on a mounted device not refreshing
                    bOk = true;
                    break;
                }
            }
            if ( bOk){
                return fileNext;
            }
            return null;
        }
    }
    
    
    /** Return previous mounted file 
     * @param file : a file
     * @return previous file from entire collection
     */
    public File getPreviousFile(File file){
        synchronized(FileManager.getInstance().getLock()){
            if (file  == null){
                return null;
            }
            File filePrevious = null;
            ArrayList alSortedFiles = new ArrayList(getItems());
            int i = alSortedFiles.indexOf(file);
            //test if this file is the very first one
            if (i == 0){
                Messages.showErrorMessage("128"); //$NON-NLS-1$
                return null;
            }
            //look for a correct file from index to collection begin
            boolean bOk = false;
            for (int index=i-1;index>=0;index--){
                filePrevious = (File)alSortedFiles.get(index);
                if (filePrevious.isReady()){  //file must be on a mounted device not refreshing
                    bOk = true;
                    break;
                }
            }
            if ( bOk){
                return filePrevious;
            }
            return null;
        }
    }
    
    
    /**
     * Return whether the given file is the very first file from collection
     * @param file
     * @return
     */
    public boolean isVeryfirstFile(File file){
        synchronized(FileManager.getInstance().getLock()){
            if (file == null || getItems().size() == 0){
                return false;
            }
            Iterator it = getItems().iterator();
            File first = (File)it.next();
            return (file.equals(first));
        }
    }
    
    /**
     * 
     * @param file
     * @return All files in the same directory than the given one
     */
    public ArrayList getAllDirectory(File file){
        synchronized(getLock()){
            if (file == null){
                return null;
            }
            ArrayList alResu = new ArrayList(10);
            Directory dir = file.getDirectory();
            Iterator it = getItems().iterator();
            while ( it.hasNext()){
                File f = (File)it.next();
                Directory d = f.getDirectory();
                if (d.equals(dir)){
                    alResu.add(f);
                }
            }
            return alResu;
        }
    }
    
    /**
     * 
     * @param file
     * @return All files in the same directory from the given one (includes the one)
     */
    public ArrayList getAllDirectoryFrom(File file){
        synchronized(getLock()){
            if (file == null){
                return null;
            }
            ArrayList alResu = new ArrayList(10);
            Directory dir = file.getDirectory();
            Iterator it = getItems().iterator();
            boolean bSeenTheOne = false;
            while ( it.hasNext()){
                File f = (File)it.next();
                if (f.equals(file)){
                    bSeenTheOne = true;
                    alResu.add(f);
                }
                else{
                    Directory d = f.getDirectory();
                    if (d.equals(dir) && bSeenTheOne){
                        alResu.add(f);
                    }    
                }
            }
            return alResu;
        }
    }
    
    /**
     * Perform a search in all files names with given criteria
     * @param sCriteria
     * @return
     */
    public TreeSet search(String sCriteria){
        synchronized(FileManager.getInstance().getLock()){
            TreeSet tsResu = new TreeSet(); 
            
            sCriteria = sCriteria.toLowerCase();
            Iterator it = hmItems.values().iterator();
            while ( it.hasNext()){
                File file = (File)it.next();
                if ( ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED) && //if  search in only in mounted devices
                        (!file.getDirectory().getDevice().isMounted() || file.getDirectory().getDevice().isRefreshing())){
                    continue;
                }
                String sResu = file.getAny();
                if ( new StringBuffer(sResu.toLowerCase()).lastIndexOf(sCriteria) != -1 ){
                    tsResu.add(new SearchResult(file,file.toStringSearch()));
                }
            }
            return tsResu;
        }
    }
    
    
    /**
     * @return Returns the bRateHasChanged.
     */
    public boolean hasRateChanged() {
        return bRateHasChanged;
    }
    
    /**
     * @param rateHasChanged The bRateHasChanged to set.
     */
    public void setRateHasChanged(boolean rateHasChanged) {
        bRateHasChanged = rateHasChanged;
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.base.ItemManager#getIdentifier()
     */
    public String getIdentifier() {
        return XML_FILES;
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
     */
    public void update(Event event) {
    }
}

/** File score*/
class FileScore implements Comparable{
    
    /** The score */
    long lScore;
    
    /**The file*/
    File file;
    
    /** A File scrore*/
    public FileScore(File file,long lScore){
        this.lScore = lScore;
        this.file = file;
    }
    
    /**
     * @return Returns the file.*/
    public File getFile() {
        return file;
    }
    
    /**
     * @return Returns the lScore.*/
    public long getLScore() {
        return lScore;
    }
    
    
    /** ToString method*/
    public String toString(){
        return new StringBuffer(file.toString()).append(',').append(lScore).toString();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        FileScore fscore = (FileScore)o;
        if ( fscore.getLScore() < lScore){
            return 1;
        }
        else if ( fscore.getLScore() > lScore){
            return -1;
        }
        return 0;
    }
    
}
