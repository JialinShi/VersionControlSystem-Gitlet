package gitlet;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;


import static gitlet.AdditionalUtils.*;

import static gitlet.Utils.*;


/**
 * Represents a gitlet repository. the structure of a gitlet repository .gitlet/ -- top level folder
 * for all persistent data -
 *
 * @author Jialin Shi
 */
public class Repository {

    /**
     * default branch name - master
     */
    private static final String MASTER_BRANCH_NAME = "master";
    /**
     * notate where references are stored
     */
    private static final String HEAD_BRANCH_PREFIX = "ref:refs/heads/";

    /**
     * The current working directory.
     */
    private static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    private static final File GITLETFOLDER = join(CWD, ".gitlet");
    /**
     * The commit directory stores commits
     */
    public static final File COMMITFOLDER = join(GITLETFOLDER, "commits");
    /**
     * The blob directory stores blob
     */
    public static final File BLOBFOLDER = join(GITLETFOLDER, "blobs");
    /**
     * the refs directory
     */
    public static final File REFSFOLDER = join(GITLETFOLDER, "refs");
    /**
     * the heads directory
     */
    public static final File HEADSFOLDER = join(REFSFOLDER, "heads");
    /**
     * The index file
     */
    public static final File INDEX = join(GITLETFOLDER, "index");
    /**
     * The HEAD file
     */
    private static final File HEAD = join(GITLETFOLDER, "HEAD");


    /**
     * Set Head branch. write fullPathBranch into HEAD FILE. Example -- HEAD file: "ref:refs/heads/"
     * + "master"
     *
     * @param branchName String "master"
     */
    private static void setHeadBranch(String branchName) {
        String fullPath = HEAD_BRANCH_PREFIX + branchName;
        writeContents(HEAD, fullPath);
    }

    /**
     * get clean Head BranchName(String) from HEAD file. Example -- HEAD file: "ref:refs/heads/" +
     * "master"
     *
     * @return clean string branchName without prefix, "master"
     */
    public static String getHeadBranchName() {
        String fullCurrentBranch = readContentsAsString(HEAD);
        String branchName = fullCurrentBranch.replace(HEAD_BRANCH_PREFIX, "");
        return branchName;
    }

    /**
     * get HEAD commit. get string branchName from HEAD file, find the Head File, pull commitid from
     * Head file, pull commit using commitId
     *
     * @return Head Commit
     */
    public static Commit getHeadCommit() {
        String branchName = getHeadBranchName();
        File headFile = join(HEADSFOLDER, branchName);
        String headCommitSHA1 = readContentsAsString(headFile);
        Commit returnCommit = Commit.fromFile(headCommitSHA1);
        return returnCommit;
    }

    /**
     * get branch head commit,  find the File with branchName, pull commit with commitid NOT FROM
     * HEAD.
     *
     * @return Commit
     */
    private static Commit getBranchHeadCommit(String branchName) {
        File branch = join(HEADSFOLDER, branchName);
        return getBranchHeadCommit(branch);

    }

    private static Commit getBranchHeadCommit(File branchFile) {
        String branchHeadCommitID = readContentsAsString(branchFile);
        return Commit.fromFile(branchHeadCommitID);
    }

    private static void setBranchHeadCommit(String branchName, String commitID) {
        File branchHeadFile = join(HEADSFOLDER, branchName);
        setBranchHeadCommit(branchHeadFile, commitID);
    }

    private static void setBranchHeadCommit(File branchHeadFile, String commitID) {
        writeContents(branchHeadFile, commitID);
    }


    /**
     * overwrite the commitId in the HEAD commit.
     */
    private static void setHeadCommit(String commitSHA1) {
        String branchName = getHeadBranchName();
        File file = join(HEADSFOLDER, branchName);
        writeContents(file, commitSHA1);
    }


    /**
     * CWD/.gitlet  ---GITLETFOLDER CWD/.gitlet/commits --COMMITFOLDER CWD/.gitlet/refs
     * --COMMITFOLDER
     */
    public static void setupPersistance() {
        GITLETFOLDER.mkdir();
        COMMITFOLDER.mkdir();
        BLOBFOLDER.mkdir();
        REFSFOLDER.mkdir();
        HEADSFOLDER.mkdir();
    }

    public static void checkCWD() {
        if (!(GITLETFOLDER.exists() && GITLETFOLDER.isDirectory())) {
            exit("Not in an initialized Gitlet directory.");
        }
    }

