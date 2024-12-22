/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2024.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.io.BufferedReader;
import java.io.InputStreamReader;
  
// Directory class declaration
class GvbDirectory {
  
    // public fields
    public String str;
    // private fields
    private String nameDir;
    private String pathDirA;
    private String pathDir;
    private long lastMod;
    private int fileID;
    private long size;
    private boolean excluded;

    // Constructor
    public GvbDirectory(String nameDir, String pathDirA, String pathDir, long lastMod, int fileID, long size, boolean excluded)
    {
        this.nameDir = nameDir;
        this.pathDirA = pathDirA;
        this.pathDir = pathDir;
        this.lastMod = lastMod;
        this.fileID = fileID;
        this.size = size;
        this.str = "assigned";
        this.excluded = excluded;
    }
  
    public String getNameDir() { return nameDir; }

    public String getPathDirA() { return pathDirA; }

    public String getPathDir() { return pathDir; }
    
    public long getLastMod() { return lastMod; }
    
    public int getFileID() { return fileID; }

    public long getSize() { return size; }

    public boolean getExcluded() {return excluded; }

    public void setNameDir(String nameDir) { this.nameDir = nameDir; }
    
    public void setPathdDirA(String pathDirA) { this.pathDirA = pathDirA; }

    public void setPathDir(String pathDir) { this.pathDir = pathDir; }

    public void setLastMod(long lastMod) { this.lastMod = lastMod; }
  
    public void setFileID(int fileID) { this.fileID = fileID; }
  
    public void setSize(int size) { this.size = size; }
    
    public void setExcluded(boolean excluded) { this.excluded = excluded; }

    // Override toString method to get required
    // output at terminal
    @Override public String toString()
    {
        return "Directory [name=" + nameDir + pathDir + " Hash=" + fileID + "]";
    }
}

// Backup statistics class
class GvbBackupInfo {
  public long nFiles;
  public long nDirectories;
  public long nBackedupFiles;
  public String rootDirectory;
  public String backupDirectory;
  public String options;
  public String[] excList;
  public int nExcList;
  public String[] incList;
  public int nIncList;
  public String[] excSuffix;
  public int nExcSuffix;
  public String separator;
  public char osType;

    // Constructor
    public GvbBackupInfo(long nFiles, long nDirectories, long nBackedupFiles, String rootDirectory, String backupDirectory, String options, String[] excList, int nExcList,
                         String[] incList, int nIncList, String[] excSuffix, int nExcSuffix, String separator, char osType)
    {
        this.nFiles = nFiles;
        this.nDirectories = nDirectories;
        this.nBackedupFiles = nBackedupFiles;
        this.rootDirectory = rootDirectory;
        this.backupDirectory = backupDirectory;
        this.options = options;
        this.excList = excList;
        this.nExcList = nExcList;
        this.incList = incList;
        this.nIncList = nIncList;
        this.excSuffix = excSuffix;
        this.nExcSuffix = nExcSuffix;
        this.separator = separator;
        this.osType = osType;
    }
  
    public long getnFiles() { return nFiles; }
    public long getnDirectories() { return nDirectories; }
    public long getnBackedupFiles() { return nBackedupFiles; }
    public String getRootDirectory() { return rootDirectory; }
    public String getBackupDirectory() { return backupDirectory; }
    public String getOptions() { return options; }
    public String[] getExcList() { return excList; }
    public int getnExcList() { return nExcList; }
    public String[] getIncList() { return incList; }
    public int getnIncList() { return nIncList; }
    public String[] getExcSuffix() { return excSuffix; }
    public int getnExcSuffix() { return nExcSuffix; }
    public String getSeparator() { return separator; }
    public char getOsType() { return osType; }
  
    public void setnFiles(long nFiles) { this.nFiles = nFiles; }
    public void setnDirectories(long nDirectories) { this.nDirectories = nDirectories; }
    public void setnBackedupFiles(long nBackedupFiles) { this.nBackedupFiles = nBackedupFiles; }
    public void setOptions(String options) {this.options = options; }
    public void setExcList(String[] excList) {this.excList = excList; }
    public void setnExcList(int nExcList) {this.nExcList = nExcList; }  
    public void setIncList(String[] incList) {this.incList = incList; }
    public void setnIncList(int nIncList) {this.nIncList = nIncList; }
    public void setExcSuffix(String[] excSuffix) {this.excSuffix = excSuffix; }
    public void setnExcSuffix(int nExcSuffix) {this.nExcSuffix = nExcSuffix; }  
}

