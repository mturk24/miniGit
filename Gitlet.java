import java.io.File;
import java.nio.file.Files;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.TreeMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Set;
import java.util.HashSet;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
// import com.google.common.io.ByteStreams;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.Stack;
import java.nio.file.Files;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;


public class Gitlet implements Serializable {
    public static TreeMap<String, File> files;
    public static File file;
    public static Set<String> branches = new HashSet();
    public static Gitlet myGitlet;
    public static int count;
    public static boolean check;
    public static Commit commitObj;
    public static Commit current;
    public static HashMap<Integer, Commit> idStorage; 
    public static HashMap<Commit, Integer> commtoidStorage; 
    public static HashMap<Commit, String> timeStorage;
    public static LinkedList<Commit> commitList;
    public static HashSet<File> unmarkedFiles;
    public static HashMap<String, Integer> findStructure;
    public static Date firstDate;
    public static String thisDate;
    public static HashMap<String, Commit> branchStorage = new HashMap();
    public static String branch;

    public static void main(String[] args) {
        if (args.length == 0){
            System.out.println("You have zero arguments.");
        }
        else if (args[0].equals("init")) {
            count = 0;
            dirCreate();
            current = new Commit("initial commit", files);
            current.previous = null;
        }
        else if (args[0].equals("add")) {
            file = new File(".gitlet");
            if (file.exists() == false){
                System.out.println("Gitlet does not exist!");
            }
            else {
                add(args[1]);
            }
        }
        else if (args[0].equals("commit")) {
            if (args.length == 1){
                System.out.println("Please enter a commit message.");
            }
            else {
                unmarkedFiles = loadUnmarked();
                commtoidStorage = loadCommstoIds();
                count = loadCounts();
                files = loadTreeMap();
                idStorage = loadIDMap();
                timeStorage = loadTimeMap();
                commitList = loadCommits();
                firstDate = loadTime();
                branchStorage = loadBranchMap();
                branch = loadBranchName();
                commitObj = new Commit(args[1], files);
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();
                for (String filename : commitObj.copy1.keySet()) {
                    if (unmarkedFiles.contains(commitObj.copy1.get(filename))) {
                        commitObj.copy1.remove(filename);
                    }
                }
                if ((files.size() == 0) && (unmarkedFiles.size() == 0)) {
                    System.out.println("No changes added to the commit.");
                }
                else {
                    commitList.add(commitObj);  
                    count += 1;
                    commtoidStorage.put(commitObj, count);
                    idStorage.put(count, commitObj);
                    timeStorage.put(commitObj, dateFormat.format(date));
                    files.clear();
                    storeTime(firstDate);
                    storeUnmarked(unmarkedFiles);
                    storeCommstoIds(commtoidStorage);
                    storeCounts(count);
                    storeTreeMap(files);
                    storeIDMap(idStorage);
                    storeTimeMap(timeStorage);
                    storeCommits(commitList);
                    storeBranchMap(branchStorage);
                    storeBranchName(branch);
                }
            }
        }
        else if (args[0].equals("log")) {
            log(); 
        }
        else if (args[0].equals("global-log")) {
            globalLog(); 
        }
        else if (args[0].equals("find")) {
            find(args[1]); 
        }
        else if (args[0].equals("status")) {
            status(); 
        }
        else if (args[0].equals("rm")) {
            remove(args[1]);
        }
        else if (args[0].equals("branch")) {
            branch(args[1]);
        }
        else if (args[0].equals("rm-branch")) {
            removeBranch(args[1]);
        }
        else if ((args[0].equals("checkout")) && (args.length == 2)) {
            checkout(args[1]);
        }
        else if ((args[0].equals("checkout")) && (args.length == 3)) {
            checkout(Integer.parseInt(args[1]), args[2]);
        }
    }
    public Gitlet() {
        //constructor for Gitlet
    }
    // helper method from creating init
    private static void dirCreate() {
        files = new TreeMap();
        idStorage = new HashMap();
        timeStorage = new HashMap();
        commitList = new LinkedList();
        unmarkedFiles = new HashSet();
        findStructure = new HashMap();
        commtoidStorage = new HashMap();
        branch = "master";
        branchStorage.put(branch, current);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        firstDate = new Date();
        thisDate = dateFormat.format(firstDate);

        file = new File(".gitlet");
        if (file.exists() == false) {
            file.mkdir();
        }
        else {
            System.out.println("A gitlet version control system already exists in the current directory.");
        }
        storeTime(firstDate);
        storeUnmarked(unmarkedFiles);
        storeCommstoIds(commtoidStorage);
        storeCounts(count);
        storeTreeMap(files);
        storeIDMap(idStorage);
        storeTimeMap(timeStorage);
        storeCommits(commitList);
        storeBranchMap(branchStorage);
        storeBranchName(branch);
    }