    public static void init() {
        if (GITLETFOLDER.exists()) {
            AdditionalUtils.exit(
                "A Gitlet version-control system already exists in the current directory.");
        }
        setupPersistance();
        Commit initial = new Commit();
        initial.saveCommit();
        setHeadBranch(MASTER_BRANCH_NAME);
        initialCommitToMasterBranch(initial);
    }

    /**
     * create specified head Branch file in HEADSFOLDER,write commitID of head branch into it
     */
    private static void initialCommitToMasterBranch(Commit initial) {
        File newFile = join(HEADSFOLDER, MASTER_BRANCH_NAME);
        writeContents(newFile, initial.getCommitSHA1());
    }

    /**
     * add file to staging area.
     */
    public static void add(String fileName) {
        File newFile = getFilePathFromCWD(fileName);
        if (!newFile.exists()) {
            exit("File does not exist.");
        }
        StagingArea stagingArea = getStagingArea();
        if (stagingArea.add(newFile)) {
            stagingArea.saveStaging();
        }
    }

    /**
     * pull stagingArea if exists. if not create new one with HeadCommit tracked Map
     *
     * @return StagingArea
     */
    public static StagingArea getStagingArea() {
        StagingArea stagingArea;

        if (INDEX.exists()) {
            stagingArea = StagingArea.fromFile();
        } else {
            stagingArea = new StagingArea();
        }
        Map<String, String> newTracked = getHeadCommit().getTracked();
        stagingArea.setTracked(newTracked);
        return stagingArea;
    }

    /**
     * create file path. Check if fileName is absolute or not.
     *
     * @return a file
     */
    private static File getFilePathFromCWD(String fileName) {
        if (Paths.get(fileName).isAbsolute()) {
            return new File(fileName);
        } else {
            return join(CWD, fileName);
        }
    }

    /**
     * Get a map of file paths and their Blob shaID in CWD.
     *
     * @return a map
     */
    private static Map<String, String> getCWDFileMap() {
        Map<String, String> map = new HashMap<>();
        File[] fileNames = CWD.listFiles(File::isFile);
        for (File file : fileNames) {
            String fp = file.getPath();
            String blobID = Blob.generateBlobSHA1(file);
            map.put(fp, blobID);
        }
        return map;
    }

    public static void commit(String message) {
        StagingArea stagingArea = getStagingArea();
        if (stagingArea.isClean()) {
            exit("No changes added to the commit.");
        }
        Map<String, String> tracked = stagingArea.commit();
        stagingArea.saveStaging();
        Commit newCommit = createNewCommit(message, tracked);
        newCommit.saveCommit();
        setHeadCommit(newCommit.getCommitSHA1());
    }

    private static void mergeCommit(String message, String mergedCommitId) {
        StagingArea stagingArea = getStagingArea();
        if (stagingArea.isClean()) {
            exit("No changes added to the commit.");
        }
        Map<String, String> tracked = stagingArea.commit();
        stagingArea.saveStaging();
        Commit newCommit = createMergedCommit(message, tracked, mergedCommitId);
        newCommit.saveCommit();
        setHeadCommit(newCommit.getCommitSHA1());
    }

    /**
     * create a new commit with HeadCommitID as parent,new message, and tracked map returned by
     * stagingArea.commit()
     *
     * @return new commit
     */
    private static Commit createNewCommit(String message, Map<String, String> tracked) {
        Commit headCommit = getHeadCommit();
        List<String> parent = new ArrayList<>();
        parent.add(headCommit.getCommitSHA1());
        Commit newCommit = new Commit(message, parent, tracked);
        return newCommit;
    }

    private static Commit createMergedCommit(String message, Map<String, String> tracked,
        String secondParentId) {
        Commit headCommit = getHeadCommit();
        List<String> parent = new ArrayList<>();
        parent.add(headCommit.getCommitSHA1());
        parent.add(secondParentId);
        Commit newCommit = new Commit(message, parent, tracked);
        return newCommit;
    }

    public static void rm(String fileName) {
        File file = getFilePathFromCWD(fileName);
        StagingArea stagingArea = getStagingArea();

        if (!stagingArea.remove(file)) {
            exit("No reason to remove the file.");
        } else {
            stagingArea.saveStaging();
        }
    }

