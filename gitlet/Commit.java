package gitlet;


import static gitlet.Utils.*;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static gitlet.AdditionalUtils.*;


/**
 * Represents a gitlet commit object.
 *
 * @author Jialin Shi
 */
public class Commit implements Serializable {


    /**
     * sha1 ID of commits
     */
    private final String commitSHA1;

    /**
     * Date of commits
     */
    private final String timestamp;
    /**
     * The message of this Commit.
     */
    private final String message;
    /**
     * The parent commits, this will create pointers, and thus everytime we save a file, we are
     * saving a tree think about treating strings as pointers to the files
     */
    private final List<String> parent;
    /**
     * Blob map The tracked files Map with blob file path as key and BlobSHA1 id as value.
     */
    private final Map<String, String> tracked;

    /**
     * The file of this instance with the path generated from SHA1 id For example, a file path:
     * CWD/.gitlet/commits/66/xxed78g
     */
    private final File file;

    /**
     * Construct commit with message and parent
     */
    public Commit(String message, List<String> parent, Map<String, String> trackedFilesMap) {
        this.timestamp = getTimestamp(new Date());
        this.message = message;
        this.parent = parent;
        this.tracked = trackedFilesMap;
        this.commitSHA1 = generateSHA1();
        this.file = getObjectFile(commitSHA1);
    }

    /**
     * initial commit with no Args -- no parent, fixed timestamp - ;
     */
    public Commit() {
        this.timestamp = getTimestamp(new Date(0));
        this.message = "initial commit";
        this.parent = new ArrayList<>();
        this.tracked = new HashMap<>();
        this.commitSHA1 = generateSHA1();
        this.file = getObjectFile(commitSHA1);
    }

    private String getTimestamp(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
        return dateFormat.format(date);
    }

    /**
     * Generate a SHA1 id from timestamp, message, parents Array and tracked files Map.
     *
     * @return SHA1 id
     */
    private String generateSHA1() {
        return sha1(timestamp, message, parent.toString(), tracked.toString());
    }

    public String getMessage() {
        return this.message;
    }

    public String pullTimestamp() {
        return timestamp;
    }

    public String getCommitSHA1() {
        return commitSHA1;
    }

    public List<String> getParent() {
        return this.parent;
    }

    /**
     * return Blob map<filepath, BlobSHA1>
     */
    public Map<String, String> getTracked() {
        return tracked;
    }

    /**
     * Save a commit to a file for future use.
     */
    public void saveCommit() {
        saveObject(file, this);
    }

    /**
     * Reads in and deserializes a commit from a file with commitSHA1 in CommitFolder.
     *
     * @param commitSHA1 of commit to load
     * @return commit read from file
     */
    public static Commit fromFile(String commitSHA1) {
        File commitFile = getObjectFile(commitSHA1);
        if (commitFile.exists()) {
            return readObject(commitFile, Commit.class);
        } else {
            return null;
        }
    }

    /**
     * tracked: Map<String filepath, String BlobSHA1></> restore the blob if exists
     *
     * @param filePath path of the blob file
     * @return return true if exists in commit
     */

    public boolean restoreBlob(String filePath) {
        String blobSHA1 = tracked.get(filePath);
        if (blobSHA1 == null) { // means no such blob
            return false;
        }
        Blob.fromFile(blobSHA1).writeBlobContentsToSource();
        return true;
    }

    public void restoreAllTracked() {
        for (String blobID : tracked.values()) {
            Blob.fromFile(blobID).writeBlobContentsToSource();
        }
    }


    /**
     * print sha1, parent sha1, date, message
     */
    public String getLog() {
        StringBuilder log = new StringBuilder();
        log.append("===").append("\n");
        log.append("commit " + commitSHA1).append("\n");
        if (parent.size() > 1) {
            log.append(
                    "Merge: " + parent.get(0).substring(0, 7) + " " + parent.get(1).substring(0, 7))
                .append("\n");
        }
        log.append("Date: " + timestamp).append("\n");
        log.append(message + "\n");
        return log.toString();
    }
}