    // add to the staged data structure, a TreeMap, if conditions are met
    public static void add(String fileName) {
        unmarkedFiles = loadUnmarked();
        commtoidStorage = loadCommstoIds();
        count = loadCounts();
        files = loadTreeMap();
        idStorage = loadIDMap();
        timeStorage = loadTimeMap();
        commitList = loadCommits();
        firstDate = loadTime();
        branchStorage = loadBranchMap();
        branch = loadBranchName();
        File newFile = new File(fileName);
        try {
            if (unmarkedFiles.contains(newFile)){
                System.out.println("You can't add this file!");
                return;
            }
            else {
                if (newFile.exists() == true) {
                    FileInputStream f1 = new FileInputStream(newFile);
                    DataInputStream d1 = new DataInputStream(f1);
                    if (files.containsKey(fileName)){
                        FileInputStream f2 = new FileInputStream(files.get(fileName));
                        DataInputStream d2 = new DataInputStream(f2);
                        byte[] b1 = ByteStream.toByteArray(d1);
                        byte[] b2 = ByteStream.toByteArray(d2);
                        for (int i = 0; i < b1.length; i++){
                            if (b1[i] != b2[i]) {
                                check = true;
                                if (check == true){
                                    files.put(fileName, newFile);
                                    check = false;
                                }
                                else {
                                    System.out.println("File has not been modified since last commit.");  
                                }
                            }
                        }
                    }
                    else {
                        files.put(fileName, newFile);
                    }   

                }
                else {
                    System.out.println("File does not exist.");
                }
            }

        }
        catch (Exception e) {
            e.printStackTrace(System.out);
        }
        storeTime(firstDate);
        storeUnmarked(unmarkedFiles);
        storeCommstoIds(commtoidStorage);
        storeCounts(count);
        storeTreeMap(files);
        storeIDMap(idStorage);
        storeTimeMap(timeStorage);
        storeCommits(commitList);
        storeBranchMap(branchStorage);
        storeBranchName(branch);
    }
    // removes a given file
    public static void remove(String fileName) {
        unmarkedFiles = loadUnmarked();
        commtoidStorage = loadCommstoIds();
        count = loadCounts();
        files = loadTreeMap();
        idStorage = loadIDMap();
        timeStorage = loadTimeMap();
        commitList = loadCommits();
        firstDate = loadTime();
        branchStorage = loadBranchMap();
        branch = loadBranchName();
        for (String file : files.keySet()) {
            if (files.get(fileName).exists() == false) {
                System.out.println("No reason to remove the file.");
            }
            if (file.equals(fileName)) {
                unmarkedFiles.add(files.get(file));
                files.remove(file);
            }
        }
        storeTime(firstDate);
        storeUnmarked(unmarkedFiles);
        storeCommstoIds(commtoidStorage);
        storeCounts(count);
        storeTreeMap(files);
        storeIDMap(idStorage);
        storeTimeMap(timeStorage);
        storeCommits(commitList);
        storeBranchMap(branchStorage);
        storeBranchName(branch);
    }
    // provides an ordered log of commits
    public static void log() {
        unmarkedFiles = loadUnmarked();
        commtoidStorage = loadCommstoIds();
        count = loadCounts();
        files = loadTreeMap();
        idStorage = loadIDMap();
        timeStorage = loadTimeMap();
        commitList = loadCommits();
        firstDate = loadTime();
        branchStorage = loadBranchMap();
        branch = loadBranchName();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        int i = commitList.size();
        for (Commit commit : commitList) {
            String newLine = System.lineSeparator(); //This will retrieve line separator dependent on OS.
            System.out.println("====");
            System.out.println("Commit" + " " + (i) + ".");
            System.out.println(commitList.get(i-1).specificDate);
            System.out.println(commitList.get(i-1).message + newLine);
            i -= 1;
        }
        String newLine = System.lineSeparator(); 
        System.out.println("====");
        System.out.println("Commit" + " " + "0.");
        System.out.println(dateFormat.format(firstDate));
        System.out.println("initial commit" + newLine);
        storeTime(firstDate);
        storeUnmarked(unmarkedFiles);
        storeCommstoIds(commtoidStorage);
        storeCounts(count);
        storeTreeMap(files);
        storeIDMap(idStorage);
        storeTimeMap(timeStorage);
        storeCommits(commitList);
        storeBranchMap(branchStorage);
        storeBranchName(branch);
    }
    // prints out every commit ever committed
    public static void globalLog() {
        unmarkedFiles = loadUnmarked();
        commtoidStorage = loadCommstoIds();
        count = loadCounts();
        files = loadTreeMap();
        idStorage = loadIDMap();
        timeStorage = loadTimeMap();
        commitList = loadCommits();
        firstDate = loadTime();
        branchStorage = loadBranchMap();
        branch = loadBranchName();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        int i = commitList.size();
        for (Commit commit : commitList) {
            String newLine = System.lineSeparator(); //This will retrieve line separator dependent on OS.
            System.out.println("====");
            System.out.println("Commit" + " " + (i) + ".");
            System.out.println(commitList.get(i-1).specificDate);
            System.out.println(commitList.get(i-1).message + newLine);
            i -= 1;
        }
        String newLine = System.lineSeparator(); 
        System.out.println("====");
        System.out.println("Commit" + " " + "0.");
        System.out.println(dateFormat.format(firstDate));
        System.out.println("initial commit" + newLine);
        storeTime(firstDate);
        storeUnmarked(unmarkedFiles);
        storeCommstoIds(commtoidStorage);
        storeCounts(count);
        storeTreeMap(files);
        storeIDMap(idStorage);
        storeTimeMap(timeStorage);
        storeCommits(commitList);
        storeBranchMap(branchStorage);
        storeBranchName(branch);
    }
    // find the commit with an id from a given message
    public static void find(String message) {
        unmarkedFiles = loadUnmarked();
        commtoidStorage = loadCommstoIds();
        count = loadCounts();
        files = loadTreeMap();
        idStorage = loadIDMap();
        timeStorage = loadTimeMap();
        commitList = loadCommits();
        firstDate = loadTime();
        branchStorage = loadBranchMap();
        branch = loadBranchName();
        for (int id : idStorage.keySet()) {
            findStructure.put(idStorage.get(id).message, id);
        }
        for (String msg : findStructure.keySet()){
            if (msg == message) {
                System.out.println(findStructure.get(msg));
            }
            else {
                System.out.println("Found no commit with that message.");
            }
        }
        storeTime(firstDate);
        storeUnmarked(unmarkedFiles);
        storeCommstoIds(commtoidStorage);
        storeCounts(count);
        storeTreeMap(files);
        storeIDMap(idStorage);
        storeTimeMap(timeStorage);
        storeCommits(commitList);
        storeBranchMap(branchStorage);
        storeBranchName(branch);
    }