    public static void log() {
        StringBuilder log = new StringBuilder();
        Commit commit = getHeadCommit();
        while (true) {
            log.append(commit.getLog()).append("\n");
            List<String> parent = commit.getParent();
            if (parent.size() == 0) {
                break;
            }
            String parentId = parent.get(0);
            commit = Commit.fromFile(parentId);
        }
        System.out.print(log);
    }

    public static void globalLog() {
        StringBuilder log = new StringBuilder();
        String[] commitIDs = getAllCommitId();

        for (String commitID : commitIDs) {
            Commit commit = Commit.fromFile(commitID);
            log.append(commit.getLog()).append("\n");
        }
        System.out.println(log);
    }


    public static void find(String message) {
        StringBuilder messageMatched = new StringBuilder();
        String[] commitIDs = getAllCommitId();
        if (commitIDs.length == 0) {
            exit("Found no commit with that message.");
        }
        for (String commitID : commitIDs) {
            Commit commit = Commit.fromFile(commitID);
            if (commit.getMessage().equals(message)) {
                messageMatched.append(commitID).append("\n");
            }
        }
        if (messageMatched.length() == 0) {
            exit("Found no commit with that message.");
        }
        System.out.println(messageMatched);
    }

    private static String[] getAllCommitId() {
        Set<String> commitFullSHAs = new HashSet<>();
        String[] commitDirs = COMMITFOLDER.list();
        if (commitDirs == null) {
            return new String[0];
        }
        for (String shaDir : commitDirs) {
            String[] commitFileNames = join(COMMITFOLDER, shaDir).list();
            if (commitFileNames == null) {
                continue;
            }
            for (String fileName : commitFileNames) {
                commitFullSHAs.add(shaDir + fileName);
            }
        }
        return commitFullSHAs.toArray(new String[0]);
    }

    public static void status() {
        Status.getGlobalStatus();
    }

    public static void checkoutFileName(String fileName) {
        String filePath = getFilePathFromCWD(fileName).getPath();
        if (!getHeadCommit().restoreBlob(filePath)) {
            exit("File does not exist in that commit.");
        }
    }

    public static void checkoutCommitID(String commitSHA1, String fileName) {
        Commit foundCommit = getCommitWithShorterUID(commitSHA1);
        if (foundCommit == null) {
            exit("No commit with that id exists.");
        }

        String filePath = getFilePathFromCWD(fileName).getPath();
        if (!foundCommit.restoreBlob(filePath)) {
            exit("File does not exist in that commit.");
        }
    }

    public static Commit getCommitWithShorterUID(String commitId) {
        Commit foundCommit;
        if (commitId.length() < UID_LENGTH) {
            if (commitId.length() < 4) {
                exit("Commit id should contain at least 4 characters.");
            }
            String dir = getShaDir(commitId);
            File dirFile = join(COMMITFOLDER, dir);
            if (!dirFile.exists()) {
                exit("No commit with that id exists.");
            }
            boolean isFound = false;
            String shortFileName = getShaFileName(commitId);
            for (File file : dirFile.listFiles()) {
                String longFileName = file.getName();
                if (longFileName.startsWith(shortFileName)) {
                    if (isFound) {
                        exit("More than 1 commit has the same id prefix.");
                    } else {
                        commitId = dir + longFileName;
                        isFound = true;
                    }
                }
            }
            if (!isFound) {
                exit("No commit with that id exists.");
            }
        }
        foundCommit = Commit.fromFile(commitId);
        return foundCommit;
    }


    public static void checkoutBranch(String branchName) {
        File file = join(HEADSFOLDER, branchName);
        if (!file.exists()) {
            exit("No such branch exists.");
        }
        if (getHeadBranchName().equals(branchName)) {
            exit("No need to checkout the current branch.");
        }

        Commit branchHeadCommit = getBranchHeadCommit(branchName);
        untrackedFileExists(branchHeadCommit); // check if status is clean
        restoreBranchCommit(branchHeadCommit);
        setHeadBranch(branchName);
    }