class GvbBackup {
  
    public static void main(String[] args) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, IOException
    {
        int i;

        // Default run values
        int days = 1;
        String root = null;
        String target = null;
        String os = System.getProperty("os.name");
        char osType = 0;

        if (os.substring(0, 3).equals("Win")) {
          root = "C:\\";
          target = "C:\\temp\\backupfolder\\";
          osType = 'W';
        } else {
          root = System.getProperty("user.home") + "/";
          target = root + "temp/backupfolder/";
        }
    
        String options = null;
        String[] excList = new String[100];
        String[] incList = new String[100];
        String[] excSuffix = new String[100];

        int nArgs =args.length;
        GVBA2I b = new GVBA2I();

        for (i = 0; i < nArgs; i++) {
          if (args[i].substring(0,1).equals("-"))
          {
            switch( args[i].substring(1,2)) {
              case "d":
                days = b.doAtois(args[i], 2);
                break;
              case "t":
                target = args[i].substring(2);
                break;
              case "r":
                root = args[i].substring(2);
                break;
              case "o":
                options = args[i].substring(2);
                break;
              case "h":
                System.out.println("Help: command line arguments");
                System.out.println("GvbBackup -dn (files modified in last n days)");
                System.out.println("GvbBackup -t<target backup directory>");
                System.out.println("GvbBackup -t<root directory for backup>");
                return;
              default:
                break;
            }
          }
        }

        // Validation to check target has / as last character goes here
        // Also, obtain the drive letter

        String targetZ = null;
        String separator = File.separator;
        char sepchar = separator.charAt(0);

        if (osType == 'W') {
          targetZ = target.substring(0, target.length() - 1) + "z\\"; // target for 7z file on Windows only
        } else {
          targetZ = root + "temp/";
        }

        int retc = -1;
        long timeYesterday = System.currentTimeMillis() - (days*24*60*60*1000 + 45*60*1000); // 100 days [yesterday] - 45 minutes to allow for run time
        long timeNow = System.currentTimeMillis();
        Date currentDate = new Date(timeNow);
        Time currentTime = new Time(timeNow);
        String dateTime = currentDate.toString().replace("-","") + "_" + currentTime.toString().replace(":","");

        if (days > 0)
        {
          System.out.println("Backing up data from root: " + root + " for " + days + " day(s) to target: " + target + ". Start time: " + dateTime + ", Os: " + os);
        }
        else
        {
          if (days == 0)
          {
            timeYesterday = 0;
            System.out.println("Performing FULL backup of data from root: " + root + " to target: " + target + ". Start time: " + dateTime + ", Os: " + os);
          }
          else
          {
            System.out.println("Number of days: " + days + " must be positive, or 0 (Full Online Save)");
            return;
          }
        }
        if (options != null) {
          System.out.println("options: " + options);
        }
        
        if (osType == 'W') {
          System.out.println("Homepath: " + System.getenv("HOMEPATH"));
        } else {
          System.out.println("Homepath: " + System.getenv("HOME"));
        }

        GvbBackupInfo bStats = new GvbBackupInfo(0, 0, 0, root, target, options, excList, 0,
                                                 incList, 0, excSuffix, 0, separator, osType);

        String rootDirectory = bStats.getRootDirectory();
        String bDirectory = bStats.getBackupDirectory();
        String stem[] = new String[25];
        int j = 0;

        stem[j] = bDirectory.substring(3, bDirectory.lastIndexOf(sepchar));                     //('\\'));
        //System.out.println("Stem[0]: " + stem[j]);
      
        do {
          j++;
          stem[j] = stem[j-1].substring(0,stem[j-1].lastIndexOf(sepchar));
          //System.out.println("Stem[" + j + "]: " + stem[j]);
        } while (stem[j].lastIndexOf(sepchar) != -1);

        String backupDirectory = stem[j-1].substring(stem[j-1].indexOf(sepchar) + 1);
      
        System.out.println("TargetZ path: " + targetZ);
        File targetZFile = new File(targetZ);
        if (targetZFile.mkdirs()) {
            // display that the directory is created
            // as the function returned true
            System.out.println("7Z target directory is created");
        }
        else {
            // display that the directory cannot be created
            // as the function returned false
            System.out.println("7Z target directory already created");
        }

        System.out.println("Backup path: " + target);
        File targetFile = new File(target);
        if (targetFile.mkdirs()) {
            // display that the directory is created
            // as the function returned true
            System.out.println("Backup directory is created");
        }
        else {
            // display that the directory cannot be created
            // as the function returned false
            System.out.println("Backup directory already created");
        }
        
        // Log writer file
        try {
          FileWriter logWriter = new FileWriter(targetZ + dateTime + ".txt");
          System.out.println("Log file: " + targetZ + dateTime + ".txt");

          // Obtain configuration information
          readExclusionList(bStats, logWriter);

          // Recurse through top directory and all subdirectories
          retc = nextLevel(rootDirectory, timeYesterday, logWriter, bStats, backupDirectory, false, false);

          long nFiles = bStats.getnFiles();
          long nDirectories = bStats.getnDirectories();
          long nBackedupFiles = bStats.getnBackedupFiles();
          System.out.println("=================================================");
          System.out.println("Relevant files scanned: " + nFiles + "\nRelevant directories searched: " + nDirectories + "\nFiles to be backed up: " + nBackedupFiles);
          System.out.println("=================================================");
  
          // The 7Z bit
          if (osType == 'W') {
            String targetZipPath = targetZFile + "\\" + dateTime + ".7z";
            System.out.println("Output zip file: " + targetZipPath);
            String CMD = "7z a -t7z "  + targetZipPath + " " + target; //C:\\Temp\\backupfolder";
            runcmd(CMD);
 
            // Set up bat file to copy to Box folder
            FileWriter batWriter = new FileWriter(targetZ + "copy2box.bat");
            batWriter.write("copy " + targetZ + dateTime + ".7z" + " C:" + System.getenv("HOMEPATH") + "\\Box\\backupfolderz\\" + dateTime + ".7z\n");
            batWriter.write("copy " + targetZ + dateTime + ".txt" + " C:" + System.getenv("HOMEPATH") + "\\Box\\backupfolderz\\" + dateTime + ".txt\n");
            batWriter.close();
          }

          logWriter.close();
        }
        catch (IOException e) {
          System.out.println("An error occurred writing to log file");
          e.printStackTrace();
        }

        long timeThen = System.currentTimeMillis();
        System.out.println("=================================================");
        System.out.println("Backup completed return code: " + retc);
        System.out.println("Run time: " + ((timeThen - timeNow)/1000) + " seconds");
        System.out.println("=================================================");
      }