    // prints branches, staged files, and files marked for removal
    public static void status() {
        unmarkedFiles = loadUnmarked();
        commtoidStorage = loadCommstoIds();
        count = loadCounts();
        files = loadTreeMap();
        idStorage = loadIDMap();
        timeStorage = loadTimeMap();
        commitList = loadCommits();
        firstDate = loadTime();
        branchStorage = loadBranchMap();
        branch = loadBranchName();
        String newLine = System.lineSeparator();
        if (branchStorage.size() == 0) {
            System.out.println("=== " + "Branches ===" + newLine);
        }
        else {
            System.out.println("=== " + "Branches ===");
        }
        for (String branchUnit : branchStorage.keySet()) {
            if (branch.equals(branchUnit)) {
                System.out.print("*");
            }
            if ((branchUnit.equals("master")) && branchStorage.size() == 1) {
                System.out.println(branchUnit + newLine);
            }
            else if ((branchUnit.equals("master")) && branchStorage.size() != 1) {
                System.out.println(branchUnit);
            }
            else {  
                System.out.println(branchUnit + newLine);
            }
        }
        if (files.size() == 0) {
            System.out.println("=== " + "Staged Files " + "===" + newLine);
        }
        else {
            System.out.println("=== " + "Staged Files " + "===");
        }
        for (String filename : files.keySet()) {
            System.out.println(files.get(filename));
        }
        if (files.size() != 0) {
            System.out.println();
        }
        if (unmarkedFiles == null) {
            System.out.println("=== " + "Files Marked for Removal " + "===" + newLine);
        }
        else {
            System.out.println("=== " + "Files Marked for Removal " + "===");
        }
        for (File file : unmarkedFiles) {
            System.out.println(file);
        }
        System.out.println();
        storeTime(firstDate);
        storeUnmarked(unmarkedFiles);
        storeCommstoIds(commtoidStorage);
        storeCounts(count);
        storeTreeMap(files);
        storeIDMap(idStorage);
        storeTimeMap(timeStorage);
        storeCommits(commitList);
        storeBranchMap(branchStorage);
        storeBranchName(branch);
    }