    /**
     * check if untrackedFile exists exit with Message if there found untrackedFiles
     */
    private static void untrackedFileExists(Commit branchCommit) {
        StagingArea stagingArea = getStagingArea();

        Map<String, String> cwdFiles = getCWDFileMap();
        Map<String, String> tracked = getHeadCommit().getTracked();
        Map<String, String> stagedForAddition = stagingArea.getAddedStagingFile();
        Set<String> stagedForRemoval = stagingArea.getRemovedStagingFile();

        List<String> untrackedFile = new ArrayList<>();

        for (String file : cwdFiles.keySet()) { //for each file in CWD
            if (tracked.containsKey(file)) { //if tracked has this file
                if (stagedForRemoval.contains(file)) { //but it is staged for removal
                    untrackedFile.add(file);
                }
            } else { //it is not tracked
                if (!stagedForAddition.containsKey(file)) { //and not staged for addition
                    untrackedFile.add(file);
                }
            }
        }

        Map<String, String> branchCommitTracked = branchCommit.getTracked();
        for (String file : untrackedFile) {
            String blobId = getCWDFileMap().get(file);
            String branchBlobId = branchCommitTracked.get(file);
            if (!blobId.equals(branchBlobId)) {
                exit(
                    "There is an untracked file in the way; delete it, or add and commit it first."
                );
            }
        }
    }


    private static void restoreBranchCommit(Commit branchCommit) {
        //Clear current StagingArea
        StagingArea stagingArea = getStagingArea();
        stagingArea.clear();
        stagingArea.saveStaging();

        // clear CWD
        File[] files = CWD.listFiles(File::isFile);
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        branchCommit.restoreAllTracked();
    }


    public static void branch(String newBranchName) {
        File newBranch = join(HEADSFOLDER, newBranchName);
        if (newBranch.exists()) {
            exit("A branch with that name already exists.");
        }
        setBranchHeadCommit(newBranchName, getHeadCommit().getCommitSHA1());
    }

    public static void rmBranch(String branchToBeRemove) {
        File branchToRemove = join(HEADSFOLDER, branchToBeRemove);
        if (!branchToRemove.exists()) {
            exit("A branch with that name does not exist.");
        }
        if (branchToBeRemove.equals(getHeadBranchName())) {
            exit("Cannot remove the current branch.");
        }
        branchToRemove.delete();
    }

    public static void reset(String commitSHAId) {
        File givenCommitFile = getObjectFile(commitSHAId);
        if (!givenCommitFile.exists()) {
            exit("No commit with that id exists.");
        }
        Commit givenCommit = Commit.fromFile(commitSHAId);
        untrackedFileExists(givenCommit);
        restoreBranchCommit(givenCommit);
        setHeadCommit(commitSHAId);
    }

    private static void mergeFailureCheck(String branchName, StagingArea stagingArea) {
        //Failure check
        if (!stagingArea.isClean()) {
            exit("You have uncommitted changes.");
        }
        File branchFile = join(HEADSFOLDER, branchName);
        if (!branchFile.exists()) {
            exit("A branch with that name does not exist.");
        }
        if (branchName.equals(getHeadBranchName())) {
            exit("Cannot merge a branch with itself.");
        }
    }

    private static void mergeSplitCommitCheck(String branchName,
        String splitCommitID, Commit currentHead, Commit branchHead) {
        if (splitCommitID.equals(branchHead.getCommitSHA1())) {
            exit("Given branch is an ancestor of the current branch.");
        }
        if (splitCommitID.equals(currentHead.getCommitSHA1())) {
            restoreBranchCommit(branchHead);
            setHeadBranch(branchName);
            exit("Current branch fast-forwarded.");
        }
    }