    /* ----------------------------------------------------------------------------------------------------------------------------------------------------- */

    private static int nextLevel(String myPathDir, long timeYesterday, FileWriter logWriter, GvbBackupInfo bStats, String backupDirectory, boolean dirsOnly, boolean filesOnly) 
    throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
      
      //System.out.println("Next level: " + myPathDir + " dirsOnly: " + dirsOnly + " filesOnly: " + filesOnly);
      File folder = new File(myPathDir);
      File[] listOfFiles = folder.listFiles();
      GvbDirectory[] d = new GvbDirectory[50000];
      int rc=0, i, j = 0, k;

      long nFiles = bStats.getnFiles();
      long nDirectories = bStats.getnDirectories();
      String[] excList = new String[100];
      String[] incList = new String[100];
      String[] excSuffix = new String[100];

      excList = bStats.getExcList();
      incList = bStats.getIncList();
      excSuffix = bStats.getExcSuffix();

      String separator = bStats.getSeparator();
      char sepchar = separator.charAt(0);
      char osType = bStats.getOsType();

      if (listOfFiles == null) {
        //System.out.println("list of files is null");
        return 4;  
      }

      //System.out.println("Length of listOfFiles:" + listOfFiles.length);
      for (i = 0; i < listOfFiles.length; i++) {
          if (listOfFiles[i].isFile()) {
            nFiles++;
            if (dirsOnly || myPathDir.equals(bStats.getRootDirectory()))
            {
              //System.out.println("Not backing up any files at this level because it's dirsOnly: " + myPathDir);
            }
            else // i.e. don't back up any files if we're matching for a partial included path
            {
              //System.out.println("FIL -- Last Update:" + listOfFiles[i].lastModified() + " Name: " + listOfFiles[i].getName() + " HashTag:" + listOfFiles[i].hashCode());
              if ( listOfFiles[i].lastModified() > timeYesterday) {
                String tail = listOfFiles[i].getName().substring(listOfFiles[i].getName().lastIndexOf('.') + 1);

                // Excluded Suffixes
                boolean excSuff = false;
                k = 0;
                do {
                  if (tail.equals(excSuffix[k])) {
                    excSuff = true;
                  }
                  k++;
                 } while (k < bStats.getnExcSuffix() && excSuff == false);
              
                 if (!excSuff) {
                   System.out.println("MODIFIED: " + listOfFiles[i].getPath()); // write to console
                   logWriter.write("MODIFIED: " + listOfFiles[i].getPath() + "\n"); // write to log file
                   rc = backupFile(listOfFiles[i].getPath(), bStats, logWriter);
                   if ( rc != 0 ) {
                     System.out.println("  WARNING: " + listOfFiles[i].getPath() + " NOT backed up");
                     logWriter.write("  WARNING: " + listOfFiles[i].getPath() + " NOT backed up\n");
                   }
                 }
              }
            }
          }
          else
          {
            if (listOfFiles[i].isDirectory())
            {
              if (dirsOnly)
              {
                // Included directory... further down the path;
                //partialIncDir = false;
                k = 0;
                boolean included = false;
                do {
                  if (incList[k].equals(listOfFiles[i].getAbsolutePath())) {
                    //System.out.println("incList[k]: " + incList[k] + " equals: " + listOfFiles[i].getAbsolutePath());
                    included = true;
                  }
                  k++;
                } while (k < bStats.getnIncList() && included == false);
                if (included)
                {
                  nDirectories++;
                  d[j++] = new GvbDirectory(listOfFiles[i].getName(), listOfFiles[i].getAbsolutePath(), listOfFiles[i].getPath(),
                                            listOfFiles[i].lastModified(), listOfFiles[i].hashCode(),listOfFiles[i].length(), false);
  
                }
              }
              else
              {
                nDirectories++;
                d[j++] = new GvbDirectory(listOfFiles[i].getName(), listOfFiles[i].getAbsolutePath(), listOfFiles[i].getPath(),
                                          listOfFiles[i].lastModified(), listOfFiles[i].hashCode(),listOfFiles[i].length(), false);
              }
            }
            else // Don't know what it might be -- but this does happen sometimes..
            {
              System.out.println("Unknow object found -- last update:" + listOfFiles[i].lastModified() + " Name: " + listOfFiles[i].getName() + " HashCode:" + listOfFiles[i].hashCode());
            }
          }
      }