    // changes file to version in commit based on id used
    public static void checkout(int id, String file) {
        unmarkedFiles = loadUnmarked();
        commtoidStorage = loadCommstoIds();
        count = loadCounts();
        files = loadTreeMap();
        idStorage = loadIDMap();
        timeStorage = loadTimeMap();
        commitList = loadCommits();
        firstDate = loadTime();
        branchStorage = loadBranchMap();
        branch = loadBranchName();
        file = (new File(file)).toString();
        try {
            if (idStorage.containsKey(id) == false) {
                System.out.println("No commit with that id exists.");
                return;
            }
            if (idStorage.get(id).copy1.containsKey(file) == false) {
                System.out.println("File does not exist in that commit.");
                return;
            }
            Files.copy(idStorage.get(id).copy1.get(file).toPath(), (new File(file)).toPath());
        } 
        catch (IOException e) {
            System.out.println("System failed to find the necessary filepaths.");
        }
        storeTime(firstDate);
        storeUnmarked(unmarkedFiles);
        storeCommstoIds(commtoidStorage);
        storeCounts(count);
        storeTreeMap(files);
        storeIDMap(idStorage);
        storeTimeMap(timeStorage);
        storeCommits(commitList);
        storeBranchMap(branchStorage);
        storeBranchName(branch);
    }

    // switch to a different branch in the program
    // I received help on this from a GSI and ideas from a friend.
    public static void checkout(String branchname) {
        unmarkedFiles = loadUnmarked();
        commtoidStorage = loadCommstoIds();
        count = loadCounts();
        files = loadTreeMap();
        idStorage = loadIDMap();
        timeStorage = loadTimeMap();
        commitList = loadCommits();
        firstDate = loadTime();
        branchStorage = loadBranchMap();
        branch = loadBranchName();
        try {
            if (branchStorage.containsKey(branchname)) {
                if (branch.equals(branchname)) {
                    System.out.println("No need to checkout the current branch.");
                    return;
                }
                branch = branchname;
                current = branchStorage.get(branchname);
                for (String nameOfFile : current.copy1.keySet()) {
                    Files.copy(current.copy1.get(nameOfFile).toPath(), (new File(nameOfFile)).toPath());
                }
            } 
            else {
                branchname = (new File(branchname)).toString();
                if (current.copy1.containsKey(branchname) == false) {
                    System.out.println("File does not exist in the most recent commit, or no such branch exists.");
                    return;
                }
                Files.copy(current.copy1.get(branchname).toPath(),(new File(branchname)).toPath());
            }
        } 
        catch (IOException e) {
            System.out.println("The paths are incorrect.");
        }
        storeTime(firstDate);
        storeUnmarked(unmarkedFiles);
        storeCommstoIds(commtoidStorage);
        storeCounts(count);
        storeTreeMap(files);
        storeIDMap(idStorage);
        storeTimeMap(timeStorage);
        storeCommits(commitList);
        storeBranchMap(branchStorage);
        storeBranchName(branch);
    }