    public static void merge(String branchName) {
        StagingArea stagingArea = getStagingArea();
        mergeFailureCheck(branchName, stagingArea); //Failure check

        Commit currentHead = getHeadCommit();
        Commit branchHead = getBranchHeadCommit(branchName);

        untrackedFileExists(branchHead); // check if there is an untracked file
        Commit splitCommit = findSplitCommit(currentHead, branchHead);
        String splitCommitID = splitCommit.getCommitSHA1();
        mergeSplitCommitCheck(branchName, splitCommitID, currentHead, branchHead);

        boolean hasConflict = false;
        Map<String, String> splitMap = splitCommit.getTracked();
        Map<String, String> currentHeadMap = currentHead.getTracked();
        Map<String, String> branchHeadMap = branchHead.getTracked();
        for (Map.Entry<String, String> file : splitMap.entrySet()) {
            String filePath = file.getKey();
            File fileFile = new File(filePath);
            String blobID = file.getValue();
            String currentBlobID = currentHeadMap.get(filePath);
            String branchBlobID = branchHeadMap.get(filePath);
            if (currentBlobID == null) { /** A-X */
                if (branchBlobID != null && !branchBlobID.equals(blobID)) { //case8c A-X-!A
                    hasConflict = true;
                    String conflictContent = getConflict(null, branchBlobID);
                    writeContents(fileFile, conflictContent);
                    stagingArea.add(fileFile);
                } // continue on case3 A-X-X && case5 A-X-A.
            } else if (currentBlobID.equals(blobID)) { /** A-A */
                if (branchBlobID == null) { //case4 A-A-X
                    stagingArea.remove(fileFile);
                } else if (!branchBlobID.equals(blobID)) { //case1 A-A-!A
                    Blob.fromFile(branchBlobID).writeBlobContentsToSource();
                    stagingArea.add(fileFile);
                }
            } else { /** A-!A */
                if (branchBlobID == null) { //case8b A-!A-X
                    hasConflict = true;
                    String conflictContent = getConflict(currentBlobID, null);
                    writeContents(fileFile, conflictContent);
                    stagingArea.add(fileFile);
                } else if (!branchBlobID.equals(currentBlobID) && !branchBlobID.equals(
                    blobID)) { //case8a A-!A-!!A
                    hasConflict = true;
                    String conflictContent = getConflict(currentBlobID, branchBlobID);
                    writeContents(fileFile, conflictContent);
                    stagingArea.add(fileFile);
                } // Continue on case2 A-!A-A & case8d A-!A-!A
            }
        }
        for (Map.Entry<String, String> file : branchHeadMap.entrySet()) {
            String branchFileName = file.getKey();
            File fileFile = new File(branchFileName);
            String branchBlobID = file.getValue();

            String splitBlobID = splitMap.get(branchFileName);
            String currentBlobID = currentHeadMap.get(branchFileName);

            if (splitBlobID != null) {
                continue;
            }
            if (currentBlobID == null) {
                Blob.fromFile(branchBlobID).writeBlobContentsToSource();
                stagingArea.add(fileFile);
            } else if (!currentBlobID.equals(branchBlobID)) {
                hasConflict = true;
                String conflictContent = getConflict(currentBlobID, branchBlobID);
                writeContents(fileFile, conflictContent);
                stagingArea.add(fileFile);
            }
        }
        String mergingCommitMessage = "Merged " + branchName + " into " + getHeadBranchName() + ".";
        stagingArea.saveStaging();
        mergeCommit(mergingCommitMessage, branchHead.getCommitSHA1());
        if (hasConflict) {
            message("Encountered a merge conflict.");
        }
    }

    /**
     * Get the latest common commit of CommitA and CommitB
     *
     * @return the latest common Commit
     */
    private static Commit findSplitCommit(Commit currentHead, Commit branchHead) {
        Comparator<Commit> commitComparator = Comparator.comparing(
            commit -> parseTimestamp(commit.pullTimestamp()), Comparator.reverseOrder());

        Queue<Commit> commitQue = new PriorityQueue<>(commitComparator);
        Set<String> seenCommit = new HashSet<>();

        commitQue.add(currentHead);
        commitQue.add(branchHead);
        seenCommit.add(currentHead.getCommitSHA1());
        seenCommit.add(branchHead.getCommitSHA1());

        while (true) {
            Commit latestCommit = commitQue.poll();
            List<String> parents = latestCommit.getParent();
            for (String parentID : parents) {
                Commit parentCommit = Commit.fromFile(parentID);
                if (seenCommit.contains(parentID)) {
                    return parentCommit;
                }
                commitQue.add(parentCommit);
                seenCommit.add(parentID);
            }
        }
    }

    private static String getConflict(String blobId1, String blobId2) {
        StringBuilder conflictContent = new StringBuilder();
        conflictContent.append("<<<<<<< HEAD").append("\n");
        if (blobId1 != null) {
            Blob blob1 = Blob.fromFile(blobId1);
            conflictContent.append(blob1.pullContentsAsString());

        }
        conflictContent.append("=======").append("\n");
        if (blobId2 != null) {
            Blob blob2 = Blob.fromFile(blobId2);
            conflictContent.append(blob2.pullContentsAsString());
        }
        conflictContent.append(">>>>>>>").append("\n");
        return conflictContent.toString();
    }

}