       bStats.setnFiles(nFiles);
       bStats.setnDirectories(nDirectories);

       if (filesOnly)
       {
        return 0;
       }
       
       // Create Field object
       Field privateFieldName = GvbDirectory.class.getDeclaredField("nameDir");
       Field privateFieldPathA = GvbDirectory.class.getDeclaredField("pathDirA");
       Field privateFieldPath = GvbDirectory.class.getDeclaredField("pathDir");
       Field privateFieldLastMod = GvbDirectory.class.getDeclaredField("lastMod");
       Field privateFieldFileID = GvbDirectory.class.getDeclaredField("fileID");
               
       // Set the accessibility as true
       privateFieldName.setAccessible(true);
       privateFieldPathA.setAccessible(true);
       privateFieldPath.setAccessible(true);
       privateFieldLastMod.setAccessible(true);
       privateFieldFileID.setAccessible(true);

       for (i = 0; i < d.length; i++) {
          if (d[i] != null) {

                String nameDir = (String)privateFieldName.get(d[i]);
                String pathDirA = (String)privateFieldPathA.get(d[i]);
                String pathDir = (String)privateFieldPath.get(d[i]);
                long lastMod = (long)privateFieldLastMod.get(d[i]);
                int fileID = (int)privateFieldFileID.get(d[i]);

                if (!nameDir.equals(backupDirectory)            // don't backup the backup folder
                && !nameDir.equals(backupDirectory + "z")) {    // don't backup the backup folder

                  // Included diretories;
                  boolean incDir = false;
                  k = 0;
                  do {
                    if (pathDirA.equals(incList[k])) {
                      incDir = true;
                    }
                    k++;
                  } while (k < bStats.getnIncList() && incDir == false);

                  if (incDir) {
                    //System.out.println("Fully matched: " + pathDirA + " dirsOnly: " + dirsOnly + " filesOnly: " + filesOnly);
                    rc = nextLevel(pathDir, timeYesterday, logWriter, bStats, backupDirectory, false, false);
                  }
                  else 
                  {
                    // Excluded diretories;
                    boolean excDir = false;
                    k = 0;
                    do {
                      if (pathDirA.equals(excList[k])) {
                        //System.out.println("excDir:=" + true + " For: " + excList[k]);
                        excDir = true;
                      }
                      k++;
                    } while (k < bStats.getnExcList() && excDir == false);
                    d[i].setExcluded(excDir); // remember if this directory is "excluded"
                    if (!excDir) {
                      //System.out.println("Non excluded directory: " + pathDirA + " dirsOnly: " + dirsOnly + " filesOnly: " + filesOnly);
                      rc = nextLevel(pathDir, timeYesterday, logWriter, bStats, backupDirectory, false, false);
                    }
                    else 
                    {
                      // Included directory... further down the path;
                      //partialIncDir = false;
                      k = 0;
                      //dirsOnly=false;
                      do {
                        if (incList[k].contains(pathDirA)) {
                          //System.out.println("dirsOnly:=" + true + " For: " + incList[k]);
                          dirsOnly = true;
                        }
                        k++;
                      } while (k < bStats.getnIncList() && dirsOnly == false);
                      if (dirsOnly)
                      {
                        //System.out.println("Partial match: " + pathDirA + " dirsOnly: " + dirsOnly + " filesOnly: " + filesOnly );
                        rc = nextLevel(pathDir, timeYesterday, logWriter, bStats, backupDirectory, true, false);
                      }
                    }
                  }
                }
          }
       }
       return rc;
    }
    
    /* ----------------------------------------------------------------------------------------------------------------------------------------------------- */

    private static int backupFile(String path, GvbBackupInfo bStats, FileWriter logWriter) {

      String stem[] = new String[25];
      int i, j=0, k;
      int rc = 0;
      long nBackedupFiles = bStats.getnBackedupFiles();
      String separator = bStats.getSeparator();
      char sepchar = separator.charAt(0);
      char osType = bStats.getOsType();

      String backupDirectory = bStats.getBackupDirectory();
      String copiedFile = backupDirectory + path.substring(3); // temporary kludge

      stem[j] = copiedFile.substring(0,copiedFile.lastIndexOf(sepchar));
      //System.out.println("Stem[0]: " + stem[j]);

      do {
        j++;
        stem[j] = stem[j-1].substring(0,stem[j-1].lastIndexOf(sepchar));
        //System.out.println("Stem[" + j + "]: " + stem[j]);
      } while (stem[j] != null && !stem[j].equals(backupDirectory.substring(0,backupDirectory.length() -1)));


      // copy file into hierarchical backup directory for zipping..
      try {
        File f0 = new File(path);
        if (f0.canRead()) {
          //System.out.println("Can read file: " + path);
          // Create hierarchical backup directories
          for (k = j; k >= 0; k--) {
            File f1 = new File(stem[k]);
            boolean bool = f1.mkdir();
            //System.out.println("Stem[" + k + "]: " + stem[k]);
            if (bool) {
                logWriter.write("Created backup sub directory: " + stem[k] + "\n");
            } else {
            //    System.out.println("not created: " + stem[k]);
            }
          }

          FileReader fileReader = new FileReader(path);
          FileWriter fileWriter = new FileWriter(copiedFile);

          while((i = fileReader.read()) != -1) {
            fileWriter.write((char)i);
          }

          fileReader.close();
          fileWriter.close();
          nBackedupFiles++;
          //System.out.println("closed copied file: " + copiedFile);
        }
        else
        {
          logWriter.write("Cannot access file: " + path + "\n");
          rc = 2;
        }

      } catch (IOException e) {
         System.out.println("An error occurred copying file:" + path + " to:" + copiedFile);
         System.out.println("Cause: " + e.getMessage());
         if (e.getMessage().contains("being used by another process")) {
           System.out.println("File: " + copiedFile + " in use by another process");
           rc = 4;
         }
         else {
           e.printStackTrace();
           rc = 8;
         }
      }
      bStats.setnBackedupFiles(nBackedupFiles);
      return rc;
    }

        /* ----------------------------------------------------------------------------------------------------------------------------------------------------- */

        private static int readExclusionList(GvbBackupInfo bStats, FileWriter logWriter) {

          int rc = 0, i=0, j=0, k=0, n;
          BufferedReader reader;
          String[] excLine = new String[100]; // excluded directory (path)
          String[] incLine = new String[100]; // included directory (path) -- takes precedence
          String[] excSuffix = new String[100]; // excluded file suffix
          String separator = bStats.getSeparator();
          char sepchar = separator.charAt(0);
          char osType = bStats.getOsType();
          String line = null;
          int lineState = -1;
    
          try {

            if (osType == 'W') {
              reader = new BufferedReader(new FileReader("C:\\Temp\\GvbBackup.cfg"));
            } else {
              reader = new BufferedReader(new FileReader(System.getProperty("user.home") + "/GvbBackup.config"));
            }
      			
            System.out.println("\n");

            line = reader.readLine();
            //System.out.println(line);

            if (line.length() >= 21 && line.substring(0,21).equals("#EXCLUDED_DIRECTORIES"))
            {
              lineState = 0;
            } 
            else
            {
               if (line.length() >= 21 && line.substring(0,21).equals("#INCLUDED_DIRECTORIES"))
               {
                    lineState = 1;
               }
               else
               {
                  if (line.length() >= 18 && line.substring(0,18).equals("#EXCLUDED_SUFFIXES"))
                  {
                        lineState = 2;
                  }
               }
            }

            while (line != null) {
              line = reader.readLine();
              if (line != null)
              {
                //System.out.println(line);
                if (line.length() >= 21 && line.substring(0,21).equals("#EXCLUDED_DIRECTORIES"))
                {
                  lineState = 0;
                } 
                else
                {
                   if (line.length() >= 21 && line.substring(0,21).equals("#INCLUDED_DIRECTORIES"))
                   {
                        lineState = 1;
                   }
                   else
                   {
                      if (line.length() >= 18 && line.substring(0,18).equals("#EXCLUDED_SUFFIXES"))
                      {
                            lineState = 2;
                      }
                      else
                      {
                        if (line.length() > 0) {
                          switch (lineState) {
                            case 0:
                                excLine[i] = line;
                                i++;
                                break;
                            case 1:
                                incLine[j] = line;
                                j++;
                                break;
                            case 2:
                                excSuffix[k] = line;
                                k++;
                                break;
                            default:
                                break;
                          }
                        }
                      }
                   }
                }
              }
            }
     
            reader.close();
            //System.out.println("i/j/k: " + i + "/" + j + "/" + k);
            bStats.setExcList(excLine);
            bStats.setnExcList(i);
            bStats.setIncList(incLine);
            bStats.setnIncList(j);
            bStats.setExcSuffix(excSuffix);
            bStats.setnExcSuffix(k);
 
            System.out.println("Number excluded directories: " + i);
            
            for (n=0; n < i; n++) {
              System.out.println(excLine[n]);
            }
            System.out.println("Number included directories: " + j);
            for (n=0; n < j; n++) {
              System.out.println(incLine[n]);
            }
            System.out.println("Number excluded suffixes: " + k);
            for (n=0; n < k; n++) {
              System.out.println(excSuffix[n]);
            }

            System.out.println("\n");

          } catch (IOException e) {
               e.printStackTrace();
               rc = 8;
          }
          return rc;
        }
    
/* ----------------------------------------------------------------------------------------------------------------------------------------------------- */

    private static void runcmd(String CMD) {

        try {
            // Run "netsh" Windows command
            System.out.println("Running command: " + CMD);
            Process process = Runtime.getRuntime().exec(CMD);

            // Get input streams
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            // Read command standard output
            String s;
            System.out.println("Standard output: ");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

            // Read command errors
            System.out.println("Standard error: ");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}