    // create branch at head of given branch
    public static void branch(String branchName) {
        unmarkedFiles = loadUnmarked();
        commtoidStorage = loadCommstoIds();
        count = loadCounts();
        files = loadTreeMap();
        idStorage = loadIDMap();
        timeStorage = loadTimeMap();
        commitList = loadCommits();
        firstDate = loadTime();
        branchStorage = loadBranchMap();
        branch = loadBranchName();
        if (branchStorage.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
        } else {
            branchStorage.put(branchName, commitList.peek());
        }
        storeTime(firstDate);
        storeUnmarked(unmarkedFiles);
        storeCommstoIds(commtoidStorage);
        storeCounts(count);
        storeTreeMap(files);
        storeIDMap(idStorage);
        storeTimeMap(timeStorage);
        storeCommits(commitList);
        storeBranchMap(branchStorage);
        storeBranchName(branch);
    }

    // remove branch reference to commit in stucture
    public static void removeBranch(String branchName) {
        unmarkedFiles = loadUnmarked();
        commtoidStorage = loadCommstoIds();
        count = loadCounts();
        files = loadTreeMap();
        idStorage = loadIDMap();
        timeStorage = loadTimeMap();
        commitList = loadCommits();
        firstDate = loadTime();
        branchStorage = loadBranchMap();
        branch = loadBranchName();
        if (branchStorage.containsKey(branchName) == false) {
            System.out.println("A branch with that name does not exist.");
        } else if (branch.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
        } else {
            branchStorage.remove(branchName);
        }
        storeTime(firstDate);
        storeUnmarked(unmarkedFiles);
        storeCommstoIds(commtoidStorage);
        storeCounts(count);
        storeTreeMap(files);
        storeIDMap(idStorage);
        storeTimeMap(timeStorage);
        storeCommits(commitList);
        storeBranchMap(branchStorage);
        storeBranchName(branch);
    }
    // serialization
    private static TreeMap<String, File> loadTreeMap() {
        files = null;
        File myFile = new File(".gitlet/files.ser");
        if (myFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(myFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                files = (TreeMap) objectIn.readObject();
            } catch (IOException e) {
                String msg = "IOException while loading files.";
                System.out.println(msg);
                System.out.println(e);
            } catch (ClassNotFoundException e) {
                String msg = "ClassNotFoundException while loading files.";
                System.out.println(msg);
                System.out.println(e);
            }
        }
        return files;
    }
    private static void storeTreeMap(TreeMap files) {
        if (files == null) {
            return;
        }
        try {
            File myFile = new File(".gitlet/files.ser");
            FileOutputStream fileOut = new FileOutputStream(myFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(files);
        } catch (IOException e) {
            String msg = "IOException while saving files.";
            System.out.println(msg);
            System.out.println(e);
        }
    }
    private static HashMap<Integer, Commit> loadIDMap() {
        idStorage = null;
        File myFile = new File(".gitlet/idmap.ser");
        if (myFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(myFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                idStorage = (HashMap) objectIn.readObject();
            } catch (IOException e) {
                String msg = "IOException while loading IDMap.";
                System.out.println(msg);
                System.out.println(e);
            } catch (ClassNotFoundException e) {
                String msg = "ClassNotFoundException while loading IDMap.";
                System.out.println(msg);
                System.out.println(e);
            }
        }
        return idStorage;
    }
    private static void storeIDMap(HashMap idStorage) {
        if (idStorage == null) {
            return;
        }
        try {
            File myFile = new File(".gitlet/idmap.ser");
            FileOutputStream fileOut = new FileOutputStream(myFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(idStorage);
        } catch (IOException e) {
            String msg = "IOException while saving IDMap.";
            System.out.println(msg);
            System.out.println(e);
        }
    }
    private static HashMap<Commit, String> loadTimeMap() {
        timeStorage = null;
        File myFile = new File(".gitlet/timemap.ser");
        if (myFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(myFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                timeStorage = (HashMap) objectIn.readObject();
            } catch (IOException e) {
                String msg = "IOException while loading TimeMap.";
                System.out.println(msg);
                System.out.println(e);
            } catch (ClassNotFoundException e) {
                String msg = "ClassNotFoundException while loading TimeMap.";
                System.out.println(msg);
                System.out.println(e);
            }
        }
        return timeStorage;
    }
    private static void storeTimeMap(HashMap timeStorage) {
        if (timeStorage == null) {
            return;
        }
        try {
            File myFile = new File(".gitlet/timemap.ser");
            FileOutputStream fileOut = new FileOutputStream(myFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(timeStorage);
        } catch (IOException e) {
            String msg = "IOException while saving TimeMap.";
            System.out.println(msg);
            System.out.println(e);
        }
    }
    private static LinkedList<Commit> loadCommits() {
        commitList = null;
        File myFile = new File(".gitlet/commits.ser");
        if (myFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(myFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                commitList = (LinkedList) objectIn.readObject();
            } catch (IOException e) {
                String msg = "IOException while loading commits.";
                System.out.println(msg);
                System.out.println(e);
            } catch (ClassNotFoundException e) {
                String msg = "ClassNotFoundException while loading commits.";
                System.out.println(msg);
                System.out.println(e);
            }
        }
        return commitList;
    }
    private static void storeCommits(LinkedList commitList) {
        if (commitList == null) {
            return;
        }
        try {
            File myFile = new File(".gitlet/commits.ser");
            FileOutputStream fileOut = new FileOutputStream(myFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(commitList);
        } catch (IOException e) {
            String msg = "IOException while saving commits.";
            System.out.println(msg);
            System.out.println(e);
        }
    }
    private static int loadCounts() {
        count = 0;
        File myFile = new File(".gitlet/counts.ser");
        if (myFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(myFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                count = (int) objectIn.readObject();
            } catch (IOException e) {
                String msg = "IOException while loading commits.";
                System.out.println(msg);
                System.out.println(e);
            } catch (ClassNotFoundException e) {
                String msg = "ClassNotFoundException while loading commits.";
                System.out.println(msg);
                System.out.println(e);
            }
        }
        return count;
    }
    private static void storeCounts(int count) {
        if (count == 0) {
            return;
        }
        try {
            File myFile = new File(".gitlet/counts.ser");
            FileOutputStream fileOut = new FileOutputStream(myFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(count);
        } catch (IOException e) {
            String msg = "IOException while saving commits.";
            System.out.println(msg);
            System.out.println(e);
        }
    }

    private static HashSet loadUnmarked() {
        unmarkedFiles = null;
        File myFile = new File(".gitlet/unmarkedFiles.ser");
        if (myFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(myFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                unmarkedFiles = (HashSet) objectIn.readObject();
            } catch (IOException e) {
                String msg = "IOException while loading unmarkedFiles.";
                System.out.println(msg);
                System.out.println(e);
            } catch (ClassNotFoundException e) {
                String msg = "ClassNotFoundException while loading unmarkedFiles.";
                System.out.println(msg);
                System.out.println(e);
            }
        }
        return unmarkedFiles;
    }
    private static void storeUnmarked(HashSet unmarkedFiles) {
        if (unmarkedFiles == null) {
            return;
        }
        try {
            File myFile = new File(".gitlet/unmarkedFiles.ser");
            FileOutputStream fileOut = new FileOutputStream(myFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(unmarkedFiles);
        } catch (IOException e) {
            String msg = "IOException while saving unmarkedFiles.";
            System.out.println(msg);
            System.out.println(e);
        }
    }

    private static HashMap loadCommstoIds() {
        commtoidStorage = null;
        File myFile = new File(".gitlet/commtoidStorage.ser");
        if (myFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(myFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                commtoidStorage = (HashMap) objectIn.readObject();
            } catch (IOException e) {
                String msg = "IOException while loading commtoidStorage.";
                System.out.println(msg);
                System.out.println(e);
            } catch (ClassNotFoundException e) {
                String msg = "ClassNotFoundException while loading commtoidStorage.";
                System.out.println(msg);
                System.out.println(e);
            }
        }
        return commtoidStorage;
    }
    private static void storeCommstoIds(HashMap commtoidStorage) {
        if (commtoidStorage == null) {
            return;
        }
        try {
            File myFile = new File(".gitlet/commtoidStorage.ser");
            FileOutputStream fileOut = new FileOutputStream(myFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(commtoidStorage);
        } catch (IOException e) {
            String msg = "IOException while saving commtoidStorage.";
            System.out.println(msg);
            System.out.println(e);
        }
    }

    private static Date loadTime() {
        firstDate = null;
        File myFile = new File(".gitlet/Time.ser");
        if (myFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(myFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                firstDate = (Date) objectIn.readObject();
            } catch (IOException e) {
                String msg = "IOException while loading Time.";
                System.out.println(msg);
                System.out.println(e);
            } catch (ClassNotFoundException e) {
                String msg = "ClassNotFoundException while loading Time.";
                System.out.println(msg);
                System.out.println(e);
            }
        }
        return firstDate;
    }
    private static void storeTime(Date firstDate) {
        if (firstDate == null) {
            return;
        }
        try {
            File myFile = new File(".gitlet/Time.ser");
            FileOutputStream fileOut = new FileOutputStream(myFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(firstDate);
        } catch (IOException e) {
            String msg = "IOException while saving Time.";
            System.out.println(msg);
            System.out.println(e);
        }
    }

    private static HashMap loadBranchMap() {
        branchStorage = null;
        File myFile = new File(".gitlet/BranchStorage.ser");
        if (myFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(myFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                branchStorage = (HashMap) objectIn.readObject();
            } catch (IOException e) {
                String msg = "IOException while loading BranchStorage.";
                System.out.println(msg);
                System.out.println(e);
            } catch (ClassNotFoundException e) {
                String msg = "ClassNotFoundException while loading BranchStorage.";
                System.out.println(msg);
                System.out.println(e);
            }
        }
        return branchStorage;
    }
    private static void storeBranchMap(HashMap branchStorage) {
        if (branchStorage == null) {
            return;
        }
        try {
            File myFile = new File(".gitlet/BranchStorage.ser");
            FileOutputStream fileOut = new FileOutputStream(myFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(branchStorage);
        } catch (IOException e) {
            String msg = "IOException while saving BranchStorage.";
            System.out.println(msg);
            System.out.println(e);
        }
    }
    private static String loadBranchName() {
        branch = null;
        File myFile = new File(".gitlet/Branch.ser");
        if (myFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(myFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                branch = (String) objectIn.readObject();
            } catch (IOException e) {
                String msg = "IOException while loading Branch.";
                System.out.println(msg);
                System.out.println(e);
            } catch (ClassNotFoundException e) {
                String msg = "ClassNotFoundException while loading Branch.";
                System.out.println(msg);
                System.out.println(e);
            }
        }
        return branch;
    }
    private static void storeBranchName(String branch) {
        if (branch == null) {
            return;
        }
        try {
            File myFile = new File(".gitlet/Branch.ser");
            FileOutputStream fileOut = new FileOutputStream(myFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(branch);
        } catch (IOException e) {
            String msg = "IOException while saving Branch.";
            System.out.println(msg);
            System.out.println(e);
        }
    }
}


	




















